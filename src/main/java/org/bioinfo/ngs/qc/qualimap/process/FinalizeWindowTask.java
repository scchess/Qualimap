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
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return -1;
        }

        return 0;
    }


}
