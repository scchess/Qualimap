package org.bioinfo.ngs.qc.qualimap.process;

public class AbsoluteLocatedRegion {
	// org.bioinfo.ntools.main params
	private long position;
	private long relativePosition;
	private long size;
	
	// extra info
	private String contig;
	protected int start;
	protected int end;
	protected double regionScore;
	
	public AbsoluteLocatedRegion(long position, long relativePosition, long size){
		this(position,relativePosition,size,"undefined",-1,-1,-1);		
	}
	
	public AbsoluteLocatedRegion(long position, long relativePosition, long size, String contig, int start, int end, double regionScore){
		this.position = position;
		this.relativePosition = relativePosition;
		this.size = size;
		this.contig = contig;
		this.start = start;
		this.end = end;
		this.regionScore = regionScore;
	}
	

	/**
	 * @return the position
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(long position) {
		this.position = position;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
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
	 * @return the relativePosition
	 */
	public long getRelativePosition() {
		return relativePosition;
	}

	/**
	 * @param relativePosition the relativePosition to set
	 */
	public void setRelativePosition(long relativePosition) {
		this.relativePosition = relativePosition;
	}	
}