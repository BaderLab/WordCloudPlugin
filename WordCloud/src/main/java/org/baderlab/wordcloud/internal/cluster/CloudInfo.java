package org.baderlab.wordcloud.internal.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.baderlab.wordcloud.internal.Stemmer;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.model.WordDelimiters;
import org.baderlab.wordcloud.internal.model.WordFilter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

/**
 * This class does the heavy lifting of calculating the contents of the cloud.
 */
public class CloudInfo {

	//Font Size Values
	protected static final Integer MINFONTSIZE = 12; 
	protected static final Integer MAXFONTSIZE = 64;
		
	private Map<String, Set<CyNode>> stringNodeMapping = new HashMap<String, Set<CyNode>>();
	private Map<String, Integer> networkCounts = new HashMap<String, Integer>(); // counts for whole network
	private Map<String, Integer> selectedCounts = new HashMap<String, Integer>(); // counts for selected nodes
	private Map<WordPair, Integer> networkPairCounts = new HashMap<WordPair, Integer>();
	private Map<WordPair, Integer> selectedPairCounts = new HashMap<WordPair, Integer>();
	private Map<String, Double> ratios = new HashMap<String, Double>();
	private Map<WordPair, Double> pairRatios = new HashMap<WordPair, Double>();
	
	private List<CloudWordInfo> cloudWords = new ArrayList<CloudWordInfo>();
	
	

	private double minRatio;
	private double maxRatio;
	private double meanRatio;
	
	private double meanWeight;
	private double minWeight;
	private double maxWeight;

	
	private boolean countInitialized = false; //true when network counts are initialized
	private boolean selInitialized = false; //true when selected counts initialized
	private boolean ratiosInitialized = false; //true when ratios are computed
	private int countTotal = 0;
	
	
	private final CloudParameters cloud; 
	
	
	/**
	 * This object is not thread safe.
	 * 
	 * If another thread changes the state of the CloudParameters object while
	 * the cloud is being calculated that might have unintended results.
	 * However the implementation of CloudTaskManager serializes
	 * calls to calculateCloud(), and the info panel updates the cloud every
	 * time a UI field changes. This means that the cloud will be always
	 * be recalcuated any time something changes.
	 */
	public CloudInfo(CloudParameters cloud) {
		this.cloud = cloud;
	}
	
	
	public boolean isForCloud(CloudParameters cloud) {
		return this.cloud == cloud;
	}
	
	public CyNetwork getNetwork() {
		return cloud.getNetworkParams().getNetwork();
	}
	
	public int getMaxWords() {
		return cloud.getMaxWords();
	}
	
	public int getMinWordOccurrence() {
		return cloud.getMinWordOccurrence();
	}
	
	public CloudDisplayStyles getDisplayStyle() {
		return cloud.getDisplayStyle();
	}
	
	//Calculate Counts
	/**
	 * Constructs stringNodeMapping and networkCounts based on the list of
	 * nodes contained in networkParams.
	 */
	private void initializeNetworkCounts() {
		NetworkParameters networkParams = cloud.getNetworkParams();
		CyNetwork network = networkParams.getNetwork();
		
		//do nothing if already initialized
		if (countInitialized || network == null)
			return;
		
		//Clear old counts
		this.networkCounts = new HashMap<String, Integer>();
		this.networkPairCounts = new HashMap<WordPair, Integer>();
		this.stringNodeMapping = new HashMap<String, Set<CyNode>>();
		
		for (String attributeName : cloud.getAttributeNames()) {
			for (CyNode curNode : networkParams.getNetwork().getNodeList()) {
				String value = getNodeAttributeVal(network, curNode, attributeName);
				if(value != null) {
					updateNetworkWordCounts(curNode, value);
				}
			}
		}
		
		countInitialized = true;
	}
	
