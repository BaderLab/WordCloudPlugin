package org.baderlab.wordcloud.internal;

import java.awt.Component;

import org.cytoscape.view.model.CyNetworkViewManager;

public class CloudListSelectionHandlerFactory {
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryPluginAction pluginAction;
	private CyNetworkViewManager viewManager;

	public CloudListSelectionHandlerFactory(SemanticSummaryManager cloudManager, CyNetworkViewManager viewManager) {
		this.cloudManager = cloudManager;
		this.viewManager = viewManager;
	}

	public void setPluginAction(SemanticSummaryPluginAction pluginAction) {
		this.pluginAction = pluginAction;
	}
	
	public CloudListSelectionHandler createHandler(Component parent) {
		return new CloudListSelectionHandler(parent, cloudManager, pluginAction, viewManager);
	}
}
