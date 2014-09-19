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
package org.bioinfo.ngs.qc.qualimap.gui.panels;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FilenameUtils;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.EditSampleInfoDialog;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.MultisampleBamQcThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.BamStatsAnalysisConfig;
import org.bioinfo.ngs.qc.qualimap.process.SampleInfo;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * Created by kokonech
 * Date: 6/6/14
 * Time: 3:04 PM
 */
public class MultisampleBamQcDialog extends AnalysisDialog implements ActionListener {

    static class SampleDataTableModel extends AbstractTableModel {

        java.util.List<SampleInfo> sampleReplicateList;
        final String[] columnNames = { "Sample name", "Path"};

        public SampleDataTableModel() {
            sampleReplicateList = new ArrayList<SampleInfo>();
        }

        public void addItem(SampleInfo item) {
            sampleReplicateList.add(item);
            fireTableDataChanged();
        }

        public void replaceItem(int index, SampleInfo newItem) {
            sampleReplicateList.set(index, newItem);
            fireTableDataChanged();
        }

        public void removeItem(int index) {
            sampleReplicateList.remove(index);
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getRowCount() {
            return sampleReplicateList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        @Override
        public Object getValueAt(int i, int j) {
            SampleInfo sampleInfo = sampleReplicateList.get(i);

            if (j == 0) {
                return sampleInfo.name;
            } else if (j == 1) {
                return sampleInfo.path;
            } else {
                return "";
            }

        }

        public SampleInfo getItem(int index) {
            return sampleReplicateList.get(index);
        }

        public java.util.List<SampleInfo> getItems() {
            return sampleReplicateList;
        }
    }

    SampleDataTableModel sampleTableModel;
    JTable inputDataTable;
    JPanel buttonPanel;
    JCheckBox activateBamQcMode;
    JButton addSampleButton, editSampleButton, removeSampleButton;

    // BAM QC stuff
    JPanel  bamQcOptionsPanel;
    JCheckBox analyzeRegionsCheckBox, drawChromosomeLimits;
    JTextField pathGffFile;
    JButton browseGffButton;
    JLabel labelNw, labelPathToGff, labelNumReadsPerBunch, labelMinHmSize;
    JSpinner valueNwSpinner, minHmSizeSpinner, numReadsPerBunchSpinner;


    public MultisampleBamQcDialog(HomeFrame parent) {
        super(parent, "Multi-sample BAM QC");
        getContentPane().setLayout(new MigLayout("insets 20"));

        add(new JLabel("Samples:"), "span 2, wrap");

        sampleTableModel = new SampleDataTableModel();
        inputDataTable = new JTable(sampleTableModel);

        JScrollPane scroller = new JScrollPane(inputDataTable);
        scroller.setPreferredSize(new Dimension(700, 100));
        add(scroller, "span, wrap");

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("insets 0"));
        addSampleButton = new JButton("Add");
        addSampleButton.setActionCommand(Constants.COMMAND_ADD_ITEM);
        addSampleButton.addActionListener(this);
        buttonPanel.add(addSampleButton, "");
        editSampleButton = new JButton("Edit");
        editSampleButton.setActionCommand(Constants.COMMAND_EDIT_ITEM);
        editSampleButton.addActionListener(this);
        buttonPanel.add(editSampleButton, "");
        removeSampleButton = new JButton("Remove");
        removeSampleButton.setActionCommand(Constants.COMMAND_REMOVE_ITEM);
        removeSampleButton.addActionListener(this);
        buttonPanel.add(removeSampleButton, "wrap");
        add(buttonPanel, "align right, span, wrap");

        activateBamQcMode = new JCheckBox("\"Raw data\" mode: run BAM QC on input samples");
        activateBamQcMode.setSelected(false);
        activateBamQcMode.setToolTipText("<html><body>By default precomputed BAM QC results are expected as input.<br>" +
                "However it is possible to provide raw BAM files as input<br>" +
                "and run BAM QC on each sample.</body></html>");
        activateBamQcMode.addActionListener(this);
        add(activateBamQcMode, "span 2, wrap");

        setupBamQcGui();
        add(bamQcOptionsPanel, "span 2, wrap");

        add(new JLabel("Log"), "wrap");
        setupLogArea();
        add(logAreaScrollPane, "span, grow, wrap 30px");

        // Action done while the statistics graphics are loaded
        setupProgressStream();
        add(progressStream, "align center");

        setupProgressBar();
        add(progressBar, "grow, wrap 30px");

        startAnalysisButton = new JButton();
        startAnalysisButton.addActionListener(this);
        startAnalysisButton.setActionCommand(Constants.COMMAND_RUN_ANALYSIS);
        startAnalysisButton.setText(">>> Run Analysis");

        add(startAnalysisButton, "span, align right, wrap");

        pack();

        // Default data  for testing
        if (System.getenv("QUALIMAP_DEBUG") != null)  {
            SampleInfo s1 = new SampleInfo();
            s1.name = "MCF7";
            s1.path = "/data/fusions-data/breast_cancer_MCF7/tophat_out/accepted_hits_stats";
            sampleTableModel.addItem(s1);


            SampleInfo s2 = new SampleInfo();
            s2.name = "Kidney";
            s2.path = "/data/qualimap_release_data/counts/kidney_stats";
            sampleTableModel.addItem(s2);

            SampleInfo s3 = new SampleInfo();
            s3.name = "Liver";
            s3.path = "/data/qualimap_release_data/counts/liver_stats";
            sampleTableModel.addItem(s3);

            SampleInfo s4 = new SampleInfo();
            s4.name = "Plasmodium DNA";
            s4.path = "/data/qualimap_release_data/alignments/Plasmodium-falciparum-3D7_stats";
            sampleTableModel.addItem(s4);

        }

        //Default data 2
        /*if (System.getenv("QUALIMAP_DEBUG") != null)  {

            SampleInfo s1 = new SampleInfo();
            s1.name = "S1";
            s1.path = "/data/qualimap_release_data/alignments/HG00096.chrom20.bam";
            sampleTableModel.addItem(s1);


            SampleInfo s2 = new SampleInfo();
            s2.name = "S2";
            s2.path = "/home/kokonech/sample_data/example-alignment.sorted.bam";
            sampleTableModel.addItem(s2);


        }*/

        updateState();
        setResizable(false);

    }

