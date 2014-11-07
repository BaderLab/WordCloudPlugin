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

package org.baderlab.wordcloud.internal.ui.action;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.baderlab.wordcloud.internal.cluster.CloudInfo;
import org.baderlab.wordcloud.internal.cluster.WordPair;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.CloudProvider;
import org.baderlab.wordcloud.internal.ui.WordCloudVisualStyleFactory;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;

/**
 * This is the action is associated with creating a new Network from an existing
 * word tag cloud.
 * @author Layla Oesper
 * @version 1.0
 */

@SuppressWarnings("serial")
public class CreateNetworkAction extends AbstractCyAction {
	
	private final CloudProvider cloudProvider;
	private final WordCloudVisualStyleFactory cloudStyleFactory;
	
	// Cytoscape services
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager viewManager;
	private final VisualMappingManager visualMappingManager;
	private final ApplyPreferredLayoutTaskFactory layoutTaskFactory;
	private final TaskManager<?, ?> taskManager;
	
	
	public CreateNetworkAction(CloudProvider cloudProvider, CyServiceRegistrar registrar) {
		super("Create Network From Cloud");
		
		this.cloudProvider = cloudProvider;
		
		this.networkFactory = registrar.getService(CyNetworkFactory.class);
		this.networkViewFactory  = registrar.getService(CyNetworkViewFactory.class);
		this.networkManager = registrar.getService(CyNetworkManager.class);
		this.viewManager = registrar.getService(CyNetworkViewManager.class);
		this.visualMappingManager = registrar.getService(VisualMappingManager.class);
		this.layoutTaskFactory = registrar.getService(ApplyPreferredLayoutTaskFactory.class);
		this.taskManager = registrar.getService(TaskManager.class);
		
		VisualStyleFactory styleFactory = registrar.getService(VisualStyleFactory.class);
		VisualMappingFunctionFactory continuousMappingFactory = registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory passthroughMappingFactory = registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		this.cloudStyleFactory = new WordCloudVisualStyleFactory(styleFactory, continuousMappingFactory, passthroughMappingFactory);
	}
	
	
	/**
	 * Method called when a Create Network Cloud action occurs.
	 */
	public void actionPerformed(ActionEvent ae)  {
		//Retrieve the current cloud and relevent information
		CloudParameters cloud = cloudProvider.getCloud();
		if(cloud == null)
			return;
		
		CloudInfo cloudInfo = cloud.calculateCloud();
		Map<String, Double> ratios = cloudInfo.getRatios();
		Map<WordPair, Double> pairRatios = cloudInfo.getPairRatios();
		
		//Create the network
		String newNetworkName = cloud.getNextNetworkName();
		CyNetwork network = createNetwork(newNetworkName);
		
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(WordCloudVisualStyleFactory.WORD_VAL, Double.class, false);
		
		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.createColumn(WordCloudVisualStyleFactory.CO_VAL, Double.class, false);
		
		//Create nodes
		Map<String, CyNode> wordNodes = new HashMap<String, CyNode>();
		for (Entry<String, Double> entry : ratios.entrySet()) {
			String curWord = entry.getKey();
			CyNode node = network.addNode();
			CyRow row = network.getRow(node);
			row.set(CyNetwork.NAME, curWord);
			
			//Add attribute to the node
			Double nodeRatio = entry.getValue();
			row.set(WordCloudVisualStyleFactory.WORD_VAL, nodeRatio);
			
			wordNodes.put(curWord, node);
		}
		
		//Create edges
		for (Entry<WordPair, Double> entry : pairRatios.entrySet()) {
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
			if (conditionalRatio > 1) {
				CyEdge edge = network.addEdge(node1, node2, false);
				CyRow row = network.getRow(edge);
				row.set(WordCloudVisualStyleFactory.CO_VAL, conditionalRatio);
			}
		}
		
		CyNetworkView view = createNetworkView(network);
		registerNetwork(network);
		registerNetworkView(view);
		
		//Visual Style stuff
		
		applyVisualStyle(view, cloud);
		view.updateView();
		
		//Create view
		applyPreferredLayout(view);
	}
	
	
	private CyNetwork createNetwork(String name) {
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, name);
		return network;
	}

	private void registerNetwork(CyNetwork network) {
		networkManager.addNetwork(network);
	}

	private CyNetworkView createNetworkView(CyNetwork network) {
		return networkViewFactory.createNetworkView(network);
	}

	private void registerNetworkView(CyNetworkView view) {
		viewManager.addNetworkView(view);
	}
	
	public void applyVisualStyle(CyNetworkView view, CloudParameters cloud) {
		CyNetwork network = view.getModel();
		String newNetworkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		String vs_name = newNetworkName + "WordCloud_style";
	
		// check to see if the style exists
		VisualStyle vs = getVisualStyle(vs_name);
		if (vs == null) {
			vs = cloudStyleFactory.createVisualStyle(vs_name, cloud);
			visualMappingManager.addVisualStyle(vs);
		}
	
		visualMappingManager.setVisualStyle(vs, view);
		vs.apply(view);
	}
	
	public void applyPreferredLayout(CyNetworkView view) {
		taskManager.execute(layoutTaskFactory.createTaskIterator(Collections.singleton(view)));
	}
	
	public VisualStyle getVisualStyle(String name) {
		for (VisualStyle style : visualMappingManager.getAllVisualStyles()) {
			if (style.getTitle().equals(name)) {
				return style;
			}
		}
		return null;
	}

}
