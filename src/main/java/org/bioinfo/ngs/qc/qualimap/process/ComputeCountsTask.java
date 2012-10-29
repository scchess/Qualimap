/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
 * http://qualimap.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.bioinfo.ngs.qc.qualimap.process;


import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalTree;
import net.sf.samtools.*;
import net.sf.samtools.util.CoordMath;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.bioinfo.formats.exception.FileFormatException;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.common.*;

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
    TranscriptDataHandler transcriptDataHandler;
    String protocol;
    String countingAlgorithm;
    String attrName;
    LoggerThread logger;
    boolean calcCoverageBias;
    boolean loadGenericRegions;
    boolean outputCoverage;

    String pathToBamFile, pathToGffFile;

    long notAligned, alignmentNotUnique, noFeature, ambiguous;

    public static final String GENE_ID_ATTR = "gene_id";
    public static final String EXON_TYPE_ATTR = "exon";
    public static final String COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED = "uniquely-mapped-reads";
    public static final String COUNTING_ALGORITHM_PROPORTIONAL = "proportional";

    public ComputeCountsTask(String pathToBamFile, String pathToGffFile) {
        this.pathToBamFile = pathToBamFile;
        this.pathToGffFile = pathToGffFile;
        this.attrName = GENE_ID_ATTR;
        protocol = LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC;
        countingAlgorithm = COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED;
        allowedFeatureList = new ArrayList<String>();
        featureIntervalMap = new MultiHashMap<String, Interval>();
        calcCoverageBias = false;
        loadGenericRegions = false;
        outputCoverage = true;

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

    public void setCalcCoverageBias(boolean calcCoverageBias) {
        this.calcCoverageBias = calcCoverageBias;
    }

    public void run() throws Exception {

        initRegions();

        logger.logLine("Starting BAM file analysis\n");

        SAMFileReader reader = new SAMFileReader(new File(pathToBamFile));

        SAMRecordIterator iter = reader.iterator();

        boolean strandSpecificAnalysis = !protocol.equals(LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC);

        int readCount = 0;
        int seqNotFoundCount = 0;

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
                seqNotFoundCount++;
                System.err.println("Chromosome " + chrName + " from read is not found in annotations.");
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
            boolean strand = read.getReadNegativeStrandFlag();
            if (pairedRead) {
                boolean firstOfPair = read.getFirstOfPairFlag();
                if ( (protocol.equals(LibraryProtocol.PROTOCOL_FORWARD_STRAND) && !firstOfPair) ||
                        (protocol.equals(LibraryProtocol.PROTOCOL_REVERSE_STRAND) && firstOfPair) ) {
                    strand = !strand;
                }
            }

            for (CigarElement cigarElement : cigarElements) {
                int length = cigarElement.getLength();

                if ( cigarElement.getOperator().equals(CigarOperator.M)  ) {
                    intervals.add(new Interval(chrName, offset, offset + length - 1, strand, "" ));
                }
                offset += length;
            }


            //Find intersections

            HashMap<String,BitSet> featureIntervalMap = new HashMap<String, BitSet>();
            int intIndex = 0;

            for (Interval alignmentInterval : intervals) {
                Iterator<IntervalTree.Node<Set<GenomicRegionSet.Feature>>> overlapIter
                        = regionSet.overlappers(alignmentInterval.getStart(), alignmentInterval.getEnd() );
                while (overlapIter.hasNext()) {
                    IntervalTree.Node<Set<GenomicRegionSet.Feature>> node = overlapIter.next();

                    if (CoordMath.encloses(node.getStart(), node.getEnd(),
                            alignmentInterval.getStart(), alignmentInterval.getEnd()) ) {

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
                                includeInterval = featureStrand == alignmentInterval.isPositiveStrand();
                            }

                            intervalBits.set(intIndex, includeInterval);
                        }
                        if (calcCoverageBias) {
                            for (GenomicRegionSet.Feature feature : features) {
                                transcriptDataHandler.addCoverage(feature.getName(),
                                alignmentInterval.getStart(), alignmentInterval.getEnd() );
                            }
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

        if (seqNotFoundCount + alignmentNotUnique == readCount) {
            throw new RuntimeException("The BAM file and annotations file have no intersections. " +
                    "Check sequence names for consistency.");
        }


        if (calcCoverageBias) {
            transcriptDataHandler.calculateCoverageBias();
        }

        logger.logLine("\nProcessed " + readCount + " reads in total");
        logger.logLine("\nBAM file analysis finished");



    }

    void initRegions() throws Exception {

        FeatureFileFormat format = DocumentUtils.guessFeaturesFileFormat(pathToGffFile);

        if (format == FeatureFileFormat.UNKNOWN) {
            throw new RuntimeException("Failed to detect annotations file format.");
        }

        if (format == FeatureFileFormat.GTF) {
            loadRegionsFromGTF();
        } else {
            loadGenericRegions(format);
        }

    }

    void loadGenericRegions(FeatureFileFormat format) throws Exception {

        logger.logLine("Detected non-GTF annotations file. The counting will " +
                "be performed based only on feature name");

         if (calcCoverageBias) {
            throw new RuntimeException("Calculating coverage bias is only available for GTF files. " +
                    "Please change your annotations file.");
         }

        GenomicFeatureStreamReader parser = new GenomicFeatureStreamReader(pathToGffFile, format);
		logger.logLine("Initializing regions from " + pathToGffFile + "...\n");

        chromosomeRegionSetMap =  new HashMap<String, GenomicRegionSet>();
        readCounts = new HashMap<String, Double>();

        GenomicFeature record;
        int recordCount = 0;
        while((record = parser.readNextRecord())!=null){
            recordCount++;
            if (recordCount % 100000 == 0) {
                logger.logLine("Initialized " + recordCount + " regions...");
            }
            addRegionToIntervalMap(record,false);

            // init results map
            readCounts.put(record.getFeatureName(), 0.0);
}

        if (chromosomeRegionSetMap.isEmpty()) {
            throw new RuntimeException("Unable to load any regions from file.");
        }

        logger.logLine("\nInitialized " + recordCount + " regions it total\n\n");
        loadGenericRegions = true;

        parser.close();
    }

    void loadRegionsFromGTF() throws IOException, NoSuchMethodException, FileFormatException {

        if (allowedFeatureList.isEmpty()) {
            // default feature to consider
            addSupportedFeatureType("exon");
        }

        GenomicFeatureStreamReader gtfParser = new GenomicFeatureStreamReader(pathToGffFile, FeatureFileFormat.GTF);
        logger.logLine("Initializing regions from " + pathToGffFile + "...\n");

        chromosomeRegionSetMap =  new HashMap<String, GenomicRegionSet>();
        readCounts = new HashMap<String, Double>();

        if (calcCoverageBias) {
            transcriptDataHandler = new TranscriptDataHandler();
            transcriptDataHandler.validateAttributes(attrName, allowedFeatureList);
        }

        GenomicFeature record;
        int recordCount = 0;
        while((record = gtfParser.readNextRecord())!=null){

            for (String featureType: allowedFeatureList) {
                // TODO: consider different type of features here?
                recordCount++;
                if (recordCount % 100000 == 0) {
                    logger.logLine("Initialized " + recordCount + " regions...");
                }
                if (record.getFeatureName().equalsIgnoreCase(featureType)) {
                    addRegionToIntervalMap(record, true);
                    // init results map
                    readCounts.put(record.getAttribute(attrName), 0.0);
                    if (calcCoverageBias) {
                        transcriptDataHandler.addExonFeature(record);
                    }
                    break;
                }

            }
        }

        if (chromosomeRegionSetMap.isEmpty()) {
            throw new RuntimeException("Unable to load any regions from file.");
        }

        if (calcCoverageBias) {
            transcriptDataHandler.constructTranscriptsMap();
        }

        logger.logLine("\nInitialized " + recordCount + " regions it total\n\n");

        gtfParser.close();

    }



    void addRegionToIntervalMap(GenomicFeature feature, boolean useAttributeForCounting) {

        GenomicRegionSet regionSet = chromosomeRegionSetMap.get(feature.getSequenceName());
        if (regionSet == null) {
            regionSet = new GenomicRegionSet();
            chromosomeRegionSetMap.put(feature.getSequenceName(), regionSet);
        }

        String featureName = useAttributeForCounting ? feature.getAttribute(attrName) : feature.getFeatureName();

        regionSet.addRegion(feature, featureName);


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
        message.append("Feature ");
        if (!loadGenericRegions) {
            message.append("\"").append(attrName).append("\" ");
        }
        message.append("counts: ").append(getTotalReadCounts()).append("\n");
        message.append("No feature: ").append(noFeature).append("\n");
        message.append("Not unique alignment: ");
        if (countingAlgorithm.equals(COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)){
            message.append(alignmentNotUnique).append("\n");
        } else {
            message.append("NA\n");
        }
        message.append("Ambiguous: ").append(ambiguous).append("\n");

        if (loadGenericRegions) {
            message.append("NOTE: features were computed based on feature name\n");
        }

        if (calcCoverageBias) {
            message.append("Median 5' bias: ").append( transcriptDataHandler.getMedianFivePrimeBias() ).append("\n");
            message.append("Median 3' bias: ").append( transcriptDataHandler.getMedianThreePrimeBias() ).append("\n");
            message.append("Median 5' to 3' bias: ").append(transcriptDataHandler.getMedianFiveToThreeBias());
            message.append("\n");
        }


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

    public void saveCoverage(String fileName) throws IOException {
        if ( transcriptDataHandler != null ) {
            transcriptDataHandler.outputTranscriptsCoverage(fileName);
        }
    }
}
