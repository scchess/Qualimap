package org.bioinfo.ngs.qc.qualimap.beans;

import org.bioinfo.commons.utils.ArrayUtils;
import org.bioinfo.math.util.MathUtils;


public class BamDetailedGenomeWindow extends BamGenomeWindow {
	// reference sequence	
	private byte[] reference;
		
	// coverage
	private long[] coverageAcrossReference;
	private long[] properlyPairedCoverageAcrossReference;
		
	// quality
	private double[] mappingQualityAcrossReference;
			
	// A content
	private long[] aContentAcrossReference;
	
	// C content
	private long[] cContentAcrossReference;

	// G content
	private long[] gContentAcrossReference;
	
	// T content
	private long[] tContentAcrossReference;
	
	// N content
	private long[] nContentAcrossReference;
		
	// GC content
	private double[] gcContentAcrossReference;
		
	// AT content
//	private double[] atContentAcrossReference;
		
	// insert size
	private double[] insertSizeAcrossReference;
	
	public BamDetailedGenomeWindow(String name, long start, long end){
		this(name,start,end,null);		
	}

	public BamDetailedGenomeWindow(String name, long start, long end, byte[] reference){
		super(name,start,end,reference);
		
		if(reference!=null){
			this.reference = reference;
		}
		
		// init arrays		
		coverageAcrossReference = new long[(int)this.windowSize];
		properlyPairedCoverageAcrossReference = new long[(int)this.windowSize];
		for(int i=0; i<this.windowSize; i++){
			coverageAcrossReference[i] = 0;
		}
		
		mappingQualityAcrossReference = new double[(int)this.windowSize];
//		sequencingQualityAcrossReference = new int[this.windowSize];
		aContentAcrossReference = new long[(int)this.windowSize];
		cContentAcrossReference = new long[(int)this.windowSize];
		gContentAcrossReference = new long[(int)this.windowSize];
		tContentAcrossReference = new long[(int)this.windowSize];
		nContentAcrossReference = new long[(int)this.windowSize];
		gcContentAcrossReference = new double[(int)this.windowSize];
//		atContentAcrossReference = new double[(int)this.windowSize];
		insertSizeAcrossReference = new double[(int)this.windowSize];
		
	}
	
	@Override
	protected void acumBase(long relative){
		super.acumBase(relative);
		coverageAcrossReference[(int)relative] = coverageAcrossReference[(int)relative]+1;
	}
	
	@Override
	protected void acumProperlyPairedBase(long relative){
		super.acumProperlyPairedBase(relative);
		properlyPairedCoverageAcrossReference[(int)relative] = properlyPairedCoverageAcrossReference[(int)relative] + 1;
	}

	@Override
	protected void acumA(long relative){
		super.acumA(relative);
		aContentAcrossReference[(int)relative] = aContentAcrossReference[(int)relative]+1;
	}
	
	@Override
	protected void acumC(long relative){
		super.acumC(relative);
		cContentAcrossReference[(int)relative] = cContentAcrossReference[(int)relative]+1;
	}
	
	@Override
	protected void acumT(long relative){
		super.acumT(relative);
		tContentAcrossReference[(int)relative] = tContentAcrossReference[(int)relative]+1;
	}
	
	@Override
	protected void acumG(long relative){
		super.acumG(relative);
		gContentAcrossReference[(int)relative] = gContentAcrossReference[(int)relative]+1;		
	}
	
	@Override
	protected void acumMappingQuality(long relative, int mappingQuality){
		super.acumMappingQuality(relative,mappingQuality);		
		// quality					
		mappingQualityAcrossReference[(int)relative] = mappingQualityAcrossReference[(int)relative]+mappingQuality;
	}

	@Override
	protected void acumInsertSize(long relative, long insertSize){
		super.acumInsertSize(relative,insertSize);
		insertSizeAcrossReference[(int)relative] = insertSizeAcrossReference[(int)relative] + Math.abs(insertSize);
	}
	
