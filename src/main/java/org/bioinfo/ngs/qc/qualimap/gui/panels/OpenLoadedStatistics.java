/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2012 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.ngs.qc.qualimap.beans.BamQCRegionReporter;
import org.bioinfo.ngs.qc.qualimap.beans.ChartRawDataWriter;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.common.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.utils.*;

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
                    JMenuItem savePictureItem = createSaveGraphicMenuItem(graphicName);
                    popup.add(savePictureItem);

                    QChart chart = tabProperties.getReporter().findChartByName(graphicName);
                    if (chart != null && chart.canExportRawData()) {
                        JMenuItem exportDataMenuItem = createExportRawDataMenuItem(chart, parent);
                        popup.addSeparator();
                        popup.add(exportDataMenuItem);
                    }

                    // TODO: Each reporter should add own items to the popup menu.
                    if (tabProperties.getTypeAnalysis() == AnalysisType.CLUSTERING) {
                        JMenuItem exportGeneListItem = new JMenuItem("Export feature list...");
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

        private JMenuItem createSaveGraphicMenuItem(final String graphicName) {
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
                        BufferedImage image =
                                tabProperties.getReporter().findChartByName(graphicName).getBufferedImage();
                        try {
                            ImageIO.write(image, "png", new File(path));
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(parent, "Failed to save image!",
                                    "Save image", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                }
            });
            return savePictureItem;
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

		if (tabProperties.getTypeAnalysis() == AnalysisType.COUNTS_QC ) {
			fillLeftRnaSplit();
		} else if (tabProperties.getTypeAnalysis() == AnalysisType.CLUSTERING) {
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

        List<QChart> charts = tabProperties.getReporter().getCharts();

        for (QChart chart : charts ) {
            JLabel j = createImageLinkLabel(chart.getTitle(), chart.getName() );
            leftPanel.add(j);
        }

    }

    /**
	 * Function that load the left panel with the statistics links
	 */
	private void fillLeftSplit() {

		boolean isGffSelected = tabProperties.isGffSelected();
		boolean showOutsideStats = tabProperties.getOutsideStatsAvailable();

        BamQCRegionReporter reporter = tabProperties.getReporter();

        String sectionName = isGffSelected ? "Results inside of regions" : "Results";
        JCheckBox checkFirstSection = createResultsCheckBox(sectionName);
        leftPanel.add(checkFirstSection);

        JLabel j1_0 = createSummaryLinkLabel("Summary", Constants.REPORT_INPUT_BAM_FILE);

        //TODO: make red color of button if warning?
        j1_0.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_green.png")));
	    leftPanel.add(j1_0);
        initialLabel = j1_0;

        JLabel j1_0_1 = createInputDescriptionLinkLabel("Input", Constants.REPORT_INPUT_BAM_FILE);
		j1_0_1.setToolTipText("Input data description");
        j1_0_1.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
        leftPanel.add(j1_0_1);

        List<QChart> charts = reporter.getCharts();


        for (QChart chart : charts) {
            JLabel linkLabel = createImageLinkLabel(chart.getTitle(), chart.getName());
            linkLabel.setToolTipText(chart.getToolTip());
            leftPanel.add(linkLabel);
        }

        if (showOutsideStats) {

			JCheckBox checkSecondSection = createResultsCheckBox("Results outside of regions");
			leftPanel.add(checkSecondSection);

			JLabel summary = createSummaryLinkLabel("Summary", Constants.REPORT_OUTSIDE_BAM_FILE);
			summary.setToolTipText("Basic information and statistics for the alignment sequencing input");
            summary.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			leftPanel.add(summary);

            JLabel inputDesc = createInputDescriptionLinkLabel("Input", Constants.REPORT_OUTSIDE_BAM_FILE);
			inputDesc.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
			inputDesc.setToolTipText("Input data description");
            leftPanel.add(inputDesc);

            List<QChart> outsideCharts = tabProperties.getOutsideReporter().getCharts();


            for (QChart chart : outsideCharts) {
                JLabel linkLabel = createImageLinkLabel(chart.getTitle(), chart.getName());
                linkLabel.setToolTipText(chart.getToolTip());
                leftPanel.add(linkLabel);
            }


		}

	}

	/**
	 * Function that load the left panel with the statistics links for the
	 * RNA-seq
	 */
	private void fillLeftRnaSplit() {

        JCheckBox checkFirstSection = createResultsCheckBox("Results");
		leftPanel.add(checkFirstSection);

		JLabel inputDesc = createInputDescriptionLinkLabel("Input", 0 );
        inputDesc.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
        leftPanel.add(inputDesc);

        List<QChart> charts = tabProperties.getReporter().getCharts();

        for (QChart chart : charts ) {
            JLabel j = createImageLinkLabel(chart.getTitle(), chart.getName() );
            leftPanel.add(j);
        }

    }

    public void showInitialPage(TabPropertiesVO tabProperties)
    {
        if(AnalysisType.BAM_QC ==  tabProperties.getTypeAnalysis()){
			showLeftSideSummaryInformation(0, initialLabel);
		}else if (AnalysisType.COUNTS_QC ==  tabProperties.getTypeAnalysis() ){
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

		if (tabProperties.getTypeAnalysis() == AnalysisType.BAM_QC ) {

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



		QChart chart = reporter.findChartByName(name);
        if (chart == null) {
            logger.error("Can not find chart " + name);
            return;
        }

	    if (chart.isBufferedImage()) {

            // Get a Singleton to manage the image to display
            GraphicImagePanel panelImage = tabProperties.getGraphicImage();
            BufferedImage imageToDisplay = chart.getBufferedImage();
  			// Set the image with the file image get
			panelImage.setImage(imageToDisplay);
            // Scale the image
            if (tabProperties.getTypeAnalysis() == AnalysisType.CLUSTERING ) {
                int width = imageToDisplay.getWidth();
                int height  =  imageToDisplay.getHeight();
                panelImage.setPreferredSize(new Dimension(width, height));
                panelImage.resizeImage(width, height);
            } else {
                panelImage.resizeImage(rightScrollPane.getWidth(), rightScrollPane.getHeight());
            }
            rightScrollPane.setViewportView(panelImage);

        } else {

            JFreeChart jFreeChart = chart.getJFreeChart();
            ChartPanel panelImage = new ChartPanel( jFreeChart );

            if (chart.canExportRawData()) {
                JMenuItem exportDataItem = createExportRawDataMenuItem(chart, this);
                panelImage.getPopupMenu().addSeparator();
                panelImage.getPopupMenu().add( exportDataItem );
            }
            panelImage.setSize(rightScrollPane.getSize());
        	rightScrollPane.setViewportView(panelImage);

        }

        tabProperties.setLoadedGraphicName(name);




        homeFrame.updateMenuBar();

    }

    private static JMenuItem createExportRawDataMenuItem(QChart chart, JComponent parent) {
        ChartRawDataWriter dataWriter = chart.getDataWriter();
        JMenuItem exportDataItem = new JMenuItem("Export plot data");
        exportDataItem.addActionListener( new ExportChartDataActionListener(parent, dataWriter, chart.getTitle()));
        return exportDataItem;
    }


    public static void addSummarySection(StringBuffer buf, StatsKeeper.Section s, int width) {

        buf.append(HtmlJPanel.COLSTART).append("<b>").append(s.getName()).append("</b>");
        buf.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));

        for (String[] row : s.getRows()) {
            buf.append(HtmlJPanel.COLSTARTFIX).append(row[0]).append(HtmlJPanel.COLMID)
                    .append(row[1]).append(HtmlJPanel.COLEND);
        }

        buf.append(HtmlJPanel.getTableFooter());
        buf.append(HtmlJPanel.COLEND);


    }

    public static void addChromosomesSections(StringBuffer summaryHtml,
                                              int width,
                                              List<StatsKeeper.Section> chromosomeSections) {
        summaryHtml.append(HtmlJPanel.COLSTART).append("<b>").append("Chromosome stats").append("</b>");
        summaryHtml.append(HtmlJPanel.getTableHeader(width, "FFFFFF"));

        for (StatsKeeper.Section s : chromosomeSections ) {
            boolean  header = s.getName().equals(Constants.CHROMOSOME_STATS_HEADER);
            List<String[]> rows = s.getRows();
            for (String[] row : rows) {
                summaryHtml.append("<tr>");
                for (String item : row) {
                    if (header) {
                        summaryHtml.append("<td><b>").append(item).append("</b></td>");
                    }   else {
                        summaryHtml.append("<td>").append(item).append("</td>");
                    }
                }
                summaryHtml.append("</tr>");
            }

        }

        summaryHtml.append(HtmlJPanel.getTableFooter());
        summaryHtml.append(HtmlJPanel.COLEND);
    }


    public static StringBuffer prepareHtmlReport(BamQCRegionReporter reporter, int width) {


        StringBuffer summaryHtml = new StringBuffer("");

        List<StatsKeeper.Section> summarySections = reporter.getSummaryDataSections();
        summaryHtml.append("<p align=center> <b>Summary</b></p>").append(HtmlJPanel.BR);
        summaryHtml.append(HtmlJPanel.getTableHeader(width, "EEEEEE"));

        for (StatsKeeper.Section s: summarySections) {
            addSummarySection(summaryHtml, s, width);
        }

        List<StatsKeeper.Section> chromosomeSections = reporter.getChromosomeSections();

        addChromosomesSections(summaryHtml, width, chromosomeSections);

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

        StringBuilder summaryHtml = new StringBuilder();

        summaryHtml.append( HtmlJPanel.getHeader() );
        summaryHtml.append(prepareHtmlReport(reporter, width));
        summaryHtml.append( HtmlJPanel.getHeadFooter() );

        panelDerecha.setHtmlPage(summaryHtml.toString());
		rightScrollPane.setViewportView(panelDerecha);
	}

    private void prepareHtmlInputDescription(BamQCRegionReporter reporter) {
        HtmlJPanel htmlPanel = new HtmlJPanel();
		htmlPanel.setSize(rightScrollPane.getWidth(), rightScrollPane.getHeight());
		htmlPanel.setFont(HomeFrame.defaultFont);
        int width = rightScrollPane.getWidth() - 100;

        StringBuilder inputDesc = new StringBuilder();
        inputDesc.append( HtmlJPanel.getHeader() );
        inputDesc.append( reporter.getInputDescription(width) );
        inputDesc.append( HtmlJPanel.getHeadFooter() );

        htmlPanel.setHtmlPage(inputDesc.toString());

        rightScrollPane.setViewportView(htmlPanel);
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
        GraphicImagePanel imagePanel = tabProperties.getGraphicImage();
        if (c == imagePanel && tabProperties.getTypeAnalysis() != AnalysisType.CLUSTERING) {
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
