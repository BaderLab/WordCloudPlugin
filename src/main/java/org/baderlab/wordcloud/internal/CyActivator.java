package org.baderlab.wordcloud.internal;
import java.util.Properties;

import javax.swing.Action;

import org.baderlab.wordcloud.internal.command.BuildWordCloudCommandHandlerTaskFactory;
import org.baderlab.wordcloud.internal.command.DeleteWordCloudCommandHandlerTaskFactory;
import org.baderlab.wordcloud.internal.command.SelectWordCloudCommandHandlerTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
		CySwingApplication application = getService(context, CySwingApplication.class);
		
		CyNetworkTableManager networkTableManager = getService(context, CyNetworkTableManager.class);
		CyTableManager tableManager = getService(context, CyTableManager.class);
		CyTableFactory tableFactory = getService(context, CyTableFactory.class);
		CyNetworkFactory networkFactory = getService(context, CyNetworkFactory.class);
		CyNetworkViewFactory networkViewFactory = getService(context, CyNetworkViewFactory.class);
		CyNetworkManager networkManager = getService(context, CyNetworkManager.class);
		CyNetworkViewManager viewManager = getService(context, CyNetworkViewManager.class);
		VisualMappingManager visualMappingManager = getService(context, VisualMappingManager.class);
		ApplyPreferredLayoutTaskFactory layoutTaskFactory = getService(context, ApplyPreferredLayoutTaskFactory.class);
		TaskManager<?, ?> taskManager = getService(context, TaskManager.class);
		CyServiceRegistrar registrar = getService(context, CyServiceRegistrar.class);
		FileUtil fileUtil = getService(context, FileUtil.class);
		StreamUtil streamUtil = getService(context, StreamUtil.class);
		
		VisualStyleFactory styleFactory = getService(context, VisualStyleFactory.class);
		VisualMappingFunctionFactory continuousMappingFactory = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory passthroughMappingFactory = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		
		WordCloudVisualStyleFactory cloudStyleFactory = new WordCloudVisualStyleFactory(styleFactory, continuousMappingFactory, passthroughMappingFactory);		
		
		ModelManager modelManager = new ModelManager(networkTableManager, tableManager, tableFactory, networkFactory, networkViewFactory, networkManager, viewManager, visualMappingManager, layoutTaskFactory, taskManager, cloudStyleFactory );
		registerAllServices(context, modelManager, new Properties());
		
		IoUtil ioUtil = new IoUtil(streamUtil);
		WordFilterFactory filterFactory = new WordFilterFactory(ioUtil);
		SemanticSummaryParametersFactory parametersFactory = new SemanticSummaryParametersFactory(modelManager, application, filterFactory);
		
		SemanticSummaryManager cloudManager = new SemanticSummaryManager(applicationManager, parametersFactory);
		registerAllServices(context, cloudManager, new Properties());
		
		CreateCloudAction createCloudAction = new CreateCloudAction(applicationManager, application, cloudManager, parametersFactory);
		CreateCloudCommandAction createCloudNoDisplayAction = new CreateCloudCommandAction(applicationManager, application, cloudManager, parametersFactory);
		DeleteCloudAction deleteCloudAction = new DeleteCloudAction(application, cloudManager);
		UpdateCloudAction updateCloudAction = new UpdateCloudAction(cloudManager, applicationManager);
		SaveCloudAction saveCloudAction = new SaveCloudAction(application, fileUtil, cloudManager);

		for (AbstractCyAction action : new AbstractCyAction[] { createCloudAction, deleteCloudAction, updateCloudAction, saveCloudAction }) {
			action.setPreferredMenu("Apps.WordCloud");
			application.addAction(action);
		}
		
		CloudListSelectionHandlerFactory handlerFactory = new CloudListSelectionHandlerFactory(cloudManager, viewManager);
		SemanticSummaryInputPanelFactory inputPanelFactory = new SemanticSummaryInputPanelFactory(modelManager, applicationManager, application, fileUtil, cloudManager, createCloudAction, deleteCloudAction, updateCloudAction, saveCloudAction, handlerFactory);
		SemanticSummaryPluginAction pluginAction = new SemanticSummaryPluginAction(cloudManager, applicationManager, inputPanelFactory, application, registrar);

		// Original code had a circular dependency on
		// SemanticSummaryPluginAction.  Need to do dependency injection
		// via setter rather than constructor.
		createCloudAction.setSemanticSummaryPluginAction(pluginAction);
		createCloudNoDisplayAction.setSemanticSummaryPluginAction(pluginAction);
		deleteCloudAction.setSemanticSummaryPluginAction(pluginAction);
		updateCloudAction.setSemanticSummaryPluginAction(pluginAction);
		saveCloudAction.setSemanticSummaryPluginAction(pluginAction);
		inputPanelFactory.setSemanticSummaryPluginAction(pluginAction);
		handlerFactory.setPluginAction(pluginAction);
		
		Properties createCloudActionProperties = new Properties();
		createCloudActionProperties.setProperty(ServiceProperties.TITLE, (String) createCloudAction.getValue(Action.NAME));
		registerService(context, new ActionNodeViewTaskFactory(createCloudAction), NodeViewTaskFactory.class, createCloudActionProperties);
		
		SemanticSummaryPlugin plugin = new SemanticSummaryPlugin(pluginAction, cloudManager, parametersFactory, modelManager, ioUtil, applicationManager, application);
		registerAllServices(context, plugin, new Properties());
		
		//command line option
		Properties properties = new Properties();
    	properties.put(ServiceProperties.COMMAND, "build");
    	properties.put(ServiceProperties.COMMAND_NAMESPACE, "wordcloud");
    	registerService(context, new BuildWordCloudCommandHandlerTaskFactory(applicationManager, application, cloudManager, createCloudNoDisplayAction, parametersFactory, tableManager, tableFactory), TaskFactory.class, properties);
   		
		//command line option
		properties = new Properties();
    	properties.put(ServiceProperties.COMMAND, "delete");
    	properties.put(ServiceProperties.COMMAND_NAMESPACE, "wordcloud");
   		registerService(context, new DeleteWordCloudCommandHandlerTaskFactory(applicationManager, cloudManager, deleteCloudAction), TaskFactory.class, properties);
   		
		//command line option
		properties = new Properties();
    	properties.put(ServiceProperties.COMMAND, "select");
    	properties.put(ServiceProperties.COMMAND_NAMESPACE, "wordcloud");
   		registerService(context, new SelectWordCloudCommandHandlerTaskFactory(applicationManager, cloudManager), TaskFactory.class, properties);
	}
}
