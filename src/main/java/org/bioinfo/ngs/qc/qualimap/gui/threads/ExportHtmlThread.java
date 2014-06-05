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
package org.bioinfo.ngs.qc.qualimap.gui.threads;

/**
 * Created by kokonech
 * Date: 1/16/12
 * Time: 2:22 PM
 */

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.ChartRawDataWriter;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.panels.SavePanel;
import org.bioinfo.ngs.qc.qualimap.common.HtmlReportGenerator;


public class ExportHtmlThread extends Thread{
	/** Logger to print information */
	protected Logger logger;

	/** Variable to manage the panel with the progress bar to increase */
	private SavePanel savePanel;

	/** Variable to control that all the files are saved */
	private int numSavedFiles;

	/** Variable to control the percent of each iteration of the progress bar */
	private double percentLoad;

	/** Variable to manage the dirPath of the file that we are going to save*/
	private String dirPath;

	/** Variables that contains the tab properties loaded in the thread*/
	AnalysisResultManager resultManager;

    boolean guiAvailable, saveRawData;


	public ExportHtmlThread(Component component, AnalysisResultManager resultManager, String dirPath) {
        super("ExportHtmlThread_Gui");
        if (component instanceof SavePanel) {
        	this.savePanel = (SavePanel)component;
        }
        this.resultManager = resultManager;
        this.dirPath = dirPath;
        this.guiAvailable = true;
        this.saveRawData = true;
    }

    public ExportHtmlThread(AnalysisResultManager resultManager, String dirPath) {
        super("ExportHtmlThread");
        this.resultManager = resultManager;
        this.dirPath = dirPath;
        this.guiAvailable = false;
        this.saveRawData = true;
    }

    void setGuiVisible(boolean enable) {
        if (guiAvailable) {
             savePanel.getProgressStream().setVisible(enable);
             savePanel.getProgressBar().setVisible(enable);
        }
    }

