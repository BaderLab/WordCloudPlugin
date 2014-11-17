/*
 File: SemanticSummaryInputPanel.java
 
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

package org.baderlab.wordcloud.internal.ui.cloud;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.baderlab.wordcloud.internal.SelectionUtils;
import org.baderlab.wordcloud.internal.cluster.CloudDisplayStyles;
import org.baderlab.wordcloud.internal.cluster.CloudInfo;
import org.baderlab.wordcloud.internal.cluster.CloudWordInfo;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.ui.CloudTaskManager;
import org.baderlab.wordcloud.internal.ui.DualPanelDocker;
import org.baderlab.wordcloud.internal.ui.DualPanelDocker.DockCallback;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * The CloudDisplayPanel class defines the panel that displays a Semantic 
 * Summary tag cloud in the South data panel.
 */
public class CloudDisplayPanel extends JPanel implements CytoPanelComponent
{

	private static final long serialVersionUID = 5996569544692738989L;
	
	private JPanel tagCloudFlowPanel;//add JLabels here for words
	private JScrollPane cloudScroll; 
	private JRootPane rootPane;
	private JPanel loadingPanel;
	
	private final CloudTaskManager cloudTaskManager;
	private final UIManager uiManager;
	

	public CloudDisplayPanel(UIManager uiManager, CloudTaskManager cloudTaskManager) {
		this.uiManager = uiManager;
		this.cloudTaskManager = cloudTaskManager;
		
		setLayout(new BorderLayout());
		
		//Create JPanel containing tag words
		tagCloudFlowPanel = initializeTagCloud();
		cloudScroll = new JScrollPane(tagCloudFlowPanel);
		cloudScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		cloudScroll.setBorder(BorderFactory.createEmptyBorder());
		cloudScroll.setBackground(getBackground());
		
		rootPane = new JRootPane(); // use to layer dock button on top
		rootPane.getContentPane().setLayout(new BorderLayout());
		rootPane.getContentPane().add(cloudScroll, BorderLayout.CENTER);
		rootPane.setBackground(getBackground());
		
		add(rootPane, BorderLayout.CENTER);
	}
	
	
	
