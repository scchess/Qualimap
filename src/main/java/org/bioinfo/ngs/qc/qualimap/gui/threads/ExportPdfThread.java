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
package org.bioinfo.ngs.qc.qualimap.gui.threads;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.panels.SavePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StatsKeeper;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by kokonech
 * Date: 3/20/12
 * Time: 2:25 PM
 */
public class ExportPdfThread extends Thread {

    SavePanel savePanel;
    TabPropertiesVO tabProperties;
    String path;
    boolean guiAvailable;
    boolean bamQCAnalysis;
    double percentLoad;
    int curChapterNum;
    int numSavedItems;


    public ExportPdfThread(String str, Component component, TabPropertiesVO tabProperties, String path) {
        super(str);
        if (component instanceof SavePanel) {
        	this.savePanel = (SavePanel)component;
        }
        this.tabProperties = tabProperties;
        this.path = path;
        this.guiAvailable = true;
    }

    public ExportPdfThread(TabPropertiesVO tabProperties, String path) {
        this.tabProperties = tabProperties;
        this.path = path;
        this.guiAvailable = false;
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

    public void run() {
        try {
			OutputStream file = new FileOutputStream(path);

			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, file);

            document.open();

            curChapterNum = 1;
			numSavedItems = 0;
            boolean loadOutsideReporter = false;

			setGuiVisible(true);
            initDocument(document);

        	// Number of items to save into the PDF file (graphics + 3 files of properties + Header + Footer)
	    	int numItemsToSave =  tabProperties.getReporter().getCharts().size();

            bamQCAnalysis = tabProperties.getTypeAnalysis().isBamQC();

	    	if(tabProperties.getOutsideReporter() != null &&
					!tabProperties.getOutsideReporter().getBamFileName().isEmpty()){
	    		loadOutsideReporter = true;
	    		numItemsToSave += tabProperties.getOutsideReporter().getCharts().size();
	    	}

	    	percentLoad = (100.0/numItemsToSave);

			BamQCRegionReporter reporter = tabProperties.getReporter();
            addReporterData(document, reporter);

            if (loadOutsideReporter) {
                BamQCRegionReporter outsideReporter = tabProperties.getOutsideReporter();
                addReporterData(document, outsideReporter);
            }


            document.close();
            file.close();

            reportSuccess("Pdf File Created Successfully \n");

		} catch (Exception e) {
            e.printStackTrace();
            // If the file could not generate correctly
			File f = new File(path);
			if (!f.delete()) {
                System.err.print("Failed to delete " + path);
            }
            reportFailure("Unable to create the pdf file \n" + e.getMessage());
		}



    }


    private void addReporterData(Document document, BamQCRegionReporter reporter) throws Exception{
        if (bamQCAnalysis) {
                addSummary( document, reporter );
            }
            addInputDesc( document, reporter );
            addPlots( document, reporter );
    }


    private void initDocument(Document document) throws  Exception{
// *********************************************
    	// Add the header of the PDF document
    	// *********************************************
    	com.lowagie.text.Image bioInfoLogo =
    		com.lowagie.text.Image.getInstance(
    				getClass().getResource(Constants.pathImages + "logo.png"));
    	bioInfoLogo.scaleAbsolute(
    			250,
    			25);
    	HeaderFooter header = new HeaderFooter(new Phrase(new Chunk(bioInfoLogo, 0, -15)), false);
    	header.setBorder(0);

    	com.lowagie.text.Image cipfLogo =
    		com.lowagie.text.Image.getInstance(
    				getClass().getResource(Constants.pathImages + "cipf_alpha.gif"));
    	cipfLogo.scaleAbsolute(50, 25);
    	header.getBefore().add(
                new Phrase(new Chunk(
                        cipfLogo,
                        document.getPageSize().getWidth() - bioInfoLogo.getScaledWidth() -
                                document.rightMargin() - cipfLogo.getWidth(),
                        -17)));
    	document.setHeader(header);

    	// *********************************************
    	// Add the footer on the PDF document
    	// *********************************************

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String dateLabel = dateFormat.format(date);

    	HeaderFooter footer = new HeaderFooter(new Phrase(""), true);
        footer.setAlignment(Element.ALIGN_RIGHT);
    	footer.setBorder(0);
        footer.getBefore().add("Page ");
        footer.setPageNumber(document.getPageNumber());


        document.setFooter(footer);


        // *********************************************
    	// Create the first page in the PDF report
    	// *********************************************
    	com.lowagie.text.Font font = new com.lowagie.text.Font(com.lowagie.text.Font.COURIER, 36, com.lowagie.text.Font.BOLD);
    	font.setColor(new Color(85, 107, 47));
    	Paragraph title = new Paragraph("");
    	title.setAlignment(Element.ALIGN_CENTER);
    	//addEmptyLine(paragraph, 12);
    	com.lowagie.text.Image pdfTitle =
    		com.lowagie.text.Image.getInstance(
    				getClass().getResource(Constants.pathImages + "pdf_title.png"));
    	pdfTitle.scaleAbsolute(400, 70);
    	pdfTitle.setAlignment(Element.ALIGN_MIDDLE);
    	title.add(pdfTitle);


         Paragraph analysisTitle = new Paragraph(tabProperties.getTypeAnalysis().toString() + " analysis",
                FontFactory.getFont(FontFactory.HELVETICA , 18,
                        Font.ITALIC));
         analysisTitle.setAlignment(Element.ALIGN_CENTER);

        Paragraph subTitle = new Paragraph("Generated by Qualimap " + NgsSmartMain.APP_VERSION,
                FontFactory.getFont(FontFactory.HELVETICA , 18,
                        Font.ITALIC));
        subTitle.setAlignment(Element.ALIGN_CENTER);

        Paragraph subTitle2 = new Paragraph(dateLabel,
                FontFactory.getFont(FontFactory.HELVETICA , 18,
                        Font.ITALIC));
        subTitle2.setAlignment(Element.ALIGN_CENTER);

    	document.add(title);
        document.add(analysisTitle);
        document.add(subTitle);
        document.add(subTitle2);


    }

