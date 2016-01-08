package org.baderlab.wordcloud.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.baderlab.wordcloud.internal.Constants;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.property.CyProperty;

public class CloudModelManager implements NetworkAboutToBeDestroyedListener, RemovedNodesListener, ColumnNameChangedListener, ColumnDeletedListener {

	static final Lock uidLock = new ReentrantLock();
	
	private final Map<CyNetwork, NetworkParameters> networks;
	private final Set<CloudModelListener> listeners;
	private NetworkParameters nullNetwork;

	private final CyNetworkManager networkManager;
	private final CyTableManager tableManager;
	private final StreamUtil streamUtil;
	private final CyProperty<Properties> cyProperties;
	
	
	/**
	 * It is assumed that only one instance of this class will be created.
	 * 
	 * The metadata that is initialized in initializeCloudMetadata() will not work
	 * if more than one CloudModelManager is created.
	 */
	public CloudModelManager(CyNetworkManager networkManager, CyTableManager tableManager, StreamUtil streamUtil, CyProperty<Properties> cyProperties) {
		this.networkManager = networkManager;
		this.tableManager = tableManager;
		this.streamUtil = streamUtil;
		this.cyProperties = cyProperties;
		
		this.listeners = new LinkedHashSet<CloudModelListener>(); // no duplicates, maintain insertion order
		this.networks = new HashMap<CyNetwork, NetworkParameters>();
	}
	
	
	/**
	 * Creates a new NetworkParameters if one does not exist already.
	 */
	public NetworkParameters addNetwork(CyNetwork network) {
		if(network == null)
			throw new NullPointerException();
		if(isManaged(network))
			return getNetworkParameters(network);
		NetworkParameters networkParameters = new NetworkParameters(this, network);
		networks.put(network, networkParameters);
		return networkParameters;
	}
	
	public synchronized NetworkParameters getNullNetwork() {
		if(nullNetwork == null) {
			nullNetwork = new NetworkParameters(this, null);
		}
		return nullNetwork;
	}
	
	public void removeNetwork(CyNetwork network) {
		NetworkParameters removed = networks.remove(network);
		fireNetworkRemoved(removed);
	}
	
	public boolean isManaged(CyNetwork network) {
		return networks.containsKey(network);
	}
	
	public Collection<NetworkParameters> getNetworks() {
		return networks.values();
	}
	
