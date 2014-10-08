/*
 File: ClusterPriorityQueue.java

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import org.baderlab.wordcloud.internal.model.CloudParameters;

/**
 * The ClusterPriorityQueue builds a priority queue of WordPairs from
 * a specified CloudParameters object.  This is a max priority queue based
 * on the probability of each word pair, with ties being broken using the the
 * selected/network ratio of each pair, and then ties are broken again 
 * alphabetically.  This priority queue is used to hierarchically cluster
 * the words appearing in a cloud.
 * @author Layla Oesper
 * @version 1.0
 */

public class ClusterPriorityQueue 
{
	private ArrayList<WordPair> queue;
	private CloudParameters params;
	
	
	/**
	 * Creates a fresh instance of the priority queue.
	 */
	public ClusterPriorityQueue(CloudParameters params)
	{
		this.queue = new ArrayList<WordPair>();
		this.params = params;
		initialize();
	}
	
	/**
	 * Initializes the priority queue for the specified cloud parameters
	 * @param CloudParameters that this queue is for.
	 */
	private void initialize()
	{
		queue = new ArrayList<WordPair>();
		
		//Initialize params if necessary
		if (!params.getRatiosInitialized())
			params.updateRatios();
		
		for (Entry<WordPair, Integer> entry : params.getSelectedPairCounts().entrySet())
		{
			WordPair curPair = entry.getKey();
			curPair.calculateProbability(entry.getValue());
			queue.add(curPair);
		}
		
		//Sort the Priority Queue so items with the largest probability are first
		// MKTODO replace with a reverse sort
		Collections.sort(queue);
		Collections.reverse(queue);
	}
	
	/**
	 * Returns the WordPair located at the top of the queue, without removing it.
	 * @return WordPair with the highest probability remaining in the queue. If the 
	 * queue is empty null, is returned.
	 */
	public WordPair peak()
	{
		if (!queue.isEmpty())
			return queue.get(0);
		else
			return null;
	}
	
	/**
	 * Returns the WordPair located at the top of the queue and removes it along
	 * with all other entries in the queue that are now obsolete.
	 * @return WordPair with the highest probability remaining in the queue.  If the 
	 * queue is empty, null is returned.
	 */
	public WordPair remove()
	{
		WordPair removedPair;
		if (!queue.isEmpty())
			removedPair = queue.remove(0);
		else
			removedPair = null;
		
		//Remove all other entries from queue necessary
		if (removedPair != null)
		{
			String firstWord = removedPair.getFirstWord();
			String secondWord = removedPair.getSecondWord();
			
			//Create list to remove
			for(Iterator<WordPair> iter = queue.iterator(); iter.hasNext();)
			{
				WordPair curPair = iter.next();
				String curFirst = curPair.getFirstWord();
				String curSecond = curPair.getSecondWord();
				
				//Remove all pairs with words in the same position as the removed
				//and the inverse of the removed
				if (firstWord.equals(curFirst) || secondWord.equals(curSecond) ||
						(firstWord.equals(curSecond) && secondWord.equals(curFirst)))
					iter.remove();
			}
		}
		return removedPair;	
	}
	
	/**
	 * Returns the size of the queue.
	 * @return int size of the queue
	 */
	public int size()
	{
		return queue.size();
	}
	
	/**
	 * Returns true if the queue is empty.
	 * @return boolean - indicating whether or not the queue is emtpy.
	 */
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}
	
	
	//Getters and Setters
	public ArrayList<WordPair> getQueue()
	{
		return queue;
	}
	
	public void setQueue(ArrayList<WordPair> aQueue)
	{
		queue = aQueue;
	}
	
	public CloudParameters getCloudParameters()
	{
		return params;
	}
	
	public void setCloudParameters(CloudParameters cloudParams)
	{
		params = cloudParams;
	}
	
}