    private void addPlots(Document document, BamQCRegionReporter reporter) throws Exception {
       for (QChart chart : reporter.getCharts() ){

            String chartTitle = chart.getTitle();

            BufferedImage bufImage;
            if (chart.isBufferedImage()) {
                bufImage = chart.getBufferedImage();
           } else {
                bufImage = chart.getJFreeChart().createBufferedImage(
                        Constants.GRAPHIC_TO_SAVE_WIDTH,
                        Constants.GRAPHIC_TO_SAVE_HEIGHT);
           }

            Paragraph chartsTitle = createChapterTitle(chartTitle + reporter.getNamePostfix());
            Chapter chapter = new Chapter(chartsTitle, curChapterNum);


            com.lowagie.text.Image image =
                    com.lowagie.text.Image.getInstance(
                            bufImage.getScaledInstance(
                                    bufImage.getWidth() /*Constants.GRAPHIC_TO_SAVE_WIDTH*/,
                                    bufImage.getHeight() /*(Constants.GRAPHIC_TO_SAVE_HEIGHT*/,
                                    Image.SCALE_REPLICATE), null);


            float scaledWidth = 400;
            float scaledHeight = 600;

            if (bufImage.getWidth() < bufImage.getHeight()) {
                scaledWidth = scaledHeight * ( (float) bufImage.getWidth() / bufImage.getHeight() );
            } else {
                scaledHeight = scaledWidth * ( (float) bufImage.getHeight() / bufImage.getTileWidth() );
            }

            image.scaleToFit(scaledWidth,scaledHeight);
            image.setAlignment(com.lowagie.text.Image.MIDDLE);
            chapter.add(image);

            document.add(chapter);
            curChapterNum++;
            increaseProgressBar(chartTitle);
        }

    }

    private void addInputDesc(Document doc, BamQCRegionReporter reporter) throws Exception {


        Paragraph chapterTitle = createChapterTitle("Input data & parameters" + reporter.getNamePostfix());

        Chapter inputChapter = new Chapter(chapterTitle, curChapterNum);

        List<StatsKeeper.Section> inputSections = reporter.getInputDataSections();

        for (StatsKeeper.Section s : inputSections ) {

            Paragraph title11 = createSectionTitle(s.getName());

            Section section = inputChapter.addSection(title11);
            Table table = new Table(2);
            table.setPadding(2);
            table.setSpacing(2);

            for (String[] row : s.getRows()) {
                table.addCell(row[0]);
                table.addCell(row[1]);
            }

            section.add(table);
        }

        doc.add(inputChapter);
        curChapterNum++;

    }



    private void addSummary(Document doc, BamQCRegionReporter reporter) throws Exception {


        Paragraph chapterTitle = createChapterTitle("Summary" + reporter.getNamePostfix());

        Chapter summaryChapter = new Chapter(chapterTitle, curChapterNum);

        List<StatsKeeper.Section> summarySections = reporter.getSummaryDataSections();

        for (StatsKeeper.Section s : summarySections ) {

            Paragraph title11 = createSectionTitle(s.getName());

            Section section = summaryChapter.addSection(title11);
            Table table = new Table(2);
            table.setPadding(2);
            table.setSpacing(2);

            for (String[] row : s.getRows()) {
                table.addCell(row[0]);
                table.addCell(row[1]);
            }

            section.add(table);
        }


        StatsKeeper chrStatsKeeper = reporter.getChromosomeStats();
        List<StatsKeeper.Section> chromosomeSections = chrStatsKeeper.getSections();
        Paragraph chrTitle = createSectionTitle(chrStatsKeeper.getName());
        Section chrSection = summaryChapter.addSection(chrTitle);

        Table chrTable = new Table(5);
        chrTable.setPadding(2);
        chrTable.setSpacing(2);

        for (StatsKeeper.Section s : chromosomeSections) {

            if (s.getName().equals(Constants.CHROMOSOME_STATS_HEADER)) {

                String row[] = s.getRows().get(0);
                for (String item : row) {
                    Cell c = new Cell(item);
                    c.setHeader(true);
                    chrTable.addCell(c);
                }

            } else {

            List<String[]> rows = s.getRows();
            for (String[] row : rows) {
                for (String item : row) {
                    chrTable.addCell(item);
                }
            }
            }
        }
        chrSection.add(chrTable);

        doc.add(summaryChapter);
        curChapterNum++;

    }


    private Paragraph createChapterTitle(String text) {
        return  new Paragraph(text,
                FontFactory.getFont(FontFactory.HELVETICA , 18,
                        Font.BOLD ));


    }

    private Paragraph createSectionTitle(String text) {
        return  new Paragraph(text,
                FontFactory.getFont(FontFactory.HELVETICA , 16,
                        Font.BOLD ));


    }


    /*
     * Increase the progress bar in the percent depends on the
     * number of the element computed.
     */
    private void increaseProgressBar(String fileName){

        if (!guiAvailable) {
            return;
        }

        double numElem = numSavedItems;

    	// Increase the number of files loaded
    	numSavedItems++;
    	// Increase the progress bar value
        int result = (int)Math.ceil(numElem * percentLoad);
    	savePanel.getProgressBar().setValue(result);

		if(fileName != null){
		    savePanel.getProgressStream().setText("Adding Graphic: "+ fileName);
		}
    }




}
