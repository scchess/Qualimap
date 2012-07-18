/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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

        String simplifiedName = name.replace("chr","");

        if(positions.containsKey(name)){
			return positions.get(name) + (relative-1);
		} else if (positions.containsKey(simplifiedName)){
            return positions.get(simplifiedName) + (relative-1);
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

    public boolean containsContig(String seqName) {
        for (ContigRecord contigRecord : contigs) {
            if (contigRecord.getName().equals(seqName)) {
                return true;
            }
        }
        return false;
    }
}