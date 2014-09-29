package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.ui.DeleteCloudAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class DeleteWordCloudCommandHandlerTaskFactory implements TaskFactory{

	private SemanticSummaryManager cloudManager;
	private DeleteCloudAction deleteCloudAction;
	private CyApplicationManager applicationManager;

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
