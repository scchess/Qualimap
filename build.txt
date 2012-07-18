Building Qualimap from source code
----------------------------------

1) Prepare environment

Install jdk-6 (both Sun and OpenJDK should work) and maven2

Download picard-tools v1.70 from http://sourceforge.net/projects/picard/files/

Add required picard-1.70.jar and sam-1.70.jar to maven repo:

mvn install:install-file -DgroupId=net.sf.picard -DartifactId=picard -Dversion=1.70 -Dpackaging=jar -Dfile=/path/to/picard-tools-1.70/picard-1.70.jar

mvn install:install-file -DgroupId=net.sf.samtools -DartifactId=samtools -Dversion=1.70 -Dpackaging=jar -Dfile=/path/to/picard-tools-1.70/sam-1.70.jar

2) Build Qualimap

Use maven2 command:

mvn install

Upon first build all dependencies will be downloaded to the repo, this may require some time.

3) Run Qualimap

The built jar can be found in target/install subdir of sources folder.


4) Troubleshouting

If you have problems building Qualimap, please refer to google-groups:
http://groups.google.com/group/qualimap

