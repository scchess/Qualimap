package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by kokonech
 * Date: 1/4/12
 * Time: 11:24 AM
 */

public class BrowseButtonActionListener implements ActionListener {

    protected JTextField pathEdit;
    protected Component parent;
    protected String description;
    protected String extention;

    public BrowseButtonActionListener(Component parent, JTextField field, String description) {
        this.parent = parent;
        this.pathEdit = field;
        this.description = description;
        this.extention = "";
    }



    public BrowseButtonActionListener(Component parent, JTextField field, String description, String extention) {
        this.parent = parent;
        this.pathEdit = field;
        this.description = description;
        this.extention = extention;
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        JFileChooser fileOpenChooser = HomeFrame.getFileChooser();

        FileFilter filter = new FileFilter() {
            public boolean accept(File fileShown) {
                if (!extention.isEmpty()) {
                    return (fileShown.getName().endsWith(extention) || fileShown.isDirectory());
                } else {
                    return true;
                }
            }

            public String getDescription() {
                return description;
            }
        };

        fileOpenChooser.setFileFilter(filter);

        int result = fileOpenChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            pathEdit.setText(fileOpenChooser.getSelectedFile().getPath());
        }

        performAdditionalOperations();
    }

    protected void performAdditionalOperations() {

    }
}
