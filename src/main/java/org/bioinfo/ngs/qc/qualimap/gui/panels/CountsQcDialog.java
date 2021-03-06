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

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.BrowseButtonActionListener;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.EditCountsSampleInfoDialog;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.CountsQCAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.CountsSampleInfo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 3:36 PM
 */

public class CountsQcDialog extends AnalysisDialog implements ActionListener {

    private SampleDataTableModel sampleTableModel;
    private JTable inputDataTable;
    private JPanel buttonPanel;
    private JButton addSampleButton, editSampleButton, removeSampleButton;
    private JCheckBox compartativeAnalysisCheckBox, provideInfoFileCheckBox;
    private JRadioButton infoFileButton, speciesButton;
    private JTextField infoFileEdit, condition1NameEdit, condition2NameEdit;
    private JLabel condition1Label, condition2Label, thresholdLabel;
    private JSpinner thresholdSpinner;
    private JButton browseInfoFileButton;
    private JComboBox speciesCombo;

    static final String INFO_FILE_TOOLTIP = "File containing the biological classification, " +
            "gc content and lengths of features in the count files.";

    static final String SPECIES_ITEM_TOOLTIP = "Qualimap provides the Ensembl biotype " +
            "classification for selected species";

    static class SampleDataTableModel extends AbstractTableModel {

        java.util.List<CountsSampleInfo> sampleReplicateList;
        final String[] columnNames = { "Sample name", "Path", "Column number", "Condition"};

        public SampleDataTableModel() {
            sampleReplicateList = new ArrayList<CountsSampleInfo>();
        }

        public void addItem(CountsSampleInfo item) {
            sampleReplicateList.add(item);
            fireTableDataChanged();
        }

        public void replaceItem(int index, CountsSampleInfo newItem) {
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
            CountsSampleInfo sampleInfo = sampleReplicateList.get(i);

            if (j == 0) {
                return sampleInfo.name;
            } else if (j == 1) {
                return sampleInfo.path;
            } else if (j == 2) {
                return  sampleInfo.columnNum;
            } else if (j == 3) {
                return  sampleInfo.conditionIndex;
            }   else {
                return "";
            }


        }

        public CountsSampleInfo getItem(int index) {
            return sampleReplicateList.get(index);
        }

        public java.util.List<CountsSampleInfo> getItems() {
            return sampleReplicateList;
        }
    }

