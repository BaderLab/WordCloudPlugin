/*
 File: DeleteCloudAction.java

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

package org.baderlab.wordcloud.internal.ui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.CloudProvider;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

/**
 * This is the action associated with deleting a Semantic Summary Tag Cloud
 * anywhere in the Semantic Summary Plugin.
 */

@SuppressWarnings("serial")
public class DeleteCloudAction extends AbstractCyAction {
	
	
	private CloudProvider cloudProvider;
	private CySwingApplication swingApplication;

	
	public DeleteCloudAction(CloudProvider cloudProvider, CySwingApplication swingApplication) {
		super("Delete Cloud");
		this.cloudProvider = cloudProvider;
		this.swingApplication = swingApplication;
	}
	
	
	/**
	 * Method called when a Delete Cloud action occurs.
	 * 
	 * @param ActionEvent - event created when choosing Delete Cloud.
	 */
	public void actionPerformed(ActionEvent e) {
		CloudParameters cloud = cloudProvider.getCloud();
		if(cloud == null)
			return;
		
		if(confirmDelete(cloud)) {
			cloud.delete();
		}		
	}
	
	private boolean confirmDelete(CloudParameters cloud) {
		Component parent = swingApplication.getJFrame();
		
		String cloudName = cloud.getCloudName();
		String networkName = cloud.getNetworkParams().getNetworkName();
		String message = String.format("Delete '%s' from network '%s'?", cloudName, networkName);
		
		int value = JOptionPane.showConfirmDialog(parent, message, "Delete Cloud", JOptionPane.YES_NO_OPTION);
		
		return value == JOptionPane.YES_OPTION;
	}
}
