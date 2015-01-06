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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;



public class StringUtilsSwing {
	/** Logger to print information */
	protected Logger logger;
	
	public String formatInteger(int decimal){
		return StringUtils.decimalFormat(decimal,"###,###,###,###,###,###,###.##");
	}
	
	public String formatLong(long decimal){
		return StringUtils.decimalFormat(decimal,"###,###,####,###,###,###,###,###,###,###,###.##");
	}
	
	public String formatDecimal(double decimal){
		return StringUtils.decimalFormat(decimal,"###,###,###,###,###,###,###.##");
	}
	
	public String formatPercentage(double numerator, double denominator){
		return formatPercentage((numerator/denominator)*100.0);		
	}
	
	public String formatPercentage(double percentage){
		return StringUtils.decimalFormat(percentage,"###.##")+"%";
	}
	
	public Long parseLong(String s){
		NumberFormat format = new DecimalFormat("###,###,####,###,###,###,###,###,###,###,###.##");
		Long result = 0L;
		
		if(s != null){
			try {
				result = (Long)format.parseObject(s);
			} catch (ParseException e) {
				logger.error("Cannot parse the String " + s + " from the properites file to a Long value");
			}
		}
		
		return result;
	}

	public Integer parseInt(String s){
		NumberFormat format = new DecimalFormat("###,###,###,###,###,###,###.##");
		Integer result = 0;
		
		if(s != null){
			try {
				Long l = (Long)format.parseObject(s);
				if(l != null){
					result = l.intValue();
				}
			} catch (ParseException e) {
				logger.error("Cannot parse the String " + s + " from the properites file to a Integer value");
			}
		}
		
		return result;
	}
	
	/**
	 * Cut the file name if it is too large and add at this case "..." at the end
	 * @param fileName name of the original file
	 * @return new name created if necessary
	 */
	public static String formatFileName(String fileName){
		String result = fileName;
		
		// If the name of the file is too large, we cut it
		if(result.length()>20){
			// If we are only cutting the extension
			if(fileName.length() > 24){
				result = fileName.substring(0, 20) + "...";
			}
		}
		
		return result;
	}
}
