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

package org.baderlab.wordcloud.internal.ui.input;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.baderlab.wordcloud.internal.model.next.CloudModelManager;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.baderlab.wordcloud.internal.model.next.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.baderlab.wordcloud.internal.ui.cloud.CloudDisplayStyles;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.CheckBoxJList;


/**
 * The SemanticSummaryInputPanel class defines the panel that appears for 
 * the Semantic Summary Plugin in the West control panel.  It contains fields
 * necessary for viewing and creating new Semantic Summaries.
 */

@SuppressWarnings("serial")
public class SemanticSummaryInputPanel extends JPanel {
	
	private static final int DEF_ROW_HEIGHT = 20;
	
	private final UIManager uiManager;
	
	
	//Text Fields
	private JFormattedTextField maxWordsTextField;
	private JFormattedTextField clusterCutoffTextField;
	
//	//JComboBox
	private JComboBox cmbStyle;
	
	//JLabels
	private JLabel networkLabel;
	
	//JListData
	private JList cloudList;
	private CloudListSelectionHandler handler;
	
	//Checkbox
//	private JCheckBox numExclusion;
//	private JCheckBox useNetworkCounts;
	private JCheckBox stemmer;
	
	//SliderBar
	private SliderBarPanel sliderPanel;
	
	//Checkbox list
	private CheckBoxJList attributeList;
	
	//Popup menu
	private JPopupMenu attributeSelectionPopupMenu;
	
	//Text Area
	private JTextArea attNames;

	private final CySwingApplication application;
	private final CyServiceRegistrar registrar;

	
	
	public SemanticSummaryInputPanel(
//			ModelManager modelManager, 
			CyApplicationManager applicationManager, 
			CySwingApplication application, 
			UIManager uiManager,
			CyServiceRegistrar registrar
//			FileUtil fileUtil 
//			SemanticSummaryManager cloudManager, 
//			PanelActivateAction pluginAction, 
//			CreateCloudAction createCloudAction, 
//			DeleteCloudAction deleteCloudAction, 
//			UpdateCloudAction updateCloudAction, 
//			SaveCloudAction saveCloudAction, 
//			CloudListSelectionHandlerFactory handlerFactory
			)
	{
		this.uiManager = uiManager;
//		this.modelManager = modelManager;
		this.application = application;
		this.registrar = registrar;
//		this.cloudManager = cloudManager;
//		this.createCloudAction = createCloudAction;
//		this.deleteCloudAction = deleteCloudAction;
//		this.updateCloudAction = updateCloudAction;
//		this.saveCloudAction = saveCloudAction;
//		this.handlerFactory = handlerFactory;
		
		
		setLayout(new BorderLayout());
		
		// Put the CloudList in a scroll pane
		JPanel cloudList = createCloudListPanel(applicationManager);
		JScrollPane cloudListScroll = new JScrollPane(cloudList);
		
		//Put the Options in a scroll pane
		JPanel optionsPanel = createOptionsPanel();
		JScrollPane optionsScroll = new JScrollPane(optionsPanel);
		optionsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		optionsScroll.setBorder(BorderFactory.createEmptyBorder());
		
		//Add button to bottom
//		JPanel bottomPanel = createBottomPanel();
		
		//Add all the vertically aligned components to the main panel
		add(cloudListScroll,BorderLayout.NORTH);
		add(optionsScroll,BorderLayout.CENTER);
//		add(bottomPanel,BorderLayout.SOUTH);
	
		
		RowsSetListener nodeSelectionListener = new RowsSetListener() {
			public void handleEvent(RowsSetEvent e) {
//				updateSyncCloud();
			}
		};
		registrar.registerService(nodeSelectionListener, RowsSetListener.class, new Properties());
	}

	
//	public void updateSyncCloud() {
//		CloudParameters cloudParams = cloudManager.getCurCloud();
//		if(cloudParams.getCloudNum() == -99) {
//			SemanticSummaryParameters networkParams = cloudParams.getNetworkParams();
//			CyNetwork network = networkParams.getNetwork();
//			
//			Set<CyNode> nodes = SelectionUtils.getSelectedNodes(network);
//			cloudParams.setSelectedNodes(nodes);
//			
//			//Retrieve values from input panel
//			cloudParams.retrieveInputVals(this);
//			
//			cloudParams.updateRatios();
//			cloudParams.calculateFontSizes();
//			
//			CloudDisplayPanel cloudPanel = cloudManager.getCloudWindow();
//			cloudPanel.updateCloudDisplay(cloudParams);
//			
//			
//			//Update the list of filter words and checkbox
//			refreshNetworkSettings();
//			
//		}
//	}
	
	
	//METHODS
	
