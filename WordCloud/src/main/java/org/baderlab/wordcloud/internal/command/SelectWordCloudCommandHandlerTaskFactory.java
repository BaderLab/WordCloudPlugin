package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class SelectWordCloudCommandHandlerTaskFactory implements TaskFactory{

	private CyApplicationManager applicationManager;
	private SemanticSummaryManager cloudManager;

	public SelectWordCloudCommandHandlerTaskFactory(
			CyApplicationManager applicationManager,
			SemanticSummaryManager cloudManager) {
		
		this.applicationManager = applicationManager;
		this.cloudManager = cloudManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SelectWordCloudCommandHandlerTask(applicationManager, cloudManager));
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
