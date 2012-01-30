package org.bioinfo.ngs.qc.qualimap.gui.threads;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.text.html.parser.ParserDelegator;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.panels.SavePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.jfree.chart.JFreeChart;

import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Class to manage a thread thar save the loaded data into a pdf file
 * @author lcruz
 */
public class SavePdfThread extends Thread{
	/** Logger to print information */
	protected Logger logger;
	
	/** Variable to manage the panel with the progress bar to increase */
	private SavePanel savePanel;
	
	/** Variable to control that all the files are saved */
	private int numSavedFiles;
	
	/** Variable to control the percent of each iteration of the progress bar */
	private double percentLoad;
	
	/** Variable to manage the path of the file that we are going to save*/
	private String path;
	
	/** Variables that contains the tab properties loaded in the thread*/
	TabPropertiesVO tabProperties;

	
	public SavePdfThread(String str, Component component, TabPropertiesVO tabProperties, String path) {
        super(str);
        if (component instanceof SavePanel) {
        	this.savePanel = (SavePanel)component;
        }
        this.tabProperties = tabProperties;
        this.path = path;
    }
	
	/**
	 * Public method to run this thread. Its executed when an user call to method start
	 * over this thread. */
    public void run() {
		try {
			OutputStream file = new FileOutputStream(path);
			
			Document document = new Document(PageSize.A4.rotate());
			PdfWriter.getInstance(document, file);
			
			boolean loadInsideReporter = false, loadOutsideReporter = false;
			
			 // Show the ProgressBar and the Text Description
	    	savePanel.getProgressStream().setVisible(true);
	    	savePanel.getProgressBar().setVisible(true);
			
			// Set the number of files saved to initial value
	    	numSavedFiles = 0;
	    	
	    	// Number of items to save into the PDF file (graphics + 3 files of properties + Header + Footer)
	    	int numItemsToSave;
	    	boolean addStats = false;

            if  (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EXOME ||
                    tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_DNA ) {
                numItemsToSave = tabProperties.getReporter().getMapCharts().size() + 3;
                addStats = true;
            } else {
                numItemsToSave = tabProperties.getReporter().getImageMap().size() + 2;
            }


	    	/*if(tabProperties.getInsideReporter().getBamFileName() != null &&
					!tabProperties.getInsideReporter().getBamFileName().isEmpty()){
	    		loadInsideReporter = true;
	    		numItemsToSave += tabProperties.getInsideReporter().getMapCharts().size() + 1;
	    	}*/

	    	if(tabProperties.getOutsideReporter() != null &&
					!tabProperties.getOutsideReporter().getBamFileName().isEmpty()){
	    		loadOutsideReporter = true;
	    		numItemsToSave += tabProperties.getOutsideReporter().getMapCharts().size() + 1;
	    	}
	    						  
	    	percentLoad = (100.0/numItemsToSave);
			
			// Add the files of the first reporter
			BamQCRegionReporter reporter = tabProperties.getReporter();
			document.open();
			
			// Add the first Page of the PDF with the Header & Footer
			this.addInitialConfiguration(document);

			boolean success = addFilesToPdf(document, reporter, 1, addStats);

			// Add the files of the third reporter
			if(success && loadOutsideReporter){
				reporter = tabProperties.getOutsideReporter();
				success = addFilesToPdf(document, reporter, 2, true);
			}
 
            document.close();
            file.close();
            
            if(success){
				// Close the window and show an info message
				savePanel.getHomeFrame().getPopUpDialog().setVisible(false);
				savePanel.getHomeFrame().remove(savePanel.getHomeFrame().getPopUpDialog());
				JOptionPane.showMessageDialog(null,
						"Pdf File Created Successfully \n", "Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				// If the file could not generate correctly
				File f = new File(path);
				f.delete();
				savePanel.getProgressBar().setVisible(false);
				savePanel.getProgressStream().setVisible(false);
				JOptionPane.showMessageDialog(null,
						"Unable to create the pdf file \n", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			savePanel.getProgressBar().setVisible(false);
			savePanel.getProgressStream().setVisible(false);
            e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Unable to create the pdf file \n", "Error", JOptionPane.ERROR_MESSAGE);
		}
    }
    
    private void addInitialConfiguration(Document document) throws DocumentException, IOException{
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
    	increaseProgressBar(numSavedFiles, null, 0);
    	
    	// *********************************************
    	// Add the footer on the PDF document
    	// *********************************************
    	HeaderFooter footer = new HeaderFooter(new Phrase(""), true);
    	footer.setBorder(0);
    	footer.getBefore().add("Page ");
    	footer.setPageNumber(document.getPageNumber());

    	document.setFooter(footer);
    	increaseProgressBar(numSavedFiles, null, 1);
    	
    	// *********************************************
    	// Create the first page in the PDF report
    	// *********************************************
    	Font font = new Font(Font.COURIER, 36, Font.BOLD);
    	font.setColor(new Color(85, 107, 47));
    	Paragraph paragraph = new Paragraph("");
    	paragraph.setAlignment(Element.ALIGN_CENTER);
    	addEmptyLine(paragraph, 12);
    	com.lowagie.text.Image pdfTitle = 
    		com.lowagie.text.Image.getInstance(
    				getClass().getResource(Constants.pathImages + "pdf_title.png"));
    	pdfTitle.scaleAbsolute(400, 70);
    	pdfTitle.setAlignment(Element.ALIGN_MIDDLE);
    	paragraph.add(pdfTitle);

    	document.add(paragraph);
    }

    
    /**
     * Inserts the elements of the reporter into the PDF file created
     * @param document Object that contains the pdf file to write in.
     * @param reporter information to put in the zip file
     * @return boolean value to inform if there was an error putting the values
     * @throws DocumentException
     * @throws IOException 
     * @throws MalformedURLException 
     */
    private boolean addFilesToPdf(Document document, BamQCRegionReporter reporter, int numChapter, boolean saveStats)
    		throws DocumentException, MalformedURLException{
    	boolean result = true;
    	Iterator<?> it = saveStats ? reporter.getMapCharts().entrySet().iterator()
                : reporter.getImageMap().entrySet().iterator();
    	BufferedImage bufImage = null;
    	String fileName = null;
    	
    	try {
        	// Start a new page
        	document.newPage();
        	
        	// Create the new Chapter
    		Paragraph paragraph = null;
    		if(numChapter == 1){
    			paragraph = new Paragraph("Charts");
    		} else if (numChapter == 2){
    			paragraph = new Paragraph("Reads Outside Region");
    		}
    		Chapter chapter = new Chapter(paragraph, numChapter);
    		
    		// Generate the Output values
            int numberDepth = 2;
            if (saveStats) {
                addOutputsToPDF(reporter, chapter, numberDepth);
            }

            addInputDescriptionToPDF(reporter, chapter, numberDepth);

	    	// Generate the Graphics images
    	 	while(it.hasNext() && result){
				@SuppressWarnings("unchecked")
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
	
				fileName = entry.getKey();
				paragraph = new Paragraph(fileName);

				Section section = chapter.addSection(paragraph, numberDepth);

				if(entry.getValue() instanceof JFreeChart){
					bufImage = ((JFreeChart)entry.getValue()).createBufferedImage(
							Constants.GRAPHIC_TO_SAVE_WIDTH,
							Constants.GRAPHIC_TO_SAVE_HEIGHT);
				} else {
					bufImage = (BufferedImage)entry.getValue();
				}
				
		        com.lowagie.text.Image image =
					com.lowagie.text.Image.getInstance(
							bufImage.getScaledInstance(
								Constants.GRAPHIC_TO_SAVE_WIDTH,
								Constants.GRAPHIC_TO_SAVE_HEIGHT,
								Image.SCALE_REPLICATE), null);
				
				image.scaleAbsolute(560, 420);
				image.setAlignment(com.lowagie.text.Image.MIDDLE);
				section.add(image);
				
				increaseProgressBar(numSavedFiles, fileName);
				//numChapter++;
			}
			document.add(chapter);
    	} catch (IOException e) {
			result = false;
		}
        
        return result;
    }
    
    private void addOutputsToPDF(BamQCRegionReporter reporter, Chapter chapter, int numberDepth){
    	StringUtilsSwing sdf = new StringUtilsSwing();
    	
    	Paragraph paragraph = new Paragraph("Summary");
		Section section = chapter.addSection(paragraph, numberDepth);
		//section.setNumberDepth(2);
		addEmptyLine(paragraph, 2);

		
		// *********************************************
    	// Create the first table of 2 subtables
    	// *********************************************
		PdfPTable tableGlobal = new PdfPTable(2);
		tableGlobal.setWidthPercentage(100);
		
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(42);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		PdfPCell column;

		table.addCell("Number of Bases");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getBasesNumber())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of Reads");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getNumReads())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of mapped reads");
		column = new PdfPCell(new Phrase(sdf.formatInteger(reporter.getNumMappedReads())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of mapped bases");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getNumMappedBases())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of sequenced bases");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getNumSequencedBases())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of aligned bases");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getNumAlignedBases())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		tableGlobal.addCell(table);
		
		
		table = new PdfPTable(2);
		table.setWidthPercentage(42);
		
		table.addCell("Number of A's");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getaNumber())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of C's");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getcNumber())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);

		table.addCell("Number of T's");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.gettNumber())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of G's");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getgNumber())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Number of N's");
		column = new PdfPCell(new Phrase(sdf.formatLong(reporter.getnNumber())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);

		table.addCell("GC Percentage");
		column = new PdfPCell(new Phrase(sdf.formatPercentage(reporter.getGcPercent())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
//		table.addCell("AT Percentage");
//		column = new PdfPCell(new Phrase(sdf.formatPercentage(reporter.getAtPercent())));
//		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		table.addCell(column);

		tableGlobal.addCell(table);
		section.add(tableGlobal);
		
		
		// *********************************************
    	// Create the second table of 2 subtables
    	// *********************************************
		paragraph = new Paragraph("");
		addEmptyLine(paragraph, 1);
		section.add(paragraph);
		
		tableGlobal = new PdfPTable(2);
		tableGlobal.setWidthPercentage(100);
		
		table = new PdfPTable(2);
		table.setWidthPercentage(42);
		
		table.addCell("Mean Coverage");
		column = new PdfPCell(new Phrase(sdf.formatDecimal(reporter.getMeanCoverage())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		table.addCell("Std Percentage");
		column = new PdfPCell(new Phrase(sdf.formatDecimal(reporter.getStdCoverage())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		tableGlobal.addCell(table);
		
		table = new PdfPTable(2);
		table.setWidthPercentage(42);
		
		table.addCell("Mean Mapping Quality");
		column = new PdfPCell(new Phrase(sdf.formatDecimal(reporter.getMeanMappingQuality())));
		column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(column);
		
		tableGlobal.addCell(table);
		section.add(tableGlobal);
		section.newPage();
		
	}

    private void addInputDescriptionToPDF(BamQCRegionReporter reporter, Chapter chapter, int numberDepth){
    	StringUtilsSwing sdf = new StringUtilsSwing();

    	Paragraph paragraph = new Paragraph("Input data & parameters");
		Section section = chapter.addSection(paragraph, numberDepth);
		//section.setNumberDepth(2);
		addEmptyLine(paragraph, 1);

        List<BamQCRegionReporter.InputDataSection> inputDescr = reporter.getInputDataSections();

        for (BamQCRegionReporter.InputDataSection inputSection : inputDescr) {

            Paragraph tablePara = new Paragraph(inputSection.getName());
            section.addSection( tablePara, numberDepth + 1);

            addEmptyLine( tablePara, 2 );

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            for ( Map.Entry<String,String> entry : inputSection.getData().entrySet()  ) {
                table.addCell(entry.getKey());
                PdfPCell column = new PdfPCell(new Phrase(entry.getValue()));
		        column.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        table.addCell(column);
            }

            section.add(table);

		}

    }

    /**
     * Increase the progress bar in the percent depends on the
     * number of the element computed.
     * @param numElem number of the element computed
     */
    private void increaseProgressBar(double numElem, String fileName){
    	increaseProgressBar(numElem, fileName, 2);
    }
    
    /**
     * Increase the progress bar in the percent depends on the
     * number of the element computed.
     * @param numElem number of the element computed
     */
    private void increaseProgressBar(double numElem, String fileName, int type){
    	int result = 0;
    	
    	// Increase the number of files loaded
    	numSavedFiles++;
    	// Increase the progress bar value
    	result = (int)Math.ceil(numElem * percentLoad);
    	savePanel.getProgressBar().setValue(result);

		if(type == 0){
			savePanel.getProgressStream().setText("Adding the PDF Header");
		} else if(type == 1){
			savePanel.getProgressStream().setText("Adding the PDF Footer");
		} else {
			if(fileName != null){
				savePanel.getProgressStream().setText("Adding Graphic: "+ fileName);
			}
		}
    }

    /**
     * Add N white lines to the PDF  
     * @param paragraph
     * @param number
     */
    private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
}