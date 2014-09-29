/*
 File: CloudListSelectionHandler.java

 Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
 
 Code written by: Layla Oesper
 Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
 
 This library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.baderlab.wordcloud.internal.ui.input;

import java.awt.Component;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.model.SemanticSummaryManager;
import org.baderlab.wordcloud.internal.model.SemanticSummaryParameters;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.ui.PanelActivateAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

/**
 * This class handles the action associated with selecting a cloud from the
 * list of clouds in the input panel.  It displays the cloud, updates the 
 * input panel and highlights the correct nodes in the view.
 * @author Layla Oesper
 * @version 1.0
 */

public class CloudListSelectionHandler implements ListSelectionListener 
{
	private Component parent;
	private SemanticSummaryManager cloudManager;
	private PanelActivateAction pluginAction;
	private CyNetworkViewManager viewManager;

	public CloudListSelectionHandler(Component parent, SemanticSummaryManager cloudManager, PanelActivateAction pluginAction, CyNetworkViewManager viewManager)
	{
		this.parent = parent;
		this.cloudManager = cloudManager;
		this.pluginAction = pluginAction;
		this.viewManager = viewManager;
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
		//retrieve model
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		
		//Only care when value is no longer changing
		if (lsm.getValueIsAdjusting())
			return;
		
		//Selection should be single, retrieve index of selected
		int index = lsm.getMinSelectionIndex();
		
		if (lsm.isSelectedIndex(index))
		{
			SemanticSummaryInputPanel inputPanel = cloudManager.getInputWindow();
			
			//Retrieve Cloud Name of selected
			String cloudName = (String)inputPanel.getListValues().elementAt(index);
			
			//Get CloudParameters
			SemanticSummaryParameters params = cloudManager.getCurNetwork();
			
			//If cloud no longer exists, pop-up warning - this should never happen
			if (!params.containsCloud(cloudName))
			{
				String message = "Warning - Cloud no longer exists.";
				JOptionPane.showMessageDialog(parent, message);
				return;
			}
			
			CloudParameters cloudParams = params.getCloud(cloudName);
			
			//InputPanel load values
			inputPanel.loadCurrentCloud(cloudParams);
			//Load Cloud
			cloudManager.getCloudWindow().
			updateCloudDisplay(cloudParams);
			
			//Highlight selected nodes if view exists
			
			CyNetwork network = params.getNetwork();
			if (network == null) {
				return;
			}
			
			Set<CyNode> selNodes = cloudParams.getSelectedNodes();
			
			SelectionUtils.setColumns(network.getDefaultNodeTable(), CyNetwork.SELECTED, Boolean.FALSE);
			SelectionUtils.setColumns(network.getDefaultEdgeTable(), CyNetwork.SELECTED, Boolean.FALSE);
			SelectionUtils.setColumns(network, selNodes, CyNetwork.SELECTED, Boolean.TRUE);
			
			for (CyNetworkView networkView : viewManager.getNetworkViews(network)) {
				networkView.updateView();
			}
			
			//Move windows to the forefront
			pluginAction.loadPanels();
		}
	}
}
