package org.bioinfo.ngs.qc.qualimap.beans;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenomeLocator implements Externalizable {
	private List<ContigRecord> contigs;
	private HashMap<String,Long> positions;
	private long totalSize;
	
	public GenomeLocator(){
		contigs = new ArrayList<ContigRecord>();
		positions = new HashMap<String, Long>();	
		totalSize = 0;
	}
	
	public void addContig(String name, int size){
		contigs.add(new ContigRecord(name,totalSize+1,size));
		positions.put(name, totalSize+1);
		totalSize+=size;
	}
	
	public Long getAbsoluteCoordinates(String name, int relative){
		if(positions.get(name)!=null){
			return positions.get(name) + (relative-1);
		} else {
			return (long)-1;
		}		
	}
		
	public ContigRecord getContigCoordinates(long absolute){
		// empty contig list
		if(contigs.size()==0){
			return null;
		}
		// mega contig
		else if(contigs.size()==1){
			ContigRecord contig = contigs.get(0);
			contig.setRelative(absolute);
			return contig;
		} 
		// search contig
		else {
			ContigRecord last = contigs.get(0);
			for(int i=1; i<contigs.size(); i++){
				if(contigs.get(i).getPosition()>absolute){
					break;
				}
				last = contigs.get(i);
			}
			last.setRelative(absolute-last.getPosition()+1);
			return last;
		}
	}

	/**
	 * @return the contigs
	 */
	public List<ContigRecord> getContigs() {
		return contigs;
	}

	/**
	 * @return the totalSize
	 */
	public long getTotalSize() {
		return totalSize;
	}


    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(contigs);
        objectOutput.writeObject(positions);
        objectOutput.writeLong(totalSize);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        contigs = (List<ContigRecord>) objectInput.readObject();
        positions = (HashMap<String,Long>) objectInput.readObject();
        totalSize = objectInput.readLong();

    }
}