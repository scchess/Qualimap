package org.bioinfo.ngs.qc.qualimap.beans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 7/12/12
 * Time: 10:51 AM
 */
public class TextFileDataWriter extends ChartRawDataWriter{

    String[] sourceFileNames;
    String dataPath;

    /**
     * Creates new data writer
     * @param dataPath Path to dir containg the input files
     * @param sourceFileName Name or several names of files, separated by semicolon
     */
    public TextFileDataWriter(String dataPath, String sourceFileName) {
        this.dataPath = dataPath;
        this.sourceFileNames = sourceFileName.split(";");
    }

    @Override
    void exportData(BufferedWriter dataWriter) throws IOException {

        for (String sourceFileName : sourceFileNames) {

            dataWriter.write(sourceFileName);
            dataWriter.write("\n");

            BufferedReader reader = new BufferedReader( new FileReader(dataPath + sourceFileName));

            String line;
            while ( (line = reader.readLine()) != null) {
                dataWriter.write(line);
                dataWriter.write("\n");

            }

            reader.close();
        }

    }
}
