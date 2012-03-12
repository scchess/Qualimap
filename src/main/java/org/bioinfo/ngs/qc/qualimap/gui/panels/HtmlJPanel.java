/*******************************************************************************
 * Copyright (c) 2009 Stefan Götz.
 * All rights reserved. 
 * 
 * Contributors:
 *     Stefan Götz - initial API and implementation
 * 
 * The software is provided "as is", without any warranty of any kind.
 ******************************************************************************/


/*
 * Blast2GO
 * All Rights Reserved.
 */
package org.bioinfo.ngs.qc.qualimap.gui.panels;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

public class HtmlJPanel extends JPanel implements HyperlinkListener {

	public static final Object BR = "<br/>";
	public static final String COLSTARTFIX = "<tr><td width=300>";
	public static final String COLSTART = "<tr><td>";
	public static final String COLMID = "</td><td align=left>";
	public static final String COLEND = "</td></tr>";
	private JEditorPane aJEditorPane;
	private String html;
	
	public HtmlJPanel() {
		this.html = getHeader()+getHeadFooter();
		this.aJEditorPane = new JEditorPane();
		final HTMLEditorKit eKit = new HTMLEditorKit();
		this.aJEditorPane.setEditorKit(eKit);
		this.aJEditorPane.setText(this.html);
		this.aJEditorPane.setEditable(false);
		this.setLayout(new BorderLayout());
		this.add(this.aJEditorPane);
	}
	
	public void setHtmlPage(final String html) {
		this.html = html;
		this.aJEditorPane.setText(this.html);
		this.revalidate();
		this.repaint();
	}

	@Override
    public void hyperlinkUpdate(HyperlinkEvent arg0) {
    }

	public static String getHeader() {
	    return "<HTML><HEAD><TITLE></TITLE></HEAD><BODY BGCOLOR=\"#EEEEEE\">";
    }

	public static String getHeadFooter() {
	    return "</BODY></HTML>";
    }

	public static Object getTableHeader(int width, String color) {
		return "<table BGCOLOR=\"#"+color+"\" cellspacing=\"-1\" border=\"0\" " +
                "width=\""+width+"\" align=\"center\" valign=\"top\">";
    }
    
	public static Object getTableFooter() {
		return "</table>";
    }
}
