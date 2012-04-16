.. _intro:

Introduction
============

What is Qualimap?
-----------------

**Qualimap** is a platform-independent application written in Java and R that provides both a Graphical User Interface (GUI) and a command-line interface to facilitate the quality control of alignment sequecing data. Shortly, Qualimap: i) examines sequencing **alignment data** according to the features of the mapped reads and their **genomic properties**; and ii) 
provides an **overall view** of the data that helps to  to the **detect biases** in the sequencing and/or mapping of the data and eases **decision-making** for further analysis.

The main features offered by Qualimap are: fast analysis across the reference genome of mapping coverage and nucleotide distribution; easy-to-interpret summary of the main properties of the alignment data; analysis of the reads mapped inside/outside of the regions defined in an annotation reference; analysis of the adequacy of the sequencing depth in RNA-seq experiments and clustering of epigenomic profiles.

Installation
------------

Download the ZIP file from the `Qualimap web page <http://qualimap.org>`_.

Unpack it to desired directory. 

Run Qualimap from this directory using the prebuilt script:

:samp:`./qualimap`

Qualimap was tested on GNU Linux, MacOS and MS Windows. !Revise Windows!

.. note:: On MS Windows use script :samp:`qualimap.bat` to launch Qualimap.

Requirements
------------

Qualimap requires

* `JAVA <http://www.java.com>`_ runtime version 6 or above.
* `R <http://www.r-project.org>`_ enviroment version 2.14 or above.

R packages:

* optparse (available from `CRAN <http://cran.r-project.org>`_)
* Repitools, Rsamtools, GenomicFeatures, rtracklayer (available from `Bioconductor <http://bioconductor.org>`_) 

The JAVA runtime can be downloaded from the `official web-site <http://www.java.com>`_.
There are prebuilt binaries available for many platforms.

R enviroment can be downloaded from `R project web-site <http://www.r-project.org>`_. 

Several Qualimap features are implemented in R, using a number of external packages.

.. note:: If R enviroment is not availble, "Epigenetics" and "RNA-seq" features will be disabled.

Currently Qualimap requires the following R-packages:
 
* optparse (available from `CRAN <http://cran.r-project.org>`_)
* Repitools, Rsamtools, GenomicFeatures, rtracklayer (available from `Bioconductor <http://bioconductor.org>`_) 

One can install these packages `manually <www.howtoinstallpackages.com>`_ or use the script from Qualimap distribution.

Provided R is properly installed, the R packages can be installed by executing the script found in the installation folder:

:samp:`Rscript scripts/installDependencies.r`

.. note:: In general the installation of R packages is platform-specific and may required additional effort.

Installing Qualimap on Ubuntu
-----------------------------

This manual is specific for Ubuntu(Debian) Linux distributive !distributive?!, however with slight differences this can be applied for others Unix systems. 

Install JAVA
^^^^^^^^^^^^

It is possible to use openjdk:

:samp:`sudo apt-get install openjdk-6-jre`

Install R
^^^^^^^^^
!Modify! 
The R latest version can be installede from public repos.

However, the repos must be added to the sources. Open sources.list:

:samp:`sudo gedit /etc/apt/sources.list`

Add the following lines:

:samp:`deb http://<my.favorite.cran.mirror>/bin/linux/ubuntu <name.of.your.distribution>/`
 
Then install R:

:samp:`sudo apt-get update`  

:samp:`sudo apt-get install r-base-core`
 
If you don't have the public key for the mirror add it:

:samp:`gpg --keyserver subkeys.pgp.net --recv-key <required.key>`

:samp:`gpg -a --export <required.key> | sudo apt-key add -`

More details available here:
 
   https://stat.ethz.ch/pipermail/r-help/2009-February/187644.html

   http://cran.r-project.org/bin/linux/ubuntu/README

.. note:: Alternatively it is possible to build R enviroment directly from sources downloaded from r-project.org.

Install required R-packages
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Use special script from Qualimap pacage:

:samp:`Rscript $QUALIMAP_HOME/scripts/installDependencies.r`

where :samp:`$QUALIMAP_HOME` is the full path to the Qualimap installation folder.

Citing Qualimap
---------------

If you use Qualimap for your research, please cite PAPER

