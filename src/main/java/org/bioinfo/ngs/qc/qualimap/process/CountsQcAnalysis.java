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
package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.AppSettings;
import org.bioinfo.ngs.qc.qualimap.common.LoggerThread;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 4:03 PM
 */
public class CountsQcAnalysis extends AnalysisProcess{

    List<CountsSampleInfo> samples;
    Map<Integer,String> conditionNames;
    boolean  reportProgress, compareConditions;
    String inputFilePath,  infoFilePath;
    int numSamples;

    static final String COMPARISON_ANALYSIS = "Comparison";
    private int countsThreshold;

    public CountsQcAnalysis(AnalysisResultManager tabProperties,
                            String homePath,
                            List<CountsSampleInfo> samples) {
        super(tabProperties, homePath);
        this.samples = samples;
        this.reportProgress = false;
        this.compareConditions = false;
        this.inputFilePath = "";
        this.infoFilePath = "";
        this.countsThreshold = 0;
        this.numSamples = samples.size();
    }


    private void setupInputDataDescription(String workDir) throws IOException {

        if (conditionNames.isEmpty()) {
            throw new IOException("The condition names are not set! Can not setup input data description");
        }

        inputFilePath = workDir + File.separator +  "input.txt";
        PrintWriter outWriter = new PrintWriter(new FileWriter(inputFilePath));

        outWriter.write("#Sample\tCondition\tPath\tColumn\n");

        for (CountsSampleInfo sampleInfo : samples) {
            outWriter.write(sampleInfo.name + "\t");
            String conditionName = conditionNames.get(sampleInfo.conditionIndex);
            outWriter.write(conditionName + "\t");
            outWriter.write(sampleInfo.path + "\t");
            outWriter.write(sampleInfo.columnNum + "\n");
        }

        outWriter.close();
    }


    @Override
    public void run() throws Exception {

        String workDir = tabProperties.createDirectory().toString();


        removeSpacesFromNames();
        setupInputDataDescription(workDir);

        String commandString = createCommand(workDir);
        if (loggerThread != null) {
            loggerThread.logLine(commandString);
        }

        //reportProgress("Running R script");
        Process p = Runtime.getRuntime().exec(commandString);

        if (loggerThread != null) {
            BufferedReader outputReader = new BufferedReader( new InputStreamReader(
                                new SequenceInputStream( p.getInputStream(), p.getErrorStream() )
                    ) );
            loggerThread.start(outputReader);
        }

        int res = p.waitFor();

        if (loggerThread != null) {
            loggerThread.join();
        }

        if (res != 0) {
            throw new RuntimeException("The RScript process finished with error.\n" +
                    " Check log for details.");
        }

        StatsReporter statsReporter = new StatsReporter();
        statsReporter.setName("Global");
        statsReporter.setFileName( "GlobalReport" );

        if (!loadBufferedImages(statsReporter, workDir) ) {
            throw new RuntimeException("No plots for global analysis generated.");
        }

        prepareInputDescription(statsReporter);
        tabProperties.addReporter(statsReporter);


        if (compareConditions) {
            String compareDirPath = workDir + File.separator + COMPARISON_ANALYSIS;
            StatsReporter reporter = new StatsReporter();
            reporter.setName(COMPARISON_ANALYSIS);
            reporter.setFileName( COMPARISON_ANALYSIS + "Report");
            if (!loadBufferedImages(reporter, compareDirPath) ) {
                 throw new RuntimeException("No images generated for comparison of conditions!");
            }
            tabProperties.addReporter(reporter);
        }


        for (CountsSampleInfo sampleInfo : samples) {

            String sampleDirPath = workDir + File.separator + sampleInfo.name;
            StatsReporter reporter = new StatsReporter();
            reporter.setName(sampleInfo.name);
            reporter.setFileName( sampleInfo.name.replaceAll("\\s+","") + "Report");
            if (!loadBufferedImages(reporter, sampleDirPath) ) {
                throw new RuntimeException("No images generated for sample " + sampleInfo.name);
            }
            tabProperties.addReporter(reporter);

        }




    }

    private void removeSpacesFromNames() {

        //TODO: this is because of the problem with NOISeq
        for (SampleInfo s : samples) {
            s.name = s.name.replaceAll("\\s", "_");
        }
        for (Integer index : conditionNames.keySet()) {
            String cName = conditionNames.get(index);
            conditionNames.put(index, cName.replaceAll("\\s", "_"));
        }

    }

