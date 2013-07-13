package org.bioinfo.ngs.qc.qualimap.gui.utils;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;

import javax.swing.*;

/**
 * Created by kokonech
 * Date: 7/13/13
 * Time: 2:23 PM
 */
public class TabPageController extends AnalysisResultManager {


    /**
    * Variable that contains the GraphicImagePanel for each tab where the
    * system load the graphic image at each moment.
    */
    private GraphicImagePanel graphicImage;

    /** Variable that contains name of the graphic image loaded into the
	 * right panel in the screen. */
    private String loadedGraphicName;

	/** Variable to manage the last link selected in the left menu, to remove the
	 * link decoration setted before. */
	private JLabel lastLinkSelected;
    private int selectedReproterIndex;

    public TabPageController(AnalysisType analysisType) {
        super(analysisType);
        this.graphicImage = new GraphicImagePanel();
        loadedGraphicName = "";
        lastLinkSelected = null;
        selectedReproterIndex = 0;
    }

    public void setLastLinkSelected(JLabel linkSelected) {
        lastLinkSelected = linkSelected;
    }

    public GraphicImagePanel getGraphicImage() {
        return graphicImage;
    }

    public void setLoadedGraphicName(String name) {
        loadedGraphicName = name;
    }

    public String getLoadedGraphicName() {
        return loadedGraphicName;
    }

    public JLabel getLastLinkSelected() {
        return lastLinkSelected;
    }

    public int getSelectedReproterIndex() {
        return selectedReproterIndex;
    }

    public void setSelectedReporterIndex(int reporterIndex) {
        this.selectedReproterIndex = reporterIndex;
    }

    public StatsReporter getActiveReporter() {
        return getReporters().get(selectedReproterIndex);
    }
}
