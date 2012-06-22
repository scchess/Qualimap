package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportHtmlThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportPdfThread;
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
	
	/** Variable that contains the path to saved file */
	JTextField pathDataDir;
	
	/** Variable to manage the message to show if any process fails */
	StringBuffer stringValidation;
	
	
	public SavePanel() {
		super();
		logger = new Logger(this.getClass().getName());
	}


    static class SaveFileButtonActionListener implements ActionListener {

        JTextField pathTextField;
        String fileType;
        String ext;
        Component parent;

        public SaveFileButtonActionListener(Component parent, JTextField pathTextField, String fileType, String ext) {
            this.pathTextField = pathTextField;
            this.parent = parent;
            this.fileType = fileType;
            this.ext = ext;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fileChooser = HomeFrame.getFileChooser();
            fileChooser.setFileFilter( new FileNameExtensionFilter(fileType,  ext));

            int res = fileChooser.showSaveDialog(parent);

            if (res == JFileChooser.APPROVE_OPTION) {
                pathTextField.setText(fileChooser.getSelectedFile()
                        .getPath());
            }

        }

    }

    /**
         * Create a panel that contains the information to set the input data path to
         * save the data into a file of the type specified.
         * @param homeFrame HomeFrame reference that contains the reference to the wrapper
         * @param fileType, String that contains the type of file to return.
         * @return JDialog, User Interface to set information needed to save the file.
         */
    public JDialog getSaveFileDialog(HomeFrame homeFrame, String fileType) {
        this.homeFrame = homeFrame;

        resultContainer = new JDialog();
        resultContainer.setLayout( new MigLayout("insets 20"));

        KeyListener keyListener = new PopupKeyListener(homeFrame, resultContainer, progressBar);
        resultContainer.add(new JLabel("Path:"));

        pathDataDir = new JTextField(40);
        pathDataDir.addKeyListener(keyListener);
        pathDataDir.setText(new File("").getAbsolutePath() + File.separator +
                "qualimapReport." + fileType.toLowerCase() );
        resultContainer.add(pathDataDir, "grow");

        JButton pathDirButton = new JButton();
        pathDirButton.setText("...");
        pathDirButton.addActionListener(
                new SaveFileButtonActionListener(this,pathDataDir, fileType, fileType));
        pathDirButton.addKeyListener(keyListener);
        resultContainer.add(pathDirButton, "wrap");

        // Progress Bar to show while the statistics graphics are loaded
        UIManager.put("ProgressBar.selectionBackground", Color.black);
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setForeground(new Color(244, 200, 120));

        // Action done while the statistics graphics are loaded
        progressStream = new JLabel();
        progressStream.setVisible(false);

        resultContainer.add(progressStream);
        resultContainer.add(progressBar, "span 2, wrap");

        JButton saveButton = new JButton();
        saveButton.setText("Save");
        if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)){
            saveButton.setAction(getActionSaveZip());
        } else if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_PDF_FILE)){
            saveButton.setAction(getActionSavePdf());
        }
        saveButton.addKeyListener(keyListener);
        resultContainer.add(saveButton, "span, align right");
        if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_COMPRESS_FILE)){
            resultContainer.setTitle("Save to Zip");
        } else if(fileType.equalsIgnoreCase(Constants.FILE_EXTENSION_PDF_FILE)){
            resultContainer.setTitle("Export to PDF");
        }
        resultContainer.setResizable(false);

        resultContainer.pack();

        return resultContainer;
    }



    public JDialog getExportToHtmlFilePanel(final HomeFrame homeFrame) {

        this.homeFrame = homeFrame;

		resultContainer = new JDialog();

        KeyListener keyListener = new PopupKeyListener(homeFrame, resultContainer, progressBar);

        resultContainer.getContentPane().setLayout( new MigLayout("insets 20") );

		// Input folder
		JLabel label = new JLabel();
		label.setText("Output folder");
		resultContainer.add(label);

		pathDataDir = new JTextField(40);
		pathDataDir.addKeyListener(keyListener);
		resultContainer.add(pathDataDir, "grow");

		JButton pathDirButton = new JButton();
		pathDirButton.setText("...");
		pathDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = HomeFrame.getFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


				int valor = fileChooser.showOpenDialog(homeFrame
						.getCurrentInstance());

				if (valor == JFileChooser.APPROVE_OPTION) {
					pathDataDir.setText(fileChooser.getSelectedFile()
							.getPath());
				}
            }
        });
		pathDirButton.addKeyListener(keyListener);
		resultContainer.add(pathDirButton, "wrap");

        // Action done while the statistics graphics are loaded
        progressStream = new JLabel();
        progressStream.setVisible(false);
        resultContainer.add(progressStream);

        // Progress Bar to show while the statistics graphics are loaded
		UIManager.put("ProgressBar.selectionBackground", Color.black);
		UIManager.put("ProgressBar.selectionForeground", Color.black);
		progressBar = new JProgressBar(0, 100);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(true);
		progressBar.setForeground(new Color(244, 200, 120));
		resultContainer.add(progressBar, "span 2, align center, wrap");

		JButton saveButton = new JButton();
		saveButton.setText("Save");

        saveButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if (pathDataDir.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(resultContainer,
                            "Output folder is not set!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Try if the file exists
                File file = new File(pathDataDir.getText());

                if (file.exists() && !file.isDirectory())  {
                    JOptionPane.showMessageDialog(resultContainer,
                            "Invalid output folder path: target is not a directory!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // If the file doesn't exists or exits and the user want to replace it
                exportToHtml(file.getAbsolutePath());
            }
        });

        saveButton.addKeyListener(keyListener);
		resultContainer.add(saveButton, "span, align right, wrap");

        resultContainer.setTitle("Export to html");
        resultContainer.pack();
		resultContainer.setResizable(false);

		return resultContainer;
	}
	
	
	/**
	 * Test if the input date are correct or not.
	 * @return boolean, true if the input data are correct.
	 */
	private boolean validateInput() {

		stringValidation = new StringBuffer();
		
		// Check the directory where we want to write
		if (pathDataDir.getText().isEmpty()) {
			stringValidation.append(" • The output path is required \n");
		}

		return stringValidation.length() == 0;
	}
	
	/**
	 * Function that calls a thread that create the zip file with the data read in the selected tab
	 * and save it into the disk.
     * @param path Output ZIP path
     */
	private void createZipFile(String path){
		//SaveZipThread t = new SaveZipThread("Save to Zip Thread", this,
		//	homeFrame.getSelectedTabPropertiesVO(), path);
		
		//t.start();
	}


