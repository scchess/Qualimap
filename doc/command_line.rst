.. _command-line:

Command Line Interface
======================

General Description
-------------------

Each analysis type presented in QualiMap GUI is also available as command line tool. The common pattern to launch the tool is the following::

    qualimap <tool_name> <tool_options>

:guilabel:`<tool_name>` is the name of the desired analysis. This could be: :ref:`genomic<cmdline-genomic>`, :ref:`rna-seq<cmdline-rnaseq>`, :ref:`epigenomic<cmdline-epigenomic>` or :ref:`counts<cmdline-counts>`. 

:guilabel:`<tool_options>` are specific to each type analysis. If not option is provided for the specific tool a full list of available options will be shown

To show available tools use command:: 

    qualimap --help


.. _cmdline-genomic:

Genomic
-------

The following command allows to perform genomic analysis::

    qualimap genomic -bam <arg> [-c] [-gff <arg>] [-home <arg>] [-nr <arg>] [-nt <arg>] [-nw <arg>] [-o] [-outdir <arg>] [-outformat <arg>]
    -bam <arg>                     input mapping file
    -c,--paint-chromosome-limits   paint chromosome limits inside charts
    -gff <arg>                     region file (gff format)
    -home <arg>                    home folder of Qualimap
    -nr <arg>                      number of reads in the bunch (advanced)
    -nt <arg>                      number of threads (advanced)
    -nw <arg>                      number of windows (advanced)
    -o,--outside-stats             compute region outside stats (only with -gff option)
    -outdir <arg>                  output folder
    -outformat <arg>               output report format (PDF or HTML, default is HTML)
    


|
| The only required parameter is :guilabel:`bam` -- the input mapping file.
| If :guilabel:`outdir` is not provided, it will be created automatically in the same folder where BAM file is located.
|
| Detailed explanation of available options can be found :ref:`here<genomic>`.

Example::

    ./qualimap genomic -bam ~/sample_data/pl.bam -gff ~/sample_data/pl_anns.gff --outside-stats


.. _cmdline-rnaseq:

RNA-seq
-------

To perform RNA-seq analysis use the following command::

    qualimap rna-seq -d1 <arg> [-d2 <arg>] [-home <arg>] [-i <arg>] [-k <arg>] [-n1 <arg>] [-n2 <arg>]
    [-outdir <arg>] [-outformat <arg>] [-s <arg>]
    -d1,--data1 <arg>      first file with counts
    -d2,--data2 <arg>      second file with counts
    -home <arg>            home folder of Qualimap
    -i,--info <arg>        info file
    -k,--threshold <arg>   threshold for the number of counts
    -n1,--name1 <arg>      name for the first sample
    -n2,--name2 <arg>      name for second sample
    -outdir <arg>          output folder
    -outformat <arg>       output report format (PDF or HTML, default is HTML)
    -s,--species <arg>     use default file for the given species [human | mouse]

|
| Detailed explanation of available options can be found :ref:`here<rna-seq>`.

Example::

    ./qualimap rna-seq -d1 ~/sample_data/counts-kidney.txt -d2 ~/sample_data/counts-liver.txt -s human -outdir ~/sample_data/result


.. _cmdline-epigenomic:

Epigenomic
----------

To perform epigenomic analysis use the following command::

    qualimap epigenomic [-b <arg>] [-c <arg>] -control <arg> [-expr <arg>] [-f <arg>] [-home <arg>]
    [-l <arg>] [-name <arg>] [-outdir <arg>] [-outformat <arg>] [-r <arg>] -regions <arg> -sample <arg> [-viz <arg>]
    -b,--bin-size <arg>          size of the bin (default is 100)
    -c,--clusters <arg>          comma-separated list of cluster sizes
    -control <arg>               path to control BAM file
    -expr <arg>                  name of the experiment
    -f,--fragment-length <arg>   smoothing length of a fragment
    -home <arg>                  home folder of Qualimap
    -l <arg>                     left offset (default is 2000)
    -name <arg>                  name of the replicate
    -outdir <arg>                output folder
    -outformat <arg>             output report format (PDF or HTML, default is HTML)
    -r <arg>                     right offset (default is 500)
    -regions <arg>               path to regions file
    -sample <arg>                path to sample BAM file
    -viz <arg>                   visualization type: heatmap or line

|
| Detailed explanation of available options can be found :ref:`here<epigenomic>`.

Example::

    ./qualimap epigenomic -sample ~/sample_data/24h-i-medip.bam -control ~/sample_data/24h-i-control.bam -regions ~/sample_data/CpGislands.bed -outdir ~/sample_data/result

.. _cmdline-counts:

Compute counts
--------------

To compute counts from mapping data use the following command::

    qualimap counts -bam <arg> [-f <arg>] -gff <arg> [-home <arg>] [-p <arg>]
    -bam <arg>            mapping file in BAM format)
    -f,--output <arg>     path to output file
    -gff <arg>            region file in GFF format
    -home <arg>           home folder of Qualimap
    -p,--protocol <arg>   forward-stranded,reverse-stranded or non-strand-specific

|
| Detailed explanation of available options can be found :ref:`here<compute-counts>`.

Example::

    ./qualimap counts -bam ~/sample_data/pl.bam -gff ~/sample_data/pl_anns.bam 




