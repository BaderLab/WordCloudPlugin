package org.baderlab.wordcloud.internal.command;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JList;

import org.baderlab.wordcloud.internal.DeleteCloudAction;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DeleteWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private DeleteCloudAction deleteCloudAction;

	@Tunable(description="Name of cloud to be destroyed")
	public String cloudName = "";
	
	public DeleteWordCloudCommandHandlerTask(
			CyApplicationManager applicationManager,
			CySwingApplication application,
			SemanticSummaryManager cloudManager,
			DeleteCloudAction deleteCloudAction) {
		
		this.applicationManager = applicationManager;
		this.application = application;
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
