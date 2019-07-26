package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.command.ShowWordSelectDialogCommand.Type;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ShowWordSelectDialogCommandFactory extends AbstractTaskFactory {
	
	private final CloudModelManager cloudManager;
	private final UIManager uiManager;
	private final CySwingApplication application;
	private final CyApplicationManager appManager;
	private final Type type;
	
	public ShowWordSelectDialogCommandFactory(Type type, CloudModelManager cloudManager, UIManager uiManager, CySwingApplication application, CyApplicationManager appManager) {
		this.type = type;
		this.cloudManager = cloudManager;
		this.uiManager = uiManager;
		this.appManager = appManager;
		this.application = application;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowWordSelectDialogCommand(type, cloudManager, uiManager, application, appManager));
	}

}
