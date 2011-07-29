package org.bioinfo.ngs.qc.qualimap.gui.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.bioinfo.commons.log.Logger;
import org.bioinfo.commons.utils.StringUtils;

import com.ibm.icu.text.NumberFormat.SimpleNumberFormatFactory;


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
		Long result = new Long(0);
		
		if(s != null){
			try {
				result = (Long)format.parseObject(s);
			} catch (ParseException e) {
				logger.error("Cannot parse the String " + s + " from the properites file to a Long value");
			}
		}
		
		return result;
	}
	
	public Double parseDouble(String s){
		// To parse the double we have to put the English format.
		NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
		Double result = new Double(0.0);
		
		if(s != null){
			try {
				Number obj = format.parse(s);
				/*if(obj instanceof Long){
					result = ((Long) obj).doubleValue();
				} else if (obj instanceof Double){
					result = (Double) obj;
				}*/
				result = obj.doubleValue();
			} catch (ParseException e) {
				logger.error("Cannot parse the String " + s + " from the properites file to a Double value");
			}	
		}
		
		return result;
	}
	
	public Integer parseInt(String s){
		NumberFormat format = new DecimalFormat("###,###,###,###,###,###,###.##");
		Integer result = new Integer(0);
		
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
