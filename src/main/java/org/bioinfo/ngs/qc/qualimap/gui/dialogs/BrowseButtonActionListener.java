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
    protected String[] extentions;
    boolean allowDirectories;

    public BrowseButtonActionListener(Component parent, JTextField field, String description, boolean allowDirs) {
        this.parent = parent;
        this.pathEdit = field;
        this.description = description;
        this.extentions = null;
        this.allowDirectories = allowDirs;

    }

    public BrowseButtonActionListener(Component parent, JTextField field, String description, String ext) {
        this.parent = parent;
        this.pathEdit = field;
        this.description = description;
        this.allowDirectories = false;
        this.extentions = new String[1];
        extentions[0] = ext;
    }


    public BrowseButtonActionListener(Component parent, JTextField field, String description, String[] extentions) {
        this.parent = parent;
        this.pathEdit = field;
        this.description = description;
        this.extentions = extentions;
        this.allowDirectories = false;
    }


    public void setAllowDirectories(boolean allowDirs) {
        this.allowDirectories = allowDirs;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        JFileChooser fileOpenChooser = HomeFrame.getFileChooser();
        if (allowDirectories) {
            fileOpenChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }

        FileFilter filter = new FileFilter() {
            public boolean accept(File fileShown) {

                if (extentions == null) {
                    return true;
                }

                for (String ext : extentions) {
                    if (fileShown.getName().endsWith(ext) || fileShown.isDirectory() ) {
                        return true;
                    }
                }

                return false;

            }

            public String getDescription() {
                return description;
            }
        };

        fileOpenChooser.setFileFilter(filter);

        int result = fileOpenChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            pathEdit.setText(fileOpenChooser.getSelectedFile().getPath());
            validateInput();
        }


    }

    protected void validateInput() {

    }
}
