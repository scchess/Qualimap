/**
 * QualiMap: evaluation of next generation sequencing alignment data
 * Copyright (C) 2015 Garcia-Alcalde et al.
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
package org.bioinfo.ngs.qc.qualimap.test;

import org.bioinfo.ngs.qc.qualimap.beans.GenericHistogram;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by kokonech
 * Date: 10/5/12
 * Time: 11:33 AM
 */
public class HistogramTest {



 @Test
 public void testHistogram(){

     GenericHistogram hist = new GenericHistogram(2,true);

          int[] d1 = {1,1,1,1,1};
          int[] d2 = {1,1,1,1};

          hist.updateHistogram(d1);
          hist.updateHistogram(d2);

     double[] histData = hist.getHist();

     assertTrue(histData[0] == 1.1);
     assertTrue(histData[1] == 0.9);




 }


}