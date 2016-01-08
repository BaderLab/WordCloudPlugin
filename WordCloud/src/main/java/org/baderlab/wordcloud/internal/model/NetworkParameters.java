package org.baderlab.wordcloud.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.wordcloud.internal.Constants;
import org.baderlab.wordcloud.internal.IoUtil;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class NetworkParameters {

	private final CloudModelManager parent;
	private final CyNetwork network;
	
	private Map<String, CloudParameters> clouds = new HashMap<String, CloudParameters>();
	private CloudParameters nullCloud;
	
	//Name creation variables
	protected static final String CLOUDNAME = "Cloud";
	protected static final String SEPARATER = "_";
	
	protected static final int NULL_COUNT = -99;
	protected static final String NULL_NAME = "wordcloud.sync";
	
	
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
	
	public void updateAllClouds() {
		for(CloudParameters cloud : clouds.values()) {
			cloud.invalidate();
		}
	}
	
	public CloudParameters getCloud(String cloudName) {
		return clouds.get(cloudName);
	}
	
	public CloudParameters getFirstCloud() {
		List<CloudParameters> list = getClouds();
		return (list.isEmpty() ? null : list.get(0));
	}
	
	
	public CloudBuilder getCloudBuilder() {
		if(isNullNetwork())
			throw new IllegalStateException("Cannot create a cloud for a null network");
		CloudBuilder builder = new CloudBuilder(this);
		
		return builder;
	}
	
	
	/**
	 * Returns a special "Null" cloud that is not contained in the main list of clouds.
	 * This is mainly for convenience, so that methods in the UI don't need special cases to handle
	 * when a network has an empty list of clouds. Also it makes it easier to implement
	 * features like "sync with selection" which don't require that a cloud be created first.
	 */
	public synchronized CloudParameters getNullCloud() {
		if(nullCloud == null) {
			List<String> attributes;
			if(isNullNetwork())
				attributes = Collections.<String>emptyList();
			else
				attributes = CloudModelManager.getColumnNames(network, CyNode.class);
			
			nullCloud = new CloudParameters(this, NULL_NAME, NULL_COUNT);
			nullCloud.setSelectedNodes(Collections.<CyNode>emptySet());
			nullCloud.setAttributeNames(attributes);
			nullCloud.setNetWeightFactor(parent.getNetWeightPropertyValue());
		}
		return nullCloud;
	}
	
	public boolean isNullNetwork() {
		return this == parent.getNullNetwork();
	}
	
	
	protected CloudParameters createCloud(CloudBuilder builder) {
		if(isNullNetwork())
			throw new IllegalStateException("Cannot create a cloud for a null network");
		
		String cloudName = builder.getName();
		if(cloudName == null) {
			cloudName = getNextCloudName();
		}
		else {
			if(clouds.containsKey(cloudName))
				throw new IllegalArgumentException("Cloud name already in use: " + cloudName);
			if(columnAlreadyExists(cloudName))
				throw new IllegalArgumentException("Column name already in use: " + cloudName);
		}
		
		CloudParameters cloudParams = new CloudParameters(this, cloudName, getCloudCount());
		cloudParams.setSelectedNodes(builder.getNodes());
		cloudParams.setAttributeNames(builder.getAttributeNames());
		cloudParams.setDisplayStyle(builder.getDisplayStyle());
		cloudParams.setMaxWords(builder.getMaxWords());
		cloudParams.setClusterCutoff(builder.getClusterCutoff());
		cloudParams.setNetWeightFactor(builder.getNetWeightFactor());
		cloudParams.setMinWordOccurrence(builder.getMinWordOccurrence());
		cloudParams.setClusterColumnName(builder.getClusterColumnName());
		cloudParams.setClusterTable(builder.getClusterTable());
		
		incrementCloudCounter();
		clouds.put(cloudParams.getCloudName(), cloudParams);
		parent.fireCloudAdded(cloudParams);
		return cloudParams;
	}
	
	
	/**
	 * Note this isn't very well tested, don't call from anywhere except the create cloud command handler.
	 */
	protected CloudParameters createFakeCloud(CloudBuilder builder) {
		CloudParameters cloudParams = new CloudParameters(this, "FakeCloud", -1);
		cloudParams.setOverrideNodes(builder.getNodes());
		cloudParams.setAttributeNames(builder.getAttributeNames());
		cloudParams.setDisplayStyle(builder.getDisplayStyle());
		cloudParams.setMaxWords(builder.getMaxWords());
		cloudParams.setClusterCutoff(builder.getClusterCutoff());
		cloudParams.setNetWeightFactor(builder.getNetWeightFactor());
		cloudParams.setMinWordOccurrence(builder.getMinWordOccurrence());
		cloudParams.setClusterColumnName(builder.getClusterColumnName());
		cloudParams.setClusterTable(builder.getClusterTable());
		return cloudParams;
	}
	
	
	
	/**
	 * Creates a new CloudParameters object for this network by reading the given
	 * properties file (which is entirely contained in the string parameter).
	 * 
	 * Warning: Do not call this method directly, it is for restoring
	 * the cloud from the session file.
	 */
	public CloudParameters createCloudFromProperties(String propFile) {
		CloudParameters cloudParams = new CloudParameters(this, propFile);
		clouds.put(cloudParams.getCloudName(), cloudParams);
//		parent.fireCloudAdded(cloudParams);
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
	
	
	
	private int getCloudCount() {
		if(network == null)
			return 0;
		Integer count = network.getRow(network).get(Constants.CLOUD_COUNTER, Integer.class);
		return count == null ? 0 : count;
	}
	
	
	public boolean containsCloud(String name) {
		return clouds.containsKey(name);		
	}
	
	
	private void incrementCloudCounter() {
		int count = getCloudCount();
		network.getRow(network).set(Constants.CLOUD_COUNTER, count + 1);
	}
	
	/**
	 * Returns the name for the next cloud for this network.
	 * Note this method is side effecting, it will return a different name every time it is called.
	 */
	private String getNextCloudName() {
		while(true) {
			int cloudCount = getCloudCount();
			String name = CLOUDNAME + SEPARATER + cloudCount;
			if(!columnAlreadyExists(name)) {
				return name;
			}
			incrementCloudCounter();
		}
	}
	
	protected boolean columnAlreadyExists(String name) {
		CyTable defaultTable = network.getDefaultNodeTable();
		CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		return defaultTable.getColumn(name) != null && localTable.getColumn(name) != null;
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
		if(network == null)
			return false;
		return network.getRow(network).get(Constants.USE_STEMMING, Boolean.class);
	}
	
	public void setIsStemming(boolean val) {
		if(network != null)
			network.getRow(network).set(Constants.USE_STEMMING, val);
	}
	
	public String getNetworkName() {
		if (network == null) {
			return "No Network Loaded";
		}
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
}
