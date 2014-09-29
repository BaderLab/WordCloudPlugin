package org.baderlab.wordcloud.internal.command;

import java.awt.event.ActionEvent;

import org.baderlab.wordcloud.internal.model.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.model.SemanticSummaryParameters;
import org.baderlab.wordcloud.internal.ui.DeleteCloudAction;
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
		while (true) {
			try {
				cloudManager.setCurCloud(params.getCloud(cloudName));
				deleteCloudAction.actionPerformed(new ActionEvent("", 1, "No confirmation"));
				break;
			} catch (Exception e) {
				// If user has deleted the cloud already there will be a null pointer
				continue;
			}
		}
	}

	@Override
	public void cancel() {
		return;
	}

}
