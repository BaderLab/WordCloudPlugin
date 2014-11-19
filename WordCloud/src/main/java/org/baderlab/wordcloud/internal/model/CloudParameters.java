/*
 File: CloudParameters.java

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.cluster.CloudDisplayStyles;
import org.baderlab.wordcloud.internal.cluster.CloudInfo;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;

/**
 * The CloudParameters class defines all of the variables that are
 * needed to create a word Cloud for a particular network, attribute, 
 * and set of selected nodes.
 * @author Layla Oesper
 * @version 1.0
 *
 */

public class CloudParameters implements Comparable<CloudParameters>, CloudProvider
{
	//Default Values for User Input
	public static final double DEFAULT_NET_WEIGHT = 0.0; // this can be overridden in wordcloud.props
	public static final String DEFAULT_ATT_NAME = CyNetwork.NAME;
	public static final int    DEFAULT_MAX_WORDS = 250;
	public static final double DEFAULT_CLUSTER_CUTOFF = 1.0;
	public static final int    DEFAULT_MIN_OCCURRENCE = 1;
	
	private final NetworkParameters networkParams; //parent network
	private CloudInfo cloudWordInfoBuilder;
	private boolean calculated = false;
	
	private String cloudName;
	private List<String> attributeNames;
	private CloudDisplayStyles displayStyle;
	
	private int cloudNum; //Used to order the clouds for each network
	private int maxWords = DEFAULT_MAX_WORDS;
	private double clusterCutoff =  DEFAULT_CLUSTER_CUTOFF;
	private double netWeightFactor = DEFAULT_NET_WEIGHT;
	private int minWordOccurrence = DEFAULT_MIN_OCCURRENCE;
	
	private String clusterColumnName;
	private CyTable clusterTable;
	
	
	//String Delimeters
	//private static final String NODEDELIMITER = "CloudParamNodeDelimiter";
	private static final String WORDDELIMITER = "CloudParamWordDelimiter";
	
	//Network Name creation variables
	private int networkCount = 1;
	private static final String NETWORKNAME = "Net";
	private static final String SEPARATER = "_";
	
	/**
	 * Default constructor to create a fresh instance
	 */
	protected CloudParameters(NetworkParameters networkParams, String cloudName, int cloudNum) {
		if(cloudName == null)
			throw new NullPointerException();
		this.networkParams = networkParams;
		this.displayStyle = CloudDisplayStyles.getDefault();
		this.cloudNum = cloudNum;
		this.cloudName = cloudName;
		createColumn(cloudName); // create the column for the cloud
	}
	
	
	public void invalidate() {
		calculated = false;
		cloudWordInfoBuilder = null;
	}
	
	
	/**
	 * Returns the object that is responsible for calculating the cloud.
	 * Warning this method has the potential to be long running.
	 * Warning this method is synchronized, so only one thread may be
	 * calculated the could at one time. This is done because 
	 * the CloudParameters object is mutable.
	 */
	public synchronized CloudInfo calculateCloud() {
		if(cloudWordInfoBuilder == null) {
			cloudWordInfoBuilder = new CloudInfo(this);
			cloudWordInfoBuilder.calculateFontSizes();
			calculated = true;
		}
		return cloudWordInfoBuilder;
	}
	
	/**
	 * Returns true if the cloud has already been calculated.
	 */
	public boolean isAlreadyCalculated() {
		return calculated;
	}
	
	/**
	 * Constructor to create CloudParameters from a cytoscape property file
	 * while restoring a session.  Property file is created when the session is saved.
	 * @param propFile - the name of the property file as a String
	 */
	protected CloudParameters(NetworkParameters networkParams, String propFile)
	{
		this.networkParams = networkParams;
		
		//Create a hashmap to contain all the values in the rpt file
		HashMap<String, String> props = new HashMap<String,String>();
		
		String[] lines = propFile.split("\n");
		
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			String[] tokens = line.split("\t");
			//there should be two values in each line
			if(tokens.length == 2)
				props.put(tokens[0],tokens[1]);
		}
		
