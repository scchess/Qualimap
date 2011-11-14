package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.BamGenomeWindow;
import org.bioinfo.ngs.qc.qualimap.beans.BamStats;

import java.io.*;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by kokonech
 * Date: 11/7/11
 * Time: 6:12 PM
 */

public class FinalizeWindowTask implements Callable {


    BamStats bamStats;
    BamGenomeWindow window;

    static private PrintStream getLogger() {
        OutputStream log = null;
        try {
            log = new BufferedOutputStream(new FileOutputStream("/home/kokonech/final.log")) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PrintStream(log);
    }

    static private PrintStream log = getLogger();

    public FinalizeWindowTask(BamStats bamStats, BamGenomeWindow windowToFinalize) {
        this.bamStats = bamStats;
        this.window = windowToFinalize;
    }

    public Integer call() {

        //System.out.println("From FinalizeWindowTask: started!");
        //log.println("Window name: " + window.getName() + " cNumber: " + window.getNumberOfCs());
        //log.flush();

        try {
            window.computeDescriptors();
            bamStats.addWindowInformation(window);
            System.out.println("From FinalizeWindowTask: finalized a window " + window.getName());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        //System.out.println("From FinalizeWindowTask: finished!");

        return 0;
    }


}
