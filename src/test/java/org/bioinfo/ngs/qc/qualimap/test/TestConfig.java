/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by kokonech
 * Date: 12/12/11
 * Time: 4:19 PM
 */
public class TestConfig {


    HashMap<String,String> attributes;
    String path;

    private final String BAM_FILE_ATTR = "bamfile";
    private final String RESULTS_FILE_ATTR = "result";
    private final String OUTSIDE_RESULTS_ATTR = "outside_result";
    private final String REGIONS_FILE_ATTR = "regions";
    private final String OUTSIDE_STATS_ATTR = "compute_outside_stats";

    private HashMap<String,String> parseConfigFile(BufferedReader fileReader, Environment testEnv) throws IOException {
        HashMap<String,String> attrMap = new HashMap<String, String>();
        String s;
        while ((s = fileReader.readLine()) != null) {
            if (s.isEmpty() || s.startsWith("#")) {
                continue;
            }
            String[] attrs = s.split("=");
            String attrName = attrs[0].trim();
            String attrValue = testEnv.processAttrValue(attrs[1].trim());
            attrMap.put( attrName , attrValue);
        }

        attrMap.put(null,"");

        return attrMap;
    }

    public TestConfig(String pathToConfig, Environment testEnv ) throws IOException {
        String qualimapTestDir = testEnv.getQualimapTestDir();
        path = qualimapTestDir + File.separator + pathToConfig;
        BufferedReader fileReader = new BufferedReader( new FileReader(path));
        attributes = parseConfigFile(fileReader,testEnv);
    }

    public String getPath() {
        return path;
    }

    public String getSpecificAttribute(String attrName) { return attributes.get(attrName); }

    public String getOutsideResultsPath() { return attributes.get(OUTSIDE_RESULTS_ATTR); }
    public String getPathToBamFile() { return attributes.get(BAM_FILE_ATTR); }
    public String getResultsPath() { return attributes.get(RESULTS_FILE_ATTR); }
    public String getPathToRegionFile() { return attributes.get(REGIONS_FILE_ATTR); }
    public boolean  getComputeOutsideStats()
    {
        return attributes.get(OUTSIDE_STATS_ATTR) != null;
    }


}