	/**
	 * This method takes in a string from a node and processes it to lower case, removes
	 * punctuation and separates the words into a non repeated list.
	 * @param String from a node that we are processing.
	 * @return Set of distinct words.
	 */
	private Collection<String> processNodeString(String nodeValue) {
		WordDelimiters delimeters = cloud.getNetworkParams().getDelimeters();
		Collection<String> words = delimeters.split(nodeValue.toLowerCase());
		
		if(cloud.getNetworkParams().getIsStemming()) {
			Set<String> stemmedWords = new HashSet<String>();
			for(String word : words) {
				Stemmer stemmer = new Stemmer();
				for(int i = 0; i < word.length(); i++) {
					stemmer.add(word.charAt(i));
				}
				stemmer.stem();
				stemmedWords.add(stemmer.toString());
			}
			words = stemmedWords;
		}
		
		return words;
	}
	
	
	private void updateNetworkWordCounts(CyNode curNode, String nodeValue) {
		if (nodeValue == null) // problem with nodes or attributes
			return;
	
		Collection<String> wordSet = this.processNodeString(nodeValue);
		String lastWord = ""; //Used for calculating pair counts
    
		WordFilter filter = cloud.getNetworkParams().getFilter();
		
		//Iterate through all words
		for(String curWord : wordSet)
		{
			//Check filters
			if (!filter.contains(curWord))
			{
				//If this word has not been encountered, or not encountered
				//in this node, add it to our mappings and counts
				//If we have not encountered this word, add it to the mapping
				if (!stringNodeMapping.containsKey(curWord))
				{
					stringNodeMapping.put(curWord, new HashSet<CyNode>());
					networkCounts.put(curWord, 0);
				}
			
				//Add node to mapping, update counts
				stringNodeMapping.get(curWord).add(curNode);
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
			}
		}
	}
	

	
	/**
	 * Constructs selectedCounts based on the list of nodes contained in 
	 * selectedNodes list.
	 * 
	 * MKTODO does this have to be called manually, can we fire from an event listener of some sort
	 */
	private void updateSelectedCounts() {
		CyNetwork network = cloud.getNetworkParams().getNetwork();
		//do nothing if selected hasn't changed initialized
		if (selInitialized || network == null)
			return;
		
		//Initialize if needed
		if (!countInitialized)
			this.initializeNetworkCounts();
		
		//Clear old counts
		this.selectedCounts = new HashMap<String, Integer>();
		this.selectedPairCounts = new HashMap<WordPair, Integer>();
		
		Collection<CyNode> selectedNodes = cloud.getSelectedNodes();
		
		for (String attributeName : cloud.getAttributeNames()) {
			for (CyNode curNode : selectedNodes) {
				String value = getNodeAttributeVal(network, curNode, attributeName);
				if(value != null) {
					updateSelectedWordCounts(curNode, value);
				}
			}
		}
		
		calculateWeights();
		
		selInitialized = true;
		
	}
	
	
	private void updateSelectedWordCounts(CyNode curNode, String nodeValue) {
		if (nodeValue == null) // problem with nodes or attributes
			return;
	
		Collection<String> wordSet = this.processNodeString(nodeValue);
		String lastWord = ""; //Used for calculating pair counts
    
		for(String curWord : wordSet) {
			//Check filters
			WordFilter filter = cloud.getNetworkParams().getFilter();
			if (!filter.contains(curWord)) {

				int curCount = 0; 
				if (selectedCounts.containsKey(curWord))
					curCount = selectedCounts.get(curWord);
				curCount = curCount + 1;
				selectedCounts.put(curWord, curCount);
			
				//Add to pair counts
				if (!lastWord.equals("")) {
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
			
				lastWord = curWord;
			}
		}
	}
	
	
	/**
	 * Sets the mean weight value to be the average of all ratios if a network normalization
	 * factor of 0 were to be used.  The values are also translated so the min value is 0.
	 */
	private void calculateWeights()
	{
		double curMin = 0.0;
		double curMax = 0.0;
		double total = 0.0;
		int count = 0;
		
		final int selTotal = cloud.getSelectedNumNodes();
		
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
			int selCount = entry.getValue();
			int netCount = networkCounts.get(curWord);
			double newNetCount = Math.pow(netCount, 0.0);
			int netTotal = cloud.getNetworkNumNodes();
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
		this.minWeight = curMin;
		this.meanWeight = total/count;
		this.maxWeight = curMax;
	}
	
	/**
	 * Calculates ratios given the current selectedNode counts.
	 */
	private void updateRatios()
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
		
		final int selTotal = cloud.getSelectedNumNodes();
		
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
			int selCount = entry.getValue();
			int netCount = networkCounts.get(curWord);
			double newNetCount = Math.pow(netCount, cloud.getNetWeightFactor());
			int netTotal = cloud.getNetworkNumNodes();
			double newNetTotal = Math.pow(netTotal,  cloud.getNetWeightFactor());
			
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
		
		this.maxRatio = curMax;
		this.minRatio = curMin;
		this.meanRatio = total/count;
		
		//PAIR COUNTS
		//Clear old counts
		this.pairRatios = new HashMap<WordPair, Double>();
		
		int netTotal = cloud.getNetworkNumNodes();
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
			
			int selPairCount = entry.getValue();
			int netPairCount = networkPairCounts.get(pair);
			double newNetCount = Math.pow(netPairCount, cloud.getNetWeightFactor());
			double newNetTotal = Math.pow(netTotal, cloud.getNetWeightFactor());
			
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
	 * 
	 * This method essentially initializes this object, call before calling any getters.
	 * 
	 */
	public void calculateFontSizes()
	{
		if (!ratiosInitialized)
			this.updateRatios();
		
		//Clear old fonts
		this.cloudWords = new ArrayList<CloudWordInfo>();
		
		if (cloud.getDisplayStyle().equals(CloudDisplayStyles.NO_CLUSTERING))
		{
			for (Entry<String, Double> entry : ratios.entrySet())
			{
				String curWord = entry.getKey();
				int fontSize = calculateFontSize(curWord, entry.getValue());
				CloudWordInfo curInfo = new CloudWordInfo(this, curWord, fontSize);
				cloudWords.add(curInfo);
			}//end while loop
			
			//Sort cloudWords in order by fontsize
			Collections.sort(cloudWords);
		}
		else
		{
			ClusterBuilder builder = new ClusterBuilder(this); // half the time here
			builder.clusterData(cloud.getClusterCutoff()); // other half the time here
			builder.clusterData(0.0);
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
	public int calculateFontSize(String aWord, double ratio) {
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
		int maxFont = MAXFONTSIZE;
		int minFont = MINFONTSIZE;
		
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
		double EPSILON = 0.00001;
		return Math.abs(d1 - d2) <= EPSILON * Math.max(Math.abs(d1), Math.abs(d2));
	}
	
	
	/**
	 * This method takes in the ID of a node and returns the string that is associated
	 * with that node and the current attribute of this CloudParameters.
	 * 
	 * 
	 */
	private String getNodeAttributeVal(CyNetwork network, CyNode curNode, String attributeName) {
		CyTable table = network.getDefaultNodeTable();
		CyColumn column = table.getColumn(attributeName);
		if (column == null) {
			return null;
		}
		if (column.getType().equals(String.class)) {
			return table.getRow(curNode.getSUID()).get(attributeName, String.class);
		}
		if (column.getType().equals(List.class) && column.getListElementType().equals(String.class)) {
			List<String> list = table.getRow(curNode.getSUID()).getList(attributeName, String.class);
			return list == null ? null : join(" ", list);
		}
		return null;
	}
	
	
	private static String join(String delimiter, List<String> list) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = list.iterator();
		if(iter.hasNext())
			sb.append(iter.next());
		while(iter.hasNext())
			sb.append(delimiter).append(iter.next());
		return sb.toString();
	}
	
	public int getCountTotal()	 {
		if(countTotal == 0) {
			// Cache the total once so it doesn't have to be repeatedly recalculated
			Map<String, Integer> selectedCounts = getSelectedCounts();
			int total = 0;
			for(int x : selectedCounts.values()) {
				total += x;
			}
			countTotal = total;
		}
		return countTotal;
	}
	
	
	public String getCloudName() {
		return cloud.getCloudName();
	}
	
	public List<CloudWordInfo> getCloudWordInfoList() {
		return cloudWords;
	}
	
	public Map<String,Double> getRatios() {
		return ratios;
	}
	
	public Map<WordPair,Double> getPairRatios() {
		return pairRatios;
	}
	
	public Map<String,Integer> getSelectedCounts() {
		return selectedCounts;
	}
	
	public Map<WordPair,Integer> getSelectedPairCounts() {
		return selectedPairCounts;
	}
	
	public Map<String, Set<CyNode>> getStringNodeMapping() {
		return stringNodeMapping;
	}
	
	public double getMinRatio() {
		return minRatio;
	}
	
	public double getMaxRatio() {
		return maxRatio;
	}
}
