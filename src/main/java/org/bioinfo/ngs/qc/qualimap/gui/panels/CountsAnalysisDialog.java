package org.bioinfo.ngs.qc.qualimap.gui.panels;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.BrowseButtonActionListener;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.CountsAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 12/21/11
 * Time: 3:34 PM
 */
public class CountsAnalysisDialog extends JDialog implements ActionListener {

    JButton startAnalysisButton, browseFileButton1, browseFileButton2;
    JTextField  filePathEdit1, filePathEdit2, thresholdEdit;
    JTextField sample1NameEdit, sample2NameEdit;
    JComboBox speciesCombo;
    JCheckBox compartativeAnalysisCheckBox;
    JProgressBar progressBar;
    JLabel fileLabel2, sample2NameLabel, thresholdLabel, progressStream;
    JTextField infoFileEdit;
    JButton browseInfoFileButton, calcCountsButton;
    JRadioButton infoFileButton, speciesButton;
    HomeFrame homeFrame;
    StringBuilder stringValidation;
    JTextArea logArea;

    static final String INPUT_FILE_TOOLTIP = "To compute feature counts from BAM file and GFF file " +
            "use menu item Tools->Compute Counts or button below.";

    static final String INFO_FILE_TOOLTIP = "File containing the biological classification of features in the count files.";

