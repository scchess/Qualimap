/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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

import antlr.StringUtils;
import net.sf.picard.annotation.Gene;
import net.sf.picard.util.MathUtil;
import net.sf.samtools.SAMRecord;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.math.stat.StatUtils;
import org.bioinfo.ngs.qc.qualimap.beans.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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
    HashMap<String, Integer> junctionMap;

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
    long numReadsWithJunction;
    long numTotalReads, numIntronicReads, numIntergenicReads;

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
        junctionMap = new HashMap<String, Integer>();

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

            Gene.Transcript t = gene.addTranscript(transcriptId, transcriptStart, transcriptEnd, transcriptStart, transcriptEnd, numExons);

            for (int i = 0; i < numExons; ++i ) {
                t.addExon(exonStarts[i], exonEnds[i]);
            }


        }

        featureCache.clear();


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
                if (tx.getGene().isPositiveStrand())  coverage = tmp;
                else coverage = copyAndReverse(tmp);
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

                if (tx.length() < Math.max(minimumLength, 100)) continue;
                if (cov == null) continue;

                final double mean = MathUtil.mean(MathUtil.promote(cov), 0, cov.length);
                if (mean < 1d) continue;
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

        final int NUM_BINS = 50;

        meanTranscriptCovHistogram = new int[NUM_BINS];

        Collection<Gene> genes = geneMap.values();

        for (final Gene gene : genes) {

            for (final Gene.Transcript tx : gene) {

                if (tx.length() == 0) {
                    continue;
                }

                final int[] cov = transcriptCoverage.get(tx);

                int sum = 0;
                if (cov != null) {
                    for (int c : cov) {
                        sum += c;
                    }
                }

                int meanCovLevel = sum / tx.length();

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

    public double[] computePerBaseTranscriptCoverageHist() {

        final int NUM_BINS = 100;

        GenericHistogram hist = new GenericHistogram(NUM_BINS, true);

        Collection<Gene> genes = geneMap.values();


        for (final Gene gene : genes) {

            for (final Gene.Transcript tx : gene) {

                final int[] cov = transcriptCoverage.get(tx);

                if (cov == null)
                    continue;

                hist.updateHistogram(cov);


            }
        }

        return hist.getHist();

    }


    public void outputTranscriptsCoverage(String fileName) throws IOException {

        double[] coverageHist = computePerBaseTranscriptCoverageHist();

        XYVector coverageData = new XYVector();

        for (int i = 0; i < coverageHist.length; ++i) {
            coverageData.addItem( new XYItem(i,coverageHist[i]));
        }


        BamQCChart geneCoverage = new BamQCChart("Transcript coverage",
                "Sample", "Transcript position", " Counts ");
        geneCoverage.addSeries("Transcript coverage profile", coverageData, new Color(255, 0, 0, 255));
        geneCoverage.setAdjustDomainAxisLimits(false);
        geneCoverage.setDomainAxisIntegerTicks(true);
        geneCoverage.setShowLegend(false);
        geneCoverage.render();
        QChart chart = new QChart(fileName, geneCoverage.getChart(), geneCoverage);

        BufferedImage bufImage =chart.getJFreeChart().createBufferedImage(
                Constants.GRAPHIC_TO_SAVE_WIDTH,
                Constants.GRAPHIC_TO_SAVE_HEIGHT);

        String imagePath = fileName + ".png";

        File imageFile = new File(imagePath);
        ImageIO.write(bufImage, "PNG", imageFile);

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

        String chartTitle = "Reads genomic origin";
        BamQCPieChart pieChart = new BamQCPieChart(chartTitle, sampleName, dataset);
        pieChart.render();

        return new QChart("Reads origin", pieChart.getJFreeChart() );

    }


    public List<QChart> createPlots(String sampleName) throws IOException {

        ArrayList<QChart> charts = new ArrayList<QChart>();


        {
            QChart readsOriginChart = createReadsOriginPieChart(sampleName);
            charts.add(readsOriginChart);
        }

        TreeMap<Double, int[]> sortedCoverageMap = getSortedTranscriptCoverage();

        {
            double[] perBaseTranscriptCoverage = computePerBaseTranscriptCoverageHist(sortedCoverageMap,
                    sortedCoverageMap.size(), true );
            QChart chart = createCoverageProfilePlot(perBaseTranscriptCoverage,
                    "Coverage Profile (Total)", sampleName);
            charts.add(chart);
        }

        {
            double[] perBaseTranscriptCoverage = computePerBaseTranscriptCoverageHist(sortedCoverageMap,
                    500, true );
            QChart chart = createCoverageProfilePlot(perBaseTranscriptCoverage,
                    "Coverage Profile (Low)", sampleName);
            charts.add(chart);
        }

        {
            double[] perBaseTranscriptCoverage = computePerBaseTranscriptCoverageHist(sortedCoverageMap,
                    500, false );
            QChart chart = createCoverageProfilePlot(perBaseTranscriptCoverage,
                    "Coverage Profile (High)", sampleName);
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

        return charts;

    }

    public void collectJunctionInfo(SAMRecord read, int posInRead) {
        byte[] seq = read.getReadBases();

        if (seq != null && posInRead - 2 > 0 && posInRead + 1 < seq.length) {
            char[] junction = new char[4];
            junction[0] =  (char) seq[posInRead - 2];
            junction[1] =  (char) seq[posInRead - 1];
            junction[2] =  (char) seq[posInRead];
            junction[3] =  (char) seq[posInRead + 1 ];
            String junctionStr = new String(junction);


            Integer count = junctionMap.get(junctionStr);
            if (count == null) {
                junctionMap.put(junctionStr, 1);
            } else {
                junctionMap.put(junctionStr, count + 1);
            }
        }

        numReadsWithJunction += 1;

    }



    public List<JunctionInfo> computeSortedJunctionsMap() {

        List<JunctionInfo> junctionList = new ArrayList<JunctionInfo>();

        if (numReadsWithJunction == 0) {
            return junctionList;
        }

        for (Map.Entry<String, Integer> entry : junctionMap.entrySet()) {

            double percentage = (entry.getValue() * 100.) / numReadsWithJunction;
            junctionList.add( new JunctionInfo(entry.getKey(), percentage));
        }

        Collections.sort(junctionList);

        return junctionList;
    }

    public long getNumReadsWithJunctions() {
        return numReadsWithJunction;
    }

    public Map<String,Gene> getGeneMap() {
        return geneMap;
    }

    public void collectIntronicRead() {
        ++numIntronicReads;
    }

    public void collectIntergenicRead() {
        ++numIntergenicReads;
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
}
