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

import java.util.BitSet;

public class BamGenomeWindow {
	protected String name;

	// window params
	protected long start;
	protected long end;
	protected long windowSize;

	// general
	protected long numberOfMappedBases;
	protected long numberOfSequencedBases;
	protected long numberOfAlignedBases;

	// gff-like
	protected boolean selectedRegionsAvailable;
	protected BitSet selectedRegions;

	/*
	 * 
	 * Reference params
	 * 
	 */
	protected boolean referenceAvailable;	
	
	// A content
	protected long numberOfAsInReference;
	protected double aRelativeContentInReference;
	
	// C content
	protected long numberOfCsInReference;
	protected double cRelativeContentInReference;
	
	// T content
	protected long numberOfTsInReference;
	protected double tRelativeContentInReference;
	
	// G content
	protected long numberOfGsInReference;
	protected double gRelativeContentInReference;
	
	// N content
	protected long numberOfNsInReference;
	protected double nRelativeContentInReference;
	
	// GC content
	protected long numberOfGcsInReference;
	protected double gcRelativeContentInReference;
	
	// AT content
	protected long numberOfAtsInReference;
	protected double atRelativeContentInReference;
	
	/*
	 * 
	 * Sample params 
	 * 
	 */
	
	// coverageData
	protected double meanCoverage;
	protected double stdCoverage;	
		
	// quality	
	protected double acumMappingQuality;
	protected double meanMappingQuality;

	// A content
	protected long numberOfAs;
	protected double meanAContent;
	protected double meanARelativeContent;
	
	// C content
	protected long numberOfCs;
	protected double meanCContent;
	protected double meanCRelativeContent;
	
	// G content
	protected long numberOfTs;
	protected double meanGContent;
	protected double meanGRelativeContent;
	
	// T content
	protected long numberOfGs;
	protected double meanTContent;
	protected double meanTRelativeContent;
	
	// N content	
	protected long numberOfNs;
	protected double meanNContent;
	protected double meanNRelativeContent;
	
	// GC content
	protected double meanGcContent;
	protected double meanGcRelativeContent;

	protected int correctInsertSizes;
	protected double acumInsertSize;
	protected double meanInsertSize;
    protected long effectiveWindowLength;

	public BamGenomeWindow(String name, long start, long end, byte[] reference){
		this.name = name;
		
		// windows params
		this.start = start;
		this.end = end;
		this.windowSize = (end - start + 1);
				
		// process reference
		if(reference!=null){
			processReference(reference);
		}
		
		// init counters
		numberOfMappedBases = 0;
		numberOfSequencedBases = 0;
		numberOfAlignedBases = 0;

	}
	
	public void processReference(byte[] reference){
		char nucleotide;
		for(long i=0; i<reference.length; i++){
			nucleotide = (char)reference[(int)i];
			if(nucleotide=='A'){
				numberOfAsInReference++;
			}
			else if(nucleotide=='C'){
				numberOfCsInReference++;
			}
			else if(nucleotide=='T'){
				numberOfTsInReference++;
			}
			else if(nucleotide=='G'){
				numberOfGsInReference++;
			}
			else if(nucleotide=='N'){
				numberOfNsInReference++;
			}
		}
		numberOfGcsInReference = numberOfGsInReference + numberOfCsInReference;
		numberOfAtsInReference = numberOfAsInReference + numberOfTsInReference;
		
		// relative contents
		aRelativeContentInReference = ((double)numberOfAsInReference/(double)windowSize)*100.0;
		cRelativeContentInReference = ((double)numberOfCsInReference/(double)windowSize)*100.0;
		tRelativeContentInReference = ((double)numberOfTsInReference/(double)windowSize)*100.0;
		gRelativeContentInReference = ((double)numberOfGsInReference/(double)windowSize)*100.0;
		nRelativeContentInReference = ((double)numberOfNsInReference/(double)windowSize)*100.0;
		gcRelativeContentInReference = ((double)numberOfGcsInReference/(double)windowSize)*100.0;
		atRelativeContentInReference = ((double)numberOfAtsInReference/(double)windowSize)*100.0;
		
		referenceAvailable = true;
	}

	
	protected void acumBase(long relative){
		numberOfSequencedBases++;
		numberOfMappedBases++;
	}
	
	protected void acumProperlyPairedBase(long relative){
		
	}
	
	protected void acumA(long relative){
		numberOfAs++;
	}
	
	protected void acumC(long relative){
		numberOfCs++;
	}
	
