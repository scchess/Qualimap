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
