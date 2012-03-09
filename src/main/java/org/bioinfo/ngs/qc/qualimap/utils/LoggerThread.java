package org.bioinfo.ngs.qc.qualimap.utils;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 1/31/12
 * Time: 12:54 PM
 */
public abstract class LoggerThread extends Thread {

    BufferedReader outputReader;

    protected  abstract void logLine(String msg);

    public void start(BufferedReader reader) {
        this.outputReader = reader;
        start();
    }

    public void run() {
        String line;
        try {
            while ((line = outputReader.readLine()) != null) {

                logLine(line);
                //logArea.append(line + "\n");
                //logArea.setCaretPosition(logArea.getText().length());
                //System.out.println("We got something!");

            }

        } catch (IOException e) {
            System.err.println("Failed to parse output stream.");
        }
    }


}
