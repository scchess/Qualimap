Building Qualimap from source code
----------------------------------

1) Prepare environment

Install jdk-6 (both Sun and OpenJDK should work) and maven2

Add required picard-1.70.jar and sam-1.70.jar to maven repo:
They can be found in directory "external"
Alternatevly one can download picard-tools v1.70 from http://sourceforge.net/projects/picard/files/

mvn install:install-file -DgroupId=net.sf.picard -DartifactId=picard -Dversion=1.70 -Dpackaging=jar -Dfile=/path/to/picard-tools-1.70/picard-1.70.jar

mvn install:install-file -DgroupId=net.sf.samtools -DartifactId=samtools -Dversion=1.70 -Dpackaging=jar -Dfile=/path/to/picard-tools-1.70/sam-1.70.jar

mvn install:install-file -DgroupId=org.ejml -DartifactId=ejml -Dversion=0.24 -Dpackaging=jar -Dfile=ejml-0.24.jar


2) Build Qualimap

Use maven2 command:

mvn install

Upon first build all dependencies will be downloaded to the repo, this may require some time.

Also special parameter "-o" allows to use local repostiory after downloading the samples once:
mvn install -o

The current online repostiory org.bioinfo is not available due to some reasons, but there is a local version to download available

3) Run Qualimap

The built jar can be found in target/install subdir of sources folder.


4) Develop Qualimap

You can use your favorite Java IDE to work on Qualimap.
However, we recommend (and use) IntelliJ IdeA Community Edition.

5) Troubleshouting

If you have problems building Qualimap, please refer to google-groups:
http://groups.google.com/group/qualimap