	protected void acumT(long relative){
		numberOfTs++;
	}
	
	protected void acumG(long relative){
		numberOfGs++;
	}
	
	protected void acumMappingQuality(long relative, int mappingQuality){
		acumMappingQuality+=mappingQuality;
	}
	
	public void acumInsertSize(long insertSize){
		if(insertSize > 0 /*& insertSize<5000*/){
			correctInsertSizes++;
			acumInsertSize += Math.abs(insertSize);
		}
	}

    public void computeDescriptors() throws CloneNotSupportedException{
		
		// org.bioinfo.ntools.process acums		

        effectiveWindowLength = windowSize;
        if ( selectedRegionsAvailable ) {
            effectiveWindowLength = selectedRegions.cardinality();
        }

        if (effectiveWindowLength != 0) {
            meanCoverage = (double) numberOfMappedBases / effectiveWindowLength;
        } else {
            meanCoverage = 0;
        }


        if (correctInsertSizes > 0) {
            meanInsertSize = acumInsertSize/(double)correctInsertSizes;
        }

		// ACTG absolute content
		if(meanCoverage > 0){

            meanMappingQuality = acumMappingQuality/(double)numberOfMappedBases;

            //meanARelativeContent = numberOfAs / numberOfMappedBases * 100.0

			meanAContent = (double)numberOfAs/ meanCoverage;
			meanCContent = (double)numberOfCs/ meanCoverage;
			meanTContent = (double)numberOfTs/ meanCoverage;
			meanGContent = (double)numberOfGs/ meanCoverage;
			meanNContent = (double)numberOfNs/ meanCoverage;
			meanGcContent = meanCContent + meanGContent; //(double)(numberOfGs+numberOfCs)/(double)meanCoverage;
            //
            // meanAtContent = meanAContent + meanTContent; //(double)(numberOfAs+numberOfTs)/(double)meanCoverage;
			// ACTG relative content
			double acumMeanContent = meanAContent + meanCContent + meanTContent + meanGContent + meanNContent;
			meanARelativeContent = ( meanAContent / acumMeanContent)*100.0;
			meanCRelativeContent = ( meanCContent / acumMeanContent)*100.0;
			meanTRelativeContent = ( meanTContent / acumMeanContent)*100.0;
			meanGRelativeContent = ( meanGContent / acumMeanContent)*100.0;
			meanNRelativeContent = ( meanNContent / acumMeanContent)*100.0;
			meanGcRelativeContent =  meanCRelativeContent + meanGRelativeContent;
//			meanAtRelativeContent = meanARelativeContent + meanTRelativeContent;			
		}

	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}
	
	/**
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}
	
	/**
	 * @return the windowSize
	 */
	public long getWindowSize() {
		return windowSize;
	}
	
	/**

	/**
	 * @return the numberOfMappedBases
	 */
	public long getNumberOfMappedBases() {
		return numberOfMappedBases;
	}
	
	/**
	 * @return the numberOfSequencedBases
	 */
	public long getNumberOfSequencedBases() {
		return numberOfSequencedBases;
	}
	
	/**
	 * @return the numberOfAlignedBases
	 */
	public long getNumberOfAlignedBases() {
		return numberOfAlignedBases;
	}
	
	/**
	 * @param selectedRegionsAvailable the selectedRegionsAvailable to set
	 */
	public void setSelectedRegionsAvailable(boolean selectedRegionsAvailable) {
		this.selectedRegionsAvailable = selectedRegionsAvailable;
	}
	
	/**
	 * @return the selectedRegions
	 */
	public BitSet getSelectedRegions() {
		return selectedRegions;
	}
	
	/**
	 * @param selectedRegions the selectedRegions to set
	 */
	public void setSelectedRegions(BitSet selectedRegions) {
		this.selectedRegions = selectedRegions;
	}

	/**
	 * @return the numberOfAsInReference
	 */
	public long getNumberOfAsInReference() {
		return numberOfAsInReference;
	}
	
