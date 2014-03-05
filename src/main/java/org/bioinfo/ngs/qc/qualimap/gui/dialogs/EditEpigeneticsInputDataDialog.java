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
import org.bioinfo.ngs.qc.qualimap.gui.panels.EpigeneticAnalysisDialog;
import org.bioinfo.ngs.qc.qualimap.process.EpiAnalysis;

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

        EpiAnalysis.ReplicateItem itemToModify = parentDlg.getDataItem(itemIndex);
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
                    EpiAnalysis.ReplicateItem item = new EpiAnalysis.ReplicateItem();
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
