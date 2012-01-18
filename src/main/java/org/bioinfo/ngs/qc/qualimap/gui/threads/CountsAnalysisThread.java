package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bioinfo.commons.exec.Command;
import org.bioinfo.commons.exec.SingleProcess;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.formats.exception.FileFormatException;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.panels.CountsAnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.panels.HtmlJPanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.RNAAnalysisVO;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.CountReadsAnalysis;

/**
 * Class to manage a thread that do the analysis from RNA-Seq of the input files
 * 
 * @author kokonech
 */
public class CountsAnalysisThread extends Thread {
	/**
	 * Variable to manage the return string loaded for the thread of the
	 * statistics each moment.
	 */
	private String processedString;

	/** Variable to manage the percent of statistics loaded each moment */
	private Double loadPercent;

	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progressbar at the init */
	private CountsAnalysisDialog settingsDlg;

	/** Variable to control the percent of each iteration of the progress bar */
	private double percentLoad;

	/**
	 * Variable to control number of differents steps to load before showing the
	 * new tab. This variable is used to control the percent loaded
	 */
	private double numStepsRnaToDo;

	/** Variable to control the current step loaded */
	private int currentStepLoaded;

	/** Variables that contains the tab properties loaded in the thread */
	TabPropertiesVO tabProperties;

    String firstSampleDataPath, secondSampleDataPath, infoFilePath;

	public CountsAnalysisThread(String str, CountsAnalysisDialog countsAnalysisDialog, TabPropertiesVO tabProperties) {
		super(str);
		this.processedString = null;
		this.loadPercent = 0.0;
		this.currentStepLoaded = 0;
		this.settingsDlg = countsAnalysisDialog;
        this.tabProperties = tabProperties;
		logger = new Logger(this.getClass().getName());
	}

