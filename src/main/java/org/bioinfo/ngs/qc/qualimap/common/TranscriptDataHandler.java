/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.common;

import net.sf.picard.annotation.Gene;
import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalTreeMap;
import net.sf.picard.util.MathUtil;
import net.sf.samtools.SAMRecord;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.math.stat.StatUtils;
import org.bioinfo.ngs.qc.qualimap.beans.*;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 7/2/12
 * Time: 1:52 PM
 */
public class TranscriptDataHandler {

    public static final String ATTR_NAME_TRANSCRIPT_ID = "transcript_id";
    public static final String ATTR_NAME_GENE_ID = "gene_id";
    public static final String FEATURE_NAME_EXON = "exon";


    MultiMap<String, GenomicFeature> featureCache;
    HashMap<String, Gene> geneMap;
    HashMap<Gene.Transcript, int[]> transcriptCoverage;
    HashMap<String, Integer> junctionSequenceMap;
    JunctionLocationMap junctionLocationMap;
    IntervalTreeMap<Integer> intronIntervalTreeMap;

    public double getMedianFivePrimeBias() {
        return medianFivePrimeBias;
    }

    public double getMedianThreePrimeBias() {
        return medianThreePrimeBias;
    }

    public double getMedianFiveToThreeBias() {
        return medianFiveToThreeBias;
    }


    double medianFivePrimeBias, medianThreePrimeBias, medianFiveToThreeBias;
    long numReadsWithJunction, knownJunctions, partlyKnownJunctions;
    long numTotalReads, numIntronicReads, numIntergenicReads, numIntersectingExonRegionReads;

    int[] meanTranscriptCovHistogram;

    static int min(int[] data) {
        int res = data[0];

        for (int val : data) {
            if (val < res) {
                res = val;
            }
        }

        return res;
    }

    static int max(int[] data) {
        int res = data[0];

        for (int val : data) {
            if (val > res) {
                res = val;
            }
        }

        return res;
    }

    public TranscriptDataHandler() {
        featureCache = new MultiHashMap<String, GenomicFeature>();
        geneMap = new HashMap<String, Gene>();
        transcriptCoverage = new HashMap<Gene.Transcript, int[]>();
        junctionSequenceMap = new HashMap<String, Integer>();
        junctionLocationMap = new JunctionLocationMap();

    }


    public void addExonFeature(GenomicFeature feature) {

        String transcriptId = feature.getAttribute(ATTR_NAME_TRANSCRIPT_ID);
        if (transcriptId == null) {
            throw new RuntimeException("GTF attribute \"transcript_id\" is missing.");
        }
        featureCache.put(transcriptId, feature );

    }

    public void constructTranscriptsMap() throws RuntimeException {

        Set<String> transciptIds = featureCache.keySet();

        for (String transcriptId : transciptIds) {
            Collection<GenomicFeature> exonFeatures = featureCache.get(transcriptId);
            int numExons = exonFeatures.size();

            int[] exonStarts = new int[numExons];
            int[] exonEnds = new int[numExons];

            GenomicFeature firstFeature = null;
            int exonCount = 0;
            for (GenomicFeature exonFeature : exonFeatures) {
                if (firstFeature == null) {
                    firstFeature = exonFeature;
                }
                exonStarts[exonCount] = exonFeature.getStart();
                exonEnds[exonCount] = exonFeature.getEnd();
                exonCount++;
            }

            int transcriptStart = min(exonStarts);
            int transcriptEnd = max(exonEnds);

            if (firstFeature == null) {
                throw new RuntimeException("No exons detected for transcript " + transcriptId);
            }


            String geneName = firstFeature.getAttribute(ATTR_NAME_GENE_ID);
            Gene gene = geneMap.get(geneName);
            if (gene == null) {
                gene = new Gene(firstFeature.getSequenceName(), transcriptStart, transcriptEnd,
                    !firstFeature.isPositiveStrand(), geneName  );
                geneMap.put(geneName, gene);
            }

            Gene.Transcript t = gene.addTranscript(transcriptId, transcriptStart, transcriptEnd,
                    transcriptStart, transcriptEnd, numExons);

            for (int i = 0; i < numExons; ++i ) {
                t.addExon(exonStarts[i], exonEnds[i]);
            }


        }

        featureCache.clear();

        createHelperMaps();


    }

