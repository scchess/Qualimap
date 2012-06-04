package org.bioinfo.ngs.qc.qualimap.process;

import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalTree;
import net.sf.samtools.*;
import net.sf.samtools.util.CoordMath;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.bioinfo.formats.exception.FileFormatException;
import org.bioinfo.ngs.qc.qualimap.utils.GenomicRegionSet;
import org.bioinfo.ngs.qc.qualimap.utils.GtfParser;
import org.bioinfo.ngs.qc.qualimap.utils.LoggerThread;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by kokonech
 * Date: 12/12/11
 * Time: 2:52 PM
 */

public class ComputeCountsTask  {

    Map<String,Double> readCounts;
    Map<String, GenomicRegionSet> chromosomeRegionSetMap;
    MultiMap<String, Interval> featureIntervalMap;
    ArrayList<String> allowedFeatureList;
    String protocol;
    String countingAlgorithm;
    String attrName;
    LoggerThread logger;

    String pathToBamFile, pathToGffFile;

    long notAligned, alignmentNotUnique, noFeature, ambiguous;

    public static final String PROTOCOL_NON_STRAND_SPECIFIC = "non-strand-specific";
    public static final String PROTOCOL_FORWARD_STRAND = "forward-stranded";
    public static final String PROTOCOL_REVERSE_STRAND = "reverse-stranded";
    public static final String GENE_ID_ATTR = "gene_id";
    public static final String EXON_TYPE_ATTR = "exon";
    public static final String COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED = "uniquely-mapped-reads";
    public static final String COUNTING_ALGORITHM_PROPORTIONAL = "proportional";

    public ComputeCountsTask(String pathToBamFile, String pathToGffFile) {
        this.pathToBamFile = pathToBamFile;
        this.pathToGffFile = pathToGffFile;
        this.attrName = GENE_ID_ATTR;
        protocol = PROTOCOL_NON_STRAND_SPECIFIC;
        countingAlgorithm = COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED;
        allowedFeatureList = new ArrayList<String>();
        featureIntervalMap = new MultiHashMap<String, Interval>();

        logger = new LoggerThread() {
            @Override
            public void logLine(String msg) {
                System.out.println(msg);
            }
        };

    }

    public void addSupportedFeatureType(String featureName) {
        allowedFeatureList.add(featureName);
    }

    public void setProtocol(String protocol) {
        this.protocol =  protocol;
    }

    public void setLogger(LoggerThread thread) {
        this.logger = thread;
    }

    public void run() throws FileFormatException, IOException, NoSuchMethodException {

        if (allowedFeatureList.isEmpty()) {
            // default feature to consider
            addSupportedFeatureType("exon");
        }


        loadRegions();

        logger.logLine("Starting BAM file analysis\n");

        SAMFileReader reader = new SAMFileReader(new File(pathToBamFile));

        SAMRecordIterator iter = reader.iterator();

        boolean strandSpecificAnalysis = !protocol.equals(PROTOCOL_NON_STRAND_SPECIFIC);

        int readCount = 0;

        while (iter.hasNext()) {

            SAMRecord read = iter.next();

            if (read == null || read.getReadUnmappedFlag()) {
                notAligned++;
                continue;
            }

            readCount++;

            double readWeight = 1.0;
            int nh = 1;
            try {
                nh = read.getIntegerAttribute("NH");
            } catch (NullPointerException ex) {
                //System.err.println("The read " + read.getReadName() + " doesn't have NH attribute");
            }
            if (nh > 1) {
                if (countingAlgorithm.equals(COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)) {
                    alignmentNotUnique++;
                    continue;
                } else if (countingAlgorithm.equals(COUNTING_ALGORITHM_PROPORTIONAL)) {
                    readWeight = 1.0 / nh;
                }
            }

            String chrName = read.getReferenceName();
            boolean pairedRead = read.getReadPairedFlag();

            GenomicRegionSet regionSet = chromosomeRegionSetMap.get(chrName);

            if (regionSet == null ) {
                System.err.println("Unknown chromosome: " + chrName);
                continue;
            }

            //Debugging  purposes
            //System.out.print("ReadName: "+read.getReadName() );
            //System.out.println("ReadStart: "+read.getAlignmentStart() + ", ReadEnd: " + read.getAlignmentEnd());
            /*if (read.getReadName().contains("SRR002320.11647971") ) {
                System.out.println("BINGO!");
            }*/

            // Create intervals for read
            Cigar cigar = read.getCigar();
            List<CigarElement> cigarElements = cigar.getCigarElements();
            List<Interval> intervals = new ArrayList<Interval>();
            int offset = read.getAlignmentStart();
            for (CigarElement cigarElement : cigarElements) {
                int length = cigarElement.getLength();
                boolean strand = read.getReadNegativeStrandFlag();
                if (pairedRead) {
                    boolean firstOfPair = read.getFirstOfPairFlag();
                    if ( (protocol.equals(PROTOCOL_FORWARD_STRAND) && !firstOfPair) ||
                            (protocol.equals(PROTOCOL_REVERSE_STRAND) && firstOfPair) ) {
                        strand = !strand;
                    }
                }

                if (cigarElement.getOperator().equals(CigarOperator.M) ) {
                    intervals.add(new Interval(chrName, offset, offset + length - 1, strand, "" ));
                }
                offset += length;
            }


            //Find intersections

            HashMap<String,BitSet> featureIntervalMap = new HashMap<String, BitSet>();
            int intIndex = 0;

            for (Interval interval : intervals) {
                Iterator<IntervalTree.Node<Set<GenomicRegionSet.Feature>>> overlapIter = regionSet.overlappers(interval.getStart(), interval.getEnd() );
                while (overlapIter.hasNext()) {
                    IntervalTree.Node<Set<GenomicRegionSet.Feature>> node = overlapIter.next();

                    if (CoordMath.encloses(node.getStart(), node.getEnd(), interval.getStart(), interval.getEnd()) ) {

                        Set<GenomicRegionSet.Feature> features = node.getValue();
                        for (GenomicRegionSet.Feature feature : features) {
                            String featureName = feature.getName();

                            BitSet intervalBits = featureIntervalMap.get(featureName);
                            if (intervalBits == null) {
                                intervalBits = new BitSet(intervals.size());
                                featureIntervalMap.put(feature.getName(), intervalBits);
                            }

                            boolean includeInterval = true;
                            if (strandSpecificAnalysis) {
                                boolean featureStrand = feature.isPositiveStrand();
                                includeInterval = featureStrand == interval.isPositiveStrand();
                            }

                            intervalBits.set(intIndex, includeInterval);
                        }
                    }
                }
                intIndex++;
            }


            Set<String> features = new HashSet<String>();

            /*if (featureIntervalMap.keySet().contains("ENSG00000214827")) {
                System.out.println("AKALAI MAKALAI!" + featureIntervalMap.keySet() + read.getReadName());
            }*/

            for (Map.Entry<String,BitSet> entry : featureIntervalMap.entrySet() ) {
                if (entry.getValue().cardinality() == intervals.size() ) {
                    features.add(entry.getKey());
                }
            }

            if (features.size()  == 0) {
                noFeature++;
            } else if (features.size()  == 1) {
                //if (features.iterator().next().contains("ENSG00000124222"))  {
                //    System.out.println(read.getReadName());
                //}
                String geneName = features.iterator().next();
                double count = readCounts.get(geneName);
                readCounts.put(geneName, count  + readWeight);
            }   else {
                ambiguous++;
            }

            if (readCount % 500000 == 0) {
                logger.logLine("Analyzed " + readCount + " reads...");
            }

        }

        if (readCount == 0) {
            throw new RuntimeException("BAM file is empty.");
        }
        logger.logLine("\nProcessed " + readCount + " reads in total");
        logger.logLine("\nBAM file analysis finished");



    }


