package org.bioinfo.ngs.qc.qualimap.beans;

public class ContigRecord {
	private String name;
	private long position;
	private long relative;
	private int size;
		
	public ContigRecord(String name,int size){
		this(name,1,1,size);
	}
	
	public ContigRecord(String name,long position,int size){
		this(name,position,1,size);
	}
	
	public ContigRecord(String name,long position,long relative,int size){
		this.name = name;
		this.position = position;
		this.relative = 1;
		this.size = size;		
	}
	
	public long getStart(){
		return this.position;
	}

	public long getEnd(){
		return this.position + this.size -1;
	}

	
	public String toString(){
		return this.name + "\t" + this.position + "\t" + this.size; 
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
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
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
	 * @return the relative
	 */
	public long getRelative() {
		return relative;
	}

	/**
	 * @param relative the relative to set
	 */
	public void setRelative(long relative) {
		this.relative = relative;
	}	
}