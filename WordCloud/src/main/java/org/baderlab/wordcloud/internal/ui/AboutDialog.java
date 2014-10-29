package org.baderlab.wordcloud.internal.ui;

/*
 File: AboutPanel.java

 Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)

 Code written by: Layla Oesper
 Authors: Layla Oesper, Ruth Isserlin, Daniele Merico

 This library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.baderlab.wordcloud.internal.CyActivator;
import org.cytoscape.util.swing.OpenBrowser;

/**
 * This class handles about WordCloud popup from the WordCloud menu.
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog {

	private static final String BUILD_PROPS_FILE = "buildinfo.props";
	private static final String BUILD_VERSION = "build.version";
	private static final String BUILD_TIMESTAMP = "build.timestamp";
	
	
	private static final String URL = "http://www.baderlab.org/Software/WordCloud";

	
	private final OpenBrowser openBrowser;

	public AboutDialog(Window parent, OpenBrowser openBrowser) {
		super(parent, "About WordCloud", ModalityType.MODELESS);
		setResizable(false);

		this.openBrowser = openBrowser;

		
		Properties buildProps = getBuildProperties();
		String version = buildProps.getProperty(BUILD_VERSION);
		String timestamp = buildProps.getProperty(BUILD_TIMESTAMP);
		
		// main panel for dialog box
		JEditorPane editorPane = new JEditorPane();
		editorPane.setMargin(new Insets(10, 10, 10, 10));
		editorPane.setEditable(false);
		editorPane.setEditorKit(new HTMLEditorKit());
		editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));

		editorPane
				.setText("<html><body>"
						+ "<table border='0'><tr>"
						+ "<td width='125'></td>"
						+ "<td width='200'>"
						+ "<p align=center><b>WordCloud</b><BR>A Cytoscape App<BR><BR></p>"
						+ "<p align=center>Version: " + version + "<br>Build: " + timestamp + "</p>"
						+ "</td>"
						+ "<td width='125'><div align='right'></td>"
						+ "</tr></table>"
						+ "<p align=center>WordCloud is a Cytoscape App that generates a word tag cloud<BR>"
						+ "from a user-define node selection, summarizing attributes of choice.<BR>"
						+ "<BR>"
						+ "by Layla Oesper, Daniele Merico, Ruth Isserlin, Mike Kucera and Gary Bader<BR>"
						+ "(<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>"
						+ "<BR>" + "App Homepage:<BR>" + "<a href='"
						+ URL + "'>" + URL + "</a><BR>" + "<BR>"
						+ "<font size='-1'>" + "</font>" + "</p></body></html>");
		
		setContentPane(editorPane);
	}
	
	
	private Properties getBuildProperties() {
		InputStream in = CyActivator.class.getResourceAsStream(BUILD_PROPS_FILE);
		if(in == null)
			return new Properties();
		try {
			Properties buildProps = new Properties();
			buildProps.load(in);
			return buildProps;
		} catch(IOException e) {
			return new Properties();
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
	}

	private class HyperlinkAction implements HyperlinkListener {
		@SuppressWarnings("unused")
		JEditorPane pane;

		public HyperlinkAction(JEditorPane pane) {
			this.pane = pane;
		}

		public void hyperlinkUpdate(HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				openBrowser.openURL(event.getURL().toString());
			}
		}
	}
}
