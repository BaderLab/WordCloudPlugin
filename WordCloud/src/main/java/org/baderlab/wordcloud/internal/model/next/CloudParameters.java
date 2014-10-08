/*
 File: CloudParameters.java

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

package org.baderlab.wordcloud.internal.model.next;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.baderlab.wordcloud.internal.Stemmer;
import org.baderlab.wordcloud.internal.cluster.SemanticSummaryClusterBuilder;
import org.baderlab.wordcloud.internal.cluster.WordPair;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayStyles;
import org.baderlab.wordcloud.internal.ui.cloud.CloudWordInfo;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;

/**
 * The CloudParameters class defines all of the variables that are
 * needed to create a word Cloud for a particular network, attribute, 
 * and set of selected nodes.
 * @author Layla Oesper
 * @version 1.0
 *
 */

public class CloudParameters implements Comparable<CloudParameters>, CloudProvider
{
	
	//Default Values for User Input
	public static final double DEFAULT_NET_WEIGHT = 0.0;
	public static final String DEFAULT_ATT_NAME = CyNetwork.NAME;
	public static final int    DEFAULT_MAX_WORDS = 250;
	public static final double DEFAULT_CLUSTER_CUTOFF = 1.0;
	public static final String DEFAULT_STYLE = CloudDisplayStyles.DEFAULT_STYLE;
	

	//VARIABLES
	private String cloudName;
	private List<String> attributeNames;
	private String displayStyle;
	
	private NetworkParameters networkParams; //parent network
	
	private int maxWords;
	private int cloudNum; //Used to order the clouds for each network
	
	private Map<String, Set<CyNode>> stringNodeMapping;
	private Map<String, Integer> networkCounts; // counts for whole network
	private Map<String, Integer> selectedCounts; // counts for selected nodes
	private Map<WordPair, Integer> networkPairCounts;
	private Map<WordPair, Integer> selectedPairCounts;
	private Map<String, Double> ratios;
	private Map<WordPair, Double> pairRatios;
	private List<CloudWordInfo> cloudWords;
	
	
	private double netWeightFactor;
	private double clusterCutoff;
	
	private double minRatio;
	private double maxRatio;
	private double meanRatio;
	
	private double meanWeight;
	private double minWeight;
	private double maxWeight;
	
	private boolean countInitialized = false; //true when network counts are initialized
	private boolean selInitialized = false; //true when selected counts initialized
	private boolean ratiosInitialized = false; //true when ratios are computed
//	private boolean useNetNormal = false; //true when network counts are used
	
	//String Delimeters
	private static final String NODEDELIMITER = "CloudParamNodeDelimiter";
	private static final String WORDDELIMITER = "CloudParamWordDelimiter";
	private static final char controlChar = '\u001F';
	
	//Network Name creation variables
	private int networkCount = 1;
	private static final String NETWORKNAME = "Net";
	private static final String SEPARATER = "_";
	private static final double EPSILON = 0.00001;
	
	private String clusterColumnName;
	private CyTable clusterTable;
	
	//CONSTRUCTORS
	
	/**
	 * Default constructor to create a fresh instance
	 */
	protected CloudParameters(NetworkParameters networkParams)
	{
		this.networkParams = networkParams;
		this.stringNodeMapping = new HashMap<String, Set<CyNode>>();
		this.networkCounts = new HashMap<String, Integer>();
		this.selectedCounts = new HashMap<String, Integer>();
		this.networkPairCounts = new HashMap<WordPair, Integer>();
		this.selectedPairCounts = new HashMap<WordPair, Integer>();
		this.ratios = new HashMap<String, Double>();
		this.pairRatios = new HashMap<WordPair, Double>();
		this.cloudWords = new ArrayList<CloudWordInfo>();
		
		this.netWeightFactor = DEFAULT_NET_WEIGHT;
		this.clusterCutoff = DEFAULT_CLUSTER_CUTOFF;
		this.maxWords = DEFAULT_MAX_WORDS;
		this.displayStyle = DEFAULT_STYLE;
	}
	
	/**
	 * Constructor to create CloudParameters from a cytoscape property file
	 * while restoring a session.  Property file is created when the session is saved.
	 * @param propFile - the name of the property file as a String
	 */
	protected CloudParameters(NetworkParameters networkParams, String propFile)
	{
		this(networkParams);
		
		//Create a hashmap to contain all the values in the rpt file
		HashMap<String, String> props = new HashMap<String,String>();
		
		String[] lines = propFile.split("\n");
		
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			String[] tokens = line.split("\t");
			//there should be two values in each line
			if(tokens.length == 2)
				props.put(tokens[0],tokens[1]);
		}
		
		this.cloudName = props.get("CloudName");
		this.displayStyle = props.get("DisplayStyle");
		this.netWeightFactor = new Double(props.get("NetWeightFactor"));
		this.clusterCutoff = new Double(props.get("ClusterCutoff"));
		this.countInitialized = Boolean.parseBoolean(props.get("CountInitialized"));
		this.selInitialized = Boolean.parseBoolean(props.get("SelInitialized"));
		this.ratiosInitialized = Boolean.parseBoolean(props.get("RatiosInitialized"));
		this.maxRatio = new Double(props.get("MaxRatio"));
		this.minRatio = new Double(props.get("MinRatio"));
		this.maxWords = new Integer(props.get("MaxWords"));
		this.cloudNum = new Integer(props.get("CloudNum"));
		
