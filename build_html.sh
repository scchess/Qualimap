#/bin/bash

export QUALIMAP_HTML_DOCS=doc_html

mkdir $QUALIMAP_HTML_DOCS

make -C doc html
cp -vr doc/_build/html/* $QUALIMAP_HTML_DOCS

zip -r doc.zip $QUALIMAP_HTML_DOCS

#cleanup
rm -rf $QUALIMAP_HTML_DOCS




