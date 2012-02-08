package org.bioinfo.ngs.qc.qualimap;

import java.io.BufferedReader;
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

    private HashMap<String,String> parseConfigFile(BufferedReader fileReader) throws IOException {
        HashMap<String,String> attrMap = new HashMap<String, String>();
        String s;
        while ((s = fileReader.readLine()) != null) {
            if (s.isEmpty() || s.startsWith("#")) {
                continue;
            }
            String[] attrs = s.split("=");
            attrMap.put(attrs[0].trim(),attrs[1].trim());
        }

        attrMap.put(null,"");

        return attrMap;
    }

    public TestConfig(String pathToConfig) throws IOException {
        BufferedReader fileReader = new BufferedReader( new FileReader(pathToConfig));
        attributes = parseConfigFile(fileReader);
    }

    public String getSpecificAttribute(String attrName) { return attributes.get(attrName); }

    public String getOutsideResultsPath() { return attributes.get(OUTSIDE_RESULTS_ATTR); }
    public String getPathToBamFile() { return attributes.get(BAM_FILE_ATTR); }
    public String getPathToValiationOptions() { return attributes.get(RESULTS_FILE_ATTR); }
    public String getPathToRegionFile() { return attributes.get(REGIONS_FILE_ATTR); }
    public boolean  getComputeOutsideStats()
    {
        return attributes.get(OUTSIDE_STATS_ATTR) != null;
    }


}
