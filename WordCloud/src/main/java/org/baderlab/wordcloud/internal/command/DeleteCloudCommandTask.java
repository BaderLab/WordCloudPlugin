package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DeleteCloudCommandTask implements Task {

	private UIManager uiManager;

	@Tunable(description="Name of cloud to be destroyed")
	public String cloudName = "";
	
	public DeleteCloudCommandTask(UIManager uiManager) {
		this.uiManager = uiManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		NetworkParameters networkParams = uiManager.getCurrentNetwork();
		if(networkParams != null) {
			CloudParameters cloudParams = networkParams.getCloud(cloudName);
			if(cloudParams != null) {
				cloudParams.delete();
			}
			else {
				throw new IllegalArgumentException("cloud not found: '" + cloudName + "'");
			}
		}
	}

	@Override
	public void cancel() {
	}

}
