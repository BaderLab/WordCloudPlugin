package org.baderlab.wordcloud.internal.ui;

import java.awt.Dimension;

import org.baderlab.wordcloud.internal.model.next.CloudModelListener;
import org.baderlab.wordcloud.internal.model.next.CloudModelManager;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;


/**
 * Keeps track of the current network.
 * 
 * Maybe also keeps track of something else???
 * @author mkucera
 *
 */
public class UIManager implements CloudModelListener {

	// Referenes to dependencies
	private final CloudModelManager cloudManager;
	private final CyApplicationManager applicationManager;
	private final CySwingApplication application;
	private final CyServiceRegistrar registrar;
		
	// UI components that are managed
	private SemanticSummaryInputPanel inputWindow;
	private CloudDisplayPanel cloudWindow;
	private DualPanelDocker docker;
	
	// Currently displayed cloud
	private CloudParameters currentCloud;
	
	
	public UIManager(
			CloudModelManager cloudManager, 
			CyApplicationManager applicationManager, 
			CySwingApplication application, 
			CyServiceRegistrar registrar) {
		this.cloudManager = cloudManager;
		this.applicationManager = applicationManager;
		this.application = application;
		this.registrar = registrar;
	}
	
	
	/**
	 * Activates the panels and brings them to the front.
	 * 
	 * @return true if this is the first time the panels have been activiated, false otherwise
	 */
	public boolean loadPanels() {
		if(docker == null) {
			inputWindow = new SemanticSummaryInputPanel(applicationManager, application, this, registrar);
			inputWindow.setPreferredSize(new Dimension(450, 300));
			
			cloudWindow = new CloudDisplayPanel();
			
			docker = new DualPanelDocker(inputWindow, cloudWindow, application, registrar);
			return true;
		}
		else {
			docker.bringToFront();
			return false;
		}
	}
	
	
	public SemanticSummaryInputPanel getInputPanel() {
		return inputWindow;
	}
	
	public CloudDisplayPanel getCloudDisplayPanel() {
		return cloudWindow;
	}

	public CloudParameters getCurrentCloud() {
		return currentCloud;
	}
	
	@Override
	public void cloudAdded(CloudParameters parameters) {
		loadPanels();
		inputWindow.addNewCloud(parameters);
	}
	
	
}
