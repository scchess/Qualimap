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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import org.bioinfo.ngs.qc.qualimap.beans.AnalysisResultManager;
import org.bioinfo.ngs.qc.qualimap.beans.StatsReporter;
import org.bioinfo.ngs.qc.qualimap.common.AnalysisType;

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
