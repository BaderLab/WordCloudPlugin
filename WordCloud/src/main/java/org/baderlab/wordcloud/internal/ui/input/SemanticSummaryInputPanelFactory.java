package org.baderlab.wordcloud.internal.ui.input;

import org.baderlab.wordcloud.internal.ModelManager;
import org.baderlab.wordcloud.internal.model.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.ui.CreateCloudAction;
import org.baderlab.wordcloud.internal.ui.DeleteCloudAction;
import org.baderlab.wordcloud.internal.ui.SaveCloudAction;
import org.baderlab.wordcloud.internal.ui.PanelActivateAction;
import org.baderlab.wordcloud.internal.ui.UpdateCloudAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;

public class SemanticSummaryInputPanelFactory {
	private final ModelManager modelManager;
	private final CyApplicationManager applicationManager;
	private final CySwingApplication application;
	private final FileUtil fileUtil;
	private final SemanticSummaryManager cloudManager;
	private final CreateCloudAction createCloudAction;
	private final DeleteCloudAction deleteCloudAction;
	private final UpdateCloudAction updateCloudAction;
	private final SaveCloudAction saveCloudAction;

	private PanelActivateAction pluginAction;
	private CloudListSelectionHandlerFactory handlerFactory;
	private CyServiceRegistrar registrar;

	public SemanticSummaryInputPanelFactory(ModelManager modelManager, CyApplicationManager applicationManager, CySwingApplication application, CyServiceRegistrar registrar, FileUtil fileUtil, SemanticSummaryManager cloudManager, CreateCloudAction createCloudAction, DeleteCloudAction deleteCloudAction, UpdateCloudAction updateCloudAction, SaveCloudAction saveCloudAction, CloudListSelectionHandlerFactory handlerFactory) {
		this.modelManager = modelManager;
		this.applicationManager = applicationManager;
		this.application = application;
		this.registrar = registrar;
		this.fileUtil = fileUtil;
		this.cloudManager = cloudManager;
		this.createCloudAction = createCloudAction;
		this.deleteCloudAction = deleteCloudAction;
		this.updateCloudAction = updateCloudAction;
		this.saveCloudAction = saveCloudAction;
		this.handlerFactory = handlerFactory;
	}
	
	public void setSemanticSummaryPluginAction(PanelActivateAction pluginAction) {
		this.pluginAction = pluginAction;
	}
	
	public SemanticSummaryInputPanel createPanel() {
		if (pluginAction == null) {
			throw new RuntimeException();
		}
		return new SemanticSummaryInputPanel(modelManager, applicationManager, application, registrar, fileUtil, cloudManager, pluginAction, createCloudAction, deleteCloudAction, updateCloudAction, saveCloudAction, handlerFactory);
	}
}
