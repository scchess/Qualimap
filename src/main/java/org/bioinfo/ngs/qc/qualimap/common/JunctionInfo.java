package org.bioinfo.ngs.qc.qualimap.common;

/**
 * Created by kokonech
 * Date: 5/28/14
 * Time: 4:51 PM
 */
public class JunctionInfo implements Comparable<JunctionInfo> {

    Double percentage;
    String junctionString;

    public JunctionInfo(String junction, Double percentage) {
        this.junctionString = junction;
        this.percentage = percentage;
    }

    @Override
    public int compareTo(JunctionInfo other) {
        return this.percentage.compareTo( other.percentage );
    }

    public double getPercentage() {
        return percentage;
    }

    public String getJunctionString() {
        return junctionString;
    }


}