    private void setupBamQcGui() {

        bamQcOptionsPanel = new JPanel();
        bamQcOptionsPanel.setLayout(new MigLayout("insets 10"));

        analyzeRegionsCheckBox = new JCheckBox("Analyze regions");
        analyzeRegionsCheckBox.addActionListener(this);
        analyzeRegionsCheckBox.setToolTipText("Check to only analyze the regions defined in the features file");
        bamQcOptionsPanel.add(analyzeRegionsCheckBox, "wrap");

        labelPathToGff = new JLabel("Regions file (GFF/BED):");
        bamQcOptionsPanel.add(labelPathToGff, "");

        pathGffFile = new JTextField(40);
        pathGffFile.setToolTipText("Path to GFF/GTF or BED file containing regions of interest");
        bamQcOptionsPanel.add(pathGffFile, "grow");

        browseGffButton = new JButton();
        browseGffButton.setText("...");
        browseGffButton.setActionCommand(Constants.COMMAND_BROWSE_GFF);
        browseGffButton.addActionListener(this);
        bamQcOptionsPanel.add(browseGffButton, "align center, wrap");

        drawChromosomeLimits = new JCheckBox("Chromosome limits");
        drawChromosomeLimits.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        drawChromosomeLimits.setToolTipText("Check to draw the chromosome limits");
        drawChromosomeLimits.setSelected(true);
        bamQcOptionsPanel.add(drawChromosomeLimits, "wrap");

        labelNw = new JLabel("Number of windows:");
		bamQcOptionsPanel.add(labelNw);

        valueNwSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_NUMBER_OF_WINDOWS, 100, 4000, 50));
		valueNwSpinner.setToolTipText("Number of sampling windows across the reference");
        bamQcOptionsPanel.add(valueNwSpinner, "wrap");

