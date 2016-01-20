#/bin/bash

# clean up
rm qualimap_v*.zip
rm doc.zip

./build_zip.sh
./build_src_zip.sh
./build_html.sh

#upload everything

#scp -v qualimap_v*.zip doc.zip ssh.cipf.es:/tmp/release_1.0


