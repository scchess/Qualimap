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

import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.ngs.qc.qualimap.main.NgsSmartMain;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class RnaSeqTest {

    static NumberFormat formatter = DecimalFormat.getInstance(Locale.US);

    @Test
    public void testBamQC(){

    	String []args = {
    			"counts"
    			};
        try {
			//NgsSmartMain.main(args);
            System.out.println("Boo!");

            formatter.setMaximumFractionDigits(2);
            System.out.println(formatter.format(1000000000));
            System.out.println(formatter.format(25.15546756745));



            //formatter.



		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
