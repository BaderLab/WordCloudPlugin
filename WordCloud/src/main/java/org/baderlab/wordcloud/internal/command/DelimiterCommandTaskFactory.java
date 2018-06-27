package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;

public class DelimiterCommandTaskFactory extends AbstractTaskFactory {

	private final CloudModelManager cloudManager;
	private final UIManager uiManager;
	private final CyApplicationManager appManager;
	private final boolean add;
	private final boolean delimiter;
	
	@Tunable
	public String value;
	
	public DelimiterCommandTaskFactory(CloudModelManager cloudManager, UIManager uiManager, CyApplicationManager appManager, 
			boolean add, boolean delimiter) {
		this.cloudManager = cloudManager;
		this.uiManager = uiManager;
		this.appManager = appManager;
		this.add = add;
		this.delimiter = delimiter;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DelimiterCommandTask(cloudManager, uiManager, appManager, add, delimiter));
	}

}
