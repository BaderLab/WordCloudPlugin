package org.baderlab.wordcloud.internal.command;

import javax.swing.JList;

import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SelectWordCloudCommandHandlerTask implements Task {

	private CyApplicationManager applicationManager;
	private SemanticSummaryManager cloudManager;

	@Tunable(description="Name of cloud to be selected")
	public String cloudName = "";
	
	public SelectWordCloudCommandHandlerTask(
			CyApplicationManager applicationManager,
			SemanticSummaryManager cloudManager) {
		
		this.applicationManager = applicationManager;
		this.cloudManager = cloudManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SemanticSummaryParameters params = cloudManager.getParameters(applicationManager.getCurrentNetwork());
		cloudManager.setCurCloud(params.getCloud(cloudName));
		JList cloudList = cloudManager.getInputWindow().getCloudList();
		for (int i = 0; i < cloudList.getModel().getSize(); i++) {
			if (cloudList.getModel().getElementAt(i).equals(cloudName)) {
				cloudList.setSelectedIndex(i);
				return;
			}
		}
	}

	@Override
	public void cancel() {
		return;
	}

}
