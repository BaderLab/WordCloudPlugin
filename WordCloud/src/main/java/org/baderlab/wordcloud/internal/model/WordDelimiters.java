/*
 File: WordDelimiters.java

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * This class defines the WordDelimiters class.  This class is used to determine
 * if a delimiter in question should be used to tokenize text.
 * 
 * @author Layla Oesper
 * @version 1.0
 */

public class WordDelimiters 
{
	
	private static final String DELIMITER = "SAVEDELIMITER";
	private static final String FIRSTDELIMITER = "NewLineEquivalent";
	private static final String SECONDDELIMITER = "TabbedEquivalent";
	
	
	private Pattern splitter = null; // lazily initialized
	
	private TreeSet<String> delimsInUse= new TreeSet<String>();
	private TreeSet<String> delimsToAdd= new TreeSet<String>();
	private TreeSet<String> userDelims= new TreeSet<String>();
	
	private final HashMap<String, String> regexTranslation = new HashMap<String,String>();
	{
		regexTranslation.put("tab", "\\t");
		regexTranslation.put("space", " ");
		regexTranslation.put("newline", "\\n");
		regexTranslation.put("carriage return", "\\r");
		regexTranslation.put("form feed", "\\f");
	}
	
	
	/**
	 * Creates the default WordDelimiters object.
	 */
	public WordDelimiters() {
		delimsInUse.add("tab");
		delimsInUse.add("space");
		delimsInUse.add("newline");
		delimsInUse.add("carriage return");
		delimsInUse.add("form feed");
		delimsInUse.add("!");
		delimsInUse.add("\"");
		delimsInUse.add("#");
		delimsInUse.add("$");
		delimsInUse.add("%");
		delimsInUse.add("&");
		delimsInUse.add("(");
		delimsInUse.add(")");
		delimsInUse.add("*");
		delimsInUse.add("+");
		delimsInUse.add(",");
		delimsInUse.add(".");
		delimsInUse.add("/");
		delimsInUse.add(":");
		delimsInUse.add(";");
		delimsInUse.add("<");
		delimsInUse.add("=");
		delimsInUse.add(">");
		delimsInUse.add("?");
		delimsInUse.add("@");
		delimsInUse.add("[");
		delimsInUse.add("\\");
		delimsInUse.add("]");
		delimsInUse.add("^");
		delimsInUse.add("_");
		delimsInUse.add("`");
		delimsInUse.add("{");
		delimsInUse.add("|");
		delimsInUse.add("}");
		delimsInUse.add("~");
		
		delimsToAdd.add("-");
		delimsToAdd.add("'");
	}
	
	
	/**
	 * Constructor to create WordDelimiters from a cytoscape property file
	 * while restoring a session.  Property file is created when the session is saved.
	 * @param propFile - the contents of the property file as a String
	 */
	public WordDelimiters(String propFile)
	{
		//Create a hashmap to contain all the values in the rpt file
		HashMap<String, String> props = new HashMap<String,String>();
		
		String[] lines = propFile.split(FIRSTDELIMITER);
		
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			String[] tokens = line.split(SECONDDELIMITER);
			//there should be two values in each line
			if(tokens.length == 2)
				props.put(tokens[0],tokens[1]);
		}
		
		//Check if this is new version, clear out initialization
		if ((props.get("DelimsInUse") != null) || (props.get("DelimsToAdd") != null))
		{
			delimsInUse = new TreeSet<String>();
			delimsToAdd = new TreeSet<String>();
		}
		
		//Rebuild inUse List
		String value = props.get("DelimsInUse");
		if (value != null)
		{
			String[] delims = value.split(DELIMITER);
			for (int i = 0; i < delims.length; i++)
			{
				String curDelim = delims[i];
				delimsInUse.add(curDelim);
			}
		}
		
