package org.bioinfo.ngs.qc.qualimap.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 12/13/11
 * Time: 10:54 AM
 */
public class GtfParser {
    public static class Record {

        String seqName;
        String source;
        String feature;
        String start;
        String end;
        String score;
        String strand;
        String frame;
        Map<String,String> attributes;

        public String getSeqName() {
            return seqName;
        }

        public String getFeature() {
            return feature;
        }

        public int getStart() {
            return Integer.parseInt(start);
        }

        public int getEnd() {
            return Integer.parseInt(end);
        }

        public boolean getStrand() {
            return strand.equals("+");
        }

        Record() {
            attributes = new HashMap<String, String>();
        }

        public String getAttribute(String attrName) {
            return attributes.get(attrName);
        }
    }

    String fileName;
    BufferedReader br;


    public GtfParser(String fileName) throws IOException {
        this.fileName = fileName;
        br = new BufferedReader(new FileReader(fileName));
    }


    public Record readNextRecord() throws IOException  {

        String line;
        while (true) {
            line = br.readLine();
            if (line == null) {
                return null;
            }
            if (line.startsWith("#") || line.isEmpty()) {
                // skip comments and empty lines
            }  else {
                break;
            }
        }

        String[] items = line.split("\t");

        Record r = new Record();
        r.seqName = items[0];
        r.source = items[1];
        r.feature = items[2];
        r.start = items[3];
        r.end = items[4];
        r.score = items[5];
        r.strand =  items[6];
        r.frame = items[7];

        String[] attrs = items[8].trim().split(";");
        for (String attr: attrs) {
            String[] pair = attr.trim().split(" ");
            if (pair.length < 2) {
                throw new RuntimeException("Badly formatted gff file!\nProblematic line is " + line);
            }
            String value = pair[1];
            //remove surrounding quotes
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            r.attributes.put(pair[0], value);
        }

        return r;
    }


    public void close() throws IOException {
        br.close();
    }

}
