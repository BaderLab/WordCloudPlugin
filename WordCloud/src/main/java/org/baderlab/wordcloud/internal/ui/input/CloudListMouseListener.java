/*
 File: CloudListMouseListener.java

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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;

import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.baderlab.wordcloud.internal.ui.action.CreateNetworkAction;
import org.baderlab.wordcloud.internal.ui.action.DeleteCloudAction;
import org.baderlab.wordcloud.internal.ui.action.ExportImageTaskFactory;
import org.baderlab.wordcloud.internal.ui.action.RenameCloudAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;


/**
 * This class handles all mouse actions associated with the list of
 * clouds displayed in the Input Panel for the Semantic Summary.
 */
public class CloudListMouseListener extends MouseAdapter {
	
	private JList<String> list;
	private CySwingApplication swingApplication;
	private UIManager uiManager;
	private CyServiceRegistrar registrar;
	private JCheckBox syncCheckBox;
	
	public CloudListMouseListener(UIManager uiManager, CySwingApplication swingApplication, CyServiceRegistrar registrar, JList<String> list, JCheckBox syncCheckBox) {
		this.uiManager = uiManager;
		this.swingApplication = swingApplication;
		this.list = list;
		this.registrar = registrar;
		this.syncCheckBox = syncCheckBox;
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) {
		showPopup(e);
	}
	
	
	@Override
	public void mouseReleased(MouseEvent e) {
//		showPopup(e);
	}
	
	
	@SuppressWarnings("serial")
	private void showPopup(MouseEvent e) {
		int clicked = list.locationToIndex(e.getPoint());
		if(clicked != -1 && list.getCellBounds(clicked, clicked).contains(e.getPoint())) {
			syncCheckBox.setSelected(false); // important, prevents unnecessary cloud updates
			list.setSelectedIndex(clicked);
			final String cloudName = (String)list.getSelectedValue();
			CloudParameters cloud = uiManager.getCurrentNetwork().getCloud(cloudName);
			
			if(e.isPopupTrigger()) {
				JPopupMenu menu = new JPopupMenu();
				menu.add(new DeleteCloudAction(cloud, swingApplication));
				menu.add(new RenameCloudAction(cloud, swingApplication, uiManager));
				menu.add(new CreateNetworkAction(cloud, registrar));
				menu.add(new AbstractAction(ExportImageTaskFactory.TITLE) {
					public void actionPerformed(ActionEvent e) {
						FileUtil fileUtil = registrar.getService(FileUtil.class);
						TaskManager<?,?> taskManager = registrar.getService(DialogTaskManager.class);
						ExportImageTaskFactory exportTaskFactory = new ExportImageTaskFactory(swingApplication, fileUtil, uiManager);
						taskManager.execute(exportTaskFactory.createTaskIterator());
					}
				});
				
				menu.show(list, e.getX(), e.getY());
			}
			else {
				uiManager.updateNodeSelection(cloud);
			}
		}
	}

	
	
}
