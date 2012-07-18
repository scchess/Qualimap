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
package org.bioinfo.ngs.qc.qualimap.common;

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
