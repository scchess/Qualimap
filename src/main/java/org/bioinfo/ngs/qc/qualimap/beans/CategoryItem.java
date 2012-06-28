package org.bioinfo.ngs.qc.qualimap.beans;

/**
 * Created by kokonech
 * Date: 6/27/12
 * Time: 12:25 PM
 */
public class CategoryItem {

    private int value;
    private String name;

    public CategoryItem(String name, int value) {
        this.name = name;
        this.value = value;
    }

    String getName() {
        return name;
    }

    int getValue() {
        return value;
    }

}
