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
package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 5:49 PM
 */

public abstract class AnalysisProcess {

    protected AnalysisResultManager tabProperties;
    protected LoggerThread loggerThread;
    protected  String homePath;

    public AnalysisProcess(AnalysisResultManager tabProperties, String homePath) {
        this.tabProperties = tabProperties;
        this.homePath = homePath;
        this.loggerThread = null;
    }

    public abstract void run() throws Exception;

    void logLine(String line) {
        if (loggerThread != null) {
            loggerThread.logLine(line);
        }
    }


}
