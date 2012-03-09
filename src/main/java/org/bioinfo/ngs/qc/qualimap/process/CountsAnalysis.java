package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.RNAAnalysisVO;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

        RNAAnalysisVO rnaAnalysisVO = tabProperties.getRnaAnalysisVO();

        // Create the outputDir directory
        StringBuilder outputDirPath = tabProperties.createDirectory();

        rnaAnalysisVO.setInfoFileIsSet(includeInfoFile);


        increaseProgressBar(currentStepLoaded, "Building Rscript sentence");

        // Create the string to execute
        String command = "Rscript " + homePath + "scripts" + File.separator + "qualimapRscript.r";

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

        increaseProgressBar(currentStepLoaded, "Running Rscript command... ");

        Process p = Runtime.getRuntime().exec(command);
        int res = p.waitFor();
        if (res != 0) {
            throw new RuntimeException("The process returned non-zero status!");
        }

        loadBufferedImages();

    }



    /**
	 * Function to load the images into a map of buffered images
	 *
     * @throws java.io.IOException In case the images ca not be saved
     */
	private void loadBufferedImages() throws IOException {
		BamQCRegionReporter reporter = tabProperties.getReporter();
        prepareInputDescription(reporter);
        reporter.setImageMap(new HashMap<String, BufferedImage>());
		increaseProgressBar(currentStepLoaded, "Loading graphic: Global Saturation");
		// Insert in the tab the graphic of the Global Saturations
		addImage(reporter, Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION);

		Iterator it = tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().entrySet().iterator();
		// If a info_file or species is selected, add the Saturation per class,
		// the counts per class and the graphics of each biotype
		if (it.hasNext()) {
			increaseProgressBar(currentStepLoaded, "Loading graphic: Detection per Class");
			addImage(reporter,Constants.GRAPHIC_NAME_RNA_SATURATION_PER_CLASS);
			increaseProgressBar(currentStepLoaded, "Loading graphic: Counts per Class");
			addImage(reporter,Constants.GRAPHIC_NAME_RNA_COUNTS_PER_CLASS);

			while (it.hasNext()) {
				Map.Entry<String, String> aux = (Map.Entry<String, String>) it.next();
				increaseProgressBar(currentStepLoaded, "Loading graphic: " + aux.getKey());
				addImage(reporter,aux.getValue());
				addImage(reporter,aux.getKey()+"_boxplot.png");
			}
			addImage(reporter,"unknown.png");
			addImage(reporter,"unknown_boxplot.png");
		}
	}


     private void addImage(BamQCRegionReporter reporter, String name){
		String path = HomeFrame.outputpath + tabProperties.getOutputFolder().toString() + name;
		BufferedImage imageToDisplay;
        try {
	        imageToDisplay = ImageIO.read(new FileInputStream(new File(path)));
			reporter.getImageMap().put(name, imageToDisplay);
        } catch (FileNotFoundException e) {
	       System.out.println("Image not found: " + path );
        } catch (IOException e) {
        	System.out.println("Image not found: " + path );
        }
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
