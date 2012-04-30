package org.bioinfo.ngs.qc.qualimap.beans;

import java.util.Arrays;
import java.util.List;
import java.util.BitSet;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

import org.bioinfo.ngs.qc.qualimap.process.Region;

public class BamGenomeWindow {
	protected String name;

	// window params
	protected long start;
	protected long end;
	protected long windowSize;

	// input reads
	protected long numberOfProcessedReads;
	protected long numberOfOutOfBoundsReads;


	// general 
	protected long numberOfMappedBases;
	protected long numberOfSequencedBases;
	protected long numberOfAlignedBases;

	// gff-like
	protected boolean selectedRegionsAvailable;
	protected BitSet selectedRegions;
	protected List<Region> selectedRegionList;	

	// working variables	
	//protected HashMap<String,Character> charMap;
	
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
	protected double meanProperlyPairedCoverage;
	protected long numberOfProperlyPairedMappedBases;
	protected double meanCoverage;
	protected double stdCoverage;	
		
	// quality	
	protected double acumMappingQuality;
	protected double meanMappingQuality;
	protected double meansequencingQuality;
			
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

//	// AT content
//	protected double meanAtContent;
//	protected double meanAtRelativeContent;
	
	protected int correctInsertSizes;
	protected double acumInsertSize;
	protected double meanInsertSize;
    protected double effectiveWindowLength;

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
	
	public boolean acumRead(SAMRecord read, String alignment, GenomeLocator locator){		
		return processRead(read,alignment,locator);
	}
	
	public boolean acumRead(SAMRecord read, GenomeLocator locator){
		String  alignment = new String(computeAlignment(read));
		return processRead(read,alignment,locator);
	}
	
	public boolean acumRead(String alignment, long readStart, long readEnd, int mappingQuality, long insertSize){
		return processRead(alignment,readStart,readEnd,mappingQuality,insertSize);
	}
	
	protected boolean processRead(SAMRecord read, String alignment, GenomeLocator locator){
		long readStart = locator.getAbsoluteCoordinates(read.getReferenceName(),read.getAlignmentStart());
		long readEnd = locator.getAbsoluteCoordinates(read.getReferenceName(), read.getAlignmentEnd());
		return processRead(alignment, readStart, readEnd, read.getMappingQuality(),read.getInferredInsertSize());
	}
	