	/**
	 * @return the aRelativeContentInReference
	 */
	public double getaRelativeContentInReference() {
		return aRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfCsInReference
	 */
	public long getNumberOfCsInReference() {
		return numberOfCsInReference;
	}
	
	/**
	 * @return the cRelativeContentInReference
	 */
	public double getcRelativeContentInReference() {
		return cRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfTsInReference
	 */
	public long getNumberOfTsInReference() {
		return numberOfTsInReference;
	}
	
	/**
	 * @return the tRelativeContentInReference
	 */
	public double gettRelativeContentInReference() {
		return tRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfGsInReference
	 */
	public long getNumberOfGsInReference() {
		return numberOfGsInReference;
	}
	
	/**
	 * @return the gRelativeContentInReference
	 */
	public double getgRelativeContentInReference() {
		return gRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfNsInReference
	 */
	public long getNumberOfNsInReference() {
		return numberOfNsInReference;
	}
	
	/**
	 * @return the nRelativeContentInReference
	 */
	public double getnRelativeContentInReference() {
		return nRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfGcsInReference
	 */
	public long getNumberOfGcsInReference() {
		return numberOfGcsInReference;
	}
	
	/**
	 * @return the gcRelativeContentInReference
	 */
	public double getGcRelativeContentInReference() {
		return gcRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfAtsInReference
	 */
	public long getNumberOfAtsInReference() {
		return numberOfAtsInReference;
	}
	
	/**
	 * @return the atRelativeContentInReference
	 */
	public double getAtRelativeContentInReference() {
		return atRelativeContentInReference;
	}
	
	/**
	 * @return the meanCoverage
	 */
	public double getMeanCoverage() {
		return meanCoverage;
	}
	
	/**
	 * @return the stdCoverage
	 */
	public double getStdCoverage() {
		return stdCoverage;
	}
	
	/**
	 * @return the meanMappingQuality
	 */
	public double getMeanMappingQuality() {
		return meanMappingQuality;
	}
	
	/**
	 * @return the numberOfAs
	 */
	public long getNumberOfAs() {
		return numberOfAs;
	}
	
	/**
	 * @return the meanAContent
	 */
	public double getMeanAContent() {
		return meanAContent;
	}
	
	/**
	 * @return the meanARelativeContent
	 */
	public double getMeanARelativeContent() {
		return meanARelativeContent;
	}
	
	/**
	 * @return the numberOfCs
	 */
	public long getNumberOfCs() {
		return numberOfCs;
	}
	
	/**
	 * @return the meanCContent
	 */
	public double getMeanCContent() {
		return meanCContent;
	}
	
	/**
	 * @return the meanCRelativeContent
	 */
	public double getMeanCRelativeContent() {
		return meanCRelativeContent;
	}

	
	/**
	 * @return the numberOfTs
	 */
	public long getNumberOfTs() {
		return numberOfTs;
	}

	/**
	 * @return the meanGContent
	 */
	public double getMeanGContent() {
		return meanGContent;
	}

	/**
	 * @return the meanGRelativeContent
	 */
	public double getMeanGRelativeContent() {
		return meanGRelativeContent;
	}
	
	/**
	 * @return the numberOfGs
	 */
	public long getNumberOfGs() {
		return numberOfGs;
	}

	
	/**
	 * @return the meanTContent
	 */
	public double getMeanTContent() {
		return meanTContent;
	}

	
	/**
	 * @return the meanTRelativeContent
	 */
	public double getMeanTRelativeContent() {
		return meanTRelativeContent;
	}

	
	/**
	 * @return the numberOfNs
	 */
	public long getNumberOfNs() {
		return numberOfNs;
	}

	
	/**
	 * @return the meanNContent
	 */
	public double getMeanNContent() {
		return meanNContent;
	}

	
	/**
	 * @return the meanNRelativeContent
	 */
	public double getMeanNRelativeContent() {
		return meanNRelativeContent;
	}

	
	/**
	 * @return the meanGcContent
	 */
	public double getMeanGcContent() {
		return meanGcContent;
	}

	
	/**
	 * @return the meanGcRelativeContent
	 */
	public double getMeanGcRelativeContent() {
		return meanGcRelativeContent;
	}


	/**
	 * @return the meanInsertSize
	 */
	public double getMeanInsertSize() {
		return meanInsertSize;
	}

	public void inverseRegions() {
        selectedRegions.flip(0, selectedRegions.size());
    }

    public void addReadAlignmentData(SingleReadData readData) {

        numberOfMappedBases += readData.numberOfMappedBases;
        numberOfSequencedBases += readData.numberOfSequencedBases;

        numberOfAs += readData.numberOfAs;
        numberOfCs += readData.numberOfCs;
        numberOfGs += readData.numberOfGs;
        numberOfTs += readData.numberOfTs;


    }

    public long getEffectiveWindowLength() {
        return effectiveWindowLength;
    }
}