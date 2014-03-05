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
