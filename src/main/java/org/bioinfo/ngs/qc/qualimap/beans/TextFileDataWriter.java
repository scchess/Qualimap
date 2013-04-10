/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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
