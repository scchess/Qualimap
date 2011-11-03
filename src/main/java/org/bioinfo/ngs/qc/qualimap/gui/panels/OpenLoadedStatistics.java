package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.GraphicImagePanel;
import org.bioinfo.ngs.qc.qualimap.gui.utils.JLabelMouseListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.ReferencePosition;
import org.bioinfo.ngs.qc.qualimap.gui.utils.StringUtilsSwing;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Class to manage the statistics loaded from a determinate file
 * 
 * @author Luis Miguel Cruz
 */
public class OpenLoadedStatistics extends JPanel {
	private static final long serialVersionUID = -6408484044729636203L;
	HomeFrame homeFrame;
	protected Logger logger;

	private ImageIcon treeMinusIcon;
	/** Panels that contains the dynamic structures */
	private JScrollPane leftScrollPane, rightScrollPane;

	/** Variable to manage the left panel that contains the links to the results */
	public JPanel leftPanel;

	/**
	 * Variable to store the last component inserted visible inserted into the
	 * left menu, to know the position in the screen for the next element that
	 * the user can see.
	 */
	private Component lastComponentVisible;

	/**
	 * Variable to store the vertical position to set the next element of a
	 * fieldset into the right panel at each moment
	 */
	private int heightValue;
	private JLabel summaryLable;

