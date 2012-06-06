package org.bioinfo.ngs.qc.qualimap;

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
        String path = qualimapTestDir + File.separator + pathToConfig;
        BufferedReader fileReader = new BufferedReader( new FileReader(path));
        attributes = parseConfigFile(fileReader,testEnv);
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
