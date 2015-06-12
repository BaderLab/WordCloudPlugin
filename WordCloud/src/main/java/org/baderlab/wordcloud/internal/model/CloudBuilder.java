package org.baderlab.wordcloud.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.baderlab.wordcloud.internal.cluster.CloudDisplayStyles;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;


/**
 * Builder object for creating instances of CloudParameters.
 * 
 * @see NetworkParameters#getCloudBuilder()
 */
public class CloudBuilder {

	private final NetworkParameters networkParams;
	
	// All of the defaults are valid
	
	private String cloudName = null;
	
	private List<CyNode> nodes = Collections.emptyList();
	private List<String> attributeNames = Collections.emptyList();
	
	private CloudDisplayStyles displayStyle = CloudDisplayStyles.getDefault();
	
	private int maxWords = CloudParameters.DEFAULT_MAX_WORDS;
	private double clusterCutoff =  CloudParameters.DEFAULT_CLUSTER_CUTOFF;
	private double netWeightFactor = CloudParameters.DEFAULT_NET_WEIGHT;
	private int minWordOccurrence = CloudParameters.DEFAULT_MIN_OCCURRENCE;
	
	private String clusterColumnName = null;
	private CyTable clusterTable = null;
	
	
	CloudBuilder(NetworkParameters network) {
		this.networkParams = network;
	}
	
	public CloudParameters build() {
		return networkParams.createCloud(this);
	}

	
	/**
	 * Copies all the fields from the given cloud except its name.
	 * @throws IllegalArgumentException 
	 */
	public CloudBuilder copyFrom(CloudParameters other) {
		setNodes(other.getSelectedNodes());
		setAttributes(other.getAttributeNames());
		setDisplayStyle(other.getDisplayStyle());
		setMaxWords(other.getMaxWords());
		setClusterCutoff(other.getClusterCutoff());
		setNetWeightFactor(other.getNetWeightFactor());
		setMinWordOccurrence(other.getMinWordOccurrence());
		setClusterColumnName(other.getClusterColumnName());
		setClusterTable(other.getClusterTable());
		return this;
	}
	
	/**
	 * Some of the fields of the builder can be overridden by cyProperties.
	 */
	public CloudBuilder applyOverrideProperties() {
		CloudModelManager manager = networkParams.getManager();
		if(manager.overrideNetWeightProperty()) {
			setNetWeightFactor(manager.getNetWeightPropertyValue());
		}
		return this;
	}
	
	public CloudBuilder setName(String cloudName) {
		this.cloudName = cloudName;
		return this;
	}


	public CloudBuilder setNodes(Collection<CyNode> nodes) {
		if(nodes == null)
			this.nodes = Collections.emptyList();
		else
			this.nodes = new ArrayList<CyNode>(nodes);
		return this;
	}

	public CloudBuilder setAttributes(Collection<String> attributeNames) {
		if(attributeNames == null)
			this.attributeNames = Collections.emptyList();
		else
			this.attributeNames = new ArrayList<String>(attributeNames);
		return this;
	}
	
	public CloudBuilder setAllAttributes() {
		this.attributeNames = new ArrayList<String>();
		this.attributeNames.addAll(CloudModelManager.getColumnNames(networkParams.getNetwork(), CyNode.class));
		return this;
	}

	public CloudBuilder setDisplayStyle(CloudDisplayStyles displayStyle) {
		this.displayStyle = displayStyle;
		return this;
	}

	public CloudBuilder setMaxWords(int maxWords) {
		this.maxWords = maxWords;
		return this;
	}

	public CloudBuilder setClusterCutoff(double clusterCutoff) {
		this.clusterCutoff = clusterCutoff;
		return this;
	}

	public CloudBuilder setNetWeightFactor(double netWeightFactor) {
		this.netWeightFactor = netWeightFactor;
		return this;
	}

	public CloudBuilder setMinWordOccurrence(int minWordOccurrence) {
		this.minWordOccurrence = minWordOccurrence;
		return this;
	}

	public CloudBuilder setClusterColumnName(String clusterColumnName) {
		this.clusterColumnName = clusterColumnName;
		return this;
	}

	public CloudBuilder setClusterTable(CyTable clusterTable) {
		this.clusterTable = clusterTable;
		return this;
	}
	
	
	public Collection<CyNode> getNodes() {
		return nodes;
	}

	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public NetworkParameters getNetworkParams() {
		return networkParams;
	}

	public String getName() {
		return cloudName;
	}

	public CloudDisplayStyles getDisplayStyle() {
		return displayStyle;
	}

	public int getMaxWords() {
		return maxWords;
	}

	public double getClusterCutoff() {
		return clusterCutoff;
	}

	public double getNetWeightFactor() {
		return netWeightFactor;
	}

	public int getMinWordOccurrence() {
		return minWordOccurrence;
	}

	public String getClusterColumnName() {
		return clusterColumnName;
	}

	public CyTable getClusterTable() {
		return clusterTable;
	}
}
