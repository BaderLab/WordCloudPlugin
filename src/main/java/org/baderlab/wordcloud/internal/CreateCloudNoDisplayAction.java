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

package org.baderlab.wordcloud.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class CreateCloudNoDisplayAction extends AbstractSemanticSummaryAction 
{

	//VARIABLES
	private static final long serialVersionUID = -5065616290485908393L;
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private String nameColumnName;
	private String clusterColumnName;
	private List<CloudWordInfo> wordInfo;
	private Integer clusterNumber;
	private String cloudNamePrefix;
	
	//CONSTRUCTORS
	
		/**
	 * CreateCloudAction constructor.
	 * @param pluginAction 
	 */
	
	public CreateCloudNoDisplayAction(CyApplicationManager applicationManager, CySwingApplication application, SemanticSummaryManager cloudManager, SemanticSummaryParametersFactory parametersFactory)
	{
		super("Create Cloud");
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.parametersFactory = parametersFactory;
	}
	
	public void setAttributeColumn(String columnName) {
		this.nameColumnName = columnName;
	}
	
	public void setClusterColumn(String columnName) {
		this.clusterColumnName = columnName;
	}
	
	public void setClusterNumber(Integer clusterNumber) {
		this.clusterNumber = clusterNumber;
	}

	public void setCloudNamePrefix(String cloudNamePrefix) {
		this.cloudNamePrefix = cloudNamePrefix;
	}
	
	public List<CloudWordInfo> getWordInfo() {
		return this.wordInfo;
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
		cloudParams.setCloudName(cloudNamePrefix + "Cloud " + clusterNumber);
		
		Set<CyNode> nodes = SelectionUtils.getSelectedNodes(network);
		
		cloudParams.setSelectedNodes(nodes);
		
		//Add to list of clouds
		params.addCloud(cloudParams.getCloudName(), cloudParams);
		
		// Select all attributes by default
		SemanticSummaryInputPanel inputPanel = cloudManager.getInputWindow();
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add(nameColumnName);
		inputPanel.setAttributeNames(attributes);
		
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
		List<CyRow> table = network.getDefaultNodeTable().getAllRows();
		for (CyRow row : table) {
			if (row.get(clusterColumnName, Integer.class) == clusterNumber) {
				row.set(cloudNamePrefix + "WC_Word", WC_Word);
				row.set(cloudNamePrefix + "WC_FontSize", WC_FontSize);
				row.set(cloudNamePrefix + "WC_Cluster", WC_Cluster);
				row.set(cloudNamePrefix + "WC_Number", WC_Number);
			}
		}

		// Get rid of the membership columns
		for (CyColumn column : network.getDefaultNodeTable().getColumns()) {
			String name = column.getName();
			if (name.equals(cloudParams.getCloudName())) {
				network.getDefaultNodeTable().deleteColumn(name);
			}
		}
	}
}