    private boolean loadBufferedImages(StatsReporter statsReporter, String outDir) throws IOException {

        List<QChart> chartList = new ArrayList<QChart>();
        int imageCount = 0;

        File dir = new File(outDir);
        if (!dir.exists() || !dir.isDirectory())  {
            return false;
        }

        File[] children = dir.listFiles();
        Arrays.sort(children);

        for (File child : children ) {
            String fileName = child.getName();
            if (fileName.endsWith(".png")) {
                String imageName = fileName.subSequence(3,  fileName.length() - 4).toString().replace('_',' ');
                BufferedImage image = ImageIO.read(new FileInputStream(child));
                chartList.add(new QChart(imageName, imageName, image) );
                imageCount++;
            }
        }

        if (imageCount == 0) {
            return false;
        }

        statsReporter.setChartList(chartList);

        return true;

    }

    private String createCommand(String workDir) {
       String pathToRscript = AppSettings.getGlobalSettings().getPathToRScript();
        String commandString = pathToRscript + " " + homePath
                + File.separator + "scripts"+ File.separator + "countsQC.r";

        commandString += " --homedir " + homePath + File.separator + "scripts";
        commandString += " --input " + inputFilePath;
        commandString += " -k " + countsThreshold;
        if (!infoFilePath.isEmpty()) {
            commandString += " --info " + infoFilePath;
        }

        if (compareConditions) {
            commandString += " --compare";
        }
        commandString += " -o " + workDir;

        return commandString;
    }

    protected void prepareInputDescription(StatsReporter reporter) {

        /*HashMap<String,String> locationParams = new HashMap<String, String>();
        locationParams.put("Upstream offset (bp): ", Integer.toString(cfg.leftOffset) );
        locationParams.put("Downstream offset (bp): ", Integer.toString( cfg.rightOffset) );
        locationParams.put("Bin size: ", Integer.toString( cfg.binSize ));
        reporter.addInputDataSection("Location: ",  locationParams);*/


        HashMap<String,String> sampleParams = new HashMap<String, String>();
        for ( CountsSampleInfo info : samples ) {
            String conditionName = conditionNames.get(info.conditionIndex);
            sampleParams.put(info.name + " " + conditionName , info.path );
        }
        reporter.addInputDataSection("Samples", sampleParams);

        HashMap<String,String> otherParams = new HashMap<String, String>();
        otherParams.put("Species info: ", infoFilePath);
        otherParams.put("Counts threshold: ", "" + countsThreshold );
        reporter.addInputDataSection("Options", otherParams);



    }

    public void setConditionNames(Map<Integer,String> cMap) {
        conditionNames = cMap;
    }

    public void setOutputParsingThread(LoggerThread outputParsingThread) {
        this.loggerThread = outputParsingThread;
    }

    public void setInfoFilePath(String infoFilePath) {
        this.infoFilePath = infoFilePath;
    }

    public static List<CountsSampleInfo> parseInputFile(String inputFilePath,
                                                        ArrayList<String> conditionsList) throws IOException {

        ArrayList<CountsSampleInfo> res = new ArrayList<CountsSampleInfo>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            String[] items = line.split("\\s+");
            if (items.length < 4) {
                throw new IOException("Failed to parse input file " + inputFilePath+
                        " not enough fields in line " + line);
            }

            CountsSampleInfo info = new CountsSampleInfo();
            info.name = items[0];
            String conditionName = items[1];
            if (!conditionsList.contains(conditionName)) {
                conditionsList.add(conditionName);
            }
            info.conditionIndex = conditionsList.indexOf(conditionName) + 1;
            if (info.conditionIndex > 2) {
                throw new IOException("More than 2 conditions detected in the input file!");
            }

            info.path = items[2];
            if (! (new File(info.path)).exists() ) {
                String absPath = new File(inputFilePath).getParent() + File.separator + info.path;
                if (new File(absPath).exists()) {
                    info.path = absPath;
                } else {
                    throw new IOException("Sample " + info.name + ": file " + info.path + " doesn't exist!");
                }
            }

            info.columnNum = Integer.parseInt( items[3] );
            res.add( info );

        }

        return res;

    }

    public void activateComparison() {
        this.compareConditions = true;
    }

    public void setInputFilePath(String filePath) {
        this.inputFilePath = filePath;
    }

    public void setThreshold(int k) {
        this.countsThreshold = k;
    }
}