	protected boolean processRead(String alignment, long readStart, long readEnd, int mappingQuality, long insertSize){
		
		// working variables
		boolean outOfBounds = false;
		long relative;
		long pos;
	
		if(readEnd<readStart){
			System.err.println("WARNING: read aligment start is greater than end: " + readStart + " > " + readEnd);
		}
		
		// acums
		numberOfProcessedReads++;
		if(readEnd>end){
			outOfBounds = true;
			numberOfOutOfBoundsReads++;
		}
		
		// TO FIX
		if(readStart>=start) {			
//			numberOfSequencedBases+=read.getReadBases().length;
//			numberOfCigarElements+=read.getCigar().numCigarElements();
		}
				
		char nucleotide;
		// run read	
		for(long j=readStart; j<=readEnd; j++){
			relative = (int)(j-start);
			pos = (int)(j-readStart);
							
			if(relative<0){
				
			} else if(relative>=windowSize){
				//	System.err.println("WARNING: " + read.getReadName() + " is fuera del tiesto " + relative);
			} else {
				if(!selectedRegionsAvailable || (selectedRegionsAvailable && selectedRegions.get((int)relative))) {
					nucleotide = alignment.charAt((int)pos);
					 
					// aligned bases
					numberOfAlignedBases++;
					
					// Any letter
					if(nucleotide=='A' || nucleotide=='C' || nucleotide=='T' || nucleotide=='G'){						
						acumBase(relative);
						if(insertSize!=-1){
							acumProperlyPairedBase(relative);
						}
					}
					
					// ATCG content
					if(nucleotide=='A'){
						acumA(relative);
					} 
					else if(nucleotide=='C'){
						acumC(relative);
					}
					else if(nucleotide=='T'){
						acumT(relative);					
					}
					else if(nucleotide=='G'){
						acumG(relative);				
					}
					else if(nucleotide=='-'){
					}
					else if(nucleotide=='N'){
					}
					
					// mapping quality
					acumMappingQuality(relative, mappingQuality);
					
					// insert size
					acumInsertSize(relative, insertSize);
					
				}
			}
		}
		
		return outOfBounds;
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
	
	protected void acumInsertSize(long relative, long insertSize){
		if(insertSize>0 & insertSize<5000){
			correctInsertSizes++;
			acumInsertSize+=Math.abs(insertSize);
		}
	}
	
	public static char[] computeAlignment(SAMRecord read){
		// init read params
		int alignmentLength = (read.getAlignmentEnd()-read.getAlignmentStart()+1);		

        if (alignmentLength < 0) {
            return null;
        }


		Cigar cigar = read.getCigar();
		
		// precompute total size of alignment
		int totalSize = 0;
        List<CigarElement> elementList = cigar.getCigarElements();
        //int numCigarElements = cigar.numCigarElements();
		for(CigarElement element : elementList){
			totalSize += element.getLength();
		}

		// compute extended cigar
		char[] extended = new char[totalSize];
		int mpos = 0;
		int npos;
        for(CigarElement element : elementList){
		    npos = mpos + element.getLength();
			Arrays.fill(extended, mpos, npos, element.getOperator().name().charAt(0));
			mpos = npos;
		}

		// init extended cigar portion
		char[] extendedCigarVector = extended; // Arrays.copyOfRange(extended,0,mpos);

		char[] alignmentVector = new char[alignmentLength];

		int readPos = 0;
		int alignmentPos = 0;
		char base;
		byte[] readBases = read.getReadBases();
		
		for(int i=0; i<extendedCigarVector.length; i++){
			// M
			if(extendedCigarVector[i]=='M'){				
				// get base
				base = (char)readBases[readPos];
				readPos++;				
				// set base
				alignmentVector[alignmentPos] = base;
				alignmentPos++;
			}
			// I
			else if(extendedCigarVector[i]=='I'){
			    readPos++;
			}
			// D
			else if(extendedCigarVector[i]=='D'){
				alignmentVector[alignmentPos] = '-'; 
				alignmentPos++;
			}
			// N
			else if(extendedCigarVector[i]=='N'){
				alignmentVector[alignmentPos] = 'N';
				alignmentPos++;
			}
			// S
			else if(extendedCigarVector[i]=='S'){
				readPos++;				
			}
			// H
			else if(extendedCigarVector[i]=='H'){

			}
			// P
			else if(extendedCigarVector[i]=='P'){
				alignmentVector[alignmentPos] = '-';
				alignmentPos++;
			}
		}
	
		return alignmentVector;
	}

    public void computeDescriptors() throws CloneNotSupportedException{
		
		// org.bioinfo.ntools.process acums		


        effectiveWindowLength = windowSize;
        if (selectedRegionsAvailable && numberOfMappedBases != 0) {
            int len = 0;
            for (int i = 0; i< windowSize; ++i )
             {
                if (selectedRegions.get(i)) {
                    len++;
                }
            }
            effectiveWindowLength = len;
        }

        meanCoverage = (double) numberOfMappedBases / effectiveWindowLength;

		meanMappingQuality = acumMappingQuality/(double)numberOfMappedBases;

        meanInsertSize = acumInsertSize/(double)numberOfProperlyPairedMappedBases;


		// ACTG absolute content
		if(meanCoverage > 0){

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
			meanARelativeContent = ((double)meanAContent/acumMeanContent)*100.0;
			meanCRelativeContent = ((double)meanCContent/acumMeanContent)*100.0;
			meanTRelativeContent = ((double)meanTContent/acumMeanContent)*100.0;  
			meanGRelativeContent = ((double)meanGContent/acumMeanContent)*100.0;
			meanNRelativeContent = ((double)meanNContent/acumMeanContent)*100.0;
			meanGcRelativeContent = meanCRelativeContent + meanGRelativeContent;
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
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}
	
	/**
	 * @param start the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}
	
	/**
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}
	
	/**
	 * @param end the end to set
	 */
	public void setEnd(long end) {
		this.end = end;
	}
	
	/**
	 * @return the windowSize
	 */
	public long getWindowSize() {
		return windowSize;
	}
	
	/**
	 * @param windowSize the windowSize to set
	 */
	public void setWindowSize(long windowSize) {
		this.windowSize = windowSize;
	}
	
	/**
	 * @return the numberOfProcessedReads
	 */
	public long getNumberOfProcessedReads() {
		return numberOfProcessedReads;
	}

	
	/**
	 * @return the numberOfOutOfBoundsReads
	 */
	public long getNumberOfOutOfBoundsReads() {
		return numberOfOutOfBoundsReads;
	}

	
	/**
	 * @return the numberOfMappedBases
	 */
	public long getNumberOfMappedBases() {
		return numberOfMappedBases;
	}
	
	/**
	 * @param numberOfMappedBases the numberOfMappedBases to set
	 */
	public void setNumberOfMappedBases(long numberOfMappedBases) {
		this.numberOfMappedBases = numberOfMappedBases;
	}
	
	/**
	 * @return the numberOfSequencedBases
	 */
	public long getNumberOfSequencedBases() {
		return numberOfSequencedBases;
	}
	
	/**
	 * @param numberOfSequencedBases the numberOfSequencedBases to set
	 */
	public void setNumberOfSequencedBases(long numberOfSequencedBases) {
		this.numberOfSequencedBases = numberOfSequencedBases;
	}
	
	/**
	 * @return the numberOfAlignedBases
	 */
	public long getNumberOfAlignedBases() {
		return numberOfAlignedBases;
	}
	
	/**
	 * @param numberOfAlignedBases the numberOfAlignedBases to set
	 */
	public void setNumberOfAlignedBases(long numberOfAlignedBases) {
		this.numberOfAlignedBases = numberOfAlignedBases;
	}
	
	/**
	 * @return the selectedRegionsAvailable
	 */
	public boolean isSelectedRegionsAvailable() {
		return selectedRegionsAvailable;
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
	 * @return the selectedRegionList
	 */
	public List<Region> getSelectedRegionList() {
		return selectedRegionList;
	}
	
	/**
	 * @param selectedRegionList the selectedRegionList to set
	 */
	public void setSelectedRegionList(List<Region> selectedRegionList) {
		this.selectedRegionList = selectedRegionList;
	}

	/**
	 * @return the referenceAvailable
	 */
	public boolean isReferenceAvailable() {
		return referenceAvailable;
	}
	
	/**
	 * @param referenceAvailable the referenceAvailable to set
	 */
	public void setReferenceAvailable(boolean referenceAvailable) {
		this.referenceAvailable = referenceAvailable;
	}
	
	/**
	 * @return the numberOfAsInReference
	 */
	public long getNumberOfAsInReference() {
		return numberOfAsInReference;
	}
	
	/**
	 * @param numberOfAsInReference the numberOfAsInReference to set
	 */
	public void setNumberOfAsInReference(long numberOfAsInReference) {
		this.numberOfAsInReference = numberOfAsInReference;
	}
	
	/**
	 * @return the aRelativeContentInReference
	 */
	public double getaRelativeContentInReference() {
		return aRelativeContentInReference;
	}
	
	/**
	 * @param aRelativeContentInReference the aRelativeContentInReference to set
	 */
	public void setaRelativeContentInReference(double aRelativeContentInReference) {
		this.aRelativeContentInReference = aRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfCsInReference
	 */
	public long getNumberOfCsInReference() {
		return numberOfCsInReference;
	}
	
	/**
	 * @param numberOfCsInReference the numberOfCsInReference to set
	 */
	public void setNumberOfCsInReference(long numberOfCsInReference) {
		this.numberOfCsInReference = numberOfCsInReference;
	}
	
	/**
	 * @return the cRelativeContentInReference
	 */
	public double getcRelativeContentInReference() {
		return cRelativeContentInReference;
	}
	
	/**
	 * @param cRelativeContentInReference the cRelativeContentInReference to set
	 */
	public void setcRelativeContentInReference(double cRelativeContentInReference) {
		this.cRelativeContentInReference = cRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfTsInReference
	 */
	public long getNumberOfTsInReference() {
		return numberOfTsInReference;
	}
	
	/**
	 * @param numberOfTsInReference the numberOfTsInReference to set
	 */
	public void setNumberOfTsInReference(long numberOfTsInReference) {
		this.numberOfTsInReference = numberOfTsInReference;
	}
	
	/**
	 * @return the tRelativeContentInReference
	 */
	public double gettRelativeContentInReference() {
		return tRelativeContentInReference;
	}
	
	/**
	 * @param tRelativeContentInReference the tRelativeContentInReference to set
	 */
	public void settRelativeContentInReference(double tRelativeContentInReference) {
		this.tRelativeContentInReference = tRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfGsInReference
	 */
	public long getNumberOfGsInReference() {
		return numberOfGsInReference;
	}
	
	/**
	 * @param numberOfGsInReference the numberOfGsInReference to set
	 */
	public void setNumberOfGsInReference(long numberOfGsInReference) {
		this.numberOfGsInReference = numberOfGsInReference;
	}
	
	/**
	 * @return the gRelativeContentInReference
	 */
	public double getgRelativeContentInReference() {
		return gRelativeContentInReference;
	}
	
	/**
	 * @param gRelativeContentInReference the gRelativeContentInReference to set
	 */
	public void setgRelativeContentInReference(double gRelativeContentInReference) {
		this.gRelativeContentInReference = gRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfNsInReference
	 */
	public long getNumberOfNsInReference() {
		return numberOfNsInReference;
	}
	
	/**
	 * @param numberOfNsInReference the numberOfNsInReference to set
	 */
	public void setNumberOfNsInReference(long numberOfNsInReference) {
		this.numberOfNsInReference = numberOfNsInReference;
	}
	
	/**
	 * @return the nRelativeContentInReference
	 */
	public double getnRelativeContentInReference() {
		return nRelativeContentInReference;
	}
	
	/**
	 * @param nRelativeContentInReference the nRelativeContentInReference to set
	 */
	public void setnRelativeContentInReference(double nRelativeContentInReference) {
		this.nRelativeContentInReference = nRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfGcsInReference
	 */
	public long getNumberOfGcsInReference() {
		return numberOfGcsInReference;
	}
	
	/**
	 * @param numberOfGcsInReference the numberOfGcsInReference to set
	 */
	public void setNumberOfGcsInReference(long numberOfGcsInReference) {
		this.numberOfGcsInReference = numberOfGcsInReference;
	}
	
	/**
	 * @return the gcRelativeContentInReference
	 */
	public double getGcRelativeContentInReference() {
		return gcRelativeContentInReference;
	}
	
	/**
	 * @param gcRelativeContentInReference the gcRelativeContentInReference to set
	 */
	public void setGcRelativeContentInReference(double gcRelativeContentInReference) {
		this.gcRelativeContentInReference = gcRelativeContentInReference;
	}
	
	/**
	 * @return the numberOfAtsInReference
	 */
	public long getNumberOfAtsInReference() {
		return numberOfAtsInReference;
	}
	
	/**
	 * @param numberOfAtsInReference the numberOfAtsInReference to set
	 */
	public void setNumberOfAtsInReference(long numberOfAtsInReference) {
		this.numberOfAtsInReference = numberOfAtsInReference;
	}
	
	/**
	 * @return the atRelativeContentInReference
	 */
	public double getAtRelativeContentInReference() {
		return atRelativeContentInReference;
	}
	
	/**
	 * @param atRelativeContentInReference the atRelativeContentInReference to set
	 */
	public void setAtRelativeContentInReference(double atRelativeContentInReference) {
		this.atRelativeContentInReference = atRelativeContentInReference;
	}
	
	/**
	 * @return the meanCoverage
	 */
	public double getMeanCoverage() {
		return meanCoverage;
	}
	
	/**
	 * @param meanCoverage the meanCoverage to set
	 */
	public void setMeanCoverage(double meanCoverage) {
		this.meanCoverage = meanCoverage;
	}
	
	/**
	 * @return the stdCoverage
	 */
	public double getStdCoverage() {
		return stdCoverage;
	}
	
	/**
	 * @param stdCoverage the stdCoverage to set
	 */
	public void setStdCoverage(double stdCoverage) {
		this.stdCoverage = stdCoverage;
	}
	
	/**
	 * @return the acumMappingQuality
	 */
	public double getAcumMappingQuality() {
		return acumMappingQuality;
	}
	
	/**
	 * @param acumMappingQuality the acumMappingQuality to set
	 */
	public void setAcumMappingQuality(double acumMappingQuality) {
		this.acumMappingQuality = acumMappingQuality;
	}
	
	/**
	 * @return the meanMappingQuality
	 */
	public double getMeanMappingQuality() {
		return meanMappingQuality;
	}
	
	/**
	 * @param meanMappingQuality the meanMappingQuality to set
	 */
	public void setMeanMappingQuality(double meanMappingQuality) {
		this.meanMappingQuality = meanMappingQuality;
	}
	
	/**
	 * @return the meansequencingQuality
	 */
	public double getMeansequencingQuality() {
		return meansequencingQuality;
	}
	
	/**
	 * @param meansequencingQuality the meansequencingQuality to set
	 */
	public void setMeansequencingQuality(double meansequencingQuality) {
		this.meansequencingQuality = meansequencingQuality;
	}
	
	/**
	 * @return the numberOfAs
	 */
	public long getNumberOfAs() {
		return numberOfAs;
	}
	
	/**
	 * @param numberOfAs the numberOfAs to set
	 */
	public void setNumberOfAs(long numberOfAs) {
		this.numberOfAs = numberOfAs;
	}
	
	/**
	 * @return the meanAContent
	 */
	public double getMeanAContent() {
		return meanAContent;
	}
	
	/**
	 * @param meanAContent the meanAContent to set
	 */
	public void setMeanAContent(double meanAContent) {
		this.meanAContent = meanAContent;
	}
	
	/**
	 * @return the meanARelativeContent
	 */
	public double getMeanARelativeContent() {
		return meanARelativeContent;
	}
	
	/**
	 * @param meanARelativeContent the meanARelativeContent to set
	 */
	public void setMeanARelativeContent(double meanARelativeContent) {
		this.meanARelativeContent = meanARelativeContent;
	}
	
	/**
	 * @return the numberOfCs
	 */
	public long getNumberOfCs() {
		return numberOfCs;
	}
	
	/**
	 * @param numberOfCs the numberOfCs to set
	 */
	public void setNumberOfCs(long numberOfCs) {
		this.numberOfCs = numberOfCs;
	}
	
	/**
	 * @return the meanCContent
	 */
	public double getMeanCContent() {
		return meanCContent;
	}
	
	/**
	 * @param meanCContent the meanCContent to set
	 */
	public void setMeanCContent(double meanCContent) {
		this.meanCContent = meanCContent;
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
	 * @param meanGRelativeContent the meanGRelativeContent to set
	 */
	public void setMeanGRelativeContent(double meanGRelativeContent) {
		this.meanGRelativeContent = meanGRelativeContent;
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
	 * @return the acumInsertSize
	 */
	public double getAcumInsertSize() {
		return acumInsertSize;
	}

	/**
	 * @param acumInsertSize the acumInsertSize to set
	 */
	public void setAcumInsertSize(double acumInsertSize) {
		this.acumInsertSize = acumInsertSize;
	}

	/**
	 * @return the meanInsertSize
	 */
	public double getMeanInsertSize() {
		return meanInsertSize;
	}

	/**
	 * @param meanInsertSize the meanInsertSize to set
	 */
	public void setMeanInsertSize(double meanInsertSize) {
		this.meanInsertSize = meanInsertSize;
	}


    public void inverseRegions() {
        selectedRegions.flip(0, selectedRegions.size());
    }

    public void addReadData(SingleReadData readData) {

        //TODO: bug-110
        //numberOfProcessedReads += readData.numberOfProcessedReads;
        //numberOfOutOfBoundsReads += readData.numberOfOutOfBoundsReads;

        numberOfAlignedBases += readData.numberOfAlignedBases;
        numberOfMappedBases += readData.numberOfMappedBases;
        numberOfSequencedBases += readData.numberOfSequencedBases;

        numberOfAs += readData.numberOfAs;
        numberOfCs += readData.numberOfCs;
        numberOfGs += readData.numberOfGs;
        numberOfTs += readData.numberOfTs;

        //correctInsertSizes += readData.correctInsertSizes;


    }

    public double getEffectiveWindowLength() {
        return effectiveWindowLength;
    }
}