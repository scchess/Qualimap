package org.bioinfo.ngs.qc.qualimap.process;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bioinfo.formats.core.feature.Gff;
import org.bioinfo.formats.core.feature.io.GffReader;
import org.bioinfo.formats.exception.FileFormatException;
import org.bioinfo.ngs.qc.qualimap.beans.GenomeLocator;

public class RegionStats {

	// input
	private String regionFile;
		
	// output
	private int numberOfRegions;
	private long numberOfBases;
	private long numberOfRegionBases;
	private long numberOfOverlappedBases;
	private GenomeLocator dictionary;
	private DecimalFormat numericFormat = new DecimalFormat("###,###,###,###,###,###,###,###,###,###"); 
		
	public static void main(String[] args) throws Exception {
		
		//RegionStats rstats = new RegionStats("/home/jose/proyectos/drafts/chromoxumuxus/chroloplast_regions_overlapped.gff");		
		//RegionStats rstats = new RegionStats("/home/jose/proyectos/sevilla/SeqCap_EZ_Exome_v2.capture.gff");
		RegionStats rstats = new RegionStats("/home/jose/proyectos/sevilla/SeqCap_EZ_Exome_v2.target.gff");
		//RegionStats rstats = new RegionStats("/home/jose/proyectos/drafts/bamqc/gambusino/gambusino_regions.gff");
		
		rstats.run();
		
	}
	
	public RegionStats(String regionFile){
		this.regionFile = regionFile;
	}
	
	
	public void run() throws SecurityException, FileFormatException, IOException, NoSuchMethodException{
		
		// load dictionary
		System.err.println("Loading dictionary...");
		dictionary = loadPartialDictionary();
		
		// filling references
		System.err.println("Filling references...");
		List<boolean[]> references = fillReferences();
				
		// filling references
		System.err.println("Computing stats...");
		computeStats(references);
		
		System.err.println("Finished");
		System.err.println("	Number of regions = " + numericFormat.format(numberOfRegions));
		System.err.println("	Number of genome bases = " + numericFormat.format(numberOfBases));
		System.err.println("	Number of bases within regions = " + numericFormat.format(numberOfRegionBases));
		System.err.println("	Number of overlapped bases = " + numericFormat.format(numberOfOverlappedBases));
		                                  
	}
	
	public void computeStats(List<boolean[]> references){

		numberOfBases = 0;
		for(int i=0; i<references.size(); i++){			
			for(int k=0; k<references.get(i).length; k++){
				if(references.get(i)[k]){
					numberOfBases++;
				}
			}
		}
		
		numberOfOverlappedBases = numberOfRegionBases - numberOfBases;
		
	}
	
	public GenomeLocator loadPartialDictionary() throws FileFormatException, IOException, SecurityException, NoSuchMethodException{
		
		// init vectors
		List<String> contigNames = new ArrayList<String>();
		HashMap<String,Integer> contigSize = new HashMap<String, Integer>();
		this.numberOfRegions = 0;
		this.numberOfRegionBases = 0;
		
		// init reader
		GffReader reader = new GffReader(regionFile);
		
		// 
		Gff gff;
		String contigName;
		int lastend = 0;
		String lastchr = "";
		while((gff=reader.read())!=null){
			
			
			contigName = gff.getSequenceName();
			if(contigSize.containsKey(contigName)){
				if(contigSize.get(contigName)<gff.getEnd()){
					contigSize.put(contigName,gff.getEnd());
				} 
			} else {
				contigNames.add(contigName);
				contigSize.put(contigName,gff.getEnd());
			}
			
			//
			this.numberOfRegions++;
			this.numberOfRegionBases += gff.getEnd() - gff.getStart() + 1;
		
			if(gff.getSequenceName().equals(lastchr) && gff.getEnd()<lastend){
				System.err.println("overlapping in " + gff);
			}
			lastend = gff.getEnd();
			lastchr = gff.getSequenceName();
		}
		reader.close();
	
		// fill dictionary
		GenomeLocator dictionary = new GenomeLocator();
		for(String contig:contigNames){
			dictionary.addContig(contig,contigSize.get(contig));
		}
		
		// return dictionary
		return dictionary;
		
	}
	
	public List<boolean[]> fillReferences() throws FileFormatException, IOException, SecurityException, NoSuchMethodException{
		
		// create references
		HashMap<String, Integer> contigPositions = new HashMap<String, Integer>();
		List<boolean[]> references = new ArrayList<boolean[]>();
		for(int i=0; i<dictionary.getContigs().size(); i++){
			references.add(new boolean[dictionary.getContigs().get(i).getSize()]);
			contigPositions.put(dictionary.getContigs().get(i).getName(),i);						
		}
				
		// run regions		
		GffReader reader = new GffReader(regionFile);
		Gff gff;
		int index;
		int minpos,maxpos;
		while((gff=reader.read())!=null){
			index = contigPositions.get(gff.getSequenceName());
			minpos = gff.getStart()-1;
			maxpos = gff.getEnd()-1;			
			for(int k=minpos; k<=maxpos; k++){
				references.get(index)[k] = true;
			}
		}
		reader.close();
			
		return references;
		
	}
	
}
