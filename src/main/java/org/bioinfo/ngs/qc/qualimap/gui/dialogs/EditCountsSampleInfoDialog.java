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
import org.bioinfo.ngs.qc.qualimap.gui.panels.CountsQcDialog;
import org.bioinfo.ngs.qc.qualimap.process.CountsSampleInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 4:40 PM
 */
public class EditCountsSampleInfoDialog extends JDialog implements KeyListener{
    CountsQcDialog parentDlg;
    JTextField sampleName, pathField;
    JButton okButton, cancelButton, browseCountsDataButton;
    JSpinner dataColumnSpinner, conditionIndexSpinner;
    Component frame;
    boolean editMode;
    int itemIndex;

    public EditCountsSampleInfoDialog( CountsQcDialog parent) {

        this.parentDlg = parent;
        this.itemIndex = -1;
        editMode = false;
        initComponents();

        int sampleIndex = parent.getItemCount() + 1;
        sampleName.setText("Sample" + sampleIndex);
        for (Component c : getContentPane().getComponents()) {
            c.addKeyListener(this);
        }

    }

    public EditCountsSampleInfoDialog(CountsQcDialog parent, int itemIndex) {

        this.parentDlg = parent;
        this.itemIndex = itemIndex;
        editMode = true;

        initComponents();

        CountsSampleInfo itemToModify = parentDlg.getDataItem(itemIndex);
        sampleName.setText(itemToModify.name);
        pathField.setText(itemToModify.path);


    }

    void initComponents() {
        frame = this;

        getContentPane().setLayout(new MigLayout("insets 20"));

        add(new JLabel("Sample name:"));
        sampleName = new JTextField(30);
        add(sampleName, "wrap");

        add(new JLabel("Path"));
        pathField = new JTextField(30);
        add(pathField);
        browseCountsDataButton = new JButton("...");
        browseCountsDataButton.addActionListener(new BrowseButtonActionListener(this, pathField, "Counts data"));
        add(browseCountsDataButton, "wrap");

        JLabel colLabel = new JLabel("Data column index:");
		add(colLabel);
        dataColumnSpinner = new JSpinner(new SpinnerNumberModel(CountsSampleInfo.DEFAULT_COLUMN, 1, 1000, 1));
        add(dataColumnSpinner, "wrap");

        JLabel conditionLabel = new JLabel("Condition index:");
        add(conditionLabel);
        conditionIndexSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
        add(conditionIndexSpinner, "wrap");


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("insets 10"));
        okButton = new JButton("OK");
        okButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String errorMessage  = validateInputData();
                if (errorMessage.isEmpty()) {
                    CountsSampleInfo item = new CountsSampleInfo();
                    item.name = sampleName.getText();
                    item.path = pathField.getText();
                    item.columnNum = ((SpinnerNumberModel) dataColumnSpinner.getModel()).getNumber().intValue();
                    item.conditionIndex =
                            ((SpinnerNumberModel) conditionIndexSpinner.getModel()).getNumber().intValue();
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
        setTitle("Counts sample data");
        setResizable(false);

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // DO NOTHING
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
         int code = keyEvent.getKeyCode();
         if(code == KeyEvent.VK_ESCAPE){
             setVisible(false);
         }
     }

    @Override
    public void keyReleased(KeyEvent e) {
        // DO NOTHING.
    }

    String validateInputData() {

        if (sampleName.getText().isEmpty()) {
            return "Sample name is empty!";
        }

        String samplePath = pathField.getText();

        if (samplePath.isEmpty()) {
            return "Sample counts path is empty!";
        }

        File sampleDataFile = new File(samplePath);
        if (!sampleDataFile.exists()) {
            return "Sample counts file doesn't exist!";
        }


        return "";
    }



}
