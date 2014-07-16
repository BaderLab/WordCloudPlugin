package org.baderlab.wordcloud.internal.command;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.baderlab.wordcloud.internal.CreateCloudCommandAction;
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

public class BuildWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private CreateCloudCommandAction createCloudCommandAction;
	private SemanticSummaryParametersFactory parametersFactory;
	private CyTableManager tableManager;
	private CyTableFactory tableFactory;
	
	@Tunable(description="Column with clusters")
	public String clusterColumnName;

	@Tunable(description="Column with gene set names")
	public String nameColumnName;
	
	@Tunable(description="Prefix before cloud names")
	public String cloudNamePrefix = "";

	
	public BuildWordCloudCommandHandlerTask(CyApplicationManager applicationManager,
			CySwingApplication application, SemanticSummaryManager cloudManager,
			CreateCloudCommandAction createCloudCommandAction, SemanticSummaryParametersFactory parametersFactory, CyTableManager tableManager, CyTableFactory tableFactory) {
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.createCloudCommandAction = createCloudCommandAction;
		this.parametersFactory = parametersFactory;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
	}
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		CyNetwork network = applicationManager.getCurrentNetwork();
		HashMap<Integer, ArrayList<CyRow>> clusters = new HashMap<Integer, ArrayList<CyRow>>();
		for (CyRow row : network.getDefaultNodeTable().getAllRows()) {
			Integer clusterNumber = row.get(clusterColumnName, Integer.class);
			if (clusterNumber != null) {
				if (!clusters.containsKey(clusterNumber)) {
					clusters.put(clusterNumber, new ArrayList<CyRow>());
				}
				clusters.get(clusterNumber).add(row);	
			}
		}
		CyTable clusterTable = tableFactory.createTable(cloudNamePrefix + " Table", "Cluster number", Integer.class, true, true);
		createColumn(clusterTable, "WC_Word");
		createColumn(clusterTable, "WC_FontSize");
		createColumn(clusterTable, "WC_Cluster");
		createColumn(clusterTable, "WC_Number");
		tableManager.addTable(clusterTable);
		if (network.getDefaultNetworkTable().getColumn(cloudNamePrefix) == null) {
			network.getDefaultNetworkTable().createColumn(cloudNamePrefix, Long.class, false);		
		}
		network.getRow(network).set(cloudNamePrefix, clusterTable.getSUID());
		
		CyTable nodeTable = network.getDefaultNodeTable();
		createColumn(nodeTable, cloudNamePrefix + "WC_Word");
		createColumn(nodeTable, cloudNamePrefix + "WC_FontSize");
		createColumn(nodeTable, cloudNamePrefix + "WC_Cluster");
		createColumn(nodeTable, cloudNamePrefix + "WC_Number");
		
		for (Integer clusterNumber : clusters.keySet()) {
			ArrayList<CyRow> rows = clusters.get(clusterNumber);
			selectNodes(rows);
			createCloudCommandAction.setAttributeColumn(nameColumnName);
			createCloudCommandAction.setClusterColumn(clusterColumnName);
			createCloudCommandAction.setCloudNamePrefix(cloudNamePrefix);
			createCloudCommandAction.setClusterNumber(clusterNumber);
			createCloudCommandAction.setClusterTable(clusterTable);
			createCloudCommandAction.actionPerformed(new ActionEvent("", 0, ""));
			deselectNodes(rows);
		}
	}
	
	public void createColumn(CyTable nodeTable, String columnName) {
		CyColumn column = nodeTable.getColumn(columnName);
		if (column != null) {
			nodeTable.deleteColumn(columnName);
		}
		nodeTable.createListColumn(columnName, String.class, false);
	}
	
	public void selectNodes(ArrayList<CyRow> cluster) {
		for (CyRow row : cluster) {
			row.set(CyNetwork.SELECTED, true);
		}
	}
	
	public void deselectNodes(ArrayList<CyRow> cluster) {
		for (CyRow row : cluster) {
			row.set(CyNetwork.SELECTED, false);
		}
	}
}
