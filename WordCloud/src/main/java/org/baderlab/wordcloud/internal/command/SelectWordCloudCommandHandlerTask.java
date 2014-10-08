package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SelectWordCloudCommandHandlerTask implements Task {

	private final UIManager uiManager;

	@Tunable(description="Name of cloud to be selected")
	public String cloudName = "";
	
	public SelectWordCloudCommandHandlerTask(UIManager uiManager) {
		this.uiManager = uiManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		NetworkParameters networkParams = uiManager.getCurrentNetwork();
		if(networkParams != null) {
			CloudParameters cloud = networkParams.getCloud(cloudName);
			if(cloud != null) {
				uiManager.setCurrentCloud(cloud);
			}
		}
	}

	@Override
	public void cancel() {
	}

}
