/*
 File: SaveCloudAction.java

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

import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

@SuppressWarnings("serial")
public class ExportImageAction extends AbstractCyAction {
	
	// Extensions for the new file
	public static final String SESSION_EXT = ".png";
	public static final String TITLE = "Export Cloud Image";
	
	private CySwingApplication application;
	private FileUtil fileUtil;
	private UIManager uiManager;

	
	
	public ExportImageAction(CySwingApplication application, FileUtil fileUtil, UIManager uiManager) {
		super(TITLE);
		this.application = application;
		this.fileUtil = fileUtil;
		this.uiManager = uiManager;
	}
	
	
	private String exportImage() {
		String fileName = promptForFileName();
		if(fileName != null)
			saveFile(fileName);
		return fileName;
	}
	
	private String promptForFileName() {
		String name; // file name

		// Open Dialog to ask user the file name.
		try {
			Collection<FileChooserFilter> filters = Collections.emptyList();
			name = fileUtil.getFile(application.getJFrame(), "Save Current Cloud as PNG File", FileUtil.SAVE, filters).toString();
		} catch (Exception exp) {
			// this is because the selection was canceled
			return null;
		}
		if (!name.endsWith(SESSION_EXT))
			name = name + SESSION_EXT;
		
		return name;
	}
	
	/**
	 * Method that actually creates the file
	 */
	private void saveFile(String name) {
		RenderedImage image = uiManager.getCloudDisplayPanel().createImage();
		try {
			ImageIO.write(image, "png", new File(name));
		} catch (IOException e) {
			e.printStackTrace(); // MKTODO no
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		exportImage();
	}

}
