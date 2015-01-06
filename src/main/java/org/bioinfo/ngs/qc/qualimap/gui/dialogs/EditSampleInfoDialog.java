/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.gui.panels.MultisampleBamQcDialog;
import org.bioinfo.ngs.qc.qualimap.process.SampleInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * Created by kokonech
 * Date: 6/6/14
 * Time: 3:17 PM
 */
public class EditSampleInfoDialog extends JDialog implements KeyListener {

    MultisampleBamQcDialog parentDlg;
    JTextField sampleName, pathField;
    JButton okButton, cancelButton, browseSampleButton;
    Component frame;
    boolean editMode;
    int itemIndex;

    public EditSampleInfoDialog( MultisampleBamQcDialog parent) {

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

    public EditSampleInfoDialog(MultisampleBamQcDialog parent, int itemIndex) {

        this.parentDlg = parent;
        this.itemIndex = itemIndex;
        editMode = true;

        initComponents();

        SampleInfo itemToModify = parentDlg.getDataItem(itemIndex);
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
        browseSampleButton = new JButton("...");
        browseSampleButton.addActionListener(
                new BrowseButtonActionListener(this, pathField, "BAM QC result or raw BAM file", true) );
        add(browseSampleButton, "wrap");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("insets 10"));
        okButton = new JButton("OK");
        okButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String errorMessage  = validateInputData();
                if (errorMessage.isEmpty()) {
                    SampleInfo item = new SampleInfo();
                    item.name = sampleName.getText();
                    item.path = pathField.getText();
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
        setTitle("Sample data");
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
            return "Sample path is empty!";
        }

        File sampleDataFile = new File(samplePath);
        if (!sampleDataFile.exists()) {
            return "Sample file doesn't exist!";
        }


        return "";
    }

}
