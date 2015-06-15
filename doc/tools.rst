.. _tools:


Tools
=====

.. _compute-counts:

Compute counts
--------------

* Given a BAM file and an annotation (`GTF file <http://genome.ucsc.edu/FAQ/FAQformat.html#format4>`_), this tool calculates how many reads are mapped to each region of interest.

  
* The user can decide:

  - At which level wants to perform the counting (genes, transcripts...).

  - What to do whith reads mapped to multiple locations.

  - The strand-specifity.

  - When a transcriptome GTF file is provided the tool allows to calculate 5' and 3' prime coverage bias.

To access the tool use :menuselection:`Tools --> Compute counts`. 

.. note:: For paired-end reads currently each mate of a pair is considered independently (taking into account the strand-specificity of the protocol). We will add full support for paired-end reads in future versions of Qualimap.


.. _example-compute-counts:

Example
^^^^^^^

- Input data:

  - BAM file: `liver.bam <http://qualimap.bioinfo.cipf.es/samples/counts/liver.bam>`_. RNA-seq of liver tissue from `Marioni JC et al <http://genome.cshlp.org/content/18/9/1509.abstract>`_

  - GTF file: `human.64.gtf <http://qualimap.bioinfo.cipf.es/samples/annotations/human.64.gtf>`_ . Human annotation from Ensembl (v. 64)

  - Parameters:

    - Feature ID: gene_id (to count at the level of genes)
    - Feature type: exon (to ignore other features like start/end codons)
    - Multimapped reads: uniquely-mapped-reads (to ignore not unique alignments)

- Output:

  - `liver.counts <http://qualimap.bioinfo.cipf.es/samples/counts/liver.counts>`_. Two-column tab-delimited text file, with the feature IDs in the first column and the number of counts in the second column.


Input
^^^^^

:guilabel:`BAM file` 
  Path to the BAM alignment file.
:guilabel:`Annotation file` 
  Path to the GTF or BED file containing regions of interest.

:guilabel:`Protocol` 
 
  Controls when to consider reads and features to be overlapping:

  :dfn:`non-strand-specific` 
    Reads overlap features if they share genomic regions regardless of the strand.
  :dfn:`forward-stranded`
    For single-end reads, the read and the feature must have the same strand to be overlapping.
    For paired-end reads, the first read of the pair must be mapped to the same strand as the feature, while the second read must be mapped to the opposite strand. 
  
  :dfn:`reverse-strand` 
    For single-end reads, the read and the feature must have the opposite strand.
    For paired-end reads, the first read of pair must be mapped to the opposite strand of the feature, while the second read of the pair  must be on the same strand as the feature.

:guilabel:`Feature ID`
  The user can select the attribute of the GTF file to be used as the feature ID. Regions with the same ID will be aggregated as part of the same feature. The application preload the first 1000 lines of the file so a list with possible feature IDs is conveniently provided.

:guilabel:`Feature type`
  The user can select the feature type (value of the third column of the GTF) considered for counting. Other types will be ignored. The application preload the first 1000 lines of the file so a list with possible feature IDs is conveniently provided.

:guilabel:`Output`
  Path to the ouput file.

:guilabel:`Save computation summary`
  This option controls whether to save overall computation statistics. If selected, the statistics will be saved in a file named `$INPUT_BAM`.counts


.. _multimapped-reads-count:

:guilabel:`Multi-mapped reads`
  This option controls what to do whith reads mapped to multiple location:


  :dfn:`uniquely-mapped-reads`
    Reads mapped to multiple locations will be ignored.


  :dfn:`proportional`
    Multi-mapped reads are detected based on "NH" tag from SAM format. Each read is weighted according to the number of mapped locations. For example, a read mapped to 4 different locations will add 0.25 to the counts of each location. After analysis is finished the value will converted to intger value.

:guilabel:`Calculate 5' and 3' coverage bias`
  If a **GTF file** is provided, the user has the possibility of computing **5' - 3' bias**. The application automatically constructs the 5' and 3' UTR (100 bp) from the gene definitions of the GTF file and determines the coverage rate of the 1000 most highly expressed transcripts in the UTR regions. This information is then stored in the *computation summary* file, together with the statistics of the counting procedure.   

.. note:: This option requires a standard gene model definition. The UTRs are computed for the first and last exons of each transcript. Therefore, `exon` is the feature of interest (third field of the GTF) and `gene_id`, `transcript_id` should be attributes (ninth field of the GTF).


Output
^^^^^^

A two-column tab-delimited text file, with the feature IDs in the first column and the number of counts in the second column, and overall calculation stats. 

