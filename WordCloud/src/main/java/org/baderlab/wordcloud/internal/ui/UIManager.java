package org.baderlab.wordcloud.internal.ui;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.model.next.CloudModelListener;
import org.baderlab.wordcloud.internal.model.next.CloudModelManager;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.model.next.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;


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
	private final CyNetworkViewManager viewManager;
	
	// UI components that are managed
	private SemanticSummaryInputPanel inputWindow;
	private CloudDisplayPanel cloudWindow;
	private DualPanelDocker docker;
	
	// Currently displayed cloud
	
	// Remembers which cloud was selected so the selection can be restored when switching clouds.
	private Map<NetworkParameters, CloudParameters> selectedClouds = new HashMap<NetworkParameters, CloudParameters>();
	private NetworkParameters currentNetwork;
	
	
	public UIManager(
			CloudModelManager cloudManager, 
			CyApplicationManager applicationManager, 
			CySwingApplication application, 
			CyServiceRegistrar registrar,
			CyNetworkViewManager viewManager ) {
		this.cloudManager = cloudManager;
		this.applicationManager = applicationManager;
		this.application = application;
		this.registrar = registrar;
		this.viewManager = viewManager;
	}
	
	
	/**
	 * Activates the panels and brings them to the front.
	 * 
	 * @return true if this is the first time the panels have been activiated, false otherwise
	 */
	private boolean loadPanels() {
		if(docker == null) {
			inputWindow = new SemanticSummaryInputPanel(applicationManager, application, this, registrar);
			inputWindow.setPreferredSize(new Dimension(450, 300));
			cloudWindow = new CloudDisplayPanel();
			docker = new DualPanelDocker(inputWindow, cloudWindow, application, registrar);
			cloudWindow.setDocker(docker);
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

	public NetworkParameters getCurrentNetwork() {
		return currentNetwork;
	}
	
	
	@Override
	public void cloudAdded(CloudParameters cloudParams) {
		setCurrentCloud(cloudParams);
	}
	
	public void setCurrentCloud(NetworkParameters networkParams) {
		CloudParameters selectedCloud = selectedClouds.get(networkParams);
		if(selectedCloud != null) {
			setCurrentCloud(selectedCloud);
		}
		else {
			CloudParameters defaultCloud = networkParams.getFirstCloud();
			if(defaultCloud != null) {
				setCurrentCloud(defaultCloud);
			}
		}
	}
	
	public void setCurrentCloud(NetworkParameters network, String cloudName) {
		CloudParameters cloud = network.getCloud(cloudName);
		if(cloud != null) {
			setCurrentCloud(cloud);
		}
	}
	
	
	public void setCurrentCloud(CloudParameters cloud) {
		if(cloud == null)
			throw new NullPointerException();
		
		loadPanels();
		
		selectedClouds.put(cloud.getNetworkParams(), cloud);
		currentNetwork = cloud.getNetworkParams();
		
		inputWindow.setCurrentCloud(cloud);
		cloudWindow.updateCloudDisplay(cloud);
		
		
		// Update the selection to show the cloud
		Set<CyNode> selNodes = cloud.getSelectedNodes();
		CyNetwork network = cloud.getNetworkParams().getNetwork();
				
		SelectionUtils.setColumns(network.getDefaultNodeTable(), CyNetwork.SELECTED, Boolean.FALSE);
		SelectionUtils.setColumns(network.getDefaultEdgeTable(), CyNetwork.SELECTED, Boolean.FALSE);
		SelectionUtils.setColumns(network, selNodes, CyNetwork.SELECTED, Boolean.TRUE);
		
		for (CyNetworkView networkView : viewManager.getNetworkViews(network)) {
			networkView.updateView();
		}
	}
	
	
	
	
	
}
