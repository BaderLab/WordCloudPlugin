package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.DeleteCloudAction;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParametersFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class DeleteWordCloudCommandHandlerTaskFactory implements TaskFactory{

	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private DeleteCloudAction deleteCloudAction;

	public DeleteWordCloudCommandHandlerTaskFactory(
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
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DeleteWordCloudCommandHandlerTask(applicationManager, application, cloudManager, deleteCloudAction));
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