    void reportFailure(String msg) {
        if (guiAvailable) {
            setGuiVisible(false);
            JOptionPane.showMessageDialog(null,
                    msg, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            System.err.println(msg);
        }
    }


    void reportSuccess(String msg) {
        if (guiAvailable) {
        // Close the window and show an info message
				savePanel.getHomeFrame().getPopUpDialog().setVisible(false);
				savePanel.getHomeFrame().remove(savePanel.getHomeFrame().getPopUpDialog());
				JOptionPane.showMessageDialog(null,
						msg, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println(msg);
        }
    }

	/**
	 * Public method to run this thread. Its executed when an user call to method start
	 * over this thread. */
    public void run() {


        try {

            File dir = new File(dirPath);
            if (!dir.exists())  {
                boolean ok = (new File(dirPath)).mkdirs();
                if (!ok) {
                    reportFailure("Unable to create the output directory for html report\n");
                    return;
                }
            }

			// Show the ProgressBar and the Text Description
            setGuiVisible(true);

			// Set the number of files saved to initial value
	    	numSavedFiles = 0;

	    	int numItemsToSave = 0;
            List<StatsReporter> reporters = resultManager.getReporters();
	    	AnalysisType analysisType = resultManager.getTypeAnalysis();
	    	for (StatsReporter reporter : reporters) {
	            numItemsToSave += reporter.getCharts().size();
	    	}

	    	percentLoad = (100.0/numItemsToSave);

			// Add the first file of the reporter
			boolean success = true;
            for ( StatsReporter reporter : reporters) {
                String htmlReportFilePath = dirPath + File.separator + reporter.getFileName() + ".html";
                success = generateAndSaveReport(reporter, htmlReportFilePath, analysisType);
                if (!success) {
                    break;
                }
            }

            prepareCss();

            if (saveRawData) {
                for ( StatsReporter reporter : reporters) {
                    saveRawData(reporter, "raw_data_" + reporter.getFileName() );
                }
            }

            if(success){
				reportSuccess("Html report created successfully\n");
			} else {
				reportFailure("Failed to create the HTML file\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
            reportFailure("Unable to create the html file \n" + e.getMessage());
		}
    }


    private boolean generateAndSaveReport(StatsReporter reporter, String path, AnalysisType genomicAnalysis) throws IOException {

        HtmlReportGenerator generator = new HtmlReportGenerator(reporter, dirPath, genomicAnalysis);
        StringBuffer htmlReport = generator.getReport();
        saveReport(htmlReport, path);
        return saveImages(reporter);

    }

    private void prepareCss() throws IOException {
        String newPath = dirPath + "/_static";
        int BUFFER = 2048;

        File destinationParent = new File(newPath);
        if (!destinationParent.exists()) {
            if (!destinationParent.mkdirs()) {
                throw new IOException("Failed to create sub directory " + destinationParent.getAbsolutePath());
            }
        }

        InputStream is = getClass().getResourceAsStream(Constants.pathResources + "css.zip");
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry entry;
        BufferedInputStream source = new BufferedInputStream(zis);

        while ((entry = zis.getNextEntry()) != null) {
            String currentEntryName = entry.getName();
            File destFile = new File(newPath,currentEntryName);

            if (destFile.exists()) {
                continue;
            }

            int currentByte;

            byte data[] = new byte[BUFFER];

            BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER);
            while ((currentByte = source.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, currentByte);
            }

            dest.flush();
            dest.close();

        }

        source.close();



    }


    private void saveReport(StringBuffer htmlReport, String path) throws FileNotFoundException {
        PrintStream outStream = new PrintStream( new BufferedOutputStream(new FileOutputStream(path)));
        outStream.print(htmlReport.toString());

        outStream.close();

    }


    public boolean saveImages(StatsReporter reporter) throws IOException {

        boolean success = true;

        Iterator<QChart> it = reporter.getCharts().iterator();

        // Generate the Graphics images

        while(it.hasNext() && success){

            QChart chart = it.next();
            BufferedImage bufImage;

            if(chart.isBufferedImage()){
                bufImage = chart.getBufferedImage();
            } else {
                bufImage = chart.getJFreeChart().createBufferedImage(
                                    Constants.GRAPHIC_TO_SAVE_WIDTH,
                                    Constants.GRAPHIC_TO_SAVE_HEIGHT);
            }

            String imagePath = HtmlReportGenerator.getImagePath(reporter, chart.getName());
            imagePath = dirPath + File.separator + imagePath;



            File imageFile = new File(imagePath);
            if (!imageFile.getParentFile().exists()) {
                boolean ok = imageFile.getParentFile().mkdirs();
                if (!ok) {
                    return false;
                }
            }

            success = ImageIO.write(bufImage, "PNG", imageFile);

            increaseProgressBar(chart.getTitle());

        }


        return success;

    }


    private void increaseProgressBar(String fileName){

    	if (!guiAvailable) {
            return;
        }

    	// Increase the number of files loaded
    	numSavedFiles++;
    	// Increase the progress bar value
    	int result = (int)Math.ceil(numSavedFiles * percentLoad);
    	savePanel.getProgressBar().setValue(result);

		if(fileName != null){
		    savePanel.getProgressStream().setText("Saving graphics: "+ fileName);
		}
    }

    void saveRawData(StatsReporter reporter, String subFolderName) throws IOException{
        List<QChart> charts = reporter.getCharts();
        String newPath = dirPath + File.separator + subFolderName;

        File destinationParent = new File(newPath);
        if (!destinationParent.exists()) {
            if (!destinationParent.mkdirs()) {
                throw new IOException("Failed to create sub directory " + destinationParent.getAbsolutePath());
            }
        }

        for (QChart chart : charts) {
            if (chart.canExportRawData()) {
                ChartRawDataWriter dataWriter = chart.getDataWriter();
                String fileName = chart.getTitle().replace(" ", "_").toLowerCase() + ".txt";
                dataWriter.exportDataToFile(newPath + "/" + fileName);
            }
        }

    }


}