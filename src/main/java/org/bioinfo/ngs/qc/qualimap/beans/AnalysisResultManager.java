/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.UniqueID;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokonech
 * Date: 7/13/13
 * Time: 3:34 PM
 */
public class AnalysisResultManager {

    AnalysisType typeAnalysis;
    String  outputFolder;
    List<StatsReporter> reporters;

    public AnalysisResultManager(AnalysisType analysisType) {
        this.typeAnalysis = analysisType;
        this.outputFolder = "";
        reporters = new ArrayList<StatsReporter>();
    }

    public AnalysisType getTypeAnalysis() {
        return typeAnalysis;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void addReporter(StatsReporter reporter) {
        reporters.add(reporter);
    }

    public List<StatsReporter> getReporters() {
        return reporters;
    }

    public StringBuilder createDirectory(){
            boolean created = false;
            StringBuilder folderName = null;
            StringBuilder folderPath = new StringBuilder(HomeFrame.outputpath);
            StringBuilder outputDirPath = null;

            while(!created){
                try {
                    folderName = new StringBuilder(""+ UniqueID.get() + "/");
                    outputDirPath = new StringBuilder(folderPath.toString() + folderName.toString());
                    FileUtils.checkDirectory(outputDirPath.toString());
                } catch (IOException e) {
                    FileUtils.createDirectory(outputDirPath.toString(), true);
                    outputFolder = folderName.toString();
                    created = true;
                }
            }

            return outputDirPath;
        }

}
