/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.BrowseButtonActionListener;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.EditEpigeneticsInputDataDialog;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.EpigeneticsAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis.ReplicateItem;
import org.bioinfo.ngs.qc.qualimap.common.DocumentUtils;


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 1/3/12
 * Time: 4:01 PM
 */
public class EpigeneticAnalysisDialog extends AnalysisDialog implements ActionListener{

    JTextField regionsField,  clustersField;
    JSpinner leftOffsetSpinner, rightOffsetSpinner, stepSpinner, smoothingLengthSpinner;
    JButton browseGeneSelectionButton;
    JButton startAnalysisButton, addSampleButton, removeSampleButton, editSampleButton;
    JTextField experimentName;
    JComboBox vizTypeBox;
    JLabel progressStream;
    JTable inputDataTable;
    JPanel buttonPanel, locationPanel;
    JProgressBar  progressBar;
    JTextArea logArea;
    SampleDataTableModel sampleTableModel;

    static final String COMMAND_ADD_ITEM = "add_item";
    static final String COMMAND_REMOVE_ITEM = "delete_item";
    static final String COMMAND_EDIT_ITEM = "edit_item";
    static final String COMMAND_RUN_ANALYSIS = "run_analysis";


    static final boolean DEBUG = false;

    public String getGeneSelectionPath() {
        return regionsField.getText();
    }

    public String getLeftOffset() {
        return leftOffsetSpinner.getValue().toString();
    }

    public String getRightOffset() {
        return rightOffsetSpinner.getValue().toString();
    }

    public String getStep() {
        return stepSpinner.getValue().toString();
    }


    public List<EpiAnalysis.ReplicateItem> getSampleItems() {
        return sampleTableModel.getItems();
    }

    public String getClusterNumbers() {
        return clustersField.getText().trim();
    }

    public String getInputDataName() {
        return experimentName.getText();
    }

