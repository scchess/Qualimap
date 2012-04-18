package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;
import org.bioinfo.ngs.qc.qualimap.utils.AnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.utils.GtfParser;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by kokonech
 * Date: 1/10/12
 * Time: 3:03 PM
 */
public class ComputeCountsDialog extends AnalysisDialog implements ActionListener{


    JTextField bamPathEdit, gffPathEdit, outputPathField, featureNameField, featureTypeField;
    JButton browseBamButton, browseGffButton, okButton, cancelButton;
    JComboBox strandTypeCombo, countingAlgoCombo;
    JComboBox availableFeatureTypesCombo, availableFeatureNamesCombo;
    JCheckBox saveStatsBox,advancedOptions;
    JLabel countingMethodLabel;
    JPanel ftPanel, fnPanel;
    Thread countReadsThread;


    static class BrowseGffButtonListener extends BrowseButtonActionListener {

        ComputeCountsDialog dlg;
        Set<String> featuresTypes, featureNames;
        static final int NUM_LINES = 100000;

        public BrowseGffButtonListener(ComputeCountsDialog parent, JTextField textField) {
            super(parent, textField, "GTF files", "gtf");
            this.dlg = parent;
            featuresTypes = new HashSet<String>();
            featureNames = new HashSet<String>();
        }

        @Override
        public void performAdditionalOperations() {
            dlg.availableFeatureTypesCombo.removeAllItems();
            dlg.availableFeatureNamesCombo.removeAllItems();

            String filePath = pathEdit.getText();
            try {
                preloadGff(filePath);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to preload GTF file, please make sure the file has correct format.",
                        dlg.getTitle(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (featuresTypes.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "No features are found in GTF file, please make sure the file is not empty.",
                        dlg.getTitle(), JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (String s : featuresTypes) {
                dlg.availableFeatureTypesCombo.addItem(s);
            }
            if (featuresTypes.contains(ComputeCountsTask.EXON_TYPE_ATTR)) {
                dlg.availableFeatureTypesCombo.setSelectedItem(ComputeCountsTask.EXON_TYPE_ATTR);
            }

            for (String s : featureNames) {
                dlg.availableFeatureNamesCombo.addItem(s);
            }
            if (featureNames.contains(ComputeCountsTask.GENE_ID_ATTR)) {
                dlg.availableFeatureNamesCombo.setSelectedItem(ComputeCountsTask.GENE_ID_ATTR);
            }

        }

        void preloadGff(String filePath) throws Exception {
            GtfParser parser = new GtfParser(filePath);
            for (int i = 0; i < NUM_LINES; ++i) {
                GtfParser.Record rec = parser.readNextRecord();
                if (rec == null) {
                    break;
                }
                String featureType = rec.getFeature();
                featuresTypes.add(featureType);
                Collection<String> fNames = rec.getAttributeNames();
                for (String name : fNames) {
                    featureNames.add(name);
                }
            }

        }
    }

    static class FeatureComboBoxListener implements ActionListener {

        JTextField targetField;
        JComboBox featuresCombo;

        FeatureComboBoxListener(JComboBox featuresCombo, JTextField targetField) {
            this.featuresCombo = featuresCombo;
            this.targetField = targetField;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Object curItem = featuresCombo.getSelectedItem();
            if (curItem == null) {
                return;
            }
                if (!curItem.toString().isEmpty()) {
                    targetField.setText(curItem.toString());
                }
        }
    }

    public ComputeCountsDialog(HomeFrame homeFrame) {

        super(homeFrame, "Compute counts");

        getContentPane().setLayout(new MigLayout("insets 20"));


        add(new JLabel("BAM file:"), "");

        bamPathEdit = new JTextField(40);
        bamPathEdit.setToolTipText("Path to BAM alignment file");
        add(bamPathEdit, "grow");

        browseBamButton = new JButton("...");
		browseBamButton.addActionListener( new BrowseButtonActionListener(this,
                bamPathEdit, "BAM files", "bam"));
        add(browseBamButton, "align center, wrap");

        add(new JLabel("Annotation file:"), "");

        gffPathEdit = new JTextField(40);
        gffPathEdit.setToolTipText("GTF file with the definition of the regions for the features.");
        add(gffPathEdit, "grow");

        browseGffButton = new JButton();
		browseGffButton.setText("...");
		browseGffButton.addActionListener(new BrowseGffButtonListener(this, gffPathEdit));
        browseBamButton.addActionListener(this);
        add(browseGffButton, "align center, wrap");


        add(new JLabel("Protocol:"));
        String[] protocolComboItems = {ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC,
                ComputeCountsTask.PROTOCOL_FORWARD_STRAND,
                ComputeCountsTask.PROTOCOL_REVERSE_STRAND};
        strandTypeCombo = new JComboBox(protocolComboItems);
        strandTypeCombo.setToolTipText("Select the corresponding sequencing protocol");
        strandTypeCombo.addActionListener(this);
        add(strandTypeCombo, "wrap");

        ftPanel = new JPanel();
        ftPanel.setLayout(new MigLayout("insets 5"));
        ftPanel.add(new JLabel("Feature type:"));
        featureTypeField = new JTextField(10);
        featureTypeField.setToolTipText("Third column of the GTF. All the features of other types will be ignored.");
        featureTypeField.setText(ComputeCountsTask.EXON_TYPE_ATTR);
        ftPanel.add(featureTypeField, "");
        ftPanel.add(new JLabel("Available feature types:"));
        availableFeatureTypesCombo = new JComboBox();
        availableFeatureTypesCombo.addItem("");
        availableFeatureTypesCombo.setToolTipText("These types of features were found in first 1000 of the GFF file");
        availableFeatureTypesCombo.addActionListener(
                new FeatureComboBoxListener(availableFeatureTypesCombo,featureTypeField));
        ftPanel.add(availableFeatureTypesCombo, "wrap");

        add(ftPanel, "span, wrap");

        fnPanel = new JPanel();
        fnPanel.setLayout(new MigLayout("insets 5"));
        fnPanel.add(new JLabel("Feature name:"));
        featureNameField = new JTextField(10);
        featureNameField.setText(ComputeCountsTask.GENE_ID_ATTR);
        featureNameField.setToolTipText("The name of the feature (attribute) to be count.");
        fnPanel.add(featureNameField, "");
        fnPanel.add(new JLabel("Available feature names:"));
        availableFeatureNamesCombo = new JComboBox();
        availableFeatureNamesCombo.setToolTipText("These types of features were found in first 1000 of the GFF file");
        availableFeatureNamesCombo.addActionListener(
                new FeatureComboBoxListener(availableFeatureNamesCombo, featureNameField));
        fnPanel.add(availableFeatureNamesCombo, "wrap");

        add(fnPanel, "span, wrap");

        advancedOptions = new JCheckBox("Advanced options:");
        advancedOptions.addActionListener(this);
        add(advancedOptions, "wrap");
        countingMethodLabel = new JLabel("Multi-mapped reads:");
        countingMethodLabel.setToolTipText("Select method to count reads that map to several genome locations");
        add(countingMethodLabel);
        String[] algoComboItems = {ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED,
                ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL
                };
        countingAlgoCombo = new JComboBox(algoComboItems);
        countingAlgoCombo.addActionListener(this);
        add(countingAlgoCombo, "wrap");


        add(new JLabel("Output:"), "");
        outputPathField = new JTextField(40);
        outputPathField.setToolTipText("Path to the file which will contain output");
        bamPathEdit.addCaretListener( new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                if (!bamPathEdit.getText().isEmpty()) {
                    outputPathField.setText(bamPathEdit.getText() + ".counts");
                }
            }
        });
        add(outputPathField, "grow");

        JButton browseOutputPathButton = new JButton("...");
        browseOutputPathButton.addActionListener(new BrowseButtonActionListener(this,
                outputPathField, "Counts file") );
        add(browseOutputPathButton, "wrap");

        saveStatsBox = new JCheckBox("Save computation summary");
        saveStatsBox.setToolTipText("<htmlThis option controls whether to save overall computation statistics.</html>");
        add(saveStatsBox, "span 2, wrap");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("insets 20"));

