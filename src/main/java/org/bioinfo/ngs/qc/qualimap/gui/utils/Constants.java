package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
	public static final int DEFAULT_NUMBER_OF_WINDOWS = 400;
	public static final int REPORT_INPUT_BAM_FILE = 0;
	public static final int REPORT_OUTSIDE_BAM_FILE = 2;
	
	public static final int GRAPHIC_TO_SAVE_WIDTH = 1024;
	public static final int GRAPHIC_TO_SAVE_HEIGHT = 768;
	
	
	/** Path to locate the images when the application is running in a jar file */
	public static final String pathImages = "/org/bioinfo/ngs/qc/qualimap/gui/images/";
	
	/** Path to locate the resources of the application */
	public static final String pathResources = "/org/bioinfo/ngs/qc/qualimap/resources/";
	
	//******************************************************************************************
	//****************************** LENGTH AND MARGIN CONSTANTS *******************************
	//******************************************************************************************
	// Max Length of the input file name showed
	public static final int fileInputNameLength = 500;
	
	// Margin top for each element
	public static final int marginTopForFirstElement = 15;
	
	// Margin left for each element
	public static final int marginLeftForElement = 10;
	
	// Margin left for sub element of a GroupBox
	public static final int marginLeftForSubElement = 15;
	
	// Margin left for an element of type JCheckbox
	public static final int marginLeftForElementCheck = 2;
	
	// Common height for each element 
	public static final int elementHeight = 20;
	
	// Margin top for an element that is not the first. To calculate the Y coordinate we use
	// the topMargin and the height of the element before this. 
	public static final int marginTopForElementI = marginTopForFirstElement + elementHeight;
	
	// Width for the labels in the input frame screen
	public static final int labelInputElementWidth = 161;
	
	// Width for the labels in the input frame screen
	public static final int labelInputElementWidthLong = 215;
	
	// Margin top for an element of a submenu
	public static final int marginTopForElementSubMenu = 3;
	
	// Margin left for an element of a submenu
	public static final int marginLeftForElementSubMenu = 6;
	
	// Width for the input box to set or read numbers 
	public static final int widthInputNumber = 125;
	
	
	//******************************************************************************************
	//******************************* FILE EXTENSION CONSTANTS *********************************
	//******************************************************************************************
	// Extension for the data Input File
	public static final String FILE_EXTENSION_DATA_INPUT = "BAM";

	// Extension for the Region Input File
	public static final Map<String, String> FILE_EXTENSION_REGION = new HashMap<String, String>();
	static{
		FILE_EXTENSION_REGION.put("GFF", "GFF");
		FILE_EXTENSION_REGION.put("GTF", "GTF");
	}
	
	// Extension for the Reference Genome File
	public static final String FILE_EXTENSION_REFERENCE_GENOME = "FASTA";
	
	// Extension for the Compress File
	public static final String FILE_EXTENSION_COMPRESS_FILE = "ZIP";
	
	// Extension for the PDF File
	public static final String FILE_EXTENSION_PDF_FILE = "PDF";
	
	
	//******************************************************************************************
	//******************************* GRAPHICS NAMES CONSTANTS *********************************
	//******************************************************************************************
	// Name of the graphic for the Coverage Across Reference
	public static final String GRAPHIC_NAME_GENOME_COVERAGE_ACROSS_REFERENCE = "genome_coverage_across_reference.png";
	
	// Name of the graphic for the Coverage Histogram
	public static final String GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM = "genome_coverage_histogram.png";
	
	// Name of the graphic for the Coverage Histogram (0-50x)
	public static final String GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM_0_50 = "genome_coverage_0to50_histogram.png";

	// Name of the graphic for the Coverage Quotes
	public static final String GRAPHIC_NAME_GENOME_COVERAGE_QUOTA = "genome_coverage_quotes.png";

    public static final String GRAPHIC_NAME_GENOME_UNIQUE_READ_COUNTS = "genome_uniq_read_starts_histogram.png";

    public static final String GRAPHIC_NAME_GENOME_OUTSIDE_UNIQUE_READ_COUNTS = "outside_uniq_read_starts_histogram.png";

    public static final String GRAPHIC_NAME_GENOME_GC_CONTENT_PER_WINDOW = "genome_gc_content_per_window.png";

    public static final String GRAPHIC_NAME_OUTSIDE_GC_CONTENT_PER_WINDOW = "outside_gc_content_per_window.png";

    public static final String GRAPHIC_NAME_GENOME_READS_CONTENT = "genome_reads_content_per_read_position.png";

    public static final String GRAPHIC_NAME_OUTSIDE_READS_CONTENT = "outside_reads_content_per_read_position.png";

	// Name of the graphic for the Nucleotide Relative Content
	public static final String GRAPHIC_NAME_GENOME_NUCLEOTIDE_RELATIVE_CONTENT = "genome_actg_across_reference.png";
	
	// Name of the graphic for the GC/AT Relative Content
	public static final String GRAPHIC_NAME_GENOME_GC_AT_RELATIVE_CONTENT = "genome_gc_across_reference.png";
	
	// Name of the graphic for the Mapping_Quality_Acorss_Reference
	public static final String GRAPHIC_NAME_GENOME_MAPPING_QUALITY_ACROSS_REFERENCE = "genome_mapping_quality_across_reference.png";
	
	// Name of the graphic for the Mapping_Quality_Histogram
	public static final String GRAPHIC_NAME_GENOME_MAPPING_QUALITY_HISTOGRAM = "genome_mapping_quality_histogram.png";

	// Name of the graphic for the Outside Mapping_Quality_Acorss_Reference
	public static final String GRAPHIC_NAME_GENOME_INSERT_SIZE_ACROSS_REFERENCE = "genome_insert_size_across_reference.png";
	
	// Name of the graphic for the Outside Mapping_Quality_Histogram
	public static final String GRAPHIC_NAME_GENOME_INSERT_SIZE_HISTOGRAM = "genome_insert_size_histogram.png";
	
	// Name of the graphic for the Inside Coverage Across Reference
	public static final String GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_ACROSS_REFERENCE = "inside_coverage_across_reference.png";
	
	// Name of the graphic for the Inside Coverage Histogram
	public static final String GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_HISTOGRAM = "inside_coverage_histogram.png";
	
	// Name of the graphic for the Inside Coverage Histogram (0-50x)
	public static final String GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_HISTOGRAM_0_50 = "inside_coverage_0to50_histogram.png";

	// Name of the graphic for the Inside Coverage Quotes
	public static final String GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_QUOTA = "inside_coverage_quotes.png";
	
	// Name of the graphic for the Inside Nucleotide Relative Content
	public static final String GRAPHIC_NAME_GENOME_INSIDE_NUCLEOTIDE_RELATIVE_CONTENT = "inside_actg_across_reference.png";
	
	// Name of the graphic for the Inside GC/AT Relative Content
	public static final String GRAPHIC_NAME_GENOME_INSIDE_GC_AT_RELATIVE_CONTENT = "inside_gc_across_reference.png";
	
	// Name of the graphic for the Inside Mapping_Quality_Acorss_Reference
	public static final String GRAPHIC_NAME_GENOME_INSIDE_INSERT_SIZE_ACROSS_REFERENCE = "inside_insert_size_across_reference.png";
	
	// Name of the graphic for the Inside Mapping_Quality_Histogram
	public static final String GRAPHIC_NAME_GENOME_INSIDE_INSERT_SIZE_HISTOGRAM = "inside_insert_size_histogram.png";
		
	// Name of the graphic for the Inside Mapping_Quality_Acorss_Reference
	public static final String GRAPHIC_NAME_GENOME_INSIDE_MAPPING_QUALITY_ACROSS_REFERENCE = "inside_mapping_quality_across_reference.png";
	
	// Name of the graphic for the Inside Mapping_Quality_Histogram
	public static final String GRAPHIC_NAME_GENOME_INSIDE_MAPPING_QUALITY_HISTOGRAM = "inside_mapping_quality_histogram.png";
	
	// Name of the graphic for the Outside Coverage Across Reference
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_ACROSS_REFERENCE = "outside_coverage_across_reference.png";
	
	// Name of the graphic for the Outside Coverage Histogram
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_HISTOGRAM = "outside_coverage_histogram.png";
	
	// Name of the graphic for the Outside Coverage Histogram (0-50x)
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_HISTOGRAM_0_50 = "outside_coverage_0to50_histogram.png";

	// Name of the graphic for the Outside Coverage Quotes
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_QUOTA = "outside_coverage_quotes.png";
	
	// Name of the graphic for the Outside Nucleotide Relative Content
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_NUCLEOTIDE_RELATIVE_CONTENT = "outside_actg_across_reference.png";
	
	// Name of the graphic for the Outside GC/AT Relative Content
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_GC_AT_RELATIVE_CONTENT = "outside_gc_across_reference.png";
	
	// Name of the graphic for the Outside Mapping_Quality_Acorss_Reference
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_ACROSS_REFERENCE = "outside_insert_size_across_reference.png";
	
	// Name of the graphic for the Outside Mapping_Quality_Histogram
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_HISTOGRAM = "outside_insert_size_histogram.png";
	
	// Name of the graphic for the Outside Mapping_Quality_Acorss_Reference
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_ACROSS_REFERENCE = "outside_mapping_quality_across_reference.png";
	
	// Name of the graphic for the Outside Mapping_Quality_Histogram
	public static final String GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_HISTOGRAM = "outside_mapping_quality_histogram.png";
	
	
	//******************************************************************************************
	//***************************** GRAPHICS NAMES RNA CONSTANTS *******************************
	//******************************************************************************************
	// Name of the graphic for the Global Saturation
	public static final String GRAPHIC_NAME_RNA_GLOBAL_SATURATION = "GlobalSaturation.png";
	
	// Name of the graphic for the Global Saturation
	public static final String GRAPHIC_NAME_RNA_SATURATION_PER_CLASS = "DetectionPerGroup.png";
	
	// Name of the graphic for the Global Saturation
	public static final String GRAPHIC_NAME_RNA_COUNTS_PER_CLASS = "counts_boxplot.png";
	
	// unkown class
	//public static final String GRAPHIC_NAME_RNA_COUNTS_PER_UNKNOWN = "unknown_boxplot.png";
	
	
	//******************************************************************************************
	//****************************** PROPERTIES IN ZIP NAME FILE *******************************
	//******************************************************************************************
	// Name of property file that contains the data in the Zip file for the reference input
	public static final String NAME_OF_PROPERTIES_IN_ZIP_FILE  = "reference.properties";
	
	// Name of property file that contains the data in the Zip file for the inside reference input
	public static final String NAME_OF_INSIDE_PROPERTIES_IN_ZIP_FILE  = "inside_reference.properties";
	
	// Name of property file that contains the data in the Zip file for the outside reference input
	public static final String NAME_OF_OUTSIDE_PROPERTIES_IN_ZIP_FILE  = "outside_reference.properties";
	
	public static final String NAME_OF_GENOME_LOCATOR_IN_ZIP_FILE = "genome_locator";

    public static final String NAME_OF_BAM_STATS_IN_ZIP_FILE = "bam_stats";

	//******************************************************************************************
	//********************************* NAMES OF DATA RAW FILES ********************************
	//******************************************************************************************
	public static final String NAME_OF_FILE_CHROMOSOMES = "chromosomes.txt";

    public static final String NAME_OF_FILE_CHROMOSOMES_OUTSIDE = "outside_chromosomes.txt";

	
	//******************************************************************************************
	//******************************** TYPES OF BAM ANALYSIS ***********************************
	//******************************************************************************************

    public static final int TYPE_BAM_ANALYSIS_DNA = 0;
	public static final int TYPE_BAM_ANALYSIS_EXOME = 1;
	public static final int TYPE_BAM_ANALYSIS_RNA = 2;
    public static final int TYPE_BAM_ANALYSIS_EPI = 3;

	//******************************************************************************************
	//*********************************** TYPES OF SPECIES *************************************
	//******************************************************************************************
	public static final String TYPE_COMBO_SPECIES_HUMAN = "HUMAN";
	public static final String TYPE_COMBO_SPECIES_MOUSE = "MOUSE";
	
	//******************************************************************************************
	//*********************************** FILES OF SPECIES *************************************
	//******************************************************************************************
	public static final String FILE_SPECIES_INFO_HUMAN = "human.61.genes.biotypes.txt";
	public static final String FILE_SPECIES_GROUPS_HUMAN = "human.biotypes.groups.txt";
	public static final String FILE_SPECIES_INFO_MOUSE = "mouse.61.genes.biotypes.txt";
	public static final String FILE_SPECIES_GROUPS_MOUSE = "mouse.biotypes.groups.txt";

    public static final String OK_COMMAND = "ok";
    public static final String CANCEL_COMMAND = "cancel";

    public static final String VIZ_TYPE_HEATMAP = "heatmap";
    public static final String VIZ_TYPE_LINE = "line";

    // This is a SAM record custom field, has to be 2 characters
    public static final String READ_IN_REGION = "XX";

    public static final String REPORT_TYPE_HTML = "HTML";
    public static final String REPORT_TYPE_PDF = "PDF";
}
