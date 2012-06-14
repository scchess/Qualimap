package org.bioinfo.ngs.qc.qualimap.beans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 6/13/12
 * Time: 11:09 AM
 */
public abstract class ChartRawDataWriter {

    public void exportDataToFile(String fileName) throws IOException{
        BufferedWriter writer = new BufferedWriter( new FileWriter(fileName));
        exportData(writer);
        writer.close();
    }

    abstract void exportData(BufferedWriter dataWriter) throws IOException;


}
