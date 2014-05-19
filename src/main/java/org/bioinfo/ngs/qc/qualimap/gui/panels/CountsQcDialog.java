package org.bioinfo.ngs.qc.qualimap.gui.panels;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.BrowseButtonActionListener;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.EditCountsSampleInfoDialog;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.CountsQCAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.AnalysisType;
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

    //TODO: these are common in many dialogs, move them to parent class
    private JProgressBar progressBar;
    private JLabel progressStream;
    private JTextArea logArea;
    private JButton startAnalysisButton;

    private SampleDataTableModel sampleTableModel;
    private JTable inputDataTable;
    private JPanel buttonPanel;
    private JButton addSampleButton, editSampleButton, removeSampleButton;
    private JCheckBox provideInfoFileCheckBox;
    private JRadioButton infoFileButton, speciesButton;
    private JTextField infoFileEdit;
    private JButton browseInfoFileButton;
    private JComboBox speciesCombo;

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
        super(homeFrame, "Multisample Counts QC");
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

        provideInfoFileCheckBox = new JCheckBox("Include feature classification");
        provideInfoFileCheckBox.addActionListener(this);
        add(provideInfoFileCheckBox, "wrap");

        speciesButton = new JRadioButton("Species: ");
        speciesButton.setSelected(true);
        speciesButton.addActionListener(this);
        //speciesButton.setToolTipText(SPECIES_ITEM_TOOLTIP);
        add(speciesButton);

        String[] speicesComboItems = { Constants.TYPE_COMBO_SPECIES_HUMAN/*, Constants.TYPE_COMBO_SPECIES_MOUSE*/ };
        speciesCombo = new JComboBox<String>(speicesComboItems);
        //speciesCombo.setToolTipText(SPECIES_ITEM_TOOLTIP);
        add(speciesCombo, "grow, wrap");

        infoFileButton = new JRadioButton("Info file:");
        infoFileButton.addActionListener(this);
        //infoFileButton.setToolTipText(INFO_FILE_TOOLTIP);
        add(infoFileButton, "");

        infoFileEdit = new JTextField(30);
        //infoFileEdit.setToolTipText(INFO_FILE_TOOLTIP);
        add(infoFileEdit, "grow");

        browseInfoFileButton = new JButton("...");
        browseInfoFileButton.addActionListener( new BrowseButtonActionListener(homeFrame,
                        infoFileEdit, "Species files", "txt"));

        add(browseInfoFileButton, "align center, wrap 30px");

        ButtonGroup group = new ButtonGroup();
        group.add(infoFileButton);
        group.add(speciesButton);


        add(new JLabel("Log"), "wrap");
        logArea = new JTextArea(10,40);
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(logArea);
        add(scrollPane, "span, grow, wrap 30px");

        // Action done while the statistics graphics are loaded
        progressStream = new JLabel();
        progressStream.setVisible(true);
        progressStream.setText("Status");
        add(progressStream, "align center");

        UIManager.put("ProgressBar.selectionForeground", Color.black);
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setForeground(new Color(244, 200, 120));
        add(progressBar, "grow, wrap 30px");

        startAnalysisButton = new JButton();
        startAnalysisButton.addActionListener(this);
        startAnalysisButton.setActionCommand(Constants.COMMAND_RUN_ANALYSIS);
        startAnalysisButton.setText(">>> Run Analysis");

        add(startAnalysisButton, "span, align right, wrap");

        pack();

        // Default data  for testing
        {
            CountsSampleInfo i1 = new CountsSampleInfo();
            i1.name = "Infected1";
            i1.path = "/home/kokonech/sample_data/counts/mb141.counts.txt";
            i1.conditionIndex = 1;
            sampleTableModel.addItem(i1);


            CountsSampleInfo i2 = new CountsSampleInfo();
            i2.name = "Infected2";
            i2.path = "/home/kokonech/sample_data/counts/mb141.counts.txt";
            i2.conditionIndex = 1;
            i2.columnNum = 3;
            sampleTableModel.addItem(i2);

            infoFileEdit.setText("/home/kokonech/sample_data/counts/human.ens68.txt");

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
                TabPageController tabController = new TabPageController(AnalysisType.MULTISAMPLE_COUNTS_QC);
                CountsQCAnalysisThread t = new CountsQCAnalysisThread(this, tabController );
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

    void updateState() {
        int numRows = inputDataTable.getRowCount();
        removeSampleButton.setEnabled(numRows > 0);
        editSampleButton.setEnabled(numRows > 0);

        boolean provideInfoFile = provideInfoFileCheckBox.isSelected();

        infoFileButton.setEnabled(provideInfoFile);
        speciesButton.setEnabled(provideInfoFile);
        infoFileEdit.setEnabled(infoFileButton.isSelected() && provideInfoFile);
        browseInfoFileButton.setEnabled(infoFileButton.isSelected() && provideInfoFile);
        speciesCombo.setEnabled(speciesButton.isSelected() && provideInfoFile);

    }

    // TODO: should be a protected method?
    public void setUiEnabled(boolean enabled) {

        for (Component c : getContentPane().getComponents()) {
            c.setEnabled(enabled);
        }

        for (Component c : buttonPanel.getComponents()) {
            c.setEnabled(enabled);
        }

        // No matter happends these guys stay enabled:
        progressBar.setEnabled(true);
        progressStream.setEnabled(true);

    }


    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getProgressStream() {
        return progressStream;
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public Map<Integer,String> getConditionsMap() {
        Map<Integer,String> cMap = new HashMap<Integer, String>();

        cMap.put(1, "Condition 1");
        cMap.put(2, "Condition 2");

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







}
