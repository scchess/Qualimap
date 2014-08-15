/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2014 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.common.LibraryProtocol;
import org.bioinfo.ngs.qc.qualimap.process.ComputeCountsTask;
import org.bioinfo.ngs.qc.qualimap.common.*;

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
import java.util.*;

/**
 * Created by kokonech
 * Date: 1/10/12
 * Time: 3:03 PM
 */
public class ComputeCountsDialog extends AnalysisDialog implements ActionListener{


    JTextField bamPathEdit, gffPathEdit, outputPathField, featureNameField, featureTypeField;
    JButton browseBamButton, browseGffButton, okButton, cancelButton;
    JComboBox<String> strandTypeCombo, countingAlgoCombo;
    JComboBox<String> availableFeatureTypesCombo, availableFeatureNamesCombo;
    JCheckBox saveStatsBox,advancedOptions, pairedAnalysisBox, alreadySortedBox;
    JLabel countingMethodLabel;
    JPanel ftPanel, fnPanel;
    Thread countReadsThread;

    static class BrowseGffButtonListener extends BrowseButtonActionListener {

        ComputeCountsDialog dlg;
        Set<String> featuresTypes, featureNames;
        static final int NUM_LINES = 100000;
        static final String[] extentions = { "gtf", "gff", "bed" };

        public BrowseGffButtonListener(ComputeCountsDialog parent, JTextField textField) {
            super(parent, textField, "Annotation files", extentions );
            this.dlg = parent;
            featuresTypes = new HashSet<String>();
            featureNames = new HashSet<String>();
        }

