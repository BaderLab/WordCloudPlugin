package org.baderlab.wordcloud.internal.ui.input;

import java.awt.Component;

import org.baderlab.wordcloud.internal.model.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.ui.PanelActivateAction;
import org.cytoscape.view.model.CyNetworkViewManager;

public class CloudListSelectionHandlerFactory {
	private SemanticSummaryManager cloudManager;
	private PanelActivateAction pluginAction;
	private CyNetworkViewManager viewManager;

	public CloudListSelectionHandlerFactory(SemanticSummaryManager cloudManager, CyNetworkViewManager viewManager) {
		this.cloudManager = cloudManager;
		this.viewManager = viewManager;
	}

	public void setPluginAction(PanelActivateAction pluginAction) {
		this.pluginAction = pluginAction;
	}
	
	public CloudListSelectionHandler createHandler(Component parent) {
		return new CloudListSelectionHandler(parent, cloudManager, pluginAction, viewManager);
	}
}
