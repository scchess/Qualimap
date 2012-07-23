.. _analysis-types:

Analysis types
==============

.. _bamqc:

BAM QC
------

BAM QC reports information for the evaluation of the quality of the provided alignment data (a BAM file). In short, the basic statistics of the alignment (number of reads, coverage, GC-content, etc.) are summarized and a number of useful graphs are produced. This analysis can be performed with any kind of sequencing data, e.g. whole-genome sequencing, exome sequencing, RNA-seq, ChIP-seq, etc.

In addition, it is possible to provide an annotation file so the results are computed for the reads mapping inside (and optionally outside) of the corresponding genomic regions, which can be especially useful for evaluating RNA-seq studies.

To start a new BAM QC analysis activate main menu item :menuselection:`File --> New Analysis --> BAM QC`.


Example
^^^^^^^

- `Whole-genome sequencing: HG00096.chrom20.bam <http://qualimap.bioinfo.cipf.es/samples/HG00096.chrom20_result/qualimapReport.html>`_. Report for sample alignment file from `1000 Genomes project <http://1000genomes.org>`_.
 
.. - Why is it no working?


 
- `Whole-genome sequencing: ERRR089819.bam <http://qualimap.bioinfo.cipf.es/samples/ERR089819_result/qualimapReport.html>`_. Report created using the whole-genome sequencing data of *Caenorhabditis elegans* from the following `study <http://trace.ncbi.nlm.nih.gov/Traces/sra/?study=ERP000975>`_.

.. !!FIX!!- `Whole-genome sequencing <http://qualimap.bioinfo.cipf.es/samples/plasm/qualimapReport.html>`_. Report created using the  whole-genome sequencing data of *Plasmodium falciparum* produced by *Wellcome Trust Sanger Institute*.
.. - `RNA-seq <http://qualimap.bioinfo.cipf.es/samples/plasm_RNASeq/qualimapReport.html>`_. Report created using the RNA-seq data of *Plasmodium falciparum* produced by *Wellcome Trust Sanger Institute* as well as the provided gene annotations. Information for reads mapped outside the genes was also produced (report `here <plasmodium_RNA-seq/qualimapReportOutsideOfRegions.html>`_).

- See the :ref:`Sample data <samples>` section for more details about the data used in the examples.


Input Parameters
^^^^^^^^^^^^^^^^

:guilabel:`BAM file` 
  Path to the sequence alignment file in **BAM format**. Note, that the BAM file has to be **sorted by chromosomal coordinates**. Sorting can be performed with `samtools sort <http://samtools.sourceforge.net/>`_.

:guilabel:`Analyze regions` 
  Activating this option allows the analysis of the alignment data for the **regions of interest**. 

:guilabel:`Regions file(GFF/BED file)` 
  The path to the annotation file that defines the regions of interest. The file must be **tab-separated** and have `GFF <http://genome.ucsc.edu/FAQ/FAQformat.html#format3>`_/`GTF <http://genome.ucsc.edu/FAQ/FAQformat.html#format4>`_  or `BED <http://genome.ucsc.edu/FAQ/FAQformat.html# format1>`_ format.

:guilabel:`Library strand specificity`

  The sequencing protocol strand specificity: *non-strand-specific*, *forward-stranded* or *reverse-stranded*. This information is required to calculate the number of **correct strand** reads.

:guilabel:`Analyze outside regions` 
  If checked, the information about the **reads** that are **mapped outside** of the regions of interest will be also computed and shown in a separate section.

:guilabel:`Chromosome limits` 
  If selected, vertical dotted lines will be placed at the beginning of each chromosome according to the information found in the header of the BAM file.

.. _input-gc-content:

:guilabel:`Compare GC content distribution with...` 
  This allows to **compare** the **GC distribution** of the sample with the selected pre-calculated **genome** GC distribution. Currently two genome distributions are available: human (hg19) and mouse (mm9). More species will be included in future releases.

Advanced parameters
"""""""""""""""""""

:guilabel:`Number of windows`
  Number of **windows** used to **split** the reference **genome**. This value is used for computing the graphs that plot information across the reference. Basically, reads falling in the same window are aggregated in the same bin. The higher the number, the bigger the resolution of the plots but also longer time will be used to process the data. By default 400 windows are used.

:guilabel:`Number of threads`
  In order to speed up the computation, the BAM QC analysis **computation** can be performed **in parallel** on a multicore system using the given number of threads. More information on the parallelization of qualimap can be found in :ref:`FAQ <faq>`. The default number of threads equals number of available processors.

:guilabel:`Size of the chunk`
  In order to **reduce the load of I/O**, reads are analyzed in chunks. Each chunk contains the selected number of reads which will be loaded into memory and analyzed by a single thread. Smaller numbers may result in lower performance, but also the memory consumption will be reduced. The default value is 1000 reads.


Output
^^^^^^

:guilabel:`Summary` 

  **Basic information** and statistics for the alignment data. Qualimap reports here information about the total number of reads, number of mapped reads, paired-end mapping performance, read length distribution, insert size, nucleotide content, coverage, number of indels, mapping quaility and chromosome-based statistics. 
  
  For region-based analysis the information is given inside of regions, including some additional information like, for example, number of correct strand reads.

