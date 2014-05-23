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
package org.bioinfo.ngs.qc.qualimap.common;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
	public static final int DEFAULT_NUMBER_OF_WINDOWS = 400;
	public static final int DEFAULT_CHUNK_SIZE = 1000;
    public static final int DEFAULT_HOMOPOLYMER_SIZE = 3;

	public static final int GRAPHIC_TO_SAVE_WIDTH = 1024;
	public static final int GRAPHIC_TO_SAVE_HEIGHT = 768;
	
	
	/** Path to locate the images when the application is running in a jar file */
	public static final String pathImages = "/org/bioinfo/ngs/qc/qualimap/gui/images/";
	
	/** Path to locate the resources of the application */
	public static final String pathResources = "/org/bioinfo/ngs/qc/qualimap/";

	
	//******************************************************************************************
	//****************************** LENGTH AND MARGIN CONSTANTS *******************************
	//******************************************************************************************
	// Margin top for each element
	public static final int marginTopForFirstElement = 15;
	
	// Margin left for each element
	public static final int marginLeftForElement = 10;
	
	// Margin left for sub element of a GroupBox
	public static final int marginLeftForSubElement = 15;
	
	// Common height for each element
	public static final int elementHeight = 20;
	
	// Margin top for an element of a submenu
	public static final int marginTopForElementSubMenu = 3;

	
	//******************************************************************************************
	//******************************* FILE EXTENSION CONSTANTS *********************************
	//******************************************************************************************
	// Extension for the data Input File
	public static final String FILE_EXTENSION_BAM = "BAM";
    public static final String FILE_EXTENSION_SAM = "SAM";


	// Extension for the Region Input File
	public static final Map<String, String> FILE_EXTENSION_REGION = new HashMap<String, String>();
	static{
		FILE_EXTENSION_REGION.put("GFF", "GFF");
		FILE_EXTENSION_REGION.put("GTF", "GTF");
        FILE_EXTENSION_REGION.put("BED", "BED");
	}
	
	// Extension for the PDF File
	public static final String FILE_EXTENSION_PDF_FILE = "PDF";
	
    public static final String PLOT_TITLE_COVERAGE_ACROSS_REFERENCE = "Coverage Across Reference";
    public static final String PLOT_TITLE_COVERAGE_HISTOGRAM = "Coverage Histogram";
    public static final String PLOT_TITLE_COVERAGE_HISTOGRAM_0_50 = "Coverage Histogram (0-50X)";
    public static final String PLOT_TITLE_MAPPING_QUALITY_ACROSS_REFERENCE = "Mapping Quality Across Reference";
    public static final String PLOT_TITLE_MAPPING_QUALITY_HISTOGRAM = "Mapping Quality Histogram";
    public static final String PLOT_TITLE_INSERT_SIZE_ACROSS_REFERENCE = "Insert Size Across Reference";
    public static final String PLOT_TITLE_INSERT_SIZE_HISTOGRAM = "Insert Size Histogram";
    public static final String PLOT_TITLE_READS_NUCLEOTIDE_CONTENT = "Mapped Reads Nucleotide Content";
    public static final String PLOT_TITLE_READS_CLIPPING_PROFILE = "Mapped Reads Clipping Profile";
    public static final String PLOT_TITLE_GENOME_FRACTION_COVERAGE = "Genome Fraction Coverage";
    public static final String PLOT_TITLE_READS_GC_CONTENT = "Mapped Reads GC-content Distribution";
    public static final String PLOT_TITLE_DUPLICATION_RATE_HISTOGRAM = "Duplication Rate Histogram";
    public static final String PLOT_TITLE_HOMOPOLYMER_INDELS = "Homopolymer Indels";


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

    public static final String GRAPHIC_NAME_RNA_GLOBAL_SATURATION = "GlobalSaturation.png";
	public static final String GRAPHIC_NAME_RNA_SATURATION_PER_CLASS = "DetectionPerGroup.png";
	public static final String GRAPHIC_NAME_RNA_COUNTS_PER_CLASS = "counts_boxplot.png";
    public static final String GRAPHIC_NAME_RNA_SAMPLE_CORRELATION = "correlation_plot.png";

	//******************************************************************************************
	//********************************* NAMES OF DATA RAW FILES ********************************
	//******************************************************************************************
	public static final String NAME_OF_FILE_CHROMOSOMES = "chromosomes.txt";

    public static final String NAME_OF_FILE_CHROMOSOMES_OUTSIDE = "outside_chromosomes.txt";

	//******************************************************************************************
	//*********************************** TYPES OF SPECIES *************************************
	//******************************************************************************************
	public static final String TYPE_COMBO_SPECIES_HUMAN = "HUMAN";
	public static final String TYPE_COMBO_SPECIES_MOUSE = "MOUSE";
	
	//******************************************************************************************
	//*********************************** FILES OF SPECIES *************************************
	//******************************************************************************************
	public static final String FILE_SPECIES_INFO_HUMAN = "human.64.genes.biotypes.txt";
	public static final String FILE_SPECIES_GROUPS_HUMAN = "human.biotypes.groups.txt";
	public static final String FILE_SPECIES_INFO_MOUSE = "mouse.64.genes.biotypes.txt";
	public static final String FILE_SPECIES_GROUPS_MOUSE = "mouse.biotypes.groups.txt";
    public static final String FILE_SPECIES_INFO_HUMAN_ENS68 = "human.ens68.txt";
    public static final String FILE_SPECIES_INFO_MOUSE_ENS68 = "mouse.ens68.txt";


    public static final String OK_COMMAND = "ok";
    public static final String CANCEL_COMMAND = "cancel";
    public static final String COMMAND_ADD_ITEM = "add_item";
    public static final String COMMAND_REMOVE_ITEM = "delete_item";
    public static final String COMMAND_EDIT_ITEM = "edit_item";
    public static final String COMMAND_RUN_ANALYSIS = "run_analysis";

    public static final String VIZ_TYPE_HEATMAP = "heatmap";
    public static final String VIZ_TYPE_LINE = "line";

    // This is a SAM record custom field, has to be 2 characters
    public static final String READ_IN_REGION = "XX";
    public static final String READ_WEIGHT_ATTR = "XW";

    public static final String ANALYSIS_NAME_BAMQC = "BAM QC";
    public static final String ANALYSIS_NAME_COUNTS_QC = "Counts QC";
    public static final String ANALYSIS_NAME_CLUSTERING = "Clustering";

    public static final String REPORT_TYPE_HTML = "HTML";
    public static final String REPORT_TYPE_PDF = "PDF";
    public static final String CHROMOSOME_STATS_HEADER = "header";
    public static final String CHROMOSOME_STATS_DATA = "data";

    public static final String TOOL_NAME_BAMQC = "bamqc";
    public static final String TOOL_NAME_COUNTS_QC = "counts";
    public static final String TOOL_NAME_MULTISAMPLE_COUNTS_QC = "mcounts";
    public static final String TOOL_NAME_RNASEQ_QC = "rnaseq";
    public static final String TOOL_NAME_CLUSTERING = "clustering";
    public static final String TOOL_NAME_COMPUTE_COUNTS = "comp-counts";
    public static final String TOOL_NAME_GC_CONTENT = "gc-content";
    public static final String TOOL_NAME_INDEL_COUNT = "indel-count";
    public static final String TOOL_NAME_GENE_COVERAGE = "gene-cov";
}
