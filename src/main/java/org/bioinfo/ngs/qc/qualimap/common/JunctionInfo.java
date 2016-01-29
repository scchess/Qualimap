/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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
