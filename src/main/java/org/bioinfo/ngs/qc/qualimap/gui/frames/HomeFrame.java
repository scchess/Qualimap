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
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AboutDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.ComputeCountsDialog;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.ExportGeneListDialog;
import org.bioinfo.ngs.qc.qualimap.gui.panels.*;
import org.bioinfo.ngs.qc.qualimap.gui.utils.*;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;
import org.bioinfo.ngs.qc.qualimap.utils.LODFileChooser;


/**
 * Class that manages the principal frames of the application.
 * 
 * @author Luis Miguel Cruz
 * @author kokonech
 */

public class HomeFrame extends JFrame implements WindowListener, ActionListener, MouseListener {
	private static final long serialVersionUID = -3290549319383957609L;
	
	public static String outputpath =File.separator+"tmp"+File.separator + "qualimap";
	
	//private static String title = "QualiMap v.1.0.1";
	public static Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	public static Font smallFont = new Font(Font.DIALOG, Font.PLAIN, 10);
	public static Font defaultFontItalic = new Font(Font.DIALOG, Font.ITALIC, 12);
	private static final int FRAME_WIDTH = 1000;
	private static final int FRAME_HEIGHT = 600;
	private int screenHeight;
	private int screenWidth;

    public static final String BAMQC_COMMAND = "bamqc";
    public static final String COUNTSQC_COMMAND = "counts";
    public static final String CLUSTERING_COMMAND = "clustering";
    public static final String CALC_COUNTS_COMMAND = "calc-counts";

    // Menu items with configurable state
    JMenuItem saveReportItem, exportToPdfItem, exportToHtmlItem, openReportItem, exportGeneListItem, closeAllTabsItem;
	JMenuItem openPdfItem;
    
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
	private Map<Component,TabPropertiesVO> tabsPropertiesMap;

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
            Process p = Runtime.getRuntime().exec("Rscript --version");
            BufferedReader outputReader = new BufferedReader( new InputStreamReader(
                                new SequenceInputStream( p.getInputStream(), p.getErrorStream() )
            ) );
            int res = p.waitFor();
            if (res != 0) {
                return "Rscript process resulted with non-zero exit code";
            }