	/**
	 * Public method to run this thread. Its executed when an user call to
	 * method start over this thread.
     */
    public void run() {

        settingsDlg.setUiEnabled(false);

        RNAAnalysisVO rnaAnalysisVO = tabProperties.getRnaAnalysisVO();

        // Create the outputDir directory
        StringBuilder outputDirPath = tabProperties.createDirectory();

        firstSampleDataPath = settingsDlg.getFirstSampleDataPath();

        if (settingsDlg.secondSampleIsProvided()) {
            secondSampleDataPath = settingsDlg.getSecondSampleDataPath();
        }

        if (settingsDlg.infoFileIsProvided())  {
            infoFilePath = settingsDlg.getInfoFilePath();
        } else {
            infoFilePath =  settingsDlg.getHomeFrame().getQualimapFolder() + "species" +
                    File.separator + settingsDlg.getSelectedSpecies();
            rnaAnalysisVO.setInfoFileIsSet(true);
        }


        increaseProgressBar(currentStepLoaded, "Building Rscript sentence");

        // Create the string to execute
        String command = "Rscript " + settingsDlg.getHomeFrame().getQualimapFolder()
                + "scripts"+File.separator+"qualimapRscript.r";

        command += " --homesrc " + settingsDlg.getHomeFrame().getQualimapFolder() + "scripts";
        command += " --data1 " + firstSampleDataPath;
        command += " --name1 " + settingsDlg.getName1().replace(" ", "_");

        if (settingsDlg.secondSampleIsProvided()) {
            command += " --data2 " + secondSampleDataPath;
            command += " --name2 " + settingsDlg.getName2().replace(" ", "_");
        }

        command += " --info " + infoFilePath;
        command += " --threshold " + settingsDlg.getThreshold();

        command += " -o " + outputDirPath;

        // TODO: understand WTF is going on here
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

        if (tabProperties.getRnaAnalysisVO().getMapClassesInfoFile() != null
                && tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().size() > 0) {
            this.numStepsRnaToDo = 5 + tabProperties.getRnaAnalysisVO().getMapClassesInfoFile().size();
        } else {
            this.numStepsRnaToDo = 3;
        }
        percentLoad = (100.0 / numStepsRnaToDo);

        increaseProgressBar(currentStepLoaded, "Running Rscript command... ");
        Command cmd = new Command(command);
        System.out.println(command);
        SingleProcess process = new SingleProcess(cmd);
        process.getRunnableProcess().run();
        try {
            loadBufferedImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputFileName = settingsDlg.getInputDataName();
        settingsDlg.getHomeFrame().addNewPane(inputFileName, tabProperties);
	}


    void calculateCounts(String pathToBamFile, String pathToGffFile, String outpath) throws FileFormatException, IOException, NoSuchMethodException {

        CountReadsAnalysis countReadsAnalysis = new CountReadsAnalysis(pathToBamFile, pathToGffFile);
        countReadsAnalysis.run();

        PrintWriter outWriter = new PrintWriter(new FileWriter(outpath));

        Map<String,Long>  counts = countReadsAnalysis.getReadCounts();
        for (Map.Entry<String,Long> entry: counts.entrySet()) {
            String str = entry.getKey() + "\t" + entry.getValue().toString();
            outWriter.println(str);
        }

        outWriter.flush();

    }

    /**
	 * Function to load the images into a map of buffered images
	 *
	 */
	private void loadBufferedImages() throws IOException {
		BamQCRegionReporter reporter = tabProperties.getReporter();
        reporter.setInputDescription(prepareInputDescription());
		reporter.setImageMap(new HashMap<String, BufferedImage>());
		increaseProgressBar(currentStepLoaded, "Loading graphic: Global Saturation");
		// Insert in the tab the graphic of the Global Saturations
		addImage(reporter,Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION);
		
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

    private String prepareInputDescription() {
        StringBuffer inputDesc = new StringBuffer();

        int width = 700;

        inputDesc.append("<p align=center><a name=\"input\"> <b>Input data & parameters </b></p>" + HtmlJPanel.BR);
        inputDesc.append(HtmlJPanel.getTableHeader(width, "EEEEEE"));

        inputDesc.append(HtmlJPanel.COLSTART + "<b>" + settingsDlg.getName1() + "</b>");

        inputDesc.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
        inputDesc.append(HtmlJPanel.COLSTARTFIX + "Path: " + HtmlJPanel.COLMID
                + settingsDlg.getFirstSampleDataPath() + HtmlJPanel.COLEND);
        inputDesc.append(HtmlJPanel.getTableFooter());


        if (settingsDlg.secondSampleIsProvided()) {
            inputDesc.append(HtmlJPanel.COLSTART + "<b>" + settingsDlg.getName2() + "</b>");

            inputDesc.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
            inputDesc.append(HtmlJPanel.COLSTARTFIX + "Path: " + HtmlJPanel.COLMID
                    + settingsDlg.getFirstSampleDataPath() + HtmlJPanel.COLEND);
            inputDesc.append( HtmlJPanel.getTableFooter() );
        }

        inputDesc.append(HtmlJPanel.COLSTART + "<b> Options </b>");

        inputDesc.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
        inputDesc.append(HtmlJPanel.COLSTARTFIX + "Species info: " + HtmlJPanel.COLMID
                + infoFilePath + HtmlJPanel.COLEND);
        inputDesc.append(HtmlJPanel.COLSTARTFIX + "Threshold: " + HtmlJPanel.COLMID
                        + settingsDlg.getThreshold() + HtmlJPanel.COLEND);
        inputDesc.append(HtmlJPanel.getTableFooter());

        inputDesc.append(HtmlJPanel.getTableFooter());

        return inputDesc.toString();
    }

    private void addImage(BamQCRegionReporter reporter, String name){
		String path = HomeFrame.outputpath + tabProperties.getOutputFolder().toString() + name;
		BufferedImage imageToDisplay = null;
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
	 * @param numElem
	 *            number of the element computed
	 */
	private void increaseProgressBar(double numElem, String action) {
		int result = 0;

		// Increase the number of files loaded
		currentStepLoaded++;
		// Increase the progress bar value
		result = (int) Math.ceil(numElem * percentLoad);
		settingsDlg.getProgressBar().setValue(result);
		if (action != null) {
			settingsDlg.getProgressStream().setText(action);
		}
	}





}
