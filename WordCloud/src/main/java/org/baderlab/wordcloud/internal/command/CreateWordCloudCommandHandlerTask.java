package org.baderlab.wordcloud.internal.command;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.CreateCloudCommandAction;
import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParametersFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
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
import org.cytoscape.work.util.ListSingleSelection;

public class CreateWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private CreateCloudCommandAction createCloudCommandAction;
	private CyTableManager tableManager;
	private CyTableFactory tableFactory;
	
	@Tunable(description="Column with words")
	public String wordColumnName;
	
	@Tunable(description="Nodes to use")
	public ListSingleSelection nodesToUse = new ListSingleSelection("selected", "all");
	
	@Tunable(description="Cloud name")
	public String cloudName = "";

	@Tunable(description="Cloud group table name")
	public String cloudGroupTableName = "WordCloud Results Table";
	
	public CreateWordCloudCommandHandlerTask(CyApplicationManager applicationManager,
			CySwingApplication application, SemanticSummaryManager cloudManager,
			CreateCloudCommandAction createCloudCommandAction, SemanticSummaryParametersFactory parametersFactory, CyTableManager tableManager, CyTableFactory tableFactory) {
		this.applicationManager = applicationManager;
		this.createCloudCommandAction = createCloudCommandAction;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
	}
	
	@Override
	public void cancel() {
		return;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		CyNetwork network = applicationManager.getCurrentNetwork();
		
		// Get the nodes to use for cloud
		Set<CyNode> nodes = null;
		if (nodesToUse.getSelectedValue().equals("selected")) {
			nodes = SelectionUtils.getSelectedNodes(network);
		} else if (nodesToUse.getSelectedValue().equals("all")) {
			nodes = new HashSet<CyNode>(network.getNodeList());
		}
		
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

		createCloudCommandAction.setAttributeColumns(Arrays.asList(wordColumnName));
		createCloudCommandAction.setCloudName(cloudName);
		createCloudCommandAction.setClusterTable(cloudGroupTable);
		createCloudCommandAction.setNodes(nodes);
		createCloudCommandAction.actionPerformed(new ActionEvent("", 0, ""));
	}
	
	private void createColumn(CyTable nodeTable, String columnName) {
		CyColumn column = nodeTable.getColumn(columnName);
		if (column != null) {
			nodeTable.deleteColumn(columnName);
		}
		nodeTable.createListColumn(columnName, String.class, false);
	}
}