The calculation stats include:
 
  :dfn:`Feature counts` 
    Number of reads assigned to various features

  :dfn:`No feature` 
    Number of reads not aligned to any feature

  :dfn:`Not unique alignment` 
    Number of reads with non-unique alignment

  :dfn:`Ambiguous` 
    Number of reads that align to features ambigously
  
The following stats are calculate only if option `Calulate 5' and 3' bias` was set:

  :dfn:`Median 5' bias` 
    For 1000 most expressed genes the ratio between coverage of 100 leftmost bases and mean coverage is calcualted and median value is provided. 

  :dfn:`Median 3' bias` 
    For 1000 most expressed gene the ratio between coverage of 100 rightmost bases and mean coverage is calculated and median value is provided.

  :dfn:`Median 5' to 3` 
    For 1000 most expressed genes the ratio between coverag of 100 leftmost and 100 rightmost bases is calculated and median value is provided.


.. _clustering:

Clustering
----------

* Qualimap provides the possibility of clustering genomic features according to their surrounding coverage profiles. This is particulary interesting in epigenomic studies (e.g. methylation). The user can import a set of features (e.g. TSSs or CpG Islands) together with the BAM file. Then the application preprocess the data and clusters the profiles using the Repitools package (`Statham et al <http://bioinformatics.oxfordjournals.org/content/26/13/1662.abstract>`_). The obtained groups of features are displayed as a heatmap or as line graphs and can be exported for further analysis (e.g. for measuring the correlation between promoter methylation and gene expression).

* Summary of the process:

  - filter out the non-uniquely-mapped reads
  - compute the smoothed coverages values of the samples at the desired locations
  - apply k-means on the smoothed coverage values for the desired values of k


* To perform this analysis the user needs to provide at least two BAM files -- one for the sample (enriched) and other for the control (input) -- and a list of features as BED file.

* Clustering analysis can be accesed using the menu item :menuselection:`File --> Tools --> Clustering`.

.. note:: Clustering coverage profiles is not a straightforward task and it may be necessary to perform a number of empirical filter steps. In order to correctly interpret the approach the results we encourage the users to read Repitools User Manual.


Input Parameters
^^^^^^^^^^^^^^^^

:guilabel:`Experiment ID`
  The experiment name

:guilabel:`Alignment data`
  Here you can provide your replicates to analyze. Each replicate includes sample file and a control file. For example, in an epigenomics experiment, the sample file could be the MeDIP-seq data and the control the non-enriched data (the so-called INPUT data). Thus, for each replicate the following information has to be provided:

  :guilabel:`Replicate name` 
    Name of the replicate
  :guilabel:`Sample file` 
    Path to sample BAM file
  :guilabel:`Control file` 
    Path to control BAM file

  To add a replicate click :guilabel:`Add` button. To remove a replicate select it and click :guilabel:`Remove` button. You can modify replicate by using :guilabel:`Edit` button.

:guilabel:`Regions of interest` 
  Path to an annotation file in `BED <http://genome.ucsc.edu/FAQ/FAQformat.html#format1>`_ or `GFF <http://genome.ucsc.edu/FAQ/FAQformat.html#format3>`_ format, which contains regions of interest.
  

:guilabel:`Location` 
  Relative location to analyze 
:guilabel:`Left offset` 
  Offset in bp upstream the selected regions
:guilabel:`Right offset` 
  Offset in bp downstream the selected regions
:guilabel:`Bin size` 
  Can be thought as the resolution of the plot. Bins of the desired size will be computed and the information falling on each bin will be aggregated
:guilabel:`Number of clusters` 
  Number of groups that you the user wants to divide the data. Several values can be used by separating them with commas
:guilabel:`Fragment length` 
  Length of the fragments that were initially sequenced. All reads will be enlarged to this length.
:guilabel:`Visualization type` 
  You can visualize cluster using heatmaps or line-based graphs.

Output
^^^^^^

After the analysis is performed, the regions of interest are clustered in groups based on the coverage pattern. The output graph shows the coverage pattern for each cluster either as a heatmap or a line graph. There can be multiple graphs based on the number of clusters provided as input. The name of each graph consists of the experiment name and the number of clusters. 

It is possible to export list of features beloning to the particular cluster. To do this use main menu item :menuselection:`File --> Export gene list` or context menu item :menuselection:`Export gene list`. After activating the item a dialog will appear where you can choose some specific cluster. One can either copy the list of features belonging to this cluster in the clipboard or export it to a text file. 



