/*
 File: CloudListSelectionHandler.java

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

package org.baderlab.wordcloud.internal.ui.input;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.wordcloud.internal.ui.UIManager;

/**
 * This class handles the action associated with selecting a cloud from the
 * list of clouds in the input panel.  It displays the cloud, updates the 
 * input panel and highlights the correct nodes in the view.
 */

public class CloudListSelectionHandler implements ListSelectionListener  {
	private UIManager uiManager;

	public CloudListSelectionHandler(UIManager uiManager) {
		this.uiManager = uiManager;
	}
	
	public void valueChanged(ListSelectionEvent e) {
		//Only care when value is no longer changing
		if (e.getValueIsAdjusting())
			return;
				
		JList list = (JList) e.getSource();
		String cloudName = (String) list.getSelectedValue();
		
		uiManager.setCurrentCloud(uiManager.getCurrentNetwork(), cloudName);
	}
}
