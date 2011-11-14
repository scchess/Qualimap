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
    public long numberOfMappedBasesSquared;
    public long acumMappingQuality;
    public long correctInsertSizes;
    public long acumInsertSize;
    public long numberOfAs;
    public long numberOfTs;
    public long numberOfCs;
    public long numberOfGs;

    public long numberOfProcessedReads;
    public long numberOfOutOfBoundsReads;
    public long numberOfAlignedBases;

    public class Cell {
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
    public List<Cell> insertSizeData;

    long windowStart, windowSize;

    public SingleReadData(long windowStart, long windowSize) {
        this.windowStart = windowStart;
        this.windowSize = windowSize;
        coverageData = new ArrayList<Integer>();
        mappingQualityData = new ArrayList<Cell>();
        insertSizeData = new ArrayList<Cell>();
    }

    public long getWindowStart() {
        return windowStart;
    }

    public void acumBase(long relative){
		numberOfSequencedBases++;
		numberOfMappedBases++;
        numberOfMappedBasesSquared++;
        coverageData.add((int)relative);
    }

	public void acumProperlyPairedBase(long relative){
        //properlyPairedCoverageAcrossReference[(int)relative] = properlyPairedCoverageAcrossReference[(int)relative] + 1;
	}

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
		acumMappingQuality+=mappingQuality;
        //TODO: check how the histogram is calculated!
        if (mappingQuality != 0) {
            mappingQualityData.add( new Cell((int)relative, mappingQuality) );
        }
    }

	public void acumInsertSize(long relative, long insertSize){
		if(insertSize>0 & insertSize<5000){
			correctInsertSizes++;
			acumInsertSize+=Math.abs(insertSize);
		}
        insertSizeData.add( new Cell((int) relative, (int) Math.abs(insertSize)) );
    }

    public void processReference(byte[] reference){
	    //TODO: implement this
    }





}