    static final String SPECIES_ITEM_TOOLTIP = "If the Info File is not given by the user, " +
            "Qualimap provides the Ensemble biotype classification for certain species";

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        updateState();
    }


    public CountsAnalysisDialog(HomeFrame homeFrame) {
        this.homeFrame = homeFrame;

        KeyListener keyListener = new PopupKeyListener(homeFrame, this, progressBar);
        getContentPane().setLayout(new MigLayout("insets 20"));

        add(new JLabel("First sample name:"));
        sample1NameEdit = new JTextField(20);
        sample1NameEdit.setText("Sample 1");
        add(sample1NameEdit, "wrap");

        JLabel fileLabel1 = new JLabel("First sample (counts):");
        add(fileLabel1, "");

        filePathEdit1 = new JTextField(40);
        filePathEdit1.addKeyListener(keyListener);
        filePathEdit1.setToolTipText(INPUT_FILE_TOOLTIP);
        add(filePathEdit1, "grow");

        browseFileButton1 = new JButton();
		browseFileButton1.setText("...");
		browseFileButton1.addKeyListener(keyListener);
        browseFileButton1.addActionListener( new BrowseButtonActionListener(this,
                filePathEdit1, "File with counts"));
        add(browseFileButton1, "align center, wrap");

        compartativeAnalysisCheckBox = new JCheckBox();
        compartativeAnalysisCheckBox.setText("Compare with other sample");
        compartativeAnalysisCheckBox.addActionListener(this);
        add(compartativeAnalysisCheckBox, "wrap");

        sample2NameLabel =  new JLabel("Second sample name:");
        add(sample2NameLabel);
        sample2NameEdit = new JTextField(20);
        sample2NameEdit.setText("Sample 2");
        add(sample2NameEdit, "wrap");


        fileLabel2 = new JLabel("Second sample (counts):");
        add(fileLabel2, "");

        filePathEdit2 = new JTextField(40);
        filePathEdit2.addKeyListener(keyListener);
        filePathEdit2.setToolTipText(INPUT_FILE_TOOLTIP);
        add(filePathEdit2, "grow");

        browseFileButton2 = new JButton();
		browseFileButton2.setText("...");
		browseFileButton2.addKeyListener(keyListener);
        browseFileButton2.addActionListener(new BrowseButtonActionListener(homeFrame,
                filePathEdit2, "File with counts"));
        add(browseFileButton2, "align center, wrap");

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
        infoFileButton.setToolTipText(INFO_FILE_TOOLTIP);
        add(infoFileButton, "");

        infoFileEdit = new JTextField(10);
        infoFileEdit.setToolTipText(INFO_FILE_TOOLTIP);
        add(infoFileEdit, "grow");

        browseInfoFileButton = new JButton("...");
        browseInfoFileButton.addActionListener( new BrowseButtonActionListener(homeFrame,
                        infoFileEdit, "Species files", "txt"));

        add(browseInfoFileButton, "align center, wrap");

        speciesButton = new JRadioButton("Species: ");
        speciesButton.setSelected(false);
        speciesButton.addActionListener(this);
        speciesButton.setToolTipText(SPECIES_ITEM_TOOLTIP);
        add(speciesButton);

        ButtonGroup group = new ButtonGroup();
        group.add(infoFileButton);
        group.add(speciesButton);

        String[] speicesComboItems = { Constants.TYPE_COMBO_SPECIES_HUMAN, Constants.TYPE_COMBO_SPECIES_MOUSE };
        speciesCombo = new JComboBox(speicesComboItems);
        speciesCombo.setToolTipText(SPECIES_ITEM_TOOLTIP);
        add(speciesCombo, "grow, wrap 30px");

        /*
        TODO: provide better logging
        add(new JLabel("Log"), "wrap");
        logArea = new JTextArea(10,40);
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(logArea);
        add(scrollPane, "span, grow, wrap 30px");
        */

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


        calcCountsButton = new JButton("Compute counts...");
        final Component frame = this;
        calcCountsButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                HomeFrame.showCountReadsDialog(frame);
            }
        });
        add(calcCountsButton);

        startAnalysisButton = new JButton();
        startAnalysisButton.addActionListener(getActionListenerRunAnalysis());
        startAnalysisButton.setText(">>> Run Analysis");
        startAnalysisButton.addKeyListener(keyListener);

        add(startAnalysisButton, "span2, align right, wrap");

        pack();

        updateState();
        setTitle("Analyze feature counts");
        setResizable(false);

    }


    void updateState() {

        boolean secondFileIsEnabled = compartativeAnalysisCheckBox.isSelected();

        fileLabel2.setEnabled(secondFileIsEnabled);
        filePathEdit2.setEnabled(secondFileIsEnabled);
        sample2NameLabel.setEnabled(secondFileIsEnabled);
        sample2NameEdit.setEnabled(secondFileIsEnabled);
        browseFileButton2.setEnabled(secondFileIsEnabled);

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
        return sample1NameEdit.getText();
    }

    public String getName2() {
        return sample2NameEdit.getText();
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
                    tabProperties.setTypeAnalysis(Constants.TYPE_BAM_ANALYSIS_RNA);
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

        if (pathToFile.isEmpty()) {
			stringValidation.append(" • The path of the ").append(fileType).append(" is required \n");
            return false;
		} else  {
            File inputFile = new File(pathToFile);
            try {
				FileUtils.checkFile(inputFile);
			} catch (IOException e) {
				stringValidation.append(" • ").append(e.getMessage()).append(" \n");
			}
            if (checkForMimeType) {
			    String mimeType = new MimetypesFileTypeMap().getContentType(inputFile);
			    if (mimeType == null) {
				    stringValidation.append(" • Incorrect MimeType for the ").append(fileType).append("\n");
			        return false;
                }
            }
            return true;
		}
    }

    boolean validateInput() {

        boolean validate = true;

		stringValidation = new StringBuilder();

		if (sample1NameEdit.getText().isEmpty()) {
            stringValidation.append("First sample name is empty!");
            return false;
        }
		// Validation for the first data file
        if (!validateInputFile(filePathEdit1.getText(), "Input File 1",  true)) {
            return false;
        }

		// Validation for the second data file
		if (secondSampleIsProvided()) {
            if (sample2NameEdit.getText().isEmpty()) {
                stringValidation.append("Second sample name is empty!");
                return false;
            }
            if (!validateInputFile(filePathEdit2.getText(), "Input File 2",  true)) {
                return false;
            }
        }

		// the name of the 2 experiments must be different
		if (sample1NameEdit.getText().equalsIgnoreCase(sample2NameEdit.getText())) {
			stringValidation.append(" • Name 1 and Name 2 must be different \n");
		}

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


    public int getThreshold() {
        return Integer.parseInt( thresholdEdit.getText() );
    }

    public JTextArea getLogArea() {
        return logArea;
    }
}
