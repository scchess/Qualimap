.. _command-line:

Command Line Interface
======================

General Description
-------------------

Each analysis type presented in QualiMap GUI is also available as command line tool. The common pattern to launch the tool is the following::

    qualimap <tool_name> <tool_options>

:guilabel:`<tool_name>` is the name of the desired analysis. This could be: :ref:`bamqc<cmdline-bamqc>`, :ref:`countsqc<cmdline-countsqc>`, :ref:`clustering<cmdline-clustering>` or :ref:`counts<cmdline-counts>`. 

:guilabel:`<tool_options>` are specific to each type analysis. If not option is provided for the specific tool a full list of available options will be shown

To show available tools use command:: 

    qualimap --help


.. _cmdline-bamqc:

BAM QC
------



The following command allows to perform BAM QC analysis::

    usage: qualimap bamqc -bam <arg> [-c] [-gd <arg>] [-gff <arg>] [-nr <arg>] [-nt
           <arg>] [-nw <arg>] [-os] [-outdir <arg>] [-outformat <arg>]
     -bam <arg>                     input mapping file
     -c,--paint-chromosome-limits   paint chromosome limits inside charts
     -gd <arg>                      compare with genome distribution (possible
                                    values: HUMAN or MOUSE)
     -gff <arg>                     region file (in GFF/GTF or BED format)
     -hm <arg>                      minimum size for a homopolymer to be considered
                                    in indel analysis (default is 3)
     -nr <arg>                      number of reads in the chunk (default is 500)
     -nt <arg>                      number of threads (default equals the number of cores)
     -nw <arg>                      number of windows (default is 400)
     -os,--outside-stats            compute region outside stats (only with -gff
                                    option)
     -outdir <arg>                  output folder
     -outformat <arg>               output report format (PDF or HTML, default is
                                    HTML)
     -p <arg>                       specify protocol to calculate correct strand
                                    reads (works only with -gff option, possible
                                    values are STRAND-SPECIFIC-FORWARD or
                                    STRAND-SPECIFIC-REVERSE, default is
                                    NON-STRAND-SPECIFIC)


| The only required parameter is :guilabel:`bam` -- the input mapping file.
| If :guilabel:`outdir` is not provided, it will be created automatically in the same folder where BAM file is located.
|
| Detailed explanation of available options can be found :ref:`here<bamqc>`.

Example (data available :ref:`here<bam-samples>`)::

    qualimap bamqc -bam ERR089819.bam -c



.. _cmdline-countsqc:

Counts QC
---------

To perform counts QC analysis (evaluation of RNA-seq data) use the following command::

    usage: qualimap counts -d1 <arg> [-d2 <arg>] [-i <arg>] [-k <arg>] [-n1 <arg>]
           [-n2 <arg>] [-outdir <arg>] [-outformat <arg>] [-s <arg>]
     -d1,--data1 <arg>      first file with counts
     -d2,--data2 <arg>      second file with counts
     -i,--info <arg>        info file
     -k,--threshold <arg>   threshold for the number of counts
     -n1,--name1 <arg>      name for the first sample
     -n2,--name2 <arg>      name for second sample
     -outdir <arg>          output folder
     -outformat <arg>       output report format (PDF or HTML, default is HTML)
     -s,--species <arg>     use default file for the given species [human | mouse]


| Detailed explanation of available options can be found :ref:`here<countsqc>`.

Example (data available :ref:`here<counts-samples>`)::

    qualimap counts -d1 kidney.counts -d2 liver.counts -s human -outdir results


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

    usage: qualimap comp-counts [-algorithm <arg>] -bam <arg> -gtf <arg> [-id <arg>]
           [-out <arg>] [-protocol <arg>] [-type <arg>]
     -algorithm <arg>   uniquely-mapped-reads(default) or proportional
     -b                 calculate 5' and 3' coverage bias
     -bam <arg>         mapping file in BAM format)
     -gtf <arg>         region file in GTF format
     -id <arg>          attribute of the GTF to be used as feature ID. Regions with
                        the same ID will be aggregated as part of the same feature.
                        Default: gene_id.
     -out <arg>         path to output file
     -protocol <arg>    forward-stranded,reverse-stranded or non-strand-specific
     -type <arg>        Value of the third column of the GTF considered for
                        counting. Other types will be ignored. Default: exon


| Detailed explanation of available options can be found :ref:`here<compute-counts>`.

Example (data available :ref:`here<counts-samples>`)::

    qualimap comp-counts -bam kidney.bam -gtf ../annotations/human.64.gtf  -out kidney.counts