		//Rebuild toAdd List
		value = props.get("DelimsToAdd");
		if (value != null)
		{
			String[] delims = value.split(DELIMITER);
			for (int i = 0; i < delims.length; i++)
			{
				String curDelim = delims[i];
				delimsToAdd.add(curDelim);
			}
		}
		
		
		//Rebuild added List
		value = props.get("AddedDelims");
		if (value != null)
		{
			String[] delims = value.split(DELIMITER);
			for (int i = 0; i < delims.length; i++)
			{
				String curDelim = delims[i];
				userDelims.add(curDelim);
			}
		}
	}
	
	
	/**
	 * Creates a String representation of all words currently in this 
	 * WordDelimiters used for saving sessions.
	 * 
	 * @return String - list of words in this WordDelimiter used for restoring.
	 */
	
	public String toString()
	{
		StringBuffer delimVariables = new StringBuffer();
		
		//List of inUse words as a delimited list
		StringBuffer output = new StringBuffer();
		if (delimsInUse.size()> 0)
		{
			for (Iterator<String> iter = delimsInUse.iterator(); iter.hasNext();)
			{
				String curDelim = iter.next();
				output.append(curDelim + DELIMITER);
			}
			delimVariables.append("DelimsInUse" + SECONDDELIMITER + output.toString() + FIRSTDELIMITER);
		}
		
		//List of toAdd words as a delimeted list
		output = new StringBuffer();
		if (delimsToAdd.size()> 0)
		{
			for (Iterator<String> iter = delimsToAdd.iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				output.append(curWord + DELIMITER);
			}
			delimVariables.append("DelimsToAdd" + SECONDDELIMITER + output.toString() + FIRSTDELIMITER);
		}
		
		//List of added delimiters as a delimeted list
		output = new StringBuffer();
		if (userDelims.size()>0)
		{
			for (Iterator<String> iter = userDelims.iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				output.append(curWord + DELIMITER);
			}
			delimVariables.append("AddedDelims" + SECONDDELIMITER + output.toString() + FIRSTDELIMITER);
		}
		
		return delimVariables.toString();
	}
	
	
	public synchronized Pattern getSplitter() {
		if(splitter == null) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for(String delim : delimsInUse) {
				if(!first) {
					sb.append('|');
				}
				if(regexTranslation.containsKey(delim)) {
					sb.append(regexTranslation.get(delim));
				}
				else {
					sb.append(Pattern.quote(delim));
				}
				first = false;
			}
			for(String delim : userDelims) {
				if(!first) {
					sb.append('|');
				}
				sb.append(Pattern.quote(delim));
				first = false;
			}
			
			splitter = Pattern.compile(sb.toString());
		}
		return splitter;
	}
	
	
	public Set<String> split(String s) {
		Set<String> words = new HashSet<String>();
		for(String word : getSplitter().split(s)) {
			words.add(word);
		}
		words.remove("");
		return words;
	}
	
	/**
	 * Adds the specified delimiter into use if it is not currently in use.
	 */
	public void addDelimToUse(String delim)
	{
		splitter = null;
		//If it is one of the defined delims
		if (delimsToAdd.contains(delim))
		{
			delimsToAdd.remove(delim);
			delimsInUse.add(delim);
		}
		//Add to user list
		else
		{
			if (!userDelims.contains(delim))
				userDelims.add(delim);
		}
	}
	
	/**
	 * Removes the specified delimiter from use if it is currently in use.
	 */
	public void removeDelimiter(String delim)
	{
		splitter = null;
		//Disable removal of newline and tab for now
		if (delim.equals("newline") || delim.equals("tab"))
		{
			// MKTODO add this to the validation in the dialog
//			JOptionPane.showMessageDialog(application.getJFrame(), 
//			delim + " is not currently enabled for removal from the list of delimters.");
		}
		else
		{
		
			//First check user delims
			if (userDelims.contains(delim))
			{
				userDelims.remove(delim);
			}
			else  if (delimsInUse.contains(delim))
			{
				delimsInUse.remove(delim);
				delimsToAdd.add(delim);
			}
		}
	}
	
	//Getters and Setters
	
	public TreeSet<String> getDelimsInUse()
	{
		return delimsInUse;
	}
	
	public TreeSet<String> getDelimsToAdd()
	{
		return delimsToAdd;
	}
	
	public TreeSet<String> getUserDelims()
	{
		return userDelims;
	}
	
	
}
