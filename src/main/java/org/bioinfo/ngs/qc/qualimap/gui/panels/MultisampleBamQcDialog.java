package org.bioinfo.ngs.qc.qualimap.gui.panels;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.EditSampleInfoDialog;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.MultisampleBamQcThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPageController;
import org.bioinfo.ngs.qc.qualimap.process.SampleInfo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    JButton addSampleButton, editSampleButton, removeSampleButton;


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

        updateState();
        setResizable(false);

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


    void updateState() {

    }

String validateInput() {

        if (sampleTableModel.getRowCount() == 0) {
            return "No input data is provided!";
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



}
