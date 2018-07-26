package org.baderlab.wordcloud.internal.command;

import static org.baderlab.wordcloud.internal.CyActivator.VERSION;

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
			int[] version = { VERSION.getMajor(), VERSION.getMinor(), VERSION.getMicro() };
			return type.cast(version);
		} 
		if(String.class.equals(type)) {
			return type.cast(VERSION.toString());
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