    public void addCoverage(String geneId, int startPos, int endPos) {
        Gene gene = geneMap.get(geneId);
        for (Gene.Transcript t : gene) {
            int[] coverage = transcriptCoverage.get(t);
            if (coverage == null) {
                coverage = new int[t.length()];
                transcriptCoverage.put(t, coverage);
            }
            t.addCoverageCounts(startPos, endPos, coverage );
        }
    }


    public void validateAttributes(String attrName, ArrayList<String> allowedFeatureList) {
        if (!attrName.equals(ATTR_NAME_GENE_ID)) {
            throw new RuntimeException("Wrong attribute name \"" + attrName +
                    "\", for 5'-3' bias calculation only \"gene_id\" is supported.");
        }

        if (allowedFeatureList.size() != 1 || !allowedFeatureList.get(0).equals(FEATURE_NAME_EXON)) {
            throw new RuntimeException("Wrong feature type \"" +  allowedFeatureList.get(0) +
            "\", for 5'-3' bias calculation only \"exon\' is supported.");
        }
    }



    // The three methods below are based on sf.picard.analysis.directed.RnaMetricsCollector

    static private double[] copyAndReverse(final double[] in) {
        final double[] out = new double[in.length];
        for (int i=0, j=in.length-1; i<in.length; ++i, --j) out[j] = in[i];
        return out;
    }

    public void calculateCoverageBias() {

        if (transcriptCoverage.size() == 0) {
            return;
        }

        final Map<Gene.Transcript,int[]> transcripts = pickTranscripts();

        int numTranscripts = transcripts.size();

        double[] fivePrimeBias = new double[numTranscripts];
        double[] threePrimeBias = new double[numTranscripts];
        double[] fiveToThreePrimeBias = new double[numTranscripts];


        int count = 0;
        for (final Map.Entry<Gene.Transcript,int[]> entry : transcripts.entrySet()) {
            final Gene.Transcript tx = entry.getKey();
            final double[] coverage;
            {
                final double[] tmp = MathUtil.promote(entry.getValue());
                if (tx.getGene().isPositiveStrand()) {
                    coverage = tmp;
                } else {
                    coverage = copyAndReverse(tmp);
                }
            }
            final double mean = MathUtil.mean(coverage, 0, coverage.length);

            final int PRIME_BASES = 100;
            final double fivePrimeCoverage = MathUtil.mean(coverage, 0, PRIME_BASES);
            final double threePrimeCoverage = MathUtil.mean(coverage, coverage.length - PRIME_BASES, coverage.length);

            fivePrimeBias[count] = fivePrimeCoverage / mean;
            threePrimeBias[count] = threePrimeCoverage / mean;
            fiveToThreePrimeBias[count] = fivePrimeCoverage /threePrimeCoverage;
            count++;

        }

        medianFivePrimeBias = StatUtils.percentile(fivePrimeBias, 50);
        medianThreePrimeBias = StatUtils.percentile(threePrimeBias, 50);
        medianFiveToThreeBias = StatUtils.percentile(fiveToThreePrimeBias, 50);

    }


    public Map<Gene.Transcript, int[]> pickTranscripts() {

        Collection<Gene> genes = geneMap.values();
        final int minimumLength = 500;
        final Map<Gene.Transcript, Double> bestPerGene = new HashMap<Gene.Transcript, Double>();

        // Make a map of the best transcript per gene to it's mean coverage
        for (final Gene gene : genes) {

            Gene.Transcript best = null;
            double bestMean = 0;

            for (final Gene.Transcript tx : gene) {
                final int[] cov = transcriptCoverage.get(tx);

                if (tx.length() < Math.max(minimumLength, 100)) {
                    continue;
                }

                if (cov == null) {
                    continue;
                }

                final double mean = MathUtil.mean(MathUtil.promote(cov), 0, cov.length);
                if (mean < 1d) {
                    continue;
                }

                if (best == null || mean > bestMean) {
                    best = tx;
                    bestMean = mean;
                }
            }

            if (best != null) bestPerGene.put(best, bestMean);
        }

        // Find the 1000th best coverage value
        final double[] coverages = new double[bestPerGene.size()];
        int i=0;
        for (final double d : bestPerGene.values()) {
            coverages[i++] = d;
        }
        Arrays.sort(coverages);
        final double min = coverages.length == 0 ? 0 : coverages[Math.max(0, coverages.length - 1001)];

        // And finally build the output map
        final Map<Gene.Transcript, int[]> retval = new HashMap<Gene.Transcript, int[]>();
        for (final Map.Entry<Gene.Transcript,Double> entry : bestPerGene.entrySet()) {
            final Gene.Transcript tx = entry.getKey();
            final double coverage = entry.getValue();

            if (coverage >= min) {
                retval.put(tx, transcriptCoverage.get(tx));
            }
        }

        return retval;
    }


