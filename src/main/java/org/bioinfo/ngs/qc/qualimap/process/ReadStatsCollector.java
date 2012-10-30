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

import java.util.ArrayList;

/**
 * Created by kokonech
 * Date: 6/15/12
 * Time: 11:58 AM
 */
public class ReadStatsCollector {

    private static int[] ensureArraySize(int[] array, int pos) {
        int size = array.length;
        if (pos >= size) {
            int new_size = size*2 < pos + 1 ? pos + 1 : size*2;
            int[] new_array = new int[new_size];
            System.arraycopy(array, 0, new_array, 0, array.length);
            return new_array;
        }
        return array;
    }

    private static final int INITIAL_SIZE = 64;

    int[] readsAContent;
    int[] readsCContent;
    int[] readsGContent;
    int[] readsTContent;
    int[] readsNContent;
    int[] readsClippingContent;
    ArrayList<Float> readsGcContent;
    int[] homopolymerIndels;

    int numClippedReads;
    int numInsertions;
    int numDeletions;

    int numBases;
    int numGC;
    int minHomopolymerSize;
    boolean prevBaseInsideIndelRegion, homopolymerStartsInsideIndelRegion;

    // per read counters
    byte prevBase;
    int homopolymerSize;

    public int[] getReadsAContent() {
        return readsAContent;
    }

    public int[] getReadsCContent() {
        return readsCContent;
    }

    public int[] getReadsGContent() {
        return readsGContent;
    }

    public int[] getReadsTContent() {
        return readsTContent;
    }

    public int[] getReadsNContent() {
        return readsNContent;
    }

    public ArrayList<Float> getReadsGcContent() {
        return readsGcContent;
    }

    public int[] getReadsClippingInfo() {
        return readsClippingContent;
    }

    ReadStatsCollector(int homopolymerSize) {
        readsGcContent = new ArrayList<Float>();
        readsAContent = new int[INITIAL_SIZE];
        readsCContent = new int[INITIAL_SIZE];
        readsGContent = new int[INITIAL_SIZE];
        readsTContent = new int[INITIAL_SIZE];
        readsNContent = new int[INITIAL_SIZE];
        readsClippingContent = new int[INITIAL_SIZE];
        homopolymerIndels = new int[5];
        minHomopolymerSize = homopolymerSize;
    }

    void saveGC() {
        if (numGC != 0) {
            float gcContent = (float)numGC / (float)numBases;
            readsGcContent.add(gcContent);
        }

        numBases = 0;
        numGC = 0;
    }


    public void collectBase(int pos, byte base, boolean insideIndelRegion) {
        if (base == 'A') {
            incAsContent(pos);
            numBases++;
        } else if (base == 'C') {
            incCsContent(pos);
            numGC++;
            numBases++;
        } else if (base == 'G') {
            incGsContent(pos);
            numGC++;
            numBases++;
        } else if (base == 'T') {
            incTsContent(pos);
            numBases++;
        } else if (base == 'N') {
            incNsContent(pos);
        }

        if (numBases >= 1000) {
            saveGC();
        }

        updateHomopolymerStats(base, insideIndelRegion);
    }


    void updateHomopolymerStats(byte base, boolean  insideIndelRegion) {

        if (base == prevBase) {
            homopolymerSize ++;
        } else {
            if (  prevBaseInsideIndelRegion || homopolymerStartsInsideIndelRegion ) {
                if (homopolymerSize >= minHomopolymerSize) {
                    saveHomopolymerData();
                }
            }
            homopolymerSize = 1;
            homopolymerStartsInsideIndelRegion = insideIndelRegion;
        }

        prevBase = base;
        prevBaseInsideIndelRegion = insideIndelRegion;
    }


    private void saveHomopolymerData() {
        if (prevBase == 'A') {
            homopolymerIndels[0]++;
        } else if (prevBase == 'C') {
            homopolymerIndels[1]++;
        } else if (prevBase == 'G') {
            homopolymerIndels[2]++;
        } else if (prevBase == 'T') {
            homopolymerIndels[3]++;
        } else if (prevBase == 'N') {
            homopolymerIndels[4]++;
        }
    }

    public void collectDeletedBase( byte nextBase) {
        if (nextBase != prevBase) {
            if (homopolymerSize + 1 >= minHomopolymerSize )  {
                saveHomopolymerData();
                homopolymerStartsInsideIndelRegion = false;
            } else {
                homopolymerStartsInsideIndelRegion = true;
            }
            prevBase = nextBase;
            homopolymerSize = 1;
        } else {
            homopolymerSize += 1;
        }
    }

    public void resetCounters() {
        prevBase = 'X';
        homopolymerSize = 1;
        prevBaseInsideIndelRegion = false;
        homopolymerStartsInsideIndelRegion = false;
    }

    private void incAsContent(int pos) {
        readsAContent = ensureArraySize(readsAContent, pos);
        readsAContent[pos]++;
    }

    private void incGsContent(int pos) {
        readsGContent = ensureArraySize(readsGContent, pos);
        readsGContent[pos]++;
    }

    private void incCsContent(int pos) {
        readsCContent = ensureArraySize(readsCContent, pos);
        readsCContent[pos]++;
    }

    private void incTsContent(int pos) {
        readsTContent = ensureArraySize(readsTContent, pos);
        readsTContent[pos]++;
    }

    private void incNsContent(int pos) {
        readsNContent = ensureArraySize(readsNContent, pos);
        readsNContent[pos]++;
    }

    public void incClippingContent(int pos){
        readsClippingContent = ensureArraySize(readsClippingContent, pos);
        readsClippingContent[pos]++;
    }

    public void incNumClippedReads() {
        ++numClippedReads;
    }

    public void incNumInsertions() {
        ++numInsertions;
    }

    public void incNumDeletions() {
        ++numDeletions;
    }

    public int getNumClippedReads() {
        return numClippedReads;
    }

    public int[] getHomopolymerIndels() {
        return homopolymerIndels;
    }

    public int getNumIndels() {
        return numInsertions + numDeletions;
    }

    public int getNumInsertions() {
        return numInsertions;
    }

    public int getNumDeletions() {
        return numDeletions;
    }


}
