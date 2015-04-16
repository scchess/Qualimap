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

import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kokonech
 * Date: 3/1/12
 * Time: 10:29 AM
 */
public class AnalysisDialog extends JDialog implements ContainerListener, KeyListener
{

    protected static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        AnalysisDialog dlg;

        public ExceptionHandler(AnalysisDialog homeDlg) {
            this.dlg = homeDlg;
        }

        public void uncaughtException(Thread th, Throwable ex) {

            String m = ex.getMessage();
            String errMsg = "Failed to perform " + dlg.getTitle() + "\n" + m;

            StringWriter errorsWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(errorsWriter));
            String errorReport = errorsWriter.toString();

            if (errorReport.contains("java.lang.OutOfMemoryError") ) {
                errMsg = NgsSmartMain.OUT_OF_MEMORY_REPORT;
            }

            JOptionPane.showMessageDialog(dlg, errMsg,  dlg.getTitle(), JOptionPane.ERROR_MESSAGE);

            dlg.resetProgress();
            dlg.setUiEnabled(true);
            dlg.updateState();

        }
    }


    protected HomeFrame homeFrame;

    protected JButton startAnalysisButton;
    protected JProgressBar progressBar;
    protected JLabel progressStream;
    protected JTextArea logArea;
    protected JScrollPane logAreaScrollPane;

    public AnalysisDialog(HomeFrame parent, String title) {
        super(parent,title);
        this.homeFrame = parent;
        addKeyAndContainerListenerRecursively(this);

    }

     private void addKeyAndContainerListenerRecursively(Component c)
     {
         c.removeKeyListener(this);
         c.addKeyListener(this);

         if(c instanceof Container){

             Container cont = (Container)c;
             cont.removeContainerListener(this);
             cont.addContainerListener(this);
             Component[] children = cont.getComponents();

             for(Component child : children){
                 addKeyAndContainerListenerRecursively(child);
             }
         }
     }

    private void removeKeyAndContainerListenerRecursively(Component c)
    {
        c.removeKeyListener(this);

        if(c instanceof Container){

            Container cont = (Container)c;

            cont.removeContainerListener(this);

            Component[] children = cont.getComponents();

            for(Component child : children){
                removeKeyAndContainerListenerRecursively(child);
            }
        }
    }



    @Override
     public void componentAdded(ContainerEvent containerEvent) {
         addKeyAndContainerListenerRecursively(containerEvent.getChild());

     }

     @Override
     public void componentRemoved(ContainerEvent containerEvent) {
         removeKeyAndContainerListenerRecursively(containerEvent.getChild());

     }

     @Override
     public void keyTyped(KeyEvent keyEvent) {


     }

     @Override
     public void keyPressed(KeyEvent keyEvent) {
         int code = keyEvent.getKeyCode();
         if(code == KeyEvent.VK_ESCAPE){
             setVisible(false);
         }
     }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }


    public HomeFrame getHomeFrame() {
        return homeFrame;
    }

    protected void setupProgressBar() {
        UIManager.put("ProgressBar.selectionBackground", Color.black);
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setForeground(new Color(244, 200, 120));
    }

    protected void setupProgressStream() {
        progressStream = new JLabel();
        progressStream.setVisible(true);
        progressStream.setText("Status");
    }

    protected void setupLogArea() {
        logArea = new JTextArea(10,40);
        logArea.setEditable(false);

        logAreaScrollPane = new JScrollPane();
        logAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logAreaScrollPane.setViewportView(logArea);
    }

    static public boolean validateInputFile(String filePath) {

        if ( filePath.isEmpty() ) {
            return false;
        }

        File inputFile = new File(filePath);
        return inputFile.exists();


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

    public void resetProgress() {
        if (progressBar != null) {
            progressBar.setValue(0);
        }
        if (progressStream != null) {
            progressStream.setText("Status");
        }
        if (logArea != null) {
            logArea.setText("");
        }
    }

    public void setUiEnabled(boolean enabled) {

        for (Component c : getContentPane().getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof JComponent) {
                JComponent panel = (JComponent) c;
                for (Component subComponent : panel.getComponents()) {
                    subComponent.setEnabled(enabled);
                }
            }
        }

        // No matter happens these guys stay enabled
        if (progressBar != null) {
            progressBar.setEnabled(true);
        }
        if (progressStream != null) {
            progressStream.setEnabled(true);
        }

    }


    public void updateProgress(int progress) {
        if (progressBar != null) {
            progressBar.setValue(progress);
        }
    }

    // virtual hopefully
    public void updateState() {

    }


 }
