/*
 File: SemanticSummaryManager.java

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

package org.baderlab.wordcloud.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.ui.DualPanelDocker;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.input.CloudListSelectionHandler;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;

/**
 * The SemanticSummaryManager class is a singleton class that manages
 * all the parameters involved in using the Semantic Summary Plugin.
 * 
 * @author Layla Oesper
 * @version 1.0
 *
 */

public class SemanticSummaryManager implements SetCurrentNetworkListener, SetCurrentNetworkViewListener, NetworkAboutToBeDestroyedListener, RowsSetListener, ColumnCreatedListener, ColumnDeletedListener, AddedNodesListener, RemovedNodesListener, NetworkAddedListener 
{
	//VARIABLES
	private Map<CyNetwork, SemanticSummaryParameters> cyNetworkList;
	
	//Create only one instance of the input and cloud panels
	private SemanticSummaryInputPanel inputWindow;
	private CloudDisplayPanel cloudWindow;
	private DualPanelDocker docker;
	
	//Keep track of current network and cloud
	private SemanticSummaryParameters curNetwork;
	private CloudParameters curCloud;
	
	
	//Null Values for params
	private SemanticSummaryParameters nullSemanticSummary;
	private CloudParameters nullCloudParameters;

	private CyApplicationManager applicationManager;

	private SemanticSummaryParametersFactory parametersFactory;
	
	//CONSTRUCTOR
	/**
	 * This is a private constructor that is only called by the getInstance()
	 * method.
	 * @param application 
	 */
	public SemanticSummaryManager(CyApplicationManager applicationManager, SemanticSummaryParametersFactory parametersFactory)
	{
		this.applicationManager = applicationManager;
		this.parametersFactory = parametersFactory;
		
		cyNetworkList = new IdentityHashMap<CyNetwork, SemanticSummaryParameters>();
		nullSemanticSummary = parametersFactory.createSemanticSummaryParameters();
		nullCloudParameters = new CloudParameters(nullSemanticSummary);
		nullCloudParameters.setCloudName("Null Cloud");
		
		curNetwork = nullSemanticSummary;
		curCloud = nullCloudParameters;
	}
	