		// Reload cloud group table if it has been created (through command line)
		for (CyTable table : networkParams.getManager().getTableManager().getAllTables(true)) {
			if (table.getTitle().equals(props.get("ClusterTableName"))) {
				this.clusterTable = table;
			}
		}
		
		//Backwards compatibale useNetNormal
//		String val = props.get("UseNetNormal");
//		if (val == null)
//		{this.useNetNormal = true;}
//		else
//		{this.useNetNormal = Boolean.parseBoolean(props.get("UseNetNormal"));}
		
		//Backwards compatible meanRatio
		String val = props.get("MeanRatio");
		if (val == null)
		{this.ratiosInitialized = false;}
		else
		{this.meanRatio = new Double(props.get("MeanRatio"));}
		
		//Backwards compatible Weights
		val = props.get("MeanWeight");
		if (val != null)
		{this.meanWeight = new Double(props.get("MeanWeight"));}
		
		val = props.get("MinWeight");
		if (val != null)
		{this.minWeight = new Double(props.get("MinWeight"));}
		
		val = props.get("MaxWeight");
		if (val != null)
		{this.maxWeight = new Double(props.get("MaxWeight"));}
		
		val = props.get("NetworkCount");
		if (val != null)
		{this.networkCount = new Integer(props.get("NetworkCount"));}
		
		//Rebuild attribute List
		String value = props.get("AttributeName");
		String[] attributes = value.split(WORDDELIMITER);
		ArrayList<String> attributeList = new ArrayList<String>();
		for (int i = 0; i < attributes.length; i++)
		{
			String curAttribute = attributes[i];
			attributeList.add(curAttribute);
		}
		this.attributeNames = attributeList;
		
		//Rebuild CloudWords
		if (props.containsKey("CloudWords")) //handle the empty case
		{
			String value2 = props.get("CloudWords");
			String[] words = value2.split(WORDDELIMITER);
			ArrayList<CloudWordInfo> cloudWordList = new ArrayList<CloudWordInfo>();
			for (int i = 0; i < words.length; i++)
			{
				String wordInfo = words[i];
				CloudWordInfo curInfo = new CloudWordInfo(wordInfo);
				curInfo.setCloudParameters(this);
				cloudWordList.add(curInfo);
			}
			this.cloudWords = cloudWordList;
		}
		else
			this.cloudWords = new ArrayList<CloudWordInfo>();
		
