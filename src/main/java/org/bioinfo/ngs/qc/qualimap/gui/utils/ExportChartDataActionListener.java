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
