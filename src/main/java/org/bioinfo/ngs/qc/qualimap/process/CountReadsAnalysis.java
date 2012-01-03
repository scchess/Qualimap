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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by kokonech
 * Date: 12/12/11
 * Time: 2:52 PM
 */

public class CountReadsAnalysis {

    Map<String,Long> readCounts;
    Map<String, GenomicRegionSet> chromosomeRegionSetMap;
    MultiMap<String, Interval> featureIntervalMap;
    ArrayList<String> allowedFeatureList;

    String pathToBamFile, pathToGffFile;

    long notAligned, alignmentNotUnique, noFeature, ambiguous;


    public CountReadsAnalysis(String pathToBamFile, String pathToGffFile) {
        this.pathToBamFile = pathToBamFile;
        this.pathToGffFile = pathToGffFile;
        allowedFeatureList = new ArrayList<String>();
        featureIntervalMap = new MultiHashMap<String, Interval>();
    }

    public void addSupportedFeatureType(String featureName) {
        allowedFeatureList.add(featureName);
    }


    public void run() throws FileFormatException, IOException, NoSuchMethodException {


        loadRegions();

        if (allowedFeatureList.isEmpty()) {
            // default feature to consider
            addSupportedFeatureType("exon");
        }

        SAMFileReader reader = new SAMFileReader(new File(pathToBamFile));

        SAMRecordIterator iter = reader.iterator();

        while (iter.hasNext()) {

            // TODO: think of strands!

            SAMRecord read = iter.next();

            if (read == null || read.getReadUnmappedFlag()) {
                notAligned++;
                continue;
            }

            if (read.getIntegerAttribute("NH") > 1) {
                alignmentNotUnique++;
                continue;
            }

            // TODO: analyze paired-mapped together!

            String chrName = read.getReferenceName();

            GenomicRegionSet regionSet = chromosomeRegionSetMap.get(chrName);

            if (regionSet == null ) {
                System.err.println("Unknown chromosome: " + chrName);
                continue;
            }

            //Debugging  purposes
            //System.out.print("ReadName: "+read.getReadName() );
            //System.out.println("ReadStart: "+read.getAlignmentStart() + ", ReadEnd: " + read.getAlignmentEnd());
            //if (read.getReadName().contains("SRR002320.11354625") ) {
            //    System.out.println("BINGO!");
            //}

            // Create intervals for read
            Cigar cigar = read.getCigar();
            List<CigarElement> cigarElements = cigar.getCigarElements();
            List<Interval> intervals = new ArrayList<Interval>();
            int offset = read.getAlignmentStart();
            for (CigarElement cigarElement : cigarElements) {
                int length = cigarElement.getLength();
                if (cigarElement.getOperator().equals(CigarOperator.M) ) {
                    intervals.add(new Interval(chrName, offset, offset + length - 1));
                }
                offset += length;
            }

            HashMap<String,BitSet> featureIntervalMap = new HashMap<String, BitSet>();
            int intIndex = 0;

            for (Interval interval : intervals) {
                Iterator<IntervalTree.Node<Set<String>>> overlapIter = regionSet.overlappers(interval.getStart(), interval.getEnd() );
                while (overlapIter.hasNext()) {
                    IntervalTree.Node<Set<String>> node = overlapIter.next();

                    if (CoordMath.encloses(node.getStart(), node.getEnd(), interval.getStart(), interval.getEnd()) ) {

                        Set<String> features = node.getValue();
                        for (String feature : features) {
                            BitSet intervalBits = featureIntervalMap.get(feature);
                            if (intervalBits == null) {
                                intervalBits = new BitSet(intervals.size());
                                featureIntervalMap.put(feature, intervalBits);
                            }
                            intervalBits.set(intIndex, true);
                        }
                    }
                }
                intIndex++;
            }


            Set<String> features = new HashSet<String>();

            for (Map.Entry<String,BitSet> entry : featureIntervalMap.entrySet() ) {
                if (entry.getValue().cardinality() == intervals.size() ) {
                    features.add(entry.getKey());
                }
            }

            if (features.size()  == 0) {
                noFeature++;
            } else if (features.size()  == 1) {
                if (features.iterator().next().contains("ENSG00000124222"))  {
                //    System.out.println(read.getReadName());
                }
                String geneName = features.iterator().next();
                long count = readCounts.get(geneName);
                readCounts.put(geneName, ++count);
            }   else {
                ambiguous++;
            }

        }

    }


    void loadRegions() throws IOException, NoSuchMethodException, FileFormatException {

        GtfParser gtfParser = new GtfParser(pathToGffFile);
		System.out.println("initializing regions from " + pathToGffFile + ".....");

        chromosomeRegionSetMap =  new HashMap<String, GenomicRegionSet>();
        readCounts = new HashMap<String, Long>();


        GtfParser.Record record;
        while((record = gtfParser.readNextRecord())!=null){

            for (String featureName: allowedFeatureList) {
                if (!record.getFeature().equalsIgnoreCase(featureName)) {
                    continue;
                }
            }

            addRegionToIntervalMap(record);

            // init results map
            readCounts.put(record.getGeneId(), 0L);

        }

        if (chromosomeRegionSetMap.isEmpty()) {
            throw new RuntimeException("Unable to load any regions from file.");
        }

        gtfParser.close();

    }

    void addRegionToIntervalMap(GtfParser.Record r) {

        GenomicRegionSet regionSet = chromosomeRegionSetMap.get(r.getSeqName());
        if (regionSet == null) {
            regionSet = new GenomicRegionSet();
            chromosomeRegionSetMap.put(r.getSeqName(), regionSet);
        }

        regionSet.addRegion(r);

    }

    public Map<String,Long> getReadCounts() {
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




}
