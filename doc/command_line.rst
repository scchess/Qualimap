.. _command-line:

Command Line Interface
======================

General Description
-------------------

Each analysis type presented in QualiMap GUI is also available as command line tool. The common pattern to launch the tool is the following::

    qualimap <tool_name> <tool_options>

:guilabel:`<tool_name>` is the name of the desired analysis. This could be: :ref:`bamqc<cmdline-bamqc>`, :ref:`rnaseq<cmdline-rnaseqqc>`, :ref:`multi-bamqc <cmdline-multibamqc>`,  :ref:`counts<cmdline-countsqc>`, :ref:`clustering<cmdline-clustering>` or :ref:`comp-counts<cmdline-counts>`. 

:guilabel:`<tool_options>` are specific to each type analysis. If not option is provided for the specific tool a full list of available options will be shown

.. note:: If you are using Qualimap on Unix server without X11 system, make sure that the DISPLAY environment variable is unset. Otherwise this might result in problems when running Qualimap. :ref:`Here<x11problem>` is an instruction how to solve this issue.

To show available tools use command:: 

    qualimap --help

There are certain options that are common to most of the command line tools::

 -outdir <arg>                        Output folder for HTML report and raw
                                       data.
 -outfile <arg>                       Output file for PDF report (default value
                                      is report.pdf).
 -outformat <arg>                     Format of the ouput report (PDF or HTML,
                                       default is HTML).


These options allow to confugre output of Qualimap. 
*-outdir* option sets the output folder for HTML report and raw data::

 qualimap bamqc -bam file.bam -outdir qualimap_results

| If the *-outfile* option is given then the output will be produced in PDF format. In this case *-outdir* option controls only the path to raw data. 
Example::

  qualimap bamqc -bam file.bam -outfile result.pdf

| It is also possible to explictily set output format by using option *-outformat*. In this case report will be saved in the output dir under default name. 
Example::

  qualimap bamqc -bam file.bam -outdir qualimap_results -outformat pdf

| Additionally each tool has its own defaults for output directory name. Check tools' description for details.


.. _cmdline-bamqc:

BAM QC
------


The following command allows to perform BAM QC analysis::

    usage: qualimap bamqc -bam <arg> [-c] [-gd <arg>] [-gff <arg>] [-hm <arg>] [-nr
       <arg>] [-nt <arg>] [-nw <arg>] [-oc <arg>] [-os] [-outdir <arg>]
       [-outfile <arg>] [-outformat <arg>] [-p <arg>]
    -bam <arg>                           Input mapping file in BAM format
    -c,--paint-chromosome-limits         Paint chromosome limits inside charts
    -gd,--genome-gc-distr <arg>          Species to compare with genome GC
                                      distribution. Possible values: HUMAN or
                                      MOUSE.
    -gff,--feature-file <arg>            Feature file with regions of interest in
                                      GFF/GTF or BED format
    -hm <arg>                            Minimum size for a homopolymer to be
                                      considered in indel analysis (default is
                                      3)
    -nr <arg>                            Number of reads analyzed in a chunk
                                      (default is 1000)
    -nt <arg>                            Number of threads (default is 8)
    -nw <arg>                            Number of windows (default is 400)
    -oc,--output-genome-coverage <arg>   File to save per base non-zero coverage.
                                      Warning: large files are  expected for large
                                      genomes
    -os,--outside-stats                  Report information for the regions outside
                                      those defined by feature-file  (ignored
                                      when -gff option is not set)
    -outdir <arg>                        Output folder for HTML report and raw
                                      data.
    -outfile <arg>                       Output file for PDF report (default value
                                      is report.pdf).
    -outformat <arg>                     Format of the ouput report (PDF or HTML,
                                      default is HTML).
    -p,--sequencing-protocol <arg>       Sequencing library protocol:
                                      strand-specific-forward,
                                      strand-specific-reverse or
                                      non-strand-specific (default)



| The only required parameter is :guilabel:`bam` -- the input mapping file.
| If :guilabel:`outdir` is not provided, it will be created automatically in the same folder where BAM file is located.
|
| Detailed explanation of available options can be found :ref:`here<bamqc>`.

Example (data available :ref:`here<bam-samples>`)::

    qualimap bamqc -bam ERR089819.bam -c



.. _cmdline-rnaseqqc:

RNA-seq QC
----------

