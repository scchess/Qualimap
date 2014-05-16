package org.bioinfo.ngs.qc.qualimap.process;

/**
 * Created by kokonech
 * Date: 5/16/14
 * Time: 2:26 PM
 */
public interface ProgressReporter {

    public void reportStatus(String statusMessage);

    public void reportProgress(int percentage);


}
