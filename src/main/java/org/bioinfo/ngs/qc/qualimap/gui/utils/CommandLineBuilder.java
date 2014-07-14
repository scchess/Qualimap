package org.bioinfo.ngs.qc.qualimap.gui.utils;

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