	public NetworkParameters getNetworkParameters(CyNetwork network) {
		return networks.get(network);
	}
	
	
	public boolean addListener(CloudModelListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeListener(CloudModelListener listener) {
		return listeners.remove(listener);
	}
	
	
	
	protected void fireCloudAdded(CloudParameters cloudParams) {
		for(CloudModelListener listener : listeners) {
			listener.cloudAdded(cloudParams);
		}
	}
	
	protected void fireNetworkRemoved(NetworkParameters networkParams) {
		for(CloudModelListener listener : listeners) {
			listener.networkRemoved(networkParams);
		}
	}
	
	protected void fireNetworkModified(NetworkParameters networkParams) {
		for(CloudModelListener listener : listeners) {
			listener.networkModified(networkParams);
		}
	}
	
	protected void fireCloudDeleted(CloudParameters cloudParams) {
		for(CloudModelListener listener : listeners) {
			listener.cloudDeleted(cloudParams);
		}
	}
	
	protected void fireCloudModified(CloudParameters cloudParameters) {
		for(CloudModelListener listener : listeners) {
			listener.cloudModified(cloudParameters);
		}
	}
	
	
	/*
	 * Get the list of attribute names for either "node" or "edge". The attribute names will be
	 * prefixed either with "node." or "edge.". Those attributes whose data type is not
	 * "String" will be excluded
	 */
	public static List<String> getColumnNames(CyNetwork network, Class<? extends CyIdentifiable> tableType) {
		if (network == null) {
			return Collections.emptyList();
		}
		
		List<String> attributeList = new ArrayList<String>();
		CyTable table = null;
		
		if (tableType.equals(CyNode.class)) {
			table = network.getDefaultNodeTable();
			
		}
		else if (tableType.equals(CyEdge.class)){
			table = network.getDefaultEdgeTable();			
		}
				
		if (table != null) {
			//  Show all attributes, with type of String or Number
			for (CyColumn column : table.getColumns()) {
				Class<?> type = column.getType();
				
				if (type.equals(String.class)) {
					attributeList.add(column.getName());
				}
				else if (type.equals(List.class) && column.getListElementType().equals(String.class))
				{
					attributeList.add(column.getName());
				}
			} //for loop
		
			//  Alphabetical sort
			Collections.sort(attributeList);
		}
		return attributeList;
	}

	
	public static boolean hasCloudMetadata(CyNetwork network) {
		return network.getDefaultNetworkTable().getColumn(Constants.NETWORK_UID) != null;
	}

	protected void initializeCloudMetadata(CyNetwork network) {
		CyTable networkTable = network.getDefaultNetworkTable();

		if(networkTable.getColumn(Constants.NETWORK_UID) == null) {
			networkTable.createColumn(Constants.USE_STEMMING, Boolean.class, false);
			networkTable.createColumn(Constants.CLOUD_COUNTER, Integer.class, false);
			networkTable.createColumn(Constants.NETWORK_UID, Integer.class, false);
		}

		CyRow row = network.getRow(network);
		if(row.get(Constants.NETWORK_UID, Integer.class) == null) {
			row.set(Constants.USE_STEMMING, Boolean.FALSE);
			row.set(Constants.CLOUD_COUNTER, 1);
			row.set(Constants.NETWORK_UID, getNextNetworkUID());
		}
	}

	private int getNextNetworkUID() {
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
	
	public CyTableManager getTableManager() {
		return tableManager;
	}


	public StreamUtil getStreamUtil() {
		return streamUtil;
	}
	
	
	/**
	 * Return the default net weight as set in the properties.
	 */
	double getNetWeightPropertyValue() {
		final String propName = "wordcloud.defaultNetWeight";
		try {
			double value = Double.valueOf((String)cyProperties.getProperties().get(propName));
			if(value < 0.0) {
				cyProperties.getProperties().put(propName, "0.0");
				return 0.0;
			}
			if(value > 1.0) {
				cyProperties.getProperties().put(propName, "1.0");
				return 1.0;
			}
			return value;
		} catch(Exception e) {
			cyProperties.getProperties().put(propName, String.valueOf(CloudParameters.DEFAULT_NET_WEIGHT));
			return CloudParameters.DEFAULT_NET_WEIGHT;
		}
	}


	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		// MKTODO is this called when the session is destroyed?
		CyNetwork network = e.getNetwork();
		removeNetwork(network);
	}



	@Override
	public synchronized void handleEvent(RemovedNodesEvent e) {
		CyNetwork network = e.getSource();
		NetworkParameters networkParams = networks.get(network);
		fireNetworkModified(networkParams);
		
		// Below code doesn't seem necessary because CloudParameters.getSelectedNodes() checks which of its nodes are actually selected.
//		if(networkParams != null) {
//			boolean networkModified = false;
//			
//			for(CloudParameters cloud : networkParams.getClouds()) {
//				boolean modified = false;
//				Set<CyNode> nodes = cloud.getSelectedNodes();
//				Iterator<CyNode> iter = nodes.iterator();
//				while(iter.hasNext()) {
//					CyNode node = iter.next();
//					if(network.getRow(node) == null) {
//						iter.remove();
//						modified = true;
//					}
//				}
//				if(modified) {
//					cloud.setRatiosInitialized(false);
//					cloud.setCountInitialized(false);
//					cloud.setSelInitialized(false);
//					networkModified = true;
//				}
//			}
//			
//			if(networkModified) {
//				fireNetworkModified(networkParams);
//			}
//		}
	}


	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		handleColumnNameChange(e.getSource(), e.getOldColumnName(), e.getNewColumnName());
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		handleColumnNameChange(e.getSource(), e.getColumnName(), null);
	}

	
	private void handleColumnNameChange(CyTable table, String oldName, String newName) {
		List<CloudParameters> modifiedClouds = new ArrayList<CloudParameters>();
		
		for(NetworkParameters networkParams : getNetworks()) {
			CyNetwork network = networkParams.getNetwork();
			if(network.getDefaultNodeTable().equals(table) || network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).equals(table)) {
				for(CloudParameters cloudParams : networkParams.getClouds()) {
					if(cloudParams.getAttributeNames().contains(oldName)) {
						cloudParams.removeAttribtueName(oldName);
						if(newName != null)
							cloudParams.addAttributeName(newName);
						modifiedClouds.add(cloudParams);
					}
				}
			}
		}
		
		for(CloudParameters cloud : modifiedClouds) {
			fireCloudModified(cloud);
		}
	}

}
