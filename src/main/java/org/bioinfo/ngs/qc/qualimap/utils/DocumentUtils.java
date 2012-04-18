package org.bioinfo.ngs.qc.qualimap.utils;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by kokonech
 * Date: 4/18/12
 * Time: 11:34 AM
 */
public class DocumentUtils {

    public static String validateTabDelimitedFile(String fileName, int expectedNumFields) {

        int countRecords = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while (countRecords < 1000) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                if (line.startsWith("#") || line.isEmpty()) {
                    // skip comments and empty lines
                }

                String[] items = line.split("\t");

                if (items.length < expectedNumFields) {
                    throw new RuntimeException("Tab-delimited file format error in " + fileName + ": not enough fields.");
                }

                countRecords++;
            }

            br.close();

        } catch (Exception e) {
            return e.getMessage();
        }

        return countRecords > 0 ? "" : "Tab-delimited file format error in " + fileName + ": file is empty.";
    }


    public static String validateCountsFile(String fileName) {
          int countRecords = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while (countRecords < 1000) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                if (line.startsWith("#") || line.isEmpty()) {
                    // skip comments and empty lines
                }

                String[] items = line.split("\\s+");

                if (items.length != 2) {
                    throw new RuntimeException("Counts file format error in " + fileName);
                }

                countRecords++;
            }

            br.close();

        } catch (Exception e) {
            return e.getMessage();
        }

        return countRecords > 0 ? "" : "Counts file format error in " + fileName + ": file is empty.";




    }

}
