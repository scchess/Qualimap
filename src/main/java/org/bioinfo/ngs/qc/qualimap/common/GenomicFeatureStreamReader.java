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
package org.bioinfo.ngs.qc.qualimap.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 6/8/12
 * Time: 5:55 PM
 */
public class GenomicFeatureStreamReader {

    static abstract class FeatureRecordParser {

        abstract GenomicFeature parseFeatureRecord(String record) throws RuntimeException;

    }

    static FeatureRecordParser getFeatureRecordParser(FeatureFileFormat format) {
        if (format.equals(FeatureFileFormat.GFF)) {
            return new FeatureRecordParser() {
                @Override
                GenomicFeature parseFeatureRecord(String line) {

                    String[] items = line.split("\t");

                    if (items.length < 8) {
                        throw new RuntimeException("GFF format error, not enough fields.\n" +
                                "Problematic line is " + line);
                    }

                    String seqName = items[0];
                    String featureName = items[2];
                    int start = Integer.parseInt(items[3]); // 1-based
                    int end = Integer.parseInt(items[4]); // 1-based, inclusive
                    boolean isNegativeStrand =  items[6].equals("-");

                    return new GenomicFeature(seqName, start, end, isNegativeStrand, featureName);
                }
            };
        } else if (format.equals(FeatureFileFormat.BED)) {
             return new FeatureRecordParser() {
                @Override
                GenomicFeature parseFeatureRecord(String line) {

                    String[] items = line.split("\t");

                    if (items.length < 6) {
                        throw new RuntimeException("BED format error, there should be at least 6 fields.\n" +
                                "Problematic line is " + line);
                    }

                    String seqName = items[0];
                    int start = Integer.parseInt(items[1]) + 1; // 0-based
                    int end = Integer.parseInt(items[2]); //0-based, not-inclusive
                    String featureName = items[3];
                    boolean isNegativeStrand =  items[5].equals("-");

                    return new GenomicFeature(seqName, start, end, isNegativeStrand, featureName);
                }
            };

        } else if (format.equals(FeatureFileFormat.GTF)) {
            return new FeatureRecordParser() {
                @Override
                GenomicFeature parseFeatureRecord(String line) throws RuntimeException {

                    String[] items = line.split("\t");

                    if (items.length != 9) {
                        throw new RuntimeException("GTF format error: there should " +
                                "9 tab-separated fields in each record.\n" +
                                "Problematic line is\n" + line);
                    }

                    String seqName = items[0];
                    String featureName = items[2];
                    int start = Integer.parseInt(items[3]); // 1-based
                    int end = Integer.parseInt(items[4]); // 1-based, inclusive
                    boolean isNegativeStrand =  items[6].equals("-");

                    GenomicFeature feature = new GenomicFeature(seqName, start, end, isNegativeStrand, featureName);

                    String[] attrs = items[8].trim().split(" ");

                    int len = attrs.length;
                    if (len % 2 != 0) {
                        throw new RuntimeException("Warning! Line with wrong attributes is skipped:\n" + line);
                    }

                    for (int i = 0; i < len; i += 2) {
                        String atrName = attrs[i];
                        String atrVal = attrs[i+1];
                        if (atrVal.endsWith(";")) {
                            atrVal = atrVal.substring(0, atrVal.length() - 1);
                        } else {
                            throw new RuntimeException("Warning! Line with wrong attributes:\n" + line);
                        }
                        if (atrVal.startsWith("\"") && atrVal.endsWith("\"")) {
                            atrVal = atrVal.substring(1, atrVal.length() - 1);
                        }
                        feature.addAttribute(atrName, atrVal);

                    }

                    return feature;

                }
            };

        }
        else {
            throw new RuntimeException("Unknown feature file format! ");
        }

    }

    String fileName;
    BufferedReader fileReader;
    FeatureRecordParser recordParser;

    public GenomicFeatureStreamReader(String fileName, FeatureFileFormat formatName ) throws IOException {
        this.fileName = fileName;
        this.fileReader = new BufferedReader(new FileReader(fileName));
        this.recordParser = getFeatureRecordParser(formatName);

    }

    /**
     * This method returns true until there are records remaining in feature file
     * @return False if end of file is reached, otherwise True
     * @throws IOException File reader exception
     */
    public boolean skipNextRecord() throws IOException {
        String line;
        while (true) {
            line = fileReader.readLine();
            if (line == null) {
                return false;
            }
            if (line.startsWith("#") || line.isEmpty()) {
                // skip comments and empty lines
            }  else {
                break;
            }
        }

        return true;
    }


    public GenomicFeature readNextRecord() throws IOException  {

        String line;
        while (true) {
            line = fileReader.readLine();
            if (line == null) {
                return null;
            }
            if (line.startsWith("#") || line.isEmpty()) {
                // skip comments and empty lines
            }  else {

                try {
                    return recordParser.parseFeatureRecord(line);
                } catch (RuntimeException e) {
                    String cause =  e.getCause().toString();
                    System.err.println(cause);
                    if (e.getMessage().startsWith("Warning")) {
                       System.err.println(e.getMessage());
                    } else {
                        throw e;
                    }
                }

            }
        }

        //return f;
        //return recordParser.parseFeatureRecord(line);

    }

    public void close() throws IOException {
        fileReader.close();
    }

    public void reset() throws IOException {
        fileReader.close();
        fileReader = new BufferedReader( new FileReader(fileName));
    }








}
