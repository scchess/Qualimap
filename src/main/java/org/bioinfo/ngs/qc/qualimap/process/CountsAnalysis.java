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
package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.TextFileDataWriter;
import org.bioinfo.ngs.qc.qualimap.common.AppSettings;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.RNAAnalysisVO;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.common.DocumentUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by kokonech
 * Date: 3/8/12
 * Time: 3:59 PM
 */
public class CountsAnalysis {


    TabPropertiesVO tabProperties;
    String homePath;

    String sample1Name, sample2Name;
    String firstSampleDataPath, secondSampleDataPath;
    String infoFilePath;
    int threshold;
    boolean secondSampleIsProvided;
    boolean includeInfoFile;

    /** Variable controls if to report progress */
    boolean reportProgress;
    JProgressBar progressBar;
    JLabel progressStream;
    /** Variable to control the percent of each iteration of the progress bar */
    private double percentLoad;

    /** Variable to control the current step loaded */
	private int currentStepLoaded;

    public CountsAnalysis(TabPropertiesVO tabProperties, String homePath) {
        this.tabProperties = tabProperties;
        this.homePath = homePath;
        this.currentStepLoaded = 0;
        includeInfoFile = false;
        secondSampleIsProvided = false;
    }


    public void run() throws Exception{

        checkInput();

        RNAAnalysisVO rnaAnalysisVO = tabProperties.getRnaAnalysisVO();

        // Create the outputDir directory
        StringBuilder outputDirPath = tabProperties.createDirectory();

        rnaAnalysisVO.setInfoFileIsSet(includeInfoFile);


        increaseProgressBar(currentStepLoaded, "Building Rscript sentence");

        // Create the string to execute
        String pathToRScript = AppSettings.getGlobalSettings().getPathToRScript();
        String command = pathToRScript + " " + homePath + "scripts" + File.separator + "qualimapRscript.r";

        command += " --homesrc " + homePath + "scripts";
        command += " --data1 " + firstSampleDataPath;
        command += " --name1 " + sample1Name.replace(" ", "_");

        if (secondSampleIsProvided) {
            command += " --data2 " + secondSampleDataPath;
            command += " --name2 " + sample2Name.replace(" ", "_");
        }

        if (includeInfoFile) {
            command += " --info " + infoFilePath;
        }

        command += " --threshold " + threshold;

        command += " -o " + outputDirPath;

        if (includeInfoFile) {

            // load features' classes from info file
            FileReader fr;
            try {
                fr = new FileReader(infoFilePath);

                BufferedReader br = new BufferedReader(fr);

                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.trim().split("\t");
                    if (words == null || words.length != 2) {

                    } else {
                        if (!rnaAnalysisVO.getMapClassesInfoFile().containsKey(words)) {
                            rnaAnalysisVO.getMapClassesInfoFile().put(words[1], words[1] + ".png");
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        double numStepsRnaToDo;
        if (tabProperties.getRnaAnalysisVO().getMapClassesInfoFile() != null
                && tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().size() > 0) {
            numStepsRnaToDo = 5 + tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().size();
        } else {
            numStepsRnaToDo = 3;
        }
        percentLoad = (100.0 / numStepsRnaToDo);

        System.out.println("Command: " + command);

        increaseProgressBar(currentStepLoaded, "Running Rscript command... ");

        Process p = Runtime.getRuntime().exec(command);
        int res = p.waitFor();
        if (res != 0) {
            throw new RuntimeException("The process returned non-zero status!");
        }

        loadBufferedImages();

    }

    private void checkInput() throws RuntimeException {
        String errMsg = DocumentUtils.validateCountsFile(firstSampleDataPath);
        if (!errMsg.isEmpty()) {
            throw new RuntimeException(errMsg);
        }


        if (secondSampleIsProvided) {
            errMsg = DocumentUtils.validateCountsFile(secondSampleDataPath);
            if (!errMsg.isEmpty()) {
                throw new RuntimeException(errMsg);
            }
        }

         if (includeInfoFile) {
             // Info file has the same format as counts file
             errMsg = DocumentUtils.validateCountsFile(infoFilePath);
             if (!errMsg.isEmpty()) {
                throw new RuntimeException(errMsg);
             }
         }
    }


    /**
	 * Function to load the images to list of QCharts
	 *
     * @throws java.io.IOException In case the images ca not be saved
     */
	private void loadBufferedImages() throws IOException {
		BamQCRegionReporter reporter = tabProperties.getReporter();
        prepareInputDescription(reporter);

        List<QChart> chartList = new ArrayList<QChart>();

        increaseProgressBar(currentStepLoaded, "Loading graphic: Global Saturation");
		addImage(chartList, Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION, "Global Saturation", "GlobalSaturation.txt");

        if (secondSampleIsProvided) {
            addImage(chartList, Constants.GRAPHIC_NAME_RNA_SAMPLE_CORRELATION, "Samples Correlation");
        }

		Iterator<Map.Entry<String,String>> it =
                tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().entrySet().iterator();

		// If a info_file or species is selected, add the Saturation per class,
		// the counts per class and the graphics of each biotype
		if (it.hasNext()) {
			increaseProgressBar(currentStepLoaded, "Loading graphic: Detection per Class");
			addImage(chartList,Constants.GRAPHIC_NAME_RNA_SATURATION_PER_CLASS,
                    "Detection Per Class", "DetectionPerGroup.txt");

            increaseProgressBar(currentStepLoaded, "Loading graphic: Counts per Class");
			String countsPerGroupData =  "Counts_boxplot_1.txt";
            if (secondSampleIsProvided) {
                countsPerGroupData += ";Counts_boxplot_2.txt";
            }
            addImage(chartList, Constants.GRAPHIC_NAME_RNA_COUNTS_PER_CLASS, "Counts Per Class", countsPerGroupData);

			while (it.hasNext()) {
				Map.Entry<String, String> aux =  it.next();
				increaseProgressBar(currentStepLoaded, "Loading graphic: " + aux.getKey());
				addImage(chartList,aux.getValue(), aux.getKey());
				addImage(chartList, aux.getKey()+"_boxplot.png", aux.getKey() + " (boxplot)");
			}
			addImage(chartList,"unknown.png", "Unknown", "nogroup.txt");
			addImage(chartList,"unknown_boxplot.png", "Unknown (boxplot)");
		}

        reporter.setChartList(chartList);
	}

    private void addImage(List<QChart> chartList, String name, String title) {
        addImage(chartList, name, title, "");
    }



    private void addImage(List<QChart> chartList, String name, String title, String rawDataFileName) {
        String dirPath = HomeFrame.outputpath + tabProperties.getOutputFolder().toString();
        String imagePath = dirPath + name;
        BufferedImage imageToDisplay;
        try {
            imageToDisplay = ImageIO.read(new FileInputStream(new File(imagePath)));
        } catch (Exception e) {
            System.out.println("Image not found: " + imagePath );
            return;
        }

        QChart chart = new QChart(name, title, imageToDisplay);
        if (!rawDataFileName.isEmpty()) {
            chart.setDataWriter( new TextFileDataWriter(dirPath, rawDataFileName));
        }

        chartList.add(chart);

    }


	/**
	 * Increase the progress bar in the percent depends on the number of the
	 * element computed.
	 *
	 * @param numElem Number of the elements computed
     * @param action  Text to set to the progress stream
     *
	 */
	private void increaseProgressBar(double numElem, String action) {
		if (!reportProgress) {
            return;
        }
		// Increase the number of files loaded
		currentStepLoaded++;
		// Increase the progress bar value
		int result = (int) Math.ceil(numElem * percentLoad);
		progressBar.setValue(result);
		if (action != null) {
			progressStream.setText(action);
		}
	}

    private void prepareInputDescription(BamQCRegionReporter reporter) {

        HashMap<String,String> sample1Params = new HashMap<String, String>();
        sample1Params.put("Path: ", firstSampleDataPath);
        reporter.addInputDataSection(sample1Name, sample1Params);

        if (secondSampleIsProvided) {
            HashMap<String,String> sample2Params = new HashMap<String, String>();
            sample2Params.put("Path: ", secondSampleDataPath);
            reporter.addInputDataSection(sample2Name, sample2Params);
        }

        HashMap<String,String> otherParams = new HashMap<String, String>();
        otherParams.put("Species info: ", infoFilePath);
        otherParams.put("Threshold: ", "" + threshold );
        reporter.addInputDataSection("Options", otherParams);

    }

    public void setProgressControls(JProgressBar progressBar, JLabel progressStream) {
        reportProgress = true;
        this.progressBar = progressBar;
        this.progressStream = progressStream;
    }

    public void setSample2Name(String sample2Name) {
        this.sample2Name = sample2Name;
    }

    public void setSample1Name(String name1) {
        sample1Name = name1;
    }

    public void setFirstSampleDataPath(String firstSampleDataPath) {
        this.firstSampleDataPath = firstSampleDataPath;
    }

    public void setSecondSampleDataPath(String secondSampleDataPath) {
        this.secondSampleDataPath = secondSampleDataPath;
    }

    public void setInfoFilePath(String infoFilePath) {
        includeInfoFile = true;
        this.infoFilePath = infoFilePath;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setSecondSampleIsProvided(boolean secondSampleIsProvided) {
        this.secondSampleIsProvided = secondSampleIsProvided;
    }



}