    public void computeMeanTranscriptCoverageHist() {

        // TODO: number of bins is currently fixed, however it might be interesting to support more bins

        final int NUM_BINS = 51;

        meanTranscriptCovHistogram = new int[NUM_BINS];

        Collection<Gene> genes = geneMap.values();

        for (final Gene gene : genes) {

            for (final Gene.Transcript tx : gene) {

                if (tx.length() == 0) {
                    continue;
                }

                final int[] cov = transcriptCoverage.get(tx);

                long sum = 0;
                if (cov != null) {
                    for (int c : cov) {
                        sum += c;
                    }
                }

                int meanCovLevel = (int) (sum / tx.length());

                if (meanCovLevel < 0) {
                    System.out.println("Error with meanCovLevel: " + meanCovLevel);
                    continue;
                }

                if (meanCovLevel >= NUM_BINS) {
                    meanCovLevel = NUM_BINS - 1;
                }

                meanTranscriptCovHistogram[meanCovLevel] += 1;

            }
        }

    }

    TreeMap<Double, int[]> getSortedTranscriptCoverage() {

        TreeMap<Double, int[]> resMap = new TreeMap<Double, int[]>();

        Collection<Gene> genes = geneMap.values();

        for (final Gene gene : genes) {

            for (final Gene.Transcript tx : gene) {

                final int[] cov = transcriptCoverage.get(tx);

                if (cov == null) {
                    continue;
                }

                double meanCov = MathUtil.mean(MathUtil.promote(cov), 0, cov.length);
                if (meanCov > 0) {
                    resMap.put(meanCov, cov);
                }


            }
        }
        return resMap;
    }

    public double[] computePerBaseTranscriptCoverageHist(TreeMap<Double, int[]> resMap, int numItems, boolean ascendingOrder) {

        final int NUM_BINS = 100;

        NavigableSet<Double> set = ascendingOrder ? resMap.navigableKeySet() : resMap.descendingKeySet();

        GenericHistogram hist = new GenericHistogram(NUM_BINS, true);

        int count = 0;
        for (Double coverage : set) {
            if (count >= numItems) {
                break;
            }

            int[] cov = resMap.get(coverage);
            hist.updateHistogram(cov);

            count++;

        }

        return hist.getHist();


    }

    public QChart createCoverageProfilePlot(double[] perBaseTranscriptCoverage, String chartName, String sampleName )
    {

        XYVector coverageData = new XYVector();

        for (int i = 0; i < perBaseTranscriptCoverage.length; ++i) {
            coverageData.addItem( new XYItem(i,perBaseTranscriptCoverage[i]));
        }


        BamQCChart geneCoverage = new BamQCChart(chartName,
                sampleName, "Transcript position", " Counts ");
        geneCoverage.addSeries("Transcript coverage profile", coverageData, new Color(255, 0, 0, 255));
        geneCoverage.setAdjustDomainAxisLimits(false);
        geneCoverage.setDomainAxisIntegerTicks(true);
        geneCoverage.setShowLegend(false);
        geneCoverage.render();
        return new QChart(chartName, geneCoverage.getChart(), geneCoverage);

    }


    public QChart createReadsOriginPieChart(String sampleName) {

        long numExonicReads = numTotalReads - numIntronicReads - numIntergenicReads;

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Exonic", new Double((100 * numExonicReads) / numTotalReads));
        dataset.setValue("Intergenic", new Double( (100.*numIntergenicReads) / numTotalReads ));
        dataset.setValue("Intronic", new Double((100.*numIntronicReads) / numTotalReads ));

        String chartTitle = "Reads Genomic Origin";
        BamQCPieChart pieChart = new BamQCPieChart(chartTitle, sampleName, dataset);
        pieChart.render();

        return new QChart(chartTitle, pieChart.getJFreeChart() );

    }

