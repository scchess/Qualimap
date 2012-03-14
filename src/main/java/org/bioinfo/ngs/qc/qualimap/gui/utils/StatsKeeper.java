package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 3/13/12
 * Time: 5:43 PM
 */

/**
 * This class allows to keep text information
 * in form of data rows grouped in sections.
 * It is used for Summary and Input html-report generation.
 */

public class StatsKeeper {

    static public class Section {
        String name;
        List<String[]> rows;
        public Section(String name) {
            this.name = name;
            this.rows = new ArrayList<String[]>();
        }

        public void addRow(String val1, String val2) {
            String[] row = new String[2];
            row[0] = val1;
            row[1] = val2;
            rows.add(row);
        }

        public String getName() {
            return name;
        }

        public List<String[]> getRows() {
            return rows;
        }

        public void addData(Map<String, String> paramsMap) {

            for (Map.Entry<String,String> entry: paramsMap.entrySet()) {
                addRow(entry.getKey(), entry.getValue());

            }

        }

        public void addRow(String[] tableValues) {
            rows.add(tableValues);
        }
    }

    List<Section> sectionList;

    public StatsKeeper() {
        sectionList = new ArrayList<Section>();
    }

    public void addSection(Section section) {
        sectionList.add(section);
    }

    public Section getSectionByName(String name) {
        for (Section s : sectionList ) {
            if (s.getName().equals(name)) {
                return s;
            }
        }

        return null;
    }

    public List<Section> getSections() {
        return sectionList;
    }





}
