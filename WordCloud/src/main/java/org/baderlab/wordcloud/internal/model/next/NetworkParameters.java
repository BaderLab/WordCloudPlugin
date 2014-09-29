package org.baderlab.wordcloud.internal.model.next;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.wordcloud.internal.Constants;
import org.baderlab.wordcloud.internal.IoUtil;
import org.baderlab.wordcloud.internal.ModelManager;
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
		StreamUtil streamUtil = parent.getModelManager().getStreamUtil();
		this.filter = new WordFilter(new IoUtil(streamUtil)); // default
		
		ModelManager modelManager = parent.getModelManager();
		if (!modelManager.hasCloudMetadata(network)) {
			modelManager.createCloudMetadata(network);
		}
	}
	
	public boolean hasNetworkChanged() {
		return parent.getModelManager().hasChanges(network);
	}
	
	
	/**
	 * Creates a new CloudParameters object for this network.
	 * @param nodes It is assumed that all the nodes in the list are part of this network.
	 */
	public CloudParameters createCloudParameters(Set<CyNode> nodes) {
		CloudParameters cloudParams = new CloudParameters(this);
		List<String> attributes = CloudModelManager.getColumnNames(network, CyNode.class);
		cloudParams.setCloudNum(getCloudCount());
		cloudParams.setCloudName(getNextCloudName());
		cloudParams.setSelectedNodes(nodes);
		cloudParams.setAttributeNames(attributes);
		cloudParams.updateRatios();
		cloudParams.calculateFontSizes();
		clouds.put(cloudParams.getCloudName(), cloudParams);
		
		parent.fireCloudAdded(cloudParams);
		
		return cloudParams;
	}
	
	public Integer getCloudCount() {
		return network.getRow(network).get(Constants.CLOUD_COUNTER, Integer.class);
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
