package org.baderlab.wordcloud.internal.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class RenameCloudAction extends AbstractCyAction {

	private CloudParameters cloud;
	private CySwingApplication swingApplication;
	private UIManager uiManager;

	
	public RenameCloudAction(CloudParameters cloud, CySwingApplication swingApplication, UIManager uiManager) {
		super("Rename Cloud");
		this.cloud = cloud;
		this.swingApplication = swingApplication;
		this.uiManager = uiManager;
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
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
			else if (isCloudNameTaken(newName)) { //Already taken name
				Object[] options = { "Try Again", "Cancel"};
				int value = JOptionPane.showOptionDialog(parent,
						"That cloud name already exists, try again.",
						"Duplicate Cloud Name",
						JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_CANCEL_OPTION,
						null,
						options,
						options[0]);
				
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
			else 
				break;
		}//end while true loop
		
		
		cloud.rename(newName);
		
	}//end actionPerformed
	
	
	/**
	 * Returns true if the specified name is already taken in the
	 * current network.
	 */
	private boolean isCloudNameTaken(String name)
	{
		return uiManager.getCurrentNetwork().containsCloud(name);
	}
	
}
