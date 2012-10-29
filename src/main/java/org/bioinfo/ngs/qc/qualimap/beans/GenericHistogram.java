package org.bioinfo.ngs.qc.qualimap.beans;

/**
 * Created by kokonech
 * Date: 10/5/12
 * Time: 11:21 AM
 */
public class GenericHistogram {


    final int numBins;
    double[] hist;
    boolean normalize;

    public GenericHistogram(int numBins, boolean normalize) {
        this.numBins  = numBins;
        this.hist = new double[numBins];
        this.normalize = normalize;
    }

    public void updateHistogram(int[] data) {

        final int norm = normalize ? data.length : 1;

        final int step = (int) Math.ceil( data.length / (double) numBins );
        int binCoverage = 0;
        int binIndex = 0;
        int count = step;

        for (int i = 0; i < data.length; ++i) {

            binCoverage += data[i];

            if (i + 1 == count || i == data.length - 1) {
                hist[binIndex] += binCoverage / (double) norm;
                ++binIndex;
                binCoverage = 0;
                count += step;

            }

        }

    }


    public double[] getHist() {
        return hist;
    }


}
