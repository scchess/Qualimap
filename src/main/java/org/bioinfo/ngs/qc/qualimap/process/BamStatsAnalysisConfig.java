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
package org.bioinfo.ngs.qc.qualimap.process;

import org.bioinfo.ngs.qc.qualimap.common.Constants;

/**
 * Created by kokonech
 * Date: 8/20/14
 * Time: 10:33 AM
 */

public class BamStatsAnalysisConfig {

    public boolean drawChromosomeLimits;
    public String gffFile;
    public int numberOfWindows, bunchSize, minHomopolymerSize;
    public BamStatsAnalysisConfig() {
        this.drawChromosomeLimits = false;
        this.numberOfWindows = Constants.DEFAULT_NUMBER_OF_WINDOWS;
        this.bunchSize = Constants.DEFAULT_CHUNK_SIZE;
        this.minHomopolymerSize = Constants.DEFAULT_HOMOPOLYMER_SIZE;

    }

    public  boolean regionsAvailable() {
        return gffFile != null;
    }

}
