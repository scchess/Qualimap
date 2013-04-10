/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
 * http://qualimap.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.bioinfo.ngs.qc.qualimap.test;

import java.io.File;

/**
 * Created by kokonech
 * Date: 6/6/12
 * Time: 11:50 AM
 */

/**
 * This class provides common environment for running Qualimap tests.
 * It can be used to access Qualimap test directory and also
 * process special variables (such as $COMMON_DATA_DIR for example) in test
 * configuration files.
 */
public class Environment {

    String qualimapTestDir;
    String qualimapDataDir;

    final static String TEST_ROOT_DIR = "$TEST_ROOT_DIR";
    final static String COMMON_DATA_DIR = "$COMMON_DATA_DIR";

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

        qualimapDataDir = qualimapTestDir + File.separator + "common_data";
    }


    public String getQualimapTestDir() {
        return qualimapTestDir;
    }


    public String processAttrValue(String attrVal) {
        return attrVal.replace(TEST_ROOT_DIR, qualimapTestDir)
                .replace(COMMON_DATA_DIR, qualimapDataDir);
}
}
