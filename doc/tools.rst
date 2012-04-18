.. _tools:


Tools
=====

.. _compute-counts:

Compute counts
--------------

Given a BAM file and an annotation, this tool calculates how many reads are mapped to each region of interest. MORE!!!

To access the tool use menu item :menuselection:`Tools --> Compute counts`. 

.. _example-compute-counts:

Example
^^^^^^^

PONER!!!

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


