.. _analysis-types:

Analysis types
==============

.. _bamqc:

BAM QC
------

BAM QC reports information for the evaluation of the quality of the provided alignment data (a BAM file). In short, the basic statistics of the alignment (number of reads, coverage, GC-content, etc.) are summarized and a number of useful graphs are produced. This analysis can be performed with any kind of sequencing data, e.g. whole-genome sequencing, exome sequencing, RNA-seq, ChIP-seq, etc.

In addition, it is possible to provide an annotation file so the results are computed for the reads mapping inside (and optionally outside) of the corresponding genomic regions, which can be especially useful for evaluating *target-enrichment* sequencing studies.

To start a new BAM QC analysis activate main menu item :menuselection:`File --> New Analysis --> BAM QC`.


Examples
^^^^^^^^

- `Whole-genome sequencing: HG00096.chrom20.bam <http://kokonech.github.io/qualimap/HG00096.chr20_bamqc/qualimapReport.html>`_. HTML report for sample alignment file from `1000 Genomes project <http://1000genomes.org>`_.

 
- `Whole-genome sequencing: ERRR089819.bam <http://kokonech.github.io/qualimap/ERR089819_report.pdf>`_. PDF report created using the whole-genome sequencing data of *Caenorhabditis elegans* from the following `study <http://www.ebi.ac.uk/ena/data/view/ERP000975>`_.

- See the :ref:`Sample data <samples>` section for more details about the data used in the examples.


Input Parameters
^^^^^^^^^^^^^^^^

:guilabel:`BAM file` 
  Path to the sequence alignment file in **BAM format**. Note, that the BAM file has to be **sorted by chromosomal coordinates**. Sorting can be performed with `samtools sort <http://samtools.sourceforge.net/>`_.

:guilabel:`Analyze regions` 
  Activating this option allows the analysis of the alignment data for the **regions of interest**. 

:guilabel:`Regions file(GFF/BED file)` 
  The path to the annotation file that defines the regions of interest. The file must be **tab-separated** and have `GFF <http://genome.ucsc.edu/FAQ/FAQformat.html#format3>`_/`GTF <http://genome.ucsc.edu/FAQ/FAQformat.html#format4>`_  or `BED <http://genome.ucsc.edu/FAQ/FAQformat.html# format1>`_ format.

.. note:: A typical problem when working with human genome annotations is the inconsistency between chromosome names due to "chr" prefix. For example, Ensemble annotations do not include this prefix, while UCSC annotations do. This can become a problem when asscociating regions file with the BAM alignment. Qualimap handles this problem: if the reference sequence of a region has "chr" prefix, it tries to search for sequence name with prefix and without prefix.

:guilabel:`Library strand specificity`

  The sequencing protocol strand specificity: *non-strand-specific*, *forward-stranded* or *reverse-stranded*. This information is required to calculate the number of **correct strand** reads.

:guilabel:`Analyze outside regions` 
  If checked, the information about the **reads** that are **mapped outside** of the regions of interest will be also computed and shown in a separate section.

:guilabel:`Chromosome limits` 
  If selected, vertical dotted lines will be placed at the beginning of each chromosome according to the information found in the header of the BAM file.

.. _input-gc-content:

:guilabel:`Compare GC content distribution with` 
  This allows to **compare** the **GC distribution** of the sample with the selected pre-calculated **genome** GC distribution. Currently two genome distributions are available: human (hg19) and mouse (mm9). More species will be included in future releases.

:guilabel:`Skip duplicates` 
  This option allows to skip duplicated alignments from analysis. If the duplicates are not flagged in BAM file, then they will be detected by Qualimap. Type of skipped duplicates will be shown in report.

:guilabel:`Compare GC content distribution with` 
  This allows to **compare** the **GC distribution** of the sample with the selected pre-calculated **genome** GC distribution. Currently two genome distributions are available: human (hg19) and mouse (mm9). More species will be included in future releases.


Advanced parameters
"""""""""""""""""""

:guilabel:`Number of windows`
  Number of **windows** used to **split** the reference **genome**. This value is used for computing the graphs that plot information across the reference. Basically, reads falling in the same window are aggregated in the same bin. The higher the number, the bigger the resolution of the plots but also longer time will be used to process the data. By default 400 windows are used.