    public QChart createJunctionAnalysisPieChart(String sampleName) {

        long novelJunctions = numReadsWithJunction - knownJunctions - partlyKnownJunctions;

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Known", new Double((100 * knownJunctions) / numReadsWithJunction));
        dataset.setValue("Partly known", new Double( (100.*partlyKnownJunctions) / numReadsWithJunction ));
        dataset.setValue("Novel", new Double((100.*novelJunctions) / numReadsWithJunction ));

        String chartTitle = "Junction Analysis";
        BamQCPieChart pieChart = new BamQCPieChart(chartTitle, sampleName, dataset);
        pieChart.render();

        return new QChart(chartTitle, pieChart.getJFreeChart() );

    }


    public List<QChart> createPlots(String sampleName) throws IOException {

        ArrayList<QChart> charts = new ArrayList<QChart>();


        if (numTotalReads > 0) {
            QChart readsOriginChart = createReadsOriginPieChart(sampleName);
            charts.add(readsOriginChart);
        }

        TreeMap<Double, int[]> sortedCoverageMap = getSortedTranscriptCoverage();

        {
            double[] perBaseTranscriptCoverage = computePerBaseTranscriptCoverageHist(sortedCoverageMap,
                    sortedCoverageMap.size(), true );
            QChart chart = createCoverageProfilePlot(perBaseTranscriptCoverage,
                    "Coverage Profile Along Genes (Total)", sampleName);
            charts.add(chart);
        }

        {
            double[] perBaseTranscriptCoverage = computePerBaseTranscriptCoverageHist(sortedCoverageMap,
                    500, true );
            QChart chart = createCoverageProfilePlot(perBaseTranscriptCoverage,
                    "Coverage Profile Along Genes (Low)", sampleName);
            charts.add(chart);
        }

        {
            double[] perBaseTranscriptCoverage = computePerBaseTranscriptCoverageHist(sortedCoverageMap,
                    500, false );
            QChart chart = createCoverageProfilePlot(perBaseTranscriptCoverage,
                    "Coverage Profile Along Genes (High)", sampleName);
            charts.add(chart);
        }


        {
            computeMeanTranscriptCoverageHist();

            XYVector covHist = new XYVector();

            for (int i = 0; i < meanTranscriptCovHistogram.length; ++i) {
                covHist.addItem(new XYItem(i, meanTranscriptCovHistogram[i]));
            }

            BamQCXYHistogramChart coverageHistogram =
                            new BamQCXYHistogramChart(Constants.PLOT_TITLE_COVERAGE_HISTOGRAM_0_50,
                                    sampleName, "Coverage (X)", "Number of transcripts");
                    coverageHistogram.addHistogram("coverageData", covHist, Color.blue);
                    coverageHistogram.setNumberOfBins(50);
                    //coverageHistogram.zoom(maxValue);
                    coverageHistogram.setDomainAxisIntegerTicks(true);
                    coverageHistogram.setDomainAxisTickUnitSize(1.0);
                    coverageHistogram.render();

                    charts.add( new QChart("Transcript coverage histogram",
                            coverageHistogram.getChart() ) );

        }

        {
            if (numReadsWithJunction > 0) {
                QChart chart = createJunctionAnalysisPieChart(sampleName);
                charts.add(chart);
            }
        }


        return charts;

    }

    public void collectJunctionInfo(SAMRecord read, int posInRead, int clippedLength) {
        byte[] seq = read.getReadBases();

        if (seq != null && posInRead - 2 > 0 && posInRead + 1 < seq.length) {
            char[] junction = new char[4];
            junction[0] =  (char) seq[posInRead - 2];
            junction[1] =  (char) seq[posInRead - 1];
            junction[2] =  (char) seq[posInRead];
            junction[3] =  (char) seq[posInRead + 1 ];
            String junctionStr = new String(junction);


            Integer count = junctionSequenceMap.get(junctionStr);
            if (count == null) {
                junctionSequenceMap.put(junctionStr, 1);
            } else {
                junctionSequenceMap.put(junctionStr, count + 1);
            }

            int alignmentStart = read.getAlignmentStart();
            int posInRef1 = alignmentStart + posInRead;
            int posInRef2 = posInRef1 + clippedLength;
            String seqName = read.getReferenceName();

            boolean j1 = junctionLocationMap.hasOverlap(seqName, posInRef1);
            boolean j2 =  junctionLocationMap.hasOverlap(seqName, posInRef2);
            if (j1 && j2) {
                knownJunctions++;
            } else if (j1 || j2) {
                partlyKnownJunctions++;
            }


        }

        numReadsWithJunction += 1;

    }

