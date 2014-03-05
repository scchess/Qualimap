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
package org.bioinfo.ngs.qc.qualimap.gui.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Class to allow formatted text in the JTextField
 * @author Luis Miguel Cruz
 */
public class JTextFieldLimit extends PlainDocument {
	private static final long serialVersionUID = -6394794410341873262L;
	private int limit;
	private boolean toUppercase = false;
	private boolean onlyNumbers = false;
  
	public JTextFieldLimit(int limit, boolean onlyNumbers) {
		super();
		this.limit = limit;
		this.onlyNumbers = onlyNumbers;
	}
   
	public JTextFieldLimit(int limit, boolean upper, boolean onlyNumbers) {
		super();
		this.limit = limit;
		this.toUppercase = upper;
		this.onlyNumbers = onlyNumbers;
	}
 
	public void insertString(int offset, String  str, AttributeSet attr)
			throws BadLocationException {
		if (str == null) 
			return;
		
		if(onlyNumbers){
			try{
				Integer.parseInt(str);	
			} catch(NumberFormatException nfe){
				return;
			}
		}

		if ((getLength() + str.length()) <= limit) {
			if (toUppercase){
				str = str.toUpperCase();
			}
			super.insertString(offset, str, attr);
		}
   }
}

