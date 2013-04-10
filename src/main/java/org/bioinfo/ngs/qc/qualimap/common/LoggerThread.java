/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2013 Garcia-Alcalde et al.
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

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 1/31/12
 * Time: 12:54 PM
 */
public abstract class LoggerThread extends Thread {

    BufferedReader outputReader;

    public abstract void logLine(String msg);

    public void start(BufferedReader reader) {
        this.outputReader = reader;
        start();
    }

    public void run() {
        String line;
        try {
            while ((line = outputReader.readLine()) != null) {
               logLine(line);
            }

        } catch (IOException e) {
            System.err.println("Failed to parse output stream.");
        }
    }




}
