package org.bioinfo.ngs.qc.qualimap.process;

/**
* Created by kokonech
* Date: 5/15/14
* Time: 4:25 PM
*/
public class CountsSampleInfo {

    public String name;
    public String path;
    public int columnNum;
    public int conditionIndex;

    public static final int DEFAULT_COLUMN = 2;

    public CountsSampleInfo() {
        columnNum = DEFAULT_COLUMN;
        conditionIndex = 1;
    }

}
