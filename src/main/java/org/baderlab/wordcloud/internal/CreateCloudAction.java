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

package org.baderlab.wordcloud.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * This is the action associated with creating a new Semantic Summary Tag Cloud
 * anywhere in the Semantic Summary Plugin.  This includes from the Plugin menu,
 * right click on a node, and from the Semantic Summary Input Panel.
 * @author Layla Oesper
 * @version 1.0
 */

public class CreateCloudAction extends AbstractSemanticSummaryAction
{
	//VARIABLES
	private static final long serialVersionUID = 1103296239269358444L;
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private String nameColumnName;
	
	//CONSTRUCTORS
	

	/**
	 * CreateCloudAction constructor.
	 * @param pluginAction 
	 */
	public CreateCloudAction(CyApplicationManager applicationManager, CySwingApplication application, SemanticSummaryManager cloudManager, SemanticSummaryParametersFactory parametersFactory)
	{
		super("Create Cloud");
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.parametersFactory = parametersFactory;
	}
	
	//METHODS
	
	/**
	 * Method called when a Create Cloud action occurs.
	 * @param nameColumnName 
	 * 
	 * @param ActionEvent - event created when choosing Create Cloud from 
	 * any of its various locations.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		//Initialize the Semantic Summary Panels
		pluginAction.actionPerformed(ae);
		
		CyNetwork network = applicationManager.getCurrentNetwork();
		if (network == null) {
			return;
		}

		//If no nodes are selected
		if (!SelectionUtils.hasSelectedNodes(network))
		{
			JOptionPane.showMessageDialog(application.getJFrame(), 
					"Please select one or more nodes.");
			return;
		}
		
		//Check if network is already in our list
		SemanticSummaryParameters params;
		
		//Get SemanticSummaryParameters or Register if necessary
		if(cloudManager.isSemanticSummary(network))
		{
			params = cloudManager.getParameters(network);
			
			//Update if necessary
			if (params.networkHasChanged(network));
				params.updateParameters(network);
		}
		else
		{
			params = parametersFactory.createSemanticSummaryParameters();
			params.updateParameters(network);
			cloudManager.registerNetwork(network, params);
		}
		
		//Create CloudParameters
		CloudParameters cloudParams = new CloudParameters(params);
		cloudParams.setCloudNum(params.getCloudCount());
		cloudParams.setCloudName(params.getNextCloudName());
		
		Set<CyNode> nodes = SelectionUtils.getSelectedNodes(network);
		
		cloudParams.setSelectedNodes(nodes);
		
		//Add to list of clouds
		params.addCloud(cloudParams.getCloudName(), cloudParams);
		
		// Select all attributes by default
		SemanticSummaryInputPanel inputPanel = cloudManager.getInputWindow();
		inputPanel.setAttributeNames(cloudManager.getColumnNames(network, CyNode.class));
		
		if (nameColumnName != null) {
			ArrayList<String> attributeNames = new ArrayList<String>();
			attributeNames.add(nameColumnName);
			inputPanel.setAttributeNames(attributeNames);
		}
		
		//Retrieve values from input panel
		cloudParams.retrieveInputVals(inputPanel);
		
		cloudParams.updateRatios();
		cloudParams.calculateFontSizes();
		
		CloudDisplayPanel cloudPanel = cloudManager.getCloudWindow();
		cloudPanel.updateCloudDisplay(cloudParams);
		
		//Update list of clouds
		//inputPanel.setNetworkList(params);
		inputPanel.addNewCloud(cloudParams);
		inputPanel.getCreateNetworkButton().setEnabled(true);
		inputPanel.getSaveCloudButton().setEnabled(true);
		
		//displayPanel.getSaveCloudButton().setEnabled(true);
		
		//Update the list of filter words and checkbox
		inputPanel.refreshNetworkSettings();
		
		//Enable adding of words to exclusion list
		inputPanel.getAddWordTextField().setEditable(true);
		inputPanel.getAddWordButton().setEnabled(true);
	}
}
