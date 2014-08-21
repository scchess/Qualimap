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
package org.bioinfo.ngs.qc.qualimap.main;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;
import org.bioinfo.ngs.qc.qualimap.process.CountsSampleInfo;
import org.bioinfo.ngs.qc.qualimap.process.CountsQcAnalysis;

/**
 * Created by kokonech
 * Date: 5/19/14
 * Time: 5:03 PM
 */

public class CountsQcTool extends NgsSmartTool{


    private String inputFile;
    private String infoFile;

    private int k;

    private static String INFO_FILE_HUMAN_68;
    private static String INFO_FILE_MOUSE_68;
    final private static int DEFAULT_NUMBER_OF_COUNTS = 5;
    private static final String SPECIES_FOLDER = "species";
    private boolean compareConditions;

    public CountsQcTool(){
        super(Constants.TOOL_NAME_COUNTS_QC, true);

        INFO_FILE_HUMAN_68 = homePath + File.separator + SPECIES_FOLDER +
                File.separator + Constants.FILE_SPECIES_INFO_HUMAN_ENS68;
        INFO_FILE_MOUSE_68 = homePath + File.separator + SPECIES_FOLDER +
                File.separator + Constants.FILE_SPECIES_INFO_MOUSE_ENS68;


        compareConditions = false;
        infoFile = "";
        k = DEFAULT_NUMBER_OF_COUNTS;
    }

    @Override
    protected void initOptions() {
        options.addOption(requiredOption("d", "data", true, "File describing the input data. " +
                "Format of the file is a 4-column tab-delimited table.\nColumn 1: sample name\nColumn 2: condition of the sample" +
                "\nColumn 3: path to the counts data for the sample\nColumn 4: index of the column with counts"));
        options.addOption("i", "info", true, "Path to info file containing genes GC-content, length and type.");
        options.addOption("s", "species", true, "Use built-in info file for the given species: HUMAN or MOUSE.");
        options.addOption("k", "threshold", true, "Threshold for the number of counts");
        options.addOption("c", "compare", false, "Perform comparison of conditions. Currently 2 maximum is possible.");

    }

    @Override
    protected void checkOptions() throws ParseException {

        // input
        if(!commandLine.hasOption("data")){
            throw new ParseException("input data description file is required");
        }else{
            inputFile = commandLine.getOptionValue("data");
            if(!exists(inputFile)) {
                throw new ParseException("input description file (--data) " + inputFile + " is not found");
            }

        }


        // Info file
        if(commandLine.hasOption("info")) {
            infoFile = commandLine.getOptionValue("info");
            if(!exists(infoFile)) {
                throw new ParseException("file of information (--info) " + infoFile + " not found");
            }
        } else if(commandLine.hasOption("species")) {
            String species =  commandLine.getOptionValue("species");

            if(species.equalsIgnoreCase("human")){
                infoFile = INFO_FILE_HUMAN_68;
            }else if(species.equalsIgnoreCase("mouse")){
                infoFile = INFO_FILE_MOUSE_68;
            }else{
                throw new ParseException("species " + species + " not found. Please select [human | mouse]");
            }
        }

        if (commandLine.hasOption("c")) {
            compareConditions = true;
        }


        // threshold for the number of counts
        if(commandLine.hasOption("k")) {
            k = Integer.parseInt(commandLine.getOptionValue("k"));
        }
    }


    @Override
    protected void execute() throws Exception {

        if ( outdir.equals(".") ) {
            outdir = new File(inputFile).getParent() + File.separator + "countsQC_results";
        }
        // init output dir
        initOutputDir();

        AnalysisResultManager resultManager = new AnalysisResultManager(AnalysisType.COUNTS_QC);

        ArrayList<String> conditionNames = new ArrayList<String>();
        List<CountsSampleInfo> samples = CountsQcAnalysis.parseInputFile(inputFile, conditionNames);
        if (samples.size() == 0) {
            System.err.println("The input file " + inputFile + " does not contain any samples. " +
                    "Please check file format.");
            System.exit(-1);
        }

        CountsQcAnalysis countsAnalysis =
                new CountsQcAnalysis(resultManager, homePath, samples);


        countsAnalysis.setInputFilePath(inputFile);

        Map<Integer,String> cMap = new HashMap<Integer, String>();
        for (int i = 0; i < conditionNames.size(); ++i) {
            cMap.put(i + 1, conditionNames.get(i));
        }
        countsAnalysis.setConditionNames(cMap);

        countsAnalysis.setThreshold( k );

        if (compareConditions) {
            countsAnalysis.activateComparison();
        }

        if (!infoFile.isEmpty()) {
            countsAnalysis.setInfoFilePath(infoFile);
        }

        LoggerThread loggerThread = new LoggerThread() {
            public void logLine(String msg) {
                System.out.println(msg);
            }
        };
        countsAnalysis.setOutputParsingThread(loggerThread);


        countsAnalysis.run();

        logger.println("\nPreparing result report");
        exportResult(resultManager);

        logger.println("Finished");
    }


}
