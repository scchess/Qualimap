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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.util.HashMap;
import java.util.Map;

import org.bioinfo.commons.log.Logger;

public class RNAAnalysisVO {
	/** Logger to print information */
	protected Logger logger;
	
	/**
	 * Variable that contains if the info file is set or not
	 */
	private Boolean infoFileIsSet;
	
	/**
	 * Variable that contains if the file of any species is set or not
	 */
	private Boolean specieFileIsSet;
	
	/**
	 * Variable to contain the different classes into the info file
	 * if the info file is selected
	 */
	private Map<String, String> mapClassesInfoFile;
	
	
	public RNAAnalysisVO(){
		this.infoFileIsSet = false;
		this.specieFileIsSet = false;
		this.mapClassesInfoFile = new HashMap<String, String>();
	}
	
	
	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS **************************************
	// ******************************************************************************************
	public Boolean getInfoFileIsSet() {
		return infoFileIsSet;
	}

	public void setInfoFileIsSet(Boolean infoFileIsSet) {
		this.infoFileIsSet = infoFileIsSet;
	}

	public Boolean getSpecieFileIsSet() {
		return specieFileIsSet;
	}

	public void setSpecieFileIsSet(Boolean specieFileIsSet) {
		this.specieFileIsSet = specieFileIsSet;
	}

	public Map<String, String> getMapClassesInfoFile() {
		return mapClassesInfoFile;
	}

}