    public CountsQcDialog(HomeFrame homeFrame) {
        super(homeFrame, "Counts QC");
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

        thresholdLabel = new JLabel();
        thresholdLabel.setText("Counts threshold:");
        add(thresholdLabel, "");

        thresholdSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        thresholdSpinner.setToolTipText("A feature is considered as detected if the corresponding number of counts is " +
                "greater than this threshold.");
        thresholdSpinner.setMaximumSize(new Dimension(120,100));
        add(thresholdSpinner, "grow, wrap");

        compartativeAnalysisCheckBox = new JCheckBox("Compare conditions");
        compartativeAnalysisCheckBox.addActionListener(this);
        add(compartativeAnalysisCheckBox, "wrap");

        condition1Label = new JLabel("Condition 1 name:");
        add(condition1Label);
        condition1NameEdit = new JTextField(20);
        condition1NameEdit.setText("Condition1");
        add(condition1NameEdit, "wrap");

        condition2Label = new JLabel("Condition 2 name:");
        add(condition2Label);
        condition2NameEdit = new JTextField(20);
        condition2NameEdit.setText("Condition2");
        add(condition2NameEdit, "wrap");

        provideInfoFileCheckBox = new JCheckBox("Include feature classification");
        provideInfoFileCheckBox.addActionListener(this);
        add(provideInfoFileCheckBox, "wrap");

        speciesButton = new JRadioButton("Species: ");
        speciesButton.setSelected(true);
        speciesButton.addActionListener(this);
        speciesButton.setToolTipText(SPECIES_ITEM_TOOLTIP);
        add(speciesButton);

        String[] speicesComboItems = { Constants.TYPE_COMBO_SPECIES_HUMAN, Constants.TYPE_COMBO_SPECIES_MOUSE };
        speciesCombo = new JComboBox<String>(speicesComboItems);
        speciesCombo.setToolTipText(SPECIES_ITEM_TOOLTIP);
        add(speciesCombo, "grow, wrap");

        infoFileButton = new JRadioButton("Info file:");
        infoFileButton.addActionListener(this);
        infoFileButton.setToolTipText(INFO_FILE_TOOLTIP);
        add(infoFileButton, "");

        infoFileEdit = new JTextField(30);
        infoFileEdit.setToolTipText(INFO_FILE_TOOLTIP);
        add(infoFileEdit, "grow");

        browseInfoFileButton = new JButton("...");
        browseInfoFileButton.addActionListener( new BrowseButtonActionListener(homeFrame,
                        infoFileEdit, "Species files", "txt"));

        add(browseInfoFileButton, "align center, wrap 30px");

        ButtonGroup group = new ButtonGroup();
        group.add(infoFileButton);
        group.add(speciesButton);


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
            CountsSampleInfo i1 = new CountsSampleInfo();
            i1.name = "Infected1";
            i1.path = "/home/kokonech/sample_data/counts/mb141.counts.txt";
            i1.conditionIndex = 1;
            i1.columnNum = 2;
            sampleTableModel.addItem(i1);


            CountsSampleInfo i2 = new CountsSampleInfo();
            i2.name = "Infected2";
            i2.path = "/home/kokonech/sample_data/counts/mb141.counts.txt";
            i2.conditionIndex = 1;
            i2.columnNum = 3;
            sampleTableModel.addItem(i2);

            CountsSampleInfo u1 = new CountsSampleInfo();
            u1.name = "Uninfected1";
            u1.path = "/home/kokonech/sample_data/counts/mb141.counts.txt";
            u1.conditionIndex = 2;
            u1.columnNum = 5;
            sampleTableModel.addItem(u1);

            CountsSampleInfo u2 = new CountsSampleInfo();
            u2.name = "Uninfected2";
            u2.path = "/home/kokonech/sample_data/counts/mb141.counts.txt";
            u2.conditionIndex = 2;
            u2.columnNum = 6;
            sampleTableModel.addItem(u2);

        }

