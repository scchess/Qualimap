package org.bioinfo.ngs.qc.qualimap;

import java.io.File;

/**
 * Created by kokonech
 * Date: 6/6/12
 * Time: 11:50 AM
 */

/**
 * This class provides common environment for running Qualimap tests.
 * It can be used to access Qualimap test directory and also
 * replace special variables (such as $TEST_DIR for example) in test
 * configuration files.
 */
public class Environment {

    String qualimapTestDir;

    final static String TEST_DIR = "$TEST_DIR";

    void initQualimapTestDir() {

        qualimapTestDir =  System.getenv("QUALIMAP_TEST_DIR");

        if (qualimapTestDir == null) {
            System.out.println("QUALIMAP_TEST_DIR environment variable is not set. Setting current dir..");
            qualimapTestDir = new File("").getAbsolutePath() + File.separator + "test";
        }
    }


    public Environment() {
        initQualimapTestDir();

        qualimapTestDir += File.separator;
        //check that directory is valid
        File testDir = new File(qualimapTestDir);
        if (! (testDir.exists() && testDir.isDirectory()) ) {
            throw new RuntimeException("Failed to init test QualiMap test directory: " + qualimapTestDir );
        }
    }


    public String getQualimapTestDir() {
        return qualimapTestDir;
    }


    public String processAttrValue(String attrVal) {
        return attrVal.replace(TEST_DIR, qualimapTestDir);
}
}
