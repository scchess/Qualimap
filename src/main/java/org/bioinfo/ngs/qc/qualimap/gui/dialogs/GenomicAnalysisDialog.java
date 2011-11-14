package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisRnaThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.JTextFieldLimit;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.ReferencePosition;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

public class GenomicAnalysisDialog extends JDialog implements ActionListener{
	private boolean loadFromZipFile;
	private HomeFrame homeFrame;
	private JProgressBar progressBar;
	private JTextField pathDataFile;
	private JTextField pathDataAditionalFile;
	private JButton pathRegionFileButton;
	private JCheckBox drawChromosomeLimits;
	private JLabel advancedInfo;
	private JTextField valueNw;
	private JCheckBox saveCoverage;
	private JLabel progressStream;
	private String startButtontext;
	private JCheckBox labelInfoFile;
	private JCheckBox labelSpeciesFile;
	private StringBuilder stringValidacion;
	private File inputFile;
	private File regionFile;


	public GenomicAnalysisDialog(HomeFrame homeFrame, Dimension dim, String title) {
		this.setTitle(title);
		this.homeFrame = homeFrame;
		this.loadFromZipFile = false;
		
		if (homeFrame.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			this.setSize(780,300);
			this.setPreferredSize(new Dimension(780,300));
		} else {
			this.setSize(780,265);
			this.setPreferredSize(new Dimension(780,265));
		}
		
		this.setResizable(false);

		ReferencePosition referencePosition = new ReferencePosition();
		KeyListener keyListener = new PopupKeyListener(homeFrame, this, progressBar);

		// Create the layout for the JDialog
		GroupLayout thisLayout = new GroupLayout((JComponent) this.getContentPane());
		this.getContentPane().setLayout(thisLayout);
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addGap(600));
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup().addGap(180));

		// Input Line of information (input data file)
		JLabel labelPathDataFile = new JLabel();
		labelPathDataFile.setText("Select BAM File");
		labelPathDataFile.setSize(Constants.labelInputElementWidth, Constants.elementHeight);
		labelPathDataFile.setLocation(Constants.marginLeftForElement, 40);
		// Set the reference position to draw the next item
		referencePosition.setComponent(labelPathDataFile);
		this.add(labelPathDataFile);

		pathDataFile = new JTextField();
		pathDataFile.setSize(Constants.fileInputNameLength, Constants.elementHeight);
		pathDataFile.setLocation(labelPathDataFile.getX() + labelPathDataFile.getWidth() + Constants.marginLeftForSubElement, labelPathDataFile.getY());
		pathDataFile.addKeyListener(keyListener);
		this.add(pathDataFile);

		JButton pathDataFileButton = new JButton();
		pathDataFileButton.setText("...");
		pathDataFileButton.setSize(pathDataFileButton.getPreferredSize().width, Constants.elementHeight);
		pathDataFileButton.setLocation(pathDataFile.getX() + pathDataFile.getWidth() + Constants.marginLeftForSubElement, labelPathDataFile.getY());
		pathDataFileButton.setActionCommand("actionLoadFile");
		pathDataFileButton.addActionListener(this);
		pathDataFileButton.addKeyListener(keyListener);
		this.add(pathDataFileButton);

		/*
		 * The gff file (region file) can only be available if we are doing the
		 * exome analysis
		 */
		if (homeFrame.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			// Input Line of information (region file information)
			JLabel labelPathAditionalDataFile = new JLabel("Region File (.gff)");
			labelPathAditionalDataFile.setSize(Constants.labelInputElementWidth, Constants.elementHeight);
			labelPathAditionalDataFile.setLocation(Constants.marginLeftForElement, referencePosition.getY() + Constants.marginTopForElementI);
			// Set the reference position to draw the next item
			referencePosition.setComponent(labelPathAditionalDataFile);
			this.add(labelPathAditionalDataFile);

			pathDataAditionalFile = new JTextField();
			pathDataAditionalFile.setSize(Constants.fileInputNameLength, Constants.elementHeight);
			pathDataAditionalFile.setLocation(labelPathAditionalDataFile.getX() + labelPathAditionalDataFile.getWidth() + Constants.marginLeftForSubElement, labelPathAditionalDataFile.getY());
			pathDataAditionalFile.addKeyListener(keyListener);
			this.add(pathDataAditionalFile);

			pathRegionFileButton = new JButton();
			pathRegionFileButton.setText("...");
			pathRegionFileButton.setSize(pathRegionFileButton.getPreferredSize().width, Constants.elementHeight);
			pathRegionFileButton.setLocation(pathDataAditionalFile.getX() + pathDataAditionalFile.getWidth() + Constants.marginLeftForSubElement, labelPathAditionalDataFile.getY());
			pathRegionFileButton.setActionCommand("actionLoadAdditionalFile");
			pathRegionFileButton.addActionListener(this);
			pathRegionFileButton.addKeyListener(keyListener);
			this.add(pathRegionFileButton);
		}

		// Input Line of information (draw chromosome check)
		drawChromosomeLimits = new JCheckBox("Draw Chromosome Limits");
		drawChromosomeLimits.setSize(drawChromosomeLimits.getPreferredSize().width + 5, Constants.elementHeight);
		drawChromosomeLimits.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		drawChromosomeLimits.setLocation(Constants.marginLeftForElement, referencePosition.getY() + Constants.marginTopForElementI);
		drawChromosomeLimits.addKeyListener(keyListener);
		// Set the reference position to draw the next item
		referencePosition.setComponent(drawChromosomeLimits);
		this.add(drawChromosomeLimits);

		// Input Line of information (check to show the advance info)
		JCheckBox labelEnableAdvancedInfo = new JCheckBox("Advanced Options");
		labelEnableAdvancedInfo.setSize(labelEnableAdvancedInfo.getPreferredSize().width + 10, Constants.elementHeight);
		labelEnableAdvancedInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		labelEnableAdvancedInfo.setLocation(Constants.marginLeftForElement, referencePosition.getY() + Constants.marginTopForElementI);
		labelEnableAdvancedInfo.setActionCommand("actionEnableAdvancedInfo");
		labelEnableAdvancedInfo.addActionListener(this);
		labelEnableAdvancedInfo.addKeyListener(keyListener);
		// Set the reference position to draw the next item
		referencePosition.setComponent(labelEnableAdvancedInfo);
		this.add(labelEnableAdvancedInfo);

		// Input Line of information for the advance info (num. of windows and
		// save coverageData item)
		advancedInfo = new JLabel();
		advancedInfo.setSize(265, 80);
		advancedInfo.setBorder(BorderFactory.createEtchedBorder());
		advancedInfo.setLocation(Constants.labelInputElementWidth + Constants.marginLeftForElementSubMenu, referencePosition.getY());
		advancedInfo.setVisible(false);
		advancedInfo.addKeyListener(keyListener);
		this.add(advancedInfo);

		JLabel labelNw = new JLabel();
		labelNw.setText("Number Of Windows");
		labelNw.setSize(Constants.labelInputElementWidth, Constants.elementHeight);
		labelNw.setLocation(10, Constants.marginTopForFirstElement);
		// Set the reference position to draw the next item
		referencePosition.setComponent(labelNw);
		advancedInfo.add(labelNw);

		valueNw = new JTextField();
		valueNw.setSize(60, Constants.elementHeight);
		valueNw.setDocument(new JTextFieldLimit(6, true));
		valueNw.setLocation(labelNw.getX() + labelNw.getWidth() + Constants.marginLeftForSubElement, labelNw.getY());
		valueNw.addKeyListener(keyListener);
		advancedInfo.add(valueNw);

		saveCoverage = new JCheckBox("Save Coverage per nucleotide");
		saveCoverage.setSize(saveCoverage.getPreferredSize().width + 5, Constants.elementHeight);
		saveCoverage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		saveCoverage.setLocation(referencePosition.getX(), referencePosition.getY() + Constants.marginTopForElementI);
		saveCoverage.addKeyListener(keyListener);
		advancedInfo.add(saveCoverage);

		// Progress Bar to show while the statistics graphics are loaded
		UIManager.put("ProgressBar.selectionBackground", Color.black);
		UIManager.put("ProgressBar.selectionForeground", Color.black);
		progressBar = new JProgressBar(0, 100);
		progressBar.setLocation(advancedInfo.getX() + advancedInfo.getWidth() + 10, advancedInfo.getY() + Constants.marginTopForFirstElement);
		progressBar.setVisible(false);
		progressBar.setSize(325, 20);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(true);
		// progressBar.setForeground(new Color(255, 228, 196));
		progressBar.setForeground(new Color(244, 200, 120));
		this.add(progressBar);

		// Action done while the statistics graphics are loaded
		progressStream = new JLabel();
		progressStream.setLocation(progressBar.getX(), progressBar.getY() + progressBar.getHeight() + Constants.marginTopForElementSubMenu);
		progressStream.setVisible(false);
		progressStream.setSize(progressBar.getWidth(), 20);
		this.add(progressStream);

		JButton loadInputDataInfo = new JButton();
		loadInputDataInfo.setText(startButtontext);
		loadInputDataInfo.setSize(200,25);
		loadInputDataInfo.setLocation(this.getWidth()-20-loadInputDataInfo.getWidth(), this.getHeight()-45-loadInputDataInfo.getHeight());
		loadInputDataInfo.setActionCommand("startButtontext");
		loadInputDataInfo.addActionListener(this);
		loadInputDataInfo.addKeyListener(keyListener);
		this.add(loadInputDataInfo);
		this.pack();
	}

	private void startAnalysis(){
		// We can load from file or from a BAM file
		TabPropertiesVO tabProperties = new TabPropertiesVO();
		if (loadFromZipFile) {
//			if (validateInputZipFile()) {
//				loadZipFileStatistics(tabProperties);
//			} else {
//				JOptionPane.showMessageDialog(null, stringValidacion.toString(), "Error", 0);
//			}
		} else {
			if (validateInput()) {
				// If the input has the required values, load the
				// results
				loadStatistics(tabProperties);
			} else {
				JOptionPane.showMessageDialog(null, stringValidacion.toString(), "Error", 0);
			}
		}
	}
	
	/**
	 * Test if the input date are correct or not.
	 * 
	 * @return boolean, true if the input data are correct.
	 */
	private boolean validateInput() {
		boolean validate = true;

		stringValidacion = new StringBuilder();

		// Validation for the input data file
		if (pathDataFile.getText().isEmpty() || (inputFile = new File(pathDataFile.getText())) == null) {
			stringValidacion.append(" • The path of the Input Data File is required \n");
		} else if (inputFile != null) {
			String mimeType = new MimetypesFileTypeMap().getContentType(inputFile);
			String extension = inputFile.getName().substring(inputFile.getName().lastIndexOf(".") + 1);
			if (mimeType == null || !extension.equalsIgnoreCase(Constants.FILE_EXTENSION_DATA_INPUT)) {
				stringValidacion.append(" • Incorrect MimeType for the Input Data File (*.bam) \n");
			}
		} else {
			try {
				FileUtils.checkFile(inputFile);
			} catch (IOException e) {
				stringValidacion.append(" • " + e.getMessage() + " \n");
			}
		}

		// Validation for the region file
		if (this.homeFrame.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			if (!pathDataAditionalFile.getText().isEmpty() && (regionFile = new File(pathDataAditionalFile.getText())) != null) {
				String mimeType = new MimetypesFileTypeMap().getContentType(regionFile);
				String extension = regionFile.getName().substring(regionFile.getName().lastIndexOf(".") + 1);

				if (mimeType == null || !Constants.FILE_EXTENSION_REGION.containsKey(extension.toUpperCase())) {
					stringValidacion.append(" • Incorrect MimeType for the Region Data File (*.gff) \n");
				}
			}
			if (regionFile == null) {
				stringValidacion.append(" • Region Data File Is Required \n");
			} else {
				try {
					FileUtils.checkFile(regionFile);
				} catch (IOException e) {
					stringValidacion.append(" • " + e.getMessage() + " \n");
				}
			}
		}

		// If we has get any error, we reset the invalidate flag
		if (stringValidacion.length() > 0) {
			validate = false;
		}

		return validate;
	}

	
	/**
	 * Function that execute the quality map program an show the results from
	 * the input data files
	 */
	private synchronized void loadStatistics(TabPropertiesVO tabProperties) {
		BamAnalysisThread t;
		tabProperties.setTypeAnalysis(homeFrame.getTypeAnalysis());
		t = new BamAnalysisThread("StatisticsAnalysisProcessThread", this, tabProperties);
		t.start();
	}
	
	private void loadFileBam(){
		if (homeFrame.getFileOpenChooser() == null) {
			homeFrame.setFileOpenChooser(new JFileChooser());
		}
		FileFilter filter = new FileFilter() {
			public boolean accept(File fileShown) {
				boolean result = true;

				if (!fileShown.isDirectory() && !fileShown.getName().substring(fileShown.getName().lastIndexOf(".") + 1).equalsIgnoreCase(Constants.FILE_EXTENSION_DATA_INPUT)) {
					result = false;
				}

				return result;
			}

			public String getDescription() {
				return ("Bam Files (*.bam)");
			}
		};
		homeFrame.getFileOpenChooser().setFileFilter(filter);

		int valor = homeFrame.getFileOpenChooser().showOpenDialog(homeFrame.getCurrentInstance());

		if (valor == JFileChooser.APPROVE_OPTION) {
			pathDataFile.setText(homeFrame.getFileOpenChooser().getSelectedFile().getPath());
		}
	}
	
	private void loadFileGff() {
		if (homeFrame.getFileOpenChooser() == null) {
			homeFrame.setFileOpenChooser(new JFileChooser());
		}
		FileFilter filter = new FileFilter() {
			public boolean accept(File fileShown) {
				boolean result = true;

				if (!fileShown.isDirectory() && !Constants.FILE_EXTENSION_REGION.containsKey(fileShown.getName().substring(fileShown.getName().lastIndexOf(".") + 1).toUpperCase())) {
					result = false;
				}

				return result;
			}

			public String getDescription() {
				return ("Region Files (*.gff)");
			}
		};
		homeFrame.getFileOpenChooser().setFileFilter(filter);
		int valor = homeFrame.getFileOpenChooser().showOpenDialog(homeFrame.getCurrentInstance());

		if (valor == JFileChooser.APPROVE_OPTION) {
			pathDataAditionalFile.setText(homeFrame.getFileOpenChooser().getSelectedFile().getPath());
		}
    }
	
	@Override
    public void actionPerformed(ActionEvent e) {
	   if(e.getActionCommand().equalsIgnoreCase("startButtontext")){
		   startAnalysis();
	   }else if(e.getActionCommand().equalsIgnoreCase("actionLoadFile")){
		   loadFileBam();
	   }else if(e.getActionCommand().equalsIgnoreCase("actionEnableAdvancedInfo")){
		   if (((JCheckBox) e.getSource()).isSelected()) {
				advancedInfo.setVisible(true);
			} else {
				advancedInfo.setVisible(false);
			}
	   }else if(e.getActionCommand().equalsIgnoreCase("actionLoadAdditionalFile")){
		   loadFileGff();
		   
	   }
    }
	
//	private synchronized void loadRnaStatistics(TabPropertiesVO tabProperties) {
//		BamAnalysisRnaThread t;
//		tabProperties.setTypeAnalysis(homeFrame.getTypeAnalysis());
//		tabProperties.getRnaAnalysisVO().setInfoFileIsSet(this.labelInfoFile.isSelected());
//		tabProperties.getRnaAnalysisVO().setSpecieFileIsSet(this.labelSpeciesFile.isSelected());
//		t = new BamAnalysisRnaThread("StatisticsRnaAnalysisProcessThread", this, tabProperties);
//		t.start();
//	}
}