    public String getExperimentName() {
        return experimentName.getText();
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public JLabel getProgressStream() {
        return progressStream;
    }

    static class SampleDataTableModel extends AbstractTableModel {

        List<ReplicateItem> sampleReplicateList;
        final String[] columnNames = { "Replicate Name", "Sample BAM file", "Control BAM file"};

        public SampleDataTableModel() {
            sampleReplicateList = new ArrayList<EpiAnalysis.ReplicateItem>();
        }

        public void addItem(ReplicateItem item) {
            sampleReplicateList.add(item);
            fireTableDataChanged();
        }

        public void replaceItem(int index, EpiAnalysis.ReplicateItem newItem) {
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
            EpiAnalysis.ReplicateItem replicate = sampleReplicateList.get(i);

            if (j == 0) {
                return replicate.name;
            } else if (j == 1) {
                return replicate.medipPath;
            } else if (j == 2) {
                return  replicate.inputPath;
            }   else {
                return "";
            }


        }

        public EpiAnalysis.ReplicateItem getItem(int index) {
            return sampleReplicateList.get(index);
        }

        public List<EpiAnalysis.ReplicateItem> getItems() {
            return sampleReplicateList;
        }
    }


    public EpigeneticAnalysisDialog(HomeFrame homeFrame) {

        super(homeFrame,"Cluster epigenomic signals") ;

        getContentPane().setLayout(new MigLayout("insets 20"));

        add(new JLabel("Experiment ID: "), "");
        experimentName = new JTextField(20);
        experimentName.setText("Experiment 1");
        add(experimentName, "wrap");

        add(new JLabel("Alignment data:"), "span 2, wrap");

        sampleTableModel = new SampleDataTableModel();
        inputDataTable = new JTable(sampleTableModel);
        JScrollPane scroller = new JScrollPane(inputDataTable);
        scroller.setPreferredSize(new Dimension(700, 100));
        add(scroller, "span, wrap");

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("insets 0"));

        addSampleButton = new JButton("Add");
        addSampleButton.setActionCommand(COMMAND_ADD_ITEM);
        addSampleButton.addActionListener(this);
        buttonPanel.add(addSampleButton, "");
        editSampleButton = new JButton("Edit");
        editSampleButton.setActionCommand(COMMAND_EDIT_ITEM);
        editSampleButton.addActionListener(this);
        buttonPanel.add(editSampleButton, "");
        removeSampleButton = new JButton("Remove");
        removeSampleButton.setActionCommand(COMMAND_REMOVE_ITEM);
        removeSampleButton.addActionListener(this);
        buttonPanel.add(removeSampleButton, "wrap");
        add(buttonPanel, "align right, span, wrap");

        // Gene selection
        add(new JLabel("Regions of interest:"));
        regionsField = new JTextField(40);
        regionsField.setToolTipText("Path to annotation file in BED or GFF format");
        add(regionsField, "grow");

        browseGeneSelectionButton = new JButton();
		browseGeneSelectionButton.setText("...");
        String[] supportedExtentions =  { "bed", "gff"};
		browseGeneSelectionButton.addActionListener(
                new BrowseButtonActionListener(this, regionsField,"Annotation files", supportedExtentions ));
        add(browseGeneSelectionButton, "align center, wrap");

        add(new JLabel("Location"), "span 2, wrap");

        locationPanel = new JPanel();
        locationPanel.setLayout(new MigLayout("insets 5"));

        locationPanel.add(new JLabel("Upstream offset (bp):"));
        leftOffsetSpinner = new JSpinner(new SpinnerNumberModel(2000, 1,1000000,1));
        locationPanel.add(leftOffsetSpinner, "");
        locationPanel.add(new JLabel("Downstream offset (bp):"));
        rightOffsetSpinner = new JSpinner(new SpinnerNumberModel(500, 1,10000000,1));
        locationPanel.add(rightOffsetSpinner, "");
        locationPanel.add(new JLabel("Bin size (bp):"));
        stepSpinner = new JSpinner(new SpinnerNumberModel(100, 1,10000,1));
        locationPanel.add(stepSpinner, "wrap");
        add(locationPanel, "span, wrap");

        add(new JLabel("Number of clusters:"));
        clustersField = new JTextField((20));
        clustersField.setText("10,15,20,25,30");
        add(clustersField, "wrap");

        add(new JLabel("Fragment length (bp):"));
        smoothingLengthSpinner = new JSpinner(new SpinnerNumberModel(300, 100,500,1));
        add(smoothingLengthSpinner, "wrap");

        String[] vizTypes = { "Heatmap", "Line" };
        vizTypeBox = new JComboBox(vizTypes);
        add(new JLabel("Visualization type:"), "");
        add(vizTypeBox, "wrap");

        add(new JLabel("Log"), "wrap");
        logArea = new JTextArea(5,40);
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(logArea);
        add(scrollPane, "span, grow, wrap 30px");

        UIManager.put("ProgressBar.selectionBackground", Color.black);
        UIManager.put("ProgressBar.selectionForeground", Color.black);

        progressStream = new JLabel("Status");
        add(progressStream, "align center");
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setForeground(new Color(244, 200, 120));
        add(progressBar, "grow, wrap 30px");

        startAnalysisButton = new JButton();
        startAnalysisButton.setText(">>> Run Analysis");
        startAnalysisButton.setActionCommand(COMMAND_RUN_ANALYSIS);
        startAnalysisButton.addActionListener(this);
        add(startAnalysisButton, "align right, span, wrap");


        pack();

        setResizable(false);

        if (DEBUG) {

            regionsField.setText("/home/kokonech/sample_data/clustering_sample/CpGIslandsByTakai.wihtNames.short.bed");

            experimentName.setText("24h-i");

            // add some preliminary data
            EpiAnalysis.ReplicateItem item1 = new ReplicateItem();
            item1.name = "24h-i_1";
            item1.medipPath = "/home/kokonech/qualimapEpi/src/medip/mapping/24h-i-medip_1.uniq.sorted.noDup.bam.small";
            item1.inputPath = "/home/kokonech/qualimapEpi/src/medip/mapping/24h-i-input.uniq.sorted.noDup.bam.small";
            addDataItem(item1);

            ReplicateItem item2 = new EpiAnalysis.ReplicateItem();
            item2.name = "24h-i_2";
            item2.medipPath = "/home/kokonech/qualimapEpi/src/medip/mapping/24h-i-medip_2.uniq.sorted.noDup.bam.small";
            item2.inputPath = "/home/kokonech/qualimapEpi/src/medip/mapping/24h-i-input.uniq.sorted.noDup.bam.small";
            addDataItem(item2);

        }

        updateState();



    }