:guilabel:`Input` 

  In this section information about the **input data** and parameters is shown.

:guilabel:`Coverage Across Reference`

  This plot consists of two figures. The upper figure provides the **coverage distribution** (red line) and coverage deviation across the reference sequence. The coverage is measured in *X* [#X]_. The lower figure shows **GC content** across reference (black line) together with its average value (red dotted line).

:guilabel:`Coverage Histogram` 

  Histogram of the number of **genomic locations** having a given **coverage rate**. The bins of the *x*-axis are conveniently scaled by aggregating some coverage values in order to produce a representative histogram also in presence of the usual NGS peaks of coverage.

:guilabel:`Coverage Histogram (0-50X)` 

   Histogram of the number of **genomic locations** having a given **coverage rate**. In this graph genome locations with a coverage greater than **50X** are grouped into the last bin. By doing so a higher resolution of the most common values for the coverage rate is obtained.

:guilabel:`Genome Fraction by Coverage`

  Provides a visual way of knowing how much **reference** has been **sequenced** with **at least** a given **coverage rate**. This graph should be interpreted as in this example:

  If one aims a coverage rate of **at least 25X** (*x*-axis), how much of reference (*y*-axis) will be considered? The answer to this question in the case of the whole-genome sequencing `provided example <http://qualimap.bioinfo.cipf.es/samples/ERR089819_result/qualimapReport.html#genome_coverage_quotes.png>`_ is **~83%**.

:guilabel:`Mapped Reads Nucleotide Content` 

  This plot shows the **nucleotide content per position** of the **mapped reads**.

:guilabel:`Mapped Reads Clipping Profile`

  This plot provides the **clipping profile histogram** along the **mapped reads**. The clipping is detected via SAM format CIGAR codes 'H' (hard clipping) and 'S' (soft clipping). Example is available `here <http://qualimap.bioinfo.cipf.es/samples/HG00096.chrom20_result/qualimapReport.html#genome_reads_clipping_profile.png>`_.
  
  The plot is not shown if there are no clipped-reads are found. Total number of clipped reads can be found in :guilabel:`Summary`.  

:guilabel:`Mapped Reads GC Content Distribution` 

  This graph shows the distribution of **GC content** per **mapped read**. If compared with a precomputed :ref:`genome distribution <input-gc-content>`, this plot allows to check if there is a shift in the GC content. 

:guilabel:`Homopolymer Indels`

  This bar chart provides the estimation of **homopolymer indels** of various types within alignment data. Large number of homopolymer indels may indicate a problem in a sequencing process. 
  
  An indel is considered homopolymeric if it overlaps a homopolymer sequence with a minimum size of **5 bp**. For genomic insertions accurate calculation is performed. In case of deletions similar calculation can not be performed without reference sequence, therefore only an estimation is provided: an insertion is estimated as homopolymeric if it has a flanking homopolymer downstream or upstream of its location in the read.    

  The chart is not shown if the sample doesn't contain any indels.


:guilabel:`Duplication Rate Histogram` 

  This plot shows the **distribution** of **duplicated** read **starts**. Due to several factors (e.g. amount of starting material, sample preparation, etc) it is possible that the same **fragments** are **sequenced several times**. For some experiments where enrichment is used (e.g. ChIP-seq ) this is expected at some *low* rate. If most of the reads share the exact same genomic positions there is very likely an associated bias.  

:guilabel:`Mapping Quality Across Reference` 

  This plot provides the **mapping quality** distribution **across the reference**.

:guilabel:`Mapping Quality Histogram` 

  Histogram of the number of **genomic locations** having a given **mapping quality**. According to Specification of the `SAM format <http://samtools.sourceforge.net/SAM1.pdf>`_ the range for the mapping quality is [0-255].

.. _countsqc:

Counts QC
---------

In **RNA-seq** experiments, the reads are usually **first mapped** to a reference genome. It is assumed that if the **number of reads** mapping to a certain biological feature of interest (gene, transcript, exon, ...) is sufficient, it can be used as an **estimation** of the **abundance** of that feature in the sample and interpreted as the quantification of the **expression level** of the corresponding region.

These **count data** can be utilized for example to assess differential expression between two or more experimental conditions. Before assesing differential expression analysis, researchers should be aware of some potential **limitations** of RNA-seq data, as for example: Has the **saturation** been reached or more features could be detected by increasing the sequencing depth? Which **type of features** are being detected in the experiment? How good is the **quantification** of expression in the sample? All of these questions are answered by interpreting the plots generated by Qualimap.

For assesing this analysis just activate from the main menu :menuselection:`File --> New Analysis --> Counts QC`. 

.. note::

    If count data need to be generated, one can use the provided tool :ref:`compute-counts`.

.. note::

   For this option to work, the **R** language must be **installed** along with the R package **optparse** (both are freely available from http://cran.r-project.org/).

Example
^^^^^^^

- `RNA-seq count data <http://qualimap.bioinfo.cipf.es/samples/counts_result/qualimapReport.html>`_. This report was produced using the counts from the RNA-seq of *Homo sapiens* kidney and liver samples [Marioni]_.
- These counts can be downloaded from :ref:`here <counts-samples>` or generated using the :ref:`compute-counts` tool.

Input Parameters
^^^^^^^^^^^^^^^^

:guilabel:`First sample (counts)` 

  File containing the count data from the sample. This must be a **two-column** **tab-delimited** text file, with the feature IDs in the first column and the number of counts in the second column. This file must not contain header nor column names. See :ref:`counts-samples` for examples

:guilabel:`First sample name`
 
  Name for the first sample that will be used as legend in the plots.

:guilabel:`Second sample (counts)`

  **Optional**. If a second sample is available, this file should contain the same information as in :guilabel:`First sample` for the second sample, i.e. the same feature IDs (first column) and the corresponding number of counts (second column). Mark the :guilabel:`Compare with other sample` checkbox to enable this option.

:guilabel:`Second sample name`

  Name for the second sample that will be used as legend in the plots.

:guilabel:`Count threshold`

  In order to **remove** the influence of **spurious reads**, a feature is considered as detected if its corresponding number of counts is **greater than this threshold**. By default, the theshold value is set to 5 counts, meaning that features having less than 5 counts will not be taken into account.

:guilabel:`Group File`

  **Optional**. File containing a classification of the features of the count files. It must be a **two columns** **tab-delimited** text file, with the features names or IDs in the first column and the group (e.g. the biotype from Ensembl database) in the second column (see `human.64.genes.biotypes <http://qualimap.bioinfo.cipf.es/samples/counts/human.64.genes.biotypes>`_ for an example). Again, the file must not contain any header or column names. If this file is provided, specific plots for each defined group are generated. Please, make sure that the **features IDs** on this file are the same in the **count files**.

:guilabel:`Species`

   **Optional**. For convinience, Qualimap provides the `Ensembl <http://www.ensembl.org/>`_ biotype classification [#biomart]_ for certain species (currently *Human* and  *Mouse*). In order to use these annotations, **Ensembl Gene IDs** should be used as the feature IDs on the **count files** (e.g. ENSG00000251282). If so, mark the box to enable this option and select the corresponding species. More annotations and species will be made available in future releases.

Output
^^^^^^

Global Plots
""""""""""""

:guilabel:`Global Saturation`

  This plot provides information about the level of saturation in the sample, so it helps the user to decide if more sequencing is needed or if no many more features will detected when increasing the number of reads. These are some tips for the interpretation of the plot: 
  
  * The increasing sequencing depth of the sample is represented at the *x*-axis. The maximum value is the real sequencing depth of the sample(s). Smaller sequencing depths correspond to samples randomly generated from the original sample(s).
  *  The curves are associated to the left *y*-axis. They represent the number of detected features at each of the sequencing depths in the *x*-axis. By "detected features" we refer to features with more than k counts, where k is the *Count threshold* selected by the user.
  * The bars are associated to the right *y*-axis. They represent the number of newly detected features when increasing the sequencing depth in one million reads at each sequencing depth value.
  
  An example for this plot can be seen `here <http://qualimap.bioinfo.cipf.es/samples/counts_result/qualimapReport.html#GlobalSaturation.png>`_. 

  When a **Group File** is **provided** by the user or chosen from those supplied by Qualimap, a series of **plots** are **additionally generated**:

:guilabel:`Detection per group`

  This barplot allows the user to know which kind of features are being detected his sample(s). The *x*-axis shows all the groups included in the :guilabel:`Group File` (or the biotypes supplied by Qualimap). The grey bars are the percentage of features of each group within the reference genome (or transcriptome, etc.). The striped color bars are the percentages of features of each group detected in the sample with regard to the genome. The solid color bars are the percentages that each group represents in the total detected features in the sample.

:guilabel:`Counts per group`

  A boxplot per each group describes the counts distribution for the detected features in that group.

Individual Group Plots
""""""""""""""""""""""

:guilabel:`Saturation per group`

 For each group, a saturation plot is generated like the one described in :guilabel:`Global Saturation`.

:guilabel:`Counts & Sequencing Depth`

  For each group, a plot is generated containing a boxplot with the distribution of counts at each sequencing depth. The *x*-axis shows the increasing sequencing depths of randomly generated samples from the original one till the true sequencing depth is reached. This plot allows the user to see how the increase of sequencing depth is changing the expression level quantification. 

.. [#X] Example for the meaning of *X*: If one genomic region has a coverage of 10X, it means that, on average, 10 different reads are mapped to each nucleotide of the region.

.. [#biomart] Downloaded from `Biomart v.61 <http://feb2011.archive.ensembl.org/biomart/martview>`_. 

.. [Marioni] Marioni JC et al, "RNA-seq: An assessment of technical reproducibility and comparison with gene expression arrays". Genome Res. 2008. 18: 1509-1517.
