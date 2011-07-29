package org.bioinfo.ngs.qc.qualimap.process;

import java.io.IOException;

import org.bioinfo.formats.core.feature.Gff;
import org.bioinfo.formats.core.feature.io.GffReader;
import org.bioinfo.formats.exception.FileFormatException;

public class RegionManager {

	private String regionFile;
	private Gff currentRegion;		
	private GffReader gffReader;
	private int numberOfLoadedRegions;
	
	public RegionManager(String regionFile){
		this.regionFile = regionFile;
		numberOfLoadedRegions = 0;
	}
	
	public void init() throws SecurityException, IOException, NoSuchMethodException{
		gffReader = new GffReader(regionFile);
	}
	
	public void next() throws FileFormatException{		
		currentRegion = gffReader.read();
		if(currentRegion!=null){
			numberOfLoadedRegions++;
		}
	}

	public static int countRegions(String regionFile) throws FileFormatException, SecurityException, IOException, NoSuchMethodException{
		int numberOfSelectedRegions = 0;		
		Gff region;
		GffReader gffReader = new GffReader(regionFile);		
		while((region = gffReader.read())!=null){
			numberOfSelectedRegions++;
		}
		return numberOfSelectedRegions;
	}
	
	
	
	/**
	 * @return the currentRegion
	 */
	public Gff getCurrentRegion() {
		return currentRegion;
	}

	/**
	 * @param currentRegion the currentRegion to set
	 */
	public void setCurrentRegion(Gff currentRegion) {
		this.currentRegion = currentRegion;
	}
	
	
	
}