		CyTable cloudTable = networkParams.getNetwork().getDefaultNodeTable();
		if (cloudTable == null) {
			throw new RuntimeException();
		}
	}
		
	
	public boolean isNullCloud() {
		return this == networkParams.getNullCloud();
	}
	
	//METHODS
	
	public void delete() {
		CyNetwork network = networkParams.getNetwork();
		if (network.getDefaultNodeTable().getColumn(cloudName) != null) {
			network.getDefaultNodeTable().deleteColumn(cloudName);
		}
		
		CyTable clusterTable = getClusterTable();
		if (clusterTable != null) {
			clusterTable.deleteRows(Arrays.asList(cloudName));
			if (clusterTable.getRowCount() == 0) {
				// Delete column in network table with wordCloud ID
				network.getDefaultNetworkTable().deleteColumn(clusterTable.getTitle());
				// Delete wordCloud table
				networkParams.getManager().getTableManager().deleteTable(clusterTable.getSUID());						
			}
		}
		
		CloudModelManager cloudModelManager = networkParams.getManager();
		networkParams.removeCloudMapping(this);
		cloudModelManager.fireCloudDeleted(this);
	}
	
	
	public void rename(String newName) {
		if(newName.equals(cloudName))
			return;
		if(networkParams.containsCloud(newName))
			throw new IllegalArgumentException("Name '" + newName + "' already exists");
		
		String oldName = cloudName;
		setCloudName(newName);
		networkParams.changeCloudMapping(oldName, newName);
		
		CloudModelManager cloudModelManager = networkParams.getManager();
		cloudModelManager.fireCloudRenamed(this);
	}
	
	//Calculate Counts
	/**
	 * Constructs stringNodeMapping and networkCounts based on the list of
	 * nodes contained in networkParams.
	 */
	public void initializeNetworkCounts()
	{
		CyNetwork network = networkParams.getNetwork();
		
		//do nothing if already initialized
		if (countInitialized || network == null || attributeNames == null)
			return;
		
		//Clear old counts
		this.networkCounts = new HashMap<String, Integer>();
		this.networkPairCounts = new HashMap<WordPair, Integer>();
		this.stringNodeMapping = new HashMap<String, Set<CyNode>>();
		
		CyTable table = network.getDefaultNodeTable();
		for (String attributeName : attributeNames)
		{
			for (CyNode curNode : networkParams.getNetwork().getNodeList())
			{
				CyColumn column = table.getColumn(attributeName);
				if (column.getType().equals(String.class)) {
					String value = table.getRow(curNode.getSUID()).get(attributeName, String.class);
					updateNetworkWordCounts(curNode, value);
				}
				if (column.getType().equals(List.class) && column.getListElementType().equals(String.class)) {
					List<String> list = table.getRow(curNode.getSUID()).getList(attributeName, String.class);
					if (list == null) {
						continue;
					}
					String value = join(" ", list);
					updateNetworkWordCounts(curNode, value);
				}
			}//end attribute iterator
		}//end node iterator
		
		countInitialized = true;
	}
	
	private void updateNetworkWordCounts(CyNode curNode, String nodeValue) {
		if (nodeValue == null) // problem with nodes or attributes
			return;
	
		List<String> wordSet = this.processNodeString(nodeValue);
		String lastWord = ""; //Used for calculating pair counts
    
		//Iterate through all words
		Iterator<String> wordIter = wordSet.iterator();
		while(wordIter.hasNext())
		{
			String curWord = wordIter.next();
		
			//Check filters
			WordFilter filter = networkParams.getFilter();
			if (!filter.contains(curWord))
			{
				//If this word has not been encountered, or not encountered
				//in this node, add it to our mappings and counts
				Map<String, Set<CyNode>> curMapping = this.getStringNodeMapping();
		
				//If we have not encountered this word, add it to the mapping
				if (!curMapping.containsKey(curWord))
				{
					curMapping.put(curWord, new HashSet<CyNode>());
					networkCounts.put(curWord, 0);
				}
			
				//Add node to mapping, update counts
				curMapping.get(curWord).add(curNode);
				int num = networkCounts.get(curWord);
				num = num + 1;
				networkCounts.put(curWord, num);
			
			
				//Add to pair counts
				if (!lastWord.equals(""))
				{
					WordPair pair = new WordPair(lastWord, curWord, this);
					
					Integer curPairCount = networkPairCounts.get(pair);
					int count;
					if (curPairCount == null) {
						count = 1;
					} else {
						count = curPairCount;
					}
				
					networkPairCounts.put(pair, count);
				}
			
				//Update curWord to be LastWord
				lastWord = curWord;
			
			}//end filter if
		}// word iterator
	}
	
	private void updateSelectedWordCounts(CyNode curNode, String nodeValue) {
		if (nodeValue == null) // problem with nodes or attributes
			return;
	
		List<String> wordSet = this.processNodeString(nodeValue);
		String lastWord = ""; //Used for calculating pair counts
    
		//Iterate through all words
		Iterator<String> wordIter = wordSet.iterator();
		while(wordIter.hasNext())
		{
			String curWord = wordIter.next();
		
			//Check filters
			WordFilter filter = networkParams.getFilter();
			if (!filter.contains(curWord))
			{
				//Add to selected Counts
			
				int curCount = 0; 
			
				if (selectedCounts.containsKey(curWord))
					curCount = selectedCounts.get(curWord);
			
				//Update Count
				curCount = curCount + 1;
			
				//Add updated count to HashMap
				selectedCounts.put(curWord, curCount);
			
				//Add to pair counts
				if (!lastWord.equals(""))
				{
					WordPair pair = new WordPair(lastWord, curWord, this);
					
					Integer curPairCount = selectedPairCounts.get(pair);
					int count;
					if (curPairCount == null) {
						count = 1;
					} else {
						count = curPairCount;
					}
				
					selectedPairCounts.put(pair, count);
				}
			
				//Update curWord to be LastWord
				lastWord = curWord;
			
			}//end filter if
		}// word iterator
	}
	
	/**
	 * Constructs selectedCounts based on the list of nodes contained in 
	 * selectedNodes list.
	 * 
	 * MKTODO does this have to be called manually, can we fire from an event listener of some sort
	 */
	public void updateSelectedCounts()
	{
		//do nothing if selected hasn't changed initialized
		if (selInitialized || attributeNames == null)
			return;
		
		//Initialize if needed
		if (!countInitialized)
			this.initializeNetworkCounts();
		
		//Clear old counts
		this.selectedCounts = new HashMap<String, Integer>();
		this.selectedPairCounts = new HashMap<WordPair, Integer>();
		
		CyNetwork network = networkParams.getNetwork();
		CyTable table = network.getDefaultNodeTable();
		for (String attributeName : attributeNames)
		{
			for (CyNode curNode : getSelectedNodes())
			{
				CyColumn column = table.getColumn(attributeName);
				if (column.getType().equals(String.class)) {
					String value = table.getRow(curNode.getSUID()).get(attributeName, String.class);
					updateSelectedWordCounts(curNode, value);
				}
				if (column.getType().equals(List.class) && column.getListElementType().equals(String.class)) {
					List<String> list = table.getRow(curNode.getSUID()).getList(attributeName, String.class);
					if (list == null) {
						continue;
					}
					String value = join(" ", list);
					updateSelectedWordCounts(curNode, value);
				}
			}// end attribute list
		}//end node iterator
		
		calculateWeights();
		
		selInitialized = true;
	}
	
	/**
	 * Sets the mean weight value to be the average of all ratios if a network normalization
	 * factor of 0 were to be used.  The values are also translated so the min value is 0.
	 */
	public void calculateWeights()
	{
		double curMin = 0.0;
		double curMax = 0.0;
		double total = 0.0;
		int count = 0;
		
		//Iterate through to calculate ratios
		boolean initialized = false;
		for (Entry<String, Integer> entry : selectedCounts.entrySet())
		{
			String curWord = entry.getKey();
			
			/* Ratio: (selCount/selTotal)/((netCount/netTotal)^netWeightFactor)
			 * But, to avoid underflow from small probabilities we calculate it as follows:
			 * (selCount * (netTotal^netWeightFactor))/(selTotal * (netCount^netWeightFactor))
			 * This is the same as the original definition of ratio, just with some
			 * different algebra.
			 */
			int selTotal = this.getSelectedNumNodes();
			int selCount = entry.getValue();
			int netCount = networkCounts.get(curWord);
			double newNetCount = Math.pow(netCount, 0.0);
			int netTotal = this.getNetworkNumNodes();
			double newNetTotal = Math.pow(netTotal, 0.0);
			
			double numerator = selCount * newNetTotal;
			double denominator = selTotal * newNetCount;
			double ratio = numerator/denominator;
			
			total = total + ratio;
			count = count + 1;
			
			//Update max/min ratios
			if (!initialized)
			{
				curMax = ratio;
				curMin = ratio;
				initialized = true;
			}
			
			if (ratio > curMax)
				curMax = ratio;
			
			if (ratio < curMin)
				curMin = ratio;
		}
		
		//store
		this.setMinWeight(curMin);
		this.setMeanWeight(total/count);
		this.setMaxWeight(curMax);
	}
	
	/**
	 * Calculates ratios given the current selectedNode counts.
	 */
	public void updateRatios()
	{
		//already up to date
		if (ratiosInitialized)
			return;
		
		//Check that selected counts are up to date
		if(!selInitialized)
			this.updateSelectedCounts();
		
		//SINGLE COUNTS
		//Clear old counts
		this.ratios = new HashMap<String, Double>();
		
		double curMin = 0.0;
		double curMax = 0.0;
		double total = 0.0;
		int count = 0;
		
		//Iterate through to calculate ratios
		boolean initialized = false;
		for (Entry<String, Integer> entry : selectedCounts.entrySet())
		{
			String curWord = entry.getKey();
			
			/* Ratio: (selCount/selTotal)/((netCount/netTotal)^netWeightFactor)
			 * But, to avoid underflow from small probabilities we calculate it as follows:
			 * (selCount * (netTotal^netWeightFactor))/(selTotal * (netCount^netWeightFactor))
			 * This is the same as the original definition of ratio, just with some
			 * different algebra.
			 */
			int selTotal = this.getSelectedNumNodes();
			int selCount = entry.getValue();
			int netCount = networkCounts.get(curWord);
			double newNetCount = Math.pow(netCount, netWeightFactor);
			int netTotal = this.getNetworkNumNodes();
			double newNetTotal = Math.pow(netTotal, netWeightFactor);
			
			double numerator = selCount * newNetTotal;
			double denominator = selTotal * newNetCount;
			double ratio = numerator/denominator;
			
			ratios.put(curWord, ratio);
			
			total = total + ratio;
			count = count + 1;
			
			//Update max/min ratios
			if (!initialized)
			{
				curMax = ratio;
				curMin = ratio;
				initialized = true;
			}
			
			if (ratio > curMax)
				curMax = ratio;
			
			if (ratio < curMin)
				curMin = ratio;
		}
		
		this.setMaxRatio(curMax);
		this.setMinRatio(curMin);
		this.setMeanRatio(total/count);
		
		//PAIR COUNTS
		//Clear old counts
		this.pairRatios = new HashMap<WordPair, Double>();
		
		//Iterate through to calculate ratios
		for (Entry<WordPair, Integer> entry : selectedPairCounts.entrySet())
		{
			WordPair pair = entry.getKey();
			/* Ratio: (selCount/selTotal)/((netCount/netTotal)^netWeightFactor)
			 * But, to avoid underflow from small probabilities we calculate it as follows:
			 * (selCount * (netTotal^netWeightFactor))/(selTotal * (netCount^netWeightFactor))
			 * This is the same as the original definition of ratio, just with some
			 * different algebra.
			 */
			int selTotal = this.getSelectedNumNodes();
			int selPairCount = entry.getValue();
			int netPairCount = networkPairCounts.get(pair);
			double newNetCount = Math.pow(netPairCount, netWeightFactor);
			int netTotal = this.getNetworkNumNodes();
			double newNetTotal = Math.pow(netTotal, netWeightFactor);
			
			double numerator = selPairCount * newNetTotal;
			double denominator = selTotal * newNetCount;
			double ratio = numerator/denominator;
			
			pairRatios.put(pair, ratio);
		}
		ratiosInitialized = true;
	}
	
	/**
	 * Creates a cloud clustering object and clusters based on the parameter
	 * in this CloudParameters.
	 * @throws  
	 */
	public void calculateFontSizes()
	{
		if (!ratiosInitialized)
			this.updateRatios();
		
		//Clear old fonts
		this.cloudWords = new ArrayList<CloudWordInfo>();
		
		if (displayStyle.equals(CloudDisplayStyles.NO_CLUSTERING))
		{
			for (Entry<String, Double> entry : ratios.entrySet())
			{
				String curWord = entry.getKey();
				int fontSize = calculateFontSize(curWord, entry.getValue());
				CloudWordInfo curInfo = new CloudWordInfo(curWord, fontSize);
				curInfo.setCloudParameters(this);
				cloudWords.add(curInfo);
			}//end while loop
			
			//Sort cloudWords in order by fontsize
			Collections.sort(cloudWords);
		}
		else
		{
			SemanticSummaryClusterBuilder builder = new SemanticSummaryClusterBuilder(this);
			builder.clusterData(this.getClusterCutoff());
			builder.buildCloudWords();
			cloudWords = builder.getCloudWords();
		}
	}
	
	
	/**
	 * Calculates the font for a given word by using its ratio, the max and
	 * min ratios as well as the max and min font size in the parent 
	 * parameters object.  Assumes ratios are up to date and that word
	 * is in the selected nodes.
	 * @return int - the calculated font size for the specified word.
	 */
	public int calculateFontSize(String aWord, double ratio)
	{
		//Zeroed mapping
		//Get zeroed values for calculations
		double zeroedMinWeight = minWeight - minWeight;
		double zeroedMeanWeight = meanWeight - minWeight;
		double zeroedMaxWeight = maxWeight - minWeight;
		
		double zeroedMinRatio = minRatio - minRatio;
		double zeroedMeanRatio = meanRatio - minRatio;
		double zeroedMaxRatio = maxRatio - minRatio;
		
		double zeroedRatio = ratio - minRatio;
		
		double newRatio = zeroedRatio * zeroedMeanWeight / zeroedMeanRatio;
		
		//Weighted Average
		int maxFont = NetworkParameters.MAXFONTSIZE;
		int minFont = NetworkParameters.MINFONTSIZE;
		
		//Check if maxRatio and minRatio are the same
		if (isCloseEnough(zeroedMaxRatio, zeroedMinRatio))
			return (minFont + (maxFont - minFont)/2);
		
		double slope = (maxFont - minFont)/(zeroedMaxWeight - zeroedMinWeight);
		double yIntercept = maxFont - (slope*zeroedMaxWeight); //maxRatio maps to maxFont
		
		//Round up to nearest int
		long temp = Math.round((slope*newRatio) + yIntercept);
		int fontSize = Math.round(temp);
		
		return fontSize;
	}
	
	private boolean isCloseEnough(double d1, double d2) {
		return Math.abs(d1 - d2) < EPSILON * Math.max(Math.abs(d1), Math.abs(d2));
	}