	@Override
	public void computeDescriptors() throws CloneNotSupportedException{
	
		// normalize vectors
		for(int i=0; i<coverageAcrossReference.length; i++){
			// number of mapped bases
//			numberOfMappedBases+=coverageAcrossReference[i];
			
			if(coverageAcrossReference[i]>0){
				
				// GC/AT contents
				gcContentAcrossReference[i] = (float)(gContentAcrossReference[i] + cContentAcrossReference[i])/(float)coverageAcrossReference[i];				
//				atContentAcrossReference[i] = (float)(aContentAcrossReference[i] + tContentAcrossReference[i])/(float)coverageAcrossReference[i];
				
				// quality
				mappingQualityAcrossReference[i] = mappingQualityAcrossReference[i]/(double)coverageAcrossReference[i];
				acumMappingQuality+=mappingQualityAcrossReference[i];
//				System.err.println(properlyPairedCoverageAcrossReference[i]);
//				insertSizeAcrossReference[i] = insertSizeAcrossReference[i]/(double)properlyPairedCoverageAcrossReference[i];
				insertSizeAcrossReference[i] = insertSizeAcrossReference[i]/(double)coverageAcrossReference[i];
				
			}
		}
				
		// compute std coverage
		stdCoverage = MathUtils.standardDeviation(ArrayUtils.toDoubleArray(coverageAcrossReference));
		
		super.computeDescriptors();
		
	}
	
	/**
	 * @return the reference
	 */
	public byte[] getReference() {
		return reference;
	}
	
	/**
	 * @param reference the reference to set
	 */
	public void setReference(byte[] reference) {
		this.reference = reference;
	}
	
	/**
	 * @return the coverageAcrossReference
	 */
	public long[] getCoverageAcrossReference() {
		return coverageAcrossReference;
	}
	
	/**
	 * @param coverageAcrossReference the coverageAcrossReference to set
	 */
	public void setCoverageAcrossReference(long[] coverageAcrossReference) {
		this.coverageAcrossReference = coverageAcrossReference;
	}
	
	/**
	 * @return the mappingQualityAcrossReference
	 */
	public double[] getMappingQualityAcrossReference() {
		return mappingQualityAcrossReference;
	}
	
	/**
	 * @param mappingQualityAcrossReference the mappingQualityAcrossReference to set
	 */
	public void setMappingQualityAcrossReference(
			double[] mappingQualityAcrossReference) {
		this.mappingQualityAcrossReference = mappingQualityAcrossReference;
	}
	
	/**
	 * @return the aContentAcrossReference
	 */
	public long[] getaContentAcrossReference() {
		return aContentAcrossReference;
	}
	
	/**
	 * @param aContentAcrossReference the aContentAcrossReference to set
	 */
	public void setaContentAcrossReference(long[] aContentAcrossReference) {
		this.aContentAcrossReference = aContentAcrossReference;
	}
	
	/**
	 * @return the cContentAcrossReference
	 */
	public long[] getcContentAcrossReference() {
		return cContentAcrossReference;
	}
	
	/**
	 * @param cContentAcrossReference the cContentAcrossReference to set
	 */
	public void setcContentAcrossReference(long[] cContentAcrossReference) {
		this.cContentAcrossReference = cContentAcrossReference;
	}
	
	/**
	 * @return the gContentAcrossReference
	 */
	public long[] getgContentAcrossReference() {
		return gContentAcrossReference;
	}
	
	/**
	 * @param gContentAcrossReference the gContentAcrossReference to set
	 */
	public void setgContentAcrossReference(long[] gContentAcrossReference) {
		this.gContentAcrossReference = gContentAcrossReference;
	}
	
	/**
	 * @return the tContentAcrossReference
	 */
	public long[] gettContentAcrossReference() {
		return tContentAcrossReference;
	}
	
	/**
	 * @param tContentAcrossReference the tContentAcrossReference to set
	 */
	public void settContentAcrossReference(long[] tContentAcrossReference) {
		this.tContentAcrossReference = tContentAcrossReference;
	}
	
	/**
	 * @return the nContentAcrossReference
	 */
	public long[] getnContentAcrossReference() {
		return nContentAcrossReference;
	}
	
	/**
	 * @param nContentAcrossReference the nContentAcrossReference to set
	 */
	public void setnContentAcrossReference(long[] nContentAcrossReference) {
		this.nContentAcrossReference = nContentAcrossReference;
	}
	
	/**
	 * @return the gcContentAcrossReference
	 */
	public double[] getGcContentAcrossReference() {
		return gcContentAcrossReference;
	}
	
	/**
	 * @param gcContentAcrossReference the gcContentAcrossReference to set
	 */
	public void setGcContentAcrossReference(double[] gcContentAcrossReference) {
		this.gcContentAcrossReference = gcContentAcrossReference;
	}

	/**
	 * @return the insertSizeAcrossReference
	 */
	public double[] getInsertSizeAcrossReference() {
		return insertSizeAcrossReference;
	}

	/**
	 * @param insertSizeAcrossReference the insertSizeAcrossReference to set
	 */
	public void setInsertSizeAcrossReference(double[] insertSizeAcrossReference) {
		this.insertSizeAcrossReference = insertSizeAcrossReference;
	}
	

}