To perform RNA-seq QC analysis use the following command::

 usage: qualimap rnaseq [-a <arg>] -bam <arg> -gtf <arg> [-oc <arg>] [-outdir
       <arg>] [-outfile <arg>] [-outformat <arg>] [-p <arg>]
 -a,--algorithm <arg>             Counting algorithm:
                                  uniquely-mapped-reads(default) or
                                  proportional.
 -bam <arg>                       Input mapping file in BAM format.
 -gtf <arg>                       Annotations file in Ensembl GTF format.
 -oc <arg>                        Path to output computed counts.
 -outdir <arg>                    Output folder for HTML report and raw data.
 -outfile <arg>                   Output file for PDF report (default value is
                                  report.pdf).
 -outformat <arg>                 Format of the ouput report (PDF or HTML,
                                  default is HTML).
 -p,--sequencing-protocol <arg>   Sequencing library protocol:
                                  strand-specific-forward,
                                  strand-specific-reverse or non-strand-specific
                                  (default)


| The required parameteres for this type of analysis are the spliced-alignment file in BAM format and annotations in GTF format.

| Detailed explanation of available options can be found :ref:`here<rnaseqqc>`.

Example (data available :ref:`here<annotation-files>`)::

    qualimap rnaseq -bam kidney.bam -gtf human.64.gtf -outdir rnaseq_qc_results



.. _cmdline-multibamqc:

Multi-sample BAM QC
-------------------

To perform multi-sample BAM QC use the following command::

 usage: qualimap multi-bamqc [-c] -d <arg> [-gff <arg>] [-hm <arg>] [-nr <arg>]
       [-nw <arg>] [-outdir <arg>] [-outfile <arg>] [-outformat <arg>] [-r]
 -c,--paint-chromosome-limits   Only for -r mode. Paint chromosome limits inside
                                charts
 -d,--data <arg>                File describing the input data. Format of the
                                file is a 2-column tab-delimited table.
                                Column 1: sample name
                                Column 2: either path to the BAM QC result or
                                path to BAM file (-r mode)
 -gff,--feature-file <arg>      Only for -r mode. Feature file with regions of
                                interest in GFF/GTF or BED format
 -hm <arg>                      Only for -r mode. Minimum size for a homopolymer
                                to be considered in indel analysis (default is
                                3)
 -nr <arg>                      Only for -r mode. Number of reads analyzed in a
                                chunk (default is 1000)
 -nw <arg>                      Only for -r mode. Number of windows (default is
                                400)
 -outdir <arg>                  Output folder for HTML report and raw data.
 -outfile <arg>                 Output file for PDF report (default value is
                                report.pdf).
 -outformat <arg>               Format of the ouput report (PDF or HTML, default
                                is HTML).
 -r,--run-bamqc                 Raw BAM files are provided as input. If this
                                option is activated BAM QC process first will be
                                run for each sample, then multi-sample analysis
                                will be performed.

 
| The main argument for this command is the configuration file describing input data (-d). This has to be a 2-column tab-delimted file. The first column should contain the sample name and the second column should contain either path to the results of BAM QC analysis or path to the BAM file (if -r mode is activated). The path for the data could be absolute or relative to the location of the configuration file.

| Detailed explanation of the analysis can be found here :ref:`here<multibamqc>`.

Example (data available :ref:`here<multibamqc-samples>`)::
    
    unzip gh2ax_chip_seq.zip
    cd gh2ax_chip_seq.txt
    qualimap multi-bamqc -i gh2ax_chip_seq.txt -outdir gh2ax_multibamqc



.. _cmdline-countsqc:

Counts QC
---------

To perform counts QC analysis use the following command::

 usage: qualimap counts [-c] -d <arg> [-i <arg>] [-k <arg>] [-outdir <arg>]
       [-outfile <arg>] [-outformat <arg>] [-R <arg>] [-s <arg>]
 -c,--compare             Perform comparison of conditions. Currently 2 maximum
                          is possible.
 -d,--data <arg>          File describing the input data. Format of the file is
                          a 4 column tab-delimited table.
                          Column 1: sample name
                          Column 2: condition of the sample
                          Column 3: path to the counts data for the sample
                          Column 4: index of the column with counts
 -i,--info <arg>          Path to info file containing genes GC-content, length
                          and type.
 -k,--threshold <arg>     Threshold for the number of counts
 -outdir <arg>            Output folder for HTML report and raw data.
 -outfile <arg>           Output file for PDF report (default value is
                          report.pdf).
 -outformat <arg>         Format of the ouput report (PDF or HTML, default is
                          HTML).
 -R,--rscriptpath <arg>   Path to Rscript executable (by default it is assumed
                          to be available from system $PATH)
 -s,--species <arg>       Use built-in info file for the given species: HUMAN or
                          MOUSE.