/**
	 * Function that calls a thread that create the pdf file with the data read in the selected tab
	 * and save it into the disk.
     * @param path Output PDF path
     */
	private void exportToPdf(String path){
		TabPropertiesVO tabProperties = homeFrame.getSelectedTabPropertiesVO();

		ExportPdfThread t =
			new ExportPdfThread("Export to Pdf Thread", this, tabProperties, path);

		t.start();
	}

	
	private void exportToHtml(String dirPath) {
        TabPropertiesVO tabPropertiesVO = homeFrame.getSelectedTabPropertiesVO();

        ExportHtmlThread t =
                new ExportHtmlThread("Export to Html Thread", this, tabPropertiesVO, dirPath);

        t.start();
    }

	
	// ***************************************************************************************
	// ************************************** LISTENERS **************************************
	// ***************************************************************************************

	/**
	 * Action to save all the loaded data into a zip file.
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionSaveZip() {
		return new AbstractAction("Save", null) {
			private static final long serialVersionUID = -6901271560142385333L;

			public void actionPerformed(ActionEvent evt) {
				if (validateInput()) {
					// Add the extension if necessary
					if(!pathDataDir.getText().endsWith(".zip")){
						pathDataDir.setText(pathDataDir.getText() + ".zip");
					}

					// Try if the file exists
					File file = new File(pathDataDir.getText());
					
					// If the file doesn't exists or exits and the user want to replace it
					if (!file.exists() || (file.exists() && JOptionPane.showConfirmDialog(null,
							"The file " + file.getPath() + " already exists." +
							"Do you want to replace the existing file?", 
							"Confirm", JOptionPane.OK_OPTION) == 0)) {
						createZipFile(file.getAbsolutePath());
					}
				} else {
					JOptionPane.showMessageDialog(null,
						stringValidation.toString(), "Error", 0);
				}
			}
		};

	}
	
	/**
	 * Action to save all the loaded data into a pdf file.
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionSavePdf() {
		return new AbstractAction("Save", null) {
			private static final long serialVersionUID = -6901271560142385333L;

			public void actionPerformed(ActionEvent evt) {
				if (validateInput()) {
					// Add the extension if necessary
					if(!pathDataDir.getText().endsWith(".pdf")){
						pathDataDir.setText(pathDataDir.getText() + ".pdf");
					}
					
					// Try if the file exists
					File file = new File(pathDataDir.getText());
					
					// If the file doesn't exists or exits and the user want to replace it
					if (!file.exists() || (file.exists() && JOptionPane.showConfirmDialog(null,
							"The file " + file.getPath() + " already exists." +
							"Do you want to replace the existing file?", 
							"Confirm", JOptionPane.OK_OPTION) == 0)) {
						//createPdfFile(file.getAbsolutePath());
                        exportToPdf(file.getAbsolutePath());
					}
				} else {
					JOptionPane.showMessageDialog(null,
						stringValidation.toString(), "Error", 0);
				}
			}
		};

	}

	
	
	// ******************************************************************************************
	// ********************************* GETTERS / SETTERS **************************************
	// ******************************************************************************************
	public HomeFrame getHomeFrame() {
		return homeFrame;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getProgressStream() {
		return progressStream;
	}


		
}