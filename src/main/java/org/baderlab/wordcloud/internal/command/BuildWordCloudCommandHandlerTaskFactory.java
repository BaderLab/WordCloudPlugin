package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.CreateCloudCommandAction;
import org.baderlab.wordcloud.internal.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.SemanticSummaryParametersFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class BuildWordCloudCommandHandlerTaskFactory implements TaskFactory{
	
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private CreateCloudCommandAction createCloudNoDisplayAction;
	private CyTableManager tableManager;
	private CyTableFactory tableFactory;

	// I'll probably have to specify more (like Network)

	public BuildWordCloudCommandHandlerTaskFactory(CyApplicationManager applicationManager,
			CySwingApplication application, SemanticSummaryManager cloudManager,
			CreateCloudCommandAction createCloudNoDisplayAction, SemanticSummaryParametersFactory parametersFactory, CyTableManager tableManager, CyTableFactory tableFactory) {
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.createCloudNoDisplayAction = createCloudNoDisplayAction;
		this.parametersFactory = parametersFactory;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new BuildWordCloudCommandHandlerTask(applicationManager, application, cloudManager, createCloudNoDisplayAction, parametersFactory, tableManager, tableFactory));
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

}
