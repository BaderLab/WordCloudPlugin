/*
 File: SemanticSummaryPlugin.java

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.model.WordDelimiters;
import org.baderlab.wordcloud.internal.model.WordFilter;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the Semantic Summary Plugin.
 * It allows a user to create a tag cloud which displays semantic 
 * information from the selected nodes in a given network.
 * 
 * @author Layla Oesper
 * @version 1.0
 */

public class SessionListener implements SessionAboutToBeSavedListener, SessionLoadedListener 
{
	
	//Variables
	private static final String netNameSep = "SemanticSummaryNetworkSeparator";
	private static final String cloudNameSep = "SemanticSummaryCloudSeparator";
	
	private final Logger logger = LoggerFactory.getLogger(SessionListener.class);
	
	private final CloudModelManager cloudManager;
	private final IoUtil ioUtil;
	private final CyNetworkManager networkManager;
	
	private final CyApplicationManager applicationManager;
	private final UIManager uiManager;

	
	public SessionListener(
			CloudModelManager cloudManager, 
			IoUtil ioUtil, 
			CyNetworkManager networkManager,
			CyApplicationManager applicationManager,
			UIManager uiManager)
	{
		this.cloudManager = cloudManager;
		this.ioUtil = ioUtil;
		this.networkManager = networkManager;
		this.applicationManager = applicationManager;
		this.uiManager = uiManager;
	}
	

	
	//METHODS
	
