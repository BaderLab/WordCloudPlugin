/*
 File: DeleteCloudAction.java

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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/**
 * This is the action associated with deleting a Semantic Summary Tag Cloud
 * anywhere in the Semantic Summary Plugin.
 * @author Layla Oesper
 * @version 1.0
 */

public class DeleteCloudAction extends AbstractSemanticSummaryAction
{
	//VARIABLES
	
	private static final long serialVersionUID = -321557784440369508L;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	
	//CONSTRUCTORS

	/**
	 * DeleteCloudAction constructor.
	 */
	public DeleteCloudAction(CySwingApplication application, SemanticSummaryManager cloudManager)
	{
		super("Delete Cloud");
		this.application = application;
		this.cloudManager = cloudManager;
	}
	
	//METHODS
	
	/**
	 * Method called when a Delete Cloud action occurs.
	 * 
	 * @param ActionEvent - event created when choosing Delete Cloud.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		//Retrieve current cloud and Network from Manager
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		CloudParameters cloudParams = cloudManager.getCurCloud();
		
		int selection;
		if (ae.getActionCommand() == "No confirmation") {
			selection = JOptionPane.YES_OPTION;
		} else {
			selection = confirmDelete();
		}
		
		if (selection == JOptionPane.YES_OPTION)
		{
		
			//Delete if cloud is not null
			if (cloudParams != null && 
					cloudParams != cloudManager.getNullCloudParameters()) {
				String cloudName = cloudParams.getCloudName();
			
				CyNetwork network = networkParams.getNetwork();
				
				//Remove cloud from list
				networkParams.getClouds().remove(cloudName);
				if (network.getDefaultNodeTable().getColumn(cloudName) != null) {
					network.getDefaultNodeTable().deleteColumn(cloudName);
				}
				
				CyTable clusterTable = cloudParams.getClusterTable();
				if (clusterTable != null) {
					ArrayList<String> cloudRow = new ArrayList<String>();
					cloudRow.add(cloudParams.getCloudName());
					clusterTable.deleteRows(cloudRow);
					if (clusterTable.getRowCount() == 0) {
						// Delete column in network table with wordCloud ID
						network.getDefaultNetworkTable().deleteColumn(cloudParams.getClusterTable().getTitle());
						// Delete wordCloud table
						networkParams.getModelManager().getTableManager().deleteTable(cloudParams.getClusterTable().getSUID());						
					}
				}
				
				//Update Current network
				cloudManager.setupCurrentNetwork(networkParams.getNetwork());
			
				pluginAction.loadPanels();
			}
		}
	}
	
	private int confirmDelete()
	{
		//Ask to continue or revert
		Component parent = application.getJFrame();
		int value = JOptionPane.NO_OPTION;
		
		value = JOptionPane.showConfirmDialog(parent,"Are you sure you want to permanently delete the selected cloud?", 
				"Delete Cloud",
				JOptionPane.YES_NO_OPTION);
		
		return value;
	}
}
