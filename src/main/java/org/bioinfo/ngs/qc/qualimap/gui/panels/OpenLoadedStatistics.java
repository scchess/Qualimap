package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.GraphicImagePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.JLabelMouseListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Class to manage the statistics loaded from a determinate file
 * 
 * @author Luis Miguel Cruz
 */
public class OpenLoadedStatistics extends JPanel implements ComponentListener {
	private static final long serialVersionUID = -6408484044729636203L;
	HomeFrame homeFrame;
	protected Logger logger;

	private ImageIcon treeMinusIcon, treePlusIcon;
	/** Panels that contains the dynamic structures */
	private JScrollPane leftScrollPane, rightScrollPane;

	/** Variable to manage the left panel that contains the links to the results */
	public JPanel leftPanel;
    private JLabel initialLabel;
    TabPropertiesVO tabProperties;

    static class RightPanelListener extends MouseAdapter {

        TabPropertiesVO tabProperties;
        JComponent parent;

        public RightPanelListener(JComponent parent, TabPropertiesVO tabProperties) {
            this.parent = parent;
            this.tabProperties=tabProperties;
        }

        public void mousePressed(MouseEvent e) {
               maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
             maybeShowPopup(e);
        }

           private void maybeShowPopup(MouseEvent e) {
               if (e.isPopupTrigger()) {
                   JPopupMenu popup = new JPopupMenu();
                   final String graphicName = tabProperties.getLoadedGraphicName();
                   if (!graphicName.isEmpty())  {
                        JMenuItem savePictureItem = new JMenuItem("Save picture...");
                        savePictureItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                JFileChooser fileChooser = HomeFrame.getFileChooser();
                                fileChooser.setFileFilter( new FileFilter() {
                                    @Override
                                    public boolean accept(File file) {
                                        return true;
                                    }

                                    @Override
                                    public String getDescription() {
                                        return "PNG images";
                                    }
                                });
                                int res = fileChooser.showDialog(parent, "Save");
                                if (res == JFileChooser.APPROVE_OPTION) {
					                String path = fileChooser.getSelectedFile().getAbsolutePath();
                                    if (path.endsWith(".png")) {
                                        path = path + ".png";
                                    }
                                    BufferedImage image = tabProperties.getReporter().getImageMap().get(graphicName);
                                    try {
                                        ImageIO.write(image, "png", new File(path));
                                    } catch (IOException e1) {
                                        JOptionPane.showMessageDialog(parent, "Failed to save image!",
                                                "Save image", JOptionPane.ERROR_MESSAGE);
                                    }

                                }
                            }
                        });
                        popup.add(savePictureItem);

