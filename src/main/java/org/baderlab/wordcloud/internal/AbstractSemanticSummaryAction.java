package org.baderlab.wordcloud.internal;

import org.cytoscape.application.swing.AbstractCyAction;

public abstract class AbstractSemanticSummaryAction extends AbstractCyAction {
	public AbstractSemanticSummaryAction(String name) {
		super(name);
	}
	
	protected SemanticSummaryPluginAction pluginAction;
	
	public void setSemanticSummaryPluginAction(SemanticSummaryPluginAction action) {
		pluginAction = action;
	}
	

	public SemanticSummaryPluginAction getSemanticSummaryPluginAction() {
		return pluginAction;
	}
}
