.. _intro:

Introduction
============

What is Qualimap?
-----------------

**Qualimap** is a platform-independent application written in Java and R that provides both a Graphical User Interface (GUI) and a command-line interface to facilitate the quality control of alignment sequencing data. Shortly, Qualimap: 

1. Examines sequencing **alignment data** according to the features of the mapped reads and their **genomic properties**  
2. Povides an **overall view** of the data that helps to  to the **detect biases** in the sequencing and/or mapping of the data and eases **decision-making** for further analysis.

The main features offered by Qualimap are: 

* fast analysis across the reference genome of mapping coverage and nucleotide distribution; 
* easy-to-interpret summary of the main properties of the alignment data; 
* analysis of the reads mapped inside/outside of the regions defined in an annotation reference; 
* computation and analysis of read counts obtained from intersting of read alignments with genomic features;
* analysis of the adequacy of the sequencing depth in RNA-seq experiments;
* support for multi-sample comparison for alignment data and counts data;
* clustering of epigenomic profiles.

Installation
------------

Download the ZIP file from the `Qualimap web page <http://qualimap.org>`_.

Unpack it to desired directory. 

Run Qualimap from this directory using the prebuilt script:

:samp:`./qualimap`

Qualimap was tested on GNU Linux and MacOS.

.. note:: On MS Windows use script :samp:`qualimap.bat` to launch Qualimap. 

Requirements
------------

Qualimap requires:

* `JAVA <http://www.java.com>`_ runtime version 6 or above.
* `R <http://www.r-project.org>`_ enviroment version 3.1 or above.

The JAVA runtime can be downloaded from the `official web-site <http://www.java.com>`_.
There are prebuilt binaries available for many platforms.

R enviroment can be downloaded from `R project web-site <http://www.r-project.org>`_. 

.. note:: In general the installation of R environment is platform-specific and may require additional efforts.

Several Qualimap features are implemented in R, using a number of external packages.

.. note:: If R environment is not available or required R-packages are missing, "Counts QC" and "Clustering" features will be disabled.

Currently Qualimap requires the following R-packages:
 
* optparse (available from `CRAN <http://cran.r-project.org>`_)
* NOISeq, Repitools, Rsamtools, GenomicFeatures, rtracklayer (available from `Bioconductor <http://bioconductor.org>`_) 

One can install these packages `manually <http://cran.r-project.org/doc/manuals/R-admin.html#Installing-packages>`_ or by executing the script found in the installation folder:

:samp:`Rscript scripts/installDependencies.r`

Installing Qualimap on Ubuntu
-----------------------------

This manual is specific for Ubuntu(Debian) Linux distribution, however with slight differences this can be applied for other GNU Linux systems. 

Install JAVA
^^^^^^^^^^^^

It is possible to use openjdk:

:samp:`sudo apt-get install openjdk-6-jre`

Install R
^^^^^^^^^

The R latest version can be installed from public repos.

The repos must be added to the sources file. Open sources.list:

:samp:`sudo gedit /etc/apt/sources.list`

Add the following line:

:samp:`deb http://<my.favorite.cran.mirror>/bin/linux/ubuntu <name.of.your.distribution>/`

List of cran mirrors can be found `here <http://cran.r-project.org/mirrors.html>`_

Here is an example for Ubuntu 10.04 (Lucid):

:samp:`deb http://cran.stat.ucla.edu/bin/linux/ubuntu lucid/`

Then install R:

:samp:`sudo apt-get update`  

:samp:`sudo apt-get install r-base-core`

If you don't have the public key for the mirror add it:

:samp:`gpg --keyserver subkeys.pgp.net --recv-key <required.key>`

:samp:`gpg -a --export <required.key> | sudo apt-key add -`

More details available here:
 
    http://cran.r-project.org/bin/linux/ubuntu/README

Qualimap needs R version 3.1 or above. This can be checked with the following command:

:samp:`Rscript --version`

.. note:: Alternatively it is possible to build R enviroment directly from sources downloaded from r-project.org.

Install required R-packages
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Some packages depend on external libraries, so you might need to install them either:

:samp:`sudo apt-get install libxml2-dev`

:samp:`sudo apt-get install libcurl4-openssl-dev`


You can install required packages manually or use special script from Qualimap installation folder:

:samp:`sudo Rscript $QUALIMAP_HOME/scripts/installDependencies.r`

where :samp:`$QUALIMAP_HOME` is the full path to the Qualimap installation folder.




Citing Qualimap
---------------

If you use Qualimap 2 for your research, please cite the following:

*Okonechnikov, K., Conesa, A., & García-Alcalde, F. (2015). "Qualimap 2: advanced multi-sample quality control for high-throughput sequencing data." Bioinformatics, btv566*

The first version of the tool was described in the following manuscript:

*García-Alcalde, et al. "Qualimap: evaluating next generation sequencing alignment data." Bioinformatics(2012) 28 (20): 2678-2679*