	/**
	 * Creates the cloud list panel for the currently selected network.
	 * @return JPanel - the cloud list panel.
	 */
	private JPanel createCloudListPanel(CyApplicationManager applicationManager)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder());
		
		//Name of the network
		JPanel networkPanel = new JPanel();
		networkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		networkLabel = new JLabel();
		networkPanel.add(networkLabel);
		networkPanel.setBorder(BorderFactory.createEmptyBorder());
		
		// list of clouds
		cloudList = new JList(new DefaultListModel());
		cloudList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cloudList.setSelectedIndex(0);
		cloudList.setVisibleRowCount(10);
		cloudList.setFixedCellHeight(DEF_ROW_HEIGHT);
		cloudList.addMouseListener(new CloudListPopupMenuListener(uiManager, application, cloudList));
		
		handler = new CloudListSelectionHandler(uiManager);
		cloudList.addListSelectionListener(handler);
		
		JScrollPane listScrollPane = new JScrollPane(cloudList);
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		//Add to panel
		panel.add(networkPanel, BorderLayout.NORTH);
		panel.add(listScrollPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	
	/**
	 * Creates a panel that holds all of the user entered cloud parameters.
	 */
	private JPanel createOptionsPanel() {
		JPanel semAnalysis = createSemAnalysisPanel();
		JPanel cloudLayout = createCloudLayoutPanel();
		JPanel normalizationPanel = createNormalizationPanel();
		JPanel advancedSettings = createAdvancedSettingsPanel();
		JPanel excludedWordsPanel = createExcludedWordsPanel();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(semAnalysis);
		panel.add(Box.createVerticalStrut(5));
		panel.add(cloudLayout);
		panel.add(Box.createVerticalStrut(5));
		panel.add(normalizationPanel);
		panel.add(Box.createVerticalStrut(15));
		panel.add(advancedSettings);
		
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BorderLayout());
		newPanel.add(panel, BorderLayout.NORTH);
		newPanel.add(excludedWordsPanel, BorderLayout.SOUTH);
		return newPanel;
	}
	
	
	private JPanel createExcludedWordsPanel() {
		JPanel excludedWordsPanel = new JPanel();
		excludedWordsPanel.setLayout(new FlowLayout());
		
		JButton excludedWordsButton = new JButton("Excluded Words...");
		excludedWordsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NetworkParameters network = uiManager.getCurrentNetwork();
				WordSelectPanel wordSelectPanel = new WordSelectPanel(network.getFilter());
				JDialog dialog = wordSelectPanel.createDialog(application.getJFrame(), network.getNetworkName());
				dialog.setVisible(true);
//				network.networkChanged();
//				updateCloudAction.doRealAction();
			}
		});
		excludedWordsPanel.add(excludedWordsButton);
		
		JButton delimetersButton = new JButton("Delimiters...");
		delimetersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NetworkParameters network = uiManager.getCurrentNetwork();
				WordSelectPanel wordSelectPanel = new WordSelectPanel(network.getDelimeters());
				JDialog dialog = wordSelectPanel.createDialog(application.getJFrame(), network.getNetworkName());
				dialog.setVisible(true);
