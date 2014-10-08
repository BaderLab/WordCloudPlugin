package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateWordCloudCommandHandlerTaskFactory implements TaskFactory{
	
	private final CyApplicationManager applicationManager;
	private final CyTableManager tableManager;
	private final CyTableFactory tableFactory;
	private final CloudModelManager cloudManager;

	// I'll probably have to specify more (like Network)

	public CreateWordCloudCommandHandlerTaskFactory(CyApplicationManager applicationManager,
			CySwingApplication application, CloudModelManager cloudManager,
			CyTableManager tableManager, CyTableFactory tableFactory) {
		this.applicationManager = applicationManager;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
		this.cloudManager = cloudManager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateWordCloudCommandHandlerTask(applicationManager, cloudManager, tableManager, tableFactory));
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
