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