//				network.networkChanged();
//				updateCloudAction.doRealAction();
			}
		});
		excludedWordsPanel.add(delimetersButton);
		
		return excludedWordsPanel;
	}
	/**
	 * Creates a CollapsiblePanel that holds the Semantic Analysis information.
	 * @return CollapsiblePanel - semantic analysis panel interface.
	 */
	private JPanel createSemAnalysisPanel()
	{
		//CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Attribute Choice");
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		
		JPanel attributePanel = new JPanel();
		attributePanel.setLayout(new GridBagLayout());

	    //Testing of stuff
	    attributeList = new CheckBoxJList();
	    DefaultListModel model = new DefaultListModel();
	    attributeList.setModel(model);
	    attributeList.addPropertyChangeListener(CheckBoxJList.LIST_UPDATED, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				updateAttNames();
//				updateCloudAction.doRealAction();
			}
		});
	    
	    JScrollPane scrollPane = new JScrollPane();
	    scrollPane.setPreferredSize(new Dimension(300, 200));
	    scrollPane.setViewportView(createAttributePanel(attributeList));
	    
	    attributeSelectionPopupMenu = new JPopupMenu();
	    attributeSelectionPopupMenu.add(scrollPane);
	    
	    JButton attributeButton = new JButton("Edit");
	    attributeButton.setToolTipText("Edit nodes values to use for semantic analysis");
	    attributeButton.addMouseListener(new MouseAdapter()
	    {
	    	public void mouseClicked(MouseEvent e)
	    	{
	    		attributeSelectionPopupMenu.show(e.getComponent(), 0,e.getComponent().getPreferredSize().height);
	    	}
	    }
	    );

	    attNames = new JTextArea();
	    attNames.setColumns(15);
	    attNames.setRows(4);
	    attNames.setEditable(false);
	    JScrollPane attListPane = new JScrollPane();
	    attListPane.setPreferredSize(attNames.getPreferredSize());
	    attListPane.setViewportView(attNames);
	    attListPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    attListPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

	    JLabel attributeLabel = new JLabel("Current Values:");
		
		attributePanel.add(attributeLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		attributePanel.add(attributeButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		attributePanel.add(attListPane, new GridBagConstraints(0, 1, 2, 1, 1.0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));	
		    
	    refreshAttributeCMB();
		
		panel.add(attributePanel);
		
		return panel;
//		collapsiblePanel.getContentPane().add(panel,BorderLayout.NORTH);
//		return collapsiblePanel;
	}
	
	private Component createAttributePanel(final CheckBoxJList attributeList) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ListModel model = attributeList.getModel();
				int size = model.getSize();
				if (size == 0) {
					return;
				}
				attributeList.getSelectionModel().setSelectionInterval(0, size - 1);
			}
		});

		JButton deselectAllButton = new JButton("Deselect all");
		deselectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> selected = Collections.emptyList();
				attributeList.setSelectedItems(selected);
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(selectAllButton);
		buttonPanel.add(deselectAllButton);
		
		panel.add(buttonPanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(attributeList, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		return panel;
	}

	
	private JPanel createNormalizationPanel() {
		sliderPanel = new SliderBarPanel(0,1," Normalize", 10);
		
		String tooltip =
			"<html>" + "Determines how much weight to give the whole network when normalizing the selected nodes" + "<br>" +
			"<b>Acceptable Values:</b> greater than or equal to 0 and less than or equal to 1" + "</html>";
		sliderPanel.setToolTipText(tooltip);
		
		return sliderPanel;
	}
	
	
	
	/**
	 * Creates a CollapsiblePanel that holds the display settings information.
	 * @return CollapsiblePanel - display settings panel interface.
	 */
	private JPanel createAdvancedSettingsPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Advanced");
		
		//Used to retrieve defaults
//		CloudParameters params = cloudManager.getCurCloud();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//Max words input
		JLabel maxWordsLabel = new JLabel("Max Number of Words");
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		intFormat.setParseIntegerOnly(true);
		maxWordsTextField = new JFormattedTextField(intFormat);
		maxWordsTextField.setColumns(7);
		maxWordsTextField.setValue(40);  
//		(params.getDefaultMaxWords()); //Set to default initially
		maxWordsTextField.addPropertyChangeListener(new SemanticSummaryInputPanel.FormattedTextFieldAction());
		
		StringBuffer buf = new StringBuffer();
		buf.append("<html>" + "Sets a limit on the number of words to display in the cloud" + "<br>");
		buf.append("<b>Acceptable Values:</b> greater than or equal to 0" + "</html>");
		maxWordsTextField.setToolTipText(buf.toString());
		//Max words panel
		JPanel maxWordsPanel = new JPanel();
		maxWordsPanel.setLayout(new BorderLayout());
		maxWordsPanel.add(maxWordsLabel, BorderLayout.WEST);
		maxWordsPanel.add(maxWordsTextField, BorderLayout.EAST);
		
		
		//Clustering Cutoff
		JLabel clusterCutoffLabel = new JLabel("Word Aggregation Cutoff");
		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(false);
		clusterCutoffTextField = new JFormattedTextField(decFormat);
		clusterCutoffTextField.setColumns(7);
		clusterCutoffTextField.setValue(20);
//				params.getDefaultClusterCutoff()); //Set to default initially
		clusterCutoffTextField.addPropertyChangeListener(new SemanticSummaryInputPanel.FormattedTextFieldAction());
		
		buf = new StringBuffer();
		buf.append("<html>" + "Cutoff for placing two words in the same cluster - ratio of the observed joint probability of the words to their joint probability if the words appeared independently of each other" + "<br>");
		buf.append("<b>Acceptable Values:</b> greater than or equal to 0" + "</html>");
		clusterCutoffTextField.setToolTipText(buf.toString());
		
		//Clustering Cutoff Panel
		JPanel clusterCutoffPanel = new JPanel();
		clusterCutoffPanel.setLayout(new BorderLayout());
		clusterCutoffPanel.add(clusterCutoffLabel, BorderLayout.WEST);
		clusterCutoffPanel.add(clusterCutoffTextField, BorderLayout.EAST);
		
		//Create Checkbox
		stemmer = new JCheckBox("Enable Stemming");
		
		buf = new StringBuffer();
		buf.append("<html>" + "Causes all words to be stemmed using the Porter Stemmer algorithm." + "<br>");
		buf.append("<b>Notice:</b> This will allow words with a similar stem to map to the same word." + "<br>");
		buf.append("However, words stems may not be what you expect." + "</html>");
		stemmer.setToolTipText(buf.toString());
//		stemmer.addActionListener(this);
		stemmer.setSelected(false);
//		stemmer.setEnabled(false);
		
		JPanel stemmingPanel = new JPanel();
		stemmingPanel.setLayout(new BorderLayout());
		stemmingPanel.add(stemmer, BorderLayout.WEST);
		
		
		//Add components to main panel
		panel.add(maxWordsPanel);
		panel.add(clusterCutoffPanel);
		panel.add(stemmingPanel);
		
		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		return collapsiblePanel;
	}
	
	
	
	/**
	 * Creates a CollapsiblePanel that holds the Cloud Layout information.
	 */
	private JPanel createCloudLayoutPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		JPanel cloudLayoutPanel = new JPanel();
		cloudLayoutPanel.setLayout(new GridBagLayout());

		JLabel cloudStyleLabel = new JLabel("Cloud Style: ");
		
		WidestStringComboBoxModel wscbm = new WidestStringComboBoxModel();
		cmbStyle = new JComboBox(wscbm);
		cmbStyle.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
		cmbStyle.setEditable(false);
	    Dimension d = cmbStyle.getPreferredSize();
	    cmbStyle.setPreferredSize(new Dimension(15, d.height));

	    StringBuffer toolTip = new StringBuffer();
	    toolTip.append("<html>" + "--Visual style for the cloud layout--" +"<br>");
	    toolTip.append("<b>Clustered:</b> If a style with clustering is selected, then the cloud will be comprised of groups of words." + "<br>");
	    toolTip.append("Each cluster is build by analyzing which words appear next to each other and what order they appear." + "<br>");
	    toolTip.append("<b> No Clustering:</b> When a non-clustering option is selected, words appear in decreasing order of of size.");
	    cmbStyle.setToolTipText(toolTip.toString());

		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		cloudLayoutPanel.add(cloudStyleLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 10, 0, 0);
		cloudLayoutPanel.add(cmbStyle, gridBagConstraints);
	    
	    DefaultComboBoxModel cmb = ((DefaultComboBoxModel)cmbStyle.getModel());
		cmb.removeAllElements();
		cmb.addElement(CloudDisplayStyles.CLUSTERED_STANDARD);
		cmb.addElement(CloudDisplayStyles.CLUSTERED_BOXES);
		cmb.addElement(CloudDisplayStyles.NO_CLUSTERING);
		cmbStyle.setSelectedItem(CloudDisplayStyles.DEFAULT_STYLE);
		cmbStyle.repaint();
		
		panel.add(cloudLayoutPanel);
		return panel;
	}
	

	
	
	public void setCurrentCloud(CloudParameters params) {
		cloudList.removeListSelectionListener(handler);
		
		List<CloudParameters> networkClouds = params.getNetworkParams().getClouds();
		DefaultListModel listModel = new DefaultListModel();
		for(CloudParameters cloud : networkClouds) {
			listModel.addElement(cloud.getCloudName());
		}
		cloudList.setModel(listModel);
		
		String cloudName = params.getCloudName();
		int index = listModel.lastIndexOf(cloudName);
		cloudList.setSelectedIndex(index);
		
		networkLabel.setText(params.getNetworkParams().getNetworkName());
		
		// MKTODO: update all the other controls to show this cloud
		List<String> attributeNames = params.getAttributeNames();
		if (attributeNames == null) {
			attributeNames = Collections.emptyList();
		}
		setAttributeNames(attributeNames);
		maxWordsTextField.setValue(params.getMaxWords());
		clusterCutoffTextField.setValue(params.getClusterCutoff());
		cmbStyle.setSelectedItem(params.getDisplayStyle());
		setupNetworkNormalization(params);
		updateStemmingBox();
		
		cloudList.addListSelectionListener(handler);
	}
	
	
	private void setAttributeNames(List<String> names) {
		attributeList.setSelectedItems(names);
		updateAttNames();
	}
	
	
	public void updateNetworkName(String networkName) {
		networkLabel.setText(networkName);
	}
	
	
