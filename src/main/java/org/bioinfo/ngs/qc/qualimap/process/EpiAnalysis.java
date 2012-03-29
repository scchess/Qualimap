package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.bioinfo.ngs.qc.qualimap.utils.LoggerThread;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kokonech
 * Date: 3/9/12
 * Time: 1:14 PM
 */
public class EpiAnalysis {


    static final String TAG_PARAMETERS = "parameters";
    static final String TAG_EXP_ID = "expID";
    static final String TAG_REGIONS = "regions";
    static final String TAG_FILE = "file";
    static final String TAG_COLUMN = "column";
    static final String TAG_LOCATION = "location";
    static final String TAG_UP = "up";
    static final String TAG_DOWN = "down";
    static final String TAG_FREQ = "freq";
    static final String TAG_SAMPLES = "samples";
    static final String TAG_REPLICATE = "replicate";
    static final String TAG_MEDIPS = "medips";
    static final String TAG_NAME = "name";
    static final String TAG_INPUT = "input";
    //static final String TAG_MICROARRAY = "microarray";
    //static final String TAG_THRESHOLD = "threshold";
    static final String TAG_CLUSTERS = "clusters";
    static final String TAG_NUM = "num";
    static final String TAG_FRAGMENT = "fragment";
    static final String TAG_DIR_OUT ="dirOut";



    static public class ReplicateItem {
        public String name;
        public String medipPath;
        public String inputPath;
    }


    static public class Config {

        public List<ReplicateItem> replicates;
        public String pathToRegions;
        public int leftOffset, rightOffset, binSize;
        public int fragmentLength;
        public String clusters, vizType, experimentName;

        public Config() {
            replicates = new ArrayList<ReplicateItem>();
            leftOffset = 2000;
            rightOffset = 500;
            binSize = 100;
            fragmentLength = 300;
            clusters = "10,15,20,25,30";
            pathToRegions = "";
            vizType = Constants.VIZ_TYPE_HEATMAP;
            experimentName = "Experiment";
        }
    }

    TabPropertiesVO tabProperties;
    Config cfg;
    LoggerThread outputParsingThread;
    JLabel progressStream;
    String homePath;


    public EpiAnalysis(TabPropertiesVO tabProperties, String homePath, Config cfg) {
        this.tabProperties = tabProperties;
        this.cfg = cfg;
        this.homePath = homePath;
        this.outputParsingThread = null;
        this.progressStream = null;
    }


    public void run() throws Exception {

        String workDir = tabProperties.createDirectory().toString();

        reportProgress("Generating configuration file...");
        createConfigFile(workDir, "config.xml");

        // Create the command to execute
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
        if (!loadBufferedImages(tabProperties, workDir) ) {
            throw new RuntimeException("No images generated.");
        }

        prepareInputDescription(tabProperties.getReporter());

    }

     String createCommand(String outputDir) {
        String commandString = "Rscript " + homePath
                + File.separator + "scripts"+ File.separator + "paintLocation.r";

        commandString += " --fileConfig=" + outputDir + "config.xml";
        commandString += " --homedir=" + homePath + File.separator + "scripts";
        commandString += " --vizType=" + cfg.vizType;

        return commandString;
    }

     void createConfigFile(String outDir, String configFileName) throws Exception {

         FileOutputStream stream = new FileOutputStream(outDir + configFileName);

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stream);

        xmlWriter.writeStartDocument();
        xmlWriter.writeCharacters("\n");

        xmlWriter.writeStartElement(TAG_PARAMETERS);
        xmlWriter.writeCharacters("\n\n\t");


        // dirOut
        xmlWriter.writeStartElement(TAG_DIR_OUT);
        xmlWriter.writeCharacters(outDir);
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\n\t");

        // expId
        xmlWriter.writeStartElement(TAG_EXP_ID);
        xmlWriter.writeCharacters(cfg.experimentName);
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\n\t");


        // gene selection
        xmlWriter.writeStartElement(TAG_REGIONS);
        xmlWriter.writeCharacters("\n\t\t");
        xmlWriter.writeCharacters(cfg.pathToRegions);
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\n\t");

        //location
        xmlWriter.writeStartElement(TAG_LOCATION);
        xmlWriter.writeCharacters("\n\t\t");
        xmlWriter.writeStartElement(TAG_UP);
        xmlWriter.writeCharacters(Integer.toString(cfg.leftOffset));
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\t\t");
        xmlWriter.writeStartElement(TAG_DOWN);
        xmlWriter.writeCharacters(Integer.toString(cfg.rightOffset));
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\t\t");
        xmlWriter.writeStartElement(TAG_FREQ);
        xmlWriter.writeCharacters(Integer.toString(cfg.binSize));
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n\n\t");

