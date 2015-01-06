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
package org.bioinfo.ngs.qc.qualimap.common;

import java.util.Properties;

/**
 * Created by kokonech
 * Date: 2/17/13
 * Time: 3:29 PM
 */
public class AppSettings {

    Properties settings;
    static AppSettings globalSettings;

    static final String PATH_TO_R_SCRIPT = "path_to_rscript";

    public AppSettings() {
        settings = new Properties();
    }

    public String getPathToRScript() {
        return settings.getProperty(PATH_TO_R_SCRIPT, "Rscript");
    }

    public void setPathToRScript(String path) {
        settings.setProperty(PATH_TO_R_SCRIPT, path);
    }

    public static AppSettings getGlobalSettings() {
        return globalSettings;
    }

    public static void setGlobalSettings(AppSettings settings) {
        globalSettings = settings;
    }

}
