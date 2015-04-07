/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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


import net.sf.picard.io.IoUtil;
import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalTree;
import net.sf.samtools.*;
import net.sf.samtools.util.CoordMath;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.io.FileUtils;
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
    LibraryProtocol protocol;
    String countingAlgorithm;
    String attrName;
    LoggerThread logger;
    boolean collectRnaSeqStats, skipSecondaryAlignments;
    boolean loadGenericRegions;
    boolean outputCoverage;
    boolean strandSpecificAnalysis, pairedEndAnalysis, sortingRequired, cleanupRequired;

    String pathToBamFile, pathToGffFile, sampleName;

    long primaryAlignments, secondaryAlignments;
    long notAligned, alignmentNotUnique, noFeature, ambiguous;
    long readCount, fragmentCount, seqNotFoundCount;
    long leftProperInPair, rightProperInPair, bothProperInPair;
    long protocolCorrectlyMapped;

    public static final String GENE_ID_ATTR = "gene_id";
    public static final String EXON_TYPE_ATTR = "exon";
    public static final String COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED = "uniquely-mapped-reads";
    public static final String COUNTING_ALGORITHM_PROPORTIONAL = "proportional";

    public ComputeCountsTask(String pathToBamFile, String pathToGffFile) {
        this.pathToBamFile = pathToBamFile;
        this.pathToGffFile = pathToGffFile;
        this.attrName = GENE_ID_ATTR;

        protocol = LibraryProtocol.NON_STRAND_SPECIFIC;
        countingAlgorithm = COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED;
        allowedFeatureList = new ArrayList<String>();
        featureIntervalMap = new MultiHashMap<String, Interval>();
        collectRnaSeqStats = false;
        skipSecondaryAlignments = false;
        loadGenericRegions = false;
        outputCoverage = true;
        pairedEndAnalysis = false;
        sortingRequired = false;
        cleanupRequired = false;

        readCount = 0;
        seqNotFoundCount = 0;

        logger = new LoggerThread() {
            @Override
            public void logLine(String msg) {
                System.out.println(msg);
            }
        };

        sampleName = (new File(this.pathToBamFile)).getName();

    }

    public void addSupportedFeatureType(String featureName) {
        allowedFeatureList.add(featureName);
    }

    public void setProtocol(LibraryProtocol protocol) {
        this.protocol =  protocol;
    }

    public void setLogger(LoggerThread thread) {
        this.logger = thread;
    }

    public void skipSecondaryAlignments() {
        this.skipSecondaryAlignments = true;
    }

    public void setCollectRnaSeqStats(boolean collectRnaSeqStats) {
        this.collectRnaSeqStats = collectRnaSeqStats;
    }

    public static String getAlgorithmTypes() {
        return ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED + "(default) or " +
                ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL;
    }

    String sortSamByName(String inputPath) throws IOException {

        // bam file by name

        long n = 0;
        File targetFile = File.createTempFile("" + UniqueID.get(), ".bam");
        String targetPath = targetFile.getAbsolutePath();

        SAMFileReader reader = new SAMFileReader(IoUtil.openFileForReading(new File(inputPath)));
        reader.getFileHeader().setSortOrder(SAMFileHeader.SortOrder.queryname);
        final SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(),
                false,
                targetFile);

        for (SAMRecord record : reader) {
            writer.addAlignment(record);
            if (++n % 10000000 == 0) {
                logger.logLine("Read " + n + " records.");
            }
        }

        logger.logLine("Finished reading inputs, merging and writing to output now.");

        reader.close();
        writer.close();

        logger.logLine("Sorting by name finished.");
        cleanupRequired = true;

        return targetPath;
    }

    boolean checkRead(SAMRecord read) {

        if (read == null || read.getReadUnmappedFlag()) {
            notAligned++;
            return false;
        }

        if (read.getNotPrimaryAlignmentFlag() ) {
            secondaryAlignments++;
            if (skipSecondaryAlignments) {
                return false;
            }
        } else {
            primaryAlignments++;
            if (read.getReadPairedFlag() ) {

                if (read.getProperPairFlag()) {
                    bothProperInPair++;
                }
                if (read.getFirstOfPairFlag()) {
                    leftProperInPair++;
                }
                if (read.getSecondOfPairFlag()) {
                    rightProperInPair++;
                }
            }
        }

        String chrName = read.getReferenceName();
        GenomicRegionSet regionSet = chromosomeRegionSetMap.get(chrName);

        if (regionSet == null ) {
            seqNotFoundCount++;
            return false;
        }

        readCount++;

        return true;

    }

    ArrayList<Interval> getReadIntervals(SAMRecord read) {

        ArrayList<Interval> intervals = new ArrayList<Interval>();

        String chrName = read.getReferenceName();
        boolean pairedRead = read.getReadPairedFlag();
        Cigar cigar = read.getCigar();
        List<CigarElement> cigarElements = cigar.getCigarElements();
        int offset = read.getAlignmentStart();
        boolean strand = read.getReadNegativeStrandFlag();
        if (pairedRead) {
            boolean firstOfPair = read.getFirstOfPairFlag();
            if ( (protocol  == LibraryProtocol.STRAND_SPECIFIC_FORWARD && !firstOfPair) ||
                    (protocol == LibraryProtocol.STRAND_SPECIFIC_REVERSE && firstOfPair) ) {
                strand = !strand;
            }
        } else {
            if (protocol == LibraryProtocol.STRAND_SPECIFIC_REVERSE)  {
                strand = !strand;
            }
        }

        int posInRead = 0;
        for (CigarElement cigarElement : cigarElements) {
            int length = cigarElement.getLength();

            if ( cigarElement.getOperator().equals(CigarOperator.M)  ) {
                intervals.add(new Interval(chrName, offset, offset + length - 1, strand, "" ));
                posInRead += length;
            }

            if (cigarElement.getOperator().equals(CigarOperator.I) ||
                    cigarElement.getOperator().equals(CigarOperator.EQ) ||
                    cigarElement.getOperator().equals(CigarOperator.S)) {
                posInRead += length;
            }

            if (cigarElement.getOperator().equals(CigarOperator.N) && collectRnaSeqStats) {

                transcriptDataHandler.collectJunctionInfo(read, posInRead, cigarElement.getLength());

            }

            offset += length;
        }

        return intervals;
    }

    Map<String,BitSet> findIntersectingFeatures(List<Interval> intervals) {

        HashMap<String,BitSet> featureIntervalMap = new HashMap<String, BitSet>();
        int intIndex = 0;


        for (Interval alignmentInterval : intervals) {
            GenomicRegionSet regionSet = chromosomeRegionSetMap.get(alignmentInterval.getSequence());
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
                    if (collectRnaSeqStats) {
                        for (GenomicRegionSet.Feature feature : features) {
                            transcriptDataHandler.addCoverage(feature.getName(),
                                    alignmentInterval.getStart(), alignmentInterval.getEnd() );
                        }
                    }

                }

            }

            intIndex++;
        }


        return featureIntervalMap;
    }

    private void processFragment(ArrayList<SAMRecord> fragmentReads) {
        int numReads = fragmentReads.size();
        if (numReads == 0) {
            return;
        } else if (numReads == 1) {
            String readName = fragmentReads.get(0).getReadName();
            System.err.println("WARNING: The fragment " + readName + " has only 1 alignments," +
                    " however multiple segments are assumed!");
            return;
        } else if (numReads > 2) {
            String readName = fragmentReads.get(0).getReadName();
            System.err.println("WARNING: The fragment " + readName + " has more than 2 alignments!");
            return;
        }

        fragmentCount++;

        ArrayList<Interval> intervals = new ArrayList<Interval>();
        float fragmentWeight = 0;
        for (SAMRecord read : fragmentReads) {
            intervals.addAll(  getReadIntervals(read) );
            fragmentWeight += read.getFloatAttribute(Constants.READ_WEIGHT_ATTR);
        }
        fragmentWeight /= numReads;
        processAlignmentIntervals(intervals, fragmentWeight);


    }


    void processRead(SAMRecord read) {
        List<Interval> intervals = getReadIntervals(read);
        float readWeight = read.getFloatAttribute(Constants.READ_WEIGHT_ATTR);
        fragmentCount++;
        processAlignmentIntervals(intervals, readWeight);
    }


    void processAlignmentIntervals(List<Interval> intervals, float alnWeight) {

        //Find intersections
        Map<String,BitSet> featureIntervalMap = findIntersectingFeatures(intervals);


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
            if (collectRnaSeqStats) {
                transcriptDataHandler.collectNonFeatureMappedReadInfo(intervals);
            }
        } else if (features.size()  == 1) {
            //if (features.iterator().next().contains("ENSG00000124222"))  {
            //    System.out.println(read.getReadName());
            //}
            String geneName = features.iterator().next();
            double count = readCounts.get(geneName);
            readCounts.put(geneName, count  + alnWeight);

        }   else {
            ambiguous++;
        }

        if (features.size() > 0 && strandSpecificAnalysis) {
            protocolCorrectlyMapped++;
        }

        if (readCount % 500000 == 0) {
            logger.logLine("Analyzed " + readCount + " reads...");
        }

    }


    boolean computeReadWeight(SAMRecord read) {
        float readWeight = 1.0f;
        int nh = 1;
        try {
            nh = read.getIntegerAttribute("NH");
        } catch (NullPointerException ex) {
            //System.err.println("The read " + read.getReadName() + " doesn't have NH attribute");
        }
        if (nh > 1) {
            if (countingAlgorithm.equals(COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)) {
                alignmentNotUnique++;
                return false;
            } else if (countingAlgorithm.equals(COUNTING_ALGORITHM_PROPORTIONAL)) {
                readWeight = 1.0f / nh;
            }
        }

        read.setAttribute(Constants.READ_WEIGHT_ATTR, readWeight);
        return true;
    }

    public void run() throws Exception {

        initRegions();

        logger.logLine("Starting BAM file analysis\n");

        if (pairedEndAnalysis) {
            if (sortingRequired) {
                logger.logLine("Sorting BAM file by name...\n");
                pathToBamFile = sortSamByName(pathToBamFile);
            }
        }

        SAMFileReader reader = new SAMFileReader(new File(pathToBamFile));

        SAMRecordIterator iter = reader.iterator();
        strandSpecificAnalysis = protocol != LibraryProtocol.NON_STRAND_SPECIFIC;

        ArrayList<SAMRecord> fragmentReads = new ArrayList<SAMRecord>();
        ArrayList<String> chr_names = new ArrayList<String>();
        HashSet<String> notFoundChrNames = new HashSet<String>();

        String curReadName = null;

        while (iter.hasNext()) {

            SAMRecord read = iter.next();

            if (!checkRead(read)) {
                if (!chr_names.contains(read.getReferenceName())) {
                    notFoundChrNames.add(read.getReferenceName());
                    chr_names.add(read.getReferenceName());
                }
                continue;
            }

            if(!computeReadWeight(read) ) {
                continue;
            }

            if (pairedEndAnalysis && read.getReadPairedFlag() && read.getProperPairFlag()) {
                String readName = read.getReadName();
                if (curReadName == null || readName.equals( curReadName ) ) {
                    fragmentReads.add(read);
                } else {
                    processFragment(fragmentReads);
                    fragmentReads.clear();
                    fragmentReads.add(read);
                }
                curReadName = readName;
            } else {
                processRead(read);
            }

        }

        if (pairedEndAnalysis && fragmentReads.size() > 0) {
            processFragment(fragmentReads);
        }

        if (readCount == 0) {
            throw new RuntimeException("BAM file is empty.");
        }

        if (seqNotFoundCount + alignmentNotUnique == readCount) {
            throw new RuntimeException("The BAM file and annotations file have no intersections. " +
                    "Check sequence names for consistency.");
        }


        if (collectRnaSeqStats) {
            transcriptDataHandler.calculateCoverageBias();
        }

        logger.logLine("\nProcessed " + readCount + " reads in total");

        if (notFoundChrNames.size() > 0) {
            System.err.println("\nWARNING! The following chromosomes from reads are not found in annotations:");
            for (String notFoundChrName : notFoundChrNames) {
                System.err.println(notFoundChrName);
            }
        }

        if (cleanupRequired) {
            logger.logLine("\nCleanup of temporary files");
            FileUtils.deleteQuietly(new File(pathToBamFile));
        }

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

         if (collectRnaSeqStats) {
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

        if (collectRnaSeqStats) {
            transcriptDataHandler = new TranscriptDataHandler();
            transcriptDataHandler.validateAttributes(attrName, allowedFeatureList);
        }

        GenomicFeature record;
        int recordCount = 0;
        while((record = gtfParser.readNextRecord())!=null){

            for (String featureType: allowedFeatureList) {
                recordCount++;
                if (recordCount % 100000 == 0) {
                    logger.logLine("Initialized " + recordCount + " regions...");
                }
                if (record.getFeatureName().equalsIgnoreCase(featureType)) {
                    addRegionToIntervalMap(record, true);
                    // init results map
                    readCounts.put(record.getAttribute(attrName), 0.0);
                    if (collectRnaSeqStats) {
                        transcriptDataHandler.addExonFeature(record);
                    }
                    break;
                }

            }
        }

        if (chromosomeRegionSetMap.isEmpty()) {
            throw new RuntimeException("Unable to load any regions from file.");
        }

        logger.logLine("\nInitialized " + recordCount + " regions it total");

        if (collectRnaSeqStats) {
            logger.logLine("\nStarting constructing transcripts for RNA-seq stats...");
            transcriptDataHandler.constructTranscriptsMap();
            logger.logLine("Finished constructing transcripts\n");

        }


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

    public long getPrimaryAlignmentsNumber() {
        return primaryAlignments;
    }

    public long getLeftProperInPair() {
        return leftProperInPair;
    }

    public long getRightProperInPair() {
        return  rightProperInPair;
    }

    public long getTotalAlignmentsNumber() {
        return primaryAlignments + secondaryAlignments;
    }

    public long getSecondaryAlignmentsNumber() {
        return secondaryAlignments;
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

    public long getNumberOfMappedPairs() {
        return bothProperInPair / 2;
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

        if (collectRnaSeqStats) {
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

    /*public void saveCoverage(String fileName) throws IOException {
        if ( transcriptDataHandler != null ) {
            transcriptDataHandler.outputTranscriptsCoverage(fileName);
        }
    }*/

    public TranscriptDataHandler getTranscriptDataHandler() {
        return transcriptDataHandler;
    }

    public void setPairedEndAnalysis() {
        pairedEndAnalysis = true;
    }

    public static boolean supportedLibraryProtocol(String protocolName) {
        return (protocolName.equals(LibraryProtocol.PROTOCOL_FORWARD_STRAND) ||
                protocolName.equals(LibraryProtocol.PROTOCOL_REVERSE_STRAND) ||
                protocolName.equals(LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC) );
    }


    public void setSortingRequired() {
        sortingRequired = true;
    }

    /*public LibraryProtocol getLibraryProtocol() {
        return protocol;
    }

    public long getTotalFragmentCount() {
        return fragmentCount;
    }

    public long getProtocolCorrectlyMapped() {
        return protocolCorrectlyMapped;
    }*/

    public String getSampleName() {
        return sampleName;
    }

    public LoggerThread getLogger() {
        return logger;
    }
}
