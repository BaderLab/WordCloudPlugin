/*
 File: WordPair.java

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

import java.util.Map;

import org.baderlab.wordcloud.internal.model.next.CloudParameters;

/**
 * A WordPair object contains information about adjacent pairs of words that
 * appear in a selected node.
 * @author Layla Oesper
 * @version 1.0
 */

public class WordPair implements Comparable<WordPair>
{
	//VARIABLES
	private String firstWord;
	private String secondWord;
	private Double probability;
	private CloudParameters params;
	
	//CONSTRUCTOR
	/**
	 * Creates a fresh instance of a WordPair object for the specified words
	 * and CloudParameters.
	 */
	public WordPair(String aWord, String nextWord, CloudParameters cloudParams)
	{
		firstWord = aWord;
		secondWord = nextWord;
		params = cloudParams;
		probability = 0.0;
	}
	
	//METHODS
	
	/**
	 * Calculates the probability for the given WordPair.
	 */
	public void calculateProbability(int pairCount)
	{
		/**
		 * For two words, A and B, we are calculating the following:
		 * (P(B|A)P(A))/(P(A)P(B)).  To simplify this statement in terms of counts
		 * we have: ((#(A,B)/#A)(#A/#Total))/((#A/#Total)(#B/#Total))
		 * Mathematically we can simplify this to be the following expression, 
		 * which is what we actually calculate:
		 * (#(A,B)* #Total)/ (#A * #B)
		 */
		// Getting the total number of words in the selection
		Map<String, Integer> selectedCounts = params.getSelectedCounts();
		Integer total = 0;
		for (String word : selectedCounts.keySet()) {
			total += selectedCounts.get(word);
		}
		Integer firstCount = selectedCounts.get(firstWord);
		Integer secondCount = selectedCounts.get(secondWord);
		
		Integer numerator = pairCount * total;
		Double doubleNumerator = numerator.doubleValue();
		Integer denominator = firstCount * secondCount;
		Double doubleDenom = denominator.doubleValue();
		
		probability = doubleNumerator/doubleDenom;
	}
	
	public int compareTo(WordPair second) 
	{
		Double firstProb = this.probability;
		Double secondProb = second.probability;
		
		if (firstProb < secondProb)
			return -1;
		else if (firstProb > secondProb)
			return 1;
		else //They are the same - so now compare with ratios
		{
			//Assumes that Ratios have been calculated
			Double firstRatio = this.getCloudParameters().getPairRatios().get(this);
			Double secondRatio = second.getCloudParameters().getPairRatios().get(second);
			
			if (firstRatio < secondRatio)
				return -1;
			else if (firstRatio > secondRatio)
				return 1;
			else
				//Third level of tie break - alphabetical of words
			{
				int result = firstWord.compareTo(second.firstWord);
				if (result != 0) {
					return result;
				}
				return secondWord.compareTo(second.secondWord);
			}
		}//end probability else
	}//end compareTo
	
	//Getters and Setters
	public void setFirstWord(String aWord)
	{
		firstWord = aWord;
	}
	
	public String getFirstWord()
	{
		return firstWord;
	}
	
	public void setSecondWord(String aWord)
	{
		secondWord = aWord;
	}
	
	public String getSecondWord()
	{
		return secondWord;
	}
	
	public void setCloudParameters(CloudParameters aParam)
	{
		params = aParam;
	}
	
	public CloudParameters getCloudParameters()
	{
		return params;
	}
	
	public Double getProbability()
	{
		return probability;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof WordPair)) {
			return false;
		}
		WordPair other = (WordPair) object;
		return firstWord.equals(other.firstWord) && secondWord.equals(other.secondWord);
	}
	
	@Override
	public int hashCode() {
		return firstWord.hashCode() * 31 + secondWord.hashCode();
	}
}