    public List<JunctionInfo> computeSortedJunctionsMap() {

        List<JunctionInfo> junctionList = new ArrayList<JunctionInfo>();

        if (numReadsWithJunction == 0) {
            return junctionList;
        }

        for (Map.Entry<String, Integer> entry : junctionSequenceMap.entrySet()) {

            double percentage = (entry.getValue() * 100.) / numReadsWithJunction;
            junctionList.add( new JunctionInfo(entry.getKey(), percentage));
        }

        Collections.sort(junctionList);

        return junctionList;
    }

    public long getNumReadsWithJunctions() {
        return numReadsWithJunction;
    }

    public long getNumIntronicReads() {
        return numIntronicReads;
    }

    public long getNumIntergenicReads() {
        return numIntergenicReads;
    }

    public void setNumTotalReads(long totalReadCounts) {
        numTotalReads = totalReadCounts;
    }

    void createHelperMaps() {
        intronIntervalTreeMap =  new IntervalTreeMap<Integer>();
        int numIntrons = 0;

        for (Map.Entry<String,Gene> entry : geneMap.entrySet()) {
            Gene gene = entry.getValue();
            for (final Gene.Transcript tx : gene) {
                int numExons = tx.exons.length;
                if (numExons < 2) {
                    continue;
                }
                int[] breakCoords = new int[(numExons - 1)*2];
                int k = 0;

                if ( gene.isPositiveStrand() ) {
                    for (int i = 0; i < numExons; ++ i) {
                        Gene.Transcript.Exon e = tx.exons[i];
                        if (i == 0) {
                            breakCoords[k++] = e.end;
                        } else if ( i == numExons - 1) {
                            breakCoords[k++] = e.start;
                        } else {
                            breakCoords[k++] = e.start;
                            breakCoords[k++] = e.end;
                        }
                    }
                } else {
                    // make sure there is no problem with sorting
                    Arrays.sort(tx.exons, new Comparator<Gene.Transcript.Exon>() {
                        @Override
                        public int compare(Gene.Transcript.Exon e1, Gene.Transcript.Exon e2) {
                            return e1.start > e2.start ? -1 : 1;
                        }
                    });

                    for (int i = numExons - 1; i >= 0; -- i) {
                        Gene.Transcript.Exon e = tx.exons[i];
                        if (i == 0) {
                            breakCoords[k++] = e.start;
                        } else if ( i == numExons - 1) {
                            breakCoords[k++] = e.end;
                        } else {
                            breakCoords[k++] = e.start;
                            breakCoords[k++] = e.end;
                        }
                    }
                }

                for (int i = 0; i < breakCoords.length; i+=2) {
                    String chrName = gene.getSequence();
                    if (breakCoords[i] >= breakCoords[i+1]) {
                        String warning = "Warning! Transcript " + tx.name +
                                " has incorrect exon coordinates. Transcript skipped...";

                        System.out.println(warning);
                        break;
                    }

                    Interval intronInterval = new Interval(chrName, breakCoords[i], breakCoords[i+1]);
                    numIntrons++;
                    intronIntervalTreeMap.put(intronInterval, numIntrons);
                    junctionLocationMap.put(chrName, breakCoords[i],breakCoords[i+1]);
                }

            }
        }

        junctionLocationMap.setupIntervalTreeMap();


    }

    public void collectNonFeatureMappedReadInfo(List<Interval> intervals, int numReads) {
        boolean liesInIntergenic = true;
        for (Interval iv : intervals) {
            Collection<Integer> intronOverlaps = intronIntervalTreeMap.getOverlapping(iv);
            if (intronOverlaps.size() > 0) {
                liesInIntergenic = false;
                break;
            }
        }
        if (liesInIntergenic) {
            numIntergenicReads += numReads;
        } else {
            numIntronicReads += numReads;
        }
    }

    public void increaseNumReadsIntersectingExonRegion() {
        numIntersectingExonRegionReads++;
    }

    public long getNumReadsIntersectingExonRegion() {
        return numIntersectingExonRegionReads;
    }

}
