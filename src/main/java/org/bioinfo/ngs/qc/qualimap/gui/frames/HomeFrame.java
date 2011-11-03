package org.bioinfo.ngs.qc.qualimap.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.gui.dialogs.AboutDialog;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenFilePanel;
import org.bioinfo.ngs.qc.qualimap.gui.panels.OpenLoadedStatistics;
import org.bioinfo.ngs.qc.qualimap.gui.panels.SavePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.ButtonTabComponent;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;


/**
 * Class that manages the principal frames of the application.
 * 
 * @author Luis Miguel Cruz
 */

public class HomeFrame extends JFrame implements WindowListener, ActionListener, MouseListener{
	private static final long serialVersionUID = -3290549319383957609L;
	
	public static String outputpath =File.separator+"tmp/outputs"+File.separator; 
	
	private static String title = "QualiMap v.1.0";
	public static Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	public static Font smallFont = new Font(Font.DIALOG, Font.PLAIN, 10);
	public static Font defaultFontItalic = new Font(Font.DIALOG, Font.ITALIC, 12);
	private static final int FRAME_WIDTH = 1000;
	private static final int FRAME_HEIGHT = 600;
	private int screenHeight;
	private int screenWidth;
	
	
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
	private List<TabPropertiesVO> listTabsProperties;

	/** Dialog to show beside the window */
	private JDialog popUpDialog;

	/**
	 * Variable to store the type of analysis to do
	 */
	private Integer typeAnalysis;
	
	JFileChooser fileOpenChooser, fileSaveChooser;

	public boolean isWebStart;
    private SplashWindow splashWindow;

