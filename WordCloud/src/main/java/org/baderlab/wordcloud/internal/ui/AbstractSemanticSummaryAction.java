package org.baderlab.wordcloud.internal.ui;

import org.cytoscape.application.swing.AbstractCyAction;

public abstract class AbstractSemanticSummaryAction extends AbstractCyAction {
	public AbstractSemanticSummaryAction(String name) {
		super(name);
	}
	
	protected PanelActivateAction pluginAction;
	
	public void setSemanticSummaryPluginAction(PanelActivateAction action) {
		pluginAction = action;
	}
	

	public PanelActivateAction getSemanticSummaryPluginAction() {
		return pluginAction;
	}
}
