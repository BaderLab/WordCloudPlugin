package org.baderlab.wordcloud.internal.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.wordcloud.internal.ui.AboutDialog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;

@SuppressWarnings("serial")
public class ShowAboutDialogAction extends AbstractCyAction {

	
	private final CySwingApplication application;
	private final OpenBrowser openBrowser;
	

    public ShowAboutDialogAction(CySwingApplication application, OpenBrowser openBrowser) {
		super("About WordCloud...");
		this.application = application;
		this.openBrowser = openBrowser;
	}
	
	public void actionPerformed(ActionEvent event) {
		AboutDialog aboutPanel = new AboutDialog(application.getJFrame(), openBrowser);
		aboutPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutPanel.pack();
		aboutPanel.setLocationRelativeTo(application.getJFrame());
		aboutPanel.setVisible(true);
	}

}
