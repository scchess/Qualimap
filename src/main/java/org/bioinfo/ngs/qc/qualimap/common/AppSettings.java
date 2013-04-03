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
