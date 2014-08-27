/*
 File: CreateCloudNetworkAction.java

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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;

/**
 * This is the action is associated with creating a new Network from an existing
 * word tag cloud.
 * @author Layla Oesper
 * @version 1.0
 */

public class CreateCloudNetworkAction extends AbstractCyAction
{

	private static final long serialVersionUID = 2683655996962050569L;
	
	//VARIABLES
	public static String WORD_VAL = "Word_Prob";
	public static String CO_VAL = "CO_Prob";
	public static String INTERACTION_TYPE = "CO";
	private static final char controlChar = '\u001F';

	private final ModelManager modelManager;

	private SemanticSummaryManager cloudManager;
	
	//CONSTRUCTORS
	
	/**
	 * CreateCloudAction constructor.
	 */
	public CreateCloudNetworkAction(ModelManager modelManager, SemanticSummaryManager cloudManager)
	{
		super("Create Cloud Network");
		this.modelManager = modelManager;
		this.cloudManager = cloudManager;
	}
	
	//METHODS
	
	/**
	 * Method called when a Create Network Cloud action occurs.
	 * 
	 * @param ActionEvent - event created when choosing to create a network
	 * from an existing cloud.
	 */
	public void actionPerformed(ActionEvent ae) 
	{
		//Retrieve the current cloud and relevent information
		CloudParameters curCloud = cloudManager.getCurCloud();
		Map<String, Double> ratios = curCloud.getRatios();
		Map<WordPair, Double> pairRatios = curCloud.getPairRatios();
		
		//Create the network
		String newNetworkName = curCloud.getNextNetworkName();
		CyNetwork network = modelManager.createNetwork(newNetworkName);
		
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(WORD_VAL, Double.class, false);
		
		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.createColumn(CO_VAL, Double.class, false);
		
		//Create nodes
		Map<String, CyNode> wordNodes = new HashMap<String, CyNode>();
		for (Entry<String, Double> entry : ratios.entrySet())
		{
			String curWord = entry.getKey();
			CyNode node = network.addNode();
			CyRow row = network.getRow(node);
			row.set(CyNetwork.NAME, curWord);
			
			//Add attribute to the node
			Double nodeRatio = entry.getValue();
			row.set(WORD_VAL, nodeRatio);
			
			wordNodes.put(curWord, node);
		}
		
		//Create edges
		for (Entry<WordPair, Double> entry : pairRatios.entrySet())
		{
			WordPair pair = entry.getKey();
			Double edgeRatio = entry.getValue();
			String nodeName1 = pair.getFirstWord();
			String nodeName2 = pair.getSecondWord();
			CyNode node1 = wordNodes.get(nodeName1);
			CyNode node2 = wordNodes.get(nodeName2);
			Double node1Ratio = ratios.get(nodeName1);
			Double node2Ratio = ratios.get(nodeName2);
			Double conditionalRatio = edgeRatio / (node1Ratio * node2Ratio);
			
			//Only create if prob > 1
			if (conditionalRatio > 1)
			{
				CyEdge edge = network.addEdge(node1, node2, false);
				CyRow row = network.getRow(edge);
				row.set(CO_VAL, conditionalRatio);
			}
		}
		
		CyNetworkView view = modelManager.createNetworkView(network);
		modelManager.registerNetwork(network);
		modelManager.registerNetworkView(view);
		
		//Visual Style stuff
		
		modelManager.applyVisualStyle(view, curCloud);
		view.updateView();
		
		//Create view
		modelManager.applyPreferredLayout(view);
	}

}