	//METHODS
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		networkDestroyed(event.getNetwork());
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent event) {
		CyNetwork network = event.getNetwork();
		setupCurrentNetwork(network);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent event) {
		CyNetworkView view = event.getNetworkView();
		CyNetwork network;
		if (view == null) {
			network = null;
		} else {
			network = view.getModel();
		}
		setupCurrentNetwork(network);
	}
	
	@Override
	public void handleEvent(ColumnCreatedEvent event) {
		if (inputWindow == null) {
			return;
		}
		inputWindow.refreshAttributeCMB();
	}
	
	@Override
	public void handleEvent(ColumnDeletedEvent event) {
		if (inputWindow == null) {
			return;
		}
		inputWindow.refreshAttributeCMB();
	}
	
	@Override
	public void handleEvent(AddedNodesEvent event) {
		networkModified(event.getSource());
	}
	
	@Override
	public void handleEvent(RemovedNodesEvent event) {
		networkModified(event.getSource());
	}

	@Override
	public void handleEvent(RowsSetEvent event) {
		TableMatch match = findMatch(event.getSource());
		if (match == null) {
			return;
		}
		
		String newTitle = null;
		for (RowSetRecord record : event.getPayloadCollection()) {
			if (record.getColumn().equals(CyNetwork.NAME)) {
				newTitle = (String) record.getValue();
				break;
			}
		}
		
		if (match.tableType.equals(CyNetwork.class) && newTitle != null && isSemanticSummary(match.network))
		{
			//Update Input Panel 
			getInputWindow().getNetworkLabel().setText(newTitle);
		}
		
	}
	
	@Override
	public void handleEvent(NetworkAddedEvent e) {
		CyNetwork network = e.getNetwork();
		if (!isSemanticSummary(network)) {
			return;
		}
		refreshCurrentNetworkList(network);
	}
	
	private TableMatch findMatch(CyTable table) {
		if (table == null) {
			return null;
		}
		for (Entry<CyNetwork, SemanticSummaryParameters> entry : cyNetworkList.entrySet()) {
			CyNetwork network = entry.getKey();
			Class<? extends CyIdentifiable> type = null;
			if (network.getDefaultNetworkTable() == table) {
				type = CyNetwork.class;
			} else if (network.getDefaultNodeTable() == table) {
				type = CyNode.class;
			} else if (network.getDefaultEdgeTable() == table) {
				type = CyEdge.class;
			}
			
			if (type != null) {
				return new TableMatch(network, type);
			}
		}
		return null;
	}

	/**
	 * Removes the CyNetwork from our list if it has just been destroyed.
	 * @param String - networkID of the destroyed CyNetwork
	 */
	private void networkDestroyed(CyNetwork network)
	{
		//Retrieve parameters and remove if it exists
		if (isSemanticSummary(network))
		{
			cyNetworkList.remove(network);
		}
	}
	
	public void reset() {
		cyNetworkList.clear();
	}
	
	/**
	 * Updates any current Network parameters that the network has changed, and 
	 * notifies clouds that they need to be recomputed.
	 *@param String - networkID of the modified CyNetwork
	 */
	private void networkModified(CyNetwork network)
	{
		//Retrieve parameters and mark modified
		if (isSemanticSummary(network))
		{
			SemanticSummaryParameters params = this.getParameters(network);
			params.updateParameters(network);
		}
	}
	
	/*
	 * Register a new network into the manager.
	 * @param CyNetwork - the CyNetwork we are adding.
	 * @param SemanticSummaryParameters - parameters for the network.
	 */
	public void registerNetwork(CyNetwork cyNetwork, SemanticSummaryParameters params)
	{
		cyNetworkList.put(cyNetwork, params);
	}
	
	/**
	 * Returns true if the networkID is already contained as a SemanticSummary
	 * @param String - the networkID to check.
	 */
	public boolean isSemanticSummary(CyNetwork network)
	{
		if (cyNetworkList.containsKey(network))
			return true;
		else
			return false;
	}
	
	
	/**
	 * Sets up the Manager with the current network.  Clears cloud and
	 * sets user input panel to defaults.
	 */
	public void setupCurrentNetwork(CyNetwork network)
	{
		//Null current network
		if (network == null)
		{
			curNetwork = nullSemanticSummary;
			curCloud = nullCloudParameters;
		}
		
		//Already Registered
		else if(isSemanticSummary(network))
			curNetwork = getParameters(network);
		
		//Need to create new
		else
		{
			SemanticSummaryParameters params = parametersFactory.createSemanticSummaryParameters(network);
			params.updateParameters(network);

			registerNetwork(network, params);
			
			curNetwork = params;
		}
		
		
		boolean hasDummy = false;
		for(CloudParameters cloud : curNetwork.getClouds().values()) {
			if(cloud.getCloudNum() == -99) {
				hasDummy = true;
				break;
			}
		}
		
		if(!hasDummy) {
			CloudParameters cloudParams = new CloudParameters(curNetwork);
			cloudParams.setCloudNum(-99);
			cloudParams.setCloudName("Synchronize with selection");
			cloudParams.setAttributeNames(getColumnNames(network, CyNode.class));
			curNetwork.addCloud(cloudParams.getCloudName(), cloudParams);
		}
		
		// MKTODO I think an NPE happens here
		//Update cloud list and update attributes
		getInputWindow().setNetworkList(curNetwork);
		getCloudWindow().clearCloud();
		getInputWindow().setUserDefaults();
		getInputWindow().refreshAttributeCMB();
		getInputWindow().refreshNetworkSettings();
		
		getInputWindow().loadCurrentCloud(curCloud);

	}
	
	/**
	 * Refreshes the current network list to be up to date.  Called whenever
	 * the tab in the control panel changes.
	 */
	public void refreshCurrentNetworkList(CyNetwork network)
	{
		String newNetworkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		
		//Null current network
		if (network == null)
		{
			curNetwork = nullSemanticSummary;
			curCloud = nullCloudParameters;
		}
		
		//Already Registered
		else if(isSemanticSummary(network))
			curNetwork = getParameters(network);
		
		//Need to create new
		else
		{
			SemanticSummaryParameters params = parametersFactory.createSemanticSummaryParameters(network);
			params.updateParameters(network);
			registerNetwork(network, params);
			curNetwork = params;
		}
		
		String oldNetworkName = curNetwork.getNetworkName();
		JList cloudList = getInputWindow().getCloudList();
		Object selected = cloudList.getSelectedValue();
		String CloudName = curCloud.getCloudName();
		
		//Update cloud list and update attributes
		getInputWindow().setNetworkList(curNetwork);//Clear cur cloud
		
		//If network has not changed, keep the same row highlighted
		if (newNetworkName.equals(oldNetworkName) && (selected != null))
		{
			//Turn off listener while doing work
			ListSelectionModel listSelectionModel = cloudList.getSelectionModel();
			CloudListSelectionHandler handler = getInputWindow().getCloudListSelectionHandler();
			listSelectionModel.removeListSelectionListener(handler);
			
			//Reset selected Value
			String selectedValue = (String)selected;
			getInputWindow().getCloudList().setSelectedValue(selectedValue, true);
			
			//Turn listener back on
			listSelectionModel.addListSelectionListener(handler);
			
			//Ensure current cloud has not changed
			if (curNetwork.containsCloud(CloudName))
				curCloud = curNetwork.getCloud(CloudName);
		}
		else if (!newNetworkName.equals(oldNetworkName))
		{
			getInputWindow().setUserDefaults();
			getInputWindow().refreshAttributeCMB();
			getCloudWindow().clearCloud();
			
		}
	}
	/**
	 * Returns instance of SemanticSummaryParameters for the networkID
	 * supplied, if it exists.
	 * @param String - networkID to get parameters for
	 * @return SemanticSummaryParameters
	 */
	public SemanticSummaryParameters getParameters(CyNetwork network)
	{
		return cyNetworkList.get(network);
	}
	
	/**
	 * Setup the nullCloudParameters for the manager now that it is initialized.
	 */
	public void setupNullCloudParams()
	{
		nullCloudParameters = new CloudParameters(null);
		nullCloudParameters.setCloudName("Null Cloud");
	}
	
	
	/**
	 * Returns the hashmap of all the SemanticSummaryParameters.
	 * @return HashMap of all the SemanticSummaryParameters for all networks.
	 */
	public Map<CyNetwork, SemanticSummaryParameters> getCyNetworkList()
	{
		return cyNetworkList;
	}
	
	/**
	 * Returns a reference to the SemanticSummaryInputPanel (WEST)
	 * @return SemanticSummaryInputPanel
	 */
	public SemanticSummaryInputPanel getInputWindow()
	{
		return inputWindow;
	}
	
	/**
	 * Sets reference to the panels.
	 * @param SemanticSummaryInputPanel - reference to panel
	 */
	public void setPanels(DualPanelDocker docker, SemanticSummaryInputPanel inputWindow, CloudDisplayPanel cloudWindow)
	{
		this.inputWindow = inputWindow;
		this.cloudWindow = cloudWindow;
		this.docker = docker;
	}
	
	/**
	 * Returns a reference to the CloudDisplayPanel (SOUTH)
	 * @return CloudDisplayPanel
	 */
	public CloudDisplayPanel getCloudWindow()
	{
		return cloudWindow;
	}
	
	public DualPanelDocker getDocker() 
	{
		return docker;
	}
	
	/**
	 * Get the parameters for the current network.
	 * @return SemanticSummaryParameters - the current network
	 */
	public SemanticSummaryParameters getCurNetwork()
	{
		return curNetwork;
	}
	
	/**
	 * Set the current network parameters.
	 * @param SemanticSummaryParameters - the current network.
	 */
	public void setCurNetwork(SemanticSummaryParameters params)
	{
		curNetwork = params;
	}
	
	/**
	 * Get the parameters of the current cloud.
	 * @return CloudParameters - the current cloud
	 */
	public CloudParameters getCurCloud()
	{
		return curCloud;
	}
	
	/**
	 * Sets the current cloud.
	 * @param CloudParameters - the current cloud.
	 */
	public void setCurCloud(CloudParameters params)
	{
		curCloud = params;
	}
	
	public SemanticSummaryParameters getNullSemanticSummary()
	{
		return nullSemanticSummary;
	}
	
	public CloudParameters getNullCloudParameters()
	{
		return nullCloudParameters;
	}

	public void allAttributeValuesRemoved(String objectKey, String attributeName) {
		// TODO Auto-generated method stub
		
	}

	public void attributeValueAssigned(CyNetwork network, CyNode curNode, String attributeName, Object[] keyIntoValue,
			Object oldAttributeValue, Object newAttributeValue) {

		if (curNode != null)
		{
			for (Entry<CyNetwork, SemanticSummaryParameters> entry : getCyNetworkList().entrySet())
			{
				SemanticSummaryParameters params = entry.getValue();
				CyNetwork cyNetwork = entry.getKey();
				
				if (cyNetwork.containsNode(curNode))
				{
					params.networkChanged();
				}
			}
		}
		
	}

	public void attributeValueRemoved(String objectKey, String attributeName, Object[] keyIntoValue,
			Object attributeValue) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Get the list of attribute names for either "node" or "edge". The attribute names will be
	 * prefixed either with "node." or "edge.". Those attributes whose data type is not
	 * "String" will be excluded
	 */
	public List<String> getColumnNames(CyNetwork network, Class<? extends CyIdentifiable> tableType) {
		if (network == null) {
			return Collections.emptyList();
		}
		
		List<String> attributeList = new ArrayList<String>();
		CyTable table = null;
		
		if (tableType.equals(CyNode.class)) {
			table = network.getDefaultNodeTable();
			
		}
		else if (tableType.equals(CyEdge.class)){
			table = network.getDefaultEdgeTable();			
		}
				
		if (table != null) {
			//  Show all attributes, with type of String or Number
			for (CyColumn column : table.getColumns()) {
				Class<?> type = column.getType();
				
				if (type.equals(String.class)) {
					attributeList.add(column.getName());
				}
				else if (type.equals(List.class) && column.getListElementType().equals(String.class))
				{
					attributeList.add(column.getName());
				}
			} //for loop
		
			//  Alphabetical sort
			Collections.sort(attributeList);
		}
		return attributeList;
	}

	static class TableMatch {
		final CyNetwork network;
		final Class<? extends CyIdentifiable> tableType;
		
		public TableMatch(CyNetwork network, Class<? extends CyIdentifiable> tableType) {
			this.network = network;
			this.tableType = tableType;
		}
	}
}
