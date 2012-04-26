.. _faq:


Frequently Asked Questions
==========================


General
-------

.. _heapsize:

**Q**: *How to increase maximum Java heap memory size?*

**A**: Open the Qualimap launching script in any text editor. Find the following lines: 

    java_options="-Xms32m -Xmx1G -XX:MaxPermSize=1024m"

Set the -Xmx parameter as desired parameter.

Also you can override this parameter by setting environment variable $JAVA_OPTS.

|

**Q**: *Does Qualimap run on MS Windows?*

**A**: Qualimap can be launched on Windows using script :file:`qualimap.bat`. However, officially we do not support MS Windows.   

|

**Q**: *I always get a message "Out of Memory". What shoud I do?*

**A**: You can try decreasing the number of reads in chunk or increasing :ref:`maximum Java heap memory size <heapsize>`.  

|

Installation
------------

**Q**: *How to install the latest possible R environment on Ubuntu?*

**A**: The latest R can be installed from official repos.

The repos must be added to the sources file. Open sources.list:

:samp:`sudo gedit /etc/apt/sources.list`

Add the following line:

:samp:`deb http://<my.favorite.cran.mirror>/bin/linux/ubuntu <name.of.your.distribution>/`

List of cran mirrors can be found `here <http://cran.r-project.org/mirrors.html>`_

Here is an example for Ubuntu 10.04 (Lucid):

:samp:`deb http://cran.stat.ucla.edu/bin/linux/ubuntu lucid/`

Then Install R:

:samp:`sudo apt-get update`  

:samp:`sudo apt-get install r-base-core`

If you don't have the public key for the mirror add it:

:samp:`gpg --keyserver subkeys.pgp.net --recv-key <required.key>`

:samp:`gpg -a --export <required.key> | sudo apt-key add -`

More details available here:
 
    http://cran.r-project.org/bin/linux/ubuntu/README

|


Performance
-----------


**Q**: *Does Qualimap make use of multicore systems to improve computation speed?*

**A**: Yes, Qualimap uses threads to perform BAM QC analysis.

In short, reads are processed in chunks and each chunk is analyzed independtly from others.

Below you can find a schema, depicting the applied algorithm.


.. image:: images/parallel.png
    :width: 450pt
    :align: center

Here each block denotes a certain algorithm step. Analysis starts with dividing reference genome into windows. The first window is set to be the current. Then analysis continues with processing BAM records belongning to current window.  

When all reads belonging to current window (region of genome) are processed, the window is finalized. This task is also performed in a separate thread. 

Analysis is finished when all windows are processed.


**Q**: *I have a powerful computer with a lot of memory. Can I make Qualimap run faster?*

**A**: Sure, just increase your :ref:`maximum JAVA heap size <heapsize>`. 

|


Working with annotation files
-----------------------------

**Q**: *I use an annotation file and the output I obtain does not make sense*

**A**: There maybe several reasons:

  1. sadfdsa
  2. sdfgds
