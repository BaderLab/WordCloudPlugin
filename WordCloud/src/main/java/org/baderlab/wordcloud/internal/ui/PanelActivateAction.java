/*
 File: SemanticSummaryPluginAction.java

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

package org.baderlab.wordcloud.internal.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import org.baderlab.wordcloud.internal.model.next.CloudModelManager;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanelFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;


/**
 * This class defines the Semantic Summary Plugin Action.
 * This action is associated with what happens when a user selects
 * the Semantic Summary option from the Plugins menu, or right click menu.
 * 
 * @author Layla Oesper
 * @version 1.0
 */

public class PanelActivateAction extends AbstractCyAction {
	
	private static final long serialVersionUID = -5407980202304156167L;
	
	private CloudModelManager cloudManager;
	private CyApplicationManager applicationManager;
	private SemanticSummaryInputPanelFactory inputPanelFactory;
	private CySwingApplication application;
	private CyServiceRegistrar registrar;
	
	
	public PanelActivateAction(CloudModelManager cloudManager, CyApplicationManager applicationManager, SemanticSummaryInputPanelFactory inputPanelFactory, CySwingApplication application, CyServiceRegistrar registrar) {
		super("Settings");
		this.cloudManager = cloudManager;
		this.applicationManager = applicationManager;
		this.inputPanelFactory = inputPanelFactory;
		this.application = application;
		this.registrar = registrar;
	}
	
	
	/**
	 * Method called when Semantic Summary is chosen from Plugins menu. Loads
	 * SemanticSummaryPanel, CloudDisplayPanel and initializes Manager object.
	 * 
	 * @param ActionEvent - event created when choosing Semantic Summary from
	 * the Plugins menu.
	 */
	public void actionPerformed(ActionEvent ae) {
		doRealAction();
	}
	
	public void doRealAction() {
		CloudParameters nullCloud = cloudManager.getNullCloudParameters();
		if (nullCloud == null) {
			cloudManager.setupNullCloudParams();
		}
		
		boolean loaded = this.loadPanels();
		if (!loaded)
			cloudManager.setupCurrentNetwork(applicationManager.getCurrentNetwork());
	}
	
	/**
	 * Loads both the input panel and the cloud panel and brings them to the front.
	 * Returns false if this is the first time that the input panel has been loaded.
	 */
	public boolean loadPanels() {
		if(cloudManager.getDocker() == null) {
			SemanticSummaryInputPanel inputWindow = inputPanelFactory.createPanel();
			inputWindow.setPreferredSize(new Dimension(450, 300));
			
			CloudDisplayPanel cloudWindow = new CloudDisplayPanel(applicationManager, cloudManager, this);  // MKTODO why the reference to this?
			
			DualPanelDocker docker = new DualPanelDocker(inputWindow, cloudWindow, application, registrar);
			cloudWindow.setDocker(docker);
			
			cloudManager.setPanels(docker, inputWindow, cloudWindow);
			return false;
		}
		else {
			DualPanelDocker docker = cloudManager.getDocker();
			docker.bringToFront();
			return true;
		}
	}
}