	public void setDocker(final DualPanelDocker docker) {
		final JButton dockButton = new JButton("Undock");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(dockButton);
		buttonPanel.setOpaque(false);
		
		// add some space so the floating button doesn't overlap the scrollbar
		int scrollWidth = ((Integer)javax.swing.UIManager.get("ScrollBar.width")).intValue(); 
		buttonPanel.add(Box.createRigidArea(new Dimension(scrollWidth, 0)));
		
		JPanel glassPane = (JPanel) rootPane.getGlassPane();
		glassPane.setLayout(new BorderLayout());
		glassPane.setVisible(true);
		glassPane.add(buttonPanel, BorderLayout.SOUTH);
		
		loadingPanel = new JPanel(new BorderLayout());
		loadingPanel.setOpaque(false);
		glassPane.add(loadingPanel, BorderLayout.NORTH);
		
		dockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				docker.flip();
			}
		});
		
		docker.setCallback(new DockCallback() {
			public void undocked() {
				dockButton.setText("Dock");
			}
			public void docked() {
				dockButton.setText("Undock");
			}
		});
		
	}
	
	
	/**
	 * Initialized a blank tag cloud JPanel object.
	 */
	private JPanel initializeTagCloud()
	{
		JPanel panel = new JPanel(new ModifiedFlowLayout(ModifiedFlowLayout.CENTER,30,25));
		return panel;
	}
	
	/**
	 * Clears all words from the CloudDisplay.
	 */
	private void clearCloud()
	{
		tagCloudFlowPanel.removeAll();
		tagCloudFlowPanel.setLayout(new ModifiedFlowLayout(ModifiedFlowLayout.CENTER, 30, 25));
		tagCloudFlowPanel.revalidate();
		cloudScroll.revalidate();
		tagCloudFlowPanel.updateUI();
	}
	
	
	/**
	 * Updates the tagCloudFlowPanel to include all of the words at the size they
	 * are defined for in params.
	 * 
	 * To clear the display pass a NetworkParameters.getNullCloud();
	 */
	public void updateCloudDisplay(final CloudParameters params)
	{
		if(!params.isAlreadyCalculated() && !params.getNetworkParams().isNullNetwork()) {
			String loading = params.isNullCloud() ? " Loading..." : " Loading " + params.getCloudName() + "...";
			JLabel label = new JLabel(loading);
			label.setOpaque(true);
			loadingPanel.add(label, BorderLayout.CENTER);
//			loadingPanel.revalidate();
		}
		
		cloudTaskManager.submit(params, new CloudTaskManager.Callback() {
			public void onFinish(CloudInfo cloudInfo) {
				if(cloudInfo != null && cloudInfo.isForCloud(uiManager.getCurrentCloud())) {
					displayCloud(cloudInfo);
				}
			}
			
		});
	}
	
	
	private synchronized void displayCloud(CloudInfo cloudInfo) {
		loadingPanel.removeAll(); // remove the loading label
		this.clearCloud();
		
		//Create a list of the words to include based on MaxWords parameters
		List<CloudWordInfo> copy = new ArrayList<CloudWordInfo>();
		List<CloudWordInfo> original = cloudInfo.getCloudWordInfoList();
		
		for (int i = 0; i < original.size(); i++)
		{
			CloudWordInfo curInfo = original.get(i);
			copy.add(curInfo);
		}
		Collections.sort(copy);
		
		int max = cloudInfo.getMaxWords();
		int numWords = copy.size();
		if (max < numWords) {
			copy.subList(max, numWords).clear();
		}
		
		//Loop through to create labels and add them
		int count = 0;
		
		Map<Integer,JPanel> clusters = new HashMap<Integer, JPanel>();
		List<CloudWordInfo> wordInfo = cloudInfo.getCloudWordInfoList();
		Iterator<CloudWordInfo> iter = wordInfo.iterator();
		
		
		//Loop while more words exist and we are under the max
		while(iter.hasNext() && (count < cloudInfo.getMaxWords()))
		{
			CloudWordInfo curWordInfo = iter.next();
			
			//Check that word in in our range
			if (copy.contains(curWordInfo))
			{
				int minOccurrence = cloudInfo.getMinWordOccurrence();
				if(cloudInfo.getSelectedCounts().get(curWordInfo.getWord()) >= minOccurrence) {
					Integer clusterNum = curWordInfo.getCluster();
					JLabel curLabel = createLabel(curWordInfo); 
				
					//Retrieve proper Panel
					JPanel curPanel;
					if (clusters.containsKey(clusterNum))
					{
						curPanel = clusters.get(clusterNum);
					}
					else
					{
						if (cloudInfo.getDisplayStyle().equals(CloudDisplayStyles.NO_CLUSTERING))
						{
							//curPanel =  new JPanel(new ModifiedFlowLayout(ModifiedFlowLayout.CENTER,10,0));
							curPanel = tagCloudFlowPanel;
							curPanel.setLayout(new ModifiedFlowLayout(ModifiedFlowLayout.CENTER, 10, 0));
						}
						else
						{
							curPanel = new JPanel(new ModifiedClusterFlowLayout(ModifiedFlowLayout.CENTER,10,0));
						}
						
						if (cloudInfo.getDisplayStyle().equals(CloudDisplayStyles.CLUSTERED_BOXES))
						{
							curPanel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.GRAY), new EmptyBorder(10,10,10,10)));
						}
					}
				
					curPanel.add(curLabel);
					clusters.put(clusterNum, curPanel);
					count++;
				}
			}
		}
		
		//Add all clusters to flow panel
		SortedSet<Integer> sortedSet = new TreeSet<Integer>(clusters.keySet());
		
		for(Iterator<Integer> iter2 = sortedSet.iterator(); iter2.hasNext();)
		{
			Integer clusterNum = iter2.next();
			JPanel curPanel = clusters.get(clusterNum);
			
			if (!curPanel.equals(tagCloudFlowPanel))
				tagCloudFlowPanel.add(curPanel);
		}
		
		tagCloudFlowPanel.revalidate();
		this.revalidate();
		this.updateUI();
		this.repaint();
	}
	
	
	private JLabel createLabel(final CloudWordInfo info) {
		JLabel label = info.createCloudLabel();
		
		//Listener stuff
		label.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent me)
			{
				JLabel clickedLabel = (JLabel)me.getComponent();
				String word = clickedLabel.getText();
				
				
				//Get all nodes containing this word
				Set<CyNode> nodes = info.getCloudInfo().getStringNodeMapping().get(word);
				
				CyNetwork network = info.getCloudInfo().getNetwork();
				if (network == null) {
					return;
				}
				SelectionUtils.setColumns(network.getDefaultNodeTable(), CyNetwork.SELECTED, Boolean.FALSE);
				SelectionUtils.setColumns(network.getDefaultEdgeTable(), CyNetwork.SELECTED, Boolean.FALSE);
				SelectionUtils.setColumns(network, nodes, CyNetwork.SELECTED, Boolean.TRUE);
			
			}
			
			public void mouseEntered(MouseEvent me)
			{
				JLabel clickedLabel = (JLabel)me.getComponent();
				clickedLabel.setForeground(new Color(0,200,255));
				clickedLabel.repaint();
			}
	
			public void mouseExited(MouseEvent me)
			{
				JLabel clickedLabel = (JLabel)me.getComponent();
				clickedLabel.setForeground(info.getTextColor());
				clickedLabel.repaint();
			}
		});
		
		return label;
	}
	
	
	/**
	 * Returns an image of the entire cloud, suitable for saving to a file.
	 */
	public RenderedImage createImage() {
		Dimension d = tagCloudFlowPanel.getSize();
		BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		tagCloudFlowPanel.paint( g2d );
		g2d.dispose();
		return image;
	}

	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "WordCloud Display";
	}


}
