package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

/**
 * Class that contains the information to put the paths for the input data
 * collection.
 * 
 * @author Luis Miguel Cruz
 */
public class OpenFilePanel extends JPanel {
	private static final long serialVersionUID = 1689369726484706085L;
	HomeFrame homeFrame;
	protected Logger logger;

	/** JDialog that contains the panel to load a file */
	JDialog resultContainer;
	JTextField pathDataFile, pathDataAditionalFile, pathFastaFile, pathCountFile2, name1, name2, pathInfoFile;
	JTextField valueNw, countThresHold;
	JCheckBox drawChromosomeLimits, saveCoverage, labelPathCountFile2, labelInfoFile, labelSpeciesFile;
	File inputFile, regionFile, fastaFile, infoFile;
	JButton pathFastaFileButton;
    JButton pathRegionFileButton;
    JButton pathCountFile2Button;
    JButton pathInfoFileButton;
    JButton startAnalysisButton;
	JLabel advancedInfo;
	JComboBox comboSpecies;

	/**
	 * Variable to manage the progress bar that shows the percent loaded at each
	 * moment
	 */
	JProgressBar progressBar;

	/**
	 * Variable to manage the text loaded at each moment that increases the
	 * progress bar
	 */
	JLabel progressStream;

	/**
	 * String to manage the information with the message to show if there is a
	 * error loading a file.
	 */
	StringBuilder stringValidacion;

	/**
	 * Variable to store if the graphics needs to be generated or are in a zip
	 * file
	 */
	private boolean loadFromZipFile;
	private String startButtontext = ">>>   Start analysis";

	public OpenFilePanel() {
		super();
		logger = new Logger(this.getClass().getName());
	}


