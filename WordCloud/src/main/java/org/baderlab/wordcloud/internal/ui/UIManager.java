package org.baderlab.wordcloud.internal.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.model.CloudModelListener;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
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
	
	// The Show/Hide action, stored here because it needs to be managed
	private AbstractCyAction showHideAction;
	
	// Remembers which cloud was selected so the selection can be restored when switching clouds.
	private Map<NetworkParameters, CloudParameters> selectedClouds = new HashMap<NetworkParameters, CloudParameters>();
	private NetworkParameters currentNetwork;
	
	private boolean hidden = true;
	
	
	public UIManager(
			CloudModelManager cloudManager, 
			CyApplicationManager applicationManager, 
			CySwingApplication application, 
			CyServiceRegistrar registrar,
			CyNetworkViewManager viewManager) {
		this.cloudManager = cloudManager;
		this.applicationManager = applicationManager;
		this.application = application;
		this.registrar = registrar;
		this.viewManager = viewManager;
		
	}
	
	@SuppressWarnings("serial")
	public AbstractCyAction createShowHideAction() {
		if(showHideAction == null) {
			showHideAction = new AbstractCyAction("Show WordCloud") {
				public void actionPerformed(ActionEvent e) {
					if(docker == null) {
						setCurrentCloud(applicationManager.getCurrentNetwork());
					} else {
						hide();
					}
				}
			};
		}
		return showHideAction;
		
	}
	
	
	/**
	 * Activates the panels and brings them to the front.
	 * To show the panels use one of the setCurrentCloud() methods.
	 */
	private void show() {
		hidden = false;
		if(docker == null) {
			inputWindow = new SemanticSummaryInputPanel(applicationManager, application, this, registrar);
			inputWindow.setPreferredSize(new Dimension(350, 400));
			cloudWindow = new CloudDisplayPanel(this);
			docker = new DualPanelDocker(inputWindow, cloudWindow, application, registrar);
			cloudWindow.setDocker(docker);
			if(showHideAction != null) {
				showHideAction.setName("Hide WordCloud");
			}
		}
	}
	
	/**
	 * Disposes of the panels.
	 */
	public void hide() {
		hidden = true;
		if(docker != null) {
			// selected clouds are still remembered
			docker.dispose();
			docker = null;
			inputWindow = null;
			cloudWindow = null;
			currentNetwork = null;
			if(showHideAction != null) {
				showHideAction.setName("Show WordCloud");
			}
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
		if(network == null)
			setCurrentCloud((NetworkParameters)null);
		else
			setCurrentCloud(cloudManager.addNetwork(network)); // always create a NetworkParameters object to store a sync cloud
	}
	
	public void setCurrentCloud(NetworkParameters networkParams) {
		if(networkParams == null)
			networkParams = cloudManager.getNullNetwork();
		
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
	
	public void setCurrentCloud(NetworkParameters network, String cloudName) {
		CloudParameters cloud = network.getCloud(cloudName);
		setCurrentCloud(cloud == null ? network.getNullCloud() : cloud);
	}
	
	
	public void setCurrentCloud(CloudParameters cloud) {
		setCurrentCloud(cloud, true);
	}
	
	public void setCurrentCloud(CloudParameters cloud, boolean updateNodeSelection) {
		if(cloud == null)
			throw new NullPointerException();
		
		currentNetwork = cloud.getNetworkParams();
		selectedClouds.put(cloud.getNetworkParams(), cloud); 
		
		show();
		
		inputWindow.setCurrentCloud(cloud);
		cloudWindow.updateCloudDisplay(cloud);
		
		if(updateNodeSelection)
			updateNodeSelection(cloud);
	}
	
	/**
	 * Update the current network view to show the selected nodes from the cloud.
	 */
	public void updateNodeSelection(CloudParameters cloud) {
		// Update the selection to show the cloud
		Set<CyNode> selNodes = cloud.getSelectedNodes();
		CyNetwork network = cloud.getNetworkParams().getNetwork();
		if(network != null)
			SelectionUtils.setSelected(network, selNodes);
	}

	
	private void clear() {
		setCurrentCloud(cloudManager.getNullNetwork());
	}
	
	
	public CloudParameters getCurrentCloud() {
		if(currentNetwork == null)
			return cloudManager.getNullNetwork().getNullCloud();
		CloudParameters cloud = selectedClouds.get(currentNetwork);
		return cloud == null ? currentNetwork.getNullCloud() : cloud;
	}
	
	public boolean isCurrentCloud(CloudParameters cloud) {
		return cloud.getNetworkParams() == currentNetwork 
			&& cloud == selectedClouds.get(cloud.getNetworkParams());
	}
	

	
	// CloudModelManager events
	
	@Override
	public void cloudAdded(CloudParameters cloudParams) {
		if(hidden)
			show();
		setCurrentCloud(cloudParams);
		docker.bringToFront();
	}
	
	@Override
	public void cloudDeleted(CloudParameters cloud) {
		if(isCurrentCloud(cloud)) {
			selectedClouds.remove(cloud.getNetworkParams());
		}
		
		if(hidden)
			return;
		
		cloudWindow.disposeCloud(cloud);
		if(cloud.getNetworkParams() == currentNetwork) {
			setCurrentCloud(cloud.getNetworkParams());
		}
	}
	
	@Override
	public void networkModified(NetworkParameters networkParams) {
		if(hidden)
			return;
		
		if(networkParams == currentNetwork) {
			// refresh current cloud
			CloudParameters currentCloud = selectedClouds.get(currentNetwork);
			inputWindow.setCurrentCloud(currentCloud);
			cloudWindow.updateCloudDisplay(currentCloud);
		}
	}
	
	@Override
	public void cloudModified(CloudParameters cloudParams) {
		if(hidden)
			return;
		
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
		if(hidden)
			return;
		
		CyNetworkView networkView = e.getNetworkView();
		if(networkView == null) {
			clear();
		} else {
			setCurrentCloud(networkView.getModel());
		}
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		if(hidden)
			return;
		
		setCurrentCloud(e.getNetwork());
	}
	
	
	/**
	 * Handle network rename.
	 * MKTODO this could be handled through the networkModified event
	 */
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(hidden)
			return;
		
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
