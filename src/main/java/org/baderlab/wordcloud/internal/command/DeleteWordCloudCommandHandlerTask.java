package org.baderlab.wordcloud.internal.command;

import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionEvent;

import org.baderlab.wordcloud.internal.DeleteCloudAction;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DeleteWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private SemanticSummaryManager cloudManager;
	private DeleteCloudAction deleteCloudAction;

	@Tunable(description="Name of cloud to be destroyed")
	public String cloudName = "";
	
	public DeleteWordCloudCommandHandlerTask(
			CyApplicationManager applicationManager,
			SemanticSummaryManager cloudManager,
			DeleteCloudAction deleteCloudAction) {
		
		this.applicationManager = applicationManager;
		this.cloudManager = cloudManager;
		this.deleteCloudAction = deleteCloudAction;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SemanticSummaryParameters params = cloudManager.getParameters(applicationManager.getCurrentNetwork());
		cloudManager.setCurCloud(params.getCloud(cloudName));
		while (true) {
			try {
				deleteCloudAction.actionPerformed(new  ActionEvent("", 1, "No confirmation"));
				for (int i = 0; i < cloudManager.getInputWindow().getCloudList().getModel().getSize(); i++) {
					if (cloudManager.getInputWindow().getCloudList().getModel().getElementAt(i) == params) {
						cloudManager.getInputWindow().getCloudList().getListSelectionListeners()[0].valueChanged(new ListSelectionEvent("", i,i, false));				
					}
				}
				break;
			} catch (Exception e) {
				continue;
			}
		}
	}

	@Override
	public void cancel() {
		return;
	}

}
