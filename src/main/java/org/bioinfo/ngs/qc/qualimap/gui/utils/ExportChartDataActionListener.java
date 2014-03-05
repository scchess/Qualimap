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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import org.apache.commons.io.FilenameUtils;
import org.bioinfo.ngs.qc.qualimap.beans.ChartRawDataWriter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.jfree.ui.ExtensionFileFilter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by kokonech
 * Date: 6/13/12
 * Time: 12:58 PM
 */
public class ExportChartDataActionListener implements ActionListener {

    ChartRawDataWriter dataWriter;
    Component parent;
    String title;

    public ExportChartDataActionListener(Component parent, ChartRawDataWriter writer, String title) {
        this.dataWriter = writer;
        this.parent = parent;
        this.title = title;
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JFileChooser fileChooser = HomeFrame.getFileChooser();
        fileChooser.setSelectedFile(new File(title.replace(" ", "_").toLowerCase() + ".txt"));
        fileChooser.setFileFilter( new ExtensionFileFilter("TAB delimited data", "txt") );

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                if (FilenameUtils.getExtension(path).isEmpty()) {
                    path += ".txt";
                }
                dataWriter.exportDataToFile(path);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to save the raw data, please check the path.",
                        "Saving plot raw data", JOptionPane.ERROR_MESSAGE);
            }
        }


    }
}
