package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;
import org.bioinfo.ngs.qc.qualimap.utils.AnalysisDialog;

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
import java.util.Map;

/**
 * Created by kokonech
 * Date: 1/10/12
 * Time: 3:03 PM
 */
public class ComputeCountsDialog extends AnalysisDialog implements ActionListener{


    JTextField bamPathEdit, gffPathEdit, outputPathField, featureTypeField;
    JButton browseBamButton, browseGffButton, okButton, cancelButton;
    JComboBox strandTypeCombo, featureNameCombo, countingAlgoCombo;
    JCheckBox saveStatsBox,advancedOptions;
    JLabel countingMethodLabel;
    Thread countReadsThread;


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
		browseGffButton.addActionListener(new BrowseButtonActionListener(this,
                gffPathEdit, "GTF files", "gtf"));
        add(browseGffButton, "align center, wrap");


        add(new JLabel("Strand Specificity:"));
        String[] protocolComboItems = {ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC,
                ComputeCountsTask.PROTOCOL_FORWARD_STRAND,
                ComputeCountsTask.PROTOCOL_REVERSE_STRAND};
        strandTypeCombo = new JComboBox(protocolComboItems);
        strandTypeCombo.setToolTipText("Select the corresponding sequencing protocol");
        add(strandTypeCombo, "wrap");

        add(new JLabel("Feature type:"));
        featureTypeField = new JTextField(10);
        featureTypeField.setToolTipText("Third column of the GTF. All the features of other types will be ignored.");
        featureTypeField.setText("exon");
        add(featureTypeField, " wrap");

        add(new JLabel("Feature name:"));
        String[] attrIems = {ComputeCountsTask.GENE_ID_ATTR,
                ComputeCountsTask.TRANSCRIPT_ID_ATTR};
        featureNameCombo = new JComboBox(attrIems);
        featureNameCombo.setToolTipText("The name of the feature (attribute) to be count.");
        add(featureNameCombo, "wrap");

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
        saveStatsBox.setToolTipText("<html>The summary of the counts will be saved to the " +
                "<br>same folder where output is located.</html>");
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
                    computeCountsTask.setAttrName(featureNameCombo.getSelectedItem().toString());

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

    }


    public void setUiEnabled(boolean  enabled) {
        for( Component c : getContentPane().getComponents()) {
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
