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

    int numBases;
    int numGC;


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

    ReadStatsCollector() {
        readsGcContent = new ArrayList<Float>();
        readsAContent = new int[INITIAL_SIZE];
        readsCContent = new int[INITIAL_SIZE];
        readsGContent = new int[INITIAL_SIZE];
        readsTContent = new int[INITIAL_SIZE];
        readsNContent = new int[INITIAL_SIZE];
        readsClippingContent = new int[INITIAL_SIZE];
    }

    void saveGC() {
        if (numGC != 0) {
            float gcContent = (float)numGC / (float)numBases;
            readsGcContent.add(gcContent);
        }

        numBases = 0;
        numGC = 0;
    }


    public void collectBase(int pos, byte base) {
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

}
