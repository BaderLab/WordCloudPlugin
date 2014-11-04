package org.baderlab.wordcloud.internal.ui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.CloudProvider;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class RenameCloudAction extends AbstractCyAction {

	private CloudProvider cloudProvider;
	private CySwingApplication swingApplication;
	private UIManager uiManager;

	
	public RenameCloudAction(CloudProvider cloudProvider, CySwingApplication swingApplication, UIManager uiManager) {
		super("Rename Cloud");
		this.cloudProvider = cloudProvider;
		this.swingApplication = swingApplication;
		this.uiManager = uiManager;
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		CloudParameters cloud = cloudProvider.getCloud();
		if(cloud == null)
			return;
		
		String curName = cloud.getCloudName();
		JFrame parent = swingApplication.getJFrame();
		
		String newName = null;
		
		//Show dialog box to change Cloud Name
		//loop until acceptable action
		while (true) {
			RenameCloudDialog theDialog = new RenameCloudDialog(parent, true, curName);
			theDialog.setLocationRelativeTo(parent);
			theDialog.setVisible(true);
			
			// Returns "" if user clicks cancel
			newName = theDialog.getNewCloudName();
			
			if (curName.equals(newName)) {  //Same as old name
				return;
			}
			else if (newName == null || newName.trim().equals("")) { //Blank or null name (or user clicked cancel)
				return;
			}
			else if (isCloudNameTaken(newName)) {
				int value = showWarnDialog(parent, 
						"A cloud with the name '" + newName + "' already exists. Please try again.", 
						"Duplicate Cloud Name");
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
			else if (isColumnNameTaken(newName)) {
				int value = showWarnDialog(parent, 
						"Cannot name cloud '" + newName + "' because a column with that name exists. Please try again.", 
						"Duplicate Column Name");
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
			else 
				break;
		}
		
		
		cloud.rename(newName);
		
	}
	
	
	private int showWarnDialog(Component parent, String message, String title) {
		Object[] options = { "Try Again", "Cancel"};
		int value = JOptionPane.showOptionDialog(parent,
				message,
				title,
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION,
				null,
				options,
				options[0]);
		
		return value;
	}
	
	/**
	 * Returns true if the specified name is already taken in the current network.
	 */
	private boolean isCloudNameTaken(String name) {
		return uiManager.getCurrentNetwork().containsCloud(name);
	}
	
	private boolean isColumnNameTaken(String name) {
		CyNetwork network = uiManager.getCurrentNetwork().getNetwork();
		if(network != null) {
			return network.getDefaultNodeTable().getColumn(name) != null;
		}
		return false;
	}
}
