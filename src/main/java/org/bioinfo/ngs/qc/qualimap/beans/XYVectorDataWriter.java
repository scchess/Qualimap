package org.bioinfo.ngs.qc.qualimap.beans;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 6/13/12
 * Time: 2:56 PM
 */
public class XYVectorDataWriter extends ChartRawDataWriter {

    XYVector data;
    String xName, yName;

    public XYVectorDataWriter(XYVector data, String xName, String yName) {
        this.data = data;
        this.xName = xName;
        this.yName = yName;
    }

    @Override
    void exportData(BufferedWriter dataWriter) throws IOException {
        dataWriter.write("#" + xName + "\t" + yName + "\n");
        int len = data.getSize();
        for (int i = 0; i < len; ++i) {
            XYItem item = data.get(i);
            dataWriter.write(item.getX() + "\t" + item.getY() + "\n");
        }
    }
}
