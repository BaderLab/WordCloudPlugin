/*
 File: SingleWordCluster.java

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
import java.util.Iterator;
import java.util.List;

import org.baderlab.wordcloud.internal.model.next.CloudParameters;

/**
 * The SingleWordCluster class contains information about a single set of 
 * clustered words for a CloudParameters object.  These objects can be 
 * sorted / compared based on the total size of the fonts that would
 * be used to represent them in a CloudParameters.
 * @author Layla Oesper
 * @version 1.0
 */

public class SingleWordCluster implements Comparable<SingleWordCluster>
{
	
	private List<String> wordList = new ArrayList<String>();
	private int totalSum = 0;
	private int numItems = 0;
	
	private CloudParameters params;
	
	
	public SingleWordCluster(CloudParameters cloudParams) {
		this.params = cloudParams;
	}
	
	/**
	 * Adds an element to the WordList and updates the totalSum.
	 * @param String - word to add to the SingleWordCluster
	 */
	public void add(String aWord)
	{
		double ratio = params.getRatios().get(aWord); 
		int fontSize = params.calculateFontSize(aWord, ratio);
		totalSum = totalSum + fontSize;
		numItems = numItems + 1;
		wordList.add(aWord);
	}
	
	/**
	 * Removes a word from the WordList and updates the totalSum.
	 * @param String - word to remove from the SingleWordCluster
	 * @return String - word that was removed from the list
	 */
	public String remove(String aWord)
	{
		if (!wordList.contains(aWord))
			return null;
		
		double ratio = params.getRatios().get(aWord); 
		int fontSize = params.calculateFontSize(aWord, ratio);
		totalSum = totalSum - fontSize;
		numItems = numItems - 1;
		wordList.remove(aWord);
		
		return aWord;
	}
	
	/**
	 * Computes the value of sum/sqrt(N) for this SingleWordCluster.
	 * @return Double - the value of sum/sqrt(N)
	 */
	public double computeRootMean()
	{
		//Return 0 if sum or num items is 0
		if (totalSum == 0 || numItems == 0)
		{
			return 0.0;
		}
		else
		{
			return totalSum/Math.pow(numItems,0.5);
		}
		
	}
	
	/**
	 * Calculates the largest value for font size in cluster.
	 */
	public int getLargestFont()
	{
		int largest = 0;
		for (Iterator<String> iter = wordList.iterator(); iter.hasNext();)
		{
			String curWord = iter.next();
			double ratio = params.getRatios().get(curWord); 
			int curSize = params.calculateFontSize(curWord, ratio);
			if (largest < curSize)
			{
				largest = curSize;
			}
		}
		return largest;
	}
	
	/**
	 * Calculates a weighted sum for all words.  The sum is the square root
	 * of the sum of squares of the font size of all words in the cluster.
	 * @return the weighted sum of all words in the cluster
	 */
	public double calculateWeightedSum()
	{
		double sum = 0.0;
		double k = 2.0;
		for (Iterator<String> iter = wordList.iterator(); iter.hasNext();)
		{
			String curWord = iter.next();
			double ratio = params.getRatios().get(curWord); 
			int curSize = params.calculateFontSize(curWord, ratio);
			
			sum = sum + Math.pow(curSize, k);
		}
		//Take kth Root
		sum = Math.pow(sum, 1/k);
		
		return sum;
	}
	

	//Weighted sum
	public int compareTo(SingleWordCluster o) 
	{
		//Sort first based on weighted sum
		double thisCount = this.calculateWeightedSum();
		double compareCount = o.calculateWeightedSum();
		
		if (thisCount < compareCount)
			{return -1;}
		else if (thisCount > compareCount)
			{return 1;}
		else
		{
			//In case of ties, break alphabetically by first word
			String thisWord = this.getWordList().get(0);
			String compareWord = this.getWordList().get(0);
			
			return thisWord.compareTo(compareWord);
		}
	}
	
	//Getters and Setters
	
	public List<String> getWordList()
	{
		return wordList;
	}
	
	public int getTotalSum()
	{
		return totalSum;
	}
	
	public int getNumItems()
	{
		return numItems;
	}

}