//	/**
//	 * Retrieves values from Input panel and stores in correct places.
//	 * @return
//	 */
//	public void retrieveInputVals(SemanticSummaryInputPanel inputPanel)
//	{
//		//Network Weight Stuff
//		SliderBarPanel panel = inputPanel.getSliderBarPanel();
//		double netNorm = panel.getNetNormValue();
//		this.setNetWeightFactor(netNorm);
////		Boolean selected = true; // inputPanel.getUseNetworkCounts().isSelected();
////		this.useNetNormal = selected;
//		
//		
//		//Attribute
//		Object[] attributes = inputPanel.getAttributeList().getSelectedValues();
//		ArrayList<String> attributeList = new ArrayList<String>();
//		
//		for (int i = 0; i < attributes.length; i++)
//		{
//			Object curAttribute = attributes[i];
//			
//			if (curAttribute instanceof String)
//			{
//				attributeList.add((String) curAttribute);
//			}
//		}
//		setAttributeNames(attributeList);
//			
//		//Max Words
////		JFormattedTextField maxWordsTextField = inputPanel.getMaxWordsTextField();
//		
////		Number value = (Number) maxWordsTextField.getValue();
////		if ((value != null) && (value.intValue() >= 0))
////		{
////			setMaxWords(value.intValue()); 
////		}
////		else
////		{
////			maxWordsTextField.setValue(defaultMaxWords);
//			setMaxWords(defaultMaxWords);
////			String message = "The maximum number of words to display must be greater than or equal to 0.";
////			JOptionPane.showMessageDialog(inputPanel, message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
////		}
//		
////		//Cluster Cutoff
////		JFormattedTextField clusterCutoffTextField = inputPanel.getClusterCutoffTextField();
////		
////		value = (Number) clusterCutoffTextField.getValue();
////		if ((value != null) && (value.doubleValue() >= 0.0))
////		{
////			setClusterCutoff(value.doubleValue()); //sets all necessary flags
////		}
////		else
////		{
////			clusterCutoffTextField.setValue(defaultClusterCutoff);
//			setClusterCutoff(defaultClusterCutoff);
////			String message = "The cluster cutoff must be greater than or equal to 0";
////			JOptionPane.showMessageDialog(inputPanel, message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
////		}
//		
//		//Style
//		Object style = inputPanel.getCMBStyle().getSelectedItem();
//		if (style instanceof String)
//			setDisplayStyle((String) style);
//		else
//		{
//			setDisplayStyle(defaultStyle);
//			inputPanel.getCMBStyle().setSelectedItem(defaultStyle);
//			String message = "You must select one of the available styles.";
//			JOptionPane.showMessageDialog(inputPanel, message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
//		}
//	}
	
	/**
	 * String representation of CloudParameters.
	 * It is used to store the persistent Attributes as a property file.
	 * @return - String representation of this object
	 */
	public String toString()
	{
		StringBuffer paramVariables = new StringBuffer();
		
		paramVariables.append("CloudName\t" + cloudName + "\n");
		paramVariables.append("DisplayStyle\t" + displayStyle + "\n");
		
		//List of attributes as a delimited list
		StringBuffer output = new StringBuffer();
		if (attributeNames != null) {
			for (int i = 0; i < attributeNames.size(); i++)
			{
				output.append(attributeNames.get(i) + WORDDELIMITER);
			}
		}
		paramVariables.append("AttributeName\t" + output.toString() + "\n");
		
		paramVariables.append("NetWeightFactor\t" + netWeightFactor + "\n");
		paramVariables.append("ClusterCutoff\t" + clusterCutoff + "\n");
		paramVariables.append("CountInitialized\t" + countInitialized + "\n");
		paramVariables.append("SelInitialized\t" + selInitialized + "\n");
		paramVariables.append("RatiosInitialized\t" + ratiosInitialized + "\n");
		paramVariables.append("MinRatio\t" + minRatio + "\n");
		paramVariables.append("MaxRatio\t" + maxRatio + "\n");
		paramVariables.append("MaxWords\t" + maxWords + "\n");
		paramVariables.append("MeanRatio\t" + meanRatio + "\n");
		paramVariables.append("MeanWeight\t" + meanWeight + "\n");
		paramVariables.append("MaxWeight\t" + maxWeight + "\n");
		paramVariables.append("MinWeight\t" + minWeight + "\n");
		paramVariables.append("CloudNum\t" + cloudNum + "\n");
//		paramVariables.append("UseNetNormal\t" + useNetNormal + "\n");
		paramVariables.append("NetworkCount\t" + networkCount + "\n");
		if (clusterTable != null) {
			paramVariables.append("ClusterTableName\t" + clusterTable.getTitle() + "\n");
		}
		
		//List of Nodes as a comma delimited list
		StringBuffer output2 = new StringBuffer();
		for (int i = 0; i < cloudWords.size(); i++)
		{
			output2.append(cloudWords.get(i).toString() + WORDDELIMITER);
		}
		
		paramVariables.append("CloudWords\t" + output2.toString() + "\n");
		
		return paramVariables.toString();
	}
	
	/**
	 * This method repopulates a properly specified Hashmap from the given file and type.
	 * @param fileInput - file name where the has map is stored
	 * @param type - the type of hashmap in the file.  The hashes are repopulated
	 * based on the property file stored in the session file.  The property file
	 * specifieds the type of objects contained in each file and this is needed in order
	 * to create the proper has in the current set of parameters.
	 * types are Counts(1) and Mapping(2)
	 * @return properly constructed Hashmap repopulated from the specified file.
	 */
	public HashMap repopulateHashmap(String fileInput, int type)
	{
		//Hashmap to contain values from the file
		HashMap newMap;
		
		//Counts (network or selected)
		if (type == 1)
			newMap = new HashMap<String, Integer>();
		//Mapping
		else if (type == 2)
			newMap = new HashMap<String, List<String>>();
		//Ratios
		else if (type == 3)
			newMap = new HashMap<String, Double>();
		else
			newMap = new HashMap();
		
		//Check that we have input
		if (!fileInput.equals(""))
		{
			String [] lines = fileInput.split("\n");
		
			for (int i = 0; i < lines.length; i++)
			{
				String line = lines[i];
				String [] tokens = line.split("\t");
			
				//the first token is the key and the rest is the object
				//Different types have different data
			
				//Counts
				if (type == 1)
					newMap.put(tokens[0], Integer.parseInt(tokens[1]));
			
				//Mapping
				if (type == 2)
				{
					//Create List
					String [] nodes = tokens[1].split(NODEDELIMITER);
					ArrayList nodeNames = new ArrayList<String>();
					for (int j =0; j < nodes.length; j++)
						nodeNames.add(nodes[j]);
				
					newMap.put(tokens[0], nodeNames);
				}
			
				//Ratios
				if (type == 3)
					newMap.put(tokens[0], Double.parseDouble(tokens[1]));
			}//end line loop
		}//end if data exists check
		return newMap;
	}
	
	/**
	 * This method takes in the ID of a node and returns the string that is associated
	 * with that node and the current attribute of this CloudParameters.
	 * @param CyNode - node we are interested in 
	 * @param String - name of the attribute we are interested in
	 * @return String - value stored in the current attribute for the given node.
	 */
	private String getNodeAttributeVal(CyNetwork network, CyNode curNode, String attributeName)
	{
		CyTable table = network.getDefaultNodeTable();
		CyColumn column = table.getColumn(attributeName);
		if (column.getType().equals(String.class)) {
			return table.getRow(curNode.getSUID()).get(attributeName, String.class);
		}
		if (column.getType().equals(List.class) && column.getListElementType().equals(String.class)) {
			List<String> list = table.getRow(curNode.getSUID()).getList(attributeName, String.class);
			if (list == null) {
				return null;
			}
			return join(" ", list);
		}
		return null;
	}//end method
	
	private String join(String delimiter, List<String> list) {
		StringBuilder buffer = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (first) {
				first = false;
			} else {
				buffer.append(delimiter);
			}
			buffer.append(item);
		}
		return buffer.toString();
	}

	/**
	 * This method takes in a string from a node and processes it to lower case, removes
	 * punctuation and separates the words into a non repeated list.
	 * @param String from a node that we are processing.
	 * @return Set of distinct words.
	 */
	private List<String> processNodeString(String nodeValue)
	{
		//Only deal with lower case
		nodeValue = nodeValue.toLowerCase();
		
		//replace all punctuation with white spaces except ' and -
		//nodeValue = nodeValue.replaceAll("[[\\p{Punct}] && [^'-]]", " ");
		String controlString = Character.toString(controlChar);
		
		//Remove all standard delimiters and replace with controlChar
		WordDelimiters delims = this.getNetworkParams().getDelimeters();
		nodeValue = nodeValue.replaceAll(delims.getRegex(),controlString);
        
		//Remove all user stated delimiters and replace with controlChar
		for (Iterator<String> iter = delims.getUserDelims().iterator(); iter.hasNext();)
		{
			String userDelim = iter.next();
			nodeValue = nodeValue.replaceAll(userDelim, controlString);
		}
		
        //Separate into non repeating set of words
		List<String> wordSet = new ArrayList<String>();
		StringTokenizer token = new StringTokenizer(nodeValue, controlString);
        while (token.hasMoreTokens())
        {
        	String a = token.nextToken();
        	
        	
        	//Stem the word if parameter is set
        	if (this.getNetworkParams().getIsStemming()) //Check for stemming
        	{
        		Stemmer stem = new Stemmer();
        		for (int i = 0; i < a.length(); i++)
        		{
        			char ch = a.charAt(i);
        			stem.add(ch);
        		}
        		stem.stem();
        		a = stem.toString();
        	}
        	
        	
        	if (!wordSet.contains(a))
        		wordSet.add(a);
        }
        
        return wordSet;
	}
	
	/**
	 * Compares two CloudParameters objects based on the order in which they
	 * were created.
	 * @param CloudParameters object to compare this object to
	 * @return
	 */
	public int compareTo(CloudParameters compare) 
	{	
		int thisCount = this.getCloudNum();
		int compareCount = compare.getCloudNum();
		return thisCount - compareCount;
	}
	
	/**
	 * Returns the name for the next network for this cloud.
	 * @return String - name of the next cloud
	 */
	public String getNextNetworkName()
	{
		String name = networkParams.getNetworkName() + "-" + cloudName + "-" + NETWORKNAME + SEPARATER + networkCount;
		networkCount++;
		
		return name;
	}
	
	
	//Getters and Setters
	public String getCloudName()
	{
		return cloudName;
	}
	
	protected void setCloudName(String name)
	{
		CyNetwork network = networkParams.getNetwork();
		if (network == null) {
			cloudName = name;
			return;
		}
		
		CyTable table = network.getDefaultNodeTable();
		if (cloudName != null) {
			CyColumn column = table.getColumn(cloudName);
			column.setName(name);
		} else {
			if (table.getColumn(name) != null) {
				table.deleteColumn(name);
			}
			table.createColumn(name, Boolean.class, false);
		}
		cloudName = name;
	}
	
	public List<String> getAttributeNames()
	{
		return attributeNames;
	}
	
	public void setAttributeNames(List<String> names)
	{
		//Check if we need to reset flags
		boolean changed = false;
		if (attributeNames == null || names.size() != attributeNames.size())
			changed = true;
		else
		{
			for (int i = 0; i < names.size(); i++)
			{
				String curAttribute = names.get(i);
				
				if (!attributeNames.contains(curAttribute))
				{
					changed = true;
					continue;
				}
			}
		}
		
		//Set flags
		if (changed)
		{
			countInitialized = false;
			selInitialized = false;
			ratiosInitialized = false;
		}
		
		//Set to new value
		attributeNames = names;
	}
	
	public void addAttributeName(String name)
	{
		if (attributeNames == null) {
			attributeNames = new ArrayList<String>();
		}
		
		if (!attributeNames.contains(name))
		{
			attributeNames.add(name);
			countInitialized = false;
			selInitialized = false;
			ratiosInitialized = false;
		}
	}
	

	
	public NetworkParameters getNetworkParams()
	{
		return networkParams;
	}
	
	public Set<CyNode> getSelectedNodes()
	{
		return getSelectedNodes(networkParams.getNetwork());
	}
	
	private Set<CyNode> getSelectedNodes(CyNetwork network) {
		if (network == null) {
			return Collections.emptySet();
		}
		
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			Boolean selected = row.get(cloudName, Boolean.class);
			if (selected != null && selected) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public void setSelectedNodes(Set<CyNode> nodes)
	{
		setSelectedNodes(networkParams.getNetwork(), nodes);
		
		selInitialized = false; //So we update when SelectedNodes change
		ratiosInitialized = false; //need to update ratios
	}
	
	private void setSelectedNodes(CyNetwork network, Set<CyNode> nodes) {
		if (network == null) {
			return;
		}
		
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			Boolean wasSelected = row.get(cloudName, Boolean.class);
			if (wasSelected == null) {
				wasSelected = Boolean.FALSE;
			}
			boolean selected = nodes.contains(node);
			if (wasSelected != selected) {
				row.set(cloudName, selected);
			}
		}
	}

	public int getSelectedNumNodes()
	{
		return getSelectedNumNodes(networkParams.getNetwork());
	}
	
	private int getSelectedNumNodes(CyNetwork network) {
		if (network == null) {
			return 0;
		}
		
		return getSelectedNodes(network).size();
	}

	public Map<String, Set<CyNode>> getStringNodeMapping()
	{
		return stringNodeMapping;
	}
	
	public Map<String,Integer> getNetworkCounts()
	{
		return networkCounts;
	}
	
	public Map<String,Integer> getSelectedCounts()
	{
		return selectedCounts;
	}
	
	public Map<WordPair,Integer> getSelectedPairCounts()
	{
		return selectedPairCounts;
	}
	
	public Map<WordPair,Integer> getNetworkPairCounts()
	{
		return networkPairCounts;
	}
	
	public Map<String,Double> getRatios()
	{
		return ratios;
	}
	
	public Map<WordPair,Double> getPairRatios()
	{
		return pairRatios;
	}
	
	public List<CloudWordInfo> getCloudWordInfoList()
	{
		return cloudWords;
	}
	
	public void setCloudWordInfoList(ArrayList<CloudWordInfo> words)
	{
		cloudWords = words;
	}
	
	public int getNetworkNumNodes()
	{
		if (networkParams == null) {
			return 0;
		}
		CyNetwork network = networkParams.getNetwork();
		if (network == null) {
			return 0;
		}
		return network.getNodeCount();
	}

	public double getMinRatio()
	{
		return minRatio;
	}
	
	public void setMinRatio(double ratio)
	{
		minRatio = ratio;
	}
	
	public double getMaxRatio()
	{
		return maxRatio;
	}
	
	public void setMaxRatio(double ratio)
	{
		maxRatio = ratio;
	}
	
	public double getMeanRatio()
	{
		return meanRatio;
	}
	
	public void setMeanRatio(double ratio)
	{
		meanRatio = ratio;
	}
	
	public double getMinWeight()
	{
		return minWeight;
	}
	
	public void setMinWeight(double val)
	{
		minWeight = val;
	}
	
	public double getMaxWeight()
	{
		return maxWeight;
	}
	
	public void setMaxWeight(double val)
	{
		maxWeight = val;
	}
	
	public double getMeanWeight()
	{
		return meanWeight;
	}
	
	public void setMeanWeight(double val)
	{
		meanWeight = val;
	}
	
	
	public boolean getCountInitialized()
	{
		return countInitialized;
	}
	
	public void setCountInitialized(boolean val)
	{
		countInitialized = val;
	}
	
	public boolean getSelInitialized()
	{
		return selInitialized;
	}
	
	public void setSelInitialized(boolean val)
	{
		selInitialized = val;
	}
	
	public boolean getRatiosInitialized()
	{
		return ratiosInitialized;
	}
	
	public void setRatiosInitialized(boolean val)
	{
		ratiosInitialized = val;
	}
	
	public double getNetWeightFactor()
	{
		return netWeightFactor;
	}
	
	public void setNetWeightFactor(double val)
	{
		//Reset flags if net Weight changes
		if (netWeightFactor != val)
			ratiosInitialized = false;
		
		netWeightFactor = val;
	}
	
	public double getClusterCutoff()
	{
		return clusterCutoff;
	}
	
	public void setClusterCutoff(double val)
	{
		clusterCutoff = val;
	}
	
	public int getMaxWords()
	{
		return maxWords;
	}
	
	public void setMaxWords(int val)
	{
		maxWords = val;
	}
	
	public int getCloudNum()
	{
		return cloudNum;
	}
	
	public void setCloudNum(int num)
	{
		cloudNum = num;
	}	
	
	public String getDisplayStyle()
	{
		return displayStyle;
	}
	
	public void setDisplayStyle(String style)
	{
		displayStyle = style;
	}
	
//	public boolean getUseNetNormal()
//	{
//		return useNetNormal;
//	}
//	
//	public void setUseNetNormal(boolean val)
//	{
//		useNetNormal = val;
//	}

	public String getClusterColumnName() {
		return clusterColumnName;
	}
	
	public void setClusterColumnName(String clusterColumnName) {
		this.clusterColumnName = clusterColumnName;		
	}

	public void setClusterTable(CyTable clusterTable) {
		this.clusterTable = clusterTable;
	}
	
	public CyTable getClusterTable() {
		if (clusterTable != null) {
			return clusterTable;
		} else {
			try {
				CyNetwork network = networkParams.getNetwork();
				CyTableManager tableManager = networkParams.getManager().getTableManager();
				String tableName = cloudName.substring(0, cloudName.indexOf(" Cloud "));
				return tableManager.getTable(network.getDefaultNetworkTable().getRow(network.getSUID()).get(tableName, Long.class));
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	@Override
	public CloudParameters getCloud() {
		return this;
	}
}
