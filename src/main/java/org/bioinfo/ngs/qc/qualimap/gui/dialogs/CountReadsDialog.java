package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.process.CountReadsAnalysis;

import javax.swing.*;
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
public class CountReadsDialog extends JDialog implements ActionListener{


    JTextField bamPathEdit, gffPathEdit, outputPathField;
    JButton browseBamButton, browseGffButton, okButton, cancelButton;
    JComboBox strandTypeCombo;
    Thread countReadsThread;


    public CountReadsDialog() {

        getContentPane().setLayout(new MigLayout("insets 20"));


        add(new JLabel("BAM file:"), "");

        bamPathEdit = new JTextField(40);
        bamPathEdit.setToolTipText("Path to BAM alignment file");
        add(bamPathEdit, "grow");

        browseBamButton = new JButton("...");
		browseBamButton.addActionListener( new BrowseButtonActionListener(this,
                bamPathEdit, "BAM files", "bam"));
        add(browseBamButton, "align center, wrap");

        add(new JLabel("GFF file with annotations:"), "");

        gffPathEdit = new JTextField(40);
        gffPathEdit.setToolTipText("Path to regions file");
        add(gffPathEdit, "grow");

        browseGffButton = new JButton();
		browseGffButton.setText("...");
		browseGffButton.addActionListener(new BrowseButtonActionListener(this,
                gffPathEdit, "GTF files", "gtf"));
        add(browseGffButton, "align center, wrap");


        add(new JLabel("Protocol:"));
        String[] comboItems = {CountReadsAnalysis.NON_STRAND_SPECIFIC,
                CountReadsAnalysis.FORWARD_STRAND,
                CountReadsAnalysis.REVERSE_STRAND};
        strandTypeCombo = new JComboBox(comboItems);
        add(strandTypeCombo,"wrap");

        add(new JLabel("Output:"), "");

        outputPathField = new JTextField(40);
        outputPathField.setText("bam.file.counts");
        add(outputPathField, "grow");

        JButton browseOutputPathButton = new JButton("...");
        browseOutputPathButton.addActionListener(new BrowseButtonActionListener(this,
                outputPathField, "Counts file") );
        add(browseOutputPathButton, "wrap");

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
            final CountReadsDialog frame = this;

            countReadsThread = new Thread()  {
                public void run() {

                    frame.setUiEnabled(false);
                    String bamPath = bamPathEdit.getText();
                    String gffPath = gffPathEdit.getText();
                    CountReadsAnalysis  countReadsAnalysis = new CountReadsAnalysis(bamPath, gffPath);
                    countReadsAnalysis.setProtocol(strandTypeCombo.getSelectedItem().toString());

                    try {
                        countReadsAnalysis.run();

                        PrintWriter outWriter = new PrintWriter(new FileWriter(outputPathField.getText()));

                        Map<String,Long> counts = countReadsAnalysis.getReadCounts();
                        for (Map.Entry<String,Long> entry: counts.entrySet()) {
                            String str = entry.getKey() + "\t" + entry.getValue().toString();
                            outWriter.println(str);
                        }
                        outWriter.flush();

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(frame, e.getMessage(),
                                getTitle(), JOptionPane.ERROR_MESSAGE);
                        frame.setUiEnabled(true);
                        return;
                    }


                    long totalCounted = countReadsAnalysis.getTotalReadCounts();
                    long noFeature = countReadsAnalysis.getNoFeatureNumber();
                    long notUnique = countReadsAnalysis.getAlignmentNotUniqueNumber();
                    long ambiguous = countReadsAnalysis.getAmbiguousNumber();

                    StringBuilder message = new StringBuilder();
                    message.append("Calculation succesful!\n");
                    message.append("Feature read counts: ").append(totalCounted).append("\n");
                    message.append("No feature: ").append(noFeature).append("\n");
                    message.append("Not unique alignment: ").append(notUnique).append("\n");
                    message.append("Ambiguous: ").append(ambiguous).append("\n");
                    message.append("Result is saved to file ").append(outputPathField.getText());

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
           if (!outputFile.createNewFile()) {
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
