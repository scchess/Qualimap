package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.AppSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kokonech
 * Date: 5/15/14
 * Time: 4:03 PM
 */
public class MultisampleCountsAnalysis extends AnalysisProcess{

    List<CountsSampleInfo> samples;
    Map<Integer,String> conditionNames;
    boolean  reportProgress;
    String inputFilePath;
    ProgressReporter progressReporter;
    int numSamples;


    public MultisampleCountsAnalysis(AnalysisResultManager tabProperties,
                                     String homePath,
                                     List<CountsSampleInfo> samples) {
        super(tabProperties, homePath);
        this.samples = samples;
        this.reportProgress = false;
        this.inputFilePath = "";
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
            throw new RuntimeException("No images generated.");
        }

        prepareInputDescription(statsReporter);
        tabProperties.addReporter(statsReporter);


        File dir = new File(workDir);
        if (dir.exists() && dir.isDirectory())  {
            for (File child : dir.listFiles()) {
                if (child.isDirectory()) {
                     StatsReporter reporter = new StatsReporter();
                     reporter.setName(child.getName());
                     if (!loadBufferedImages(reporter, child.getAbsolutePath()) ) {
                        throw new RuntimeException("No images generated.");
                     }
                     tabProperties.addReporter(reporter);
                }
            }
        }




    }

    private boolean loadBufferedImages(StatsReporter statsReporter, String outDir) throws IOException {

        List<QChart> chartList = new ArrayList<QChart>();
        int imageCount = 0;

        File dir = new File(outDir);
        if (!dir.exists() || !dir.isDirectory())  {
            return false;
        }
        for (File child : dir.listFiles()) {
            String fileName = child.getName();
            if (fileName.endsWith(".png")) {
                String imageName = fileName.subSequence(0,  fileName.length() - 4).toString();
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
        commandString += " -o " + workDir;


        return commandString;
    }

    @Override
    protected void prepareInputDescription(StatsReporter reporter) {
        //To change body of implemented methods use File | Settings | File Templates.
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
}
