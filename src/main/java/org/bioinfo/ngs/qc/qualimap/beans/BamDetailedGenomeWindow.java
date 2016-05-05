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
package org.bioinfo.ngs.qc.qualimap.beans;


//TODO: BamDetailedGenomeWindow has to be removed, all processing can be done in BamGenomeWindow


public class BamDetailedGenomeWindow extends BamGenomeWindow {

	// reference sequence
	private byte[] reference;

	// coverageData
	private int[] coverageAcrossReference;

	// quality
	private long[] mappingQualityAcrossReference;

    // required for calculation of mean and std of coverage
    private long sumCoverageSquared;
    private long sumCoverage;


	public BamDetailedGenomeWindow(String name, long start, long end, byte[] reference){

        super(name,start,end,reference);
		
		if(reference!=null){
			this.reference = reference;
		}

        // init arrays
		coverageAcrossReference = new int[(int)this.windowSize];
		mappingQualityAcrossReference = new long[(int)this.windowSize];
//		sequencingQualityAcrossReference = new int[this.windowSize];
//		aContentAcrossReference = new long[(int)this.windowSize];
//		cContentAcrossReference = new long[(int)this.windowSize];
//		gContentAcrossReference = new long[(int)this.windowSize];
//		tContentAcrossReference = new long[(int)this.windowSize];
//		nContentAcrossReference = new long[(int)this.windowSize];
//		gcContentAcrossReference = new double[(int)this.windowSize];
//		atContentAcrossReference = new double[(int)this.windowSize];

	}
	
	@Override
	protected void acumBase(long relative){
		super.acumBase(relative);
		coverageAcrossReference[(int)relative] = coverageAcrossReference[(int)relative]+1;
	}
	
	@Override
	protected void acumProperlyPairedBase(long relative){
		super.acumProperlyPairedBase(relative);
		//properlyPairedCoverageAcrossReference[(int)relative] = properlyPairedCoverageAcrossReference[(int)relative] + 1;
	}

	/*@Override
	protected void acumMappingQuality(long relative, int mappingQuality){
		super.acumMappingQuality(relative,mappingQuality);		
		// quality					
		mappingQualityAcrossReference[(int)relative] = mappingQualityAcrossReference[(int)relative]+mappingQuality;
	}*/

	@Override
	public void computeDescriptors() throws CloneNotSupportedException{

        super.computeDescriptors();

		// normalize vectors

        sumCoverage = 0;

		for(int i=0; i<coverageAcrossReference.length; i++){

			long coverageAtPosition =  coverageAcrossReference[i];

            if(coverageAtPosition > 0){

				// quality
				mappingQualityAcrossReference[i] = mappingQualityAcrossReference[i] / coverageAtPosition;
                // insert size
                // System.err.println(properlyPairedCoverageAcrossReference[i]);
                // insertSizeAcrossReference[i] = insertSizeAcrossReference[i]/(double)properlyPairedCoverageAcrossReference[i];

                sumCoverageSquared += coverageAtPosition*coverageAtPosition;
                sumCoverage += coverageAtPosition;
                //numberOfProperlyPairedMappedBases++;
				
			} else {
                // make it invalid for histogram
                 mappingQualityAcrossReference[i] = -1;
            }

		}
				
		// compute std coverageData

        long meanCoverage = sumCoverage / coverageAcrossReference.length;
        stdCoverage = Math.sqrt( (double) sumCoverageSquared / coverageAcrossReference.length - meanCoverage*meanCoverage);

	}

    @Override
    public void addReadAlignmentData(SingleReadData readData) {
        super.addReadAlignmentData(readData);
        for (int pos : readData.coverageData) {
            coverageAcrossReference[pos]++;
        }

        for (SingleReadData.Cell cell : readData.mappingQualityData) {
            int pos = cell.getPosition();
            int val = cell.getValue();
            mappingQualityAcrossReference[pos] += val;
            super.acumMappingQuality += val;
        }

    }

    /**
	 * @return the coverageAcrossReference
	 */
	public int[] getCoverageAcrossReference() {
		return coverageAcrossReference;
	}

    public long getSumCoverageSquared() {
        return sumCoverageSquared;
    }

    public long getSumCoverage() {
        return sumCoverage;
    }

	/**
	 * @return the mappingQualityAcrossReference
	 */
	public long[] getMappingQualityAcrossReference() {
		return mappingQualityAcrossReference;
	}





}