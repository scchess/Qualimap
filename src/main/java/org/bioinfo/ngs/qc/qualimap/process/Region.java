package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.formats.core.feature.Gff;

public class Region {
	// input params
	protected String contig;
	protected int start;
	protected int end;
	protected double regionScore;
	
	// inferred
	protected int size;
	
	public Region(String contig, int start, int end){
		this(contig,start,end,0);		
	}
	
	public Region(String contig, int start, int end, double score){
		this.contig = contig;
		this.start = start;
		this.end = end;
		this.regionScore = score;
		
		this.size = end - start + 1;
	}
	
	public String toString(){
		return this.contig + ":" + start + "-" + end;
	}


	public Gff toGff(){
		return new Gff(contig,"josete-tools","featured",start,end,"" + regionScore,"+", "frame", "group");
	}
	
	public boolean contains(String contig, int start, int end){
		boolean res = this.contig.equalsIgnoreCase(contig) && this.start<=start && this.end>=start;
		return res;
	}

	/**
	 * @return the contig
	 */
	public String getContig() {
		return contig;
	}
	
	/**
	 * @param contig the contig to set
	 */
	public void setContig(String contig) {
		this.contig = contig;
	}
	
	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}
	
	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}
	
	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}
	
	/**
	 * @param end the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	
	/**
	 * @return the regionScore
	 */
	public double getRegionScore() {
		return regionScore;
	}
	
	/**
	 * @param regionScore the regionScore to set
	 */
	public void setRegionScore(double regionScore) {
		this.regionScore = regionScore;
	}
	
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}	
}
