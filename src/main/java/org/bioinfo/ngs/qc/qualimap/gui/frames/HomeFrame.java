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
package org.bioinfo.ngs.qc.qualimap.gui.frames;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
import org.bioinfo.ngs.qc.qualimap.common.AppSettings;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AboutDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.ComputeCountsDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.ExportGeneListDialog;
import org.bioinfo.ngs.qc.qualimap.gui.panels.*;
import org.bioinfo.ngs.qc.qualimap.gui.utils.*;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;


/**
 * Class that manages the principal frames of the application.
 * 
 * @author Luis Miguel Cruz
 * @author kokonech
 */

public class HomeFrame extends JFrame implements WindowListener, ActionListener, MouseListener {
	private static final long serialVersionUID = -3290549319383957609L;
	
	public static final String outputpath =File.separator+"tmp"+File.separator + "qualimap";
	
	public static Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	//public static Font smallFont = new Font(Font.DIALOG, Font.PLAIN, 10);
	public static Font defaultFontItalic = new Font(Font.DIALOG, Font.ITALIC, 12);
	private static final int FRAME_WIDTH = 1000;
	private static final int FRAME_HEIGHT = 600;
    private static final String WM_COMMAND_EXPORT_GENE_LIST = "exportgenelist";
    private static final String WM_COMMAND_EXIT = "exit";
    private static final String WM_COMMAND_ABOUT = "about";
    private static final String WM_COMMAND_OPEN_MANUAL = "manual";
    private static final String WM_COMMAND_CLOSE_TABS = "closealltabs";
    private static final String WM_COMMAND_WEB_BIOINFO = "bioinfoweb";
    private static final String WM_COMMAND_WEB_QUALIMAP = "qualionline";
    private int screenHeight;
    private int screenWidth;

    private static final String WM_COMMAND_EXPORT_HTML = "exporthtml";
    private static final String WM_COMMAND_EXPORT_PDF = "exportpdf";

    public static final String WM_COMMAND_BAMQC = "bamqc";
    public static final String WM_COMMAND_RNASEQQC = "rnaseqqc";
    public static final String WM_COMMAND_COUNTSQC = "counts";
    public static final String WM_COMMAND_COUNTSQC_MS = "counts-multi";
    public static final String WM_COMMAND_BAMQC_MS = "bamqc-multi";
    public static final String WM_COMMAND_CLUSTERING = "clustering";
    public static final String WM_COMMAND_CALC_COUNTS = "calc-counts";


    // Menu items with configurable state
    JMenuItem exportToPdfItem, exportToHtmlItem, exportGeneListItem, closeAllTabsItem;

	/** Logger to print information */
	protected Logger logger;

	/**
	 * Variable to manage the path of the qualimap folder where we have the R
	 * scripts and resources without compress
	 */
	private String qualimapFolder;
	private Dimension dim;

	/** Variable to contains the tabs of the window */
	private JTabbedPane aTabbedPane;
	/**
	 * Variable that contains the list of paths from the output folders for each
	 * tab in the program
	 */
	private Map<Component,TabPageController> tabsPropertiesMap;

	/** Dialog to show beside the window */
	private JDialog popUpDialog;

	public boolean isWebStart;
    private SplashWindow splashWindow;
    private boolean rIsAvailable;
    private boolean countsQCPackagesAvailable;
    private boolean clusteringPacakgesAvailble;

    private static class TabbedPaneListener implements ContainerListener, ChangeListener {

        HomeFrame homeFrame;

        TabbedPaneListener(HomeFrame homeFrame) {
            this.homeFrame = homeFrame;
        }

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            homeFrame.updateMenuBar();
        }

       @Override
        public void componentAdded(ContainerEvent containerEvent) {
            homeFrame.updateMenuBar();
        }

