/*
 File: CloudWordInfo.java

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
import java.awt.Font;

import javax.swing.JLabel;

import org.baderlab.wordcloud.internal.model.CloudParameters;

/**
 * The CloudWordInfo class defines information pertaining to a particular
 * word in a Cloud.  In particular this class defines information that 
 * relates to how that word will be displayed in the cloud.
 * @author Layla Oesper
 * @version 1.0
 */

public class CloudWordInfo implements Comparable<CloudWordInfo>
{
	private final String word;
	private final int fontSize;
	private final CloudParameters params;
	private final Color textColor;
	private final int cluster;
	private final int wordNum;
	
	//String Delimeters
	private static final String FIRSTDELIMITER = "TabbedEquivalent";
	private static final String SECONDDELIMITER = "NewLineEquivalent";
	
	
	
	
	public CloudWordInfo(CloudParameters params, String word, int fontSize, Color textColor, int cluster, int wordNum) {
		this.params = params;
		this.word = word;
		this.fontSize = fontSize;
		this.textColor = textColor;
		this.cluster = cluster;
		this.wordNum = wordNum;
	}
	
	public CloudWordInfo(CloudParameters params, String word, int fontSize) {
		this(params, word, fontSize, null, 0, 0);
	}

	
	/**
	 * Compares two CloudWordInfo objects based on their fontSize.  Then, based
	 * on cluster number, then based on wordNum, and then alphabetically.
	 * @param CloudWordInfo - object to compare
	 * @return true if 
	 */
	public int compareTo(CloudWordInfo c)
	{
		Integer first = this.getFontSize();
		Integer second = c.getFontSize();
		
		//switch order since we want to sort biggest to smallest
		int result = second.compareTo(first);
		
		if (result == 0)
		{
			first = this.getCluster();
			second = c.getCluster();
			result = first.compareTo(second);
			
			if (result == 0)
			{
				first = this.getWordNumber();
				second = c.getWordNumber();
				result = first.compareTo(second);
				
				if (result == 0)
				{
					String firstString = this.getWord();
					String secondString = c.getWord();
					result = firstString.compareTo(secondString);
				}//end string compare
			}//end word number compare
		}//end cluster compare
		
		return result;
	}
	
	/**
	 * Returns a JLabel that can be used to display this word in a cloud.
	 * @return JLabel - for display in Cloud.
	 */
	public JLabel createCloudLabel()
	{
		JLabel label = new JLabel(word);
		label.setFont(new Font("sansserif",Font.BOLD, fontSize));
		label.setForeground(textColor);
		return label;
	}
	
	/**
	 * String representation of CloudWordInfo.
	 * It is used to store the persistent attributes when a session is saved.
	 * @return - String representation of this object
	 */
	public String toString()
	{
		StringBuffer paramVariables = new StringBuffer();
		
		paramVariables.append("Word" + FIRSTDELIMITER + word + SECONDDELIMITER);
		paramVariables.append("FontSize" + FIRSTDELIMITER + fontSize + SECONDDELIMITER);
		paramVariables.append("Cluster" + FIRSTDELIMITER + cluster + SECONDDELIMITER);
		paramVariables.append("WordNum" + FIRSTDELIMITER + wordNum + SECONDDELIMITER);
		paramVariables.append("TextColor" + FIRSTDELIMITER + textColor.getRGB() + SECONDDELIMITER);
		
		return paramVariables.toString();
	}
	
	public String[] toSplitString()
	{
		return new String[] {word, Integer.toString(fontSize), Integer.toString(cluster), Integer.toString(wordNum)};
	}
	
	//Getters and Setters
	public String getWord()
	{
		return word;
	}
	
	public int getFontSize()
	{
		return fontSize;
	}
	
	public CloudParameters getCloudParameters()
	{
		return params;
	}
	
	public Color getTextColor()
	{
		return textColor;
	}
	
	public int getCluster()
	{
		return cluster;
	}
	
	public int getWordNumber()
	{
		return wordNum;
	}
	
}
