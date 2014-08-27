package org.baderlab.wordcloud.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;

public class SemanticSummaryParametersFactory {
	private ModelManager modelManager;
	private CySwingApplication application;
	private WordFilterFactory filterFactory;

	public SemanticSummaryParametersFactory(ModelManager modelManager, CySwingApplication application, WordFilterFactory filterFactory) {
		this.modelManager = modelManager;
		this.application = application;
		this.filterFactory = filterFactory;
	}
	
	public SemanticSummaryParameters createSemanticSummaryParameters(CyNetwork network) {
		return new SemanticSummaryParameters(network, modelManager, application, filterFactory);
	}
	
	public SemanticSummaryParameters createSemanticSummaryParameters() {
		return new SemanticSummaryParameters(modelManager, application, filterFactory);
	}
}
