package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.SavePdfThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.SaveZipThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

/**
 * Class to generate the save panel to control the save options
 * for a file
 * @author Luis Miguel Cruz
 */
public class SavePanel extends javax.swing.JPanel {
	/** Serial Version ID */
	private static final long serialVersionUID = -5164084147407879120L;
	
	/** Object that contains the frame to wrap this panel */
	HomeFrame homeFrame;
	
	/** Logger to print information */
	protected Logger logger;
	
	/** JDialog that contains the panel to save a file */
	JDialog resultContainer;
	
	/** Progress bar to show the process state */
	JProgressBar progressBar;
	
	/** Variable to show the status progress while we are saving */
	JLabel progressStream;
	
	/** Variable that contains the directory where the system is going to save */
	JTextField pathDataDir, fileName;
	
	/** Variable to manage the message to show if any process fails */
	StringBuffer stringValidacion;
	
	
	public SavePanel() {
		super();
		logger = new Logger(this.getClass().getName());
	}
	
	/**
	 * Create a panel that contains the information to set the input data path to
	 * save the data into a file of the type specified.
	 * @param homeFrame HomeFrame reference that contains the reference to the wrapper
	 * @param fileType, String that contains the type of file to return.
	 * @return JDialog, User Interface to set information needed to save the file.
	 */
	public JDialog getSaveFilePanel(HomeFrame homeFrame, String fileType) {
		this.homeFrame = homeFrame;
		
		resultContainer = new JDialog();
		resultContainer.pack();
		resultContainer.setSize(new Dimension(650, 200));
		
		KeyListener keyListener = new PopupKeyListener(homeFrame, resultContainer, progressBar);
		
		if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)){
			resultContainer.setTitle("Save to Zip File");
		} else if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_PDF_FILE)){
			resultContainer.setTitle("Save to Pdf File");
		}
		resultContainer.setResizable(false);
		
		GroupLayout thisLayout = new GroupLayout((JComponent)resultContainer.getContentPane());
		resultContainer.getContentPane().setLayout(thisLayout);
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addGap(650));
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup().addGap(200));
		
		// Input Line of information (input folder)
		JLabel label = new JLabel();
		label.setText("Parent Folder");
		label.setSize(111, label.getPreferredSize().height);
		label.setLocation(Constants.marginLeftForElement,
				40);
		resultContainer.add(label);

		pathDataDir = new JTextField();
		pathDataDir.setSize(437,
				Constants.elementHeight);
		pathDataDir.setLocation(
				label.getX() + label.getWidth() + Constants.marginLeftForSubElement,
				label.getY());
		pathDataDir.addKeyListener(keyListener);
		resultContainer.add(pathDataDir);

		JButton pathDirButton = new JButton();
		pathDirButton.setText("...");
		pathDirButton.setSize(pathDirButton.getPreferredSize());
		pathDirButton.setLocation(
				pathDataDir.getX() + pathDataDir.getWidth() + Constants.marginLeftForSubElement,
				pathDataDir.getY() - 3);
		pathDirButton.setAction(getActionLoadOutputPath());
		pathDirButton.addKeyListener(keyListener);
		resultContainer.add(pathDirButton);
		
		// Input Line of information (input file name)
		label = new JLabel();
		if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)){
			label.setText("File Name (.zip)");
		} else if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_PDF_FILE)){
			label.setText("File Name (.pdf)");
		}
		label.setSize(111, label.getPreferredSize().height);
		label.setLocation(Constants.marginLeftForElement,
				pathDataDir.getY() + Constants.marginTopForElementI);
		resultContainer.add(label);

		fileName = new JTextField();
		fileName.setSize(437,
				Constants.elementHeight);
		fileName.setLocation(
				label.getX() + label.getWidth() + Constants.marginLeftForSubElement,
				label.getY());
		fileName.addKeyListener(keyListener);
		resultContainer.add(fileName);
		
		// Progress Bar to show while the statistics graphics are loaded
		UIManager.put("ProgressBar.selectionBackground", Color.black);
		UIManager.put("ProgressBar.selectionForeground", Color.black);
		progressBar = new JProgressBar(0, 100);
		progressBar.setLocation(
				label.getX() + 30,
				label.getY() + Constants.marginTopForFirstElement + 20);
		progressBar.setVisible(false);
		progressBar.setSize(350, 20);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(true);
		progressBar.setForeground(new Color(244, 200, 120));
		resultContainer.add(progressBar);
		
        // Action done while the statistics graphics are loaded
		progressStream = new JLabel();
		progressStream.setLocation(
				progressBar.getX(),
				progressBar.getY() + progressBar.getHeight() + Constants.marginTopForElementSubMenu);
		progressStream.setVisible(false);
		progressStream.setSize(350, 20);
		resultContainer.add(progressStream);
		
		JButton saveButton = new JButton();
		saveButton.setText("Save");
		saveButton.setSize(saveButton.getPreferredSize());
		saveButton.setLocation(
				resultContainer.getWidth() - saveButton.getWidth() - 20,
				resultContainer.getHeight() - saveButton.getHeight() - 50);
		if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)){
			saveButton.setAction(getActionSaveZip());
		} else if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_PDF_FILE)){
			saveButton.setAction(getActionSavePdf());
		}
		saveButton.addKeyListener(keyListener);
		resultContainer.add(saveButton);
		
		return resultContainer;
	}
	
	
	/**
	 * Test if the input date are correct or not.
	 * @return boolean, true if the input data are correct.
	 */
	private boolean validateInput() {
		boolean validate = true;

		stringValidacion = new StringBuffer();
		
		// Check the directory where we want to write
		if (pathDataDir.getText() == null || pathDataDir.getText().isEmpty()) {
			stringValidacion.append(" • The output folder path is required \n");
		} else {
			try {
				FileUtils.checkDirectory(pathDataDir.getText(), true);
			} catch (IOException e) {
				stringValidacion.append(" • " + e.getMessage() + " \n");
			}
		}
		
		// Check the file input name
		if(fileName.getText() == null || fileName.getText().isEmpty()){
			stringValidacion.append(" • The name of the Output Data File is required \n");
		}
		
		// Check the validation string
		if(stringValidacion.length() > 0){
			validate = false;
		}

		return validate;
	}
	
	/**
	 * Function that calls a thread that create the zip file with the data read in the selected tab
	 * and save it into the disk.
	 */
	private void createZipFile(String path){
		SaveZipThread t = new SaveZipThread("Save to Zip Thread", this,
			homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()), path); 
		
		t.start();
	}
	
	/**
	 * Function that calls a thread that create the pdf file with the data read in the selected tab
	 * and save it into the disk.
	 */
	private void createPdfFile(String path){
		TabPropertiesVO tabProperties = 
			this.homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		
		SavePdfThread t = 
			new SavePdfThread("Save to Pdf Thread", this,
				homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()), path);
		
		t.start();
	}
	
	
	
	// ***************************************************************************************
	// ************************************** LISTENERS **************************************
	// ***************************************************************************************
	/**
	 * Action to load the input directory.
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionLoadOutputPath() {
		AbstractAction actionLoadFile = new AbstractAction("...", null) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				if(homeFrame.getFileSaveChooser() == null){
					homeFrame.setFileSaveChooser(new JFileChooser());
				}
				homeFrame.getFileSaveChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
								
				int valor = homeFrame.getFileSaveChooser().showOpenDialog(homeFrame
						.getCurrentInstance());

				if (valor == JFileChooser.APPROVE_OPTION) {
					pathDataDir.setText(homeFrame.getFileSaveChooser().getSelectedFile()
							.getPath());
				}
				
				// Set the focus to the next element
				fileName.requestFocus();
			}
		};

		return actionLoadFile;
	}
	
	
	/**
	 * Action to save all the loaded data into a zip file.
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionSaveZip() {
		AbstractAction actionLoadFile = new AbstractAction("Save", null) {
			private static final long serialVersionUID = -6901271560142385333L;

			public void actionPerformed(ActionEvent evt) {
				if (validateInput()) {
					// Add the extension if necessary
					if(!fileName.getText().endsWith(".zip")){
						fileName.setText(fileName.getText() + ".zip");
					}
					
					// Try if the file exists
					String separator = pathDataDir.getText().endsWith("/")?"":"/";
					File file = new File(pathDataDir.getText() + separator + fileName.getText());
					
					// If the file doesn't exists or exits and the user want to replace it
					if (!file.exists() || (file.exists() && JOptionPane.showConfirmDialog(null,
							"The file " + file.getPath() + "already exists." +
							"Do you want to replace the existing file?", 
							"Confirm", JOptionPane.OK_OPTION) == 0)) {
						createZipFile(file.getAbsolutePath());
					}
				} else {
					JOptionPane.showMessageDialog(null,
						stringValidacion.toString(), "Error", 0);
				}
			}
		};

		return actionLoadFile;
	}
	
	/**
	 * Action to save all the loaded data into a pdf file.
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionSavePdf() {
		AbstractAction actionLoadFile = new AbstractAction("Save", null) {
			private static final long serialVersionUID = -6901271560142385333L;

			public void actionPerformed(ActionEvent evt) {
				if (validateInput()) {
					// Add the extension if necessary
					if(!fileName.getText().endsWith(".pdf")){
						fileName.setText(fileName.getText() + ".pdf");
					}
					
					// Try if the file exists
					String separator = pathDataDir.getText().endsWith("/")?"":"/";
					File file = new File(pathDataDir.getText() + separator + fileName.getText());
					
					// If the file doesn't exists or exits and the user want to replace it
					if (!file.exists() || (file.exists() && JOptionPane.showConfirmDialog(null,
							"The file " + file.getPath() + "already exists." +
							"Do you want to replace the existing file?", 
							"Confirm", JOptionPane.OK_OPTION) == 0)) {
						createPdfFile(file.getAbsolutePath());
					}
				} else {
					JOptionPane.showMessageDialog(null,
						stringValidacion.toString(), "Error", 0);
				}
			}
		};

		return actionLoadFile;
	}

	
	
	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS **************************************
	// ******************************************************************************************
	public HomeFrame getHomeFrame() {
		return homeFrame;
	}

	public void setHomeFrame(HomeFrame homeFrame) {
		this.homeFrame = homeFrame;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public JLabel getProgressStream() {
		return progressStream;
	}

	public void setProgressStream(JLabel progressStream) {
		this.progressStream = progressStream;
	}

	public JTextField getPathDataDir() {
		return pathDataDir;
	}

	public void setPathDataDir(JTextField pathDataDir) {
		this.pathDataDir = pathDataDir;
	}
		
}