package org.baderlab.wordcloud.internal.command;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class GetVersionCommandTaskFactory implements TaskFactory {

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GetVersionCommandTask());
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
