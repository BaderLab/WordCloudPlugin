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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private List<WordPair> queue;
	private CloudInfo cloudInfo;
	
	
	/**
	 * Creates a fresh instance of the priority queue.
	 */
	public ClusterPriorityQueue(CloudInfo cloudInfo)
	{
		this.queue = new LinkedList<WordPair>();
		this.cloudInfo = cloudInfo;
		initialize();
	}
	
	/**
	 * Initializes the priority queue for the specified cloud parameters
	 * @param CloudParameters that this queue is for.
	 */
	private void initialize()
	{
		
		Map<WordPair, Integer> selectedPairCounts = cloudInfo.getSelectedPairCounts();
		queue = new ArrayList<WordPair>(selectedPairCounts.size());
//		queue = new LinkedList<WordPair>();
		
		
		for (Entry<WordPair, Integer> entry : selectedPairCounts.entrySet())
		{
			WordPair curPair = entry.getKey();
			curPair.calculateProbability(entry.getValue());
			queue.add(curPair);
		}
		
		//Sort the Priority Queue so items with the largest probability are first
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
		if(queue.isEmpty())
			return null;
		
		WordPair removedPair = queue.remove(0);
		
		//Remove all other entries from queue necessary
		String firstWord = removedPair.getFirstWord();
		String secondWord = removedPair.getSecondWord();
		
		int h1 = firstWord.hashCode();
		int h2 = secondWord.hashCode();
		
		//Create list to remove
		for(Iterator<WordPair> iter = queue.iterator(); iter.hasNext();)
		{
			WordPair curPair = iter.next();
			String curFirst = curPair.getFirstWord();
			String curSecond = curPair.getSecondWord();
			
//			//Remove all pairs with words in the same position as the removed
//			//and the inverse of the removed
//			if (firstWord.equals(curFirst) || secondWord.equals(curSecond) || (firstWord.equals(curSecond) && secondWord.equals(curFirst)))
//				iter.remove();
			
			// believe it or not comparing hashcodes first actually speeds this up a lot
			int c1 = curFirst.hashCode();
			int c2 = curSecond.hashCode();
			if(h1 == c1 || h2 == c2 || h1 == c2 || h2 == c1) {
				if (firstWord.equals(curFirst) || secondWord.equals(curSecond) || (firstWord.equals(curSecond) && secondWord.equals(curFirst))) {
					iter.remove();
				}
			}
			
			
		}
		return removedPair;	
	}
	

	public boolean isEmpty()
	{
		return queue.isEmpty();
	}
	
	
}
