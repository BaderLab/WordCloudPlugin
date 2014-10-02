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
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;


/**
 * Keeps track of the current network and cloud and updates the UI.
 * @author mkucera
 */
public class UIManager implements CloudModelListener, SetCurrentNetworkListener, SetCurrentNetworkViewListener, RowsSetListener {

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
			//docker.bringToFront();
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

	public CloudModelManager getCloudModelManager() {
		return cloudManager;
	}
	
	
	public void setCurrentCloud(CyNetwork network) {
		setCurrentCloud(cloudManager.getNetworkParameters(network));
	}
	
	public void setCurrentCloud(NetworkParameters networkParams) {
		if(networkParams == null) {
			clear();
		}
		else {
			CloudParameters selectedCloud = selectedClouds.get(networkParams);
			if(selectedCloud != null) {
				setCurrentCloud(selectedCloud);
			}
			else {
				CloudParameters defaultCloud = networkParams.getFirstCloud();
				if(defaultCloud != null) {
					setCurrentCloud(defaultCloud);
				}
				else {
					setCurrentCloud(networkParams.getNullCloud());
				}
			}
		}
	}
	
	public void setCurrentCloud(NetworkParameters network, String cloudName) {
		CloudParameters cloud = network.getCloud(cloudName);
		setCurrentCloud(cloud == null ? network.getNullCloud() : cloud);
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

	
	public void clear() {
		if(docker != null) {
			cloudWindow.clearCloud();
			inputWindow.setCurrentCloud(cloudManager.getNullNetwork().getNullCloud());
		}
	}
	
	
	public CloudParameters getCurrentCloud() {
		return selectedClouds.get(currentNetwork);
	}
	
	public boolean isCurrentCloud(CloudParameters cloud) {
		return cloud.getNetworkParams() == currentNetwork 
			&& cloud == selectedClouds.get(cloud.getNetworkParams());
	}
	

	
	// CloudModelManager events
	
	@Override
	public void cloudAdded(CloudParameters cloudParams) {
		setCurrentCloud(cloudParams);
		docker.bringToFront();
	}
	
	@Override
	public void cloudDeleted(CloudParameters cloud) {
		if(isCurrentCloud(cloud)) {
			selectedClouds.remove(cloud.getNetworkParams());
			setCurrentCloud(cloud.getNetworkParams());
		}
		else if(cloud.getNetworkParams() == currentNetwork) {
			setCurrentCloud(cloud.getNetworkParams());
		}
	}
	
	@Override
	public void networkModified(NetworkParameters networkParams) {
		if(networkParams == currentNetwork) {
			// refresh current cloud
			CloudParameters currentCloud = selectedClouds.get(currentNetwork);
			inputWindow.setCurrentCloud(currentCloud);
			cloudWindow.updateCloudDisplay(currentCloud);
		}
	}
	
	@Override
	public void cloudRenamed(CloudParameters cloudParams) {
		if(cloudParams.getNetworkParams() == currentNetwork) {
			inputWindow.setCurrentCloud(selectedClouds.get(currentNetwork)); // this basically does a refresh
		}
	}
	
	@Override
	public void networkRemoved(NetworkParameters networkParams) {
		// Actually, the below events take care of this just fine
	}
	
	
	// Cytoscape Events
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		if(networkView == null) {
			clear();
		} else {
			setCurrentCloud(networkView.getModel());
		}
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		setCurrentCloud(e.getNetwork());
	}
	
	
	/**
	 * Handle network rename.
	 * MKTODO this could be handled through the networkModified event
	 */
	@Override
	public void handleEvent(RowsSetEvent e) {
		// Just doing an approximate calculation here, detect if ANY network has changed name.
		boolean isNetworkRename = false;
		for (RowSetRecord record : e.getPayloadCollection()) {
			if (record.getColumn().equals(CyNetwork.NAME)) {
				isNetworkRename = true;
				break;
			}
		}
		
		if(isNetworkRename) {
			inputWindow.updateNetworkName(currentNetwork.getNetworkName());
		}
	}
	
	
}
