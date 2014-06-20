package org.baderlab.wordcloud.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class BuildWordCloudCommandHandlerTaskFactory implements TaskFactory{
	
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private CreateCloudNoDisplayAction createCloudNoDisplayAction;

	// I'll probably have to specify more (like Network)

	public BuildWordCloudCommandHandlerTaskFactory(CyApplicationManager applicationManager,
			CySwingApplication application, SemanticSummaryManager cloudManager,
			CreateCloudNoDisplayAction createCloudNoDisplayAction, SemanticSummaryParametersFactory parametersFactory) {
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.createCloudNoDisplayAction = createCloudNoDisplayAction;
		this.parametersFactory = parametersFactory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new BuildWordCloudCommandHandlerTask(applicationManager, application, cloudManager, createCloudNoDisplayAction, parametersFactory));
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

}
