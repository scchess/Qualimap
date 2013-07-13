package org.bioinfo.ngs.qc.qualimap.beans;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.ngs.qc.qualimap.common.UniqueID;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;

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
