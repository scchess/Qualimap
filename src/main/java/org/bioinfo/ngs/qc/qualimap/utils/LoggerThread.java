package org.bioinfo.ngs.qc.qualimap.utils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by kokonech
 * Date: 1/31/12
 * Time: 12:54 PM
 */
public class LoggerThread extends Thread {
    JTextArea logArea;
    BufferedReader outputReader;

    public LoggerThread(JTextArea logArea, BufferedReader outputReader) {
        this.logArea = logArea;
        this.outputReader = outputReader;
    }

    public void run() {
         String line;
            try {
                while ((line = outputReader.readLine()) != null) {
                    logArea.append(line + "\n");
                    logArea.setCaretPosition(logArea.getText().length());
                    System.out.println("We got something!");

                }

            } catch (IOException e) {
                System.err.println("Failed to parse output stream.");
            }
    }


}
