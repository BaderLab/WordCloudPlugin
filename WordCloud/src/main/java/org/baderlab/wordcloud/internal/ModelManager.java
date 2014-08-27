package org.baderlab.wordcloud.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskManager;

public class ModelManager implements AddedNodesListener, RemovedNodesListener, AddedEdgesListener, RemovedEdgesListener {
	static final Lock uidLock = new ReentrantLock();
	
	private CyNetworkTableManager networkTableManager;
	private CyTableManager tableManager;
	private CyTableFactory tableFactory;
	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory networkViewFactory;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager viewManager;
	private VisualMappingManager visualMappingManager;
	private ApplyPreferredLayoutTaskFactory layoutTaskFactory;
	private TaskManager<?, ?> taskManager;
	private Map<CyNetwork, Object> changedNetworks;
	private WordCloudVisualStyleFactory cloudStyleFactory;

	public ModelManager(CyNetworkTableManager networkTableManager,
			CyTableManager tableManager, CyTableFactory tableFactory,
			CyNetworkFactory networkFactory,
			CyNetworkViewFactory networkViewFactory,
			CyNetworkManager networkManager, CyNetworkViewManager viewManager,
			VisualMappingManager visualMappingManager,
			ApplyPreferredLayoutTaskFactory layoutTaskFactory,
			TaskManager<?, ?> taskManager, WordCloudVisualStyleFactory cloudStyleFactory) {

		this.networkTableManager = networkTableManager;
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.networkManager = networkManager;
		this.viewManager = viewManager;
		this.visualMappingManager = visualMappingManager;
		this.layoutTaskFactory = layoutTaskFactory;
		this.taskManager = taskManager;
		this.cloudStyleFactory = cloudStyleFactory;
		
		changedNetworks = new WeakHashMap<CyNetwork, Object>();
	}

	public boolean hasCloudMetadata(CyNetwork network) {
		return network.getDefaultNetworkTable().getColumn(Constants.NETWORK_UID) != null;
	}

	public void createCloudMetadata(CyNetwork network) {
		CyTable networkTable = network.getDefaultNetworkTable();

		networkTable.createColumn(Constants.USE_STEMMING, Boolean.class, false);
		networkTable.createColumn(Constants.CLOUD_COUNTER, Integer.class, false);
		networkTable.createColumn(Constants.NETWORK_UID, Integer.class, false);

		CyRow row = network.getRow(network);
		row.set(Constants.USE_STEMMING, Boolean.FALSE);
		row.set(Constants.CLOUD_COUNTER, 1);
		row.set(Constants.NETWORK_UID, getNextNetworkUID());
	}

	public int getNextNetworkUID() {
		uidLock.lock();
		try {
			int maxUid = 0;
			for (CyNetwork network : networkManager.getNetworkSet()) {
				if (!hasCloudMetadata(network)) {
					continue;
				}
				Integer uid = network.getRow(network).get(Constants.NETWORK_UID, Integer.class);
				if (uid != null && uid > maxUid) {
					maxUid = uid;
				}
			}
			return maxUid + 1;
		} finally {
			uidLock.unlock();
		}
	}
	
	public void incrementCloudCounter(CyNetwork network) {
		CyRow row = network.getRow(network);
		int count = row.get(Constants.CLOUD_COUNTER, Integer.class);
		row.set(Constants.CLOUD_COUNTER, count + 1);
	}

	public CyNetwork createNetwork(String name) {
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, name);
		return network;
	}

	public void registerNetwork(CyNetwork network) {
		networkManager.addNetwork(network);
	}

	public CyNetworkView createNetworkView(CyNetwork network) {
		return networkViewFactory.createNetworkView(network);
	}

	public void registerNetworkView(CyNetworkView view) {
		viewManager.addNetworkView(view);
	}

	public void applyVisualStyle(CyNetworkView view, CloudParameters cloud) {
		CyNetwork network = view.getModel();
		String newNetworkName = network.getRow(network).get(CyNetwork.NAME,
				String.class);
		String vs_name = newNetworkName + "WordCloud_style";

		// check to see if the style exists
		VisualStyle vs = getVisualStyle(vs_name);
		if (vs == null) {
			vs = cloudStyleFactory.createVisualStyle(vs_name, cloud);
			visualMappingManager.addVisualStyle(vs);
		}

		visualMappingManager.setVisualStyle(vs, view);
		vs.apply(view);
	}

	public VisualStyle getVisualStyle(String name) {
		for (VisualStyle style : visualMappingManager.getAllVisualStyles()) {
			if (style.getTitle().equals(name)) {
				return style;
			}
		}
		return null;
	}

	public void applyPreferredLayout(CyNetworkView view) {
		taskManager.execute(layoutTaskFactory.createTaskIterator(Collections.singleton(view)));
	}

	public boolean hasChanges(CyNetwork network) {
		return changedNetworks.containsKey(network);
	}

	public void acceptChanges(CyNetwork network) {
		changedNetworks.remove(network);
	}
	
	@Override
	public void handleEvent(AddedEdgesEvent event) {
		changedNetworks.put(event.getSource(), null);
	}
	
	@Override
	public void handleEvent(AddedNodesEvent event) {
		changedNetworks.put(event.getSource(), null);
	}
	
	@Override
	public void handleEvent(RemovedEdgesEvent event) {
		changedNetworks.put(event.getSource(), null);
	}
	
	@Override
	public void handleEvent(RemovedNodesEvent event) {
		changedNetworks.put(event.getSource(), null);
	}

	public Set<CyNetwork> getSemanticSummaryNetworks() {
		Set<CyNetwork> networks = new HashSet<CyNetwork>();
		for (CyNetwork network : networkManager.getNetworkSet()) {
			if (hasCloudMetadata(network)) {
				networks.add(network);
			}
		}
		return networks;
	}

	public CyNetwork getNetwork(int uid) {
		for (CyNetwork network : networkManager.getNetworkSet()) {
			CyRow row = network.getRow(network);
			if (row == null) {
				continue;
			}
			Integer other = row.get(Constants.NETWORK_UID, Integer.class);
			if (other == null) {
				continue;
			}
			if (uid == other) {
				return network;
			}
		}
		return null;
	}

	public CyTableManager getTableManager() {
		return tableManager;
	}
}
