package org.baderlab.wordcloud.internal.model.next;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.wordcloud.internal.ModelManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class CloudModelManager {

	private final Map<CyNetwork, NetworkParameters> networks;
	private final Set<CloudModelListener> listeners;
	
	private final ModelManager modelManager;
	
	
	public CloudModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
		this.listeners = new LinkedHashSet<CloudModelListener>(); // no duplicates, maintain insertion order
		this.networks = new HashMap<CyNetwork, NetworkParameters>();
	}
	
	
	/**
	 * Creates a new NetworkParameters if one does not exist already.
	 * @param network
	 * @return
	 */
	public NetworkParameters addNetwork(CyNetwork network) {
		if(isManaged(network))
			return getNetworkParameters(network);
		NetworkParameters networkParameters = new NetworkParameters(this, network);
		networks.put(network, networkParameters);
		return networkParameters;
	}
	
	
	public void removeNetwork(CyNetwork network) {
		networks.remove(network);
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
	
	
	
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	
	public void fireCloudAdded(CloudParameters cloudParams) {
		for(CloudModelListener listener : listeners) {
			listener.cloudAdded(cloudParams);
		}
	}
	
//	protected void fireNetworkAdded(NetworkParameters parameters) {
//		for(CloudModelListener listener : listeners) {
//			listener.networkAdded(parameters);
//		}
//	}
	
	
	
	
	/*
	 * Get the list of attribute names for either "node" or "edge". The attribute names will be
	 * prefixed either with "node." or "edge.". Those attributes whose data type is not
	 * "String" will be excluded
	 */
	protected static List<String> getColumnNames(CyNetwork network, Class<? extends CyIdentifiable> tableType) {
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

	
	
}
