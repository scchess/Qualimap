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
public class MultisampleCountsAnalysis extends AnalysisProcess{

    List<CountsSampleInfo> samples;
    Map<Integer,String> conditionNames;
    boolean  reportProgress;
    String inputFilePath,  infoFilePath;
    ProgressReporter progressReporter;
    int numSamples;


    public MultisampleCountsAnalysis(AnalysisResultManager tabProperties,
                                     String homePath,
                                     List<CountsSampleInfo> samples) {
        super(tabProperties, homePath);
        this.samples = samples;
        this.reportProgress = false;
        this.inputFilePath = "";
        this.infoFilePath = "";
        this.numSamples = samples.size();
    }


    private void setupInputDataDescription(String workDir) throws IOException {

        if (conditionNames.isEmpty()) {
            throw new IOException("The condition names are not set");
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

        reportProgress("Generating configuration file...");
        if (inputFilePath.isEmpty()) {
            setupInputDataDescription(workDir);
        }

        String commandString = createCommand(workDir);
        if (outputParsingThread != null) {
            outputParsingThread.logLine(commandString);
        }

        reportProgress("Running R script");
        Process p = Runtime.getRuntime().exec(commandString);

        if (outputParsingThread != null) {
            BufferedReader outputReader = new BufferedReader( new InputStreamReader(
                                new SequenceInputStream( p.getInputStream(), p.getErrorStream() )
                    ) );
            outputParsingThread.start(outputReader);
        }

        int res = p.waitFor();

        if (outputParsingThread != null) {
            outputParsingThread.join();
        }

        if (res != 0) {
            throw new RuntimeException("The RScript process finished with error.\n" +
                    " Check log for details.");
        }

        reportProgress("Loading images...");
        StatsReporter statsReporter = new StatsReporter();
        statsReporter.setName("Global");
        if (!loadBufferedImages(statsReporter, workDir) ) {
            throw new RuntimeException("No plots for global analysis generated.");
        }

        prepareInputDescription(statsReporter);
        tabProperties.addReporter(statsReporter);


        for (CountsSampleInfo sampleInfo : samples) {

            String sampleDirPath = workDir + File.separator + sampleInfo.name;
            StatsReporter reporter = new StatsReporter();
            reporter.setName(sampleInfo.name);
            if (!loadBufferedImages(reporter, sampleDirPath) ) {
                throw new RuntimeException("No images generated for sample " + sampleInfo.name);
            }
            tabProperties.addReporter(reporter);


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
        if (!infoFilePath.isEmpty()) {
            commandString += " --info " + infoFilePath;
        }
        commandString += " -o " + workDir;

        return commandString;
    }

    @Override
    protected void prepareInputDescription(StatsReporter reporter) {

        /*HashMap<String,String> locationParams = new HashMap<String, String>();
        locationParams.put("Upstream offset (bp): ", Integer.toString(cfg.leftOffset) );
        locationParams.put("Downstream offset (bp): ", Integer.toString( cfg.rightOffset) );
        locationParams.put("Bin size: ", Integer.toString( cfg.binSize ));
        reporter.addInputDataSection("Location: ",  locationParams);*/


        HashMap<String,String> sampleParams = new HashMap<String, String>();
        for ( CountsSampleInfo info : samples ) {
            sampleParams.put(info.name , info.path );
        }
        reporter.addInputDataSection("Samples", sampleParams);


    }

    @Override
    protected void reportProgress(String msg) {
        if (reportProgress) {
            progressReporter.reportStatus(msg);
        }
    }

    public void setProgressReporter(ProgressReporter reporter) {
        reportProgress = true;
        this.progressReporter = reporter;
    }

    public void setConditionNames(Map<Integer,String> cMap) {
        conditionNames = cMap;
    }

    public void setOutputParsingThread(LoggerThread outputParsingThread) {
        this.outputParsingThread = outputParsingThread;
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
            String[] items = line.split("\t");
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
                throw new IOException("Sample " + info.name + ": file " + info.path + "doesn't exist!");
            }

            info.columnNum = Integer.parseInt( items[3] );
            res.add( info );

        }

        return res;

    }
}
