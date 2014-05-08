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

package org.baderlab.wordcloud.internal;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.Icon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;


/**
 * This class defines the Semantic Summary Plugin Action.
 * This action is associated with what happens when a user selects
 * the Semantic Summary option from the Plugins menu, or right click menu.
 * 
 * @author Layla Oesper
 * @version 1.0
 */

public class SemanticSummaryPluginAction extends AbstractCyAction
{
	//VARIABLES
	private static final long serialVersionUID = -5407980202304156167L;
	private SemanticSummaryManager cloudManager;
	private CyApplicationManager applicationManager;
	private SemanticSummaryInputPanelFactory inputPanelFactory;
	private CySwingApplication application;
	private CyServiceRegistrar registrar;
	
	//CONSTRUCTORS
	
	/**
	 * SemanticSummaryPluginAction constructor
	 * @param registrar 
	 * @param modelManager 
	 * @param fileUtil 
	 * 
	 */
	public SemanticSummaryPluginAction(SemanticSummaryManager cloudManager, CyApplicationManager applicationManager, SemanticSummaryInputPanelFactory inputPanelFactory, CySwingApplication application, CyServiceRegistrar registrar)
	{
		super("Settings");
		this.cloudManager = cloudManager;
		this.applicationManager = applicationManager;
		this.inputPanelFactory = inputPanelFactory;
		this.application = application;
		this.registrar = registrar;
	}
	
	//METHODS
	
	/**
	 * Method called when Semantic Summary is chosen from Plugins menu. Loads
	 * SemanticSummaryPanel, CloudDisplayPanel and initializes Manager object.
	 * 
	 * @param ActionEvent - event created when choosing Semantic Summary from
	 * the Plugins menu.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		
		doRealAction();
	}
	
	public void doRealAction()
	{
		//Create Null Cloud in Manager
		CloudParameters nullCloud = cloudManager.getNullCloudParameters();
		if (nullCloud == null)
		{
			cloudManager.setupNullCloudParams();
		}
		
		
		boolean loaded = this.loadInputPanel();
		this.loadCloudPanel();
		
		if (!loaded)
			cloudManager.setupCurrentNetwork(applicationManager.getCurrentNetwork());
	}
	
	/**
	 * Loads the InputPanel or brings it into the forefront.  Returns false
	 * if this is the first time that the input panel has been loaded.
	 */
	public boolean loadInputPanel()
	{
		boolean loaded = false;
		
		CytoPanel cytoPanel = application.getCytoPanel(CytoPanelName.WEST);
		
		//Check if panel already exists
		SemanticSummaryInputPanel inputWindow = cloudManager.getInputWindow();
		
		if(inputWindow == null)
		{
			inputWindow = inputPanelFactory.createPanel();
			inputWindow.setPreferredSize(new Dimension(450, 300));

			//Set input window in the manager
			cloudManager.setInputWindow(inputWindow);
			
			//Add panel to display
			CytoPanelComponent panelComponent = createCytoPanelComponent("WordCloud", null, CytoPanelName.WEST, inputWindow);
			registrar.registerService(panelComponent, CytoPanelComponent.class, new Properties());
			
			//Move to front of display
			int index = cytoPanel.indexOfComponent(inputWindow);
			cytoPanel.setSelectedIndex(index);
		}//end if not loaded
		
		else
		{
			//Move to front of display
			int index = cytoPanel.indexOfComponent(inputWindow);
			cytoPanel.setSelectedIndex(index);
			loaded = true;
		}//end else
		
		return loaded;
	}//end loadInputPanel() method
	
	
	
	/**
	 * Loads the CloudPanel or brings it into the forefront.
	 */
	public void loadCloudPanel()
	{
		CytoPanel cytoPanel = application.getCytoPanel(CytoPanelName.SOUTH);
		//Check if panel already exists
		CloudDisplayPanel cloudWindow = cloudManager.getCloudWindow();
		
		if(cloudWindow == null)
		{
			
			cloudWindow = new CloudDisplayPanel(applicationManager, cloudManager, this);
			
			//Set input window in the manager
			cloudManager.setCloudDisplayWindow(cloudWindow);
			
			CytoPanelComponent panelComponent = createCytoPanelComponent("WordCloud Display", null, CytoPanelName.SOUTH, cloudWindow);
			registrar.registerService(panelComponent, CytoPanelComponent.class, new Properties());

			//Move to front of display
			int index = cytoPanel.indexOfComponent(cloudWindow);
			cytoPanel.setSelectedIndex(index);
		}//end if not loaded
		
		else
		{
			//Move to front of display
			int index = cytoPanel.indexOfComponent(cloudWindow);
			cytoPanel.setSelectedIndex(index);
		}//end else
	}//end loadCloudPanel() method
	
	CytoPanelComponent createCytoPanelComponent(final String title, final Icon icon, final CytoPanelName position, final Component component) {
		return new CytoPanelComponent() {
			
			@Override
			public String getTitle() {
				return title;
			}
			
			@Override
			public Icon getIcon() {
				return icon;
			}
			
			@Override
			public CytoPanelName getCytoPanelName() {
				return position;
			}
			
			@Override
			public Component getComponent() {
				return component;
			}
		};
	}
}


