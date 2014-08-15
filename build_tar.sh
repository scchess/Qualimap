#/bin/bash

export QUALIMAP_DIR=qualimap-build-`date +%d-%m-%y`

mvn install
mkdir $QUALIMAP_DIR
cp -vR target/install/* $QUALIMAP_DIR

make -C doc latexpdf 
cp -v doc/_build/latex/QualimapManual.pdf $QUALIMAP_DIR

tar -cf $QUALIMAP_DIR.tar $QUALIMAP_DIR 
gzip -f $QUALIMAP_DIR.tar
#scp $QUALIMAP_DIR.tar.gz genome:/data/qualimapLatestBuild/

#cleanup
rm -rf $QUALIMAP_DIR




