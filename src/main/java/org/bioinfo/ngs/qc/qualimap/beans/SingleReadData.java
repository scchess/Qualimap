/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokonech
 * Date: 11/11/11
 * Time: 11:25 AM
 */
public class SingleReadData {

    public long numberOfSequencedBases;
    public long numberOfMappedBases;
    public long numberOfAs;
    public long numberOfTs;
    public long numberOfCs;
    public long numberOfGs;
    // These number denotes how many bases are aligned from sequenced bases
    //public long numberOfAlignedBases;
    public long acumInsertSize;
    public long numberOfProperlyPairedBases;

    public static class Cell {
        int position;
        int value;
        public Cell(int position, int value) {
            this.position = position;
            this.value = value;
        }
        public int getPosition() {
            return position;
        }

        public int getValue() {
            return value;
        }
    }

    public List<Integer> coverageData;
    public List<Cell> mappingQualityData;

    long windowStart;

    public SingleReadData(long windowStart) {
        this.windowStart = windowStart;
        coverageData = new ArrayList<Integer>();
        mappingQualityData = new ArrayList<Cell>();
    }

    public long getWindowStart() {
        return windowStart;
    }

    public void acumBase(long relative, char base, long insertSize){
		numberOfSequencedBases++;

        // ATCG content
        if(base=='A'){
            acumA(relative);
        } else if(base=='C'){
            acumC(relative);
        } else if(base=='T'){
            acumT(relative);
        } else if(base=='G'){
            acumG(relative);
        }

        /*if ( insertSize != -1 ){
            acumProperlyPairedBase(relative);
        }*/

        coverageData.add((int)relative);
    }


	/*public void acumProperlyPairedBase(long relative){
        //properlyPairedCoverageAcrossReference[(int)relative] = properlyPairedCoverageAcrossReference[(int)relative] + 1;
	}*/

	public void acumA(long relative){
		numberOfAs++;

    }

	public void acumC(long relative){
		numberOfCs++;

    }

	public void acumT(long relative){
		numberOfTs++;

    }

	public void acumG(long relative){
		numberOfGs++;

    }

	public void acumMappingQuality(long relative, int mappingQuality){
		//acumMappingQuality+=mappingQuality;
        if (mappingQuality != 0) {
            mappingQualityData.add( new Cell((int)relative, mappingQuality) );
        }
    }

	public void acumInsertSize(long relative, long insertSize){
		if(insertSize > 0 & insertSize < 5000){
            acumInsertSize += insertSize;
            numberOfProperlyPairedBases++;
			//insertSizeData.add( new Cell((int) relative, (int) insertSize) );
        }

    }

}
