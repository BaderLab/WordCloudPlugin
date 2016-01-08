package org.baderlab.wordcloud.internal.command;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class GetVersionCommandHandlerTaskFactory implements TaskFactory {

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GetVersionCommandHandlerTask());
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