        @Override
        public void validateInput() {
            dlg.setGtfSpecificOptionsEnabled(false);
            dlg.availableFeatureTypesCombo.removeAllItems();
            dlg.availableFeatureNamesCombo.removeAllItems();

            String filePath = pathEdit.getText();
            try {
                preloadGff(filePath);
            } catch (Exception e) {
                return;
            }

            if (featuresTypes.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "No features are found in GTF file, please make sure the file is not empty.",
                        dlg.getTitle(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            dlg.setGtfSpecificOptionsEnabled(true);

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
            GenomicFeatureStreamReader parser = new GenomicFeatureStreamReader(filePath, FeatureFileFormat.GTF);
            for (int i = 0; i < NUM_LINES; ++i) {
                GenomicFeature rec = parser.readNextRecord();
                if (rec == null) {
                    break;
                }
                String featureType = rec.getFeatureName();
                featuresTypes.add(featureType);
                Collection<String> fNames = rec.getAttributeNames();
                for (String name : fNames) {
                    featureNames.add(name);
                }
            }

        }
    }

    private void setGtfSpecificOptionsEnabled(boolean enabled) {

        for (Component c : fnPanel.getComponents()) {
            c.setEnabled(enabled);
        }

        for (Component c : ftPanel.getComponents()) {
            c.setEnabled(enabled);
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
        gffPathEdit.setToolTipText("File with the definition of the regions for the features (GTF/GFF or BED).");
        add(gffPathEdit, "grow");

        browseGffButton = new JButton();
		browseGffButton.setText("...");
		browseGffButton.addActionListener(new BrowseGffButtonListener(this, gffPathEdit));
        browseBamButton.addActionListener(this);
        add(browseGffButton, "align center, wrap");


        add(new JLabel("Protocol:"));
        String[] protocolComboItems = LibraryProtocol.getProtocolNames();
        strandTypeCombo = new JComboBox<String>(protocolComboItems);
        strandTypeCombo.setToolTipText("Select the corresponding sequencing protocol");
        strandTypeCombo.addActionListener(this);
        add(strandTypeCombo, "wrap");

        JLabel gtfLabel = new JLabel("GTF-specific options:");
        gtfLabel.setToolTipText("<html>If annotations are given in GTF format, counting is performed based " +
                "on given attributes.</html>");
        add(gtfLabel, "wrap");
        fnPanel = new JPanel();
        fnPanel.setLayout(new MigLayout("insets 5"));
        fnPanel.add(new JLabel("Feature ID:"));
        featureNameField = new JTextField(10);
        featureNameField.setText(ComputeCountsTask.GENE_ID_ATTR);
        featureNameField.setToolTipText("<html>Attribute of the GTF to be used as feature ID. Regions with the same ID" +
                    "will be aggregated as part of the same feature. Default: gene_id</html>");
        fnPanel.add(featureNameField, "");
        fnPanel.add(new JLabel("Available feature IDs:"));
        availableFeatureNamesCombo = new JComboBox<String>();
        availableFeatureNamesCombo.setToolTipText("These feature IDS were found in first 1000 of the GTF file");
        availableFeatureNamesCombo.addActionListener(
                new FeatureComboBoxListener(availableFeatureNamesCombo, featureNameField));
        fnPanel.add(availableFeatureNamesCombo, "wrap");

        add(fnPanel, "span, wrap");


        ftPanel = new JPanel();
        ftPanel.setLayout(new MigLayout("insets 5"));
        ftPanel.add(new JLabel("Feature type:"));
        featureTypeField = new JTextField(10);
        featureTypeField.setToolTipText("<html>Value of the third column of the GTF considered for counting. " +
                "<br>Other types will be ignored. Default: exon</html>");
        featureTypeField.setText(ComputeCountsTask.EXON_TYPE_ATTR);
        ftPanel.add(featureTypeField, "");
        ftPanel.add(new JLabel("Available feature types:"));
        availableFeatureTypesCombo = new JComboBox<String>();
        availableFeatureTypesCombo.addItem("");
        availableFeatureTypesCombo.setToolTipText("These types of features were found in first 1000 of the GFF file");
        availableFeatureTypesCombo.addActionListener(
                new FeatureComboBoxListener(availableFeatureTypesCombo,featureTypeField));
        ftPanel.add(availableFeatureTypesCombo, "wrap");

        add(ftPanel, "span, wrap");

        pairedAnalysisBox = new JCheckBox("Paired-end analysis");
        pairedAnalysisBox.setToolTipText("<html>This option activates counting of fragments instead of counting of reads. " +
                "<br>Only valid for paired-end sequencing experiments.</html>");
        pairedAnalysisBox.addActionListener(this);
        add(pairedAnalysisBox);

        alreadySortedBox = new JCheckBox("Alignment is sorted by name");
        alreadySortedBox.setToolTipText("<html>The paired-end analysis requires the BAM file to be sorted by name. " +
                "<br>Check this box if it is the case, otherwise temporary BAM sorted by name" +
                "<br>will be created.</html>");
        add(alreadySortedBox, "wrap");

        setGtfSpecificOptionsEnabled(false);

        advancedOptions = new JCheckBox("Advanced options:");
        advancedOptions.addActionListener(this);
        add(advancedOptions, "wrap");

        countingMethodLabel = new JLabel("Multi-mapped reads:");
        countingMethodLabel.setToolTipText("Select method to count reads that map to several genome locations");
        add(countingMethodLabel);
        String[] algoComboItems = {ComputeCountsTask.COUNTING_ALGORITHM_ONLY_UNIQUELY_MAPPED,
                ComputeCountsTask.COUNTING_ALGORITHM_PROPORTIONAL
                };
        countingAlgoCombo = new JComboBox<String>(algoComboItems);
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
                outputPathField, "Counts file", false) );
        add(browseOutputPathButton, "wrap");

        saveStatsBox = new JCheckBox("Save computation summary");
        saveStatsBox.setToolTipText("<html>This option controls whether to save " +
                "overall computation statistics.</html>");
        add(saveStatsBox, "span 2, wrap");

        add(new JLabel("Log:"), "wrap");

        setupLogArea();
        add(logAreaScrollPane, "span, grow, wrap 30px");

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

        if (System.getenv("QUALIMAP_DEBUG") != null) {
            bamPathEdit.setText("/home/kokonech/sample_data/paired_rna_seq/pe_nssp_hg19.chr20.bam");
            gffPathEdit.setText("/data/annotations/Homo_sapiens.GRCh37.68.chr20.gtf");

        }

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
                    logArea.setText("");
                    String bamPath = bamPathEdit.getText();
                    String gffPath = gffPathEdit.getText();
                    String featureType = featureTypeField.getText();
                    ComputeCountsTask computeCountsTask = new ComputeCountsTask(bamPath, gffPath);
                    String pName = strandTypeCombo.getSelectedItem().toString();
                    computeCountsTask.setProtocol(LibraryProtocol.getProtocolByName(pName));
                    computeCountsTask.setCountingAlgorithm(countingAlgoCombo.getSelectedItem().toString());
                    computeCountsTask.addSupportedFeatureType(featureType);
                    computeCountsTask.setAttrName(featureNameField.getText());
                    if (pairedAnalysisBox.isSelected()) {
                        computeCountsTask.setPairedEndAnalysis();
                        if (!alreadySortedBox.isSelected()) {
                            computeCountsTask.setSortingRequired();
                        }
                    }

                    final JTextArea loggerDestArea = logArea;

                    computeCountsTask.setLogger( new LoggerThread() {
                        @Override
                        public void logLine(String msg) {
                            loggerDestArea.append(msg + "\n");
                            loggerDestArea.setCaretPosition(loggerDestArea.getText().length());
                        }
                    });


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
                    logArea.append(message.toString());
                    logArea.setCaretPosition(logArea.getText().length());

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
        alreadySortedBox.setEnabled(pairedAnalysisBox.isSelected());

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
        if (protocol.equals(LibraryProtocol.PROTOCOL_NON_STRAND_SPECIFIC))  {
            strandTypeCombo.setToolTipText("Reads are counted if mapped to a feature independent of strand");
        }   else if (protocol.equals(LibraryProtocol.PROTOCOL_FORWARD_STRAND)) {
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
        super.setUiEnabled(enabled);

        if (enabled) {
            boolean gtfSpecific =
                    DocumentUtils.guessFeaturesFileFormat(gffPathEdit.getText()) == FeatureFileFormat.GTF;
            setGtfSpecificOptionsEnabled( gtfSpecific );
        } else {
            setGtfSpecificOptionsEnabled(false);
        }


    }


    private String validateInput() {

        File bamFile = new File(bamPathEdit.getText());
        if (!bamFile.exists()) {
            return "BAM file is not found!";
        }

        File gffFile = new File(gffPathEdit.getText());
        if (!gffFile.exists())  {
            return "Annotations file is not found!";
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
