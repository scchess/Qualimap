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
