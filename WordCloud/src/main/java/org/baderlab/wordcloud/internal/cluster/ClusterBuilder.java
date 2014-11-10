/*
 File: SemanticSummaryClusterBuilder.java

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

package org.baderlab.wordcloud.internal.cluster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The SemanticSummaryClusterBuilder class contains the methods and
 * variables necessary for clustering the data contained in a 
 * CloudParameter object using a greedy hierarchical style algorithm.
 * @author Layla Oesper
 * @version 1.0
 */

public class ClusterBuilder 
{
	private final CloudInfo cloudInfo;
	private final ClusterPriorityQueue queue;
	private final WordClusters clusters;
	private List<CloudWordInfo> cloudWords;
	
	
	/** 
	 * Default constructor to create a fresh instance.  
	 */
	public ClusterBuilder(CloudInfo cloudInfo)
	{
		this.cloudInfo = cloudInfo;
		this.queue = new ClusterPriorityQueue(cloudInfo);
		this.clusters = new WordClusters(cloudInfo);
		
		this.cloudWords = new ArrayList<CloudWordInfo>();
	}
	
	/**
	 * Clusters the data from the CloudParameters stored in the variables
	 * using the input cutoff if this object has been initialized.
	 * @param Double - the cutoff value to use for clustering.
	 */
	public void clusterData(double cutoffVal)
	{
		boolean isDone = false; //flag for when we pass the cutoff value
		
		
		while (!isDone && !queue.isEmpty())
		{
			WordPair curPair = queue.peak();
			
			//Check cutoff
			if (curPair.getProbability() < cutoffVal)
			{
				isDone = true;
				continue;
			}
			
			curPair = queue.remove();
			
			clusters.combineClusters(curPair);
			
		}//end while
		
		//Sort Clusters
		clusters.orderClusters();
	}
	
	/**
	 * Maps a cluster number (based on its index in clusters) to the color that will
	 * be used to display that cluster in the Semantic Summary.
	 * @assumes that the cluster number provided is a valid number.
	 * @param cluster number
	 */
	private Color getClusterColor(int clusterNum)
	{
		switch(clusterNum % 7) {
			default:
			case 0:  return Color.BLACK;
	    	case 1:  return new Color(204,0,0);
	    	case 2:  return new Color(0,110,0);
	    	case 3:  return new Color(255,179,0);
	    	case 4:  return new Color(0,0,160);
	    	case 5:  return new Color(130, 32, 130);
	    	case 6:  return Color.GRAY;
		}
	}
	
	/**
	 * Builds an array of CloudWords based on the current state of clustering
	 * for this CloudParameters.
	 */
	public void buildCloudWords()
	{
		cloudWords = new ArrayList<CloudWordInfo>();
		
		Integer wordCount = 0;
		
		Map<String, Double> ratios = cloudInfo.getRatios();
		for(int i = 0; i < clusters.getClusters().size(); i++)
		{
			SingleWordCluster curCluster = clusters.getClusters().get(i);
			List<String> curList = curCluster.getWordList();
			Color clusterColor = getClusterColor(i);
			
			//Iterate through the words
			for (int j = 0; j < curList.size(); j++)
			{
				String curWord = curList.get(j);
				Integer fontSize = cloudInfo.calculateFontSize(curWord, ratios.get(curWord));
				CloudWordInfo curInfo = new CloudWordInfo(cloudInfo, curWord, fontSize, clusterColor, i, wordCount);
				wordCount++;
				cloudWords.add(curInfo);
			}
		}
	}
	
	
	
	public List<CloudWordInfo> getCloudWords()
	{
		return cloudWords;
	}
	
	public WordClusters getClusters()
	{
		return clusters;
	}
	
	public ClusterPriorityQueue getQueue()
	{
		return queue;
	}
	
}
