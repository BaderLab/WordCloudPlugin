package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.model.next.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DeleteWordCloudCommandHandlerTask implements Task {

	private UIManager uiManager;

	@Tunable(description="Name of cloud to be destroyed")
	public String cloudName = "";
	
	public DeleteWordCloudCommandHandlerTask(UIManager uiManager) {
		this.uiManager = uiManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		NetworkParameters networkParams = uiManager.getCurrentNetwork();
		if(networkParams != null) {
			CloudParameters cloudParams = networkParams.getCloud(cloudName);
			if(cloudParams != null) {
				cloudParams.delete();
			}
		}
	}

	@Override
	public void cancel() {
	}

}
