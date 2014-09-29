/*
 File: CreateCloudNoDisplayAction.java

 Copyright 2014 - The Cytoscape Consortium (www.cytoscape.org)
 
 Code written by: Arkady Arkhangorodsky
 
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

package org.baderlab.wordcloud.internal.command;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.model.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.model.SemanticSummaryParameters;
import org.baderlab.wordcloud.internal.model.SemanticSummaryParametersFactory;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.ui.AbstractSemanticSummaryAction;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.cloud.CloudWordInfo;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class CreateCloudCommandAction extends AbstractSemanticSummaryAction 
{

	//VARIABLES
	private static final long serialVersionUID = -5065616290485908393L;
	private CyApplicationManager applicationManager;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private List<String> attributeNames;
	private List<CloudWordInfo> wordInfo;
	private String cloudName;
	private CyTable clusterTable;
	private Set<CyNode> nodes;
	
	//CONSTRUCTORS

	/**
	 * CreateCloudAction constructor.
	 * @param pluginAction 
	 */

	public CreateCloudCommandAction(CyApplicationManager applicationManager, SemanticSummaryManager cloudManager, SemanticSummaryParametersFactory parametersFactory)
	{
		super("Create Cloud");
		this.applicationManager = applicationManager;
		this.cloudManager = cloudManager;
		this.parametersFactory = parametersFactory;
	}

	//GETTERS AND SETTERS
	
	public void setAttributeColumns(List<String> columnNames) {
		this.attributeNames = columnNames;
	}

	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
	}

	public void setClusterTable(CyTable clusterTable) {
		this.clusterTable = clusterTable;
	}

	public List<CloudWordInfo> getWordInfo() {
		return this.wordInfo;
	}
	
	public void setNodes(Set<CyNode> nodes) {
		this.nodes = nodes;
	}


	//METHODS

	/**
	 * Method called when a Create Cloud action occurs.
	 * @param wordColumnName 
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

		//Check if network is already in our list
		SemanticSummaryParameters params;

		//Get SemanticSummaryParameters or register if necessary
		if(cloudManager.isSemanticSummary(network))
		{
			params = cloudManager.getParameters(network);

			//Update if necessary
			if (params.networkHasChanged(network)) params.updateParameters(network);
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
		cloudParams.setClusterTable(clusterTable);
		cloudParams.setCloudName(cloudName);
		cloudParams.setSelectedNodes(nodes);

		//Add to list of clouds
		params.addCloud(cloudParams.getCloudName(), cloudParams);

		// Select specified attribute column
		SemanticSummaryInputPanel inputPanel = cloudManager.getInputWindow();
		inputPanel.setAttributeNames(attributeNames);

		//Retrieve values from input panel
		cloudParams.retrieveInputVals(inputPanel);

		cloudParams.updateRatios();
		cloudParams.calculateFontSizes();

		CloudDisplayPanel cloudPanel = cloudManager.getCloudWindow();
		cloudPanel.updateCloudDisplay(cloudParams);

		//Update list of clouds
		inputPanel.addNewCloud(cloudParams);
		
//		inputPanel.getCreateNetworkButton().setEnabled(true);
//		inputPanel.getSaveCloudButton().setEnabled(true);
		
		//Update the list of filter words and checkbox
		inputPanel.refreshNetworkSettings();

//		//Enable adding of words to exclusion list
//		inputPanel.getAddWordTextField().setEditable(true);
//		inputPanel.getAddWordButton().setEnabled(true);

		// Add wordInfo to table
		this.wordInfo = cloudParams.getCloudWordInfoList();
		ArrayList<String> WC_Word = new ArrayList<String>();
		ArrayList<String> WC_FontSize = new ArrayList<String>();
		ArrayList<String> WC_Cluster = new ArrayList<String>();
		ArrayList<String> WC_Number = new ArrayList<String>();
		for (CloudWordInfo cloudWord : wordInfo) {
			String[] wordInfo = cloudWord.toSplitString();
			WC_Word.add(wordInfo[0]);
			WC_FontSize.add(wordInfo[1]);
			WC_Cluster.add(wordInfo[2]);
			WC_Number.add(wordInfo[3]);
		}

		CyRow clusterRow = clusterTable.getRow(cloudName);
		clusterRow.set("WC_Word", WC_Word);
		clusterRow.set("WC_FontSize", WC_FontSize);
		clusterRow.set("WC_Cluster", WC_Cluster);
		clusterRow.set("WC_Number", WC_Number);
	}
}