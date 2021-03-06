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
import org.baderlab.wordcloud.internal.model.CloudBuilder;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


@SuppressWarnings("serial")
public class CreateCloudAction extends AbstractCyAction {
	
	private final CyApplicationManager applicationManager;
	private final CySwingApplication application;
	private final CloudModelManager cloudManager;
	private final UIManager uiManager;
	

	public CreateCloudAction(CyApplicationManager applicationManager, CySwingApplication application, CloudModelManager cloudManager, UIManager uiManager) {
		super("Create Cloud");
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.uiManager = uiManager;
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
		
		CloudBuilder builder = cloudManager.addNetwork(network).getCloudBuilder();
		
		CloudParameters currentCloud = uiManager.getCurrentCloud();
		if(currentCloud == null) {
			builder.setAllAttributes();
		}
		else {
			builder.copyFrom(currentCloud); // inherit all the values currently set in the info panel, this works because of live update
		}
		
		builder.setNodes(nodes);
		builder.build(); // fires event that will update the UI
	}
}