        // samples
        xmlWriter.writeStartElement(TAG_SAMPLES);

            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeStartElement("sample1");
            List<EpiAnalysis.ReplicateItem> sampleItems = cfg.replicates;
            for (EpiAnalysis.ReplicateItem item : sampleItems) {
                xmlWriter.writeCharacters("\n\t\t\t");
                xmlWriter.writeStartElement(TAG_REPLICATE);

                xmlWriter.writeCharacters("\n\t\t\t\t");
                xmlWriter.writeStartElement(TAG_MEDIPS);
                xmlWriter.writeCharacters(item.medipPath);
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n\t\t\t\t");
                xmlWriter.writeStartElement(TAG_INPUT);
                xmlWriter.writeCharacters(item.inputPath);
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n\t\t\t\t");
                xmlWriter.writeStartElement(TAG_NAME);
                xmlWriter.writeCharacters(item.name);
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n\t\t\t");
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeEndElement();



        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement();

        // MAYBE WILL BE INCLUDED IN FUTURE VERSIONS
        /*if (settingsDialog.microArrayDataAvailable()) {
            xmlWriter.writeCharacters("\n\n\t");
            xmlWriter.writeStartElement(TAG_MICROARRAY);

            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeStartElement(TAG_FILE);
            xmlWriter.writeCharacters(settingsDialog.getMicroArrayDataPath());
            xmlWriter.writeEndElement();

            String[] thresholds = settingsDialog.getMicroArrayThresholds();

            for (String threshold : thresholds) {
                xmlWriter.writeCharacters("\n\t\t");
                xmlWriter.writeStartElement(TAG_THRESHOLD);
                xmlWriter.writeCharacters(threshold);
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeCharacters("\n\t");
            xmlWriter.writeEndElement();
        }*/

        xmlWriter.writeCharacters("\n\n\t");
        xmlWriter.writeStartElement(TAG_CLUSTERS);

        String[] clusters = cfg.clusters.split(",");

        for (String numClusters : clusters) {
            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeStartElement(TAG_NUM);
            xmlWriter.writeCharacters(numClusters);
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement();

        xmlWriter.writeCharacters("\n\n\t");
        xmlWriter.writeStartElement(TAG_FRAGMENT);
        xmlWriter.writeCharacters(Integer.toString(cfg.fragmentLength));

        xmlWriter.writeEndElement();

        xmlWriter.writeCharacters("\n\n");
        xmlWriter.writeEndElement(); // parameters
        xmlWriter.writeCharacters("\n");


        xmlWriter.writeEndDocument();


    }


    void reportProgress(String msg) {
        if (progressStream == null) {
            System.out.println(msg);
        } else {
            progressStream.setText(msg);
        }

    }


    public static boolean loadBufferedImages(TabPropertiesVO tabProperties, String outDir) throws IOException {

        List<QChart> chartList = new ArrayList<QChart>();
        int imageCount = 0;

        File dir = new File(outDir);
        if (!dir.exists() || !dir.isDirectory())  {
            return false;
        }
        for (File child : dir.listFiles()) {
            String fileName = child.getName();
            if (fileName.endsWith(".jpg")) {
                String imageName = fileName.subSequence(0,  fileName.length() - 4).toString();
                BufferedImage image = ImageIO.read(new FileInputStream(child));
                chartList.add(new QChart(imageName, imageName, image) );
                imageCount++;
            }
        }

        if (imageCount == 0) {
            return false;
        }

        BamQCRegionReporter reporter = tabProperties.getReporter();
        reporter.setChartList(chartList);

        return true;

    }

    private void prepareInputDescription(BamQCRegionReporter reporter) {

        HashMap<String,String> selectionParams = new HashMap<String, String>();
        selectionParams.put("Name: ", cfg.experimentName);
        selectionParams.put("Path to regions file: ", cfg.pathToRegions);
        reporter.addInputDataSection("Experiment", selectionParams);

        HashMap<String,String> locationParams = new HashMap<String, String>();
        locationParams.put("Left offset (bp): ", Integer.toString(cfg.leftOffset) );
        locationParams.put("Right offset (bp): ", Integer.toString( cfg.rightOffset) );
        locationParams.put("Bin size: ", Integer.toString( cfg.binSize ));
        reporter.addInputDataSection("Location: ",  locationParams);

        List<EpiAnalysis.ReplicateItem> items = cfg.replicates;

        for ( EpiAnalysis.ReplicateItem item : items ) {
            HashMap<String,String> sampleParams = new HashMap<String, String>();
            sampleParams.put("Sample file: ", item.medipPath );
            sampleParams.put("Control file: ", item.inputPath );
            reporter.addInputDataSection("Sample " + item.name, sampleParams);
        }

        HashMap<String,String> otherParams = new HashMap<String, String>();
        otherParams.put("Thresholds: ", cfg.clusters );
        otherParams.put("Smoothing length: ", Integer.toString(cfg.fragmentLength));
        otherParams.put("Visualization: ", cfg.vizType );
        reporter.addInputDataSection("Options", otherParams);

    }

    public void setOutputParsingThread(LoggerThread outputParsingThread) {
        this.outputParsingThread = outputParsingThread;
    }

    public void setProgressStream(JLabel progressStream) {
        this.progressStream = progressStream;
    }



}
