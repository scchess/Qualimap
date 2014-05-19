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
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * Created by kokonech
 * Date: 3/1/12
 * Time: 10:29 AM
 */
public class AnalysisDialog extends JDialog implements ContainerListener, KeyListener
{

    protected HomeFrame homeFrame;

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

    static public boolean validateInputFile(String filePath) {

        if ( filePath.isEmpty() ) {
            return false;
        }

        File inputFile = new File(filePath);
        return inputFile.exists();


    }

 }
