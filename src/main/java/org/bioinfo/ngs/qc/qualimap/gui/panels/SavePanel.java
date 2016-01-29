/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportHtmlThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.ExportPdfThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;

/**
 * Class to generate the save panel to control the save options
 * for a file.
 * @author Luis Miguel Cruz
 * @author kokonech
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

    public JDialog getExportToPdfDialog(HomeFrame homeFrame) {
        this.homeFrame = homeFrame;

        resultContainer = new JDialog();
        resultContainer.setLayout( new MigLayout("insets 20"));

        KeyListener keyListener = new PopupKeyListener(homeFrame, resultContainer, progressBar);
        resultContainer.add(new JLabel("Path:"));

        final String fileType = Constants.FILE_EXTENSION_PDF_FILE;

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
        saveButton.setAction(getActionSavePdf());
        saveButton.addKeyListener(keyListener);
        resultContainer.add(saveButton, "span, align right");
        resultContainer.setTitle("Export to PDF");
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
	 * Function that calls a thread that create the pdf file with the data read in the selected tab
	 * and save it into the disk.
     * @param path Output PDF path
     */
	private void exportToPdf(String path){
		TabPageController tabController = homeFrame.getSelectedTabPageController();

		ExportPdfThread t =
			new ExportPdfThread(this, tabController, path);

		t.start();
	}

	
	private void exportToHtml(String dirPath) {
        TabPageController tabController = homeFrame.getSelectedTabPageController();

        ExportHtmlThread t =
                new ExportHtmlThread(this, tabController, dirPath);

        t.start();
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