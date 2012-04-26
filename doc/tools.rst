.. _tools:


Tools
=====

.. _compute-counts:

Compute counts
--------------

- Given a BAM file and an annotation (`GTF file <http://genome.ucsc.edu/FAQ/FAQformat.html#format4>`_), this tool calculates how many reads are mapped to each region of interest.

  
- The user can select the **attribute** of the GTF file to be used as the **feature ID**. Regions with the same ID will be aggregated as part of the same feature. The application preload the first 1000 lines of the file so a list with possible feature IDs is conveniently provided.

- The user can select the **feature type** (value of the third column of the GTF) considered for counting. Other types will be ignored. The application preload the first 1000 lines of the file so a list with possible feature IDs is conveniently provided.


To access the tool use menu item :menuselection:`Tools --> Compute counts`. 

.. _example-compute-counts:

**Example**: Counts per gene of an Human RNA-seq study.
^^^^^^^^^^^^

- Input data:

  - BAM file: `<link!>`. RNA-seq of liver tissue from [Marioni]

  - GTF file: `<link!>`. Human annotation from Ensembl (v. 64)

  - Parameters:

    - Feature ID: gene_id (to count at the level of genes)
    - Feature type: exon (to ignore other features like ...!!)
    - Multimapped reads: uniquely-mapped-reads (to ignore not unique alignments)

- Output:

  - `<link>`. Text file 


Input
^^^^^

:guilabel:`BAM file` 
  Path to a BAM alignment file
:guilabel:`Annotation file` 
  Path to a GTF file containing regions of interest

:guilabel:`Protocol` 
  Three options are avalalbe:

  :dfn:`non-strand-specific` 
    Reads are counted if mapped to a feature independent of strand
  :dfn:`forward-stranded` 
    The single-end reads must have the same strand as the feature. For paired-end reads first of a pair must have the same strand as the feature, while the second read must be on the opposite strand. 
  :dfn:`reverse-strand` 
    The single-end reads must have the strand opposite to the feature. For paired-end reads first of a pair must have opposite strand, while the second read must be on the same strand as the feature.

:guilabel:`Feature type`
  Third column of the GTF file. Only features of this particular type are counted.
:guilabel:`Feature name`
  The name of the feature to be counted.
:guilabel:`Output`
  Path to the file which will contain output.
:guilabel:`Save computation summary`
  This option controls whether to save overall computation statistics.

Output
^^^^^^


