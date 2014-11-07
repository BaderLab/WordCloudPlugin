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


package org.baderlab.wordcloud.internal.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.cluster.CloudWordInfo;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.CloudProvider;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

/**
 * This is the action associated with updating a Semantic Summary Tag Cloud
 * anywhere in the Semantic Summary Plugin.
 */
@SuppressWarnings("serial")
public class UpdateCloudAction extends AbstractCyAction
{

	private final CloudProvider cloudProvider;
	private final UIManager uiManager;
	
	
	public UpdateCloudAction(CloudProvider cloudProvider, UIManager uiManager) {
		super("Update Cloud");
		this.cloudProvider = cloudProvider;
		this.uiManager = uiManager;
	}

	
	public void actionPerformed(ActionEvent e) {
		CloudParameters cloud = cloudProvider.getCloud();
		if(cloud == null)
			return;
		
		CyNetwork network = cloud.getNetworkParams().getNetwork();
		if(network == null)
			return;
		
		Set<CyNode> nodes = SelectionUtils.getSelectedNodes(network);
		
		if(!nodes.equals(cloud.getSelectedNodes())) {
			cloud.setSelectedNodes(nodes);
			uiManager.getCloudDisplayPanel().updateCloudDisplay(cloud);	
		}
		
		if (cloud.getClusterTable() != null) {
			List<CloudWordInfo> wordInfos = cloud.calculateCloud().getCloudWordInfoList();
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
			CyRow clusterRow = cloud.getClusterTable().getRow(cloud.getCloudName());
			clusterRow.set("WC_Word", WC_Word);
			clusterRow.set("WC_FontSize", WC_FontSize);
			clusterRow.set("WC_Cluster", WC_Cluster);
			clusterRow.set("WC_Number", WC_Number);
		}
	}
	
}