:guilabel:`Homopolymer size`
  Only homopolymers of this size or larger will be considered when estimating homopolymer indels count. 

:guilabel:`Number of threads`
  In order to speed up the computation, the BAM QC analysis **computation** can be performed **in parallel** on a multicore system using the given number of threads. More information on the parallelization of qualimap can be found in :ref:`FAQ <faq>`. The default number of threads equals number of available processors.

:guilabel:`Size of the chunk`
  In order to **reduce the load of I/O**, reads are analyzed in chunks. Each chunk contains the selected number of reads which will be loaded into memory and analyzed by a single thread. Smaller numbers may result in lower performance, but also the memory consumption will be reduced. The default value is 1000 reads.


Output
^^^^^^

:guilabel:`Summary` 

  **Basic information** and statistics for the alignment data. The following sections are available:
  
    *Globals* 
  
    This section contains information about the total number of reads, number of mapped reads, paired-end mapping performance, read length distribution, number of clipped reads and duplication rate (estimated from the start positions of read alignments).
    
    *ACGT Content* 
  
    Nucleotide content and GC percentage in the mapped reads.
     
    *Coverage* 
   
    Mean and standard deviation of the coverage depth.    
    
    *Mapping quality* 
    
    Mean mapping quality of the mapped reads.
    
    *Insert size* 

    Mean, standard deviation and percentiles of the insert size distribution if applicable. The features are computed based on the TLEN field of the SAM file.    
    
    *Mismatches and indels* 

    The section reports general alignment error rate (computed as a ratio of total collected edit distance to the number of mapped bases), total number of mismatches and total number of indels (computed from the CIGAR values). Additionally fraction of the homopolymer indels among total indels is provided. Note, the error rate and mismatches metrics are based on optional fields of a SAM record (**NM** for edit distance, **MD** for mismatches). The features are not reported if these fields are missing in the SAM file.
    
    *Chromosome stats* 

    Number of mapped bases, mean and standard deviation of the coverage depth for each chromosome as defined by the header of the SAM file.
  
  For region-based analysis the information is given inside of regions, including some additional information like, for example, number of correct strand reads.

:guilabel:`Input` 

  Here one can check the **input data** and the **parameters** used for the analysis.