        @Override
        public void componentRemoved(ContainerEvent containerEvent) {
            Component componentToRemove = containerEvent.getChild();
            homeFrame.tabsPropertiesMap.remove(componentToRemove);
            homeFrame.updateMenuBar();
        }
    }


    public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new HomeFrame(null);
			}
		});
	}

	public HomeFrame(String homeFolder) {
		super("Qualimap " + NgsSmartMain.APP_VERSION);
		isWebStart = isRunningJavaWebStart();
		if(isWebStart){
			copyFilesFromResourcesToFolder();
		}
		if(homeFolder == null){
            setQualimapFolder(new File("").getAbsolutePath()+File.separator);
        }  else {
            setQualimapFolder(homeFolder);
        }
        logger = new Logger(this.getClass().getName());
        logger.println("Qualimap home is " + getQualimapFolder());
        initGUI();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void copyFilesFromResourcesToFolder() {

        //TODO: is this ever tested?
		String outputpath = new File("").getAbsolutePath()+File.separator+".qualimap"+File.separator;
		this.setQualimapFolder(outputpath);
		
		createDirectory(outputpath+"scripts"+ File.separator);
		File newFile = new File(outputpath+"scripts"+ File.separator +"qualimapRscript.r");
        createFile("/org/bioinfo/ngs/qc/qualimap/scripts/qualimapRscript.r",newFile);
        newFile = new File(outputpath+"scripts"+ File.separator +"qualimapRfunctions.r");
        createFile("/org/bioinfo/ngs/qc/qualimap/scripts/qualimapRfunctions.r",newFile);
        
		createDirectory(outputpath+"species"+ File.separator);
		newFile = new File(outputpath+"species"+ File.separator +Constants.FILE_SPECIES_INFO_HUMAN);
        createFile("/org/bioinfo/ngs/qc/qualimap/species/"+Constants.FILE_SPECIES_INFO_HUMAN, newFile);
		newFile = new File(outputpath+"species"+ File.separator +Constants.FILE_SPECIES_GROUPS_HUMAN);
        createFile("/org/bioinfo/ngs/qc/qualimap/species/"+Constants.FILE_SPECIES_GROUPS_HUMAN, newFile);
		newFile = new File(outputpath+"species"+ File.separator +Constants.FILE_SPECIES_INFO_MOUSE);
        createFile("/org/bioinfo/ngs/qc/qualimap/species/"+Constants.FILE_SPECIES_INFO_MOUSE, newFile);
		newFile = new File(outputpath+"species"+ File.separator +Constants.FILE_SPECIES_GROUPS_MOUSE);
        createFile("/org/bioinfo/ngs/qc/qualimap/species/"+Constants.FILE_SPECIES_GROUPS_MOUSE, newFile);
	}


    private String checkForRScript()  {

        String errMsg = "";

        try {
            String testCommand = AppSettings.getGlobalSettings().getPathToRScript() + " --version";
            Process p = Runtime.getRuntime().exec( testCommand );
            BufferedReader outputReader = new BufferedReader( new InputStreamReader(
                                new SequenceInputStream( p.getInputStream(), p.getErrorStream() )
            ) );
            int res = p.waitFor();
            if (res != 0) {
                return "Rscript process resulted with non-zero exit code";
            }

            String output = outputReader.readLine();
            Pattern pattern = Pattern.compile("(\\d).([\\d]+)");
            Matcher matcher = pattern.matcher(output);
            boolean versionOk = false;
            if (matcher.find() ) {
                int majorVersion = Integer.parseInt( matcher.group(1) );
                if (majorVersion  < 2 ) {
                    versionOk = false;
                } else if (majorVersion == 2) {
                    int minorVersion = Integer.parseInt( matcher.group(2) );
                    if (minorVersion >= 14) {
                        versionOk = true;
                    }

                } else {
                    versionOk = true;
                }
            }

            if (!versionOk) {
                errMsg = "Unsupported version of RScript " + output;
                errMsg += "Please use RScript 2.14. or higher. Refer to documentation for details.";
                return errMsg;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to launch RScript: " + e.getMessage();
        }

        return errMsg;
    }

    /**
     * Returns fileChooser which remembers last opened dir
     * @return The file chooser
     */

    public static JFileChooser getFileChooser() {
        return new LODFileChooser();
    }

    private ArrayList<String> getMissingPackages() throws Exception {

        String path = getQualimapFolder() + File.separator;
        final StringBuffer buf = new StringBuffer();
        final ArrayList<String> missingPackages = new ArrayList<String>();

        String pathToRScript = AppSettings.getGlobalSettings().getPathToRScript();
        final Process p = Runtime.getRuntime().exec(pathToRScript + " " + path + "scripts/init.r");
        Thread outputReadingThread = new Thread(new Runnable() { public void run() {
            BufferedReader outputReader = new BufferedReader( new InputStreamReader ( p.getInputStream() ) );
            String line;
            try {
                while ((line = outputReader.readLine()) != null) {
                    buf.append(line);
                    if (line.contains("ERROR!")) {
                        String packageName = line.split(":")[1].trim();
                        System.out.println(packageName);
                        missingPackages.add(packageName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } } );
        outputReadingThread.start();
        int res = p.waitFor();
        outputReadingThread.join();

        if (res != 0) {
            System.err.print("Rscript output:\n" + buf.toString() );
            throw new RuntimeException("R process finished with non-zero exit code.");
        }

        return missingPackages;

    }

    private static String reportMissingPackages(ArrayList<String> missingPacages) {
        StringBuilder message = new StringBuilder();

        message.append("The following R packages are missing:\n");
        for (String packageName : missingPacages) {
            message.append("-").append(packageName).append("\n");
        }
        message.append("Features dependent on these packages are disabled.\n");
        message.append("See user manual for details.\n");

        return message.toString();
    }


    private static boolean isRunningJavaWebStart() {
    	String jwsVersion = System.getProperty("javawebstart.version", null);
    	if(jwsVersion!=null) System.out.println("Java Web Start Version: "+ jwsVersion);
	    return  jwsVersion != null;
	}
	
	private static void createDirectory(String path) {
		path = path.substring(0, path.lastIndexOf(File.separator));
		boolean success = (new File(path)).mkdirs();
		if (success) {
			System.out.println("Created directory: " + path + " ");
		}
	}

	public void initGUI() {
        this.tabsPropertiesMap = new HashMap<Component, TabPageController>();
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);

        String errMsg = checkForRScript();
        rIsAvailable = errMsg.isEmpty();
        if (!rIsAvailable) {
            logger.error(errMsg);
            JOptionPane.showMessageDialog(this,
                    "" + "Some features of Qualimap, relying on the R language, will be disabled.\n" +
                    "To enable them please install R v2.14 or higher and\nmake sure the Rscript command is available" +
                            " from PATH.", "Rscript is not found",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        if (rIsAvailable) {

            try {
                ArrayList<String> missingPackages = getMissingPackages();
                countsQCPackagesAvailable = !missingPackages.contains("optparse");
                clusteringPacakgesAvailble = missingPackages.isEmpty();
                if (!missingPackages.isEmpty()) {
                    String message = reportMissingPackages(missingPackages);
                    JOptionPane.showMessageDialog(this, message,
                            "Checking for required R packages",
                            JOptionPane.INFORMATION_MESSAGE);
                }


            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to check for R dependencies! RScript process finished with errors.",
                        "Checking for required R packages",
                        JOptionPane.INFORMATION_MESSAGE);
            }


        }

        if (this.getClass().getResource(Constants.pathImages + "qualimap_logo_medium.png") != null ) {
			splashWindow = new SplashWindow(this.getClass().getResource(Constants.pathImages + "qualimap_logo_medium.png"), this, 4000);
		}

		try {
			Image iconImage = new ImageIcon(getClass().getResource(Constants.pathImages + "dna.png")).getImage();
			this.setIconImage(iconImage);
		} catch (NullPointerException e) {
			logger.error("Incorrect path of the icon image");
		}

		setMainFrameSize();
		createMenuBar();
		updateMenuBar();

        aTabbedPane = new JTabbedPane();
        TabbedPaneListener tabbedPaneListener = new TabbedPaneListener(this);
        aTabbedPane.addMouseListener(this);
        aTabbedPane.addContainerListener(tabbedPaneListener);
        aTabbedPane.addChangeListener(tabbedPaneListener);
        this.getContentPane().add(aTabbedPane);

        this.pack();
		this.validate();
	}
	
	

	private void createMenuBar() {

        setJMenuBar( new JMenuBar());
        JMenu fileMenu = getMenu("File",KeyEvent.VK_F);
		JMenu analysisMenu = getMenu("New Analysis",KeyEvent.VK_N);
		JMenu toolsMenu = getMenu("Tools", KeyEvent.VK_T);
        JMenu windowsMenu = getMenu("Windows", KeyEvent.VK_W);
        JMenu helpMenu = getMenu("Help",KeyEvent.VK_H);

        analysisMenu.setIcon(new ImageIcon(this.getClass().getResource(Constants.pathImages + "report.png")));
        fileMenu.add(analysisMenu);

        fileMenu.addSeparator();
        exportToHtmlItem = addMenuItem("Export as HTML", WM_COMMAND_EXPORT_HTML, "save_zip.png", "ctrl pressed H");
        fileMenu.add(exportToHtmlItem);
        exportToPdfItem = addMenuItem("Export as PDF", WM_COMMAND_EXPORT_PDF, "save_pdf.png", "ctrl pressed P");
        fileMenu.add(exportToPdfItem);
        exportGeneListItem = addMenuItem("Export feature list", WM_COMMAND_EXPORT_GENE_LIST, "save_zip.png", "" );
        fileMenu.add(exportGeneListItem);
        fileMenu.addSeparator();
        fileMenu.add(addMenuItem("Exit QualiMap", WM_COMMAND_EXIT, "door_out.png", "ctrl pressed Q"));

        analysisMenu.add(addMenuItem(AnalysisType.BAM_QC.toString() , WM_COMMAND_BAMQC,
                "chart_curve_add.png", "ctrl pressed G"));
        analysisMenu.add(addMenuItem(AnalysisType.RNA_SEQ_QC.toString(), WM_COMMAND_RNASEQQC,
                "chart_curve_add.png", "ctrl pressed R"));
        JMenuItem analyzeCountsItem =   addMenuItem(AnalysisType.COUNTS_QC.toString(),
                WM_COMMAND_COUNTSQC, "chart_curve_add.png", "ctrl pressed C");
        analyzeCountsItem.setEnabled(countsQCPackagesAvailable);
        analysisMenu.add(analyzeCountsItem);

        if (System.getenv("QUALIMAP_DEVEL") != null) {
            JMenuItem countsQcItem =   addMenuItem(AnalysisType.MULTISAMPLE_COUNTS_QC.toString(),
                WM_COMMAND_COUNTSQC_MS, "chart_curve_add.png", "");
            countsQcItem.setEnabled(countsQCPackagesAvailable);
            analysisMenu.add(countsQcItem);
        }

        JMenuItem multiBamQcItem =   addMenuItem(AnalysisType.MULTISAMPLE_BAM_QC.toString(),
                    WM_COMMAND_BAMQC_MS, "chart_curve_add.png", "");
                analysisMenu.add(multiBamQcItem);


		closeAllTabsItem =  addMenuItem("Close All Tabs", WM_COMMAND_CLOSE_TABS, "", "ctrl pressed A");
        windowsMenu.add(closeAllTabsItem);

        toolsMenu.add(addMenuItem("Compute counts", WM_COMMAND_CALC_COUNTS, "calculator_edit.png", "ctrl pressed T"));
        JMenuItem epiMenuItem =  addMenuItem(AnalysisType.CLUSTERING.toString(), WM_COMMAND_CLUSTERING,
                "chart_curve_add.png", "ctrl pressed E");
        epiMenuItem.setEnabled(clusteringPacakgesAvailble);
        toolsMenu.add(epiMenuItem);


		if (checkForUserManual()) {
            helpMenu.add(addMenuItem("User Manual", WM_COMMAND_OPEN_MANUAL, "help.png", "F1"));
            
        }
        helpMenu.add(addMenuItem("QualiMap Online", WM_COMMAND_WEB_QUALIMAP, "world_go.png", ""));
		helpMenu.add(addMenuItem("CIPF BioInfo Web", WM_COMMAND_WEB_BIOINFO, "world_go.png", ""));
        helpMenu.add(addMenuItem("About QualiMap", WM_COMMAND_ABOUT, "help.png","F12"));

        JMenuBar menuBar = getJMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(windowsMenu);
        menuBar.add(helpMenu);


   }

    private boolean checkForUserManual() {
        File manualFile =  new File(qualimapFolder + File.separator + "QualimapManual.pdf");
        return manualFile.exists();
    }

    private JMenu getMenu(String name, int vkH) {
		JMenu m = new JMenu();
		m.setText(name);
		m.setMnemonic(vkH);
		m.setSize(new Dimension(30, 30));
		return m;
    }

    private JMenuItem addMenuItem(String text, String command, String icon, String keys){
		JMenuItem a = new JMenuItem();
		a.setText(text);
		a.setActionCommand(command);
		a.addActionListener(this);
		if ( icon.length() > 0 ) {
            a.setIcon(new ImageIcon(this.getClass().getResource(Constants.pathImages + icon)));
        }
		if ( keys.length() > 0 ) {
            a.setAccelerator(KeyStroke.getKeyStroke(keys));
        }
		return a;
	}

	public void addNewPane(final String inputFileName, final TabPageController tabProperties) {
		
		this.getPopUpDialog().setVisible(false);
		this.remove(this.getPopUpDialog());
		
	    AnalysisType typeAnalysis = tabProperties.getTypeAnalysis();

		// Cutting the file name if necessary
		String fileName = StringUtilsSwing.formatFileName(inputFileName);
		ImageIcon ic = new ImageIcon(getClass().getResource(Constants.pathImages + "chart_curve.png"));
		String prefix = typeAnalysis.toString() +  ": ";

        OpenLoadedStatistics op = new OpenLoadedStatistics(this,tabProperties);
        JSplitPane statisticsPane = op.getLoadedStatistics();
        tabsPropertiesMap.put(statisticsPane, tabProperties);

        aTabbedPane.addTab(null, statisticsPane);
        aTabbedPane.setTabComponentAt( aTabbedPane.indexOfComponent(statisticsPane),
                new ButtonTabComponent(aTabbedPane, ic, prefix + fileName) );

        aTabbedPane.setSelectedComponent(statisticsPane);

        op.showInitialPage();

	}
	
	/**
	 * Private function that erase from the disk the temporal directories that
	 * contains the output data generated by the application.
	 */
	private void deleteOutputFolders() {
		File dir = new File(HomeFrame.outputpath);

		if (dir.isDirectory()) {
            Collection<TabPageController> tabPropertiesVOList = tabsPropertiesMap.values();
			for (TabPageController tabController : tabPropertiesVOList) {
				deleteOutputIndivifualFolder(tabController);
			}
		}
	}

	/*
	 * Private function that erase from the disk the temporal directory
	 * specified
	 */
	private void deleteOutputIndivifualFolder(TabPageController tabPageController) {
		String path = HomeFrame.outputpath + tabPageController.getOutputFolder();

		try {
			FileUtils.checkDirectory(path);
			File outputDir = new File(path);
			FileUtils.deleteDirectory(outputDir);
		} catch (IOException e) {
			logger.debug("Cannot delete directory " + path + ". It does not exists");
		}
	}

	public void openUrl(final String url) {
		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
	            desktop.browse(new URI(url));
            } catch (IOException e) {
            	System.out.println("Problem when trying to open a Browser: " + e.getMessage());
            } catch (URISyntaxException e) {
            	System.out.println("Problem when trying to open a Browser: " + e.getMessage());
            }
		}
	}
	
	private void setMainFrameSize() {
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Dimension dimScreen = toolkit.getScreenSize();
		screenHeight = dimScreen.height;
		screenWidth = dimScreen.width;
		this.setBounds((screenWidth - FRAME_WIDTH) / 2, (screenHeight - FRAME_HEIGHT) / 2, FRAME_WIDTH, FRAME_HEIGHT);
		//this.setExtendedState(MAXIMIZED_BOTH);
		this.setBackground(Color.lightGray);
		this.dim = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
		this.setMinimumSize(new Dimension(720, 440));
		this.setMaximumSize(new Dimension(2000, 2000));
		this.setPreferredSize(dim);
		this.getContentPane().setLayout(new BorderLayout());
		this.setResizable(true);
		this.setState(Frame.NORMAL);
		this.setSize(dim);
    }
	
	public void closeHomeFrame() {

        int n = 0;

        if (tabsPropertiesMap.size() > 0) {
            n = JOptionPane.showConfirmDialog(this,
                    "There are open reports. Close QualiMap?", "QualiMap", JOptionPane.OK_CANCEL_OPTION);
        }

        if (n == 0) {
			deleteOutputFolders();
			System.exit(0);
		}
	}
	
	public void windowActivated(final WindowEvent arg0) {}
	public void windowClosed(final WindowEvent arg0) {}
	public void windowClosing(final WindowEvent arg0) {
		this.closeHomeFrame();
	}
	public void windowDeactivated(final WindowEvent arg0) {}
	public void windowDeiconified(final WindowEvent arg0) {}
	public void windowIconified(final WindowEvent arg0) {}
	public void windowOpened(final WindowEvent arg0) {}

	@Override
    public void actionPerformed(ActionEvent e) {
	    splashWindow.setVisible(false);
        if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_EXIT) ){
	    	this.closeHomeFrame();
	    }
	    else if (e.getActionCommand().equalsIgnoreCase(WM_COMMAND_ABOUT)){
			AboutDialog about = new AboutDialog(HomeFrame.this);
			about.dispose();
			about.pack();
			about.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			about.setLocationRelativeTo(null);
			about.setVisible(true);
	    }
	    else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_WEB_QUALIMAP)){
	    	openUrl("http://bioinfo.cipf.es/qualimap");
	    }
	    else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_WEB_BIOINFO)){
	    	openUrl("http://bioinfo.cipf.es");
	    }
	    else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_CLOSE_TABS)){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
                int res = JOptionPane.showConfirmDialog(this,
                        "Close all tabs?", "QualiMap", JOptionPane.OK_CANCEL_OPTION);
                if (res == 0) {
	    		    aTabbedPane.removeAll();
				    aTabbedPane = new JTabbedPane();
				    this.validate();
                }
			}
	    }else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_EXPORT_HTML)){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {

                TabPageController tabProperties = getSelectedTabPageController();

				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporters().size() > 0) {
					SavePanel pathSaveDialog = new SavePanel();
                    popUpDialog = pathSaveDialog.getExportToHtmlFilePanel(this);
                    popUpDialog.setModal(true);
					popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					popUpDialog.setLocationRelativeTo(this);
					popUpDialog.setVisible(true);
				} else {
                    JOptionPane.showMessageDialog(this, "Can not export to HTML!", "Error", JOptionPane.ERROR_MESSAGE);
                }
			}
	    } else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_EXPORT_PDF)){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
				TabPageController tabProperties = getSelectedTabPageController();

				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporters().size() > 0) {
					exportToPdf();
				} else {
                    JOptionPane.showMessageDialog(this, "Can not export PDF!", "Error", JOptionPane.ERROR_MESSAGE);
                }
			}
	    } else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_BAMQC)){
	    	runBamQC();
        }else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_RNASEQQC)){
	    	runRnaSeqQC();
        }else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_COUNTSQC)){
	        runCountsAnalysis();
        }else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_COUNTSQC_MS)){
	        runMultisampleCountsQc();
	    }else if(e.getActionCommand().equalsIgnoreCase(WM_COMMAND_BAMQC_MS)){
	        runMultisampleBamQc();
	    }else if(e.getActionCommand().equals(WM_COMMAND_CLUSTERING)) {
            runClusteringAnalysis();
        } else if (e.getActionCommand().equals(WM_COMMAND_CALC_COUNTS)) {
            showCountReadsDialog(this);
        } else if (e.getActionCommand().equalsIgnoreCase(WM_COMMAND_EXPORT_GENE_LIST)) {
            showExportGenesDialog(this, getSelectedTabPageController());
        } else if (e.getActionCommand().equals(WM_COMMAND_OPEN_MANUAL)) {
            openUserManual();
        }
    }


    private void openUserManual() {
        File manualFile = new File(qualimapFolder + File.separator + "QualimapManual.pdf");
            try {
                Desktop.getDesktop().open(manualFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "No application is registered for viewing PDF files.",
                        getTitle(),
                        JOptionPane.ERROR_MESSAGE);
            }
    }

    public static void showExportGenesDialog(Component frame, TabPageController tabProperties) {
        ExportGeneListDialog dlg;
        try {
            String exprName = tabProperties.getLoadedGraphicName();
            if (exprName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "There is no chart selected! Please select chart.",
                        "Export feature list", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String path = HomeFrame.outputpath + tabProperties.getOutputFolder() + "/" +
                            tabProperties.getLoadedGraphicName() + ".txt";
            dlg = new ExportGeneListDialog(exprName, path);
            dlg.setModal(true);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setLocationRelativeTo(frame);
            dlg.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Can not prepare feature list! "+ e.getMessage(),
                    "Export feature list", JOptionPane.ERROR_MESSAGE);
        }

    }


    private void showPopUpDialog() {
        popUpDialog.setModal(true);
        popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        popUpDialog.setLocationRelativeTo(this);
        popUpDialog.setVisible(true);
    }

    private void runBamQC(){
        popUpDialog = new BamAnalysisDialog(this);
        showPopUpDialog();
	}

    private void runRnaSeqQC(){
        popUpDialog = new RNASeqQCDialog(this);
        showPopUpDialog();
    }

    private void runCountsAnalysis(){
        popUpDialog = new CountsAnalysisDialog(this);
        showPopUpDialog();
    }


    private void runMultisampleCountsQc() {
        popUpDialog = new CountsQcDialog(this);
        showPopUpDialog();
    }

    private void runMultisampleBamQc() {
        popUpDialog = new MultisampleBamQcDialog(this);
        showPopUpDialog();

    }


    static public void showCountReadsDialog(HomeFrame parent) {
        ComputeCountsDialog dlg = new ComputeCountsDialog(parent);
        dlg.setModal(true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }


    private void runClusteringAnalysis(){
        popUpDialog = new EpigeneticAnalysisDialog(this);
        showPopUpDialog();
    }

    private void exportToPdf() {
        SavePanel pathSaveDialog = new SavePanel();
		popUpDialog = pathSaveDialog.getExportToPdfDialog(this);
		popUpDialog.setModal(true);
		popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		popUpDialog.setLocationRelativeTo(HomeFrame.this);
		popUpDialog.setVisible(true);


    }


    public void updateMenuBar() {

        boolean activeTabsAvailable = aTabbedPane != null && aTabbedPane.getTabCount() > 0;
        boolean canExportGeneList = false;
        if (activeTabsAvailable) {
            TabPageController tabProperties = getSelectedTabPageController();
            if (tabProperties != null ) {

                AnalysisType typeAnalysis = tabProperties.getTypeAnalysis();

                canExportGeneList = typeAnalysis == AnalysisType.CLUSTERING &&
                    !tabProperties.getLoadedGraphicName().isEmpty();

            }
        }

        exportToPdfItem.setEnabled(activeTabsAvailable);
        closeAllTabsItem.setEnabled(activeTabsAvailable);
        exportToHtmlItem.setEnabled(activeTabsAvailable);
        exportGeneListItem.setEnabled(canExportGeneList);



    }

    public TabPageController getSelectedTabPageController() {
        Component selectedComponent = aTabbedPane.getSelectedComponent();
        if (selectedComponent != null) {
            return tabsPropertiesMap.get(selectedComponent);
        } else {
            return null;
        }

    }


	public JFrame getCurrentInstance() {
		return this;
	}

	public String getQualimapFolder() {
		return qualimapFolder;
	}

	public void setQualimapFolder(String qualimapFolder) {
		this.qualimapFolder = qualimapFolder;
		//System.out.println("QualiMapHome: "+qualimapFolder);
	}

	public JDialog getPopUpDialog() {
		return popUpDialog;
	}

	@Override
    public void mouseClicked(MouseEvent event) {
		// Events to change the tab name and close the selected tab
		if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1 && tabsPropertiesMap.size() > 0) {
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem renameBtn = new JMenuItem("Rename Tab");
			//renameBtn.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "chart_curve_edit.png")));
			renameBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
                    //Component tabPane =
                    ButtonTabComponent tabPane = (ButtonTabComponent) aTabbedPane.getTabComponentAt(aTabbedPane.getSelectedIndex());
					String title = tabPane.getTitle();
                    //String title = aTabbedPane.getSelectedComponent().getName();
					//showInputDialog(Component parentComponent, Object message, String title, int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue) 
					String temp = (String) JOptionPane.showInputDialog(null,"Enter the name for the tab", "Tab Name", JOptionPane.QUESTION_MESSAGE, null, null,title);
					if(temp.length()>1){
                        tabPane.setTitle(temp);
						//aTabbedPane.setTitleAt(aTabbedPane.getSelectedIndex(), temp);
					}
				}
			});

			popupMenu.add(renameBtn);
			popupMenu.show(event.getComponent(), event.getX(), event.getY() - 10);
		}
	}

	@Override
    public void mouseEntered(MouseEvent arg0) {}
	@Override
    public void mouseExited(MouseEvent arg0) {}
	@Override
    public void mousePressed(MouseEvent arg0) {}
	@Override
    public void mouseReleased(MouseEvent arg0) {}


	private boolean createFile(final String resource, final File file ) {
		BufferedWriter bw = null;
		BufferedReader br = null;
		FileWriter  fw;
		InputStreamReader isr;
		String temp;
		try {
			if (this.getClass().getResource(resource) != null){
				/// write new propertyfile
				fw = new FileWriter(file);
				bw = new BufferedWriter(fw);
				isr = new InputStreamReader(this.getClass().getResource(resource).openStream());
				br = new BufferedReader(isr);
				while ((temp = br.readLine()) != null) {
					temp = temp.trim();
					bw.write(temp);
					bw.newLine();
				}
			}
			return true;
		} catch (final IOException e1) {
			System.out.println("Error while creating file: " + file.getAbsolutePath() +"\nError: "+ e1.getMessage());
			return false;
		} finally{
			try {
				if(bw!=null)bw.close();
				if(br!=null)br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}


}
