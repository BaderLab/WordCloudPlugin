package org.baderlab.wordcloud.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayPanel;
import org.baderlab.wordcloud.internal.ui.input.SemanticSummaryInputPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * A container for a dialog that holds two panels.
 * The panels can be undocked and docked from the main Cytoscape UI.
 * 
 * @author mkucera
 */
public class DualPanelDocker {

	private static enum State { DOCKED, UNDOCKED };
	
	public static interface DockCallback {
		public void docked();
		public void undocked();
	}
	
	
	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	
	private final SemanticSummaryInputPanel inputPanel;
	private final CloudDisplayPanel cloudPanel;
	
	private DockCallback callback;
	
	private JDialog dialog;
	private State state;
	
	
	
	public DualPanelDocker(SemanticSummaryInputPanel inputPanel, CloudDisplayPanel cloudPanel, 
			               CySwingApplication swingApplication, CyServiceRegistrar registrar) {
		this.inputPanel = inputPanel;
		this.cloudPanel = cloudPanel;
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		
		state = State.UNDOCKED;
		flip(); // this will initially dock it
	}
	
	
	public void setCallback(DockCallback callback) {
		this.callback = callback;
	}
	
	
	public void flip() {
		switch(state) {
			case DOCKED:
				undock();
				state = State.UNDOCKED;
				if(callback != null)
					callback.undocked();
				break;
			case UNDOCKED:
				dock();
				state = State.DOCKED;
				if(callback != null)
					callback.docked();
				break;
		}
	}
	
	
	private void dock() {
		if(dialog != null) {
			dialog.removeWindowListener(dialog.getWindowListeners()[0]);
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			dialog = null;
		}
		
		CytoPanelComponent inputComponent = wrapInCytoPanel(inputPanel, CytoPanelName.WEST, "WordCloud");
		CytoPanelComponent cloudComponent = wrapInCytoPanel(cloudPanel, CytoPanelName.SOUTH, "WordCloud Display");
		
		registrar.registerService(inputComponent, CytoPanelComponent.class, new Properties());
		registrar.registerService(cloudComponent, CytoPanelComponent.class, new Properties());
		
		// Bring to front
		bringToFront(inputComponent);
		bringToFront(cloudComponent);
	}
	
	
	
	private void undock() {
		registrar.unregisterService(inputPanel, CytoPanelComponent.class);
		registrar.unregisterService(cloudPanel, CytoPanelComponent.class);
		
		dialog = new JDialog(swingApplication.getJFrame(), "WordCloud", false);
		dialog.setLayout(new BorderLayout());
		dialog.add(inputPanel, BorderLayout.WEST);
		dialog.add(cloudPanel, BorderLayout.CENTER);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Dimension inputSize = inputPanel.getPreferredSize();
		Dimension cloudSize = cloudPanel.getPreferredSize();
		dialog.setSize(new Dimension(inputSize.width + Math.max(cloudSize.width, 400), inputSize.height));
		dialog.setVisible(true);
		dialog.pack();
		
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				flip();
			}
		});
	}
	
	
	private void bringToFront(CytoPanelComponent panel) {
		bringToFront(panel.getCytoPanelName(), panel.getComponent());
	}
	
	private void bringToFront(CytoPanelName compassPoint, Component component) {
		CytoPanel cytoPanel = swingApplication.getCytoPanel(compassPoint);
		int index = cytoPanel.indexOfComponent(component);
		cytoPanel.setSelectedIndex(index);
	}
	
	public void bringToFront() {
		bringToFront(CytoPanelName.WEST, inputPanel);
		bringToFront(CytoPanelName.SOUTH, cloudPanel);
	}

	
	
	public SemanticSummaryInputPanel getInputPanel() {
		return inputPanel;
	}
	
	public CloudDisplayPanel getCloudPanel() {
		return cloudPanel;
	}
	

	private CytoPanelComponent wrapInCytoPanel(final JPanel panel, final CytoPanelName compassPoint, final String title) {
		return new CytoPanelComponent() {
			public String getTitle() {
				return title;
			}
			public Icon getIcon() {
				return null;
			}
			public CytoPanelName getCytoPanelName() {
				return compassPoint;
			}
			public Component getComponent() {
				return panel;
			}
		};
	}
}