                        // TODO: Each reporter should add own items to the popup menu.
                        if (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EPI) {
                            JMenuItem exportGeneListItem = new JMenuItem("Export gene list...");
                            exportGeneListItem.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    HomeFrame.showExportGenesDialog(parent, tabProperties);
                                }
                            });
                            popup.addSeparator();
                            popup.add(exportGeneListItem);
                        }
                   }

                   popup.show(e.getComponent(),
                              e.getX(), e.getY());
               }
           }


    }

	public OpenLoadedStatistics(HomeFrame homeFrame, TabPropertiesVO tabProperties) {
		super();
		this.homeFrame = homeFrame;
        this.tabProperties = tabProperties;
		treeMinusIcon = new ImageIcon(homeFrame.getClass().getResource(Constants.pathImages + "minus_blue.png"));
        treePlusIcon = new ImageIcon(homeFrame.getClass().getResource(Constants.pathImages + "add.png"));
	}

	/**
	 * Show the statistics loaded into the tab selected
	 * 
	 * @return JSplitPane, User Interface with the data loaded.
	 */
	public JSplitPane getLoadedStatistics() {
		JSplitPane statisticsContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		statisticsContainer.setDividerSize(2);
		int leftPanelWidth = 250;

		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftScrollPane = new JScrollPane(leftPanel);
		leftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		leftScrollPane.setMinimumSize(new Dimension(leftPanelWidth, 500));
		leftScrollPane.setPreferredSize(new Dimension(leftPanelWidth, 2000));
		leftScrollPane.setViewportView(leftPanel);

		rightScrollPane = new JScrollPane();
		rightScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rightScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		tabProperties.getGraphicImage().addComponentListener(this);

        tabProperties.setLastLinkSelected(null);
        tabProperties.setLoadedGraphicName("");

		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
			fillLeftRnaSplit();
		} else if (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EPI) {
            fillEpiSplit();
        } else {
			fillLeftSplit();
		}
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GroupLayout(rightPanel));
		rightScrollPane.setViewportView(rightPanel);
        rightScrollPane.addMouseListener(new RightPanelListener(rightPanel,tabProperties));

		statisticsContainer.setLeftComponent(leftScrollPane);
		statisticsContainer.setRightComponent(rightScrollPane);
		statisticsContainer.validate();
		leftScrollPane.validate();

        validate();

        return statisticsContainer;
    }

    private void fillEpiSplit() {

        JCheckBox checkFirstSection = createResultsCheckBox("Results");
		leftPanel.add(checkFirstSection);

        JLabel j1_0_1 = createInputDescriptionLinkLabel("Input", 0);
		j1_0_1.setToolTipText("Input data description");
        j1_0_1.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
        leftPanel.add(j1_0_1);

        Map<String,BufferedImage> imageMap = tabProperties.getReporter().getImageMap();

        for (Map.Entry<String,BufferedImage> entry : imageMap.entrySet() ) {
            JLabel j = createImageLinkLabel(entry.getKey(), entry.getKey() );
            leftPanel.add(j);
        }

    }

    /**
	 * Function that load the left panel with the statistics links
	 */
	private void fillLeftSplit() {

		boolean isGffSelected = tabProperties.isGffSelected();
		boolean showOutsideStats = tabProperties.getOutsideStatsAvailable();

        String sectionName = isGffSelected ? "Results inside of regions" : "Results";
        JCheckBox checkFirstSection = createResultsCheckBox(sectionName);
        leftPanel.add(checkFirstSection);

        JLabel j1_0 = createSummaryLinkLabel("Summary", Constants.REPORT_INPUT_BAM_FILE);
        j1_0.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
	    leftPanel.add(j1_0);
        initialLabel = j1_0;

        JLabel j1_0_1 = createInputDescriptionLinkLabel("Input", Constants.REPORT_INPUT_BAM_FILE);
		j1_0_1.setToolTipText("Input data description");
        j1_0_1.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
        leftPanel.add(j1_0_1);

        JLabel j1_1 = createImageLinkLabel("Coverage Across Reference", Constants.GRAPHIC_NAME_GENOME_COVERAGE_ACROSS_REFERENCE);
        leftPanel.add(j1_1);

        JLabel j1_2 = createImageLinkLabel("Coverage Histogram", Constants.GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM);
        j1_2.setToolTipText("Frequency histogram of the coverageData");
        leftPanel.add(j1_2);

        JLabel j1_3 = createImageLinkLabel("Coverage Histogram (0-50x)", Constants.GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM_0_50);
        j1_3.setToolTipText("<html>There is often big picks of coverageData across the reference"
                + "<br>and the scale of the Coverage Histogram graph scale may not be adequate." +
                "<br>In order to solve this, in this graph genome locations with a coverageData greater "
                + "<br>than 50X are grouped into the last bin</html>");
        leftPanel.add(j1_3);

        JLabel j1_4 = createImageLinkLabel("Coverage Quota", Constants.GRAPHIC_NAME_GENOME_COVERAGE_QUOTA);
        j1_4.setToolTipText("<html>Provides an easy way of viewing how much reference has been "
                + "sequenced<br>with a coverageData higher than a selected level</html>");
        leftPanel.add(j1_4);

        JLabel readsContent = createImageLinkLabel("Reads content per position", Constants.GRAPHIC_NAME_GENOME_READS_CONTENT);
        readsContent.setToolTipText("Provides relative nucleotide content per read position");
        leftPanel.add(readsContent);

        JLabel gcContentHist = createImageLinkLabel("GC content distribution", Constants.GRAPHIC_NAME_GENOME_GC_CONTENT_PER_WINDOW);
        gcContentHist.setToolTipText("Shows gc content distribution per window ");
        leftPanel.add(gcContentHist);

        JLabel uniqReadsLabel = createImageLinkLabel("Unique reads per position", Constants.GRAPHIC_NAME_GENOME_UNIQUE_READ_COUNTS);
        uniqReadsLabel.setToolTipText("Provides a histogram of unique read starts per position.");
        leftPanel.add(uniqReadsLabel);

        JLabel j1_5 = createImageLinkLabel("Mapping Quality Across Ref.", Constants.GRAPHIC_NAME_GENOME_MAPPING_QUALITY_ACROSS_REFERENCE);
        j1_5.setToolTipText("Mapping Quality Across Reference");
        leftPanel.add(j1_5);

        JLabel j1_6 = createImageLinkLabel("Mapping Quality Histogram", Constants.GRAPHIC_NAME_GENOME_MAPPING_QUALITY_HISTOGRAM);
        j1_6.setToolTipText("Frequency histogram of the mapping quality");
        leftPanel.add(j1_6);

        if(tabProperties.isPairedData()){
            JLabel j1_8 = createImageLinkLabel("Insert Size Across Reference", Constants.GRAPHIC_NAME_GENOME_INSERT_SIZE_ACROSS_REFERENCE);
            j1_8.setToolTipText("Insert size across the reference");
            leftPanel.add(j1_8);

            JLabel j1_7 = createImageLinkLabel("Insert Size Histogram", Constants.GRAPHIC_NAME_GENOME_INSERT_SIZE_HISTOGRAM);
            j1_7.setToolTipText("Frequency histogram of the insert size");
            leftPanel.add(j1_7);
        }

        if (showOutsideStats) {

			JCheckBox checkSecondSection = createResultsCheckBox("Results outside of regions");
			leftPanel.add(checkSecondSection);

			JLabel summary = createSummaryLinkLabel("Summary", Constants.REPORT_OUTSIDE_BAM_FILE);
			summary.setToolTipText("Basic information and statistics for the alignment sequencing input");
            summary.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			leftPanel.add(summary);

            JLabel inputDesc = createInputDescriptionLinkLabel("Input", Constants.REPORT_OUTSIDE_BAM_FILE);
			inputDesc.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
			inputDesc.setToolTipText("Input data description");
            leftPanel.add(inputDesc);

			JLabel j3_1 = createImageLinkLabel("Coverage Across Reference", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_ACROSS_REFERENCE);
			leftPanel.add(j3_1);

			JLabel j3_2 = createImageLinkLabel("Coverage Histogram", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_HISTOGRAM);
			j3_2.setToolTipText("Frequency histogram of the coverageData");
			leftPanel.add(j3_2);

			JLabel j3_3 = createImageLinkLabel("Coverage Histogram (0-50x)", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_HISTOGRAM_0_50);
			j3_3.setToolTipText("There is often big picks of coverageData across the reference " + "and the scale of the Coverage Histogram graph scale may not be adequate. " + "In order to solve this, in this graph genome locations with a coverageData greater " + "than 50X are groped into the last bin");
			leftPanel.add(j3_3);

			JLabel j3_4 = createImageLinkLabel("Coverage Quota", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_QUOTA);
			j3_4.setToolTipText("Provides an easy way of viewing how much reference has been " + "sequenced with a coverageData higher than a selected level");
			leftPanel.add(j3_4);

            JLabel readsContentOutside = createImageLinkLabel("Reads content per position", Constants.GRAPHIC_NAME_OUTSIDE_READS_CONTENT);
            readsContentOutside.setToolTipText("Provides relative nucleotide content per read position");
            leftPanel.add(readsContentOutside);

            JLabel gcContentHistOutside = createImageLinkLabel("GC content per read", Constants.GRAPHIC_NAME_OUTSIDE_GC_CONTENT_PER_WINDOW);
            gcContentHistOutside.setToolTipText("Shows gc content distribution per window ");
            leftPanel.add(gcContentHistOutside);

            JLabel uniqReadsLabelOutside = createImageLinkLabel("Unique reads per position", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_UNIQUE_READ_COUNTS);
            uniqReadsLabelOutside.setToolTipText("Provides a histogram of unique read starts per position.");
            leftPanel.add(uniqReadsLabelOutside);

			JLabel j3_5 = createImageLinkLabel("Mapping Quality Across Ref.", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_ACROSS_REFERENCE);
			j3_5.setToolTipText("Mapping Quality Across Reference");
			leftPanel.add(j3_5);

			JLabel j3_6 = createImageLinkLabel("Mapping Quality Histogram", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_HISTOGRAM);
			j3_6.setToolTipText("Frequency histogram of the mapping quality");
			leftPanel.add(j3_6);

            if(tabProperties.isPairedData()){
                JLabel j3_8 = createImageLinkLabel("Insert Size Across Reference", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_ACROSS_REFERENCE);
                j3_8.setToolTipText("Insert size across the reference");
                leftPanel.add(j3_8);


                JLabel j3_7 = createImageLinkLabel("Insert Size Histogram", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_HISTOGRAM);
                j3_7.setToolTipText("Frequency histogram of the insert size");
                leftPanel.add(j3_7);
            }
        }

	}

	/**
	 * Function that load the left panel with the statistics links for the
	 * RNA-sqe
	 */
	private void fillLeftRnaSplit() {

        boolean infoFileIsSet = tabProperties.getRnaAnalysisVO().getInfoFileIsSet();

        JCheckBox checkFirstSection = createResultsCheckBox("Results");
		leftPanel.add(checkFirstSection);

		JLabel inputDesc = createInputDescriptionLinkLabel("Input", 0 );
        inputDesc.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
        leftPanel.add(inputDesc);

        JLabel j1_0 = createImageLinkLabel("Global Saturation", Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION);
		j1_0.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
		j1_0.setToolTipText("Basic information and statistics for the alignment sequencing input");
		leftPanel.add(j1_0);
		initialLabel = j1_0;

		if (infoFileIsSet) {

			JLabel j1_1 = createImageLinkLabel("Detection per group", Constants.GRAPHIC_NAME_RNA_SATURATION_PER_CLASS);
			leftPanel.add(j1_1);

			JLabel j1_2 = createImageLinkLabel("Counts per group", Constants.GRAPHIC_NAME_RNA_COUNTS_PER_CLASS);
			j1_2.setToolTipText("Frequency histogram of the coverageData");
			leftPanel.add(j1_2);


			JCheckBox checkSaturationSection = createResultsCheckBox("Saturation per group");
			leftPanel.add(checkSaturationSection);

            Map<String, Object> mapGenotypes = tabProperties.getRnaAnalysisVO().getMapClassesInfoFile();
			// Hack to add unknown
			mapGenotypes.put("unknown", "unknown.png");
			Iterator it = mapGenotypes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				JLabel j = createImageLinkLabel(entry.getKey(), entry.getValue());
				leftPanel.add(j);
			}

			JCheckBox checkCountsSection = createResultsCheckBox("Counts & Sequencing depth");
			checkCountsSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
			leftPanel.add(checkCountsSection);

			it = mapGenotypes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				JLabel j = createImageLinkLabel(entry.getKey().toString(), entry.getKey().toString() + "_boxplot.png");
				leftPanel.add(j);
			}
		}

    }

    public void showInitialPage(TabPropertiesVO tabProperties)
    {
        if(Constants.TYPE_BAM_ANALYSIS_DNA ==  tabProperties.getTypeAnalysis() ||
           Constants.TYPE_BAM_ANALYSIS_EXOME ==  tabProperties.getTypeAnalysis()){
			showLeftSideSummaryInformation(Constants.TYPE_BAM_ANALYSIS_DNA, initialLabel);
		}else if (Constants.TYPE_BAM_ANALYSIS_RNA ==  tabProperties.getTypeAnalysis() ){
			showLeftSideInformation(Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION, initialLabel);
		}

    }

	/**
	 * Refresh the left menu showing or hiding the elements depends on the user
	 * selection.
	 * 
	 * @param evt
	 *            ActionEvent that contains the JCheckbox selected by the user.
	 */
	private void refreshLeftMenu(ActionEvent evt) {


        JCheckBox checkBoxSelected = (JCheckBox) evt.getSource();
		boolean showItem = (checkBoxSelected).isSelected();


        int i = 0;
		boolean afterSubmenuChanged = false;
		Component elem;

        while (i < leftPanel.getComponentCount()) {
            elem = leftPanel.getComponent(i);

            // We have to get the last checkbox selected to change the
            // location of the elements of its submenu.
            if (elem instanceof JCheckBox) {
                afterSubmenuChanged = checkBoxSelected.equals(elem);
            } else if (elem instanceof JLabel && afterSubmenuChanged) {
                // If the element is into the submenu of the checkbox selected
                // and this checkbox have to show the elements, we show this
                // element.
                if (showItem) {
                    elem.setVisible(true);
                } else {
                    elem.setVisible(false);
                }
            }

            i++;
        }

        leftPanel.validate();
	}

    /**
	 * Fill the commons properties for the graphic and text links
	 *
	 * @param labelText
	 *            Name displayed of the label
	 * @return JLabel label set
	 */

    JLabel createLinkLabel(final String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), 11));
        label.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_green.png")));
        label.setOpaque(true);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setIconTextGap(0);
        label.setToolTipText(labelText);

        return label;
    }

    JLabel createSummaryLinkLabel(final String labelText, final int reporterIndex) {
        final JLabel label = createLinkLabel(labelText);
        label.addMouseListener(new JLabelMouseListener() {
            public void mouseClicked(MouseEvent arg0) {
                showLeftSideSummaryInformation(reporterIndex, label);
            }
        });

        return label;
    }

     JLabel createInputDescriptionLinkLabel(final String labelText, final int reporterIndex) {
        final JLabel label = createLinkLabel(labelText);
        label.addMouseListener(new JLabelMouseListener() {
            public void mouseClicked(MouseEvent arg0) {
                showLeftSideInputDescription(reporterIndex, label);
            }
        });

        return label;
    }


    JLabel createImageLinkLabel(final String labelText, final String graphicName) {
        final JLabel label = createLinkLabel(labelText);
        label.addMouseListener(new JLabelMouseListener() {
            public void mouseClicked(MouseEvent arg0) {
				showLeftSideInformation(graphicName, label);
			}
		});

		return label;
    }


    JCheckBox createResultsCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setSelected(true);
		checkBox.setIcon(treePlusIcon);
		checkBox.setSelectedIcon(treeMinusIcon);
		checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		checkBox.setAction(getActionShowSubMenu(text));

        return checkBox;
    }

	public void showLeftSideSummaryInformation(int reporterIndex, JLabel label) {
		tabProperties.setLoadedGraphicName("");
        prepareHtmlSummary(getReporter(reporterIndex));
		fillColorLink(label);
	}

    public void showLeftSideInputDescription(int reporterIndex, JLabel label) {
        tabProperties.setLoadedGraphicName("");
        prepareHtmlInputDescription(getReporter(reporterIndex));
        fillColorLink(label);
    }

	private void showLeftSideInformation(String graphicName, JLabel label) {
		if (graphicName != null) {
			showGraphic(graphicName, getReporterByName(graphicName));
		}
		fillColorLink(label);
	}
	
	private BamQCRegionReporter getReporter(int reporterIndex){
		BamQCRegionReporter reporter;
		// Select the reporter that contains the data
		if (reporterIndex == Constants.REPORT_OUTSIDE_BAM_FILE) {
			reporter = tabProperties.getOutsideReporter();
		} else {
			reporter = tabProperties.getReporter();
		}
		return reporter;
	}

	
	private BamQCRegionReporter getReporterByName(String graphicName){
		// TODO: replace this method with getReporter(int index)
        BamQCRegionReporter reporter;

		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0 || 
				tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {

			// Select the reporter that contains the graphics
			if (graphicName.startsWith("outside")) {
				reporter = tabProperties.getOutsideReporter();
			} else {
				reporter = tabProperties.getReporter();
			}

		} else {
			reporter = tabProperties.getReporter();
			
		}
		return reporter;
	}

	/**
	 * Fill the color of the link pressed by the user in the left menu
	 * 
	 * @param label
	 *            label to set the colors
	 */
	private void fillColorLink(JLabel label) {
		//TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());

		if (tabProperties.getLastLinkSelected() != null) {
			tabProperties.getLastLinkSelected().setBackground(null);
			tabProperties.getLastLinkSelected().setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), label.getFont().getSize()));
		}
		if (label != null) {
			label.setBackground(new Color(240, 230, 140));
			label.setFont(HomeFrame.defaultFontItalic);
            label.setSize(label.getPreferredSize());
            label.validate();
			tabProperties.setLastLinkSelected(label);
		}
	}

	/**
	 * Show into the split of the selected tab the selected graphic data.
	 * 
	 * @param name
	 *            Name of the file loaded (if its a graphic file).
	 * @param reporter
	 *            BamQCRegionReporter graphic input values
	 */
	private void showGraphic(String name, BamQCRegionReporter reporter) {

        Object imageToDisplay = reporter.getChart(name);

		tabProperties.setLoadedGraphicName(name);
		// The image can be a JFreeChart generated of come from a file like a
		// BufferedImage
		if (imageToDisplay instanceof JFreeChart) {
			// Create the ChartPanel that contains the chart
			ChartPanel panelImage = new ChartPanel((JFreeChart) imageToDisplay);
			panelImage.setSize(rightScrollPane.getSize());

			rightScrollPane.setViewportView(panelImage);
		} else if (imageToDisplay instanceof BufferedImage) {
			// Get a Singleton to manage the image to display

			GraphicImagePanel panelImage = tabProperties.getGraphicImage();

  			// Set the image with the file image get
			panelImage.setImage((BufferedImage) imageToDisplay);

            // Scale the image
            if (tabProperties.getTypeAnalysis() == Constants.TYPE_BAM_ANALYSIS_EPI ) {
                int width = ((BufferedImage) imageToDisplay).getWidth();
                int height  = ((BufferedImage) imageToDisplay).getHeight();
                panelImage.setPreferredSize(new Dimension(width, height));
                panelImage.resizeImage(width, height);
            } else {
                panelImage.resizeImage(rightScrollPane.getWidth(), rightScrollPane.getHeight());
            }

            rightScrollPane.setViewportView(panelImage);
        }
        //TODO: better approach -> add event listener to homeframe
        homeFrame.updateMenuBar();

    }


    public static StringBuffer prepareHtmlReport(BamQCRegionReporter reporter, TabPropertiesVO tabProperties, int width) {
        StringUtilsSwing sdf = new StringUtilsSwing();

        StringBuffer summaryHtml = new StringBuffer("");
        String postfix = reporter.getNamePostfix();

		summaryHtml.append("<p align=center><a name=\"summary\"> <b>Summary</b></p>" + HtmlJPanel.BR);
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "EEEEEE"));
		summaryHtml.append(HtmlJPanel.COLSTART + "<b>Globals:</b>");
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Reference size" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getBasesNumber()) + HtmlJPanel.COLEND);
        if (reporter.getNumSelectedRegions() > 0) {
            summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number of selected regions" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumSelectedRegions()) + HtmlJPanel.COLEND);
            summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Size of selected regions" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getInRegionsReferenceSize()) + HtmlJPanel.COLEND);
            summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Size of non-selected regions" + HtmlJPanel.COLMID +
                    sdf.formatLong(reporter.getBasesNumber() - reporter.getInRegionsReferenceSize()) + HtmlJPanel.COLEND);
        }
        summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number of reads" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumReads()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of mapped reads" + HtmlJPanel.COLMID + sdf.formatInteger(reporter.getNumMappedReads())
                + " / " + sdf.formatPercentage(reporter.getPercentMappedReads()) + HtmlJPanel.COLEND);
        if (reporter.getNumSelectedRegions() > 0) {
            summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of mapped reads inside of regions"
                    + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumInsideMappedReads())
                    + " / " + sdf.formatPercentage(reporter.getPercentageInsideMappedReads()) + HtmlJPanel.COLEND);
            summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of mapped reads outside of regions"
                    + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumOutsideMappedReads())
                    + " / " + sdf.formatPercentage(reporter.getPercentageOutsideMappedReads()) + HtmlJPanel.COLEND);
        }

        summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of unmapped reads" + HtmlJPanel.COLMID
                + sdf.formatLong(reporter.getNumReads() - reporter.getNumMappedReads()) + "/"
                + sdf.formatPercentage(100.0 - reporter.getPercentMappedReads()) + HtmlJPanel.COLEND);

        summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of paired reads"+ HtmlJPanel.COLMID
                 + sdf.formatLong(reporter.getNumPairedReads()) + "/"
                + sdf.formatPercentage(reporter.getPercentPairedReads()) + HtmlJPanel.COLEND);

        summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of reads both mates paired"+ HtmlJPanel.COLMID
                 + sdf.formatLong(reporter.getNumPairedReads() - reporter.getNumSingletons()) + "/"
                + sdf.formatPercentage( (reporter.getPercentageBothMatesPaired())  ) + HtmlJPanel.COLEND);
        summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of singletons" + HtmlJPanel.COLMID
                + sdf.formatLong(reporter.getNumSingletons()) + "/"
                + sdf.formatPercentage(reporter.getPercentSingletons()) + HtmlJPanel.COLEND);

         summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Read min/max/mean size" + HtmlJPanel.COLMID
                + sdf.formatLong(reporter.getReadMinSize()) + "/"
                + sdf.formatLong(reporter.getReadMaxSize()) + "/"
                 + sdf.formatDecimal(reporter.getReadMeanSize())
                 + HtmlJPanel.COLEND);

        summaryHtml.append(HtmlJPanel.getTableFooter());

		summaryHtml.append(HtmlJPanel.BR);
		summaryHtml.append("<b>ACGT Content" + postfix + ": </b>" + HtmlJPanel.BR);
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of A's" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getaNumber()) +
                " / " + sdf.formatPercentage(reporter.getaPercent())+ HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of C's" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getcNumber()) +
                " / " + sdf.formatPercentage(reporter.getcPercent()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of T's" + HtmlJPanel.COLMID + sdf.formatLong(reporter.gettNumber()) +
                " / " + sdf.formatPercentage(reporter.gettPercent()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of G's" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getgNumber()) +
                " / " + sdf.formatPercentage(reporter.getgPercent()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of N's" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getnNumber()) +
                " / " + sdf.formatPercentage(reporter.getnPercent()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "GC Percentage" + HtmlJPanel.COLMID + sdf.formatPercentage(reporter.getGcPercent()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		summaryHtml.append(HtmlJPanel.BR);
		summaryHtml.append("<b>Coverage" + postfix + ":</b>" + HtmlJPanel.BR);
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Mean" + HtmlJPanel.COLMID + sdf.formatDecimal(reporter.getMeanCoverage()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Standard Deviation" + HtmlJPanel.COLMID + sdf.formatDecimal(reporter.getStdCoverage()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		summaryHtml.append(HtmlJPanel.BR);
		summaryHtml.append("<b>Mapping Quality" + postfix + "</b>");
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Mean Mapping Quality" + HtmlJPanel.COLMID + sdf.formatDecimal(reporter.getMeanMappingQuality()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0) {
			summaryHtml.append(HtmlJPanel.BR);
			summaryHtml.append("<b>Per chromosome statistics " +  postfix + ":</b>" + HtmlJPanel.BR);
			summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
            String pathToChromosomeStats = reporter.getChromosomeFilePath();
			summaryHtml.append(fillHtmlTableFromFile(pathToChromosomeStats));
			summaryHtml.append(HtmlJPanel.getTableFooter());
		}
		summaryHtml.append(HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

        return summaryHtml;


    }

	/**
	 * Show into the split of the selected tab the text values selected.
	 * 
	 * @param reporter
	 *            BamQCRegionReporter data input values
	 */
	private void prepareHtmlSummary(BamQCRegionReporter reporter) {
		HtmlJPanel panelDerecha = new HtmlJPanel();
		panelDerecha.setSize(rightScrollPane.getWidth(), rightScrollPane.getHeight());
		panelDerecha.setFont(HomeFrame.defaultFont);
        int width = rightScrollPane.getWidth() - 100;

        StringBuffer summaryHtml = new StringBuffer();

        summaryHtml.append( HtmlJPanel.getHeader() );
        summaryHtml.append(prepareHtmlReport(reporter, tabProperties, width));
        summaryHtml.append( HtmlJPanel.getHeadFooter() );

        panelDerecha.setHtmlPage(summaryHtml.toString());
		rightScrollPane.setViewportView(panelDerecha);
	}

    private void prepareHtmlInputDescription(BamQCRegionReporter reporter) {
        HtmlJPanel htmlPanel = new HtmlJPanel();
		htmlPanel.setSize(rightScrollPane.getWidth(), rightScrollPane.getHeight());
		htmlPanel.setFont(HomeFrame.defaultFont);
        int width = rightScrollPane.getWidth() - 100;

        StringBuffer inputDesc = new StringBuffer();
        inputDesc.append( HtmlJPanel.getHeader() );
        inputDesc.append( reporter.getInputDescription(width) );
        inputDesc.append( HtmlJPanel.getHeadFooter() );

        htmlPanel.setHtmlPage(inputDesc.toString());

        rightScrollPane.setViewportView(htmlPanel);
    }


	private static String fillHtmlTableFromFile(String pathToChromosomeStats) {
		StringBuffer htmlTable = new StringBuffer("");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(pathToChromosomeStats)));
			String strLine;
			// Iterate the file reading the lines
			while ((strLine = br.readLine()) != null) {
				// Test if the read is the header of the table or not
				if (strLine.startsWith("#")) {
                    htmlTable.append("<th align='left'>Name</th>");
                    htmlTable.append("<th align='left'>Length</th>");
                    htmlTable.append("<th align='left'>Mapped bases</th>");
                    htmlTable.append("<th align='left'>Mean coverage</th>");
                    htmlTable.append("<th align='left'>Standard deviation</th>");
            	} else {
					String[] tableValues = strLine.split("\t");
					htmlTable.append(HtmlJPanel.COLSTART);
					int i = 0;
					for (String s : tableValues) {
					    if (i == 1) {
                            String[] coords = s.split(":");
                            assert(coords.length == 2);
                            long len = Long.parseLong(coords[1]) - Long.parseLong(coords[0]) + 1;
                            s = Long.toString(len);
                        }
                        i++;
                        htmlTable.append(s);
						if (i < tableValues.length) {
							htmlTable.append(HtmlJPanel.COLMID);
						}
					}
					htmlTable.append(HtmlJPanel.COLEND);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return htmlTable.toString();
	}

	/**
	 * Action to show or hide the submenu of graphics images
	 *
     * @param name Action name
	 * @return AbstractAction with the event selected
	 */
	private AbstractAction getActionShowSubMenu(String name) {
		return new AbstractAction(name, null) {
			private static final long serialVersionUID = -6311968455290159751L;

			public void actionPerformed(ActionEvent evt) {
				refreshLeftMenu(evt);
			}
		};
}

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        Component c = componentEvent.getComponent();
        //TabPropertiesVO tabProperties =  homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
        GraphicImagePanel imagePanel = tabProperties.getGraphicImage();
        if (c == imagePanel && tabProperties.getTypeAnalysis() != Constants.TYPE_BAM_ANALYSIS_EPI) {
            imagePanel.resizeImage(c.getWidth(), c.getHeight());
            rightScrollPane.setViewportView(imagePanel);
        }
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {
        //Do nothing
    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {
        //Do nothing
    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {
        //Do nothing
    }
}
