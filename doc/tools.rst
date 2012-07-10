.. _tools:


Tools
=====

.. _compute-counts:

Compute counts
--------------

Given a BAM file and an annotation (`GTF file <http://genome.ucsc.edu/FAQ/FAQformat.html#format4>`_), this tool calculates how many reads are mapped to each region of interest.

  
The user can decide:

  - At which level wants to perform the counting (genes, transcripts...).

  - What to do whith reads mapped to multiple locations.

  - The strand-specifity.

Additionaly the tool allows to calculate 5' and 3' prime coverage bias.

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
  Path to the GTF file containing regions of interest.

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
  This option controls whether to save overall computation statistics. !Say where!


:guilabel:`Multi-mapped reads`
  This option controls what to do whith reads mapped to multiple location:


  :dfn:`uniquely-mapped-reads`
    Reads mapped to multiple locations will be ignored.


  :dfn:`proportional`
    Each read is weighted according to the number of mapped locations. For example, a read mapped to 4 different locations will add 0.25 to the counts of each location.

:guilabel:`Calculate 5' and 3' coverage bias`
   When this option is selected coverage of 1000 most expressed genes is analysed to determine coverage bias in 5' and 3' ends of the genes. Note, that this option currently works only for canonical gene model as it presented in GTF file i.e. if `Feature ID` equals **gene_id** and `Feature type` equals **exon**, and also attributes must include **transcript_id**.    


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
 
