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
    String name;

    public StatsKeeper() {
        name = "";
        sectionList = new ArrayList<Section>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
