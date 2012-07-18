/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.common;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by kokonech
 * Date: 4/18/12
 * Time: 11:34 AM
 */
public class DocumentUtils {

    static final int MAX_SCORE = 100;

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
        } else if (fileExt.equalsIgnoreCase("gtf")) {
            return FeatureFileFormat.GTF;
        } else {
            // try guessing format, read 100 lines and collect scores
            int scoreBed = 0, scoreGff = 0, scoreGtf = 0, countRecords = 0;
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                while (countRecords < MAX_SCORE) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    if (line.startsWith("#") || line.isEmpty()) {
                        // skip comments and empty lines
                    }

                    String[] items = line.split("\t");

                    if (items.length >= 9) {
                        try {
                            Integer.parseInt(items[3]);
                        } catch (NumberFormatException ex) {
                            --scoreGff;
                        }
                        ++scoreGff;

                        String attrs = items[8];
                        if (attrs.contains("gene_id") && attrs.contains("transcript_id")) {
                            scoreGtf++;
                        }

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

            if (scoreBed == countRecords)  {
                return FeatureFileFormat.BED;
            } if (scoreGff == countRecords) {
                if (scoreGtf > 0) {
                    return FeatureFileFormat.GTF;
                } else {
                    return FeatureFileFormat.GFF;
                }
            } else {
                return FeatureFileFormat.UNKNOWN;
            }




        }




    }



}
