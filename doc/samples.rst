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

Annotations
***********

.. _annotation-files:

- `human.64.gtf <http://qualimap.bioinfo.cipf.es/samples/annotations/human.64.gtf>`_ 
    Human genome annotations from Ensembl database (v. 64).
- `transcripts.human.64.bed <http://qualimap.bioinfo.cipf.es/samples/annotations/transcripts.human.64.bed>`_
    Human transcripts in BED format from Ensembl database (v. 64).

.. `Plasmodium-falciparum-3D7.gff <http://qualimap.bioinfo.cipf.es/samples/annotations/Plasmodium-falciparum-3D7.gff>`_ 
  Gene Annotations of Plasmodium falciparum 3D7 clone , from `Wellcome Trust Sanger Institue <http://www.sanger.ac.uk/resources/downloads/protozoa/plasmodium-falciparum.html>`_.



.. _counts-samples:

Counts
******

Mice counts data from a study investigating effects of D-Glucosamine (`GSE54853 <http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=GSE54853>`_):

- `mouse_counts_ensembl.txt <http://kokonech.github.io/qualimap/samples/mouse_counts_ensembl.txt>`_
    Counts table

- `countsqc_input.txt <http://kokonech.github.io/qualimap/samples/countsqc_input.txt>`_
    Counts QC command line tool input configuration

Human RNA-seq data from the paper of `Marioni JC et al <http://genome.cshlp.org/content/18/9/1509.abstract>`_.

- Counts:

  `kidney.counts <http://qualimap.bioinfo.cipf.es/samples/counts/kidney.counts>`_ and `liver.counts <http://qualimap.bioinfo.cipf.es/samples/counts/liver.counts>`_

- BAM files used to produce the counts:

  `kidney.bam <http://qualimap.bioinfo.cipf.es/samples/counts/kidney.bam>`_ and `liver.bam <http://qualimap.bioinfo.cipf.es/samples/counts/liver.bam>`_

- Genes Biotypes:

  `human.64.genes.biotypes.txt <http://qualimap.bioinfo.cipf.es/samples/counts/human.64.genes.biotypes.txt>`_
  

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

Analysis of the WG-seq data (ERR089819.bam): `QualiMap HTML report <http://qualimap.bioinfo.cipf.es/samples/ERR089819_result/qualimapReport.html>`_.

Analysis of the WG-seq data (HG00096.chrom20.bam): `QualiMap HTML report <http://qualimap.bioinfo.cipf.es/samples/HG00096.chrom20_result/qualimapReport.html>`_.


RNA-seq QC
**********



Multisample BAM QC
******************




Counts QC
*********

.. _counts-example-output:

Counts QC HTML report computed from an RNA-seq experiment analyzing influence of D-Glucosamine on mice. The analysis was performed for 6 samples in 2 conditions (GlcN positive and negative): 

- `Global report <http://kokonech.github.io/qualimap/glcn_mice_counts/GlobalReport.html>`_ 

- `Comparison of conditions <http://kokonech.github.io/qualimap/glcn_mice_counts/ComparisonReport.html>`_

- `Sample 01 (GlcN negative) <http://kokonech.github.io/qualimap/glcn_mice_counts/nGlcn01Report.html>`_

- `Sample 02 (GlcN negative) <http://kokonech.github.io/qualimap/glcn_mice_counts/nGlcn02Report.html>`_

- `Sample 03 (GlcN negative) <http://kokonech.github.io/qualimap/glcn_mice_counts/nGlcn03Report.html>`_

- `Sample 04 (GlcN positive) <http://kokonech.github.io/qualimap/glcn_mice_counts/pGlcn01Report.html>`_

- `Sample 05 (GlcN positive) <http://kokonech.github.io/qualimap/glcn_mice_counts/pGlcn02Report.html>`_

- `Sample 06 (GlcN positive) <http://kokonech.github.io/qualimap/glcn_mice_counts/pGlcn03Report.html>`_


Clustering
**********

Analysis of MeDIP-seq data: `QualiMap HTML report <http://qualimap.bioinfo.cipf.es/samples/clustering_result/qualimapReport.html>`_.




