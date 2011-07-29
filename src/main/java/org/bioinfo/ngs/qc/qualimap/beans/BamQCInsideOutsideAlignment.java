package org.bioinfo.ngs.qc.qualimap.beans;

public class BamQCInsideOutsideAlignment {
	private String insideAlignment;
	private long insideReadStart;
	private int insideRegion;
	
	private String outsideAlignment;
	private long outsideReadStart;
	private int outsideRegion;
	
	
	public void computeInsideAndOutsideAlignment(long position, String alignment, int currentRegion,long[] selectedRegionStarts, long[] selectedRegionEnds, long referenceSize){
		StringBuffer insideAlignment  = new StringBuffer();
		StringBuffer outsideAlignment = new StringBuffer();
						
		insideReadStart = -1;
		outsideReadStart = -1;
		
		int cr = currentRegion;
		
		long cp;
		boolean inside;
		boolean outside;
		long next;
		boolean error = false;
		try {
			// filter nucleotides inside regions
			for(int i=0; i<alignment.length(); i++){
				
				cp = position + i;			
				inside = false;
				outside = false;
				
				while(!inside && !outside){
					if(cr==-1){					
						if(cp>=selectedRegionStarts[0]){
							cr++;
						} else {
							outside = true;
						}
					} else {
						// inside/outside before? 
						if(cp<selectedRegionStarts[cr]){
							cr--;					} 
						// inside??
						else if(cp>=selectedRegionStarts[cr] && cp<=selectedRegionEnds[cr]) {
							inside = true;
						} 
						// outside after?
						else {
							if(cr<(selectedRegionStarts.length-1)) {
								next = selectedRegionStarts[cr+1];
							} else {
								next = referenceSize;
							}
							if(cp>selectedRegionEnds[cr] && cp<next) {
								outside = true;						
							} else {
								cr++;
							}
						}
					}				
				}
					
				// nucleotide inside
				if(inside) {
					insideAlignment.append(alignment.charAt(i));
					if(insideReadStart==-1) {
						insideReadStart = cp;
						insideRegion = cr;			
					}
				} else {
					outsideAlignment.append(alignment.charAt(i));
					if(outsideReadStart==-1) {
						outsideReadStart = cp;
						outsideRegion = cr;					
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			error = true;
		}
		
		this.insideAlignment = insideAlignment.toString();
		this.outsideAlignment = outsideAlignment.toString();
		
		if(error){
			System.err.println("position: " + position + " icr: " + insideRegion + 
					" is: " + insideReadStart + "[" + this.insideAlignment + "] ocr: " +
					outsideRegion + " os: " + outsideReadStart + "[" + this.outsideAlignment + 
					"]");	
		}
	}

	/**
	 * @return the insideAlignment
	 */
	public String getInsideAlignment() {
		return insideAlignment;
	}

	/**
	 * @param insideAlignment the insideAlignment to set
	 */
	public void setInsideAlignment(String insideAlignment) {
		this.insideAlignment = insideAlignment;
	}

	/**
	 * @return the insideReadStart
	 */
	public long getInsideReadStart() {
		return insideReadStart;
	}

	/**
	 * @param insideReadStart the insideReadStart to set
	 */
	public void setInsideReadStart(long insideReadStart) {
		this.insideReadStart = insideReadStart;
	}

	/**
	 * @return the outsideAlignment
	 */
	public String getOutsideAlignment() {
		return outsideAlignment;
	}

	/**
	 * @param outsideAlignment the outsideAlignment to set
	 */
	public void setOutsideAlignment(String outsideAlignment) {
		this.outsideAlignment = outsideAlignment;
	}

	/**
	 * @return the insideRegion
	 */
	public int getInsideRegion() {
		return insideRegion;
	}

	/**
	 * @param insideRegion the insideRegion to set
	 */
	public void setInsideRegion(int insideRegion) {
		this.insideRegion = insideRegion;
	}

	/**
	 * @return the outsideReadStart
	 */
	public long getOutsideReadStart() {
		return outsideReadStart;
	}

	/**
	 * @param outsideReadStart the outsideReadStart to set
	 */
	public void setOutsideReadStart(long outsideReadStart) {
		this.outsideReadStart = outsideReadStart;
	}

	/**
	 * @return the outsideRegion
	 */
	public int getOutsideRegion() {
		return outsideRegion;
	}

	/**
	 * @param outsideRegion the outsideRegion to set
	 */
	public void setOutsideRegion(int outsideRegion) {
		this.outsideRegion = outsideRegion;
	}	
}