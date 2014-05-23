/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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

import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by kokonech
 * Date: 12/12/11
 * Time: 4:11 PM
 */
public class ReadCountsTest {

    ArrayList<TestConfig> externalTests = new ArrayList<TestConfig>();
    ArrayList<TestConfig> internalTests = new ArrayList<TestConfig>();

    Map<String,Long> expectedCounts;
    long noFeatureExpected, ambigExpected, lowQualExpected, notAlignedExpected, notUniqueExpected;



    public ReadCountsTest() throws IOException {

        Environment testEnv = new Environment();

        internalTests.add(new TestConfig("count-reads/test001.txt", testEnv) );
        internalTests.add(new TestConfig("count-reads/test002.txt", testEnv) );
        internalTests.add(new TestConfig("count-reads/test003.txt", testEnv) );
        internalTests.add(new TestConfig("count-reads/test004.txt", testEnv) );


        /* External tests use external data */
        externalTests.add(new TestConfig("count-reads/external/test001.txt", testEnv) );
        externalTests.add(new TestConfig("count-reads/external/test002.txt", testEnv) );
        externalTests.add(new TestConfig("count-reads/external/test003.txt", testEnv) );
        externalTests.add(new TestConfig("count-reads/external/test004.txt", testEnv) );
        externalTests.add(new TestConfig("count-reads/external/test005.txt", testEnv) );
        externalTests.add(new TestConfig("count-reads/external/test006.txt", testEnv) );


    }

    public static
    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
      List<T> list = new ArrayList<T>(c);
      java.util.Collections.sort(list);
      return list;
    }


    @Test
    public void externalTests() {
        runTests(externalTests);
    }

    @Test
    public void internalTests() {
        runTests(internalTests);
    }


    public void runTests(ArrayList<TestConfig> tests) {


        for (TestConfig test : tests) {

            System.err.println("Running test " + test.getPath());

            String pathToBamFile = test.getPathToBamFile();
            if (pathToBamFile.isEmpty()) {
                assertTrue("Path to region file is empty", false);
                return;
            }

            String pathToRegionFile = test.getPathToRegionFile();
            if (pathToRegionFile.isEmpty()) {
                assertTrue("Path to region file is empty",false);
                return;

            }

            String pathToExpectedResults = test.getResultsPath();
            if (pathToExpectedResults.isEmpty()) {
                assertTrue("Path to result is empty",false);
                return;

            }

            ComputeCountsTask computeCountsTask = new ComputeCountsTask(pathToBamFile, pathToRegionFile);

            String strandType = test.getSpecificAttribute("strand");
            if (strandType != null) {
                assertTrue(ComputeCountsTask.supportedLibraryProtocol(strandType));
                computeCountsTask.setProtocol(strandType);
            }

            String pairedAnalysis = test.getSpecificAttribute("paired");
            if (pairedAnalysis != null) {
                if (pairedAnalysis.equalsIgnoreCase("yes")) {
                    computeCountsTask.setPairedEndAnalysis();
                }
            }

            try {
                computeCountsTask.run();
                Map<String,Double> readCounts = computeCountsTask.getReadCounts();

                /*List<String> geneNames = asSortedList(readCounts.keySet());

                for (String key : geneNames) {
                    System.out.println(key + ":" + readCounts.get(key));
                }*/

                System.out.println("No feature: " + computeCountsTask.getNoFeatureNumber());
                System.out.println("Not unique alignment: " + computeCountsTask.getAlignmentNotUniqueNumber());
                System.out.println("Not aligned: " + computeCountsTask.getNotAlignedNumber());
                System.out.println("Ambiguous: " + computeCountsTask.getAmbiguousNumber());


                loadExpectedResults(pathToExpectedResults) ;

                assertTrue("Number of \"ambiguous\" reads is wrong!",
                        ambigExpected == computeCountsTask.getAmbiguousNumber());

                assertTrue("Number of \"no feature\" reads is wrong!",
                        noFeatureExpected == computeCountsTask.getNoFeatureNumber());



                for (Map.Entry<String,Double> entry: readCounts.entrySet()) {

                    String geneName = entry.getKey();
                    if (!expectedCounts.containsKey(geneName)) {
                         assertTrue("Gene is not presented in expected results: " + geneName, false);
                        return;
                    }

                    long expected = expectedCounts.get(geneName);
                    long calculated = entry.getValue().longValue();

                    if (expected != calculated ) {
                        String report = " Expected value (" + expected + ") " +
                                "doesn't match the calculated (" + calculated + ") " +
                                "for gene " + geneName;
                        assertTrue(report, false);
                        //System.err.println(report);
                    }
                }


            } catch (Exception e) {
                assertTrue("Error calculating stats. " + e.getMessage(), false);
                e.printStackTrace();
                return;
            }


        }
     }

    private void loadExpectedResults(String pathToResults) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pathToResults));
        expectedCounts = new HashMap<String, Long>();

        String line;
        while ( (line = reader.readLine()) != null ) {
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("no_feature")) {
                noFeatureExpected = Long.parseLong(line.split("\t")[1]);
            }

            if (line.startsWith("ambiguous")) {
                ambigExpected = Long.parseLong(line.split("\t")[1]);
            }

            if (line.startsWith("not_unique")) {
                notUniqueExpected = Long.parseLong(line.split("\t")[1]);
            }

            if (line.startsWith("not_aligned")) {
                notAlignedExpected = Long.parseLong(line.split("\t")[1]);
            }

            if (line.startsWith("too_low_aQual")) {
                lowQualExpected = Long.parseLong(line.split("\t")[1]);
            }

            String[] values = line.split("\t");
            expectedCounts.put( values[0].trim(), Long.parseLong(values[1]));

        }

    }


}
