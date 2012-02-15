package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;

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
public class ComputeCountsDialog extends JDialog implements ActionListener{


    JTextField bamPathEdit, gffPathEdit, outputPathField, featureTypeField;
    JButton browseBamButton, browseGffButton, okButton, cancelButton;
    JComboBox strandTypeCombo, featureNameCombo, countingAlgoCombo;
    JCheckBox saveStatsBox;
    Thread countReadsThread;


    public ComputeCountsDialog() {

        getContentPane().setLayout(new MigLayout("insets 20"));


        add(new JLabel("BAM file:"), "");

        bamPathEdit = new JTextField(40);
        bamPathEdit.setToolTipText("Path to BAM alignment file");
        add(bamPathEdit, "grow");

        browseBamButton = new JButton("...");
		browseBamButton.addActionListener( new BrowseButtonActionListener(this,
                bamPathEdit, "BAM files", "bam"));
        add(browseBamButton, "align center, wrap");

        add(new JLabel("Annotations file:"), "");

        gffPathEdit = new JTextField(40);
        gffPathEdit.setToolTipText("Path to regions file");
        add(gffPathEdit, "grow");

        browseGffButton = new JButton();
		browseGffButton.setText("...");
		browseGffButton.addActionListener(new BrowseButtonActionListener(this,
                gffPathEdit, "GTF files", "gtf"));
        add(browseGffButton, "align center, wrap");


        add(new JLabel("Protocol:"));
        String[] protocolComboItems = {ComputeCountsTask.PROTOCOL_NON_STRAND_SPECIFIC,
                ComputeCountsTask.PROTOCOL_FORWARD_STRAND,
                ComputeCountsTask.PROTOCOL_REVERSE_STRAND};
        strandTypeCombo = new JComboBox(protocolComboItems);
        add(strandTypeCombo,"wrap");

        add(new JLabel("Counting method:"));
        String[] algoComboItems = {ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED,
                ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL
                };
        countingAlgoCombo = new JComboBox(algoComboItems);
        add(countingAlgoCombo,"wrap");

        add(new JLabel("Feature type:"));
        featureTypeField = new JTextField(10);
        featureTypeField.setToolTipText("Only features of these type will be considered in analysis. " +
                "Default is \"exon\"");
        featureTypeField.setText("exon");
        add(featureTypeField, " wrap");

        add(new JLabel("Feature name:"));
        String[] attrIems = {ComputeCountsTask.GENE_ID_ATTR,
                ComputeCountsTask.TRANSCRIPT_ID_ATTR};
        featureNameCombo = new JComboBox(attrIems);
        featureNameCombo.setToolTipText("The name of the feature (attribute) to be count.");
        add(featureNameCombo, "wrap");

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
        saveStatsBox.setToolTipText("The summary of the counts will be saved to the same folder where output is located.");
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

        setTitle("Compute counts");


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
        }


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
