package org.baderlab.wordcloud.internal.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.wordcloud.internal.cluster.CloudInfo;
import org.baderlab.wordcloud.internal.cluster.CloudWordInfo;
import org.baderlab.wordcloud.internal.model.CloudBuilder;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateCloudCommandTask implements ObservableTask {

	private CyApplicationManager applicationManager;
	private CyTableManager tableManager;
	private CyTableFactory tableFactory;
	private CloudModelManager cloudModelManager;
	private UIManager uiManager;
	private CyNetwork network;
	
	private Map<String, Object> taskResults;
	
	
	@Tunable(description="Column with words")
	public String wordColumnName;
	
	@Tunable(description="Nodes to use")
	public NodeList nodeList = new NodeList(null);
	@Tunable(description="List of Nodes", context="nogui")
	public NodeList getnodeList() {
		if(network ==null) network = applicationManager.getCurrentNetwork();
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}
	
	@Tunable(description="Cloud name ----")
	public String cloudName = "";

	@Tunable(description="Actually create the cloud or just return the results without creating the cloud.")
	public boolean create = true;
	
	
	/**
	 * AutoAnnotate used to get the cloud data by having this command put the
	 * results into an unassigned table. This has been changed so that a table
	 * is not created by default, the table name must be provided or else
	 * the table will not be created.
	 */
	@Tunable(description="Cloud group table name (deprecated)")
	public String cloudGroupTableName = null;
	
	
	public CreateCloudCommandTask(
			CyApplicationManager applicationManager,
			CloudModelManager cloudManager,
			UIManager uiManager,
			CyTableManager tableManager, 
			CyTableFactory tableFactory) {
		this.applicationManager = applicationManager;
		this.uiManager = uiManager;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
		this.cloudModelManager = cloudManager;
	}
	
	@Override
	public void cancel() {
	}

	@Override
	public void run(TaskMonitor monitor) {
		if(nodeList == null || nodeList.getValue() == null)
			throw new IllegalArgumentException("nodeList is null");
		if(cloudName == null || cloudName.trim().isEmpty())
			throw new IllegalArgumentException("cloudName is null");
		if(wordColumnName == null || wordColumnName.trim().isEmpty())	
			throw new IllegalArgumentException("wordColumnName is null");
		if(cloudGroupTableName != null && cloudGroupTableName.trim().isEmpty())
			cloudGroupTableName = null;
		
		network = applicationManager.getCurrentNetwork();
		Set<CyNode> nodes = new HashSet<CyNode>(nodeList.getValue());
		
		// Get the table to return the results to
		CyTable cloudGroupTable = null;
		if(cloudGroupTableName != null) {
			if (network.getDefaultNetworkTable().getColumn(cloudGroupTableName) != null) {
				cloudGroupTable = tableManager.getTable(network.getRow(network).get(cloudGroupTableName, Long.class));
			} else {
				cloudGroupTable = tableFactory.createTable(cloudGroupTableName, "Cloud", String.class, true, true);
				createColumn(cloudGroupTable, "WC_Word");
				createColumn(cloudGroupTable, "WC_FontSize");
				createColumn(cloudGroupTable, "WC_Cluster");
				createColumn(cloudGroupTable, "WC_Number");
				tableManager.addTable(cloudGroupTable);
				// Store table ID in the network table
				network.getDefaultNetworkTable().createColumn(cloudGroupTableName, Long.class, false);
				network.getRow(network).set(cloudGroupTableName, cloudGroupTable.getSUID());
			}
		}
		
		NetworkParameters networkParams = cloudModelManager.addNetwork(network);
		CloudBuilder builder = networkParams.getCloudBuilder();
		
		CloudParameters currentCloud = uiManager.getCurrentCloud();
		if(currentCloud != null) {
			builder.copyFrom(currentCloud); // inherit all the values currently set in the info panel
		}
		
		builder.setName(cloudName)
			   .setNodes(nodes)
			   .setAttributes(Arrays.asList(wordColumnName))
			   .setClusterTable(cloudGroupTable);
			   
		CloudParameters cloudParams;
		if(create)
			cloudParams = builder.build();
		else
			cloudParams = builder.buildFakeCloud();
		
		CloudInfo cloudInfo = cloudParams.calculateCloud();
		List<CloudWordInfo> wordInfo = cloudInfo.getCloudWordInfoList();

		// Prepare results
		Map<String, Object> results = new HashMap<>();
		results.put("name", cloudName);
		results.put("size", wordInfo.size());
		List<String> words = new ArrayList<>();
		List<Integer> fontSizes = new ArrayList<>();
		List<Integer> clusters = new ArrayList<>();
		List<Integer> numbers = new ArrayList<>();
		results.put("words", words);
		results.put("fontSizes", fontSizes);
		results.put("clusters", clusters);
		results.put("numbers", numbers);
		for(CloudWordInfo cloudWord : wordInfo) {
			words.add(cloudWord.getWord());
			fontSizes.add(cloudWord.getFontSize());
			clusters.add(cloudWord.getCluster());
			numbers.add(cloudWord.getWordNumber());
		}
		
		// additional parameters
		results.put("netWeightFactor", cloudParams.getNetWeightFactor());
		results.put("attributeNames", cloudParams.getAttributeNames());
		results.put("displayStyle", cloudParams.getDisplayStyle());
		results.put("maxWords", cloudParams.getMaxWords());
		results.put("clusterCutoff", cloudParams.getClusterCutoff());
		results.put("minWordOccurrence", cloudParams.getMinWordOccurrence());
		results.put("selectedCounts", cloudInfo.getSelectedCounts());
		
		
		this.taskResults = results;
		
		// Add wordInfo to table (only here for backwards compatibility)
		if(cloudGroupTable != null) {
			List<String> WC_Word = new ArrayList<>();
			List<String> WC_FontSize = new ArrayList<>();
			List<String> WC_Cluster = new ArrayList<>();
			List<String> WC_Number = new ArrayList<>();
			for (CloudWordInfo cloudWord : wordInfo) {
				String[] split = cloudWord.toSplitString();
				WC_Word.add(split[0]);
				WC_FontSize.add(split[1]);
				WC_Cluster.add(split[2]);
				WC_Number.add(split[3]);
			}
	
			CyRow clusterRow = cloudGroupTable.getRow(cloudName);
			clusterRow.set("WC_Word", WC_Word);
			clusterRow.set("WC_FontSize", WC_FontSize);
			clusterRow.set("WC_Cluster", WC_Cluster);
			clusterRow.set("WC_Number", WC_Number);
		}
	}
	
	
	private void createColumn(CyTable nodeTable, String columnName) {
		CyColumn column = nodeTable.getColumn(columnName);
		if (column != null) {
			nodeTable.deleteColumn(columnName);
		}
		nodeTable.createListColumn(columnName, String.class, false);
	}
	
	
	public static String getDescription() {
		return 	"Creates a Word Cloud from a list of nodes.<br>" +
				"This is an ObservableTask that returns a result.<br>"+
				"Result type: Map.class. The map contains 4 parallel lists.<br>" +
				"Key: \"name\", Value: String, Cloud name.<br>" +
				"Key: \"size\", Value: Integer, Number of words in the cloud (and size of each List).<br>" +
				"Key: \"words\", Value: List&lt;String&gt;, List of words in the cloud.<br>" +
				"Key: \"fontSizes\", Value: List&lt;Integer&gt;, The font size of each word.<br>" +
				"Key: \"clusters\", Value: List&lt;Integer&gt;, An ID for the cluster that the word belongs to.<br>" +
				"Key: \"numbers\", Value: List&lt;Integer&gt;, A unique ID for each word.<br>";
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(Map.class.equals(type)) {
			return type.cast(taskResults);
		}
		return null;
	}
}
