package org.bioinfo.ngs.qc.qualimap.utils;

/**
 * Created by kokonech
 * Date: 2/9/12
 * Time: 11:14 AM
 */

public class ReadStartsHistogram {

    public static final int MAX_READ_STARTS_PER_POSITION = 50;
    long currentReadStartPosition;
    int readStartCounter;
    long[] readStartsHistogram;

    public ReadStartsHistogram() {
        readStartsHistogram = new long[MAX_READ_STARTS_PER_POSITION + 1];
        readStartCounter = 1;
        currentReadStartPosition = -1;
    }

    public void update( long position ) {
        if (position == currentReadStartPosition) {
            readStartCounter++;
        } else {
            int histPos = readStartCounter < MAX_READ_STARTS_PER_POSITION ?  readStartCounter :
                    MAX_READ_STARTS_PER_POSITION;
            readStartsHistogram[histPos]++;
            readStartCounter = 1;
            currentReadStartPosition = position;
        }
    }

    public long[] getHistorgram() {
        return readStartsHistogram;
    }
}