        okButton = new JButton("Run calculation");
        okButton.setActionCommand(Constants.OK_COMMAND);
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        cancelButton = new JButton("Close");
        cancelButton.setActionCommand(Constants.CANCEL_COMMAND);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        add(buttonPanel, "span, align right, wrap");

        pack();
        updateState();


    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals(Constants.OK_COMMAND)) {
            String errMsg = validateInput();
            if (!errMsg.isEmpty()) {
                JOptionPane.showMessageDialog(this, errMsg, getTitle(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            final ComputeCountsDialog frame = this;

            countReadsThread = new Thread()  {
                public void run() {

                    frame.setUiEnabled(false);
                    String bamPath = bamPathEdit.getText();
                    String gffPath = gffPathEdit.getText();
                    String featureType = featureTypeField.getText();
                    ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamPath, gffPath);
                    computeCountsTask.setProtocol(strandTypeCombo.getSelectedItem().toString());
                    computeCountsTask.setCountingAlgorithm(countingAlgoCombo.getSelectedItem().toString());
                    computeCountsTask.addSupportedFeatureType(featureType);
                    computeCountsTask.setAttrName(featureNameField.getText());

                    try {
                        computeCountsTask.run();

                        PrintWriter outWriter = new PrintWriter(new FileWriter(outputPathField.getText()));

                        Map<String,Double> counts = computeCountsTask.getReadCounts();
                        for (Map.Entry<String,Double> entry: counts.entrySet()) {
                            String str = entry.getKey() + "\t" + entry.getValue().longValue();
                            outWriter.println(str);
                        }
                        outWriter.flush();

                        if (saveStatsBox.isSelected()) {
                            PrintWriter statsWriter = new PrintWriter(new FileWriter(outputPathField.getText() + ".stats"));

                            String statsMessage = computeCountsTask.getOutputStatsMessage().toString();
                            statsWriter.println(statsMessage);

                            statsWriter.flush();
                        }

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(frame, e.getMessage(),
                                getTitle(), JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                        frame.setUiEnabled(true);
                        return;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append("Counts generated!\n\n");
                    message.append(computeCountsTask.getOutputStatsMessage());
                    message.append("\nResult is saved to ").append(outputPathField.getText());

                    JOptionPane.showMessageDialog(frame, message.toString(),
                            getTitle(), JOptionPane.INFORMATION_MESSAGE);

                    frame.setUiEnabled(true);

                }
            };

            countReadsThread.start();

        }  else if (actionEvent.getActionCommand().equals(Constants.CANCEL_COMMAND)) {
            setVisible(false);
        }  else {
            updateState();
        }


    }

    private void updateState() {
        countingAlgoCombo.setEnabled(advancedOptions.isSelected());
        countingMethodLabel.setEnabled(advancedOptions.isSelected());

        String countingMethod =  countingAlgoCombo.getSelectedItem().toString();
        String algorithmHint =  countingMethod.equals(ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED)  ?
                "Only uniquely mapped reads will be consider" :
                "<html>Each read will be weighted according to its number" +
                " of mapped locations. <br>For example, a read with 4 mapping locations will " +
                "<br>add 0.25 to the counts on each location.</html>";

        countingAlgoCombo.setToolTipText(algorithmHint);


        String protocol =  strandTypeCombo.getSelectedItem().toString();
        if (protocol.equals(ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC))  {
            strandTypeCombo.setToolTipText("Reads are counted if mapped to a feature independent of strand");
        }   else if (protocol.equals(ComputeCountsTask.PROTOCOL_FORWARD_STRAND)) {
            strandTypeCombo.setToolTipText("<html>The single-end reads must have the same strand as the feature." +
                    "<br>For paired-end reads first of a pair must have the same strand as the feature," +
                    "<br>while the second read must be on the opposite strand</html>");
        } else {
            strandTypeCombo.setToolTipText("<html>The single-end reads must have the strand opposite to the feature." +
                    "<br>For paired-end reads first of a pair must have on the opposite strand," +
                    "<br>while the second read must be on the same strand as the feature.</html>");
        }


    }


    public void setUiEnabled(boolean  enabled) {
        for( Component c : getContentPane().getComponents()) {
            c.setEnabled(enabled);
        }

        for (Component c : fnPanel.getComponents()) {
            c.setEnabled(enabled);
        }

        for (Component c: ftPanel.getComponents()) {
            c.setEnabled(enabled);
        }

        okButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }


    private String validateInput() {

        File bamFile = new File(bamPathEdit.getText());
        if (!bamFile.exists()) {
            return "BAM file is not found!";
        }

        File gffFile = new File(gffPathEdit.getText());
        if (!gffFile.exists())  {
            return "GFF file is not found!";
        }

        File outputFile = new File(outputPathField.getText());
        try {
           if (!outputFile.exists() && !outputFile.createNewFile()) {
               throw new IOException();
           }
        } catch (IOException e) {
            return "Output file path is not valid!";
        }
        if (!outputFile.delete()) {
            return "Output path is not valid! Deleting probing directory failed.";
        }

        return "";
    }
}
