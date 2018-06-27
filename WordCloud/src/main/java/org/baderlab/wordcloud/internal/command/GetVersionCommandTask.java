package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.CyActivator;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class GetVersionCommandTask implements ObservableTask {

	
	@Override
	public void run(TaskMonitor taskMonitor) {
		// Do nothing
	}
	

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(int[].class.equals(type)) {
			int[] version = { CyActivator.VERSION.getMajor(), CyActivator.VERSION.getMinor(), CyActivator.VERSION.getMicro() };
			return type.cast(version);
		}
		return null;
	}
	
	@Override
	public void cancel() {
	}

	
	public static String getDescription() {
		return 	"Return wordcloud app version.<br>" +
				"This is an ObservableTask that returns a result.<br>"+
				"Result type: int[].class. The returned array contains 3 elements: major version, minor version, patch version";
	}
}
