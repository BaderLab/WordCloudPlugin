package org.baderlab.wordcloud.internal.command;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.baderlab.wordcloud.internal.CreateCloudCommandAction;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParametersFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class BuildWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private CreateCloudCommandAction createCloudCommandAction;
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
		this.createCloudCommandAction = createCloudCommandAction;
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
		// Get rows for each cluster based on the specified cluster column
		HashMap<Integer, ArrayList<CyRow>> clusters = new HashMap<Integer, ArrayList<CyRow>>();
		CyColumn clusterColumn = network.getDefaultNodeTable().getColumn(clusterColumnName);
		Class<?> columnType = clusterColumn.getType();
		if (columnType == Integer.class) {
			// If cluster column contains integers
			for (CyRow row : network.getDefaultNodeTable().getAllRows()) {	
				Integer clusterNumber = row.get(clusterColumnName, Integer.class);
				if (clusterNumber != null) {
					if (!clusters.containsKey(clusterNumber)) {
						clusters.put(clusterNumber, new ArrayList<CyRow>());
					}
					clusters.get(clusterNumber).add(row);	
				}
			}			
		} else if (columnType == List.class && clusterColumn.getListElementType() == Integer.class) {
			// If cluster column contains lists of integers
			for (CyRow row : network.getDefaultNodeTable().getAllRows()) {	
				@SuppressWarnings("unchecked")
				List<Integer> clusterNumbers = row.get(clusterColumnName, List.class);
				for (int clusterNumber : clusterNumbers) {
					if (!clusters.containsKey(clusterNumber)) {
						clusters.put(clusterNumber, new ArrayList<CyRow>());
					}
					clusters.get(clusterNumber).add(row);
				}
			}
		}
		// Create table to add WordInfos to
		CyTable clusterTable = tableFactory.createTable(cloudNamePrefix + " Table", "Cluster number", Integer.class, true, true);
		createColumn(clusterTable, "WC_Word");
		createColumn(clusterTable, "WC_FontSize");
		createColumn(clusterTable, "WC_Cluster");
		createColumn(clusterTable, "WC_Number");
		tableManager.addTable(clusterTable);
		// Store table ID in the network table
		if (network.getDefaultNetworkTable().getColumn(cloudNamePrefix) == null) {
			network.getDefaultNetworkTable().createColumn(cloudNamePrefix, Long.class, false);		
		}
		network.getRow(network).set(cloudNamePrefix, clusterTable.getSUID());
		
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
