/*
 File: UpdateCloudAction.java

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
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

/**
 * This is the action associated with updating a Semantic Summary Tag Cloud
 * anywhere in the Semantic Summary Plugin.
 * @author Layla Oesper
 * @version 1.0
 */

public class UpdateCloudAction extends AbstractSemanticSummaryAction
{

	//VARIABLES
	private static final long serialVersionUID = -814444939886393362L;
	private SemanticSummaryManager cloudManager;
	private CyApplicationManager applicationManager;
	
	//CONSTRUCTORS
	

	/**
	 * DeleteCloudAction constructor.
	 */
	public UpdateCloudAction(SemanticSummaryManager cloudManager, CyApplicationManager applicationManager)
	{
		super("Update Cloud");
		this.cloudManager = cloudManager;
		this.applicationManager = applicationManager;
	}
	
	//METHODS
	
	/**
	 * Method called when an Update Cloud action occurs.
	 * 
	 * @param ActionEvent - event created when choosing Update Cloud.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		this.doRealAction();
	}
	
	/**
	 * Method that actually contains what actually needs to happen on this action.
	 */
	public void doRealAction()
	{
		//Initialize the Semantic Summary Panels/Bring to front
		pluginAction.doRealAction();
		
		//Retrieve current cloud and Network from Manager
		CloudParameters cloudParams = cloudManager.getCurCloud();
		SemanticSummaryParameters networkParams = cloudParams.getNetworkParams();
		
		//Retrieve current network and view
		CyNetwork network = networkParams.getNetwork();
		if (network == null || !cloudManager.isSemanticSummary(network)) {
			return;
		}
		
		//Update network if necessary
		if (networkParams.networkHasChanged(network))
			networkParams.updateParameters(network);
		
		//Retrieve values from input panel
		cloudParams.retrieveInputVals(cloudManager.getInputWindow());
		
		//Update with new information
		cloudParams.calculateFontSizes();
		
		if ((Integer) cloudParams.getClusterNumber() != null) {
			List<CloudWordInfo> wordInfos = cloudParams.getCloudWordInfoList();
			ArrayList<String> WC_Word = new ArrayList<String>();
			ArrayList<String> WC_FontSize = new ArrayList<String>();
			ArrayList<String> WC_Cluster = new ArrayList<String>();
			ArrayList<String> WC_Number = new ArrayList<String>();
			for (CloudWordInfo cloudWord : wordInfos) {
				String[] wordInfo = cloudWord.toSplitString();
				WC_Word.add(wordInfo[0]);
				WC_FontSize.add(wordInfo[1]);
				WC_Cluster.add(wordInfo[2]);
				WC_Number.add(wordInfo[3]);
			}
			List<CyRow> table = network.getDefaultNodeTable().getAllRows();
			for (CyRow row : table) {
				if (row.get(cloudParams.getClusterColumnName(), Integer.class) != null &&  row.get(cloudParams.getClusterColumnName(), Integer.class) == cloudParams.getClusterNumber()) {
					String cloudNamePrefix = cloudParams.getCloudNamePrefix();
					row.set(cloudNamePrefix + "WC_Word", WC_Word);
					row.set(cloudNamePrefix + "WC_FontSize", WC_FontSize);
					row.set(cloudNamePrefix + "WC_Cluster", WC_Cluster);
					row.set(cloudNamePrefix + "WC_Number", WC_Number);
				}
			}
		}
		
		CloudDisplayPanel cloudPanel =
			cloudManager.getCloudWindow();
		
		cloudPanel.updateCloudDisplay(cloudParams);
	}
}