	/**
	 * Create a panel that contains the information to set the input data path
	 * from the zip file.
	 * 
	 * @param homeFrame
	 *            HomeFrame reference that contains the reference to the wrapper
	 * @param dim
	 *            Dimension of the the Container that will contain the actual
	 *            panel
	 * @return JDialog, User Interface to set the input data path.
	 */
	public JDialog getOpenZipFilePanel(HomeFrame homeFrame, Dimension dim) {
		this.homeFrame = homeFrame;
		this.loadFromZipFile = true;

		resultContainer = new JDialog();
		resultContainer.pack();
		resultContainer.setSize(new Dimension(600, 180));
		resultContainer.setTitle("Open File (.zip)");
		resultContainer.setResizable(false);

		KeyListener keyListener = new PopupKeyListener(homeFrame, resultContainer, progressBar);

		GroupLayout thisLayout = new GroupLayout( resultContainer.getContentPane());
		resultContainer.getContentPane().setLayout(thisLayout);
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addGap(600));
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup().addGap(180));

		// Input Line of information (input data file)
		JLabel label = new JLabel();
		label.setText("Zip File");
		label.setSize(label.getPreferredSize());
		label.setLocation(Constants.marginLeftForElement, 40);
		resultContainer.add(label);

		pathDataFile = new JTextField();
		pathDataFile.setSize(440, Constants.elementHeight);
		pathDataFile.setLocation(label.getX() + label.getWidth() + Constants.marginLeftForSubElement, label.getY());
		pathDataFile.addKeyListener(keyListener);
		resultContainer.add(pathDataFile);

		JButton zipFileButton = new JButton();
		zipFileButton.setText("...");
		zipFileButton.setSize(zipFileButton.getPreferredSize());
		zipFileButton.setLocation(pathDataFile.getX() + pathDataFile.getWidth() + Constants.marginLeftForSubElement, pathDataFile.getY() - 3);
		zipFileButton.setAction(getActionLoadZipFile());
		zipFileButton.addKeyListener(keyListener);
		resultContainer.add(zipFileButton);

		// Progress Bar to show while the statistics graphics are loaded
		UIManager.put("ProgressBar.selectionBackground", Color.black);
		UIManager.put("ProgressBar.selectionForeground", Color.black);
		progressBar = new JProgressBar(0, 100);
		progressBar.setLocation(label.getX() + 30, label.getY() + Constants.marginTopForFirstElement + 20);
		progressBar.setVisible(false);
		progressBar.setSize(350, 20);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(true);
		progressBar.setForeground(new Color(244, 200, 120));
		resultContainer.add(progressBar);

		// Action done while the statistics graphics are loaded
		progressStream = new JLabel();
		progressStream.setLocation(progressBar.getX(), progressBar.getY() + progressBar.getHeight() + Constants.marginTopForElementSubMenu);
		progressStream.setVisible(false);
		progressStream.setSize(350, 20);
		resultContainer.add(progressStream);

		JButton loadStatisticsButton = new JButton();
		loadStatisticsButton.setText("Open file");
		loadStatisticsButton.setSize(200,25);
		loadStatisticsButton.setLocation(resultContainer.getWidth() - loadStatisticsButton.getWidth() - 20, resultContainer.getHeight() - loadStatisticsButton.getHeight() - 45);
		loadStatisticsButton.setAction(getActionLoadQualimapFromZipFile("Open file"));
		loadStatisticsButton.addKeyListener(keyListener);
		resultContainer.add(loadStatisticsButton);

		return resultContainer;
	}

	/**
	 * Test if the input date are correct or not.
	 * 
	 * @return boolean, true if the input data are correct.
	 */
	/*private boolean validateInput() {
		boolean validate = true;

		stringValidation = new StringBuilder();

		// Validation for the input data file
		if (pathDataFile.getText().isEmpty() || (inputFile = new File(pathDataFile.getText())) == null) {
			stringValidation.append(" • The path of the Input Data File is required \n");
		} else if (inputFile != null) {
			String mimeType = new MimetypesFileTypeMap().getContentType(inputFile);
			String extension = inputFile.getName().substring(inputFile.getName().lastIndexOf(".") + 1);
			if (mimeType == null || !extension.equalsIgnoreCase(Constants.FILE_EXTENSION_DATA_INPUT)) {
				stringValidation.append(" • Incorrect MimeType for the Input Data File (*.bam) \n");
			}
		} else {
			try {
				FileUtils.checkFile(inputFile);
			} catch (IOException e) {
				stringValidation.append(" • " + e.getMessage() + " \n");
			}
		}

		// Validation for the region file
		if (this.homeFrame.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
			if (!pathDataAditionalFile.getText().isEmpty() && (regionFile = new File(pathDataAditionalFile.getText())) != null) {
				String mimeType = new MimetypesFileTypeMap().getContentType(regionFile);
				String extension = regionFile.getName().substring(regionFile.getName().lastIndexOf(".") + 1);

				if (mimeType == null || !Constants.FILE_EXTENSION_REGION.containsKey(extension.toUpperCase())) {
					stringValidation.append(" • Incorrect MimeType for the Region Data File (*.gff) \n");
				}
			}
			if (regionFile == null) {
				stringValidation.append(" • Region Data File Is Required \n");
			} else {
				try {
					FileUtils.checkFile(regionFile);
				} catch (IOException e) {
					stringValidation.append(" • " + e.getMessage() + " \n");
				}
			}
		}

		// If we has get any error, we reset the invalidate flag
		if (stringValidation.length() > 0) {
			validate = false;
		}

		return validate;
	}*/


	/**
	 * Test if the input zip file is correct or not.
	 * 
	 * @return boolean, true if the input data file is a correct file.
	 */
	private boolean validateInputZipFile() {
		boolean validate = true;

		stringValidacion = new StringBuilder();

		// Validation for the input data file
		if (pathDataFile.getText().isEmpty() || (inputFile = new File(pathDataFile.getText())) == null) {
			stringValidacion.append(" • The path of the Input Data File is required \n");
		} else if (inputFile != null) {
			String mimeType = new MimetypesFileTypeMap().getContentType(inputFile);
			String extension = inputFile.getName().substring(inputFile.getName().lastIndexOf(".") + 1);
			if (mimeType == null || !extension.equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)) {
				stringValidacion.append(" • Incorrect MimeType for the Input Zip File (*.zip) \n");
			}
		}

		if (stringValidacion.length() > 0) {
			validate = false;
		}

		return validate;
	}

	/**
	 * Function that execute the quality map program an show the results form a
	 * zip file generated before
	 */
	private synchronized void loadZipFileStatistics(TabPropertiesVO tabProperties) {
		//GraphicsFromZipAnalysisThread t;
		//t = new GraphicsFromZipAnalysisThread("StatisticsZipProcessThread", this, tabProperties);
		//t.start();
	}


	// ***************************************************************************************
	// ************************************** LISTENERS
	// **************************************
	// ***************************************************************************************


	/**
	 * Action to load the input data file.
	 * 
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionLoadZipFile() {
		AbstractAction actionLoadFile = new AbstractAction("...", null) {
			private static final long serialVersionUID = -8111339366112980049L;

			public void actionPerformed(ActionEvent evt) {
			    JFileChooser fileChooser = HomeFrame.getFileChooser();
				FileFilter filter = new FileFilter() {
					public boolean accept(File fileShown) {
						boolean result = true;

						if (!fileShown.isDirectory() && !fileShown.getName().substring(fileShown.getName().lastIndexOf(".") + 1).equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)) {
							result = false;
						}

						return result;
					}

					public String getDescription() {
						return ("Zip Files (*.zip)");
					}
				};
				fileChooser.setFileFilter(filter);

				int valor = fileChooser.showOpenDialog(homeFrame.getCurrentInstance());

				if (valor == JFileChooser.APPROVE_OPTION) {
					pathDataFile.setText(fileChooser.getSelectedFile().getPath());
				}
			}
		};

		return actionLoadFile;
	}

	/**
	 * Action to load the additional input data file.
	 * 
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionEnableCountFile2() {
		AbstractAction actionLoadFile = new AbstractAction("Count file 2", null) {
			private static final long serialVersionUID = 7030077047232767919L;

			public void actionPerformed(ActionEvent evt) {
				if (((JCheckBox) evt.getSource()).isSelected()) {
					pathCountFile2.setEnabled(true);
					pathCountFile2Button.setEnabled(true);
					name2.setEnabled(true);
				} else {
					pathCountFile2.setEnabled(false);
					pathCountFile2Button.setEnabled(false);
					name2.setEnabled(false);
				}
			}
		};

		return actionLoadFile;
	}

	/**
	 * Action to load the additional input data file.
	 * 
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionEnableInfoFile() {
		AbstractAction actionLoadFile = new AbstractAction("Info File", null) {
			private static final long serialVersionUID = 7030077047232767919L;

			public void actionPerformed(ActionEvent evt) {
				if (((JCheckBox) evt.getSource()).isSelected()) {
					pathInfoFile.setEnabled(true);
					pathInfoFileButton.setEnabled(true);
					labelSpeciesFile.setSelected(false);
					comboSpecies.setEnabled(false);
				} else {
					pathInfoFile.setEnabled(false);
					pathInfoFileButton.setEnabled(false);
				}
			}
		};

		return actionLoadFile;
	}

	/**
	 * Action to load the additional input species file.
	 * 
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionEnableSpeciesFile() {
		AbstractAction actionLoadFile = new AbstractAction("Species", null) {
			private static final long serialVersionUID = 7030077047232767919L;

			public void actionPerformed(ActionEvent evt) {
				if (((JCheckBox) evt.getSource()).isSelected()) {
					comboSpecies.setEnabled(true);
					labelInfoFile.setSelected(false);
					pathInfoFile.setEnabled(false);
					pathInfoFileButton.setEnabled(false);
				} else {
					comboSpecies.setEnabled(false);
				}
			}
		};

		return actionLoadFile;
	}

    /**
	 * Action to load the qualimap from a zip file
	 *
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionLoadQualimapFromZipFile(String text) {
		AbstractAction actionLoadFile = new AbstractAction(text, null) {
			private static final long serialVersionUID = 8329832238125153187L;

			public void actionPerformed(ActionEvent evt) {
                // Laod Qualimap from zip file
                TabPropertiesVO tabProperties = new TabPropertiesVO();
				if (validateInputZipFile()) {
				    loadZipFileStatistics(tabProperties);
				} else {
				    JOptionPane.showMessageDialog(null, stringValidacion.toString(), "Error", 0);
			    }
			}
		};

		return actionLoadFile;
	}


	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS
	// **************************************
	// ******************************************************************************************
	public HomeFrame getHomeFrame() {
		return homeFrame;
	}

	public JLabel getProgressStream() {
		return progressStream;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public File getInputFile() {
		return inputFile;
	}

	public JTextField getPathDataFile() {
		return pathDataFile;
	}

}
