package org.bioinfo.ngs.qc.qualimap;

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.process.BamQCSplitted;
import org.bioinfo.ngs.qc.qualimap.gui.threads.SaveZipThread;
import org.junit.Test;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class BamQCTest {

    @Test
    public void testStats() {

        String pathToBamFile = "/home/kokonech/sample_data/example-alignment.sorted.bam";
        String pathToValidationOptions = "/home/kokonech/sample_data/example-alignment.properties";

        BamQCSplitted bamQc = new BamQCSplitted(pathToBamFile);
        bamQc.setNumberOfWindows(200);
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
            validProps.load(new BufferedInputStream(new FileInputStream(pathToValidationOptions)));
        } catch (IOException e) {
            assertTrue("Error loading valid stats.", false);
            e.printStackTrace();
            return;
        }

        final List<String> keysToSkip = Arrays.asList("key1", "key2");

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