	/**
	 * Provides a description of the SemanticSummaryPlugin
	 * @return String that describes the SemanticSummaryPlugin
	 */
	public String describe()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("For every node in the current network, this plugin ");
		sb.append("displays a word cloud of the selected ");
		sb.append("cyNode attribute.  The node ID is the defuault ");
		sb.append("attribute.");
		return sb.toString();
	}
	
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		List<File> files = new ArrayList<File>();
		saveSessionStateFiles(files);
		try {
			event.addAppFiles(Constants.NAMESPACE, files);
		} catch (Exception e) {
			logger.error("Unexpected error while loading WordCloud app state", e);
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		List<File> files = event.getLoadedSession().getAppFileListMap().get(Constants.NAMESPACE);
		if (files == null) {
			return;
		}
		restoreSessionState(files);
	}
	
	/**
	 * SaveSessionStateFiles collects all the data stored in the Semantic 
	 * Summary data structures and creates property files for each network 
	 * listing the variables needed to rebuild the Semantic Summary.
	 * 
	 * @param pFileList - pointer to the set of files to be added to the session
	 */
	public void saveSessionStateFiles(List<File> pFileList)
	{
		//Create an empty file on system temp directory
		String tmpDir = System.getProperty("java.io.tmpdir");
		
		//get the networks
		Collection<NetworkParameters> networks = cloudManager.getNetworks();
		
		//Create a props file for each network
		for (NetworkParameters params : networks)
		{
			//Update the network if it has changed
			CyNetwork network = params.getNetwork();
			
			//write out files.
			try 
			{
				int uid = network.getRow(network).get(Constants.NETWORK_UID, Integer.class);
				File current_filter = new File(tmpDir, netNameSep + uid + netNameSep + ".FILTER.txt");
				BufferedWriter filterWriter = new BufferedWriter(new FileWriter(current_filter));
				filterWriter.write(params.getFilter().toString());
				filterWriter.close();
				pFileList.add(current_filter);
				
				File current_delimiter = new File(tmpDir, netNameSep + uid + netNameSep + ".DELIMITER.txt");
				BufferedWriter delimiterWriter = new BufferedWriter(new FileWriter(current_delimiter));
				delimiterWriter.write(params.getDelimeters().toString());
				delimiterWriter.close();
				pFileList.add(current_delimiter);
				
				//Loop on Clouds
				for(CloudParameters cloud : params.getClouds()) {
					String cloud_name = cloud.getCloudName();
					
					//File for CloudParameters
					File current_cloud = new File(tmpDir, netNameSep + uid + netNameSep + 
							cloudNameSep + cloud_name + cloudNameSep + ".CLOUDS.txt");
					BufferedWriter subCloud1Writer = new BufferedWriter(new FileWriter(current_cloud));
					subCloud1Writer.write(cloud.toString());
					subCloud1Writer.close();
					pFileList.add(current_cloud);
				}
				
			}//end try
			catch (Exception ex)
			{
				ex.printStackTrace();
			}//end catch
		}//end network iterator
	}//end save session method
	
	
	private Set<CyNetwork> getSemanticSummaryNetworks() {
		Set<CyNetwork> networks = new HashSet<CyNetwork>();
		for (CyNetwork network : networkManager.getNetworkSet()) {
			if (CloudModelManager.hasCloudMetadata(network)) {
				networks.add(network);
			}
		}
		return networks;
	}
	
	private CyNetwork getNetwork(int uid) {
		for (CyNetwork network : networkManager.getNetworkSet()) {
			CyRow row = network.getRow(network);
			if (row == null) {
				continue;
			}
			Integer other = row.get(Constants.NETWORK_UID, Integer.class);
			if (other == null) {
				continue;
			}
			if (uid == other) {
				return network;
			}
		}
		return null;
	}
	
	/**
	 * Restore Semantic Summaries
	 * 
	 * @param pStateFileList - list of files associated with the session
	 */

	public void restoreSessionState(List<File> pStateFileList)
	{
		
		if ((pStateFileList == null) || (pStateFileList.size() == 0))
		{
			return; //no previous state to restore
		}
		
		
		try
		{
			for (CyNetwork network : getSemanticSummaryNetworks()) {
				cloudManager.addNetwork(network);
			}
			
			//Go through the prop files to create the clouds and set filters
			for (int i = 0; i < pStateFileList.size(); i++)
			{
				File prop_file = pStateFileList.get(i);
				
				if (prop_file.getName().contains(".CLOUDS.txt"))
				{
					String fullText = ioUtil.readAll(prop_file.getAbsolutePath());
					
					//Get the networkID from the props file
					String[] fullname = prop_file.getName().split(netNameSep);
					String net_name = fullname[1];
					int uid = Integer.parseInt(net_name);
					
					//Get the cloudID from the props file
					//String[] fullname2 = prop_file.getName().split(cloudNameSep);
//					String cloud_name = fullname2[1]; // cloud name is in properties file
					
					//Get the Network Parameters
					CyNetwork network = getNetwork(uid);
					NetworkParameters networkParams = cloudManager.getNetworkParameters(network);
					
					networkParams.createCloudFromProperties(fullText);
					
				}//end if .CLOUDS.txt file
				
				if (prop_file.getName().contains(".FILTER.txt"))
				{
					String fullText = ioUtil.readAll(prop_file.getAbsolutePath());
					
					//Get the networkID from the props file
					String[] fullname = prop_file.getName().split(netNameSep);
					String net_name = fullname[1];
					int uid = Integer.parseInt(net_name);
					
					//Get the Network Parameters
					CyNetwork network = getNetwork(uid);
					NetworkParameters networkParams = cloudManager.getNetworkParameters(network);
					
					
					//Recreate the Filter and set pointer in cloud
					WordFilter curFilter = new WordFilter(fullText);
					networkParams.setFilter(curFilter);
				}
				
				if (prop_file.getName().contains(".DELIMITER.txt"))
				{
					String fullText = ioUtil.readAll(prop_file.getAbsolutePath());
					
					//Get the networkID from the props file
					String[] fullname = prop_file.getName().split(netNameSep);
					String net_name = fullname[1];
					int uid = Integer.parseInt(net_name);
					
					//Get the Network Parameters
					CyNetwork network = getNetwork(uid);
					NetworkParameters networkParams = cloudManager.getNetworkParameters(network);
					
					//Recreate the Delimiter and set pointer in cloud
					WordDelimiters curDelimiter = new WordDelimiters(fullText);
					networkParams.setDelimeters(curDelimiter);
				}
			}//end loop through all props files
			
//			//Set current network and Initialize the panel appropriately
//			for (NetworkParameters parameters: cloudManager.getNetworks()) {
//				for (CloudParameters cloud : parameters.getClouds()) {
//					cloud.updateSelectedCounts();
//				}
//			}
		}//end try
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// show the panels once the session has finished loading
		//uiManager.setCurrentCloud(applicationManager.getCurrentNetwork());
	}//end restore session method
	
}