		this.cloudName = props.get("CloudName");
		this.displayStyle = CloudDisplayStyles.fromString(props.get("DisplayStyle"));
		
		this.netWeightFactor = Double.valueOf(props.get("NetWeightFactor"));
		this.clusterCutoff = Double.valueOf(props.get("ClusterCutoff"));
		this.cloudNum = Integer.valueOf(props.get("CloudNum"));
		
		if(props.get("MaxWords") != null) {
			maxWords = Integer.valueOf(props.get("MaxWords"));
		}
		if(props.get("MinOccurrence") != null) {
			minWordOccurrence = Integer.valueOf(props.get("MinOccurrence"));
		}
		
		// Reload cloud group table if it has been created (through command line)
		for (CyTable table : networkParams.getManager().getTableManager().getAllTables(true)) {
			if (table.getTitle().equals(props.get("ClusterTableName"))) {
				this.clusterTable = table;
			}
		}
		
		//Rebuild attribute List
		String value = props.get("AttributeName");
		String[] attributes = value.split(WORDDELIMITER);
		ArrayList<String> attributeList = new ArrayList<String>();
		for (int i = 0; i < attributes.length; i++)
		{
			String curAttribute = attributes[i];
			attributeList.add(curAttribute);
		}
		this.attributeNames = attributeList;
	}
	
	/**
	 * This method assumes that NetworkParameters has already checked if the
	 * column name already exists.
	 */
	private void createColumn(String name) {
		CyNetwork network = networkParams.getNetwork(); // in case of null network
		if (network == null)
			return;
		
		CyTable table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS); // always create new columns in the local table
		if(table.getColumn(name) == null) {
			table.createColumn(name, Boolean.class, false);
		}
	}
		
	
	public boolean isNullCloud() {
		return this == networkParams.getNullCloud();
	}
	
	//METHODS
	
	public void delete() {
		if(!networkParams.getClouds().contains(this)) // already deleted
			return;
		
		CyNetwork network = networkParams.getNetwork();
		
		// Check default table for backwards compatibility (older versions of wordcloud created a column in the default table).
		CyTable defaultNodeTable = network.getDefaultNodeTable();
		if (defaultNodeTable.getColumn(cloudName) != null) {
			defaultNodeTable.deleteColumn(cloudName);
		}
		
		CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		if (localTable.getColumn(cloudName) != null) {
			localTable.deleteColumn(cloudName);
		}
		
		CyTable clusterTable = getClusterTable();
		if (clusterTable != null) {
			clusterTable.deleteRows(Arrays.asList(cloudName));
			if (clusterTable.getRowCount() == 0) {
				// Delete column in network table with wordCloud ID
				network.getDefaultNetworkTable().deleteColumn(clusterTable.getTitle());
				// Delete wordCloud table
				networkParams.getManager().getTableManager().deleteTable(clusterTable.getSUID());						
			}
		}
		
		CloudModelManager cloudModelManager = networkParams.getManager();
		networkParams.removeCloudMapping(this);
		cloudModelManager.fireCloudDeleted(this);
	}
	
	
	// MKTODO shouldn't this rename the cloud column?
	public void rename(String newName) {
		if(newName.equals(cloudName))
			return;
		if(networkParams.containsCloud(newName))
			throw new IllegalArgumentException("Name '" + newName + "' already exists");
		if(networkParams.columnAlreadyExists(newName))
			throw new IllegalArgumentException("Column '" + newName + "' already exists");
		
		String oldName = cloudName;
		cloudName = newName;
		networkParams.changeCloudMapping(oldName, newName);
		
		CyNetwork network = networkParams.getNetwork();
		
		// Column might be in local or default table (backwards compatibility)
		CyColumn column;
		
		CyTable defaultNodeTable = network.getDefaultNodeTable();
		column = defaultNodeTable.getColumn(oldName);
		if(column == null) {
			CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
			column = localTable.getColumn(oldName);
		}
		
		column.setName(newName);
		
		CloudModelManager cloudModelManager = networkParams.getManager();
		cloudModelManager.fireCloudModified(this);
	}
	
	

	
	/**
	 * String representation of CloudParameters.
	 * It is used to store the persistent Attributes as a property file.
	 * @return - String representation of this object
	 */
	public String toString()
	{
		StringBuffer paramVariables = new StringBuffer();
		
		paramVariables.append("CloudName\t" + cloudName + "\n");
		paramVariables.append("DisplayStyle\t" + displayStyle + "\n");
		
		//List of attributes as a delimited list
		StringBuffer output = new StringBuffer();
		if (attributeNames != null) {
			for (int i = 0; i < attributeNames.size(); i++)
			{
				output.append(attributeNames.get(i) + WORDDELIMITER);
			}
		}
		paramVariables.append("AttributeName\t" + output.toString() + "\n");
		
		// Save out dummy values for fields that are not longer used just to be compatible with WordCloud 2.0
		paramVariables.append("NetWeightFactor\t" + netWeightFactor + "\n");
		paramVariables.append("ClusterCutoff\t" + clusterCutoff + "\n");
		paramVariables.append("CountInitialized\t" + false + "\n");
		paramVariables.append("SelInitialized\t" + false + "\n");
		paramVariables.append("RatiosInitialized\t" + false + "\n");
		paramVariables.append("MinRatio\t" + 0.0 + "\n");
		paramVariables.append("MaxRatio\t" + 0.0 + "\n");
		paramVariables.append("MaxWords\t" + maxWords + "\n");
		paramVariables.append("MeanRatio\t" + 0.0 + "\n");
		paramVariables.append("MeanWeight\t" + 0.0 + "\n");
		paramVariables.append("MaxWeight\t" + 0.0 + "\n");
		paramVariables.append("MinWeight\t" + 0.0 + "\n");
		paramVariables.append("CloudNum\t" + cloudNum + "\n");
		paramVariables.append("UseNetNormal\t" + true + "\n");
		paramVariables.append("NetworkCount\t" + networkCount + "\n");
		if (clusterTable != null) {
			paramVariables.append("ClusterTableName\t" + clusterTable.getTitle() + "\n");
		}
		paramVariables.append("MinOccurrence\t" + minWordOccurrence + "\n");
		
//		//List of Nodes as a comma delimited list
		StringBuffer output2 = new StringBuffer();
//		for (int i = 0; i < cloudWords.size(); i++)
//		{
//			output2.append(cloudWords.get(i).toString() + WORDDELIMITER);
//		}
		
		paramVariables.append("CloudWords\t" + output2.toString() + "\n");
		
		return paramVariables.toString();
	}
	
	
	
	
	
	

	
	
	/**
	 * Compares two CloudParameters objects based on the order in which they
	 * were created.
	 * @param CloudParameters object to compare this object to
	 * @return
	 */
	public int compareTo(CloudParameters other) 
	{	
		return cloudNum - other.cloudNum;
	}
	
	/**
	 * Returns the name for the next network for this cloud.
	 * @return String - name of the next cloud
	 */
	public String getNextNetworkName()
	{
		String name = networkParams.getNetworkName() + "-" + cloudName + "-" + NETWORKNAME + SEPARATER + networkCount;
		networkCount++;
		
		return name;
	}
	
	
	//Getters and Setters
	public String getCloudName()
	{
		return cloudName;
	}
	
	
	public List<String> getAttributeNames()
	{
		return attributeNames;
	}
	
	public void setAttributeNames(List<String> names)
	{
		//Check if we need to reset flags
		boolean changed = false;
		if (attributeNames == null || names.size() != attributeNames.size())
			changed = true;
		else
		{
			for (int i = 0; i < names.size(); i++)
			{
				String curAttribute = names.get(i);
				
				if (!attributeNames.contains(curAttribute))
				{
					changed = true;
					continue;
				}
			}
		}
		
		//Set flags
		if (changed)
		{
			invalidate();
		}
		
		//Set to new value
		attributeNames = names;
	}
	
	public void addAttributeName(String name)
	{
		if (attributeNames == null) {
			attributeNames = new ArrayList<String>();
		}
		
		if (!attributeNames.contains(name))
		{
			attributeNames.add(name);
			invalidate();
		}
	}
	
	public void removeAttribtueName(String name) {
		if(attributeNames != null) {
			boolean removed = attributeNames.remove(name);
			if(removed) {
				invalidate();
			}
		}
	}
	

	
	public NetworkParameters getNetworkParams()
	{
		return networkParams;
	}
	
	public Set<CyNode> getSelectedNodes()
	{
		return getSelectedNodes(networkParams.getNetwork());
	}
	
	private Set<CyNode> getSelectedNodes(CyNetwork network) {
		if (network == null) {
			return Collections.emptySet();
		}
		
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			Boolean selected = row.get(cloudName, Boolean.class);
			if (selected != null && selected) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public void setSelectedNodes(Collection<CyNode> nodes)
	{
		setSelectedNodes(networkParams.getNetwork(), nodes);
		invalidate();
	}
	
	private void setSelectedNodes(CyNetwork network, Collection<CyNode> nodes) {
		if (network == null) {
			return;
		}
		
		// if the user deleted the column then do nothing, better than an exception
		if(network.getDefaultNodeTable().getColumn(cloudName) == null) {
			return;
		}
		
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			Boolean wasSelected = row.get(cloudName, Boolean.class);
			if (wasSelected == null) {
				wasSelected = Boolean.FALSE;
			}
			boolean selected = nodes.contains(node);
			if (wasSelected != selected) {
				row.set(cloudName, selected);
			}
		}
	}

	public int getSelectedNumNodes()
	{
		return getSelectedNumNodes(networkParams.getNetwork());
	}
	
	private int getSelectedNumNodes(CyNetwork network) {
		if (network == null) {
			return 0;
		}
		
		return getSelectedNodes(network).size();
	}
	
	
	public int getNetworkNumNodes()
	{
		if (networkParams == null) {
			return 0;
		}
		CyNetwork network = networkParams.getNetwork();
		if (network == null) {
			return 0;
		}
		return network.getNodeCount();
	}

	public double getMinRatio()
	{
		return cloudWordInfoBuilder.getMinRatio();
	}
	
	public double getMaxRatio()
	{
		return cloudWordInfoBuilder.getMaxRatio();
	}
	
	public double getNetWeightFactor()
	{
		return netWeightFactor;
	}
	
	public void setNetWeightFactor(double val)
	{
		netWeightFactor = val;
	}
	
	public double getClusterCutoff()
	{
		return clusterCutoff;
	}
	
	public void setClusterCutoff(double val)
	{
		clusterCutoff = val;
	}
	
	public int getMaxWords()
	{
		return maxWords;
	}
	
	public void setMaxWords(int val)
	{
		maxWords = val;
	}
	
	public int getMinWordOccurrence()
	{
		return minWordOccurrence;
	}
	
	public void setMinWordOccurrence(int val) 
	{
		minWordOccurrence = val;
	}
	
	public int getCloudNum()
	{
		return cloudNum;
	}
	
	public CloudDisplayStyles getDisplayStyle()
	{
		return displayStyle;
	}
	
	public void setDisplayStyle(CloudDisplayStyles style)
	{
		displayStyle = style;
	}
	
	public String getClusterColumnName() {
		return clusterColumnName;
	}
	
	public void setClusterColumnName(String clusterColumnName) {
		this.clusterColumnName = clusterColumnName;		
	}

	public void setClusterTable(CyTable clusterTable) {
		this.clusterTable = clusterTable;
	}
	
	public CyTable getClusterTable() {
		if (clusterTable != null) {
			return clusterTable;
		} else {
			try {
				CyNetwork network = networkParams.getNetwork();
				CyTableManager tableManager = networkParams.getManager().getTableManager();
				String tableName = cloudName.substring(0, cloudName.indexOf(" Cloud "));
				return tableManager.getTable(network.getDefaultNetworkTable().getRow(network.getSUID()).get(tableName, Long.class));
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	@Override
	public CloudParameters getCloud() {
		return this;
	}
}
