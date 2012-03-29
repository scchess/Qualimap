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