        updateState();
        setResizable(false);




    }

    public int getItemCount() {
        return sampleTableModel.getRowCount();
    }

    public CountsSampleInfo getDataItem(int index) {
        return sampleTableModel.getItem(index);
    }

    public void addDataItem(CountsSampleInfo item) {
        sampleTableModel.addItem(item);
        updateState();
    }

    public void replaceDataItem(int itemIndex, CountsSampleInfo item) {
        sampleTableModel.replaceItem(itemIndex, item);
        updateState();
    }

    private void removeDataItem(int index) {
        sampleTableModel.removeItem(index);
        updateState();
    }

    public List<CountsSampleInfo> getDataItems() {
            return sampleTableModel.getItems();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();

        if ( actionCommand.equals(Constants.COMMAND_ADD_ITEM) ) {
            EditCountsSampleInfoDialog dlg = new EditCountsSampleInfoDialog(this);
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
                EditCountsSampleInfoDialog dlg = new EditCountsSampleInfoDialog(this, index);
                dlg.setModal(true);
                dlg.setLocationRelativeTo(this);
                dlg.setVisible(true);
            }
        } else if ( actionCommand.equals(Constants.COMMAND_RUN_ANALYSIS) ) {

            String errMsg = validateInput();
            if (errMsg.isEmpty()) {
                resetProgress();
                TabPageController tabController = new TabPageController(AnalysisType.COUNTS_QC);
                CountsQCAnalysisThread t = new CountsQCAnalysisThread(this, tabController );

                Thread.UncaughtExceptionHandler eh = new AnalysisDialog.ExceptionHandler(this);
                t.setUncaughtExceptionHandler(eh);

                t.start();

            } else {
                JOptionPane.showMessageDialog(this, errMsg, "Validate Input", JOptionPane.ERROR_MESSAGE);
            }
        } else  {
            updateState();
        }
    }


    String validateInput() {

        if (sampleTableModel.getRowCount() == 0) {
            return "No input data is provided!";
        }

        if (compartativeAnalysisCheckBox.isSelected()) {

            if (sampleTableModel.getRowCount() == 0) {
                return "Need at least 2 samples to compare conditions";
            }
            if (condition1NameEdit.getText().length() == 0) {
                return "Condition 1 name is empty!";
            }
            if (condition2NameEdit.getText().length() == 0) {
                return "Condition 2 name is empty!";
            }

            List<CountsSampleInfo> items = sampleTableModel.getItems();
            int idx = items.get(0).conditionIndex;
            boolean allSamplesSameIndex = true;
            for (int i = 1; i < items.size(); ++i) {
                if (items.get(i).conditionIndex != idx) {
                    allSamplesSameIndex = false;
                    break;
                }
            }

            if (allSamplesSameIndex) {
                return "All samples have the same condition!\n" +
                        "To perform comparison different conditions are required.";
            }




        }

        if (provideInfoFileCheckBox.isSelected()) {
            // Validation for info file
            if (infoFileButton.isSelected() ) {
                String infoFilePath = infoFileEdit.getText();
                if (infoFilePath.isEmpty()) {
                    return "Info file path is empty!";
                } else if (!validateInputFile(infoFilePath)) {
                    return "Info file does not exist!";
                }

            }
        }

        return "";
    }

    public void updateState() {
        int numRows = inputDataTable.getRowCount();
        removeSampleButton.setEnabled(numRows > 0);
        editSampleButton.setEnabled(numRows > 0);

        boolean provideInfoFile = provideInfoFileCheckBox.isSelected();

        infoFileButton.setEnabled(provideInfoFile);
        speciesButton.setEnabled(provideInfoFile);
        infoFileEdit.setEnabled(infoFileButton.isSelected() && provideInfoFile);
        browseInfoFileButton.setEnabled(infoFileButton.isSelected() && provideInfoFile);
        speciesCombo.setEnabled(speciesButton.isSelected() && provideInfoFile);

        boolean performComparison = compartativeAnalysisCheckBox.isSelected();
        condition1NameEdit.setEnabled(performComparison);
        condition1Label.setEnabled(performComparison);
        condition2NameEdit.setEnabled(performComparison);
        condition2Label.setEnabled(performComparison);

    }

    public Map<Integer,String> getConditionsMap() {
        Map<Integer,String> cMap = new HashMap<Integer, String>();

        cMap.put(1, condition1NameEdit.getText() );
        cMap.put(2, condition2NameEdit.getText() );

        return cMap;

    }

    public boolean infoFileIsProvided() {
        return infoFileButton.isSelected();
    }

    public String getInfoFilePath() {
        return infoFileEdit.getText();
    }

    public String getSelectedSpecies() {

        String speciesName = speciesCombo.getSelectedItem().toString();

        if (speciesName.equals(Constants.TYPE_COMBO_SPECIES_HUMAN)) {
            return Constants.FILE_SPECIES_INFO_HUMAN_ENS68;
        } else if (speciesName.equals(Constants.TYPE_COMBO_SPECIES_MOUSE)) {
            return Constants.FILE_SPECIES_INFO_MOUSE_ENS68;
        } else {
            return "";
        }
    }

    public boolean annotationIsProvided() {
        return provideInfoFileCheckBox.isSelected();
    }

    public boolean performComparison() {
        return compartativeAnalysisCheckBox.isSelected();
    }

    public int getCountsThreshold() {
        return ((SpinnerNumberModel) thresholdSpinner.getModel()).getNumber().intValue();
    }


}
