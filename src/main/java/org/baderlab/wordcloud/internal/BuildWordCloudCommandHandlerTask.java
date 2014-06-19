package org.baderlab.wordcloud.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class BuildWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private CreateCloudAction createCloudAction;
	private SemanticSummaryParametersFactory parametersFactory;
	
	@Tunable(description="Column with clusters")
	public String clusterColumnName;

	@Tunable(description="Column with gene set names")
	public String nameColumnName;

	
	public BuildWordCloudCommandHandlerTask(CyApplicationManager applicationManager,
			CySwingApplication application, SemanticSummaryManager cloudManager,
			CreateCloudAction createCloudAction, SemanticSummaryParametersFactory parametersFactory) {
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.createCloudAction = createCloudAction;
		this.parametersFactory = parametersFactory;
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
		for (ArrayList<CyRow> rows : clusters.values()) {
			selectNodes(rows);
			createCloudAction.setAttributeColumn(nameColumnName);
			createCloudAction.actionPerformed(new ActionEvent("", 0, ""));
			deselectNodes(rows);
		}
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
