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

//TODO: move pure HTML stuff to another class


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
	    return "<HTML>\n<HEAD>\n <link rel=\"stylesheet\" href=\"css/agogo.css\" type=\"text/css\" /> " +
                "\n<link rel=\"stylesheet\" href=\"css/pygments.css\" type=\"text/css\" />" +

                "\n<TITLE>Qualimap Report</TITLE>\n</HEAD><BODY BGCOLOR=\"#EEEEEE\">";
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
