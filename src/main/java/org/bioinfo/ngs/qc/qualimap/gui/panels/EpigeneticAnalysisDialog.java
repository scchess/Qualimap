package org.bioinfo.ngs.qc.qualimap.gui.panels;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.BrowseButtonActionListener;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.EditEpigeneticsInputDataDialog;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.EpigeneticsAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 1/3/12
 * Time: 4:01 PM
 */
public class EpigeneticAnalysisDialog extends JDialog implements ActionListener{

    JTextField geneSelectionField,  clustersField;
    JSpinner columnSpinner, leftOffsetSpinner, rightOffsetSpinner, stepSpinner, smoothingLengthSpinner;
    JButton browseGeneSelectionButton;
    JButton startAnalysisButton, addSampleButton, removeSampleButton, editSampleButton;
    JTextField sampleName;
    JComboBox vizTypeBox;
    JLabel progressStream;
    JTable inputDataTable;
    JPanel buttonPanel, locationPanel;
    JProgressBar  progressBar;
    SampleDataTableModel sampleTableModel;
    HomeFrame homeFrame;

    static final String COMMAND_ADD_ITEM = "add_item";
    static final String COMMAND_REMOVE_ITEM = "delete_item";
    static final String COMMAND_EDIT_ITEM = "edit_item";
    static final String COMMAND_RUN_ANALYSIS = "run_analysis";


    static final boolean DEBUG = false;

    public String getGeneSelectionPath() {
        return geneSelectionField.getText();
    }

    public String getGeneSelectionColumn() {
        return columnSpinner.getValue().toString();
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


    public List<DataItem> getSampleItems() {
        return sampleTableModel.getItems();
    }

    public String[] getClusterNumbers() {
        return clustersField.getText().trim().split(",");
    }

    public String getInputDataName() {
        return sampleName.getText();
    }

    public String getSampleName() {
        return sampleName.getText();
    }

    static public class DataItem {
        public String name;
        public String medipPath;
        public String inputPath;
    }

    static class SampleDataTableModel extends AbstractTableModel {

        List<DataItem> sampleDataList;
        final String[] columnNames = { "Name", "Medip Path", "Input Path"};

        public SampleDataTableModel() {
            sampleDataList = new ArrayList<DataItem>();
        }

        public void addItem(DataItem item) {
            sampleDataList.add(item);
            fireTableDataChanged();
        }

        public void replaceItem(int index, DataItem newItem) {
            sampleDataList.set(index, newItem);
            fireTableDataChanged();
        }

        public void removeItem(int index) {
            sampleDataList.remove(index);
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getRowCount() {
            return sampleDataList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int i, int j) {
            DataItem data = sampleDataList.get(i);

            if (j == 0) {
                return data.name;
            } else if (j == 1) {
                return data.medipPath;
            } else if (j == 2) {
                return  data.inputPath;
            }   else {
                return "";
            }


        }

        public DataItem getItem(int index) {
            return sampleDataList.get(index);
        }

        public List<DataItem> getItems() {
            return sampleDataList;
        }
    }


    public EpigeneticAnalysisDialog(HomeFrame homeFrame) {

        this.homeFrame = homeFrame;

        KeyListener keyListener = new PopupKeyListener(homeFrame, this, null);
        getContentPane().setLayout(new MigLayout("insets 20"));

        // Gene selection
        add(new JLabel("Gene selection:"));

        geneSelectionField = new JTextField(30);
        geneSelectionField.setToolTipText("Path to gene selection file");
        add(geneSelectionField, "grow");

        browseGeneSelectionButton = new JButton();
		browseGeneSelectionButton.setText("...");
		browseGeneSelectionButton.addKeyListener(keyListener);
        browseGeneSelectionButton.addActionListener(
                new BrowseButtonActionListener(this, geneSelectionField,"Gene selection files", "txt"));
        add(browseGeneSelectionButton, "align center, wrap");

        add(new JLabel("Column:"));
        columnSpinner = new JSpinner(new SpinnerNumberModel(1, 1,20,1));
        add(columnSpinner, "wrap");

        add(new JLabel("Relative location:"), "span 2, wrap");

        locationPanel = new JPanel();
        locationPanel.setLayout(new MigLayout("insets 5"));

        locationPanel.add(new JLabel("Left offset:"));
        leftOffsetSpinner = new JSpinner(new SpinnerNumberModel(500, 1,1000000,1));
        locationPanel.add(leftOffsetSpinner, "");
        locationPanel.add(new JLabel("Right offset:"));
        rightOffsetSpinner = new JSpinner(new SpinnerNumberModel(500, 1,10000000,1));
        locationPanel.add(rightOffsetSpinner, "");
        locationPanel.add(new JLabel("Step:"));
        stepSpinner = new JSpinner(new SpinnerNumberModel(100, 1,10000,1));
        locationPanel.add(stepSpinner, "wrap");
        add(locationPanel, "span, wrap");

        add(new JLabel("Sample name: "), "");
        sampleName = new JTextField(20);
        sampleName.setText("Sample 1");
        add(sampleName, "wrap");

        add(new JLabel("MEDIP input data:"), "span 2, wrap");

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

        add(new JLabel("Clusters"));
        clustersField = new JTextField((20));
        clustersField.setText("10,15,20,25,30");
        add(clustersField, "wrap");

        add(new JLabel("Read smoothing length:"));
        smoothingLengthSpinner = new JSpinner(new SpinnerNumberModel(300, 100,500,1));
        add(smoothingLengthSpinner, "wrap");

        String[] vizTypes = { "Heatmap", "Line" };
        vizTypeBox = new JComboBox(vizTypes);
        add(new JLabel("Visuzliation type:"), "");
        add(vizTypeBox, "wrap 30px");


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
        startAnalysisButton.addKeyListener(keyListener);
        add(startAnalysisButton, "align right, span, wrap");


        pack();

        setTitle("MEDIP samples comparison");
        setResizable(false);

        if (DEBUG) {


            geneSelectionField.setText("/home/kokonech/qualimapEpi/src/medip/DMRs.24h.all.merged.2kbProm.txt");
            columnSpinner.setValue(4);

            sampleName.setText("24h-i");

            // add some preliminary data
            DataItem item1 = new DataItem();
            item1.name = "24h-i_1";
            item1.medipPath = "/home/kokonech/qualimapEpi/src/medip/mapping/24h-i-medip_1.uniq.sorted.noDup.bam.small";
            item1.inputPath = "/home/kokonech/qualimapEpi/src/medip/mapping/24h-i-input.uniq.sorted.noDup.bam.small";
            addDataItem(item1);

            DataItem item2 = new DataItem();
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
                TabPropertiesVO tabProperties = new TabPropertiesVO();
                tabProperties.setTypeAnalysis(Constants.TYPE_BAM_ANALYSIS_EPI);
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

    public void addDataItem(DataItem item) {
        sampleTableModel.addItem(item);
        updateState();
    }

    public void replaceDataItem(int itemIndex, DataItem item) {
        sampleTableModel.replaceItem(itemIndex, item);
        updateState();
    }


    public DataItem getDataItem(int index) {
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
            return "Gene selection path is not set!";
        }

        if ( !(new File(geneSelectionPath)).exists() ) {
            return "Gene selection path is not valid!";
        }

        if (sampleName.getText().isEmpty()) {
            return "Sample name is not provided!";
        }

        String[] clusterNumbers = getClusterNumbers();

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
