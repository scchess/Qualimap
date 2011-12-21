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
    private String pathToBamFile;
    private String pathToValiationOptions;
    private String pathToRegionFile;
    private boolean computeOutsideRegions;

    private final String BAM_FILE_ATTR = "bamfile";
    private final String RESULTS_FILE_ATTR = "result";
    private final String REGIONS_FILE_ATTR = "regions";
    private final String OUTSIDE_STATS_ATTR = "compute-outside-stats";

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
        HashMap<String,String> attributes = parseConfigFile(fileReader);

        pathToBamFile = attributes.get(BAM_FILE_ATTR);
        pathToValiationOptions = attributes.get(RESULTS_FILE_ATTR);
        if (attributes.containsKey(REGIONS_FILE_ATTR)) {
            pathToRegionFile = attributes.get(REGIONS_FILE_ATTR);
        }   else {
            pathToRegionFile = "";
        }
        computeOutsideRegions = attributes.get(OUTSIDE_STATS_ATTR) != null;

    }

    public String getPathToBamFile() { return pathToBamFile; }
    public String getPathToValiationOptions() { return pathToValiationOptions; }
    public String getPathToRegionFile() { return pathToRegionFile; }
    public boolean  getComputeOutsideStats() { return computeOutsideRegions; }


}
