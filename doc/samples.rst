.. _samples:

Examples
========

Sample Data
-----------

.. _bam-samples:

Alignments
**********

- `ERR089819.bam <http://qualimap.bioinfo.cipf.es/samples/alignments/ERR089819.bam>`_ (2.6 GB)
   Whole genome sequencing data of C. elegans from the following `study <http://trace.ncbi.nlm.nih.gov/Traces/sra/?study=ERP000975>`_.

- `HG00096.chrom20.bam <http://qualimap.bioinfo.cipf.es/samples/alignments/HG00096.chrom20.bam>`_ (278 MB)
   Sequencing of the chromosome 20 from a H. sapiens sample from `1000 Genomes project <http://www.1000genomes.org/>`_. The header of the BAM file was changed in order to contain only chromosome 20. Original file can be found `here <ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/phase1/data/HG00096/alignment/HG00096.chrom20.ILLUMINA.bwa.GBR.low_coverage.20101123.bam>`_.

- `kidney.bam <http://qualimap.bioinfo.cipf.es/samples/counts/kidney.bam>`_ (386 MB) and `liver.bam <http://qualimap.bioinfo.cipf.es/samples/counts/liver.bam>`_ (412 MB)
   Human RNA-seq sequencing data from from the paper of `Marioni JC et al <http://genome.cshlp.org/content/18/9/1509.abstract>`_ 

Annotations
***********

.. _annotation-files:

- `human.64.gtf <http://qualimap.bioinfo.cipf.es/samples/annotations/human.64.gtf>`_ 
    Human genome annotations from Ensembl database (v. 64).
- `transcripts.human.64.bed <http://qualimap.bioinfo.cipf.es/samples/annotations/transcripts.human.64.bed>`_
    Human transcripts in BED format from Ensembl database (v. 64).

.. `Plasmodium-falciparum-3D7.gff <http://qualimap.bioinfo.cipf.es/samples/annotations/Plasmodium-falciparum-3D7.gff>`_ 
  Gene Annotations of Plasmodium falciparum 3D7 clone , from `Wellcome Trust Sanger Institue <http://www.sanger.ac.uk/resources/downloads/protozoa/plasmodium-falciparum.html>`_.

.. _multibamqc-samples:

Multisample BAM QC
******************

- `gh2ax_chip_seq.zip <http://kokonech.github.io/qualimap/samples/gh2ax_chip_seq.zip>`_
    
    Example dataset from an unpublished ChiP-seq experiment with 4 condtions, each having 3 replicates (12 sampels in total). The archive contains BAM QC results for each sample and input configuration for command line version of Multisample BAM QC.


.. _counts-samples:

Counts QC
*********

- `mouse_counts_ensembl.txt <http://kokonech.github.io/qualimap/samples/mouse_counts_ensembl.txt>`_
   Mouse counts data from a `study  <http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=GSE54853>`_ investigating effects of D-Glucosamine:

- `GlcN_countsqc_input.txt <http://kokonech.github.io/qualimap/samples/GlcN_countsqc_input.txt>`_
    Command line input configuration for the counts data above.


- `kidney.counts <http://qualimap.bioinfo.cipf.es/samples/counts/kidney.counts>`_ and `liver.counts <http://qualimap.bioinfo.cipf.es/samples/counts/liver.counts>`_
   Counts data from the paper by `Marioni JC et al <http://genome.cshlp.org/content/18/9/1509.abstract>`_.

- `marioini_countsqc_input.txt <http://kokonech.github.io/qualimap/samples/marioni_countsqc_input.txt>`_
    Command line input configuration for the counts data above.
 

.. _clustering-samples:

Clustering
**********

- `hmeDIP.bam <http://qualimap.bioinfo.cipf.es/samples/clustering/hmeDIP.bam>`_ (988M)
    MeDIP-seq of human embryonic stem cells from the study of `Stroud H et al <http://genomebiology.com/content/12/6/R54>`_.

- `input.bam <http://qualimap.bioinfo.cipf.es/samples/clustering/input.bam>`_ (1.8G)
    Input data of the same study

Sample Output
-------------

BAM QC
******

Analysis of the WG-seq data (HG00096.chrom20.bam): `QualiMap HTML report <http://rawgit.com/kokonech/kokonech.github.io/master/qualimap/HG00096.chr20_bamqc/qualimapReport.html>`_.

Analysis of the WG-seq data (ERR089819.bam): `QualiMap PDF report <http://rawgit.com/kokonech/kokonech.github.io/master/qualimap/ERR089819_report.pdf>`_.


RNA-seq QC
**********

Analysis of RNA-seq data (kidney.bam, human.64.gtf): `QualiMap HTML report <http://rawgit.com/kokonech/kokonech.github.io/master/qualimap/kidney_rnaseqqc/qualimapReport.html>`_.


Multisample BAM QC
******************

Multisample analysis of 12 gH2AX ChiP-seq alignments: `Qualimap HTML report <http://rawgit.com/kokonech/kokonech.github.io/master/qualimap/gh2ax_multibamqc/multisampleBamQcReport.html>`_.


Counts QC
*********

.. _counts-example-output:

Counts QC HTML reports computed from RNA-seq experiment analyzing influence of D-Glucosamine on mice. The analysis was performed for 6 samples in 2 conditions - GlcN positive and negative (mouse_counts_ensembl.txt): 

- `Global report <http://kokonech.github.io/qualimap/glcn_mice_counts/GlobalReport.html>`_ 

- `Comparison of conditions <http://kokonech.github.io/qualimap/glcn_mice_counts/ComparisonReport.html>`_

- `Sample 01 (GlcN negative) <http://kokonech.github.io/qualimap/glcn_mice_counts/nGlcn01Report.html>`_

- `Sample 02 (GlcN negative) <http://kokonech.github.io/qualimap/glcn_mice_counts/nGlcn02Report.html>`_

- `Sample 03 (GlcN negative) <http://kokonech.github.io/qualimap/glcn_mice_counts/nGlcn03Report.html>`_

- `Sample 04 (GlcN positive) <http://kokonech.github.io/qualimap/glcn_mice_counts/pGlcn01Report.html>`_

- `Sample 05 (GlcN positive) <http://kokonech.github.io/qualimap/glcn_mice_counts/pGlcn02Report.html>`_

- `Sample 06 (GlcN positive) <http://kokonech.github.io/qualimap/glcn_mice_counts/pGlcn03Report.html>`_

Counts QC HTML reports from human RNA-seq data from study by `Marioni JC et al <http://genome.cshlp.org/content/18/9/1509.abstract>`_ (kidney.counts, liver.counts): 

- `Global report <http://kokonech.github.io/qualimap/marioni_counts/GlobalReport.html>`_ 

- `Comparison of conditions <http://kokonech.github.io/qualimap/marioni_counts/ComparisonReport.html>`_

- `Sample 01 (Kidney) <http://kokonech.github.io/qualimap/marioni_counts/KidneyReport.html>`_

- `Sample 02 (Liver) <http://kokonech.github.io/qualimap/marioni_counts/LiverReport.html>`_



Clustering
**********

Analysis of MeDIP-seq data: `QualiMap HTML report <http://qualimap.bioinfo.cipf.es/samples/clustering_result/qualimapReport.html>`_.




