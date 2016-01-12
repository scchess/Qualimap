/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.process;

/**
 * Created by kokonech
 * Date: 6/5/14
 * Time: 2:59 PM
 */
public class SampleInfo{

    public String name;
    public String path;
    public String group;

    public SampleInfo() {}

    public SampleInfo(String name, String path) {
        this.name = name;
        this.path = path;
        this.group = name;
    }

    public SampleInfo(String name, String path, String group) {
        this.name = name;
        this.path = path;
        this.group = group;
    }


}
