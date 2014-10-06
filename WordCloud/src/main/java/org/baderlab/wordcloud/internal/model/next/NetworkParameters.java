package org.baderlab.wordcloud.internal.model.next;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.wordcloud.internal.Constants;
import org.baderlab.wordcloud.internal.IoUtil;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class NetworkParameters {

	private final CloudModelManager parent;
	private final CyNetwork network;
	
	private Map<String, CloudParameters> clouds = new HashMap<String, CloudParameters>();
	
	//Name creation variables
	protected static final String CLOUDNAME = "Cloud";
	protected static final String SEPARATER = "_";
	
	protected static final int NULL_COUNT = -99;
	protected static final String NULL_NAME = "null";
	
	//Font Size Values
	protected static final Integer MINFONTSIZE = 12; 
	protected static final Integer MAXFONTSIZE = 64;
	
	//Filter stuff
	private WordFilter filter;
	private WordDelimiters delimiters;
	
	
	
	protected NetworkParameters(CloudModelManager parent, CyNetwork network) {
		this.parent = parent;
		this.network = network;
		this.delimiters = new WordDelimiters(); // default
		StreamUtil streamUtil = parent.getStreamUtil();
		this.filter = new WordFilter(new IoUtil(streamUtil)); // default
		
		if(network != null) {
			parent.initializeCloudMetadata(network);
		}
	}
	
	
	/**
	 * Returns the clouds sorted by cloud num.
	 */
	public List<CloudParameters> getClouds() {
		List<CloudParameters> result = new ArrayList<CloudParameters>(clouds.values());
		Collections.sort(result);
		return result;
	}
	
	public CloudParameters getCloud(String cloudName) {
		return clouds.get(cloudName);
	}
	
	public CloudParameters getFirstCloud() {
		List<CloudParameters> list = getClouds();
		return (list.isEmpty() ? null : list.get(0));
	}
	
	
	/**
	 * Returns a special "Null" cloud that is not contained in the main list of clouds.
	 * This is mainly for convenience, so that methods in the UI don't need special cases to handle
	 * when a network has an empty list of clouds. Also it makes it easier to implement
	 * features like "sync with selection" which don't require that a cloud be created first.
	 */
	public CloudParameters getNullCloud() {
		return createCloudParameters(Collections.<CyNode>emptySet(), NULL_COUNT, NULL_NAME);
	}
	
	private CloudParameters createCloudParameters(Set<CyNode> nodes, int count, String name) {
		List<String> attributes = null;
		if(network == null)
			attributes = null;
		else
			attributes = CloudModelManager.getColumnNames(network, CyNode.class);
		
		CloudParameters cloudParams = new CloudParameters(this);
		cloudParams.setCloudNum(count);
		cloudParams.setCloudName(name);
		cloudParams.setSelectedNodes(nodes);
		cloudParams.setAttributeNames(attributes);
		cloudParams.updateRatios();
		cloudParams.calculateFontSizes();
		return cloudParams;
	}
	
	
	/**
	 * Creates a new CloudParameters object for this network.
	 * @param nodes It is assumed that all the nodes in the list are part of this network.
	 */
	public CloudParameters createCloud(Set<CyNode> nodes) {
		return createCloud(nodes, getNextCloudName());
	}
	
	/**
	 * Creates a new CloudParameters object for this network.
	 * @param nodes It is assumed that all the nodes in the list are part of this network.
	 */
	public CloudParameters createCloud(Set<CyNode> nodes, String cloudName) {
		CloudParameters cloudParams = createCloudParameters(nodes, getCloudCount(), cloudName);
		clouds.put(cloudParams.getCloudName(), cloudParams);
		parent.fireCloudAdded(cloudParams);
		return cloudParams;
	}
	
	/**
	 * Creates a new CloudParameters object for this network by reading the given
	 * properties file (which is entirely contained in the string parameter).
	 */
	public CloudParameters createCloud(String propFile) {
		CloudParameters cloudParams = new CloudParameters(this, propFile);
		clouds.put(cloudParams.getCloudName(), cloudParams);
		parent.fireCloudAdded(cloudParams);
		return cloudParams;
	}
	
	
	/**
	 * Called buy {@link CloudParameters#delete()} to remove the mapping.
	 */
	protected void removeCloudMapping(CloudParameters cloud) {
		clouds.remove(cloud.getCloudName());
	}
	
	/**
	 * Called by {@link CloudParameters#rename(String)} to change the name mapping.
	 */
	protected void changeCloudMapping(String oldName, String newName) {
		clouds.put(newName, clouds.remove(oldName));
	}
	
	
	
	public Integer getCloudCount() {
		return network.getRow(network).get(Constants.CLOUD_COUNTER, Integer.class);
	}
	
	
	public boolean containsCloud(String name) {
		return clouds.containsKey(name);		
	}
	
	
	public void incrementCloudCounter(CyNetwork network) {
		int count = getCloudCount();
		network.getRow(network).set(Constants.CLOUD_COUNTER, count + 1);
	}
	
	/**
	 * Returns the name for the next cloud for this network.
	 * @return String - name of the next cloud
	 */
	public String getNextCloudName() {
		int cloudCount = getCloudCount();
		String name = CLOUDNAME + SEPARATER + cloudCount;
		incrementCloudCounter(network);
		return name;
	}
	
	public CyNetwork getNetwork() {
		return network;
	}
	
	public CloudModelManager getManager() {
		return parent;
	}
	
	
	public void setFilter(WordFilter filter) {
		this.filter = filter;
	}
	
	public WordFilter getFilter() {
		return filter;
	}
	
	public void setDelimeters(WordDelimiters delimiters) {
		this.delimiters = delimiters;
	}
	
	public WordDelimiters getDelimeters() {
		return delimiters;
	}
	
	public boolean getIsStemming() {
		return network.getRow(network).get(Constants.USE_STEMMING, Boolean.class);
	}
	
	public void setIsStemming(boolean val) {
		network.getRow(network).set(Constants.USE_STEMMING, val);
	}
	
	public String getNetworkName() {
		if (network == null) {
			return "No Network Loaded";
		}
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}


	
	
	
}
