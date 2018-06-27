package org.baderlab.wordcloud.internal.command;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.model.WordDelimiters;
import org.baderlab.wordcloud.internal.model.WordFilter;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class DelimiterCommandTask extends AbstractTask {

	private final CloudModelManager cloudManager;
	private final UIManager uiManager;
	private final CyApplicationManager appManager;
	private final boolean add;
	private final boolean delimiter;
	
	@Tunable(description="The delimiter or word to be added or removed")
	public String value;
	
	@Tunable(description="The network to use (optional, will use current network if not specified.", context="nogui")
	public CyNetwork network = null;
	
	
	public DelimiterCommandTask(CloudModelManager cloudManager, UIManager uiManager, CyApplicationManager appManager, 
			boolean add, boolean delimiter) {
		this.cloudManager = cloudManager;
		this.uiManager = uiManager;
		this.appManager = appManager;
		this.add = add;
		this.delimiter = delimiter;
	}

	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(value == null || value.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "value must be provided");
			return;
		}
		
		if(network == null) {
			network = appManager.getCurrentNetwork();
		}
		if(network == null) {
			taskMonitor.showMessage(Level.ERROR, "no current network");
			return;
		}
		
		NetworkParameters networkParameters = cloudManager.addNetwork(network);
		if(networkParameters == null) {
			taskMonitor.showMessage(Level.ERROR, "please create a cloud first");
			return;
		}
		
		
		if(delimiter) {
			WordDelimiters delimiters = networkParameters.getDelimeters();
			if(add) {
				delimiters.addDelimToUse(value);
			} else {
				delimiters.removeDelimiter(value);
			}
		} else {
			WordFilter filter = networkParameters.getFilter();
			if(add) {
				if(!value.matches("[\\w]*")) {
					taskMonitor.showMessage(Level.ERROR, "Word must contain only letters and numbers (no spaces)");
					return;
				}
				filter.add(value);
			} else {
				filter.remove(value);
			}
		}

		networkParameters.updateAllClouds();
		CloudParameters currentCloud = uiManager.getCurrentCloud();
		if(currentCloud != null) {
			for(CloudParameters cloud : networkParameters.getClouds()) {
				if(cloud.equals(currentCloud)) {
					uiManager.getCloudDisplayPanel().updateCloudDisplay(cloud);
				}
			}
		}
	}

}
