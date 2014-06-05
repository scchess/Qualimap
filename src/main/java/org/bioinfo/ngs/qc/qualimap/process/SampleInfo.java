package org.bioinfo.ngs.qc.qualimap.process;

/**
 * Created by kokonech
 * Date: 6/5/14
 * Time: 2:59 PM
 */
public class SampleInfo{

    public String name;
    public String path;

    public SampleInfo() {}

    public SampleInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }


}
