package org.bioinfo.ngs.qc.qualimap.gui.threads;

/**
 * Created by kokonech
 * Date: 1/16/12
 * Time: 2:22 PM
 */

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenLoadedStatistics;
import org.bioinfo.ngs.qc.qualimap.gui.panels.SavePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.jfree.chart.JFreeChart;


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
	TabPropertiesVO tabProperties;

    static final int WIDTH = 700;


	public ExportHtmlThread(String str, Component component, TabPropertiesVO tabProperties, String dirPath) {
        super(str);
        if (component instanceof SavePanel) {
        	this.savePanel = (SavePanel)component;
        }
        this.tabProperties = tabProperties;
        this.dirPath = dirPath;
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
                    savePanel.getProgressBar().setVisible(false);
                    savePanel.getProgressStream().setVisible(false);
                    JOptionPane.showMessageDialog(savePanel,
                            "Unable to create the output directory for html report \n", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String htmlReportFilePath = dirPath + "/report.html";

			boolean loadOutsideReporter = false;

			 // Show the ProgressBar and the Text Description
	    	savePanel.getProgressStream().setVisible(true);
	    	savePanel.getProgressBar().setVisible(true);

			// Set the number of files saved to initial value
	    	numSavedFiles = 0;

	    	int numItemsToSave;
	    	boolean genomicAnalysis = false;

            if  (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EXOME ||
                    tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_DNA ) {
                numItemsToSave = tabProperties.getReporter().getMapCharts().size();
                genomicAnalysis = true;
            } else {
                numItemsToSave = tabProperties.getReporter().getImageMap().size();
            }

	    	if(tabProperties.getOutsideReporter() != null &&
					!tabProperties.getOutsideReporter().getBamFileName().isEmpty()){
	    		loadOutsideReporter = true;
	    		numItemsToSave += tabProperties.getOutsideReporter().getMapCharts().size() + 1;
	    	}

	    	percentLoad = (100.0/numItemsToSave);

			// Add the first file of the reporter
			BamQCRegionReporter reporter = tabProperties.getReporter();

            StringBuffer htmlReport = new StringBuffer();

            addHeader(htmlReport, tabProperties);

            htmlReport.append( addTableOfContents(reporter, genomicAnalysis) );

			if (genomicAnalysis) {
                htmlReport.append(  OpenLoadedStatistics.prepareHtmlReport(reporter,tabProperties,WIDTH) );
            }

            htmlReport.append( "<br><br>").append( reporter.getInputDescription(800) ).append("<br><br>");

            boolean success = saveImages(htmlReport, reporter, genomicAnalysis);

			// Add the files of the third reporter
			if(success && loadOutsideReporter){
				BamQCRegionReporter outsideReporter = tabProperties.getOutsideReporter();

                htmlReport.append( addTableOfContents(outsideReporter, true) );
                htmlReport.append( OpenLoadedStatistics.prepareHtmlReport(outsideReporter, tabProperties, WIDTH) );
                success = saveImages( htmlReport, outsideReporter, true);

            }

            addFooter(htmlReport);

            PrintStream outStream = new PrintStream( new BufferedOutputStream(new FileOutputStream(htmlReportFilePath)));
            outStream.print(htmlReport.toString());

            outStream.close();

            if(success){
				// Close the window and show an info message
				savePanel.getHomeFrame().getPopUpDialog().setVisible(false);
				savePanel.getHomeFrame().remove(savePanel.getHomeFrame().getPopUpDialog());
				JOptionPane.showMessageDialog(null,
						"Html Report Created Successfully \n", "Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				savePanel.getProgressBar().setVisible(false);
				savePanel.getProgressStream().setVisible(false);
				JOptionPane.showMessageDialog(null,
						"Failed to create the htmlfile \n", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			savePanel.getProgressBar().setVisible(false);
			savePanel.getProgressStream().setVisible(false);
            e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Unable to create the html file \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
    }

    private StringBuffer addTableOfContents( BamQCRegionReporter reporter, boolean genomicAnalysis) {

        StringBuffer contents = new StringBuffer();
        contents.append("<h1 align=\"center\">Qualimap report</h2>\n");
        contents.append("<br>\n");

        if (genomicAnalysis) {
            String fileName = new File(reporter.getBamFileName()).getName();
            contents.append("<li> <a class=\"content\" href=\"report.html#summary\">").
                    append("Summary of: ").append(fileName).append("</a>");
        }
        contents.append("<li> <a class=\"content\" href=\"report.html#input\">").
                       append("Input data & parameters").append("</a>");


        Set<String> names = genomicAnalysis ? reporter.getMapCharts().keySet() :
                reporter.getImageMap().keySet();

        for (String name : names) {
            contents.append("<li> <a class=\"content\" href=\"report.html#").append(name)
            .append("\">Graph: ").append(name).append("</a>\n");
        }

        contents.append("<br><br><br>\n");

        return contents;
    }

    private void addHeader(StringBuffer htmlReport, TabPropertiesVO tabProperties) {

        htmlReport.append("<!DOCTYPE HTML>\n");
        htmlReport.append("<html>\n");

        String analysis;
        if (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_RNA) {
            analysis = "RNA=seq";
        }  else if (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EPI) {
            analysis = "Epigenetics";
        } else {
            analysis = "Genomic analysis";
        }


        htmlReport.append("<head><title>").append("Qualimap report: ").append(analysis)
                .append("</title>\n");

        htmlReport.append("</head>\n");
        htmlReport.append("<body>\n");

    }


    private void addFooter(StringBuffer htmlReport) {
        htmlReport.append("<br><br>Generated by Qualimap\n");
        htmlReport.append("</body>");
        htmlReport.append("</html>");

    }

    public boolean saveImages(StringBuffer htmlReport, BamQCRegionReporter reporter, boolean genomicAnalysis) throws IOException {

        boolean success = true;

        Iterator<?> it = genomicAnalysis ? reporter.getMapCharts().entrySet().iterator()
                : reporter.getImageMap().entrySet().iterator();

        // Generate the Graphics images

        htmlReport.append("\n\n");
        while(it.hasNext() && success){
            @SuppressWarnings("unchecked")
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();

            BufferedImage bufImage;

            if(entry.getValue() instanceof JFreeChart){
                bufImage = ((JFreeChart)entry.getValue()).createBufferedImage(
                        Constants.GRAPHIC_TO_SAVE_WIDTH,
                        Constants.GRAPHIC_TO_SAVE_HEIGHT);
            } else {
                bufImage = (BufferedImage)entry.getValue();
            }

            String imagePath = dirPath + "/" + entry.getKey();
            String extension = entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1);
            if (!extension.equalsIgnoreCase("png")) {
                imagePath += ".png";
            }
            File imageFile = new File(imagePath);
            success = ImageIO.write(bufImage, "PNG", imageFile);

            htmlReport.append("<br><br><p align=\"center\">\n<b>").append("<a name=\"")
                    .append(entry.getKey()).append("\">")
                    .append(entry.getKey()).append("</a></b><br><br>\n");
            htmlReport.append("<div align=\"center\"><img src=").append(imagePath).append("></div>\n");

            increaseProgressBar(entry.getKey());

        }


        return success;

    }


    private void increaseProgressBar(String fileName){

    	// Increase the number of files loaded
    	numSavedFiles++;
    	// Increase the progress bar value
    	int result = (int)Math.ceil(numSavedFiles * percentLoad);
    	savePanel.getProgressBar().setValue(result);

		if(fileName != null){
		    savePanel.getProgressStream().setText("Saving graphics: "+ fileName);
		}
    }


}