            String output = outputReader.readLine();
            Pattern pattern = Pattern.compile("2.(\\d\\d)");
            Matcher matcher = pattern.matcher(output);
            boolean versionOk = false;
            if (matcher.find() ) {
                int minorVersion = Integer.parseInt( matcher.group(1) );
                if (minorVersion >= 14) {
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
        final ArrayList<String> missingPackages = new ArrayList<String>();

        final Process p = Runtime.getRuntime().exec("Rscript " + path + "scripts/init.r");
        Thread outputReadingThread = new Thread(new Runnable() { public void run() {
            BufferedReader outputReader = new BufferedReader( new InputStreamReader ( p.getInputStream() ) );
            String line;
            try {
                while ((line = outputReader.readLine()) != null) {
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
            throw new RuntimeException("R process finished with non-zero exit code");
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
        this.tabsPropertiesMap = new HashMap<Component, TabPropertiesVO>();
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
        exportToHtmlItem = addMenuItem("Export as HTML", "exporthtml", "save_zip.png", "ctrl pressed H");
        fileMenu.add(exportToHtmlItem);
        exportToPdfItem = addMenuItem("Export as PDF", "exportpdf", "save_pdf.png", "ctrl pressed P");
        fileMenu.add(exportToPdfItem);
        exportGeneListItem = addMenuItem("Export feature list", "exportgenelist", "save_zip.png", null);
        fileMenu.add(exportGeneListItem);
        fileMenu.addSeparator();
        fileMenu.add(addMenuItem("Exit QualiMap", "exit", "door_out.png", "ctrl pressed Q"));

        analysisMenu.add(addMenuItem(AnalysisType.BAM_QC.toString() , BAMQC_COMMAND, "chart_curve_add.png", "ctrl pressed G"));
        JMenuItem rnaSeqItem =   addMenuItem(AnalysisType.COUNTS_QC.toString(), COUNTSQC_COMMAND, "chart_curve_add.png", "ctrl pressed C");
        rnaSeqItem.setEnabled(countsQCPackagesAvailable);
        analysisMenu.add(rnaSeqItem);
        JMenuItem epiMenuItem =  addMenuItem(AnalysisType.CLUSTERING.toString(), CLUSTERING_COMMAND, "chart_curve_add.png", "ctrl pressed E");
        epiMenuItem.setEnabled(clusteringPacakgesAvailble);
        analysisMenu.add(epiMenuItem);

		closeAllTabsItem =  addMenuItem("Close All Tabs", "closealltabs", null,"ctrl pressed A");
        windowsMenu.add(closeAllTabsItem);

        toolsMenu.add(addMenuItem("Compute counts", CALC_COUNTS_COMMAND, "calculator_edit.png", "ctrl pressed T"));

		if (checkForUserManual()) {
            helpMenu.add(addMenuItem("User Manual", "manual", "help.png", "F1"));
            
        }
        helpMenu.add(addMenuItem("QualiMap Online", "qualionline", "world_go.png", null));
		helpMenu.add(addMenuItem("CIPF BioInfo Web", "bioinfoweb", "world_go.png", null));
        helpMenu.add(addMenuItem("About QualiMap", "about", "help.png","F12"));


        JMenuBar menuBar = getJMenuBar();
        menuBar.add(fileMenu);
        //menuBar.add(analysisMenu);
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
		if(icon!=null)a.setIcon(new ImageIcon(this.getClass().getResource(Constants.pathImages + icon)));
		if(keys!=null)a.setAccelerator(KeyStroke.getKeyStroke(keys));
		return a;
	}

	public void addNewPane(final String inputFileName, final TabPropertiesVO tabProperties) {
		
		//TODO: the dialog has to be closed where it was opened!
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

        op.showInitialPage(tabProperties);

	}
	
	/**
	 * Private function that erase from the disk the temporal directories that
	 * contains the output data generated by the application.
	 */
	private void deleteOutputFolders() {
		File dir = new File(HomeFrame.outputpath);

		if (dir.isDirectory()) {
            Collection<TabPropertiesVO> tabPropertiesVOList = tabsPropertiesMap.values();
			for (TabPropertiesVO tabProperties : tabPropertiesVOList) {
				deleteOutputIndivifualFolder(tabProperties);
			}
		}
	}

	/*
	 * Private function that erase from the disk the temporal directory
	 * specified
	 */
	private void deleteOutputIndivifualFolder(TabPropertiesVO tabProperties) {
		String path =HomeFrame.outputpath + tabProperties.getOutputFolder();

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
        if(e.getActionCommand().equalsIgnoreCase("exit")){
	    	this.closeHomeFrame();
	    }
	    else if (e.getActionCommand().equalsIgnoreCase("about")){
			AboutDialog about = new AboutDialog(HomeFrame.this);
			about.dispose();
			about.pack();
			about.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			about.setLocationRelativeTo(null);
			about.setVisible(true);
	    }
	    else if(e.getActionCommand().equalsIgnoreCase("qualionline")){
	    	openUrl("http://bioinfo.cipf.es/qualimap");
	    }
	    else if(e.getActionCommand().equalsIgnoreCase("bioinfoweb")){
	    	openUrl("http://bioinfo.cipf.es");
	    }
	    else if(e.getActionCommand().equalsIgnoreCase("closealltabs")){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
                int res = JOptionPane.showConfirmDialog(this,
                                "Close all tabs?", "QualiMap", JOptionPane.OK_CANCEL_OPTION);
                if (res == 0) {
	    		    aTabbedPane.removeAll();
				    aTabbedPane = new JTabbedPane();
				    this.validate();
                }
			}
	    }else if(e.getActionCommand().equalsIgnoreCase("exporthtml")){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {

                TabPropertiesVO tabProperties = getSelectedTabPropertiesVO();

				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporter() != null) {
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
	    } else if(e.getActionCommand().equalsIgnoreCase("exportpdf")){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
				TabPropertiesVO tabProperties = getSelectedTabPropertiesVO();

				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporter() != null) {
					exportToPdf();
				} else {
                    JOptionPane.showMessageDialog(this, "Can not export PDF!", "Error", JOptionPane.ERROR_MESSAGE);
                }
			}
	    } else if(e.getActionCommand().equalsIgnoreCase("saveproject")){
	    	// First of all, we test if there is a tab selected
			if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
				TabPropertiesVO tabProperties = getSelectedTabPropertiesVO();
				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporter() != null) {
					if (tabProperties.getTypeAnalysis() == AnalysisType.BAM_QC ) {
						SavePanel pathSaveDialog = new SavePanel();
                    	popUpDialog = pathSaveDialog.getSaveFileDialog(HomeFrame.this, Constants.FILE_EXTENSION_COMPRESS_FILE);
						popUpDialog.setModal(true);
						popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						popUpDialog.setLocationRelativeTo(HomeFrame.this);
						popUpDialog.setVisible(true);
					}
			    }
            }
	    }
	    else if(e.getActionCommand().equalsIgnoreCase("openproject")){
	    	OpenFilePanel inputDataZipDialog = new OpenFilePanel();
			popUpDialog = inputDataZipDialog.getOpenZipFilePanel(HomeFrame.this, dim);
			popUpDialog.setModal(true);
			popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			popUpDialog.setLocationRelativeTo(HomeFrame.this);
			popUpDialog.setVisible(true);
	    }else if(e.getActionCommand().equalsIgnoreCase(BAMQC_COMMAND)){
	    	runBamQC();
        }else if(e.getActionCommand().equalsIgnoreCase(COUNTSQC_COMMAND)){
	        runCountsAnalysis();
	    }else if(e.getActionCommand().equals(CLUSTERING_COMMAND)) {
            runClusteringAnalysis();
        } else if (e.getActionCommand().equals(CALC_COUNTS_COMMAND)) {
            showCountReadsDialog(this);
        } else if (e.getActionCommand().equalsIgnoreCase("exportgenelist")) {
            showExportGenesDialog(this, getSelectedTabPropertiesVO());
        } else if (e.getActionCommand().equals("manual")) {
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

    public static void showExportGenesDialog(Component frame, TabPropertiesVO tabProperties) {
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

    private void runBamQC(){

        popUpDialog = new BamAnalysisDialog(this);
        popUpDialog.setModal(true);
        popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        popUpDialog.setLocationRelativeTo(this);
        popUpDialog.setVisible(true);

	}


    private void runCountsAnalysis(){

        popUpDialog = new CountsAnalysisDialog(this);
        popUpDialog.setModal(true);
        popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        popUpDialog.setLocationRelativeTo(this);
        popUpDialog.setVisible(true);
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
        popUpDialog.setModal(true);
        popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        popUpDialog.setLocationRelativeTo(this);
        popUpDialog.setVisible(true);
    }

    private void exportToPdf() {
        SavePanel pathSaveDialog = new SavePanel();
		popUpDialog = pathSaveDialog.getSaveFileDialog(HomeFrame.this, Constants.FILE_EXTENSION_PDF_FILE);
		popUpDialog.setModal(true);
		popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		popUpDialog.setLocationRelativeTo(HomeFrame.this);
		popUpDialog.setVisible(true);


    }


    public void updateMenuBar() {

        boolean activeTabsAvailable = aTabbedPane != null && aTabbedPane.getTabCount() > 0;
        boolean canExportGeneList = false;
        if (activeTabsAvailable) {
            TabPropertiesVO tabProperties = getSelectedTabPropertiesVO();
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

    public TabPropertiesVO getSelectedTabPropertiesVO() {
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
			renameBtn.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "chart_curve_edit.png")));
			renameBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String title = aTabbedPane.getSelectedComponent().getName();
					//showInputDialog(Component parentComponent, Object message, String title, int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue) 
					String temp = (String) JOptionPane.showInputDialog(null,"Enter the name for the tab", "Tab Name", JOptionPane.QUESTION_MESSAGE, null, null,title);
					if(temp.length()>1){
						aTabbedPane.setTitleAt(aTabbedPane.getSelectedIndex(), temp);
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