:guilabel:`Coverage Across Reference`

  This plot consists of two figures. The upper figure provides the **coverage distribution** (red line) and coverage deviation across the reference sequence. The coverage is measured in *X* [#X]_. The lower figure shows **GC content** across reference (black line) together with its average value (red dotted line).

:guilabel:`Coverage Histogram` 

  Histogram of the number of **genomic locations** having a given **coverage rate**. The bins of the *x*-axis are conveniently scaled by aggregating some coverage values in order to produce a representative histogram also in presence of the usual NGS peaks of coverage.

:guilabel:`Coverage Histogram (0-50X)` 

   Histogram of the number of **genomic locations** having a given **coverage rate**. In this graph genome locations with a coverage greater than **50X** are grouped into the last bin. By doing so a higher resolution of the most common values for the coverage rate is obtained.


:guilabel:`Genome Fraction Coverage`

  Provides a visual way of knowing how much **reference** has been **sequenced** with **at least** a given **coverage rate**. This graph should be interpreted as in this example:

  If one aims a coverage rate of **at least 25X** (*x*-axis), how much of reference (*y*-axis) will be considered? The answer to this question in the case of the whole-genome sequencing `provided example <http://qualimap.bioinfo.cipf.es/samples/ERR089819_result/qualimapReport.html#genome_coverage_quotes.png>`_ is **~83%**.

:guilabel:`Duplication Rate Histogram` 

  This plot shows the **distribution** of **duplicated** read **starts**. Due to several factors (e.g. amount of starting material, sample preparation, etc) it is possible that the same **fragments** are **sequenced several times**. For some experiments where enrichment is used (e.g. ChIP-seq ) this is expected at some *low* rate. If most of the reads share the exact same genomic positions there is very likely an associated bias.  


:guilabel:`Mapped Reads Nucleotide Content` 

  This plot shows the **nucleotide content per position** of the **mapped reads**.

:guilabel:`Mapped Reads GC Content Distribution` 

  This graph shows the distribution of **GC content** per **mapped read**. If compared with a precomputed :ref:`genome distribution <input-gc-content>`, this plot allows to check if there is a shift in the GC content. 

:guilabel:`Mapped Reads Clipping Profile`

  Represents the percentage of clipped bases across the reads. The clipping is detected via SAM format CIGAR codes ‘H’ (hard clipping) and ‘S’ (soft clipping). In addition, the total number of clipped reads can be found in the report Summary. The plot is not shown if there are no clipped-reads are found. Total number of clipped reads can be found in :guilabel:`Summary`. `Example <http://qualimap.bioinfo.cipf.es/samples/HG00096.chrom20_result/qualimapReport.html#genome_reads_clipping_profile.png>`_.


:guilabel:`Homopolymer Indels`

  This bar plot shows separately the number of indels that are within a **homopolymer** of A's, C's, G's or T's together with the number of **indels** that are not within a homopolymer. Large numbers of homopolymer indels may indicate a problem in a sequencing process. An indel is considered homopolymeric if it is found within a homopolymer (defined as at least 5 equal consecutive bases). Owing to the fact that Qualimap works directly from BAM files (and not from reference genomes), we make use of the CIGAR code from the corresponding read for this task. 
  Indel statistics cam be found in a dedicated section of the report Summary.

  This chart is not shown if the sample doesn't contain any indels.


:guilabel:`Mapping Quality Across Reference` 

  This plot provides the **mapping quality** distribution **across the reference**.

:guilabel:`Mapping Quality Histogram` 

  Histogram of the number of **genomic locations** having a given **mapping quality**. According to Specification of the `SAM format <http://samtools.sourceforge.net/SAM1.pdf>`_ the range for the mapping quality is [0-255].


:guilabel:`Insert Size Across Reference`

  This plot provides the **insert size** distribution **across the reference**. Insert size is collected from the SAM alignment field TLEN. Only positive values are taken into account.

:guilabel:`Insert Size Histogram`

  Histogram of **insert size** distribution.


.. _rnaseqqc:

RNA-seq QC
----------

RNA-seq QC reports quality control metrics and bias estimations which are specific for whole transcriptome sequencing, including reads genomic origin, junction analysis, transcript coverage and 5'-3' bias computation. This analysis could be applied as a complementary tool together with :ref:`BAM QC<bamqc>` and additionally to produce gene counts for further analysis with :ref:`Counts QC <countsqc>`.

To start a new RNA-seq QC analysis activate main menu item :menuselection:`File --> New Analysis --> RNA-seq QC`.

Examples
^^^^^^^^

- `RNA-seq QC report <http://kokonech.github.io/qualimap/kidney_rnaseqqc/qualimapReport.html>`_. This report was produced using the RNA-seq alignment of *Homo sapiens* kidney sample [Marioni]_ and Ensembl v.64 GTF file.
- These data can be downloaded from :ref:`here <bam-samples>`.

Input parameters
^^^^^^^^^^^^^^^^
 

:guilabel:`BAM file`
  Path to the sequence alignment file in **BAM** format, produced by a splicing-aware aligner similar to `Tophat <http://tophat.cbcb.umd.edu>`_. 

:guilabel:`GTF file`
  Genomic annotations in Ensembl **GTF** format. The corresponding annotations can be downloaded from the `Ensembl website <www.ensembl.org/downloads.html>`_.

.. note:: Only annotations in GTF format are supported for this analysis mode. GTF annotations allow to reconstruct the exon structure of transcripts to compute the coverage. For simple region-based analysis please use BAM QC. 

:guilabel:`Library protocol`
  The strand-specficity of the sequencing library. By default non-strand specific library is assumed.


:guilabel:`Paired-end analysis`
   This option activates counting of pair fragments instead of counting of single reads. Only valid for paired-end sequencing experiments.

:guilabel:`Alignment sorted by name`
   The paired-end analysis requires the BAM file to be sorted by name. If the BAM file is already sorted by name, then this option should be check, otherwise temporary BAM sorted by name will be created.

:guilabel:`Output counts`
   If checked, the gene counts will be saved to a specified file. 

:guilabel:`Path to counts`
   Path to the output file with the computed counts.


Advanced parameters
"""""""""""""""""""

:guilabel:`Multi-mapped reads`
   Select method to count reads that are mapped to several genome locations. By default only **uniquely-mapped-reads** are used to compute counts. However, it is possible to include multimapped reads by activating **proprtional** method. More details :ref:`here <multimapped-reads-count>`.
        

Output
^^^^^^

:guilabel:`Summary`

  The summary contains the following sections:

    *Reads alignment* 

    The assignment of read counts per-category: 
        - total number of mapped reads 
        - total number of alignments 
        - number of secondary alignments (duplicates are marked as SAM flag)
        - number of non-unique alignments (SAM format "NH" tag of a read is  more than one) 
        - number of reads aligned to genes, also without any feature (intronic and intergenic) 
        - number of ambiguous alignments 
        - number of unmapped reads.
   
    *Transcript coverage profile* 

    The ratios between mean coverage at the 5' region, 3' region and whole transcript.

    *Reads genomic origin*
    
    Shows how many alignments fall into exonic, intronic and intergenic regions. Exonic region includes 5'UTR,protein coding region and 3'UTR region.

    *Junction analysis*

    Total number of reads with splice junctions and 10 most frequent junction rates.
   
:guilabel:`Input`

  Here one can check the **input data** and the **parameters** used for the analysis.


:guilabel:`Reads Genomic Origin`

    Pie chart showing how many of read alignments fall into exonic, intronic and intergenic regions.

:guilabel:`Coverage Profile (Total)`

  The plot shows mean coverage profile of the transcripts. All transcripts with non-zero coverage are used to calculate this plot.

:guilabel:`Coverage Profile (Low)`

  The plot shows mean coverage profile of 500 lowest-expressed genes. 

:guilabel:`Coverage Profile (Total)`

  The plot shows mean coverage profile of 500 highest-expressed genes.

:guilabel:`Coverage Histogram (0-50x)`

  Coverage of transcripts from 0 to 50X. If certain genes have higher coverage level they are added to the last column (50X).

:guilabel:`Junction Analysis`

  This pie chart shows analysis of junction positions in spliced alignments. **Known** category represents percentage of alignments where both junction sides are known. **Partly known** represents alignments where only one junction side is known. All other alignments with junctions are marked as **Novel**.


.. _countsqc:

Counts QC
---------

In **RNA-seq** experiments, the reads are usually **first mapped** to a reference genome. It is assumed that if the **number of reads** mapping to a certain biological feature of interest (gene, transcript, exon, ...) is sufficient, it can be used as an **estimation** of the **abundance** of that feature in the sample and interpreted as the quantification of the **expression level** of the corresponding region.

These **count data** can be utilized for example to assess differential expression between two or more experimental conditions. Before assesing differential expression analysis, researchers should be aware of some potential **limitations** of RNA-seq data, as for example: Has the **saturation** been reached or more features could be detected by increasing the sequencing depth? Which **type of features** are being detected in the experiment? How good is the **quantification** of expression in the sample? All of these questions are answered by interpreting the plots generated by Counts QC.


Starting from **version 2.0** Counts QC module has been redisigned to work with **multiple samples** under different conditions. The new functionality is based on `NOISeq package <http://www.bioconductor.org/packages/release/bioc/html/NOISeq.html>`_, therefore to use Counts QC it is required to have **R** language along with **NOISeq** and **optparse** packages installed. 

To run this analysis activate from the main menu :menuselection:`File --> New Analysis --> Counts QC`. 

.. note::

    If count data need to be generated, one can use the provided tool :ref:`compute-counts`.

Example
^^^^^^^

- RNA-seq counts analysis from 2 experiments can be found :ref:`here <counts-example-output>`


- Sample counts data can be downloaded from :ref:`here <counts-samples>`.

Input Parameters
^^^^^^^^^^^^^^^^

:guilabel:`Samples`

    The input samples can be added using button :guilabel:`Add`. 

    For each input sample it is required to provide the following information: 

    * **Sample name**. Name of the analyzed sample as it will be used as a legend in the plots.
    
    * **Path** to the input file containing the counts data for the sample. This must be a **tab-delimited** file with at least **two columns**. First column of the file must contain feature IDs, while other columns should contain counts for features. Rows starting with # symbol and empty lines are ignored. 

    * **Data column index**. By default it is assumed that the counts are contained in the second column of the input file. However if the input file contains counts for multiple samples it is possible to define the column corresponding for the sample.

    * **Condition index**. If comparison of conditions is activated, this index defines under which condition was the input sample.

    Each added sample will be shown in **Samples** table. One can edit samples using button :guilabel:`Edit` and remove them using button :guilabel:`Remove`.


:guilabel:`Counts threshold`

    In order to **remove** the influence of **spurious reads**, a feature is considered as detected if its corresponding number of counts is **greater than this threshold**. By default, the threshold value is set to 5 counts, meaning that features having less than 5 counts will not be taken into account.


:guilabel:`Compare conditions`

    This option allows to compare groups of samples under different conditions. The name of a specific condition can be given using field :guilabel:`Condition name`.

.. note:: 
    
    Currently Qualimap allows to compare samples under two conditions. More conditions will be supported in future versions.

:guilabel:`Include feature classification`
    
    **Optional**. This option enables analysis of distribution of counts among feature groups defined by the biotype. In addition GC-content and length bias will be estimated.

:guilabel:`Species`

    For convinience, Qualimap provides the `Ensembl <http://www.ensembl.org/>`_ annotations for certain species (currently *Human* and  *Mouse*). In order to use these annotations, **Ensembl Gene IDs** should be used as the feature IDs on the **count files** (e.g. ENSG00000251282). If this is true, mark the box to enable this option and select the corresponding species. More annotations and species will be made available in future releases.


:guilabel:`Info File`

    File containing annotations of the features of the count files. It must be a **four-column** **tab-delimited** text file, with the features names or IDs in the first column, the group (e.g. the biotype from Ensembl database) in the second column, feature GC content in the third column and feature length in the last column (see `human.ens68.txt <http://kokonech.github.io/qualimap/samples/human.ens68.txt>`_ for an example). Please, make sure that the **features IDs** on this file are the same in the **count files**.

.. note::
    To generate info file based on an arbitrary GTF annotations and genome FASTA file, one can use the following `Python script <https://bitbucket.org/kokonech/qualimap/src/master/util/createQualimapInfoFile.py?at=master>`_ available from Qualimap repo.


Output
^^^^^^

Many of plots in Counts QC mode are created using `NOISeq package <http://www.bioconductor.org/packages/release/bioc/html/NOISeq.html>`_. The `NOISeq vignette <http://www.bioconductor.org/packages/release/bioc/vignettes/NOISeq/inst/doc/NOISeq.pdf>`_ contains a lot of useful information about the plots and how to interpret them. Here we provide short explanation of the plots.

Global Plots
""""""""""""

Plots from this report present a global overview of the counts data and include all samples.

:guilabel:`Counts Density`

    This plot shows density of counts computed from the histogram of log-transformed counts. In order to avoid infinite values in case of zero counts the transformation *log2(expr + 0.5)* is applied, where *expr* is a number of read counts for a given feature. Only log-transformed counts having value greater than 1 are plotted.


:guilabel:`Scatterplot Matrix`

    The panel shows a scatterplot along with smoothed line (lower panel) and Pearson correlation coefficients (upper panel) for each pair of samples. Plots are generated using log-transformed counts.

:guilabel:`Saturation`

  This plot provides information about the level of saturation in the samples, so it helps the user to decide if more sequencing is needed and more features could be detected when increasing the number of reads. These are some tips for the interpretation of the plot: 
  
  * The increasing sequencing depth of the sample is represented at the *x*-axis. The maximum value is the real sequencing depth of the sample(s). Smaller sequencing depths correspond to samples randomly generated from the original sample(s).
  *  The curves are associated to the left *y*-axis. They represent the number of detected features at each of the sequencing depths in the *x*-axis. By "detected features" we refer to features with more than *k* counts, where *k* is the *Count threshold* selected by the user.
  * The bars are associated to the right *y*-axis. They represent the number of newly detected features when increasing the sequencing depth in one million reads at each sequencing depth value.
  
.. An example for this plot can be seen `here <http://qualimap.bioinfo.cipf.es/samples/counts_result/qualimapReport.html#GlobalSaturation.png>`_. 


.. TODO: fix this

.. :guilabel:`Samples Correlation`

..   When two samples are provided, this plot determines the **correlation level** between both samples. Due to the often wide range of expression data (counts), a log2-transformation is applied in order to improve the graphical representation. Features not detected in any of the two samples are removed for this analysis. To avoid infinite values in the case of genes with 0 counts in one of the samples, log2(expression + 1) is used.  Thus, sample 1 is depicted in X-axis and sample 2 in Y-axis.
  The colors of the plot should be interpreted as a map. The blue color is the level of the sea and the white color the top of the mountain. Hence, the higher you are over the sea level, the more genes you have in that range of X-Y values.
  In addition, the title of the plot includes the **Pearson's correlation coefficient**, which indicates if both samples present a linear relationship.


:guilabel:`Counts Distribution`

    This box plot shows the global distribution of counts in each sample.

:guilabel:`Features With Low Counts`

    This plot shows the proportion of features with low counts in the samples. Such features are usually less reliable and could be filtered out. In this plot, the bars show the percentage of features within each sample having more than 0 counts per million (CPM), or more than 1, 2, 5 and 10 CPM.


Individual Sample Plots
"""""""""""""""""""""""

Apart from global overview there are plots generated individually for each sample. 

:guilabel:`Saturation`
    
    For each sample, a saturation plot is generated like the one described in :guilabel:`Global Saturation`.

When a **Info File** is provided by the user or annotations are chosen from those supplied by Qualimap, additional series of plots are generated:

:guilabel:`Bio Detection`

  This barplot allows the user to know which kind of features are being detected his sample(s). The *x*-axis shows all the groups included in the annotations file. The grey bars are the percentage of features of each group within the reference genome (or transcriptome, etc.). The striped color bars are the percentages of features of each group detected in the sample with regard to the genome. The solid color bars are the percentages that each group represents in the total detected features in the sample.

:guilabel:`Counts Per Biotype`

  A boxplot per each group describes the counts distribution in the given biotype.

:guilabel:`Length Bias`

    The plot describes the relationship between the length of the features and the expression values. The length is divided into bins. Mean expression of features falling into a particular length interval is computed and plotted. A cubic spline regression model is fitted to explain the relation between length and expression. `Coefficient of determination R^2 <http://en.wikipedia.org/wiki/Coefficient_of_determination>`_ and p-value are shown together with regression curve.

:guilabel:`GC Bias`

    The plot describes the relantionship between the GC-content of the features and the expression values. The data for the plot is generated similar to :guilabel:`Length Bias` plot. The GC content divided into beans and then mean expression of features corresponding to given GC interval are computed. The relation between GC-content and expression is investigated using cubic spline regression model.


Comparison Plots
""""""""""""""""

When **Compare conditions** option is selected, additional plots comparing data in groups of samples having the same biological condition or treatment are available.

:guilabel:`Counts Distribution`

    The plot is similar to the one in :guilabel:`Global` report. It compares distributions of **mean** counts across conditions.

:guilabel:`Features With Low Counts`

    The plot is similar to the one in :guilabel:`Global` report. It compares proportions of features with low counts using **mean** counts across conditions.

:guilabel:`Bio Detection`

    The plot is similar to the one in :guilabel:`Indvidual Sample Plots` report. It compares distribution of the detected features for the given biotype for **mean** counts across conditions.

:guilabel:`Length Bias`

    The plot is similar to the one in :guilabel:`Individual Sample Plots` report. It analyzes relation between feature length and expression across conditions.

:guilabel:`GC Bias`

    The plot is similar to the one in :guilabel:`Individual Sample Plots` report. It analyzes realtion between GC-content and expression across conditions.


.. _multibamqc:

Multi-sample BAM QC
-------------------

Very often in genomics one has to work with multiple samples, which could represent sequencing results from either biological replicates or different conditions. For example, to reliably detect significant mutations from sequencing data in cancer it is required to analyze tens or even hundreds of samples from matched normal-tumor data. When performing such large scale experiments it is always important to know if all samples pass the quality controls. To detect possible outliers one can compare results of :ref:`BAM QC analysis<bamqc>` performed on each individual sample. 

QualiMap provides an automated solution for this task. Basically, the QC metrics computed in *BAM QC analysis* are combined together for all samples. Additionally **Principal Component Analysis** is performed to analyze variability and detect outliers.

One can apply multi-sample analysis for precomputed results of QualiMap BAM QC or directly for raw BAM files. In latter case firstly BAM QC analysis will be performed for each input file and then multi-sample analysis will be executed.

To start a new multi-sample BAM QC analysis activate main menu item :menuselection:`File --> New Analysis --> Multisample BAM QC`.

Examples
^^^^^^^^

- `gH2AX Chip-seq data: 4 conditions, 3 replicates per condition <http://kokonech.github.io/qualimap/gh2ax_multibamqc/multisampleBamQcReport.html>`_. Example report for a ChIP-seq experiment having 12 samples.
 
- See the :ref:`Sample data <samples>` section for more details about the data used in the example.

Input Parameters
^^^^^^^^^^^^^^^^

There are 2 types of input data that are accepted by *Multi-sample BAM QC*:

1. By default directory with the summary statistics and plot data produced by BAM QC analysis is expected as input data for multi-sample comparison. 

2. If a special **"raw data" mode** is activated, then BAM files can be provided as input. In this case Qualimap will first run the :ref:`BAM QC analysis<bamqc>` on each indvidual BAM file, and then multi-sample report will be computed. 

The input samples can be added using button :guilabel:`Add`. For each sample one has to provide the following information:

1. **Name** of the sample as it will be used in legend.

2. **Path** to the folder with which contains results of BAM QC analysis performed on the sample. The folder must include file **genome_results.txt** and subfolder **raw_data_qualimapReport** containing data of BAM QC plots. If **"Raw data" mode** is activated then the path to the BAM file should be provided.

.. note::

   In QualiMap version <= 2.0 directory with raw data of BAM QC analysis was called **raw_data**. This name is also supported.

Each added sample will be shown in **Samples** table. One can edit samples using button :guilabel:`Edit` and remove them using button :guilabel:`Remove`.


:guilabel:`"Raw data" mode: run BAM QC on input samples`


    Activate this checkbox to analyze BAM files directly. A selected set of options is available to customize *BAM QC* process. One can read detailed explantion of these options in a :ref:`corresponding section<bamqc>` of the manual.


To start the analysis click button :guilabel:`Run analysis`.


Output
^^^^^^

:guilabel:`Summary` 

  The summary table contains comparison of selected critical alignment metrics for all samples. The metrics include mean and standard deviation of coverage, mean GC content, mean insert size and mean mapping qualities. 

:guilabel:`Input` 

  Here one can check the **input data** and the **parameters** used for the analysis.

:guilabel:`PCA`

  The alignment features presented in the *Summary* section undergo `Principal Component Analysis <http://en.wikipedia.org/wiki/Principal_component_analysis>`_. Afterwards the `biplot <http://en.wikipedia.org/wiki/Biplot>`_ presenting first and second principal component is constructed. The plot shows how much variability demonstarte the analyzed samples. It allows to detect if any samples group together and if there are any outliers among analyzed samples.

:guilabel:`Coverage Across Reference`, :guilabel:`Coverage Histogram (0-50X)` , :guilabel:`Genome Fraction Coverage`, :guilabel:`Duplication Rate Histogram`, :guilabel:`Mapped Reads GC Content`, :guilabel:`Mapped Reads GC Content Distribution`, :guilabel:`Mapped Reads Clipping Profile`, :guilabel:`Mapping Quality Across Reference`, :guilabel:`Mapping Quality Histogram`, :guilabel:`Insert Size Across Reference`, :guilabel:`Insert Size Histogram`

The following plots demonstrate the comparison of samples using data from corresponding plots computed during BAM QC analysis. Each curve on a plot represents a single sample.

Please refer to documentation of :ref:`BAM QC<bamqc>` for detailed information about the plots.


  \*\*\*


.. [#X] Example for the meaning of *X*: If one genomic region has a coverage of 10X, it means that, on average, 10 different reads are mapped to each nucleotide of the region.

.. [#biomart] Downloaded from `Biomart v.61 <http://feb2011.archive.ensembl.org/biomart/martview>`_. 

.. [Marioni] Marioni JC et al, "RNA-seq: An assessment of technical reproducibility and comparison with gene expression arrays". Genome Res. 2008. 18: 1509-1517.
