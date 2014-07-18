package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.DeleteCloudAction;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class DeleteWordCloudCommandHandlerTaskFactory implements TaskFactory{

	private CyApplicationManager applicationManager;
	private SemanticSummaryManager cloudManager;
	private DeleteCloudAction deleteCloudAction;

	public DeleteWordCloudCommandHandlerTaskFactory(
			CyApplicationManager applicationManager,
			SemanticSummaryManager cloudManager,
			DeleteCloudAction deleteCloudAction) {
		
		this.applicationManager = applicationManager;
		this.cloudManager = cloudManager;
		this.deleteCloudAction = deleteCloudAction;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DeleteWordCloudCommandHandlerTask(applicationManager, cloudManager, deleteCloudAction));
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
