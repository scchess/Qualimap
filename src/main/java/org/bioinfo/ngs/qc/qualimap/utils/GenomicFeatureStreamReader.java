package org.bioinfo.ngs.qc.qualimap.utils;

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
                        throw new RuntimeException("GFF format error, not enough fields.\nProblematic line is " + line);
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
                        throw new RuntimeException("BED format error, there should be at least 6 fields.\nProblematic line is " + line);
                    }

                    String seqName = items[0];
                    int start = Integer.parseInt(items[1]) + 1; // 0-based
                    int end = Integer.parseInt(items[2]); //0-based, not-inclusive
                    String featureName = items[3];
                    boolean isNegativeStrand =  items[5].equals("-");

                    return new GenomicFeature(seqName, start, end, isNegativeStrand, featureName);
                }
            };

        } else {
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
                break;
            }
        }

        return recordParser.parseFeatureRecord(line);

    }

    public void close() throws IOException {
        fileReader.close();
    }

    public void reset() throws IOException {
        fileReader.close();
        fileReader = new BufferedReader( new FileReader(fileName));
    }








}
