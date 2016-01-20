#/bin/bash


export QUALIMAP_VERSION=`head pom.xml | grep "<version>" | awk 'BEGIN{FS="<";}{print $2}' | awk 'BEGIN{FS=">";}{print $2}'`
export OUTPUT_DIR=qualimap-v$QUALIMAP_VERSION-src
export TIME=`date +%d-%m-%y`

mkdir $OUTPUT_DIR

mvn license:check -Dyear=2015
rc=$?
if [[ $rc != 0 ]] ; then
    exit $rc
fi

git checkout-index --prefix=$OUTPUT_DIR/ -a

zip -r qualimap_v${QUALIMAP_VERSION}_src.zip $OUTPUT_DIR 

#cleanup
rm -rf $OUTPUT_DIR





