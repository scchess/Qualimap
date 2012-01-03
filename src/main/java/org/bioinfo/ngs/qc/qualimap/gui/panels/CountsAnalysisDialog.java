package org.bioinfo.ngs.qc.qualimap.gui.panels;

import com.sun.org.apache.bcel.internal.classfile.Constant;
import net.miginfocom.swing.MigLayout;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisRnaThread;
import org.bioinfo.ngs.qc.qualimap.gui.threads.CountsAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 12/21/11
 * Time: 3:34 PM
 */
public class CountsAnalysisDialog extends JDialog implements ActionListener {

    JButton startAnalysisButton, browseFileButton1, browseGffButton1, browseFileButton2, browseGffButton2;
    JTextField  filePathEdit1, gffPathEdit1,filePathEdit2, gffPathEdit2, thresholdEdit;
    JComboBox analysisTypeCombo1, analysisTypeCombo2, speciesCombo;
    JCheckBox compartativeAnalysisCheckBox;
    JProgressBar progressBar;
    JLabel fileLabel1, gffLabel1, fileLabel2, gffLabel2;
    JLabel comboLabel1, comboLabel2, thresholdLabel;
    JLabel progressStream;
    JTextField infoFileEdit;
    JButton browseInfoFileButton;
    JRadioButton infoFileButton, speciesButton;
    HomeFrame homeFrame;
    StringBuilder stringValidation;
    static final String INPUT_TYPE_BAM_FILE = "BAM file and GFF file";
    static final String INPUT_TYPE_COUNTS_FILE = "Precalculated file with counts";


    static class BrowseDataButtonActionListener implements ActionListener {
        JTextField pathEdit;
        HomeFrame homeFrame;
        JComboBox typeCombo;

        public BrowseDataButtonActionListener(HomeFrame homeFrame, JTextField field, JComboBox typeCombo) {
            this.homeFrame = homeFrame;
            this.pathEdit = field;
            this.typeCombo = typeCombo;
        }


        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (homeFrame.getFileOpenChooser() == null) {
                homeFrame.setFileOpenChooser(new JFileChooser());
            }
            FileFilter filter = new FileFilter() {
                public boolean accept(File fileShown) {

                    String extension = typeCombo.getSelectedItem().toString().equals(INPUT_TYPE_COUNTS_FILE)
                            ? "txt" : "bam";

                    return (fileShown.getName().endsWith(extension) || fileShown.isDirectory());

                }

                public String getDescription() {
                    return typeCombo.getSelectedItem().toString().equals(INPUT_TYPE_COUNTS_FILE)
                            ? "File with counts" : "BAM files";
                }
            };
            homeFrame.getFileOpenChooser().setFileFilter(filter);

            int valor = homeFrame.getFileOpenChooser().showOpenDialog(homeFrame.getCurrentInstance());

