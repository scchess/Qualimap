package org.bioinfo.ngs.qc.qualimap;

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

    ArrayList<TestConfig> tests = new ArrayList<TestConfig>();
    Map<String,Long> expectedCounts;
    long noFeatureExpected, ambigExpected, lowQualExpected, notAlignedExpected, notUniqueExpected;



    public ReadCountsTest() throws IOException {

        Environment testEnv = new Environment();

        tests.add(new TestConfig("count-reads/test001.txt", testEnv) );
        tests.add(new TestConfig("count-reads/test002.txt", testEnv) );
        tests.add(new TestConfig("count-reads/test003.txt", testEnv) );
        tests.add(new TestConfig("count-reads/test004.txt", testEnv) );
        tests.add(new TestConfig("count-reads/test005.txt", testEnv) );
        tests.add(new TestConfig("count-reads/test006.txt", testEnv) );

    }

    public static
    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
      List<T> list = new ArrayList<T>(c);
      java.util.Collections.sort(list);
      return list;
    }


    @Test
    public void testStats() {


        for (TestConfig test : tests) {

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
                computeCountsTask.setProtocol(strandType);
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
