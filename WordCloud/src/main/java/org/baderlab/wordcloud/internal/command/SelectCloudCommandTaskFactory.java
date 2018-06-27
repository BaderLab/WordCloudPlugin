package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class SelectCloudCommandTaskFactory implements TaskFactory{

private UIManager uiManager;
	
	public SelectCloudCommandTaskFactory(UIManager uiManager) {
		this.uiManager = uiManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SelectCloudCommandTask(uiManager));
	}

	@Override
	public boolean isReady() {
		return true;
	}
	
}