        labelMinHmSize = new JLabel("Homopolymer size:");
        bamQcOptionsPanel.add(labelMinHmSize);
        minHmSizeSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_HOMOPOLYMER_SIZE, 2, 7, 1));
        minHmSizeSpinner.setToolTipText("<html>Only homopolymers of this size or larger will be considered " +
                "<br>when estimating number of homopolymer indels.</html>" );
        bamQcOptionsPanel.add(minHmSizeSpinner, "wrap");

        labelNumReadsPerBunch = new JLabel("Size of the chunk:");
        bamQcOptionsPanel.add(labelNumReadsPerBunch, "gapleft 20");
        numReadsPerBunchSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_CHUNK_SIZE, 100, 5000, 1));
        numReadsPerBunchSpinner.setToolTipText("<html>To speed up the computation reads are analyzed in chunks. " +
                "Each bunch is analyzed by single thread. <br>This option controls the number of reads in the chunk." +
                "<br>Smaller number may result in lower performance, " +
                "but also the memory consumption will be reduced.</html>");
        bamQcOptionsPanel.add(numReadsPerBunchSpinner, "wrap 20px");




    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();

        if ( actionCommand.equals(Constants.COMMAND_ADD_ITEM) ) {
            EditSampleInfoDialog dlg = new EditSampleInfoDialog(this);
            dlg.setModal(true);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } else if ( actionCommand.equals(Constants.COMMAND_REMOVE_ITEM) ) {
            int index = inputDataTable.getSelectedRow();
            if (index != -1 ) {
                removeDataItem(index);
            }
        } else if ( actionCommand.equals(Constants.COMMAND_EDIT_ITEM) ) {
            int index = inputDataTable.getSelectedRow();
            if (index != -1) {
                EditSampleInfoDialog dlg = new EditSampleInfoDialog(this, index);
                dlg.setModal(true);
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);
            }
        } else if (actionCommand.equals(Constants.COMMAND_BROWSE_GFF)) {
            browseGffFile();
        } else if ( actionCommand.equals(Constants.COMMAND_RUN_ANALYSIS) ) {

            String errMsg = validateInput();
            if (errMsg.isEmpty()) {
                resetProgress();
                TabPageController tabController = new TabPageController(AnalysisType.MULTISAMPLE_BAM_QC);
                MultisampleBamQcThread t = new MultisampleBamQcThread(this, tabController );
                t.start();
            } else {
                JOptionPane.showMessageDialog(this, errMsg, "Validate Input", JOptionPane.ERROR_MESSAGE);
            }
        } else  {
            updateState();
        }
    }

    private void browseGffFile() {

        JFileChooser fileChooser = HomeFrame.getFileChooser();

        FileFilter filter = new FileFilter() {
            public boolean accept(File fileShown) {
                boolean result = true;

                String ext = FilenameUtils.getExtension(fileShown.getName());

                if (!fileShown.isDirectory() && !Constants.FILE_EXTENSION_REGION.containsKey(ext.toUpperCase())) {
                    result = false;
                }

                return result;
            }

            public String getDescription() {
                return ("Region Files (*.gff *.gtf *.bed)");
            }
        };
        fileChooser.setFileFilter(filter);
        int valor = fileChooser.showOpenDialog(homeFrame.getCurrentInstance());

        if (valor == JFileChooser.APPROVE_OPTION) {
            pathGffFile.setText(fileChooser.getSelectedFile().getPath());
        }
    }


    void updateState() {
        boolean runBamQc = activateBamQcMode.isSelected();
        bamQcOptionsPanel.setEnabled(runBamQc);
        analyzeRegionsCheckBox.setEnabled(runBamQc);
        boolean regionsAvailable = analyzeRegionsCheckBox.isSelected();
        browseGffButton.setEnabled(runBamQc && regionsAvailable);
        pathGffFile.setEnabled(runBamQc && regionsAvailable);
        labelPathToGff.setEnabled(runBamQc && regionsAvailable);
        drawChromosomeLimits.setEnabled(runBamQc);
        labelMinHmSize.setEnabled(runBamQc);
        minHmSizeSpinner.setEnabled(runBamQc);
        labelNumReadsPerBunch.setEnabled(runBamQc);
        numReadsPerBunchSpinner.setEnabled(runBamQc);
        labelNw.setEnabled(runBamQc);
        valueNwSpinner.setEnabled(runBamQc);
    }

    String validateInput() {

        if (sampleTableModel.getRowCount() == 0) {
            return "No input samples are provided!";
        } else if (sampleTableModel.getRowCount() == 1) {
            return "Only one sample is provided! Please include more samples.";
        }

        if (analyzeRegionsCheckBox.isSelected()) {
            if (pathGffFile.getText().isEmpty()) {
                return "Path to GFF file is not set!";
            }
        }

        return "";
    }

    public int getItemCount() {
        return sampleTableModel.getRowCount();
    }

    public SampleInfo getDataItem(int index) {
        return sampleTableModel.getItem(index);
    }

    public void addDataItem(SampleInfo item) {
        sampleTableModel.addItem(item);
        updateState();
    }

    public void replaceDataItem(int itemIndex, SampleInfo item) {
        sampleTableModel.replaceItem(itemIndex, item);
        updateState();
    }

    private void removeDataItem(int index) {
        sampleTableModel.removeItem(index);
        updateState();
    }

    public java.util.List<SampleInfo> getDataItems() {
            return sampleTableModel.getItems();
    }

    public boolean runBamQcFirst() {
        return activateBamQcMode.isSelected();
    }

    public BamStatsAnalysisConfig getBamQcConfig() {

        BamStatsAnalysisConfig cfg = new BamStatsAnalysisConfig();
        if (analyzeRegionsCheckBox.isSelected()) {
            cfg.gffFile = pathGffFile.getText();
        }

        cfg.drawChromosomeLimits = drawChromosomeLimits.isSelected();
        cfg.numberOfWindows = (Integer) valueNwSpinner.getValue();
        cfg.minHomopolymerSize = (Integer) minHmSizeSpinner.getValue();
        cfg.bunchSize = (Integer) numReadsPerBunchSpinner.getValue();

        return cfg;
    }



}
