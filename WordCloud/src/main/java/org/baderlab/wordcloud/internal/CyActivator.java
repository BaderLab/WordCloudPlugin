package org.baderlab.wordcloud.internal;
import java.util.Properties;

import javax.swing.Action;

import org.baderlab.wordcloud.internal.command.CreateWordCloudCommandHandlerTaskFactory;
import org.baderlab.wordcloud.internal.command.DeleteWordCloudCommandHandlerTaskFactory;
import org.baderlab.wordcloud.internal.command.SelectWordCloudCommandHandlerTaskFactory;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.ui.CloudTaskManager;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.baderlab.wordcloud.internal.ui.action.CreateCloudAction;
import org.baderlab.wordcloud.internal.ui.action.ExportImageTaskFactory;
import org.baderlab.wordcloud.internal.ui.action.ShowAboutDialogAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	
	private static final String APPS_MENU = "Apps.WordCloud";
	
	private CloudTaskManager cloudTaskManager;
	private UIManager uiManager;
	
	@Override
	public void start(BundleContext context) throws Exception {
		
		// Get services
		CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
		CySwingApplication application = getService(context, CySwingApplication.class);
		CyTableManager tableManager = getService(context, CyTableManager.class);
		CyTableFactory tableFactory = getService(context, CyTableFactory.class);
		CyNetworkManager networkManager = getService(context, CyNetworkManager.class);
		CyServiceRegistrar registrar = getService(context, CyServiceRegistrar.class);
		FileUtil fileUtil = getService(context, FileUtil.class);
		StreamUtil streamUtil = getService(context, StreamUtil.class);
		OpenBrowser openBrowser = getService(context, OpenBrowser.class);
		
		
		// Configuration properties
		PropsReader propsReader = new PropsReader("wordcloud", "wordcloud.props");
        Properties propsReaderServiceProps = new Properties();
        propsReaderServiceProps.setProperty("cyPropertyName", "wordcloud.props");
        registerAllServices(context, propsReader, propsReaderServiceProps);
        
        
		// Managers
		CloudModelManager cloudModelManager = new CloudModelManager(networkManager, tableManager, streamUtil, propsReader);
		registerAllServices(context, cloudModelManager, new Properties());
		cloudTaskManager = new CloudTaskManager();
		
		uiManager = new UIManager(cloudModelManager, applicationManager, application, registrar, cloudTaskManager);
		cloudModelManager.addListener(uiManager);
		registerAllServices(context, uiManager, new Properties());
		
		
		// Actions
		AbstractCyAction showAction = uiManager.createShowHideAction();
		showAction.setPreferredMenu(APPS_MENU);
		registerService(context, showAction, CyAction.class, new Properties());
		
		CreateCloudAction createAction = new CreateCloudAction(applicationManager, application, cloudModelManager);
		createAction.setPreferredMenu(APPS_MENU);
		registerService(context, createAction, CyAction.class, new Properties());
		
		ExportImageTaskFactory exportImageTaskFactory = new ExportImageTaskFactory(application, fileUtil, uiManager);
		Properties props = new Properties();
		props.put(ServiceProperties.PREFERRED_MENU, APPS_MENU);
		props.put(ServiceProperties.TITLE, ExportImageTaskFactory.TITLE);
		registerService(context, exportImageTaskFactory, TaskFactory.class, props);
		
		props = new Properties();
		props.setProperty(ServiceProperties.TITLE, (String) createAction.getValue(Action.NAME));
		registerService(context, new ActionNodeViewTaskFactory(createAction), NodeViewTaskFactory.class, props);
		
		AbstractCyAction aboutAction = new ShowAboutDialogAction(application, openBrowser);
		aboutAction.setPreferredMenu(APPS_MENU);
		registerService(context, aboutAction, CyAction.class, new Properties());
		
		
		// Session persistence
		SessionListener sessionListener = new SessionListener(cloudModelManager, new IoUtil(streamUtil), networkManager, applicationManager, uiManager);
		registerAllServices(context, sessionListener, new Properties());
		
		
		// Command line
    	registerCommand(context, "create", new CreateWordCloudCommandHandlerTaskFactory(applicationManager, application, cloudModelManager, tableManager, tableFactory));
		registerCommand(context, "delete", new DeleteWordCloudCommandHandlerTaskFactory(uiManager));
		registerCommand(context, "select", new SelectWordCloudCommandHandlerTaskFactory(uiManager));
		
		
		// Always show WordCloud panels when Cytoscape starts.
		showAction.actionPerformed(null);
	}
	
	
	@Override
	public void shutDown() {
		uiManager.dispose();
		cloudTaskManager.disposeAll();
	}
	
	
	private void registerCommand(BundleContext context, String name, TaskFactory factory) {
		Properties props = new Properties();
    	props.put(ServiceProperties.COMMAND, name);
    	props.put(ServiceProperties.COMMAND_NAMESPACE, "wordcloud");
		registerService(context, factory, TaskFactory.class, props);
	}
	
	
	class PropsReader extends AbstractConfigDirPropsReader {
		public PropsReader(String name, String fileName) {
			super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
		}
	}
}