| The main argument for this command is the configuration file describing the input samples (-d). This has to be a 4-column tab-delimited file. The first column should contain the name of the sample, the second - name of the biological condition (e.g treated or untreated), the third - path to the file containing counts data for the sample and the fourth - the index of the column in the data file which contains counts. This is useful when counts for all samples are contained in the one file, but in different columns.

| Detailed explanation of the analysis can be found :ref:`here<countsqc>`.

Example. Note: requires counts file `mouse_counts_ensembl.txt <http://kokonech.github.io/qualimap/samples/mouse_counts_ensembl.txt>`_ (data available :ref:`here<counts-samples>`)::

    qualimap counts -d GlcN_countsqc_input.txt -c -s mouse -outdir glcn_mice_counts


.. _cmdline-clustering:

Clustering
----------

To perform clustering of epigenomic signals use the following command::

    usage: qualimap clustering [-b <arg>] [-c <arg>] -control <arg> [-expr <arg>]
           [-f <arg>] [-l <arg>] [-name <arg>] [-outdir <arg>] [-outformat <arg>]
           [-r <arg>] -regions <arg> -sample <arg> [-viz <arg>]
     -b,--bin-size <arg>          size of the bin (default is 100)
     -c,--clusters <arg>          comma-separated list of cluster sizes
     -control <arg>               comma-separated list of control BAM files
     -expr <arg>                  name of the experiment
     -f,--fragment-length <arg>   smoothing length of a fragment
     -l <arg>                     upstream offset (default is 2000)
     -name <arg>                  comma-separated names of the replicates
     -outdir <arg>                output folder
     -outformat <arg>             output report format (PDF or HTML, default is
                                  HTML)
     -r <arg>                     downstream offset (default is 500)
     -regions <arg>               path to regions file
     -sample <arg>                comma-separated list of sample BAM files
     -viz <arg>                   visualization type: heatmap or line


| Detailed explanation of available options can be found :ref:`here<clustering>`.

Example (data available :ref:`here<clustering-samples>`)::

    qualimap clustering -sample clustering/hmeDIP.bam -control clustering/input.bam -regions annotations/transcripts.human.64.bed -outdir clustering_result


.. _cmdline-counts:

Compute counts
--------------

To compute counts from mapping data use the following command::

 usage: qualimap comp-counts [-a <arg>] -bam <arg> -gtf <arg> [-id <arg>] [-out
       <arg>] [-p <arg>] [-pe] [-s <arg>] [-type <arg>]
 -a,--algorithm <arg>             Counting algorithm:
                                  uniquely-mapped-reads(default) or proportional
 -bam <arg>                       Mapping file in BAM format
 -gtf <arg>                       Region file in GTF, GFF or BED format. If GTF
                                  format is provided, counting is based on
                                  attributes, otherwise based on feature name
 -id <arg>                        GTF-specific. Attribute of the GTF to be used
                                  as feature ID. Regions with the same ID will
                                  be aggregated as part of the same feature.
                                  Default: gene_id.
 -out <arg>                       Path to output file
 -p,--sequencing-protocol <arg>   Sequencing library protocol:
                                  strand-specific-forward,
                                  strand-specific-reverse or non-strand-specific
                                  (default)
 -pe,--paired                     Setting this flag for paired-end experiments
                                  will result in counting fragments instead of
                                  reads
 -s,--sorted <arg>                This flag indicates that the input file is
                                  already sorted by name. If not set, additional
                                  sorting by name will be performed. Only
                                  required for paired-end analysis.
 -type <arg>                      GTF-specific. Value of the third column of the
                                  GTF considered for counting. Other types will
                                  be ignored. Default: exon

| Detailed explanation of available options can be found :ref:`here<compute-counts>`.

Example (data available :ref:`here<counts-samples>`)::

    qualimap comp-counts -bam kidney.bam -gtf ../annotations/human.64.gtf  -out kidney.counts