    void updateState() {

        int numRows = inputDataTable.getRowCount();
        removeSampleButton.setEnabled(numRows > 0);
        editSampleButton.setEnabled(numRows > 0);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();

        if ( actionCommand.equals(COMMAND_ADD_ITEM) ) {
            EditEpigeneticsInputDataDialog dlg = new EditEpigeneticsInputDataDialog(this);
            dlg.setModal(true);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } else if ( actionCommand.equals(COMMAND_REMOVE_ITEM) ) {
            int index = inputDataTable.getSelectedRow();
            if (index != -1 ) {
                removeDataItem(index);
            }
        } else if ( actionCommand.equals(COMMAND_EDIT_ITEM) ) {
            int index = inputDataTable.getSelectedRow();
            if (index != -1) {
                EditEpigeneticsInputDataDialog dlg = new EditEpigeneticsInputDataDialog(this, index);
                dlg.setModal(true);
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);
            }
        } else if ( actionCommand.equals(COMMAND_RUN_ANALYSIS) ) {

            String errMsg = validateInput();
            if (errMsg.isEmpty()) {
                TabPropertiesVO tabProperties = new TabPropertiesVO(AnalysisType.CLUSTERING);
                EpigeneticsAnalysisThread t = new EpigeneticsAnalysisThread(this, tabProperties );
                t.start();
            } else {
                JOptionPane.showMessageDialog(this, errMsg, "Validate Input", JOptionPane.ERROR_MESSAGE);
            }
        } else  {
            updateState();
        }
    }

    public void removeDataItem(int index) {
        sampleTableModel.removeItem(index);
        updateState();
    }

    public void addDataItem(ReplicateItem item) {
        sampleTableModel.addItem(item);
        updateState();
    }

    public void replaceDataItem(int itemIndex, ReplicateItem item) {
        sampleTableModel.replaceItem(itemIndex, item);
        updateState();
    }


    public EpiAnalysis.ReplicateItem getDataItem(int index) {
        return sampleTableModel.getItem(index);
    }

    public void setUiEnabled(boolean enabled) {

        for (Component c : getContentPane().getComponents()) {
            c.setEnabled(enabled);
        }

        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(enabled);
        }

        for (Component c : locationPanel.getComponents())  {
            c.setEnabled(enabled);
        }

        // No matter happends these guys stay enabled:
        progressBar.setEnabled(true);
        progressStream.setEnabled(true);

    }

    public HomeFrame getHomeFrame() {
        return homeFrame;
    }

    String validateInput() {

        String geneSelectionPath = getGeneSelectionPath();

        if (geneSelectionPath.isEmpty()) {
            return "Path to regions is not set!";
        }

        if ( !(new File(geneSelectionPath)).exists() ) {
            return "Gene selection path is not valid!";
        }

        String errMsg = DocumentUtils.validateTabDelimitedFile(geneSelectionPath, 4);
        if (!errMsg.isEmpty()) {
            return errMsg;
        }

        String[] clusterNumbers = getClusterNumbers().split(",");

        if (clusterNumbers.length == 0) {
            return "Cluster numbers are not provided!";
        } else {
             for (String clusterNumber : clusterNumbers) {
                    try {
                        Integer.parseInt(clusterNumber);
                    } catch (NumberFormatException e) {
                        return "Can not parse number of clusters: " + clusterNumber;
                    }
                }
        }

        if (sampleTableModel.getRowCount() == 0) {
            return "No MEDIP input data is provided!";
        }

        return "";

    }

    public void setProgressStatus(String message) {
        progressStream.setText(message);
    }

    public String getReadSmoothingLength() {
        return smoothingLengthSpinner.getValue().toString();
    }

    public String getVisuzliationType() {
        return vizTypeBox.getSelectedItem().toString().toLowerCase();
    }

}
