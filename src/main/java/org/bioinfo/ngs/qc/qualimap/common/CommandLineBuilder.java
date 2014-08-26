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
package org.bioinfo.ngs.qc.qualimap.common;

import java.util.ArrayList;

/**
 * Created by kokonech
 * Date: 7/14/14
 * Time: 5:49 PM
 */

public class CommandLineBuilder {

    ArrayList<String[]> options;
    String head;
    public CommandLineBuilder(String head) {
        this.head = head;
        options = new ArrayList<String[]>();
    }

    public void append(String name, String value) {
        String[] option = { name, value };
        options.add(option);
    }

    public void append(String name, int value) {
        String[] option = { name, Integer.toString(value) };
        options.add(option);
    }

    public void append(String name) {
        String[] option = { name };
        options.add(option);
    }

    public String getCmdLine() {
        StringBuilder cmd = new StringBuilder();
        cmd.append(head);
        for (String[] option : options) {
            if (option.length == 1) {
                cmd.append(" -").append(option[0]);
            } else if( option.length == 2) {
                cmd.append(" -").append(option[0]).append(" ").append(option[1]);
            }
        }



        return cmd.toString();
    }



}