            if (valor == JFileChooser.APPROVE_OPTION) {
                pathEdit.setText(homeFrame.getFileOpenChooser().getSelectedFile().getPath());
            }
        }

    }

    static class BrowseButtonActionListener implements ActionListener {

        JTextField pathEdit;
        HomeFrame homeFrame;
        String description;
        String extention;

        public BrowseButtonActionListener(HomeFrame homeFrame, JTextField field, String description, String extention) {
            this.homeFrame = homeFrame;
            this.pathEdit = field;
            this.description = description;
            this.extention = extention;
        }



        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (homeFrame.getFileOpenChooser() == null) {
					homeFrame.setFileOpenChooser(new JFileChooser());
				}
				FileFilter filter = new FileFilter() {
					public boolean accept(File fileShown) {

                        return (fileShown.getName().endsWith(extention) || fileShown.isDirectory());

                    }

					public String getDescription() {
						return description;
					}
				};
				homeFrame.getFileOpenChooser().setFileFilter(filter);

				int valor = homeFrame.getFileOpenChooser().showOpenDialog(homeFrame.getCurrentInstance());

				if (valor == JFileChooser.APPROVE_OPTION) {
					pathEdit.setText(homeFrame.getFileOpenChooser().getSelectedFile().getPath());
				}
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        updateState();
    }


    public CountsAnalysisDialog(HomeFrame homeFrame) {
        this.homeFrame = homeFrame;

        KeyListener keyListener = new PopupKeyListener(homeFrame, this, progressBar);
        getContentPane().setLayout(new MigLayout("insets 20"));

        comboLabel1 = new JLabel("First sample input type:");
        add(comboLabel1, "");
        analysisTypeCombo1 = new JComboBox();
        analysisTypeCombo1.addItem(INPUT_TYPE_BAM_FILE);
        analysisTypeCombo1.addItem(INPUT_TYPE_COUNTS_FILE);
        analysisTypeCombo1.addActionListener(this);
        add(analysisTypeCombo1, "wrap");

        fileLabel1 = new JLabel();
        fileLabel1.setText("Input file:");
        add(fileLabel1, "");

        filePathEdit1 = new JTextField(40);
        filePathEdit1.addKeyListener(keyListener);
        filePathEdit1.setToolTipText("Path to BAM alignment file");
        add(filePathEdit1, "grow");

        browseFileButton1 = new JButton();
		//browseFileButton1.setAction(getActionLoadBamFile());
        browseFileButton1.setText("...");
		browseFileButton1.addKeyListener(keyListener);
        browseFileButton1.addActionListener( new BrowseDataButtonActionListener(homeFrame,
                filePathEdit1, analysisTypeCombo1));
        add(browseFileButton1, "align center, wrap");

        gffLabel1 = new JLabel();
        gffLabel1.setText("Input GFF file:");
        add(gffLabel1, "");

        gffPathEdit1 = new JTextField(40);
        gffPathEdit1.addKeyListener(keyListener);
        gffPathEdit1.setToolTipText("Path to regions file");
        add(gffPathEdit1, "grow");

        browseGffButton1 = new JButton();
		//browseFileButton1.setAction(getActionLoadBamFile());
        browseGffButton1.setText("...");
		browseGffButton1.addKeyListener(keyListener);
        browseGffButton1.addActionListener(new BrowseButtonActionListener(homeFrame,
                gffPathEdit1, "GFF files", "gff"));
        add(browseGffButton1, "align center, wrap");

        compartativeAnalysisCheckBox = new JCheckBox();
        compartativeAnalysisCheckBox.setText("Perform comparison with other sample");
        compartativeAnalysisCheckBox.addActionListener(this);
        add(compartativeAnalysisCheckBox, "wrap");

        comboLabel2 = new JLabel("Second sample input type:");
        add(comboLabel2, "");
        analysisTypeCombo2 = new JComboBox();
        analysisTypeCombo2.addItem(INPUT_TYPE_BAM_FILE);
        analysisTypeCombo2.addItem(INPUT_TYPE_COUNTS_FILE);
        analysisTypeCombo2.addActionListener(this);
        add(analysisTypeCombo2, "wrap");

        fileLabel2 = new JLabel();
        fileLabel2.setText("Input file:");
        add(fileLabel2, "");

        filePathEdit2 = new JTextField(40);
        filePathEdit2.addKeyListener(keyListener);
        add(filePathEdit2, "grow");

        browseFileButton2 = new JButton();
		browseFileButton2.setText("...");
		browseFileButton2.addKeyListener(keyListener);
        browseFileButton2.addActionListener(new BrowseDataButtonActionListener(homeFrame,
                filePathEdit2, analysisTypeCombo2));
        add(browseFileButton2, "align center, wrap");

        gffLabel2 = new JLabel();
        gffLabel2.setText("Input GFF file:");
        add(gffLabel2, "");

        gffPathEdit2 = new JTextField(40);
        gffPathEdit2.addKeyListener(keyListener);
        gffPathEdit2.setToolTipText("Path to regions file");
        add(gffPathEdit2, "grow");

        browseGffButton2 = new JButton();
		//browseFileButton1.setAction(getActionLoadBamFile());
        browseGffButton2.setText("...");
		browseGffButton2.addKeyListener(keyListener);
        browseGffButton2.addActionListener(new BrowseButtonActionListener(homeFrame,
                        gffPathEdit2, "GFF files (*.gff)", "gff"));
        add(browseGffButton2, "align center, wrap");

        thresholdLabel = new JLabel();
        thresholdLabel.setText("Threshold:");
        add(thresholdLabel, "");

        thresholdEdit = new JTextField(20);
        thresholdEdit.addKeyListener(keyListener);
        thresholdEdit.setText("5");
        thresholdEdit.setToolTipText("A feature is considered as detected if the corresponding number of counts is " +
                "greater than this count threshold.");
        thresholdEdit.setMaximumSize(new Dimension(120,100));
        add(thresholdEdit, "grow, wrap");

        infoFileButton = new JRadioButton("Info file:");
        infoFileButton.addActionListener(this);
        infoFileButton.setSelected(true);
        add(infoFileButton, "");

        infoFileEdit = new JTextField(10);
        infoFileEdit.setToolTipText("File containing the biological classification of features in the count files.");
        add(infoFileEdit, "grow");

        browseInfoFileButton = new JButton("...");
        browseInfoFileButton.addActionListener( new BrowseButtonActionListener(homeFrame,
                        infoFileEdit, "Species files", "txt"));

        add(browseInfoFileButton, "align center, wrap");

        speciesButton = new JRadioButton("Species: ");
        speciesButton.setSelected(false);
        speciesButton.addActionListener(this);
        add(speciesButton);

        ButtonGroup group = new ButtonGroup();
        group.add(infoFileButton);
        group.add(speciesButton);

        String[] speicesComboItems = { Constants.TYPE_COMBO_SPECIES_HUMAN, Constants.TYPE_COMBO_SPECIES_MOUSE };
        speciesCombo = new JComboBox(speicesComboItems);
        speciesButton.setToolTipText("If the Info File is not given by the user, Qualimap provides the Ensembl " +
                "biotype classification for certain species");
        add(speciesCombo, "grow, wrap 30px");

        // Action done while the statistics graphics are loaded
        progressStream = new JLabel();
        progressStream.setVisible(true);
        progressStream.setText("Status");
        add(progressStream, "align center");

        // Progress Bar to show while the statistics graphics are loaded
        UIManager.put("ProgressBar.selectionBackground", Color.black);
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setForeground(new Color(244, 200, 120));
        add(progressBar, "grow, wrap 30px");


        startAnalysisButton = new JButton();
        startAnalysisButton.addActionListener(getActionListenerRunAnalysis());
        startAnalysisButton.setText(">>> Run Analysis");
        startAnalysisButton.addKeyListener(keyListener);

        add(new JLabel(""), "span 2");
        add(startAnalysisButton, "wrap");

        pack();

        updateState();
        setTitle("Analyze feature counts");
        setResizable(false);

    }


    void updateState() {

        boolean secondFileIsEnabled = compartativeAnalysisCheckBox.isSelected();
        boolean bamFileModeIsEnabled1 = analysisTypeCombo1.getSelectedItem().toString().equals(INPUT_TYPE_BAM_FILE);
        boolean bamFileModeIsEnabled2 = analysisTypeCombo2.getSelectedItem().toString().equals(INPUT_TYPE_BAM_FILE);

        filePathEdit1.setToolTipText(bamFileModeIsEnabled1 ? "Path to BAM alignment file" : "Path to file with counts");
        gffLabel1.setEnabled(bamFileModeIsEnabled1);
        gffPathEdit1.setEnabled(bamFileModeIsEnabled1);
        browseGffButton1.setEnabled(bamFileModeIsEnabled1);
        comboLabel2.setEnabled(secondFileIsEnabled);
        fileLabel2.setEnabled(secondFileIsEnabled);
        filePathEdit2.setEnabled(secondFileIsEnabled);
        filePathEdit2.setToolTipText(bamFileModeIsEnabled2 ? "Path to BAM alignment file" : "Path to file with counts");
        analysisTypeCombo2.setEnabled(secondFileIsEnabled);
        browseFileButton2.setEnabled(secondFileIsEnabled);
        gffLabel2.setEnabled(secondFileIsEnabled && bamFileModeIsEnabled2);
        gffPathEdit2.setEnabled(secondFileIsEnabled && bamFileModeIsEnabled2);
        browseGffButton2.setEnabled(secondFileIsEnabled && bamFileModeIsEnabled2);

        infoFileEdit.setEnabled(infoFileButton.isSelected());
        browseInfoFileButton.setEnabled(infoFileButton.isSelected());

        speciesCombo.setEnabled(speciesButton.isSelected());

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

    public String getName1() {
        return "Sample 1";
    }

    public String getName2() {
        return "Sample 2";
    }


    public boolean firstSampleCountsPrecalculated() {
        return analysisTypeCombo1.getSelectedItem().toString().equals(INPUT_TYPE_COUNTS_FILE);
    }

    public boolean secondSampleCountsPrecalculated() {
        return analysisTypeCombo2.getSelectedItem().toString().equals(INPUT_TYPE_COUNTS_FILE);
    }


    public boolean infoFileIsProvided() {
        return infoFileButton.isSelected();
    }

    public String getSelectedSpecies() {
        String speciesName = speciesCombo.getSelectedItem().toString();

        if (speciesName.equals(Constants.TYPE_COMBO_SPECIES_HUMAN)) {
            return Constants.FILE_SPECIES_INFO_HUMAN;
        } else if (speciesName.equals(Constants.TYPE_COMBO_SPECIES_MOUSE)) {
            return Constants.FILE_SPECIES_INFO_MOUSE;
        } else {
            return "";
        }

    }

    public boolean secondSampleIsProvided() {
        return compartativeAnalysisCheckBox.isSelected();
    }

    public String getFirstSampleDataPath() {
        return filePathEdit1.getText();
    }

    public String getFirstSampleGffPath() {
        return gffPathEdit1.getText();
    }

    public String getSecondSampleGffPath() {
        return gffPathEdit2.getText();
    }


    public String getSecondSampleDataPath() {
        return filePathEdit2.getText();
    }

    public String getInfoFilePath() {
        return infoFileEdit.getText();
    }

    public String getInputDataName() {
        return "Data Analysis";
    }


    private ActionListener getActionListenerRunAnalysis() {

        final CountsAnalysisDialog dlg = this;

        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
				// We can load from file or from a BAM file
				TabPropertiesVO tabProperties = new TabPropertiesVO();

                if (validateInput()) {
                    tabProperties.setTypeAnalysis(homeFrame.getTypeAnalysis());
		            tabProperties.getRnaAnalysisVO().setInfoFileIsSet(infoFileIsProvided());

		            CountsAnalysisThread t = new CountsAnalysisThread("StatisticsRnaAnalysisProcessThread", dlg, tabProperties);
		            t.start();

                } else {
				    JOptionPane.showMessageDialog(null, stringValidation.toString(), "Error", 0);
				}
			}

        };

    }

    boolean validateInputFile(String pathToFile, String fileType, boolean  checkForMimeType) {
        File inputFile;

        if (pathToFile.isEmpty() || (inputFile = new File(pathToFile)) == null) {
			stringValidation.append(" • The path of the ").append(fileType).append(" is required \n");
            return false;
		} else if (inputFile != null) {
            if (checkForMimeType) {
			    String mimeType = new MimetypesFileTypeMap().getContentType(inputFile);
			    if (mimeType == null) {
				    stringValidation.append(" • Incorrect MimeType for the ").append(fileType).append("\n");
			        return false;
                }
            }
            return true;
		} else {
			try {
				FileUtils.checkFile(inputFile);
			} catch (IOException e) {
				stringValidation.append(" • ").append(e.getMessage()).append(" \n");
			}
            return false;
		}

    }


    boolean validateInput() {

        boolean validate = true;

		stringValidation = new StringBuilder();

		// Validation for the first data file
        if (!validateInputFile(filePathEdit1.getText(), "Input File 1",  true)) {
            return false;
        }

        if (!firstSampleCountsPrecalculated()) {
            if (!validateInputFile(gffPathEdit1.getText(), "GFF File 1",  true))  {
                return false;
            }
        }

		// Validation for the second data file
		if (secondSampleIsProvided()) {
            if (!validateInputFile(filePathEdit2.getText(), "Input File 2",  true)) {
                return false;
            }
            if (!secondSampleCountsPrecalculated()) {
                if (!validateInputFile(gffPathEdit2.getText(), "GFF File 2",  true)) {
                    return false;
                }
            }
		}

		// the name of the 2 experiments must be different
		//TODO: add names
		/*if (name1.getText() != null && name1.getText().length() > 0 && name2.getText() != null && name2.getText().length() > 0 && name1.getText().equalsIgnoreCase(name2.getText())) {
			stringValidation.append(" • Name 1 and Name 2 must be different \n");
		}*/

        String thresholdEditText = thresholdEdit.getText();

		if (thresholdEditText == null || thresholdEditText.length() < 1) {
			stringValidation.append(" • Count threshold must be a number > 0 \n");
		} else {
			Integer i = Integer.parseInt(thresholdEditText);
			if (i < 1) {
				stringValidation.append(" • Count threshold must be a number > 0 \n");
			}
		}

		// Validation for the region file
		if (infoFileButton.isSelected() && !validateInputFile(infoFileEdit.getText(), "Info File",  true)) {
            return false;
        }

		// If we has get any error, we reset the invalidate flag
		if (stringValidation.length() > 0) {
			validate = false;
		}

		return validate;
    }

    public void setUiEnabled(boolean enabled) {

        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            component.setEnabled(enabled);
        }


    }



}
