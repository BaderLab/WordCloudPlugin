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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
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

public class SemanticSummaryPlugin implements SessionAboutToBeSavedListener, SessionLoadedListener, SessionAboutToBeLoadedListener 
{
	
	//Variables
	private static final String netNameSep = "SemanticSummaryNetworkSeparator";
	private static final String cloudNameSep = "SemanticSummaryCloudSeparator";
	
	private final Logger logger = LoggerFactory.getLogger(SemanticSummaryPlugin.class);
	
	private SemanticSummaryPluginAction pluginAction;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private ModelManager modelManager;
	private IoUtil ioUtil;
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	
	//CONSTRUCTORS
	
	/**
	 * SemanticSummaryPlugin Constructor
	 * @param pluginAction 
	 * @param parametersFactory 
	 * @param modelManager 
	 * @param ioUtil 
	 * @param applicationManager 
	 * @param application 
	 */
	
	public SemanticSummaryPlugin(SemanticSummaryPluginAction pluginAction, SemanticSummaryManager cloudManager, SemanticSummaryParametersFactory parametersFactory, ModelManager modelManager, IoUtil ioUtil, CyApplicationManager applicationManager, CySwingApplication application)
	{
		this.pluginAction = pluginAction;
		this.cloudManager = cloudManager;
		this.parametersFactory = parametersFactory;
		this.modelManager = modelManager;
		this.ioUtil = ioUtil;
		this.applicationManager = applicationManager;
		this.application = application;
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
	public void handleEvent(SessionAboutToBeLoadedEvent event) {
		cloudManager.reset();
		cloudManager.setupCurrentNetwork(null);
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
		cloudManager.setupCurrentNetwork(applicationManager.getCurrentNetwork());
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
		Map<CyNetwork, SemanticSummaryParameters> networks = cloudManager.getCyNetworkList();
		
		//Create a props file for each network
		for (Entry<CyNetwork, SemanticSummaryParameters> entry : networks.entrySet())
		{
			SemanticSummaryParameters params = entry.getValue();
			
			//Update the network if it has changed
			CyNetwork network = entry.getKey();
			if (params.networkHasChanged(network));
				params.updateParameters(network);
			
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
				delimiterWriter.write(params.getDelimiter().toString());
				delimiterWriter.close();
				pFileList.add(current_delimiter);
				
				//Loop on Clouds
				if (!params.getClouds().isEmpty())
				{
					Map<String, CloudParameters> all_clouds = params.getClouds();
					
					for (Iterator<String> j=all_clouds.keySet().iterator(); j.hasNext();)
					{
						String cloud_name = j.next().toString();
						
						CloudParameters cloud = all_clouds.get(cloud_name);
						
						//File for CloudParameters
						File current_cloud = new File(tmpDir, netNameSep + uid + netNameSep + 
								cloudNameSep + cloud_name + cloudNameSep + ".CLOUDS.txt");
						BufferedWriter subCloud1Writer = new BufferedWriter(new FileWriter(current_cloud));
						subCloud1Writer.write(cloud.toString());
						subCloud1Writer.close();
						pFileList.add(current_cloud);
					}//end iteration over clouds
				}//end if clouds exist for network
			}//end try
			catch (Exception ex)
			{
				ex.printStackTrace();
			}//end catch
		}//end network iterator
	}//end save session method
	
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
		
		//Initialize and load panels
		pluginAction.loadPanels();
		
		try
		{
			for (CyNetwork network : modelManager.getSemanticSummaryNetworks()) {
				SemanticSummaryParameters params = parametersFactory.createSemanticSummaryParameters(network);
				cloudManager.registerNetwork(network, params);
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
					String[] fullname2 = prop_file.getName().split(cloudNameSep);
					String cloud_name = fullname2[1];
					
					//Get the Network Parameters
					CyNetwork network = modelManager.getNetwork(uid);
					SemanticSummaryParameters networkParams = cloudManager.getCyNetworkList().get(network);
					
					//Given the file with all the parameters, create a new parameters
					CloudParameters params = new CloudParameters(networkParams, fullText);
					networkParams.addCloud(cloud_name, params);
					
				}//end if .CLOUDS.txt file
				
				if (prop_file.getName().contains(".FILTER.txt"))
				{
					String fullText = ioUtil.readAll(prop_file.getAbsolutePath());
					
					//Get the networkID from the props file
					String[] fullname = prop_file.getName().split(netNameSep);
					String net_name = fullname[1];
					int uid = Integer.parseInt(net_name);
					
					//Get the Network Parameters
					CyNetwork network = modelManager.getNetwork(uid);
					SemanticSummaryParameters networkParams = cloudManager.getCyNetworkList().get(network);
					
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
					CyNetwork network = modelManager.getNetwork(uid);
					SemanticSummaryParameters networkParams = cloudManager.getCyNetworkList().get(network);
					
					//Recreate the Delimiter and set pointer in cloud
					WordDelimiters curDelimiter = new WordDelimiters(application, fullText);
					networkParams.setDelimiter(curDelimiter);
				}
			}//end loop through all props files
			
			//Set current network and Initialize the panel appropriately
			cloudManager.setupCurrentNetwork(applicationManager.getCurrentNetwork());

			for (SemanticSummaryParameters parameters: cloudManager.getCyNetworkList().values()) {
				for (CloudParameters cloud : parameters.getClouds().values()) {
					cloud.updateSelectedCounts();
				}
			}
		}//end try
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}//end restore session method
	
    /**
     * 
     * @param propFileName
     * @return
     * @throws IOException
     */
	private Properties getPropertiesFromClasspath(String propFileName) throws IOException {
        // loading properties file from the classpath
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream == null) {
            throw new FileNotFoundException("property file '" + propFileName
                    + "' not found in the classpath");
        }

        props.load(inputStream);
        return props;
    }
	
}
