package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;

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

	public OpenLoadedStatistics(HomeFrame homeFrame) {
		super();
		this.homeFrame = homeFrame;
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

		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
        tabProperties.getGraphicImage().addComponentListener(this);


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

		statisticsContainer.setLeftComponent(leftScrollPane);
		statisticsContainer.setRightComponent(rightScrollPane);
		statisticsContainer.validate();
		leftScrollPane.validate();

        return statisticsContainer;
    }

    private void fillEpiSplit() {

        TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		tabProperties.setLastLinkSelected(null);

        JCheckBox checkFirstSection = createResultsCheckBox("Results");
		leftPanel.add(checkFirstSection);

        Map<String,BufferedImage> imageMap = tabProperties.getReporter().getImageMap();

        for (Map.Entry<String,BufferedImage> entry : imageMap.entrySet() ) {
            JLabel j = createImageLinkLabel(entry.getKey().toString(), entry.getKey().toString() );
            leftPanel.add(j);
        }

    }

    /**
	 * Function that load the left panel with the statistics links
	 */
	private void fillLeftSplit() {

		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
        boolean isGffSelected = tabProperties.isGffSelected();
		boolean showOutsideStats = tabProperties.getOutsideStatsAvailable();
        tabProperties.setLastLinkSelected(null);

        String sectionName = isGffSelected ? "Results inside of region" : "Results";
        JCheckBox checkFirstSection = createResultsCheckBox(sectionName);
        leftPanel.add(checkFirstSection);

        JLabel j1_0 = createSummaryLinkLabel("Summary", Constants.REPORT_INPUT_BAM_FILE);
        leftPanel.add(j1_0);
        initialLabel = j1_0;

        JLabel j1_1 = createImageLinkLabel("Coverage Across Reference", Constants.GRAPHIC_NAME_GENOME_COVERAGE_ACROSS_REFERENCE);
        leftPanel.add(j1_1);

        JLabel j1_2 = createImageLinkLabel("Coverage Histogram", Constants.GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM);
        j1_2.setToolTipText("Frequency histogram of the coverageData");
        leftPanel.add(j1_2);

        JLabel j1_3 = createImageLinkLabel("Coverage Histogram (0-50x)", Constants.GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM_0_50);
        j1_3.setToolTipText("There is often big picks of coverageData across the reference " + "and the scale of the Coverage Histogram graph scale may not be adequate. " + "In order to solve this, in this graph genome locations with a coverageData greater " + "than 50X are groped into the last bin");
        leftPanel.add(j1_3);

        JLabel j1_4 = createImageLinkLabel("Coverage Quota", Constants.GRAPHIC_NAME_GENOME_COVERAGE_QUOTA);
        j1_4.setToolTipText("Provides an easy way of viewing how much reference has been " + "sequenced with a coverageData higher than a selected level");
        leftPanel.add(j1_4);

        JLabel j1_5 = createImageLinkLabel("Mapping Quality Across Ref.", Constants.GRAPHIC_NAME_GENOME_MAPPING_QUALITY_ACROSS_REFERENCE);
        j1_5.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
        j1_5.setToolTipText("Mapping Quality Across Reference");
        leftPanel.add(j1_5);

        JLabel j1_6 = createImageLinkLabel("Mapping Quality Histogram", Constants.GRAPHIC_NAME_GENOME_MAPPING_QUALITY_HISTOGRAM);
        j1_6.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
        j1_6.setToolTipText("Frequency histogram of the mapping quality");
        leftPanel.add(j1_6);

        if(tabProperties.isPairedData()){
            JLabel j1_7 = createImageLinkLabel("Insert Size Histogram", Constants.GRAPHIC_NAME_GENOME_INSERT_SIZE_HISTOGRAM);
            j1_7.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
            j1_7.setToolTipText("Frequency histogram of the insert size");
            leftPanel.add(j1_7);

            JLabel j1_8 = createImageLinkLabel("Insert Size Across Reference", Constants.GRAPHIC_NAME_GENOME_INSERT_SIZE_ACROSS_REFERENCE);
            j1_8.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
            j1_8.setToolTipText("Frequency histogram of the insert size");
            leftPanel.add(j1_8);
        }

        if (showOutsideStats) {

			JCheckBox checkSecondSection = createResultsCheckBox("Results outside of region");
			leftPanel.add(checkSecondSection);

			JLabel summary = createSummaryLinkLabel("Summary", Constants.REPORT_OUTSIDE_BAM_FILE);
			summary.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
			summary.setToolTipText("Basic information and statistics for the alignment sequencing input");
            leftPanel.add(summary);

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

			JLabel j3_5 = createImageLinkLabel("Mapping Quality Across Ref.", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_ACROSS_REFERENCE);
			j3_5.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j3_5.setToolTipText("Mapping Quality Across Reference");
			leftPanel.add(j3_5);

			JLabel j3_6 = createImageLinkLabel("Mapping Quality Histogram", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_HISTOGRAM);
			j3_6.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j3_6.setToolTipText("Frequency histogram of the mapping quality");
			leftPanel.add(j3_6);

			if(tabProperties.isPairedData()){
				JLabel j3_7 = createImageLinkLabel("Insert Size Histogram", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_HISTOGRAM);
				j3_7.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j3_7.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j3_7);
				
				JLabel j3_8 = createImageLinkLabel("Insert Size Across Reference", Constants.GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_ACROSS_REFERENCE);
				j3_8.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j3_8.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j3_8);
			}
        }

	}

	/**
	 * Function that load the left panel with the statistics links for the
	 * RNA-sqe
	 */
	private void fillLeftRnaSplit() {
		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		boolean infoFileIsSet = tabProperties.getRnaAnalysisVO().getInfoFileIsSet();
		tabProperties.setLastLinkSelected(null);

        JCheckBox checkFirstSection = createResultsCheckBox("Results");
		leftPanel.add(checkFirstSection);

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

    public void showInitialPage()
    {
        if(Constants.TYPE_BAM_ANALYSIS_DNA ==  homeFrame.getTypeAnalysis() ||
           Constants.TYPE_BAM_ANALYSIS_EXOME ==  homeFrame.getTypeAnalysis()){
			showLeftSideSummaryInformation(Constants.TYPE_BAM_ANALYSIS_DNA, initialLabel);
		}else if (Constants.TYPE_BAM_ANALYSIS_RNA ==  homeFrame.getTypeAnalysis() ){
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

    JLabel createSummaryLinkLabel(final String labelText, final int analysisType) {
        final JLabel label = createLinkLabel(labelText);
        label.addMouseListener(new JLabelMouseListener() {
            public void mouseClicked(MouseEvent arg0) {
                showLeftSideSummaryInformation(analysisType, label);
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

	public void showLeftSideSummaryInformation(int typeAnalysis, JLabel label) {
		prepareHtmlSummary(getReport(typeAnalysis));
		fillColorLink(label);
	}

	private void showLeftSideInformation(String graphicName, JLabel label) {
		if (graphicName != null) {
			openGraphic(graphicName, getReporterNew(graphicName));
		}
		fillColorLink(label);
	}
	
	private BamQCRegionReporter getReport(int typeAnalysis){
		// TODO: WTF? Another shitty method? What is the difference between this and new?
        BamQCRegionReporter reporter;
		// Select the reporter that contains the data
		if (typeAnalysis == Constants.REPORT_INPUT_BAM_FILE ||
                typeAnalysis == Constants.REPORT_INSIDE_BAM_FILE) {
			reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getReporter();
		} else {
			reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getOutsideReporter();
		}
		return reporter;
	}
	
	private BamQCRegionReporter getReporterNew(String graphicName){
		BamQCRegionReporter reporter;
		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		
		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0 || 
				tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_EXOME) == 0) {

			// Select the reporter that contains the graphics
			if (graphicName.startsWith("outside")) {
				reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getOutsideReporter();
			} else {
				reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getReporter();
			}

		} else {
			reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getReporter();
			
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
		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());

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
	private void openGraphic(String name, BamQCRegionReporter reporter) {

        Object imageToDisplay = reporter.getChart(name);

		// The image can be a JFreeChart generated of come from a file like a
		// BufferedImage
		if (imageToDisplay instanceof JFreeChart) {
			// Create the ChartPanel that contains the chart
			ChartPanel panelImage = new ChartPanel((JFreeChart) imageToDisplay);
			panelImage.setSize(rightScrollPane.getSize());

			rightScrollPane.setViewportView(panelImage);
		} else if (imageToDisplay instanceof BufferedImage) {
			// Get a Singleton to manage the image to display
			TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());

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
		StringUtilsSwing sdf = new StringUtilsSwing();

		StringBuffer summaryHtml = new StringBuffer("");
		int width = rightScrollPane.getWidth() - 100;

		summaryHtml.append(HtmlJPanel.getHeader());
		summaryHtml.append("<p align=center><b>Summary of: " + new File(reporter.getBamFileName()).getName() + "</b></p>" + HtmlJPanel.BR);
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "EEEEEE"));
		summaryHtml.append(HtmlJPanel.COLSTART + "<b>Globals:</b>");
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Reference size" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getBasesNumber()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number of reads" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumReads()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of mapped reads" + HtmlJPanel.COLMID + sdf.formatInteger(reporter.getNumMappedReads())
                + " / " + sdf.formatPercentage(reporter.getPercentMappedReads()) + HtmlJPanel.COLEND);
        summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number/percentage of unmapped reads" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumReads() - reporter.getNumMappedReads())
                        + " / " + sdf.formatPercentage(100.0 - reporter.getPercentMappedReads()) + HtmlJPanel.COLEND);

        //TODO:replace with something about read length disitribtion
        //summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number of mapped bases" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumMappedBases()) + HtmlJPanel.COLEND);
        //summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number of sequenced bases" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumSequencedBases()) + HtmlJPanel.COLEND);
        //summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Number of aligned bases" + HtmlJPanel.COLMID + sdf.formatLong(reporter.getNumAlignedBases()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		summaryHtml.append(HtmlJPanel.BR);
		summaryHtml.append("<b>ACGT Content:</b>" + HtmlJPanel.BR);
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
//		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "AT Percentage" + HtmlJPanel.COLMID + sdf.formatPercentage(reporter.getAtPercent()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		summaryHtml.append(HtmlJPanel.BR);
		summaryHtml.append("<b>Coverage:</b>" + HtmlJPanel.BR);
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Mean" + HtmlJPanel.COLMID + sdf.formatDecimal(reporter.getMeanCoverage()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Standard Deviation" + HtmlJPanel.COLMID + sdf.formatDecimal(reporter.getStdCoverage()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		summaryHtml.append(HtmlJPanel.BR);
		summaryHtml.append("<b>Mapping Quality:</b>");
		summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
		summaryHtml.append(HtmlJPanel.COLSTARTFIX + "Mean Mapping Quality:" + HtmlJPanel.COLMID + sdf.formatDecimal(reporter.getMeanMappingQuality()) + HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());

		if (homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_DNA) == 0) {
			summaryHtml.append(HtmlJPanel.BR);
			summaryHtml.append("<b>Chromosomes:</b>" + HtmlJPanel.BR);
			summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));
			summaryHtml.append(fillHtmlTableFromFile(Constants.NAME_OF_FILE_CHROMOSOMES));
			summaryHtml.append(HtmlJPanel.getTableFooter());
		}
		summaryHtml.append(HtmlJPanel.COLEND);
		summaryHtml.append(HtmlJPanel.getTableFooter());
		panelDerecha.setHtmlPage(summaryHtml.toString());
		rightScrollPane.setViewportView(panelDerecha);
	}

	private String fillHtmlTableFromFile(String fileName) {
		StringBuffer htmlTable = new StringBuffer("");
		TabPropertiesVO tabPropertiesVO = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(HomeFrame.outputpath + tabPropertiesVO.getOutputFolder() + fileName)));
			String strLine;
			// Iterate the file reading the lines
			while ((strLine = br.readLine()) != null) {
				// Test if the read is the header of the table or not
				if (strLine.startsWith("#")) {
                    htmlTable.append("<th align='left'>Name</th>");
                    htmlTable.append("<th align='left'>Length</th>");
                    htmlTable.append("<th align='left'>Mapped bases</th>");
                    htmlTable.append("<th align='left'>Mean coverageData</th>");
                    htmlTable.append("<th align='left'>Std coverageData</th>");
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
        TabPropertiesVO tabProperties =  homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
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
