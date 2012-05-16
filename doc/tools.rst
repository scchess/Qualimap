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


To access the tool use :menuselection:`Tools --> Compute counts`. 

.. note:: Currently "Compute Counts" tool analyzes paired-end reads as if they are independent; only the strand-specificity of the protocol is taken into account. This may lead to discrepancy, when computing the feature counts from paired-end data. We will add full support for paired-end reads in future versions of Qualimap.  


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

Output
^^^^^^

A two-column tab-delimited text file, with the feature IDs in the first column and the number of counts in the second column.

