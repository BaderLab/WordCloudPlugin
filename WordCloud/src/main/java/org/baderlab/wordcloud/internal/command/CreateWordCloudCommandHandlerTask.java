package org.baderlab.wordcloud.internal.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.cloud.CloudWordInfo;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private CyTableManager tableManager;
	private CyTableFactory tableFactory;
	private CloudModelManager cloudModelManager;
	
	private CyNetwork network;
	
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
	
	@Tunable(description="Cloud name")
	public String cloudName = "";

	@Tunable(description="Cloud group table name")
	public String cloudGroupTableName = "WordCloud Results Table";
	
	
	public CreateWordCloudCommandHandlerTask(
			CyApplicationManager applicationManager,
			CloudModelManager cloudManager,
			CyTableManager tableManager, 
			CyTableFactory tableFactory) {
		this.applicationManager = applicationManager;
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
		if(cloudName == null || cloudName.trim().equals(""))
			throw new IllegalArgumentException("cloudName is null");
		if(cloudGroupTableName == null || cloudGroupTableName.trim().equals(""))
			throw new IllegalArgumentException("cloudGroupTableName is null");
		if(wordColumnName == null || wordColumnName.trim().equals(""))	
			throw new IllegalArgumentException("wordColumnName is null");
		
		network = applicationManager.getCurrentNetwork();
		Set<CyNode> nodes = new HashSet<CyNode>(nodeList.getValue());
		
		// Get the table to return the results to
		CyTable cloudGroupTable = null;
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
		
		NetworkParameters networkParams = cloudModelManager.addNetwork(network);
		CloudParameters cloudParams = networkParams.createCloud(nodes, cloudName, wordColumnName, cloudGroupTable);
		
		// Add wordInfo to table
		List<CloudWordInfo> wordInfo = cloudParams.getCloudWordInfoList();
		ArrayList<String> WC_Word = new ArrayList<String>();
		ArrayList<String> WC_FontSize = new ArrayList<String>();
		ArrayList<String> WC_Cluster = new ArrayList<String>();
		ArrayList<String> WC_Number = new ArrayList<String>();
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
	
	
	private void createColumn(CyTable nodeTable, String columnName) {
		CyColumn column = nodeTable.getColumn(columnName);
		if (column != null) {
			nodeTable.deleteColumn(columnName);
		}
		nodeTable.createListColumn(columnName, String.class, false);
	}
}