    void loadRegions() throws IOException, NoSuchMethodException, FileFormatException {

        GtfParser gtfParser = new GtfParser(pathToGffFile);
		logger.logLine("Initializing regions from " + pathToGffFile + "...\n");

        chromosomeRegionSetMap =  new HashMap<String, GenomicRegionSet>();
        readCounts = new HashMap<String, Double>();


        GtfParser.Record record;
        int recordCount = 0;
        while((record = gtfParser.readNextRecord())!=null){

            for (String featureType: allowedFeatureList) {
                // TODO: consider different type of features here

                recordCount++;
                if (recordCount % 100000 == 0) {
                    logger.logLine("Initialized " + recordCount + " regions...");
                }
                if (record.getFeature().equalsIgnoreCase(featureType)) {
                    addRegionToIntervalMap(record);
                    // init results map
                    readCounts.put(record.getAttribute(attrName), 0.0);
                    break;
                }




            }
        }

        if (chromosomeRegionSetMap.isEmpty()) {
            throw new RuntimeException("Unable to load any regions from file.");
        }

        logger.logLine("\nInitialized " + recordCount + " regions it total\n\n");

        gtfParser.close();

    }

    void addRegionToIntervalMap(GtfParser.Record r) {

        GenomicRegionSet regionSet = chromosomeRegionSetMap.get(r.getSeqName());
        if (regionSet == null) {
            regionSet = new GenomicRegionSet();
            chromosomeRegionSetMap.put(r.getSeqName(), regionSet);
        }

        regionSet.addRegion(r, attrName);

    }

    public Map<String,Double> getReadCounts() {
        return readCounts;
    }

    public long getNotAlignedNumber() {
        return notAligned;
    }

    public long getNoFeatureNumber() {
        return noFeature;
    }

    public long getAlignmentNotUniqueNumber() {
        return alignmentNotUnique;
    }

    public long getAmbiguousNumber() {
        return ambiguous;
    }


    public StringBuilder getOutputStatsMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Feature \"").append(attrName).append("\" counts: ").append(getTotalReadCounts()).append("\n");
        message.append("No feature: ").append(noFeature).append("\n");
        message.append("Not unique alignment: ");
        if (countingAlgorithm.equals(COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)){
            message.append(alignmentNotUnique).append("\n");
        } else {
            message.append("NA\n");
        }
        message.append("Ambiguous: ").append(ambiguous).append("\n");

        return message;
    }

    public long getTotalReadCounts() {
        long totalCount = 0;
        for ( Double count: readCounts.values()) {
            totalCount += count;
        }

        return totalCount;
    }


    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public void setCountingAlgorithm(String countingAlgorithm) {
        this.countingAlgorithm = countingAlgorithm;
    }
}
