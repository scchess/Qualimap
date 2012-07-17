package org.bioinfo.ngs.qc.qualimap.gui.dialogs;

import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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


 }
