/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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

import java.io.Serializable;

public class ContigRecord implements Serializable {
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