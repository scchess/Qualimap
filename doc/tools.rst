.. _tools:


Tools
=====

.. _compute-counts:

Compute counts
--------------

- Given a BAM file and an annotation (`GTF file <http://genome.ucsc.edu/FAQ/FAQformat.html#format4>`_), this tool calculates how many reads are mapped to each region of interest.

  
- The user can decide:

  - At which level wants to perform the counting (genes, transcripts...).

  - What to do whith reads mapped to multiple locations.

  - The strand-specifity.


- To access the tool use :menuselection:`Tools --> Compute counts`. 

.. _example-compute-counts:

Example
^^^^^^^

PONER!!!

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
  The user can select the **attribute** of the GTF file to be used as the **feature ID**. Regions with the same ID will be aggregated as part of the same feature. The application preload the first 1000 lines of the file so a list with possible feature IDs is conveniently provided.

:guilabel:`Feature type`
  The user can select the **feature type** (value of the third column of the GTF) considered for counting. Other types will be ignored. The application preload the first 1000 lines of the file so a list with possible feature IDs is conveniently provided.

:guilabel:`Output`
  Path to the file which will contain output.

:guilabel:`Save computation summary`
  This option controls whether to save overall computation statistics.


:guilabel:`Multi-mapped reads`
  This option controls what to do whith reads mapped to multiple location:


  :dfn:`uniquely-mapped-reads`
    Reads mapped to multiple locations will be ignored.


  :dfn:`proportional`
    Each read is weighted according to the number of mapped locations. For example, a read mapped to 4 different locations will add 0.25 to the counts of each location.

Output
^^^^^^