    public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new HomeFrame();
			}
		});
	}

	public HomeFrame() {
		super(title);
		System.out.println(title);
		isWebStart = isRunningJavaWebStart();
		if(isWebStart){
			copyFilesFromResourcesToFolder();
		}
		if(this.getQualimapFolder()==null)this.setQualimapFolder(new File("").getAbsolutePath()+File.separator);
		logger = new Logger(this.getClass().getName());
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


	public void copyFile(InputStream from ,File to){
		InputStreamReader asd = new InputStreamReader(from);
		FileReader in =  (FileReader)asd;
		FileWriter out;
        try {
	        out = new FileWriter(to);
	        int c;

	        while ((c = in.read()) != -1)
	        	out.write(c);

	        in.close();
	        out.close();
        } catch (Exception e) {
	        e.printStackTrace();
        }
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
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
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
		this.listTabsProperties = new LinkedList<TabPropertiesVO>();
		this.pack();
		this.validate();
	}
	
	

	private void createMenuBar() {
		setJMenuBar( new JMenuBar());
		JMenu analysisMenu = getMenu("Analysis",KeyEvent.VK_A);
		JMenu fileMenu = getMenu("Project",KeyEvent.VK_F);
		JMenu helpMenu = getMenu("Help",KeyEvent.VK_H);
		
		analysisMenu.add(addMenuItem("Genomic", "genomic", "chart_curve_add.png", "ctrl pressed G"));
		analysisMenu.add(addMenuItem("Genomic Region", "genomicregion", "chart_curve_add.png", "ctrl pressed R"));
		analysisMenu.add(addMenuItem("Counts", "counts", "chart_curve_add.png", "ctrl pressed C"));
		analysisMenu.addSeparator();
		analysisMenu.add(addMenuItem("Exit QualiMap", "exit", "door_out.png", "ctrl pressed Q"));
		
		fileMenu.add(addMenuItem("Open Project (.zip)", "openproject", "open_folder.png", "ctrl pressed O"));
		fileMenu.add(addMenuItem("Save Project", "saveproject", "save_zip.png", "ctrl pressed S"));
		fileMenu.add(addMenuItem("Export as PDF", "exportpdf", "save_pdf.png", "ctrl pressed P"));
		fileMenu.addSeparator();
		fileMenu.add(addMenuItem("Close All Tabs", "closealltabs", null,"ctrl pressed A"));
		
		helpMenu.add(addMenuItem("About QualiMap", "about", "help.png","ctrl pressed H"));
		helpMenu.add(addMenuItem("QualiMap Online", "qualionline", "help.png",null));
		helpMenu.add(addMenuItem("CIPF BioInfo Web", "bioinfoweb", "help.png",null));
	}
	
	private JMenu getMenu(String name, int vkH) {
		JMenu m = new JMenu();
		m.setText(name);
		m.setMnemonic(vkH);
		m.setSize(new Dimension(30, 30));
		this.getJMenuBar().add(m);
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

	public void addNewPane(final OpenFilePanel openFilePanel, final TabPropertiesVO tabProperties) {
		
		this.getPopUpDialog().setVisible(false);
		this.remove(this.getPopUpDialog());
		
		final JScrollPane inputScrollPane = new JScrollPane();
		inputScrollPane.setViewportView(null);
		listTabsProperties.add(tabProperties);
		if (aTabbedPane == null) {
			aTabbedPane = new JTabbedPane();
		}
		// Cutting the file name if necessary
		String fileName = StringUtilsSwing.formatFileName(openFilePanel.getInputFile().getName());
		ImageIcon ic = new ImageIcon(getClass().getResource(Constants.pathImages + "chart_curve.png"));
		String prefix = "";
		if(Constants.TYPE_BAM_ANALYSIS_DNA==typeAnalysis){
			prefix = "Genomic: ";
		}else if (Constants.TYPE_BAM_ANALYSIS_EXOME==typeAnalysis){
			prefix = "Region: ";
		}else if (Constants.TYPE_BAM_ANALYSIS_RNA==typeAnalysis){
			prefix = "Counts: ";
		}
		
		aTabbedPane.addTab(prefix + fileName, ic, inputScrollPane,prefix + openFilePanel.getInputFile().getName());
		aTabbedPane.setTabComponentAt(aTabbedPane.indexOfComponent(inputScrollPane), new ButtonTabComponent(aTabbedPane, ic, prefix + openFilePanel.getInputFile().getName()));
		aTabbedPane.setSelectedIndex(aTabbedPane.getTabCount() - 1);
		OpenLoadedStatistics op = new OpenLoadedStatistics(this);
        aTabbedPane.setComponentAt(aTabbedPane.getSelectedIndex(), op.getLoadedStatistics());
		aTabbedPane.setToolTipText(fileName);
		aTabbedPane.addMouseListener(this);
		this.getContentPane().add(aTabbedPane);
		aTabbedPane.validate();
		this.validate();
        this.pack();
		
		if(Constants.TYPE_BAM_ANALYSIS_DNA==typeAnalysis){
			op.showSummary();
		}else if (Constants.TYPE_BAM_ANALYSIS_EXOME==typeAnalysis){
			op.showSummary();
		}else if (Constants.TYPE_BAM_ANALYSIS_RNA==typeAnalysis){
			op.showImage(Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION);
		}	
		op.resizeLeftPanel();
        aTabbedPane.validate();
		inputScrollPane.validate();
		op.leftPanel.validate();
    }
	
	/**
	 * Private function that erase from the disk the temporal directories that
	 * contains the output data generated by the application.
	 */
	private void deleteOutputFolders() {
		TabPropertiesVO tabProperties;
		File dir = new File(HomeFrame.outputpath);

		if (dir != null && dir.isDirectory()) {
			for (int i = 0; i < listTabsProperties.size(); i++) {
				tabProperties = listTabsProperties.get(i);

				deleteOutputIndivifualFolder(tabProperties);
			}
		}
	}

	/**
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
			logger.debug("Cannot delete directory " + path.toString() + ". It does not exists");
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
	
	public void myexit() {
		int n = JOptionPane.showConfirmDialog(this, "Close QualiMap?", "Close QualiMap?", JOptionPane.OK_CANCEL_OPTION);
		if (n == 0) {
			deleteOutputFolders();
			System.exit(0);
		}
	}
	
	public void windowActivated(final WindowEvent arg0) {}
	public void windowClosed(final WindowEvent arg0) {}
	public void windowClosing(final WindowEvent arg0) {
		this.myexit();
	}
	public void windowDeactivated(final WindowEvent arg0) {}
	public void windowDeiconified(final WindowEvent arg0) {}
	public void windowIconified(final WindowEvent arg0) {}
	public void windowOpened(final WindowEvent arg0) {}

	@Override
    public void actionPerformed(ActionEvent e) {
	    splashWindow.setVisible(false);
        if(e.getActionCommand().equalsIgnoreCase("exit")){
	    	this.myexit();
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
	    		aTabbedPane.removeAll();
				aTabbedPane = new JTabbedPane();
				this.validate();
			}
	    }else if(e.getActionCommand().equalsIgnoreCase("exportpdf")){
	    	if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
				TabPropertiesVO tabProperties = listTabsProperties.get(aTabbedPane.getSelectedIndex());

				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporter() != null && tabProperties.getReporter().getBamFileName() != null && tabProperties.getReporter().getBamFileName().length() > 0) {
					SavePanel pathSaveDialog = new SavePanel();

					popUpDialog = pathSaveDialog.getSaveFilePanel(HomeFrame.this, Constants.FILE_EXTENSION_PDF_FILE);
					popUpDialog.setModal(true);
					popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					popUpDialog.setLocationRelativeTo(HomeFrame.this);
					popUpDialog.setVisible(true);
				}
			}
	    }
	    else if(e.getActionCommand().equalsIgnoreCase("saveproject")){
	    	// First of all, we test if there is a tab selected
			if (aTabbedPane != null && aTabbedPane.getTabCount() > 0) {
				TabPropertiesVO tabProperties = listTabsProperties.get(aTabbedPane.getSelectedIndex());
				// We test if this tab has result values or is an input tab
				if (tabProperties != null && tabProperties.getReporter() != null) {
					if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0 || tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {
						SavePanel pathSaveDialog = new SavePanel();

						popUpDialog = pathSaveDialog.getSaveFilePanel(HomeFrame.this, Constants.FILE_EXTENSION_COMPRESS_FILE);
						popUpDialog.setModal(true);
						popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						popUpDialog.setLocationRelativeTo(HomeFrame.this);
						popUpDialog.setVisible(true);
					} else if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
						SavePanel pathSaveDialog = new SavePanel();

						popUpDialog = pathSaveDialog.getSaveFilePanel(HomeFrame.this, Constants.FILE_EXTENSION_COMPRESS_FILE);
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
	    }else if(e.getActionCommand().equalsIgnoreCase("genomic")){
	    	runAnalysis(Constants.TYPE_BAM_ANALYSIS_DNA);
	    }else if(e.getActionCommand().equalsIgnoreCase("genomicregion")){
	    	runAnalysis(Constants.TYPE_BAM_ANALYSIS_EXOME);
	    }else if(e.getActionCommand().equalsIgnoreCase("counts")){
	    	runAnalysis(Constants.TYPE_BAM_ANALYSIS_RNA);
	    }
    }
	
	private void runAnalysis(int type){
    	//TODO: get rid of the typeAnalysis variable
        HomeFrame.this.setTypeAnalysis(type);
		OpenFilePanel inputDataBamAnalysisDialog = new OpenFilePanel();

		if(type==Constants.TYPE_BAM_ANALYSIS_DNA){
			//popUpDialog = new GenomicAnalysisDialog(this,dim,"Select Genomic Dataset");
			popUpDialog = inputDataBamAnalysisDialog.getOpenBamAnalysisDnaFilePanel(HomeFrame.this, dim, "Select Genomic Dataset");
		}
		else if(type==Constants.TYPE_BAM_ANALYSIS_EXOME){
			//popUpDialog = new GenomicAnalysisDialog(this,dim,"Select Genomic Region Dataset");
			
			popUpDialog = inputDataBamAnalysisDialog.getOpenBamAnalysisDnaFilePanel(HomeFrame.this, dim, "Select Genomic Region Dataset");
		}
		else if(type==Constants.TYPE_BAM_ANALYSIS_RNA){
			popUpDialog = inputDataBamAnalysisDialog.getOpenBamAnalysisRnaFilePanel(HomeFrame.this, dim, "Select Counts");
		}
		popUpDialog.setModal(true);
		popUpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		popUpDialog.setLocationRelativeTo(HomeFrame.this);
		popUpDialog.setVisible(true);
	}
	
	
	public JFrame getCurrentInstance() {
		return this;
	}

	public String getQualimapFolder() {
		return qualimapFolder;
	}

	public void setQualimapFolder(String qualimapFolder) {
		this.qualimapFolder = qualimapFolder;
		System.out.println("QualiMapHome: "+qualimapFolder);
	}

	public JTabbedPane getTabbedPane() {
		return aTabbedPane;
	}

	public JDialog getPopUpDialog() {
		return popUpDialog;
	}

	public void setPopUpDialog(JDialog popUpDialog) {
		this.popUpDialog = popUpDialog;
	}

	public Integer getTypeAnalysis() {
		return typeAnalysis;
	}

	public void setTypeAnalysis(Integer typeAnalysis) {
		this.typeAnalysis = typeAnalysis;
	}

	public JFileChooser getFileOpenChooser() {
		return fileOpenChooser;
	}

	public void setFileOpenChooser(JFileChooser fileOpenChooser) {
		this.fileOpenChooser = fileOpenChooser;
	}

	public JFileChooser getFileSaveChooser() {
		return fileSaveChooser;
	}

	public void setFileSaveChooser(JFileChooser fileSaveChooser) {
		this.fileSaveChooser = fileSaveChooser;
	}

	public List<TabPropertiesVO> getListTabsProperties() {
	    return this.listTabsProperties;
    }

	@Override
    public void mouseClicked(MouseEvent event) {
		// Events to change the tab name and close the selected tab
		if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem renameBtn = new JMenuItem("Rename Selected Tab");
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
		FileWriter  fw = null;
		InputStreamReader isr = null;
		String temp = "";
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
            } catch (IOException e) {}
		}
	}
}
