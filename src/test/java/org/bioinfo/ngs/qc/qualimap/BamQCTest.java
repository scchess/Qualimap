package org.bioinfo.ngs.qc.qualimap;

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;
import org.bioinfo.ngs.qc.qualimap.gui.threads.SaveZipThread;
import org.junit.Test;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class BamQCTest {

    private class TestConfig {
        private String pathToBamFile;
        private String pathToValiationOptions;
        public TestConfig(String pathToBamFile, String pathToValidationOptions) {
            this.pathToBamFile = pathToBamFile;
            this.pathToValiationOptions = pathToValidationOptions;
        }

        public String getPathToBamFile() { return pathToBamFile; }
        public String getPathToValiationOptions() { return pathToValiationOptions; }

    }

    List<TestConfig> tests;


    public BamQCTest() {
        tests = new ArrayList<TestConfig>();

        tests.add( new TestConfig("/home/kokonech/sample_data/example-alignment.sorted.bam",
                "/home/kokonech/sample_data/example-alignment.properties"));

        tests.add( new TestConfig("/home/kokonech/sample_data/PlasmodiumD37_RNASeq.bam",
                       "/home/kokonech/sample_data/PlasmodiumD37_RNASeq.properties"));


    }

    @Test
    public void testStats() {


        for (TestConfig test : tests) {
            BamQCSplitted bamQc = new BamQCSplitted(test.getPathToBamFile());
            bamQc.setNumberOfWindows(500);
            BamQCRegionReporter bamQcReporter = new BamQCRegionReporter();

            try {
                bamQc.run();
                bamQcReporter.loadReportData(bamQc.getBamStats());
                bamQcReporter.computeChartsBuffers(bamQc.getBamStats(), bamQc.getLocator(),bamQc.isPairedData());
            } catch (Exception e) {
                assertTrue("Error calculating stats.", false);
                e.printStackTrace();
                return;
            }

            Properties calculatedProps = new Properties();
            SaveZipThread.generateBamQcProperties(calculatedProps, bamQcReporter);

            Properties validProps = new Properties();
            try {
                validProps.load(new BufferedInputStream(new FileInputStream(test.getPathToValiationOptions())));
            } catch (IOException e) {
                assertTrue("Error loading valid stats.", false);
                e.printStackTrace();
                return;
            }

            final List<String> keysToSkip = Arrays.asList("meanMappingQuality", "numWindows");

            for (String key : calculatedProps.stringPropertyNames()) {
                if (keysToSkip.contains(key)) {
                    continue;
                }
                if (validProps.containsKey(key)) {
                    String expectedValue = validProps.getProperty(key);
                    String calculatedValue = calculatedProps.getProperty(key);
                    String errorMessage = "Stats are not equal. Key: " + key +
                            "\nExpected: " + expectedValue + "\nCalculated: " + calculatedValue;
                    assertTrue(errorMessage, expectedValue.equals(calculatedValue));

                }
            }
        }
     }



}