	public OpenLoadedStatistics(HomeFrame homeFrame) {
		super();
		this.homeFrame = homeFrame;
		treeMinusIcon = new ImageIcon(homeFrame.getClass().getResource(Constants.pathImages + "minus_blue.png"));
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
		leftPanel.setLayout(new GroupLayout(leftPanel));
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

		if (tabProperties.getTypeAnalysis().compareTo(Constants.TYPE_BAM_ANALYSIS_RNA) == 0) {
			fillLeftRnaSplit();
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

	/**
	 * Function that load the left panel with the statistics links
	 */
	private void fillLeftSplit() {
		// TODO: refactor this crap. seriously.
        boolean showAditionalGraphicsInfo = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).isGffSelected();

		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		tabProperties.setLastLinkSelected(null);

		if (!showAditionalGraphicsInfo) {
			JCheckBox checkFirstSection = new JCheckBox("Results");
			checkFirstSection.setSelected(true);
			checkFirstSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
			checkFirstSection.setSelectedIcon(treeMinusIcon);
			checkFirstSection.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			checkFirstSection.setSize(checkFirstSection.getPreferredSize());
			//checkFirstSection.setLocation(0, 3);
			checkFirstSection.setAction(getActionShowSubMenu("Results"));
			leftPanel.add(checkFirstSection);

			Integer marginSubMenu = checkFirstSection.getX() + Constants.marginLeftForElementSubMenu;

			JLabel j1_0 = fillLabelSubMenuText("Summary", checkFirstSection, marginSubMenu, null, Constants.REPORT_INPUT_BAM_FILE);
			j1_0.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
			j1_0.setToolTipText("Basic information and statistics for the alignment sequencing input");
			leftPanel.add(j1_0);
			summaryLable = j1_0;

			JLabel j1_1 = fillLabelSubMenuGraphic("Coverage Across Reference", j1_0, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_COVERAGE_ACROSS_REFERENCE);
			leftPanel.add(j1_1);

			JLabel j1_2 = fillLabelSubMenuGraphic("Coverage Histogram", j1_1, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM);
			j1_2.setToolTipText("Frequency histogram of the coverage");
			leftPanel.add(j1_2);

			JLabel j1_3 = fillLabelSubMenuGraphic("Coverage Histogram (0-50x)", j1_2, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_COVERAGE_HISTOGRAM_0_50);
			j1_3.setToolTipText("There is often big picks of coverage across the reference " + "and the scale of the Coverage Histogram graph scale may not be adequate. " + "In order to solve this, in this graph genome locations with a coverage greater " + "than 50X are groped into the last bin");

			leftPanel.add(j1_3);

			JLabel j1_4 = fillLabelSubMenuGraphic("Coverage Quota", j1_3, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_COVERAGE_QUOTA);
			j1_4.setToolTipText("Provides an easy way of viewing how much reference has been " + "sequenced with a coverage higher than a selected level");
			leftPanel.add(j1_4);

			JLabel j1_5 = fillLabelSubMenuGraphic("Mapping Quality Across Ref.", j1_4, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_MAPPING_QUALITY_ACROSS_REFERENCE);
			j1_5.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j1_5.setToolTipText("Mapping Quality Across Reference");
			leftPanel.add(j1_5);

			JLabel j1_6 = fillLabelSubMenuGraphic("Mapping Quality Histogram", j1_5, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_MAPPING_QUALITY_HISTOGRAM);
			j1_6.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j1_6.setToolTipText("Frequency histogram of the mapping quality");
			leftPanel.add(j1_6);
			if(tabProperties.isPairedData()){
				JLabel j1_7 = fillLabelSubMenuGraphic("Insert Size Histogram", j1_6, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSERT_SIZE_HISTOGRAM);
				j1_7.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j1_7.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j1_7);
				
				JLabel j1_8 = fillLabelSubMenuGraphic("Insert Size Across Reference", j1_7, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSERT_SIZE_ACROSS_REFERENCE);
				j1_8.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j1_8.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j1_8);
			}
			
			// JLabel j1_7 =
			// fillLabelSubMenuGraphic("Nucleotide Rel. Content", j1_6,
			// marginSubMenu,
			// Constants.GRAPHIC_NAME_GENOME_NUCLEOTIDE_RELATIVE_CONTENT);
			// j1_7.setIcon(new
			// ImageIcon(getClass().getResource(Constants.pathImages +
			// "bullet_purple.png")));
			// j1_7.setToolTipText("Nucleotide Relative Content");
			// leftPanel.add(j1_7);
			//
			// JLabel j1_8 =
			// fillLabelSubMenuGraphic("GC/AT Relative Content", j1_7,
			// marginSubMenu,
			// Constants.GRAPHIC_NAME_GENOME_GC_AT_RELATIVE_CONTENT);
			// j1_8.setIcon(new
			// ImageIcon(getClass().getResource(Constants.pathImages +
			// "bullet_purple.png")));
			// leftPanel.add(j1_8);

			lastComponentVisible = leftPanel.getComponent(leftPanel.getComponentCount() - 1);

		} else {
			JCheckBox checkFirstSection = new JCheckBox("Reads inside region");
			checkFirstSection.setSelected(true);
			checkFirstSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
			checkFirstSection.setSelectedIcon(treeMinusIcon);
			checkFirstSection.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			checkFirstSection.setSize(checkFirstSection.getPreferredSize());
			checkFirstSection.setLocation(0, 3);
			checkFirstSection.setAction(getActionShowSubMenu("Reads inside region"));
			leftPanel.add(checkFirstSection);

			Integer marginSubMenu = checkFirstSection.getX() + Constants.marginLeftForElementSubMenu;

			lastComponentVisible = leftPanel.getComponent(leftPanel.getComponentCount() - 1);
			JLabel j2_0 = fillLabelSubMenuText("Summary", checkFirstSection, marginSubMenu, null, Constants.REPORT_INSIDE_BAM_FILE);
			j2_0.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
			j2_0.setToolTipText("Basic information and statistics for the alignment sequencing input");
			leftPanel.add(j2_0);
			summaryLable = j2_0;

			JLabel j2_1 = fillLabelSubMenuGraphic("Coverage Across Reference", summaryLable, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_ACROSS_REFERENCE);
			leftPanel.add(j2_1);

			JLabel j2_2 = fillLabelSubMenuGraphic("Coverage Histogram", j2_1, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_HISTOGRAM);
			j2_2.setToolTipText("Frequency histogram of the coverage");
			leftPanel.add(j2_2);

			JLabel j2_3 = fillLabelSubMenuGraphic("Coverage Histogram (0-50x)", j2_2, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_HISTOGRAM_0_50);
			j2_3.setToolTipText("There is often big picks of coverage across the reference " + "and the scale of the Coverage Histogram graph scale may not be adequate. " + "In order to solve this, in this graph genome locations with a coverage greater " + "than 50X are groped into the last bin");
			leftPanel.add(j2_3);

			JLabel j2_4 = fillLabelSubMenuGraphic("Coverage Quota", j2_3, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_COVERAGE_QUOTA);
			j2_4.setToolTipText("Provides an easy way of viewing how much reference has been " + "sequenced with a coverage higher than a selected level");
			leftPanel.add(j2_4);

			JLabel j2_5 = fillLabelSubMenuGraphic("Mapping Quality Across Ref.", j2_4, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_MAPPING_QUALITY_ACROSS_REFERENCE);
			j2_5.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j2_5.setToolTipText("Mapping Quality Across Reference");
			leftPanel.add(j2_5);

			JLabel j2_6 = fillLabelSubMenuGraphic("Mapping Quality Histogram", j2_5, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_MAPPING_QUALITY_HISTOGRAM);
			j2_6.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j2_6.setToolTipText("Frequency histogram of the mapping quality");
			leftPanel.add(j2_6);

			if(tabProperties.isPairedData()){
				JLabel j2_7 = fillLabelSubMenuGraphic("Insert Size Histogram", j2_6, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_INSERT_SIZE_HISTOGRAM);
				j2_7.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j2_7.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j2_7);
				
				JLabel j2_8 = fillLabelSubMenuGraphic("Insert Size Across Reference", j2_7, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_INSIDE_INSERT_SIZE_ACROSS_REFERENCE);
				j2_8.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j2_8.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j2_8);
			}

			// JLabel j2_7 =
			// fillLabelSubMenuGraphic("Nucleotide Rel. Content", j2_6,
			// marginSubMenu,
			// Constants.GRAPHIC_NAME_GENOME_INSIDE_NUCLEOTIDE_RELATIVE_CONTENT,
			// false);
			// j2_7.setToolTipText("Nucleotide Relative Content");
			// j2_7.setIcon(new
			// ImageIcon(getClass().getResource(Constants.pathImages +
			// "bullet_purple.png")));
			// leftPanel.add(j2_7);
			//
			// JLabel j2_8 =
			// fillLabelSubMenuGraphic("GC/AT Relative Content", j2_7,
			// marginSubMenu,
			// Constants.GRAPHIC_NAME_GENOME_INSIDE_GC_AT_RELATIVE_CONTENT,
			// false);
			// j2_8.setIcon(new
			// ImageIcon(getClass().getResource(Constants.pathImages +
			// "bullet_purple.png")));
			// leftPanel.add(j2_8);

			lastComponentVisible = leftPanel.getComponent(leftPanel.getComponentCount() - 1);

			JCheckBox checkSecondSection = new JCheckBox("Reads outside region");
			checkSecondSection.setSelected(true);
			checkSecondSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
			checkSecondSection.setSelectedIcon(treeMinusIcon);
			checkSecondSection.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			checkSecondSection.setSize(checkSecondSection.getPreferredSize());
            checkSecondSection.setLocation(0, lastComponentVisible.getY() + lastComponentVisible.getHeight() + Constants.marginTopForElementSubMenu);
			checkSecondSection.setAction(getActionShowSubMenu("Reads outside region"));
			leftPanel.add(checkSecondSection);
            lastComponentVisible = leftPanel.getComponent(leftPanel.getComponentCount() - 1);


			// General Summary
			JLabel summary = fillLabelSubMenuText("Summary", checkSecondSection, marginSubMenu, null, Constants.REPORT_OUTSIDE_BAM_FILE);
			summary.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
			summary.setToolTipText("Basic information and statistics for the alignment sequencing input");
            leftPanel.add(summary);


			JLabel j3_1 = fillLabelSubMenuGraphic("Coverage Across Reference", summary, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_ACROSS_REFERENCE);
			leftPanel.add(j3_1);

			JLabel j3_2 = fillLabelSubMenuGraphic("Coverage Histogram", j3_1, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_HISTOGRAM);
			j3_2.setToolTipText("Frequency histogram of the coverage");
			leftPanel.add(j3_2);

			JLabel j3_3 = fillLabelSubMenuGraphic("Coverage Histogram (0-50x)", j3_2, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_HISTOGRAM_0_50);
			j3_3.setToolTipText("There is often big picks of coverage across the reference " + "and the scale of the Coverage Histogram graph scale may not be adequate. " + "In order to solve this, in this graph genome locations with a coverage greater " + "than 50X are groped into the last bin");
			leftPanel.add(j3_3);

			JLabel j3_4 = fillLabelSubMenuGraphic("Coverage Quota", j3_3, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_COVERAGE_QUOTA);
			j3_4.setToolTipText("Provides an easy way of viewing how much reference has been " + "sequenced with a coverage higher than a selected level");
			leftPanel.add(j3_4);

			JLabel j3_5 = fillLabelSubMenuGraphic("Mapping Quality Across Ref.", j3_4, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_ACROSS_REFERENCE);
			j3_5.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j3_5.setToolTipText("Mapping Quality Across Reference");
			leftPanel.add(j3_5);

			JLabel j3_6 = fillLabelSubMenuGraphic("Mapping Quality Histogram", j3_5, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_MAPPING_QUALITY_HISTOGRAM);
			j3_6.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			j3_6.setToolTipText("Frequency histogram of the mapping quality");
			leftPanel.add(j3_6);

			if(tabProperties.isPairedData()){
				JLabel j3_7 = fillLabelSubMenuGraphic("Insert Size Histogram", j3_6, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_HISTOGRAM);
				j3_7.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j3_7.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j3_7);
				
				JLabel j3_8 = fillLabelSubMenuGraphic("Insert Size Across Reference", j3_7, marginSubMenu, Constants.GRAPHIC_NAME_GENOME_OUTSIDE_INSERT_SIZE_ACROSS_REFERENCE);
				j3_8.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
				j3_8.setToolTipText("Frequency histogram of the insert size");
				leftPanel.add(j3_8);
			}

            // JLabel j3_7 =
			// fillLabelSubMenuGraphic("Nucleotide Rel. Content", j3_6,
			// marginSubMenu,
			// Constants.GRAPHIC_NAME_GENOME_OUTSIDE_NUCLEOTIDE_RELATIVE_CONTENT,
			// false);
			// j3_7.setIcon(new
			// ImageIcon(getClass().getResource(Constants.pathImages +
			// "bullet_purple.png")));
			// j3_7.setToolTipText("Nucleotide Relative Content");
			// leftPanel.add(j3_7);
			//
			// JLabel j3_8 =
			// fillLabelSubMenuGraphic("GC/AT Relative Content", j3_7,
			// marginSubMenu,
			// Constants.GRAPHIC_NAME_GENOME_OUTSIDE_GC_AT_RELATIVE_CONTENT,
			// false);
			// j3_8.setIcon(new
			// ImageIcon(getClass().getResource(Constants.pathImages +
			// "bullet_purple.png")));
			// leftPanel.add(j3_8);
            lastComponentVisible = leftPanel.getComponent(leftPanel.getComponentCount() - 1);

        }

		// Set the last component showed at the left split
		tabProperties.setLeftSplitLastElement((JComponent) lastComponentVisible);
    }

	/**
	 * Function that load the left panel with the statistics links for the
	 * RNA-sqe
	 */
	private void fillLeftRnaSplit() {
		TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());
		boolean infoFileSelected = tabProperties.getRnaAnalysisVO().getInfoFileIsSet();
		boolean speciesFileSelected = tabProperties.getRnaAnalysisVO().getSpecieFileIsSet();
		tabProperties.setLastLinkSelected(null);

		JCheckBox checkFirstSection = new JCheckBox("Results");
		checkFirstSection.setSelected(true);
		checkFirstSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
		checkFirstSection.setSelectedIcon(treeMinusIcon);
		checkFirstSection.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		checkFirstSection.setSize(checkFirstSection.getPreferredSize());
		checkFirstSection.setLocation(0, 3);
		checkFirstSection.setAction(getActionShowSubMenu("Results"));
		ReferencePosition referencePosition = new ReferencePosition(checkFirstSection);
		leftPanel.add(checkFirstSection);

		Integer marginSubMenu = referencePosition.getX() + Constants.marginLeftForElementSubMenu;

		JLabel j1_0 = fillLabelSubMenuGraphic("Global Saturation", referencePosition, marginSubMenu, Constants.GRAPHIC_NAME_RNA_GLOBAL_SATURATION);
		j1_0.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_yellow.png")));
		j1_0.setToolTipText("Basic information and statistics for the alignment sequencing input");
		referencePosition.setComponent(j1_0);
		leftPanel.add(j1_0);
		summaryLable = j1_0;

		if (infoFileSelected || speciesFileSelected) {
			
			JLabel j1_1 = fillLabelSubMenuGraphic("Detection per group", referencePosition, marginSubMenu, Constants.GRAPHIC_NAME_RNA_SATURATION_PER_CLASS, true);
			referencePosition.setComponent(j1_1);
			leftPanel.add(j1_1);
			
			JLabel j1_2 = fillLabelSubMenuGraphic("Counts per group", referencePosition, marginSubMenu, Constants.GRAPHIC_NAME_RNA_COUNTS_PER_CLASS, true);
			j1_2.setToolTipText("Frequency histogram of the coverage");
			referencePosition.setComponent(j1_2);
			leftPanel.add(j1_2);

			
			JCheckBox checkSaturationSection = new JCheckBox("Saturation per group");
			checkSaturationSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
			checkSaturationSection.setSelectedIcon(treeMinusIcon);
			checkSaturationSection.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			checkSaturationSection.setSize(checkSaturationSection.getPreferredSize());
			checkSaturationSection.setLocation(0, referencePosition.getY() + referencePosition.getHeight() + Constants.marginTopForElementSubMenu);
			checkSaturationSection.setAction(getActionShowSubMenu("Saturation per group"));
			checkSaturationSection.setSelected(true);
			checkSaturationSection.setVisible(true);
			referencePosition.setComponent(checkSaturationSection);
			leftPanel.add(checkSaturationSection);


			JLabel j = null;
			Map<String, Object> mapGenotypes = tabProperties.getRnaAnalysisVO().getMapClassesInfoFile();
			// Hack to add unknown
			mapGenotypes.put("unknown", "unknown.png");
			Iterator it = mapGenotypes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				j = fillLabelSubMenuGraphic(entry.getKey(), referencePosition, marginSubMenu, entry.getValue().toString(), true);
				referencePosition.setComponent(j);
				leftPanel.add(j);
			}

			JCheckBox checkCountsSection = new JCheckBox("Counts & Sequencing depth");
			checkCountsSection.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "add.png")));
			checkCountsSection.setSelectedIcon(treeMinusIcon);
			checkCountsSection.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			checkCountsSection.setSize(checkCountsSection.getPreferredSize());
			checkCountsSection.setLocation(0, referencePosition.getY() + referencePosition.getHeight() + Constants.marginTopForElementSubMenu);
			checkCountsSection.setAction(getActionShowSubMenu("Counts per group"));
			checkCountsSection.setVisible(true);
			checkCountsSection.setSelected(true);
			referencePosition.setComponent(checkCountsSection);
			leftPanel.add(checkCountsSection);

			it = mapGenotypes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				j = fillLabelSubMenuGraphic(entry.getKey().toString(), referencePosition, marginSubMenu, entry.getKey().toString() + "_boxplot.png", true);
				referencePosition.setComponent(j);
				leftPanel.add(j);
			}
		}

		// Set the last component showed at the left split
		lastComponentVisible = leftPanel.getComponent(leftPanel.getComponentCount() - 1);
        tabProperties.setLeftSplitLastElement((JComponent) lastComponentVisible);
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

        lastComponentVisible = leftPanel.getComponent(0);
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

            if (i > 0) {
                elem.setLocation(elem.getLocation().x, lastComponentVisible.getY() + lastComponentVisible.getHeight() + Constants.marginTopForElementSubMenu);
                resizeLeftPanel(elem);
            }

            if (elem.isVisible()) {
                lastComponentVisible = elem;
            }

            i++;
        }
	}
	
	public void resizeLeftPanel() {
		resizeLeftPanel(lastComponentVisible);
	}
	
	
    /**
     * Function to resize the left panel if the content is bigger than the
     * reserved space adding scroll bars.
     * 
     * @param elem
     *            Component that contains the element with the size and position
     *            of the last element to draw
     */
    private void resizeLeftPanel(Component elem) {
            JViewport leftPanel = leftScrollPane.getViewport();
            Component subElementIzquierda = leftPanel.getComponent(0);
            TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());

            if (subElementIzquierda instanceof JPanel) {
                    tabProperties.setLeftSplitLastElement((JComponent) lastComponentVisible);
                    if (lastComponentVisible.getX() + lastComponentVisible.getWidth() <= leftScrollPane.getWidth() && lastComponentVisible.getY() + lastComponentVisible.getHeight() <= leftScrollPane.getHeight()) {
                            this.leftPanel.setSize(this.leftPanel.getWidth(), lastComponentVisible.getY() + lastComponentVisible.getHeight());
                            this.leftPanel.setPreferredSize(this.leftPanel.getSize());
                    } else if (lastComponentVisible.getY() + lastComponentVisible.getHeight() > leftScrollPane.getWidth()) {
                            this.leftPanel.setSize(this.leftPanel.getWidth(), elem.getY() + elem.getHeight());
                            this.leftPanel.setPreferredSize(this.leftPanel.getSize());
                    }
            }
            leftPanel.validate();
    }

	private JLabel fillLabelSubMenuGraphic(String labelName, Component locationRef, Integer marginSubMenu, String graphicName) {
		return fillLabelSubMenuGraphic(labelName, locationRef, marginSubMenu, graphicName, true);
	}

	private JLabel fillLabelSubMenuGraphic(String labelName, Component locationRef, Integer marginSubMenu, final String graphicName, boolean visible) {
		final JLabel label = fillLabelLink(labelName, locationRef, marginSubMenu, visible);
		label.addMouseListener(new JLabelMouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				showLeftSideInformation(graphicName, label);
			}
		});
		if (!visible) {
			label.setVisible(false);
		}
		return label;
	}

	private JLabel fillLabelSubMenuText(String labelName, Component locationRef, Integer marginSubMenu, String textFileName, int typeReport) {
		return fillLabelSubMenuText(labelName, locationRef, marginSubMenu, textFileName, true, typeReport);
	}

	private JLabel fillLabelSubMenuText(String labelName, Component locationRef, Integer marginSubMenu, final String textFileName, boolean visible, final int typeReport) {
		final JLabel label = fillLabelLink(labelName, locationRef, marginSubMenu, visible);
		label.addMouseListener(new JLabelMouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				showLeftSideSummaryInformation(typeReport, label);
			}
		});
		return label;
	}

	public void showLeftSideSummaryInformation(int typeReport, JLabel label) {
		homeFrame.setTypeAnalysis(typeReport);
        prepareHtmlSummary(getReport());
		fillColorLink(label);
	}
	
	public void showSummary() {
		if(this.summaryLable!=null){
			prepareHtmlSummary(getReport());
			fillColorLink(summaryLable);
		}
	}
	
	public void showImage(String graphic) {
		if(this.summaryLable!=null){
			openGraphic(graphic, getReporterNew(graphic));
			fillColorLink(summaryLable);
		}
	}

	private void showLeftSideInformation(String graphicName, JLabel label) {
		if (graphicName != null) {
			openGraphic(graphicName, getReporterNew(graphicName));
		}
		fillColorLink(label);
	}
	
	private BamQCRegionReporter getReport(){
		// TODO: WTF? Another shitty method? What is the difference between this and new?
        BamQCRegionReporter reporter;
		// Select the reporter that contains the data
		if (homeFrame.getTypeAnalysis() == Constants.REPORT_INPUT_BAM_FILE) {
			reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getReporter();
		} else if (homeFrame.getTypeAnalysis() == Constants.REPORT_INSIDE_BAM_FILE) {
			reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getInsideReporter();
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
			if (graphicName.startsWith("inside")) {
				reporter = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex()).getInsideReporter();
			} else if (graphicName.startsWith("outside")) {
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
	 * Fill the commons properties for the graphic and text links
	 * 
	 * @param labelName
	 *            Name displayed of the label
	 * @param locationRef
	 *            position in the screen to refer
	 * @param marginSubMenu
	 *            margin respect the element before
	 * @param visible visibility of the label
	 * @return JLabel label set
	 */
	private JLabel fillLabelLink(String labelName, Component locationRef, Integer marginSubMenu, boolean visible) {
		JLabel label = new JLabel(labelName);
		label.setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), 11));
		label.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_green.png")));
		label.setSize(label.getPreferredSize());
		label.setOpaque(true);
		label.setLocation(marginSubMenu, locationRef.getY() + locationRef.getHeight() + Constants.marginTopForElementSubMenu);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.setIconTextGap(0);
		label.setToolTipText(labelName);

		if (!visible) {
			label.setVisible(false);
		}
		return label;
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
		Object imageToDisplay = reporter.getMapCharts().get(name);

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
			panelImage.resizeImage(rightScrollPane.getWidth(), rightScrollPane.getHeight());

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
                    htmlTable.append("<th align='left'>Mean coverage</th>");
                    htmlTable.append("<th align='left'>Std coverage</th>");
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
}
