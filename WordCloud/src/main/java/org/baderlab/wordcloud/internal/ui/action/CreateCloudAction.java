/*
 File: CreateCloudAction.java

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

package org.baderlab.wordcloud.internal.ui.action;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JOptionPane;

import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.model.next.CloudModelManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


public class CreateCloudAction extends AbstractCyAction {
	private static final long serialVersionUID = 1103296239269358444L;
	
	private final CyApplicationManager applicationManager;
	private final CySwingApplication application;
	private final CloudModelManager cloudManager;
	

	public CreateCloudAction(CyApplicationManager applicationManager, CySwingApplication application, CloudModelManager cloudManager) {
		super("Create Cloud");
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
	}
	
	
	public void actionPerformed(ActionEvent ae) {
		CyNetwork network = applicationManager.getCurrentNetwork();
		if (network == null) {
			return;
		}

		if (!SelectionUtils.hasSelectedNodes(network)) {
			JOptionPane.showMessageDialog(application.getJFrame(), "Please select one or more nodes.");
			return;
		}
		
		Set<CyNode> nodes = SelectionUtils.getSelectedNodes(network);
		
		cloudManager.addNetwork(network).createCloud(nodes); // fires event that will update the UI
	}
}
