package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.ngs.qc.qualimap.gui.panels.EpigeneticAnalysisDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by kokonech
 * Date: 1/3/12
 * Time: 6:29 PM
 */
public class EditEpigeneticsInputDataDialog extends JDialog {

    JTextField replicateName, medipDataField, inputDataField;
    JButton okButton, cancelButton, browseMedipDataButton, browseInputDataButton;
    Component frame;
    EpigeneticAnalysisDialog parentDlg;
    boolean editMode;
    int itemIndex;

    public EditEpigeneticsInputDataDialog(EpigeneticAnalysisDialog parent) {

        this.parentDlg = parent;
        this.itemIndex = -1;
        editMode = false;
        initComponents();

    }

    public EditEpigeneticsInputDataDialog(EpigeneticAnalysisDialog parent, int itemIndex) {

        this.parentDlg = parent;
        this.itemIndex = itemIndex;
        editMode = true;

        initComponents();

        EpigeneticAnalysisDialog.DataItem itemToModify = parentDlg.getDataItem(itemIndex);
        replicateName.setText(itemToModify.name);
        medipDataField.setText(itemToModify.medipPath);
        inputDataField.setText(itemToModify.inputPath);

    }

    void initComponents() {
        frame = this;

        getContentPane().setLayout(new MigLayout("insets 20"));

        add(new JLabel("Replicate name:"));
        replicateName = new JTextField(30);
        add(replicateName, "wrap");

        add(new JLabel("Sample BAM file"));
        medipDataField = new JTextField(30);
        add(medipDataField);
        browseMedipDataButton = new JButton("...");
        browseMedipDataButton.addActionListener(new BrowseButtonActionListener(this,medipDataField,
                "BAM files", "bam"));
        add(browseMedipDataButton, "wrap");

        add(new JLabel("Control BAM file"));
        inputDataField = new JTextField(30);
        add(inputDataField);
        browseInputDataButton = new JButton("...");
        browseInputDataButton.addActionListener( new BrowseButtonActionListener(this, inputDataField,
                "BAM files", "bam"));
        add(browseInputDataButton, "wrap");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("insets 10"));
        okButton = new JButton("Ok");
        okButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String errorMessage  = validateInputData();
                if (errorMessage.isEmpty()) {
                    EpigeneticAnalysisDialog.DataItem item = new EpigeneticAnalysisDialog.DataItem();
                    item.name = replicateName.getText();
                    item.inputPath = inputDataField.getText();
                    item.medipPath = medipDataField.getText();

                    if (editMode) {
                        parentDlg.replaceDataItem(itemIndex, item);
                    } else {
                        parentDlg.addDataItem(item);
                    }
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(frame, errorMessage, "Input error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton, "wrap");

        add(buttonPanel, "align right, span, wrap");


        pack();
        setTitle("Input sample data");
        setResizable(false);

    }

    String validateInputData() {

        if (replicateName.getText().isEmpty()) {
           return "Replicate name is empty!";
        }

        String medipDataPath = medipDataField.getText();

        if (medipDataPath.isEmpty()) {
            return "Medip input data path is empty!";
        }

        File medipDataFile = new File(medipDataPath);
        if (!medipDataFile.exists()) {
            return "Medip input data file doesn't exist!";
        }

        String inputDataPath = inputDataField.getText();

        if (inputDataPath.isEmpty()) {
            return "Control input data path is empty!";
        }

        File inputDataFile = new File(inputDataPath);
        if (!inputDataFile.exists()) {
            return "Control input data file doesn't exist!";
        }


        return "";
    }












}
