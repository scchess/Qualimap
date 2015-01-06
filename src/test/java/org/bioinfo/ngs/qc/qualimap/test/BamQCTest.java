/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysis;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class BamQCTest {


    List<TestConfig> tests;


    public BamQCTest() throws IOException {

        Environment testEnv = new Environment();

        tests = new ArrayList<TestConfig>();

        tests.add( new TestConfig("bamqc/test001.txt", testEnv) );
        tests.add( new TestConfig("bamqc/test002.txt", testEnv) );
        tests.add( new TestConfig("bamqc/test003.txt", testEnv) );
        tests.add( new TestConfig("bamqc/test004.txt", testEnv) );
        tests.add( new TestConfig("bamqc/test005.txt", testEnv) );
        //tests.add( new TestConfig("bamqc/test006.txt", testEnv) );
        //tests.add( new TestConfig("bamqc/test007.txt", testEnv) );
    }

    @Test
    public void testStats() {


        for (TestConfig test : tests) {

            System.err.println("\nRunning test " + test.getPath() + "\n");


            BamStatsAnalysis bamQc = new BamStatsAnalysis(test.getPathToBamFile()) ;

            String pathToRegionFile = test.getPathToRegionFile();
            if (pathToRegionFile != null && !pathToRegionFile.isEmpty()) {
                bamQc.setSelectedRegions(pathToRegionFile);
                bamQc.setComputeOutsideStats(test.getComputeOutsideStats());
            }

            BamQCRegionReporter bamQcReporter = new BamQCRegionReporter(false, true);


            try {
                bamQc.run();
                bamQcReporter.loadReportData(bamQc.getBamStats());
                bamQcReporter.computeChartsBuffers(bamQc.getBamStats(), bamQc.getLocator(), bamQc.isPairedData());
            } catch (Exception e) {
                assertTrue("Error calculating stats. " + e.getMessage(), false);
                e.printStackTrace();
                return;
            }

            Properties calculatedProps = bamQcReporter.generateBamQcProperties();

            // For generating correct tests
            try {
                calculatedProps.store(new FileOutputStream(test.getResultsPath() + ".new"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            compareProperties(calculatedProps, test.getResultsPath());

            if (test.getComputeOutsideStats() && test.getOutsideResultsPath() != null) {
                BamQCRegionReporter outsideReporter = new BamQCRegionReporter(true, false);
                outsideReporter.loadReportData(bamQc.getOutsideBamStats());
                /*outsideReporter.computeChartsBuffers(bamQc.getOutsideBamStats(),
                        bamQc.getLocator(), bamQc.isPairedData());*/
                Properties outsideProps = outsideReporter.generateBamQcProperties();

                /*try {
                    outsideProps.store(new FileOutputStream(test.getOutsideResultsPath()), null);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }*/

                compareProperties(outsideProps, test.getOutsideResultsPath());
            }

        }
     }


    void compareProperties(Properties calculatedProps, String pathToValidProperties){
          Properties validProps = new Properties();
            try {
                validProps.load(new BufferedInputStream(new FileInputStream(pathToValidProperties)));
            } catch (IOException e) {
                assertTrue("Error loading valid stats.", false);
                e.printStackTrace();
                return;
            }

            final List<String> keysToSkip = Arrays.asList("meanMappingQualityX", "numWindowsX", "aPercentX");

            for (String key : calculatedProps.stringPropertyNames()) {
                if (keysToSkip.contains(key)) {
                    continue;
                }
                if (validProps.containsKey(key)) {
                    //System.out.println("Property: " + key);
                    String expectedValue = validProps.getProperty(key);
                    String calculatedValue = calculatedProps.getProperty(key);
                    String errorMessage = "Stats are not equal. Key: " + key +
                            "\nExpected: " + expectedValue + "\nCalculated: " + calculatedValue;
                    assertTrue(errorMessage, expectedValue.equals(calculatedValue));
                } else {
                    assertTrue("Item \"" + key + "\" is not found in result properties.", false);
                }
            }
    }



}