//	/**
//	 * Sets all user input fields to their default values.
//	 */
//	public void setUserDefaults()
//	{
//		CloudParameters params = cloudManager.getCurCloud();
//		
//		attributeList.setSelectedIndex(0);
//		updateAttNames();
////		maxWordsTextField.setValue(params.getDefaultMaxWords());
////		clusterCutoffTextField.setValue(params.getDefaultClusterCutoff());
//		cmbStyle.setSelectedItem(params.getDefaultDisplayStyle());
//		
//		this.setupNetworkNormalization(params);
//		sliderPanel.setNetNormValue(params.getDefaultNetWeight());
//		
//		this.refreshNetworkSettings();
//	}
//	

	
	private void updateStemmingBox() {
		NetworkParameters networkParams = uiManager.getCurrentNetwork();
		boolean val = networkParams.getIsStemming();
		stemmer.setSelected(val);
	}
	
	
	/**
	 * Sets up the network normalization panel for the given cloud.
	 */
	private void setupNetworkNormalization(CloudParameters params) {
		//Turn off slider listener
		ChangeListener[] listeners = sliderPanel.getSlider().getChangeListeners();
		for (int i = 0; i < listeners.length; i++) {
			sliderPanel.getSlider().removeChangeListener(listeners[i]);
		}
		
		sliderPanel.setNetNormValue(params.getNetWeightFactor());
		sliderPanel.setLabel(sliderPanel.getSlider().getValue());
		
		//Turn back on slider listener
		for (int i = 0; i < listeners.length; i++) {
			sliderPanel.getSlider().addChangeListener(listeners[i]);
		}
	}
	
	
	private void updateAttNames() {
		StringBuilder buffer = new StringBuilder();
		if (!attributeList.isSelectionEmpty()) {
			Object[] names = attributeList.getSelectedValues();
			for (int i = 0; i < names.length; i++) {
				if (names[i] instanceof String) {
					String curName = (String)names[i];
					if (i > 0) {
						buffer.append("\n");
					}
					buffer.append(curName);
				}
			}
		}
		attNames.setText(buffer.toString());
	}
	
	
	/**
	 * Update the attribute list in the attribute combobox.
	 */
	private void updateCMBAttributes() {
		//Turn off listeners
		ListSelectionListener[] listeners = attributeList.getListSelectionListeners();
		for (int i = 0; i < listeners.length; i++) {
			ListSelectionListener curListener = listeners[i];
			attributeList.removeListSelectionListener(curListener);
		}
		
		//Updated GUI
		DefaultListModel listModel;
		listModel = ((DefaultListModel)attributeList.getModel());
		listModel.removeAllElements();
		
		
		CyNetwork network = uiManager.getCurrentNetwork().getNetwork();
		if (network != null) {
			for (String name : CloudModelManager.getColumnNames(network, CyNode.class)) {
				listModel.addElement(name);
			}
		}
		
		//Turn listeners back on
		for (int i = 0; i < listeners.length; i++) {
			ListSelectionListener curListener = listeners[i];
			attributeList.addListSelectionListener(curListener);
		}
	}
	
	
	/**
	 * Refreshes the list of attributes
	 */
	public void refreshAttributeCMB() {
		updateCMBAttributes();
		CloudParameters curCloud = uiManager.getCurrentCloud();
		
		//Updated GUI
		List<String> curAttList = curCloud.getAttributeNames();
		
		if (curAttList == null) {
			curAttList = CloudModelManager.getColumnNames(curCloud.getNetworkParams().getNetwork(), CyNode.class);
		}
		
		attributeList.setSelectedItems(curAttList);
		attributeList.repaint();
	}
	
	
	/**
	 * Private Class to ensure that text fields are being set properly
	 */
	private class FormattedTextFieldAction implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			JFormattedTextField source = (JFormattedTextField) e.getSource();
			
