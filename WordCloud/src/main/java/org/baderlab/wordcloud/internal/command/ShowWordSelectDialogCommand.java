package org.baderlab.wordcloud.internal.command;

import javax.swing.JDialog;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.baderlab.wordcloud.internal.ui.input.WordSelectPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class ShowWordSelectDialogCommand extends AbstractTask {

	private final CloudModelManager cloudManager;
	private final UIManager uiManager;
	private final CySwingApplication application;
	private final CyApplicationManager appManager;
	private final Type type;
	
	public static enum Type {
		WORDS, DELIMITERS
	}
	
	@Tunable(description="The network to use (optional, will use current network if not specified.", context="nogui")
	public CyNetwork network = null;
	
	
	public ShowWordSelectDialogCommand(Type type, CloudModelManager cloudManager, UIManager uiManager, CySwingApplication application, CyApplicationManager appManager) {
		this.type = type;
		this.cloudManager = cloudManager;
		this.uiManager = uiManager;
		this.appManager = appManager;
		this.application = application;
	}
	
	
	@Override
	public void run(TaskMonitor tm)  {
		if(network == null) {
			network = appManager.getCurrentNetwork();
		}
		if(network == null) {
			tm.showMessage(Level.ERROR, "no current network");
			return;
		}
		
		NetworkParameters networkParameters = cloudManager.addNetwork(network);
		if(networkParameters == null) {
			tm.showMessage(Level.ERROR, "please create a cloud first");
			return;
		}
		
		WordSelectPanel wordSelectPanel = createPanel(networkParameters);
		JDialog dialog = wordSelectPanel.createDialog(application.getJFrame(), networkParameters.getNetworkName());
		dialog.setVisible(true);
		
		networkParameters.updateAllClouds();
		CloudParameters currentCloud = uiManager.getCurrentCloud();
		if(currentCloud != null) {
			for(CloudParameters cloud : networkParameters.getClouds()) {
				if(cloud.equals(currentCloud)) {
					uiManager.getCloudDisplayPanel().updateCloudDisplay(cloud);
				}
			}
		}
	}

	
	private WordSelectPanel createPanel(NetworkParameters network) {
		if(type == Type.WORDS)
			return new WordSelectPanel(network.getFilter());
		else
			return new WordSelectPanel(network.getDelimeters());
	}
}
