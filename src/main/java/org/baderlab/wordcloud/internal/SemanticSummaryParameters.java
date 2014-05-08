/*
 File: SemanticSummaryParameters.java

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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;

/**
 * This SemanticSummaryParameters class defines all of the variables that are
 * needed to create and manipulate an individual Semantic Summary for a 
 * particular network and attribute.
 * @author Layla Oesper
 * @version 1.0
 *
 */

public class SemanticSummaryParameters 
{
	
	//VARIABLES
	private CyNetwork network;
	private Map<String, CloudParameters> clouds; //list of network's clouds
	
	//Name creation variables
	private static final String CLOUDNAME = "Cloud";
	private static final String SEPARATER = "_";
	
	
	//Font Size Values
	private static final Integer MINFONTSIZE = 12; 
	private static final Integer MAXFONTSIZE = 64;
	
	//Filter stuff
	private WordFilter filter;
	private WordDelimiters delimiters;
	
	private ModelManager modelManager;
	
	//CONSTRUCTORS
	
	/**
	 * Default constructor to create a fresh instance
	 * @param application 
	 * @throws IOException 
	 */
	public SemanticSummaryParameters(ModelManager modelManager, CySwingApplication application, WordFilterFactory filterFactory)
	{
		this.clouds = new HashMap<String,CloudParameters>();
		this.filter = filterFactory.createWordFilter();
		this.delimiters = new WordDelimiters(application);
		this.modelManager = modelManager;
	}
	
	/**
	 * Constructor to create SemanticSummaryParameters from a cytoscape property file
	 * while restoring a session.  Property file is created when the session is saved.
	 * @param propFile - the name of the property file as a String
	 * @throws IOException 
	 */
	public SemanticSummaryParameters(CyNetwork network, ModelManager modelManager, CySwingApplication application, WordFilterFactory filterFactory)
	{
		this(modelManager, application, filterFactory);
		
		this.network = network;
		if (!modelManager.hasCloudMetadata(network)) {
			modelManager.createCloudMetadata(network);
		}
	}
	
	//METHODS
	
	//DATA MANIPULATIONS
	/**
	 * Adds a new cloud to the SemanticSummary HashMap of clouds for this
	 * network.
	 * @param String - the name of the new cloud.
	 * @param CloudParameters - parameters for this cloud.
	 */
	public void addCloud(String name, CloudParameters params)
	{
		if (!clouds.containsKey(name))
		{
			clouds.put(name, params);
		}
	}
	
	/**
	 * Removes a cloud from the HashMap of clouds for this network.
	 * @param String - name of the cloud to remove.
	 */
	public void removeCloud(String name)
	{
		if (clouds.containsKey(name))
			clouds.remove(name);
	}
	
	/**
	 * Returns true if the particular cloud named is contained in this
	 * SemanticSummaryParameters object.
	 * @return true if the specified cloud is contained in this object.
	 */
	public boolean containsCloud(String name)
	{
		if (clouds.containsKey(name))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the specified cloudParameters if it is contained in this object.
	 * Or returns, null if the cloud is not contained.
	 * @param String - name of the CloudParameters to return.
	 * @return CloudParameters associated with the given name.
	 */
	public CloudParameters getCloud(String name)
	{
		if (this.containsCloud(name))
			return clouds.get(name);
		else
			return null;	
	}
	/**
	 * Tells all the contained clouds that the network has changed and that
	 * they need to re-initialize.
	 */
	public void networkChanged()
	{
		for (String curCloud : clouds.keySet())
		{
			CloudParameters cloudParams = clouds.get(curCloud);
			cloudParams.setCountInitialized(false);
			cloudParams.setSelInitialized(false);
			cloudParams.setRatiosInitialized(false);
		}
		modelManager.acceptChanges(network);
	}
	
	/**
	 * Returns the name for the next cloud for this network.
	 * @return String - name of the next cloud
	 */
	public String getNextCloudName()
	{
		int cloudCount = getCloudCount();
		String name = CLOUDNAME + SEPARATER + cloudCount;
		modelManager.incrementCloudCounter(network);
		
		return name;
	}
	
	/**
	 * This method updates the parameters associated with a network to be current.
	 * Specifically, this includes updating the list of nodes included
	 * in the Parameters.
	 * @param CyNetwork to update the parameters based on.
	 */
	public void updateParameters(CyNetwork network)
	{
		this.networkChanged();
	}
	
	
	/**
	 * This method will check to see if the list of nodes in this object are
	 * up to date with the supplied network.  It returns true only if the network
	 * is different than the parameters.
	 * @return boolean - whether or not this network has changed.
	 */
	public boolean networkHasChanged(CyNetwork network)
	{
		return modelManager.hasChanges(network);
	}
	
	

	//GETTERS and SETTERS
	
	public String getNetworkName()
	{
		if (network == null) {
			return "No Network Loaded";
		}
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
	public Map<String, CloudParameters> getClouds()
	{
		return clouds;
	}
	
	public void setClouds(HashMap<String, CloudParameters> cloudMap)
	{
		clouds = cloudMap;
	}
	
	public Integer getMaxFont()
	{
		return MAXFONTSIZE;
	}
	
	public Integer getMinFont()
	{
		return MINFONTSIZE;
	}
	
	public Integer getCloudCount()
	{
		return network.getRow(network, Constants.NAMESPACE).get(Constants.CLOUD_COUNTER, Integer.class);
	}
	
	public String getCloudName()
	{
		return CLOUDNAME;
	}
	
	public String getSeparater()
	{
		return SEPARATER;
	}
	
	public WordFilter getFilter()
	{
		return filter;
	}
	
	public void setFilter(WordFilter aFilter)
	{
		filter = aFilter;
	}
	
	public WordDelimiters getDelimiter()
	{
		return delimiters;
	}
	
	public void setDelimiter(WordDelimiters aDelimiter)
	{
		delimiters = aDelimiter;
	}
	
	public boolean getIsStemming()
	{
		return network.getRow(network, Constants.NAMESPACE).get(Constants.USE_STEMMING, Boolean.class);
	}
	
	public void setIsStemming(boolean val)
	{
		network.getRow(network, Constants.NAMESPACE).set(Constants.USE_STEMMING, val);
	}
	
	public CyNetwork getNetwork() {
		return network;
	}
}
