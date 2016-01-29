/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2016 Garcia-Alcalde et al.
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
import org.bioinfo.ngs.qc.qualimap.beans.ChartRawDataWriter;
import org.bioinfo.ngs.qc.qualimap.beans.QChart;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;
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
    private TabPageController tabPageController;

    static class RightPanelListener extends MouseAdapter {

        TabPageController tabPageController;
        JComponent parent;

        public RightPanelListener(JComponent parent, TabPageController tabProperties) {
            this.parent = parent;
            this.tabPageController = tabProperties;
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
                final String graphicName = tabPageController.getLoadedGraphicName();
                StatsReporter reporter = tabPageController.getActiveReporter();
                if (!graphicName.isEmpty())  {
                    JMenuItem savePictureItem = createSaveGraphicMenuItem(reporter, graphicName);
                    popup.add(savePictureItem);

                    QChart chart = tabPageController.getActiveReporter().findChartByName(graphicName);
                    if (chart != null && chart.canExportRawData()) {
                        JMenuItem exportDataMenuItem = createExportRawDataMenuItem(chart, parent);
                        popup.addSeparator();
                        popup.add(exportDataMenuItem);
                    }

                    // TODO: Each reporter should add own items to the popup menu.
                    if (tabPageController.getTypeAnalysis() == AnalysisType.CLUSTERING) {
                        JMenuItem exportGeneListItem = new JMenuItem("Export feature list...");
                        exportGeneListItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                HomeFrame.showExportGenesDialog(parent, tabPageController);
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

        private JMenuItem createSaveGraphicMenuItem(final StatsReporter reporter, final String graphicName) {
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
                        BufferedImage image = reporter.findChartByName(graphicName).getBufferedImage();
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

	public OpenLoadedStatistics(HomeFrame homeFrame, TabPageController tabProperties) {
		super();
		this.homeFrame = homeFrame;
        this.tabPageController = tabProperties;
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

		tabPageController.getGraphicImage().addComponentListener(this);

        /*
        Done by constructor
        tabPageController.setLastLinkSelected(null);
        tabPageController.setLoadedGraphicName("");*/


        prepareLeftPanel();

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GroupLayout(rightPanel));
		rightScrollPane.setViewportView(rightPanel);
        rightScrollPane.addMouseListener(new RightPanelListener(rightPanel,tabPageController));

		statisticsContainer.setLeftComponent(leftScrollPane);
		statisticsContainer.setRightComponent(rightScrollPane);
		statisticsContainer.validate();
		leftScrollPane.validate();

        validate();

        return statisticsContainer;
    }

    private void prepareLeftPanel() {

        List<StatsReporter> reporters = tabPageController.getReporters();


        int idx = 0;
        for (StatsReporter reporter : reporters) {
            String sectionName = reporter.getName() + reporter.getNamePostfix();
            JCheckBox checkFirstSection = createResultsCheckBox(sectionName);
            leftPanel.add(checkFirstSection);

            if (reporter.hasInputDescription()) {
                JLabel inputLabel = createInputDescriptionLinkLabel("Input data & parameters", idx);
                inputLabel.setToolTipText("Input data description");
                inputLabel.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_blue.png")));
                leftPanel.add(inputLabel);
            }

            if (reporter.hasSummary() ) {
                JLabel summaryLabel = createSummaryLinkLabel("Summary", idx);
                summaryLabel.setIcon(new ImageIcon(getClass().getResource(Constants.pathImages + "bullet_green.png")));
	            leftPanel.add(summaryLabel);
                if (initialLabel == null) {
                    initialLabel = summaryLabel;
                }
            }

            List<QChart> charts = reporter.getCharts();

            for (QChart chart : charts) {
                JLabel linkLabel = createImageLinkLabel(chart.getTitle(), chart.getName(), idx);
                linkLabel.setToolTipText(chart.getToolTip());
                leftPanel.add(linkLabel);
            }
            ++idx;
        }
    }


    public void showInitialPage()
    {
        if(AnalysisType.BAM_QC ==  tabPageController.getTypeAnalysis() && initialLabel != null){
			showLeftSideSummaryInformation(0, initialLabel);
		} else {

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0, 50));
            centerPanel.add(new JLabel("To start browsing statistics please select an item" +
                    " from the list to the left."));
            rightScrollPane.setViewportView( centerPanel);
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


    JLabel createImageLinkLabel(final String labelText, final String graphicName, final int reporterIndex) {
        final JLabel label = createLinkLabel(labelText);
        label.addMouseListener(new JLabelMouseListener() {
            public void mouseClicked(MouseEvent arg0) {
				showLeftSideInformation(graphicName, label, reporterIndex);
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
		tabPageController.setLoadedGraphicName("");
        prepareHtmlSummary(getReporter(reporterIndex));
        tabPageController.setSelectedReporterIndex(reporterIndex);
		fillColorLink(label);
	}

    public void showLeftSideInputDescription(int reporterIndex, JLabel label) {
        tabPageController.setLoadedGraphicName("");
        prepareHtmlInputDescription(getReporter(reporterIndex));
        tabPageController.setSelectedReporterIndex(reporterIndex);
        fillColorLink(label);
    }

	private void showLeftSideInformation(String graphicName, JLabel label, int reporterIndex) {
		if (graphicName != null) {
			showGraphic(graphicName, getReporter(reporterIndex));
            tabPageController.setSelectedReporterIndex(reporterIndex);
		}
		fillColorLink(label);
	}
	
	private StatsReporter getReporter(int reporterIndex){
		return tabPageController.getReporters().get(reporterIndex);
	}

	/**
	 * Fill the color of the link pressed by the user in the left menu
	 * 
	 * @param label
	 *            label to set the colors
	 */
	private void fillColorLink(JLabel label) {
		//TabPropertiesVO tabProperties = homeFrame.getListTabsProperties().get(homeFrame.getTabbedPane().getSelectedIndex());

		if (tabPageController.getLastLinkSelected() != null) {
			tabPageController.getLastLinkSelected().setBackground(null);
			tabPageController.getLastLinkSelected().setFont(new Font(label.getFont().getFontName(), label.getFont().getStyle(), label.getFont().getSize()));
		}
		if (label != null) {
			label.setBackground(new Color(240, 230, 140));
			label.setFont(HomeFrame.defaultFontItalic);
            label.setSize(label.getPreferredSize());
            label.validate();
			tabPageController.setLastLinkSelected(label);
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
	private void showGraphic(String name, StatsReporter reporter) {



		QChart chart = reporter.findChartByName(name);
        if (chart == null) {
            logger.error("Can not find chart " + name);
            return;
        }

	    if (chart.isBufferedImage()) {

            // Get a Singleton to manage the image to display
            GraphicImagePanel panelImage = tabPageController.getGraphicImage();
            BufferedImage imageToDisplay = chart.getBufferedImage();
  			// Set the image with the file image get
			panelImage.setImage(imageToDisplay);
            // Scale the image
            if (tabPageController.getTypeAnalysis() == AnalysisType.CLUSTERING ) {
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

        tabPageController.setLoadedGraphicName(name);




        homeFrame.updateMenuBar();

    }

    private static JMenuItem createExportRawDataMenuItem(QChart chart, JComponent parent) {
        ChartRawDataWriter dataWriter = chart.getDataWriter();
        JMenuItem exportDataItem = new JMenuItem("Export plot data");
        exportDataItem.addActionListener( new ExportChartDataActionListener(parent, dataWriter, chart.getTitle()));
        return exportDataItem;
    }


	/**
	 * Show into the split of the selected tab the text values selected.
	 * 
	 * @param reporter
	 *            BamQCRegionReporter data input values
	 */
	private void prepareHtmlSummary(StatsReporter reporter) {
		HtmlJPanel panelDerecha = new HtmlJPanel();
		panelDerecha.setSize(rightScrollPane.getWidth(), rightScrollPane.getHeight());
		panelDerecha.setFont(HomeFrame.defaultFont);
        int width = rightScrollPane.getWidth() - 100;

        StringBuilder summaryHtml = new StringBuilder();

        summaryHtml.append( HtmlJPanel.getHeader() );
        summaryHtml.append(reporter.getSummary(width));
        summaryHtml.append( HtmlJPanel.getHeadFooter() );

        panelDerecha.setHtmlPage(summaryHtml.toString());
		rightScrollPane.setViewportView(panelDerecha);
	}

    private void prepareHtmlInputDescription(StatsReporter reporter) {
        int width = rightScrollPane.getWidth() - 100;
        String inputDescText = reporter.getInputDescription(width);

        if (inputDescText != null) {
            HtmlJPanel htmlPanel = new HtmlJPanel();
            htmlPanel.setSize(rightScrollPane.getWidth(), rightScrollPane.getHeight());
            htmlPanel.setFont(HomeFrame.defaultFont);

            StringBuilder inputDesc = new StringBuilder();
            inputDesc.append( HtmlJPanel.getHeader() );
            inputDesc.append( inputDescText );
            inputDesc.append( HtmlJPanel.getHeadFooter() );
            htmlPanel.setHtmlPage(inputDesc.toString());

            rightScrollPane.setViewportView(htmlPanel);
        }
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
        GraphicImagePanel imagePanel = tabPageController.getGraphicImage();
        if (c == imagePanel && tabPageController.getTypeAnalysis() != AnalysisType.CLUSTERING) {
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
