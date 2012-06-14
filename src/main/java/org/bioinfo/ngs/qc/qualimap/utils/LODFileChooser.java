package org.bioinfo.ngs.qc.qualimap.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by kokonech
 * Date: 2/3/12
 * Time: 4:07 PM
 * Last open dir file helper
 */

public class LODFileChooser extends JFileChooser {

    private static File lastOpenedDir = null;

    public LODFileChooser()
    {
        super();

        if (lastOpenedDir == null) {
            lastOpenedDir = new File(System.getProperty("user.home"));
        }
        setCurrentDirectory(lastOpenedDir);

    }

    public int showOpenDialog(Component parent)
    {
        int res = super.showOpenDialog(parent);
        lastOpenedDir = getCurrentDirectory();
        return res;
    }

    public int showSaveDialog(Component parent)
    {
        int res = super.showSaveDialog(parent);
        lastOpenedDir = getCurrentDirectory();
        return res;
    }

    @Override
    public void approveSelection(){
        File f = getSelectedFile();
        if(f.exists() && getDialogType() == SAVE_DIALOG){
            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result){
                case JOptionPane.YES_OPTION:
                    super.approveSelection();
                    return;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CANCEL_OPTION:
                    cancelSelection();
                    return;
            }
        }
        super.approveSelection();
    }


}
