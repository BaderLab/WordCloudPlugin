package org.baderlab.wordcloud.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class SelectionUtils {

	public static void setColumns(CyTable table, String columnName, Object value) {
		for (CyRow row : table.getAllRows()) {
			row.set(columnName, value);
		}
	}
	
	public static void setColumns(CyNetwork network, Collection<? extends CyIdentifiable> entities, String columnName, Object value) {
		for (CyIdentifiable entity : entities) {
			CyRow row = network.getRow(entity);
			if (row == null) {
				continue;
			}
			row.set(columnName, value);
		}
	}

	public static boolean hasSelectedNodes(CyNetwork network) {
		for (CyRow row: network.getDefaultNodeTable().getAllRows()) {
			Boolean selected = row.get(CyNetwork.SELECTED, Boolean.class);
			Long suid = row.get(CyNetwork.SUID, Long.class);
			if (selected != null && selected && network.getNode(suid) != null) {
				return true;
			}
		}
		return false;
	}

	public static Set<CyNode> getSelectedNodes(CyNetwork network) {
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyRow row: network.getDefaultNodeTable().getAllRows()) {
			Boolean selected = row.get(CyNetwork.SELECTED, Boolean.class);
			Long suid = row.get(CyNetwork.SUID, Long.class);
			CyNode node = network.getNode(suid);
			if (selected != null && selected && node != null) {
				nodes.add(node);
			}
		}
		return nodes;
	}
}
