package org.bioinfo.ngs.qc.qualimap.utils;

import org.apache.commons.io.FilenameUtils;

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
                    continue;
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


    public static FeatureFileFormat guessFeaturesFileFormat(String fileName) {

        String fileExt = FilenameUtils.getExtension(fileName);

        if (fileExt.equalsIgnoreCase("bed")) {
            return FeatureFileFormat.BED;
        } else if (fileExt.equalsIgnoreCase("gff") ) {
            return FeatureFileFormat.GFF;
        } else {
            // try guessing format, read 100 lines and collect scores
            int scoreBed = 0, scoreGff = 0, countRecords = 0;
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                while (countRecords < 100) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    if (line.startsWith("#") || line.isEmpty()) {
                        // skip comments and empty lines
                    }

                    String[] items = line.split("\\s+");

                    if (items.length >= 9) {
                        try {
                            Integer.parseInt(items[3]);
                        } catch (NumberFormatException ex) {
                            --scoreGff;
                        }
                        ++scoreGff;
                    }

                    if (items.length >= 3) {
                        try {
                            Integer.parseInt(items[1]);
                        } catch (NumberFormatException ex) {
                            --scoreBed;
                        }
                        ++scoreBed;
                    }

                    ++countRecords;
                }

                br.close();
            } catch (Exception ex) {
                System.err.println("Failed to guess feature file format.");
                return FeatureFileFormat.UNKNOWN;
            }

            if (scoreBed == 0 && scoreGff == 0) {
                return FeatureFileFormat.UNKNOWN;
            } else {
                return ( scoreGff > scoreBed ? FeatureFileFormat.GFF : FeatureFileFormat.BED );
            }




        }




    }



}
