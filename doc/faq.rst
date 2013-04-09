.. _faq:


Frequently Asked Questions
==========================


General
-------
.. _heapsize:

**Q**: *How to increase maximum Java heap memory size?*

**A**: The Qualimap launching script allows to set desired memory size using special command line argument :samp:`--java-mem-size`. Here are some usage examples:

    :samp:`qualimap --java-mem-size=1200M`

    :samp:`qualimap bamqc -bam very_large_alignment.bam --java-mem-size=4G`

Note that there should be **no whitespace** between argument and its value.     

Alternatively one can change default memory size parameter  by modifying the following line in the launching script:

    JAVA_MEM_DEFAULT_SIZE="1200M"
    

Also one can override this parameter by setting environment variable $JAVA_OPTS.


|

**Q**: *Does Qualimap run on MS Windows?*

**A**: Qualimap can be launched on Windows using script :file:`qualimap.bat`. However, officially we do not support MS Windows.   

|

**Q**: *I always get a message "Out of Memory". What should I do?*

**A**: You can try decreasing the number of reads in chunk or increasing :ref:`maximum Java heap memory size <heapsize>`.  

|

Command line
------------
.. _x11problem:

**Q**: *I launch Qualimap command-line tool on my big and powerful Linux server. However it doesn't finish properly and outputs some strange message like*:

| **Exception in thread "main" java.lang.InternalError: Can't connect to X11**
| **window server using 'foo:42.0' as the value of the DISPLAY variable.**

*What is going on?*

**A**: Java virtual machine uses **DISPLAY** environment variable to detect if the X11 system is available. Sometimes this variable is set incorrectly by the operating system or some applications. To make Qualimap work simply unset this variable:
    :samp:`unset DISPLAY`
or like this:
    :samp:`export DISPLAY=:0`

|

Performance
-----------


**Q**: *Does Qualimap make use of multicore systems to improve computation speed?*

**A**: Yes, Qualimap uses threads to perform BAM QC analysis.

In short, reads are processed in chunks and each chunk is analyzed in parallel.

Below you can find a schema, depicting the applied algorithm.


.. image:: images/parallel.png
    :width: 450pt
    :align: center

Here each block denotes a certain algorithm step. The analysis starts dividing the reference genome into windows. The first window is set to be the current one. Then the analysis continues processing BAM records belonging to the current window.

When all the reads belonging to the current window are processed, the window is finalized in a newly created thread. 

The analysis is finished when all windows are processed.

|

**Q**: *What is the scalability of QualiMap? Can it run on a cluster?*

**A**: Currently qualimap is designed to run in a single multicore machine. In the future we plan to support cluster and computational cloud execution for BAM QC.

|

**Q**: *I have a powerful computer with a lot of memory. Can I make Qualimap run faster?*

**A**: Sure, just increase your :ref:`maximum JAVA heap size <heapsize>`. 

|


