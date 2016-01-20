#/bin/bash

# working dir of this script should be ROOT of the sources

# get qualimap version from pom.xml

export QUALIMAP_VERSION=`head pom.xml | grep "<version>" | awk 'BEGIN{FS="<";}{print $2}' | awk 'BEGIN{FS=">";}{print $2}'`

export QUALIMAP_DIR=qualimap_v$QUALIMAP_VERSION

mvn install
mkdir $QUALIMAP_DIR
cp -vR target/install/* $QUALIMAP_DIR

make -C doc latexpdf 
cp -v doc/_build/latex/QualimapManual.pdf $QUALIMAP_DIR

zip -r qualimap_v${QUALIMAP_VERSION}.zip $QUALIMAP_DIR 
#cleanup
rm -rf $QUALIMAP_DIR





