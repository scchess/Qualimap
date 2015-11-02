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
package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;

import java.util.concurrent.Callable;

/**
 * Created by kokonech
 * Date: 11/7/11
 * Time: 6:12 PM
 */

public class FinalizeWindowTask implements Callable<Integer> {


    BamStats bamStats;
    BamGenomeWindow window;

    public FinalizeWindowTask(BamStats bamStats, BamGenomeWindow windowToFinalize) {
        this.bamStats = bamStats;
        this.window = windowToFinalize;
    }

    public Integer call() {

        try {
            window.computeDescriptors();
            bamStats.addWindowInformation(window);
            //System.out.println("Finalzed window " + window.getName() + "\n");
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return -1;
        }

        return 0;
    }


}
