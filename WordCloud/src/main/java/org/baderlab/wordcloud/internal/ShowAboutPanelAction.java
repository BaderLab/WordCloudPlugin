/*
 File: ShowAboutPanelAction.java

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

package org.baderlab.wordcloud.internal;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;

public class ShowAboutPanelAction extends AbstractCyAction 
{
	private CySwingApplication application;
	private OpenBrowser openBrowser;

	public ShowAboutPanelAction(CySwingApplication application, OpenBrowser openBrowser)
	{
		super("About");
		this.application = application;
		this.openBrowser = openBrowser;
	}
	
	public void actionPerformed(ActionEvent event)
	{
		AboutPanel aboutPanel = new AboutPanel(application.getJFrame(), openBrowser);
		aboutPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutPanel.pack();
		aboutPanel.setLocationRelativeTo(application.getJFrame());
		aboutPanel.setVisible(true);
	}
}
