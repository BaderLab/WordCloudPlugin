package org.baderlab.wordcloud.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.baderlab.wordcloud.internal.CyActivator;
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
	private CytoPanelComponent inputComponent;
	private CytoPanelComponent cloudComponent;
	
	private DockCallback callback;
	
	private JFrame dialog;
	private State state;
	
	
	
	public DualPanelDocker(SemanticSummaryInputPanel inputPanel, CloudDisplayPanel cloudPanel, 
			               CySwingApplication swingApplication, CyServiceRegistrar registrar) {
		this.inputPanel = inputPanel;
		this.cloudPanel = cloudPanel;
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		
		Icon icon = getIcon();
		this.inputComponent = wrapInCytoPanel(inputPanel, CytoPanelName.WEST, "WordCloud", icon);
		this.cloudComponent = wrapInCytoPanel(cloudPanel, CytoPanelName.SOUTH, "WordCloud Display", icon);
		
		state = State.UNDOCKED;
		flip(); // this will initially dock it
	}
	
	
	public void setCallback(DockCallback callback) {
		this.callback = callback;
	}
	
	private Icon getIcon() {
		URL url = CyActivator.class.getResource("wordcloud_logo_v6_16by16.png");
		return url == null ? null : new ImageIcon(url);
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
	
	public void dispose() {
		registrar.unregisterService(inputComponent, CytoPanelComponent.class);
		registrar.unregisterService(cloudComponent, CytoPanelComponent.class);
		if(dialog != null) {
			dialog.removeWindowListener(dialog.getWindowListeners()[0]);
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			dialog = null;
		}
	}
	
	
	private void dock() {
		if(dialog != null) {
			dialog.removeWindowListener(dialog.getWindowListeners()[0]);
			dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			dialog.dispose();
			dialog = null;
		}
		
		registrar.registerService(inputComponent, CytoPanelComponent.class, new Properties());
		registrar.registerService(cloudComponent, CytoPanelComponent.class, new Properties());
		
		cloudPanel.setBackground(cloudPanel.getParent().getBackground());
		
		// Bring to front
		bringToFront(inputComponent);
		bringToFront(cloudComponent);
	}
	
	
	
	private void undock() {
		registrar.unregisterService(inputComponent, CytoPanelComponent.class);
		registrar.unregisterService(cloudComponent, CytoPanelComponent.class);
		
		dialog = new JFrame("WordCloud");
		
		dialog.getContentPane().add(inputPanel, BorderLayout.WEST);
		dialog.getContentPane().add(cloudPanel, BorderLayout.CENTER);
		cloudPanel.setBackground(cloudPanel.getParent().getBackground());
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		dialog.setPreferredSize(new Dimension(inputPanel.getPreferredSize().width * 3, 700));
		dialog.pack();
		dialog.setVisible(true);
		
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
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
	

	private CytoPanelComponent wrapInCytoPanel(final JPanel panel, final CytoPanelName compassPoint, final String title, final Icon icon) {
		return new CytoPanelComponent() {
			public String getTitle() {
				return title;
			}
			public Icon getIcon() {
				return icon;
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