//			CloudParameters params = cloudManager.getCurCloud();
			
			String message = "The value you have entered is invalid. \n";
			boolean invalid = false;

			
//			//Max Words
//			if (source == maxWordsTextField)
//			{
//				Number value = (Number) maxWordsTextField.getValue();
//				if ((value != null) && (value.intValue() >= 0))
//				{
//					//All is well - do nothing
//				}
//				else
//				{
//					Integer defaultMaxWords = params.getDefaultMaxWords();
//					maxWordsTextField.setValue(defaultMaxWords);
//					message += "The maximum number of words to display must be greater than or equal to 0.";
//					invalid = true;
//				}
//			}// end max Words
//			
//			else if (source == clusterCutoffTextField)
//			{
//				Number value = (Number) clusterCutoffTextField.getValue();
//				if ((value != null) && (value.doubleValue() >= 0.0))
//				{
//					//All is well - leave it be
//				}
//				else
//				{
//					Double defaultClusterCutoff = params.getDefaultClusterCutoff();
//					clusterCutoffTextField.setValue(defaultClusterCutoff);
//					message += "The cluster cutoff must be greater than or equal to 0";
//					invalid = true;
//				}
//			}
//			
//			if (source == addWordTextField)
//			{
//				String value = (String)addWordTextField.getText();
//				if (value.equals("") || value.matches("[\\w]*"))
//				{ 
//					//All is well, leave it be
//				}
//				else
//				{
//					//addWordTextField.setValue("");
//					//message += "You can only add a word that contains letters or numbers and no spaces";
//					//invalid = true;
//				}
//			}
//			
//			if (invalid)
//				JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
		}
	}	
}
