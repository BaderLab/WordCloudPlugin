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

package org.baderlab.wordcloud.internal;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.CheckBoxJList;
import org.cytoscape.util.swing.FileUtil;


/**
 * The SemanticSummaryInputPanel class defines the panel that appears for 
 * the Semantic Summary Plugin in the West control panel.  It contains fields
 * necessary for viewing and creating new Semantic Summaries.
 * 
 * @author Layla Oesper
 * @version 1.0
 */

public class SemanticSummaryInputPanel extends JPanel implements ItemListener, 
			ActionListener
{
	
	//VARIABLES
	private static final long serialVersionUID = 2453517387682663100L;
	
	private DecimalFormat decFormat; //used in formatted text fields with decimals
	private NumberFormat intFormat; //used in formatted text fields with integers
	
	
	//Text Fields
	private JFormattedTextField maxWordsTextField;
	private JFormattedTextField clusterCutoffTextField;
	private JTextField addWordTextField;
	
	//JComboBox
	private JComboBox cmbRemoval;
	private JComboBox cmbStyle;
	private JComboBox cmbDelimiterRemoval;
	private JComboBox cmbDelimiterAddition;
	
	//JLabels
	private JLabel networkLabel;
	
	//JListData
	private DefaultListModel listValues;
	private JList cloudList;
	private CloudListSelectionHandler handler;
	
	//Buttons
	private JButton removeWordButton;
	private JButton addWordButton;
	private JButton addDelimiterButton;
	private JButton removeDelimiterButton;
	private JButton createNetworkButton;
	private JButton saveCloudButton;
	
	//Checkbox
	private JCheckBox numExclusion;
	private JCheckBox useNetworkCounts;
	private JCheckBox stemmer;
	
	//SliderBar
	private SliderBarPanel sliderPanel;
	
	//Checkbox list
	private CheckBoxJList attributeList;
	
	//Popup menu
	private JPopupMenu attributeSelectionPopupMenu;
	
	//Text Area
	private JTextArea attNames;

	private final ModelManager modelManager;

	private final CySwingApplication application;

	private SemanticSummaryManager cloudManager;

	private CreateCloudAction createCloudAction;

	private DeleteCloudAction deleteCloudAction;

	private UpdateCloudAction updateCloudAction;

	private SaveCloudAction saveCloudAction;

	private CloudListSelectionHandlerFactory handlerFactory;
	
	//String Constants for Separators in remove word combo box
	private static final String addedSeparator = "--Added Words--";
	private static final String flaggedSeparator = "--Flagged Words--";
	private static final String stopSeparator = "--Stop Words--";
	
	//String COnstants for Separators in delimiter combo boxes
	private static final String commonDelimiters = "--Common Delimiters--";
	private static final String userAddDelimiters = "--User Defined--";
	private static final String selectMe = "Select to add your own";
	
	private static final int DEF_ROW_HEIGHT = 20;

	
	//CONSTRUCTORS
	public SemanticSummaryInputPanel(ModelManager modelManager, CyApplicationManager applicationManager, CySwingApplication application, FileUtil fileUtil, SemanticSummaryManager cloudManager, SemanticSummaryPluginAction pluginAction, CreateCloudAction createCloudAction, DeleteCloudAction deleteCloudAction, UpdateCloudAction updateCloudAction, SaveCloudAction saveCloudAction, CloudListSelectionHandlerFactory handlerFactory)
	{
		this.modelManager = modelManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.createCloudAction = createCloudAction;
		this.deleteCloudAction = deleteCloudAction;
		this.updateCloudAction = updateCloudAction;
		this.saveCloudAction = saveCloudAction;
		this.handlerFactory = handlerFactory;
		
		decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(false);
		
		intFormat = NumberFormat.getIntegerInstance();
		intFormat.setParseIntegerOnly(true);
		
		setLayout(new BorderLayout());
		
		//INITIALIZE PARAMETERS
		
		//Create the three main panels: CloudList, Options, and Bottom
		
		// Put the CloudList in a scroll pane
		JPanel cloudList = createCloudListPanel(applicationManager);
		JScrollPane cloudListScroll = new JScrollPane(cloudList);
		
		//Put the Options in a scroll pane
		JPanel optionsPanel = createOptionsPanel();
		JScrollPane optionsScroll = new JScrollPane(optionsPanel);
		optionsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//Add button to bottom
		JPanel bottomPanel = createBottomPanel();
		
		//Add all the vertically aligned components to the main panel
		add(cloudListScroll,BorderLayout.NORTH);
		add(optionsScroll,BorderLayout.CENTER);
		add(bottomPanel,BorderLayout.SOUTH);
	
	}
	
	//METHODS
	
	/**
	 * Creates the cloud list panel for the currently selected network.
	 * @return JPanel - the cloud list panel.
	 */
	public JPanel createCloudListPanel(CyApplicationManager applicationManager)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		//Name of the network
		JPanel networkPanel = new JPanel();
		networkPanel.setLayout(new FlowLayout());
		networkLabel = new JLabel();
		networkPanel.add(networkLabel);
		
		//List of Clouds
		listValues = new DefaultListModel();
		
		cloudList = new JList(listValues);
		cloudList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cloudList.setSelectedIndex(0);
		cloudList.setVisibleRowCount(10);
		cloudList.setFixedCellHeight(DEF_ROW_HEIGHT);
		cloudList.addMouseListener(new CloudListMouseListener(this, cloudManager));
		
		//Setup Selection Listener
		ListSelectionModel listSelectionModel = cloudList.getSelectionModel();
		handler = handlerFactory.createHandler(this);
		listSelectionModel.addListSelectionListener(handler);
		JScrollPane listScrollPane = new JScrollPane(cloudList);
		
		//Add to panel
		panel.add(networkPanel, BorderLayout.NORTH);
		panel.add(listScrollPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	
	/**
	 * Creates a collapsable panel that holds all of the user entered
	 * cloud parameters.
	 * 
	 * @return collapsiblePanel - main panel with cloud parameters
	 */
	//public CollapsiblePanel createOptionsPanel()
	public JPanel createOptionsPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Cloud Parameters");
		collapsiblePanel.setCollapsed(false);
		collapsiblePanel.getTitleComponent().setToolTipText("Parameters that can be set differently for each individual cloud");
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//Semantic Analysis Panel
		CollapsiblePanel semAnalysis = createSemAnalysisPanel();
		semAnalysis.setCollapsed(false);
		
		//Display Settings
		CollapsiblePanel displaySettings = createDisplaySettingsPanel();
		displaySettings.setCollapsed(true);
		
		
		//Cloud Layout
		CollapsiblePanel cloudLayout = createCloudLayoutPanel();
		cloudLayout.setCollapsed(true);
		
		//Add all Panels
		panel.add(semAnalysis);
		panel.add(displaySettings);
		panel.add(cloudLayout);
		
		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		
		//Network Level Panel
		CollapsiblePanel collapsiblePanel2 = new CollapsiblePanel("Text Processing Parameters");
		collapsiblePanel2.setCollapsed(false);
		collapsiblePanel2.getTitleComponent().setToolTipText("Text processing parameters that will be applied to all clouds created from the current network");
		
		JPanel networkPanel = new JPanel();
		networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.Y_AXIS));
		
		//Word Exclusion
		CollapsiblePanel exclusionList = createExclusionListPanel();
		exclusionList.setCollapsed(true);
		
		networkPanel.add(exclusionList);
		
		//Delimiter/Tokenization Panel
		CollapsiblePanel tokenizationPanel = createTokenizationPanel();
		tokenizationPanel.setCollapsed(true);
		
		networkPanel.add(tokenizationPanel);
		
		//Stemmer Panel
		CollapsiblePanel stemmingPanel = createStemmingPanel();
		stemmingPanel.setCollapsed(true);
		
		networkPanel.add(stemmingPanel);
		
		//Add to collapsible panel
		collapsiblePanel2.getContentPane().add(networkPanel, BorderLayout.NORTH);
		
		//Container Panel for Cloud and Network parameters
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BorderLayout());
		newPanel.add(collapsiblePanel, BorderLayout.NORTH);
		newPanel.add(collapsiblePanel2, BorderLayout.CENTER);
				
		return newPanel;
	}
	
	/**
	 * Creates a CollapsiblePanel that holds the Semantic Analysis information.
	 * @return CollapsiblePanel - semantic analysis panel interface.
	 */
	private CollapsiblePanel createSemAnalysisPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Attribute Choice");
		
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
				updateCloudAction.doRealAction();
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
		
		collapsiblePanel.getContentPane().add(panel,BorderLayout.NORTH);
		return collapsiblePanel;
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

	/**
	 * Creates a CollapsiblePanel that holds the display settings information.
	 * @return CollapsiblePanel - display settings panel interface.
	 */
	private CollapsiblePanel createDisplaySettingsPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Advanced");
		
		//Used to retrieve defaults
		CloudParameters params = cloudManager.getCurCloud();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//Max words input
		JLabel maxWordsLabel = new JLabel("Max Number of Words");
		maxWordsTextField = new JFormattedTextField(intFormat);
		maxWordsTextField.setColumns(10);
		maxWordsTextField.setValue(params.getDefaultMaxWords()); //Set to default initially
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
		
		
		//buf = new StringBuffer();
		//buf.append("<html>" + "Determines how much weight to give the whole network when normalizing the selected nodes" + "<br>");
		//buf.append("<b>Acceptable Values:</b> greater than or equal to 0 and less than or equal to 1" + "</html>");
		
		
		//Clustering Cutoff
		JLabel clusterCutoffLabel = new JLabel("Word Aggregation Cutoff");
		clusterCutoffTextField = new JFormattedTextField(decFormat);
		clusterCutoffTextField.setColumns(3);
		clusterCutoffTextField.setValue(params.getDefaultClusterCutoff()); //Set to default initially
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
		
		
		//New Network Normalization Panel
		JPanel netNormalizationPanel = new JPanel();
		netNormalizationPanel.setLayout(new GridBagLayout());
		
		//Checkbox
		useNetworkCounts = new JCheckBox("Normalize word size using selection/network ratios");
		useNetworkCounts.setToolTipText("Enables word size to be calculated using using counts over the entire network, rather than just selected nodes");
		useNetworkCounts.addActionListener(this);
		useNetworkCounts.setSelected(false);
		useNetworkCounts.setEnabled(false);
		
		sliderPanel = new SliderBarPanel(0,1,"Network Normalization", "Network Normalization", 10, cloudManager, updateCloudAction);
		sliderPanel.setEnabled(false);
		sliderPanel.setVisible(false);
		
		buf = new StringBuffer();
		buf.append("<html>" + "Determines how much weight to give the whole network when normalizing the selected nodes" + "<br>");
		buf.append("<b>Acceptable Values:</b> greater than or equal to 0 and less than or equal to 1" + "</html>");
		sliderPanel.setToolTipText(buf.toString());
		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		netNormalizationPanel.add(useNetworkCounts, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0,0,0,0);
		netNormalizationPanel.add(sliderPanel, gridBagConstraints);
		
		//Add components to main panel
		panel.add(maxWordsPanel);
		panel.add(clusterCutoffPanel);
		panel.add(netNormalizationPanel);
		
		
		//Add to collapsible panel
		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		
		return collapsiblePanel;
	}
	
	/**
	 * Creates a CollapsiblePanel that holds the word exclusion list information.
	 * @return CollapsiblePanel - word exclusion list panel interface.
	 */
	private CollapsiblePanel createExclusionListPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Word Exclusion List");
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		//Add Word
		JLabel addWordLabel = new JLabel("Add Word");
		addWordTextField = new JFormattedTextField();
		addWordTextField.setColumns(15);
		
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		if (networkParams.equals(cloudManager.getNullSemanticSummary()))
			addWordTextField.setEditable(false);
		else
			addWordTextField.setEditable(true);
		
		addWordTextField.setText("");
		addWordTextField.addPropertyChangeListener(new SemanticSummaryInputPanel.FormattedTextFieldAction());
		
		StringBuffer buf = new StringBuffer();
		buf.append("<html>" + "Allows for specification of an additional word to be excluded when doing semantic analysis" + "<br>");
		buf.append("<b>Acceptable Values:</b> Only alpha numeric values - no spaces allowed" + "</html>");
		addWordTextField.setToolTipText(buf.toString());
		
		addWordButton = new JButton();
		addWordButton.setText("Add");
		addWordButton.setEnabled(false);
		addWordButton.addActionListener(this);
		
		//Word panel
		JPanel wordPanel = new JPanel();
		wordPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(addWordLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5,10,0,10);
		wordPanel.add(addWordTextField, gridBagConstraints);
		
		//Button stuff
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(addWordButton, gridBagConstraints);
		
		
		// Word Removal Label
		JLabel removeWordLabel = new JLabel("Remove Word");
		
		//Word Removal Combo Box
		WidestStringComboBoxModel wscbm = new WidestStringComboBoxModel();
		cmbRemoval = new JComboBox(wscbm);
		cmbRemoval.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
		cmbRemoval.setEditable(false);
	    Dimension d = cmbRemoval.getPreferredSize();
	    cmbRemoval.setPreferredSize(new Dimension(15, d.height));
	    cmbRemoval.addItemListener(this);
	    cmbRemoval.setToolTipText("Allows for selection a word to remove from the semantic analysis exclusion list");

	    //Word Removal Button
	    removeWordButton = new JButton();
	    removeWordButton.setText("Remove");
	    removeWordButton.setEnabled(false);
	    removeWordButton.addActionListener(this);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(removeWordLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 10, 0, 10);
		wordPanel.add(cmbRemoval, gridBagConstraints);
		
		//Button stuff
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
		wordPanel.add(removeWordButton, gridBagConstraints);

		refreshRemovalCMB();
		
		//Number Exclusion Stuff
		
		//Checkbox
		numExclusion = new JCheckBox("Add the numbers 0 - 999 to the word exclusion list");
		
		buf = new StringBuffer();
		buf.append("<html>" + "Causes numbers in the range 0 - 999 that appear as <b>separate words</b> to be excluded" + "<br>");
		buf.append("<b>Hint:</b> To exclude numbers that appear within a word, either add the entire word to the exclusion list" + "<br>");
		buf.append("or add the specific number to the list of delimiters used for word tokenization" + "</html>");
		numExclusion.setToolTipText(buf.toString());
		numExclusion.addActionListener(this);
		numExclusion.setSelected(false);
		numExclusion.setEnabled(false);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(numExclusion, gridBagConstraints);
		
		//Add components to main panel
		panel.add(wordPanel);
		
		//Add to collapsible panel
		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		
		return collapsiblePanel;
	}
	
	/**
	 * Creates a CollapsiblePanel that holds the word tokenization information.
	 * @return CollapsiblePanel - word tokenization panel interface.
	 */
	private CollapsiblePanel createTokenizationPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Word Tokenization");
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		//Add Delimiter
		JLabel addDelimiterLabel = new JLabel("Add Delimiter");

		//Delimiter Addition Combo Box
		WidestStringComboBoxModel wscbm = new WidestStringComboBoxModel();
		cmbDelimiterAddition = new JComboBox(wscbm);
		cmbDelimiterAddition.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
		cmbDelimiterAddition.setEditable(false);
	    Dimension d = cmbDelimiterAddition.getPreferredSize();
	    cmbDelimiterAddition.setPreferredSize(new Dimension(15, d.height));
	    cmbDelimiterAddition.addItemListener(this);
	    
	    StringBuffer buf = new StringBuffer();
		buf.append("<html>" + "Allows for specification of an additional delimiter to be used when tokenizing nodes" + "<br>");
		buf.append("<b>Acceptable Values:</b> Values entered must have proper escape character if necessary" + "</html>");
		cmbDelimiterAddition.setToolTipText(buf.toString());
		
		addDelimiterButton = new JButton();
		addDelimiterButton.setText("Add");
		addDelimiterButton.setEnabled(false);
		addDelimiterButton.addActionListener(this);
		
		//Word panel
		JPanel wordPanel = new JPanel();
		wordPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(addDelimiterLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5,10,0,10);
		wordPanel.add(cmbDelimiterAddition, gridBagConstraints);
		
		//Button stuff
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(addDelimiterButton, gridBagConstraints);
		
		
		// Word Removal Label
		JLabel removeDelimiterLabel = new JLabel("Remove Delimiter");
		
		//Word Removal Combo Box
		wscbm = new WidestStringComboBoxModel();
		cmbDelimiterRemoval = new JComboBox(wscbm);
		cmbDelimiterRemoval.addPopupMenuListener(new WidestStringComboBoxPopupMenuListener());
		cmbDelimiterRemoval.setEditable(false);
	    d = cmbDelimiterRemoval.getPreferredSize();
	    cmbDelimiterRemoval.setPreferredSize(new Dimension(15, d.height));
	    cmbDelimiterRemoval.addItemListener(this);
	    cmbDelimiterRemoval.setToolTipText("Allows for selection of a delimiter to remove from the list used when tokenizing");

	    //Word Removal Button
	    removeDelimiterButton = new JButton();
	    removeDelimiterButton.setText("Remove");
	    removeDelimiterButton.setEnabled(false);
	    removeDelimiterButton.addActionListener(this);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(removeDelimiterLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 10, 0, 10);
		wordPanel.add(cmbDelimiterRemoval, gridBagConstraints);
		
		//Button stuff
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
		wordPanel.add(removeDelimiterButton, gridBagConstraints);

		updateDelimiterCMBs();
		
		//Add components to main panel
		panel.add(wordPanel);
		
		//Add to collapsible panel
		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		
		return collapsiblePanel;
	}
	
	/**
	 * Creates a CollapsiblePanel that holds the word stemming information.
	 * @return CollapsiblePanel - word stemming panel interface.
	 */
	private CollapsiblePanel createStemmingPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Word Stemming");
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		StringBuffer buf = new StringBuffer();
		
		
		//Word panel
		JPanel wordPanel = new JPanel();
		//wordPanel.setLayout(new GridBagLayout());
		wordPanel.setLayout(new BorderLayout());
		
		//Create Checkbox
		stemmer = new JCheckBox("Enable Stemming");
		
		buf = new StringBuffer();
		buf.append("<html>" + "Causes all words to be stemmed using the Porter Stemmer algorithm." + "<br>");
		buf.append("<b>Notice:</b> This will allow words with a similar stem to map to the same word." + "<br>");
		buf.append("However, words stems may not be what you expect." + "</html>");
		stemmer.setToolTipText(buf.toString());
		stemmer.addActionListener(this);
		stemmer.setSelected(false);
		stemmer.setEnabled(false);
		
		//GridBagConstraints gridBagConstraints = new GridBagConstraints();
		//gridBagConstraints.gridx = 0;
		//gridBagConstraints.gridy = 0;
		//gridBagConstraints.gridwidth = 1;
		//gridBagConstraints.anchor = GridBagConstraints.WEST;
		//gridBagConstraints.insets = new Insets(5,0,0,0);
		wordPanel.add(stemmer, BorderLayout.WEST);
		
		//Add components to main panel
		panel.add(wordPanel);
		
		//Add to collapsible panel
		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		
		return collapsiblePanel;
	}
	
	
	/**
	 * Creates a CollapsiblePanel that holds the Cloud Layout information.
	 * @return CollapsiblePanel - cloud Layout panel interface.
	 */
	private CollapsiblePanel createCloudLayoutPanel()
	{
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Layout");
		
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
	    
	    buildStyleCMB();
		
		panel.add(cloudLayoutPanel);
		
		//Create network button stuff
		JLabel createNetworkLabel = new JLabel("Network View:");
		
		createNetworkButton = new JButton("Export Cloud to Network");
		createNetworkButton.setEnabled(false);
		createNetworkButton.setToolTipText("Creates a new network based on the current cloud");
		createNetworkButton.addActionListener(new CreateCloudNetworkAction(modelManager, cloudManager));
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 0, 0, 0);
		cloudLayoutPanel.add(createNetworkLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 10, 0, 0);
		cloudLayoutPanel.add(createNetworkButton, gridBagConstraints);
		
		//Save file to .jpg stuff
		/*
		JLabel saveCloudLabel = new JLabel("Save Cloud Image:");
		
		saveCloudButton = new JButton("Export Cloud to File");
		saveCloudButton.setEnabled(false);
		saveCloudButton.setToolTipText("Saves the current cloud as an image file");
		saveCloudButton.addActionListener(new SaveCloudAction());
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(5, 0, 0, 0);
		cloudLayoutPanel.add(saveCloudLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 10, 0, 0);
		cloudLayoutPanel.add(saveCloudButton, gridBagConstraints);
		*/
		
		collapsiblePanel.getContentPane().add(panel,BorderLayout.NORTH);
		return collapsiblePanel;
	}
	
	
	/**
	 * Utility to create a panel for the buttons at the bottom of the Semantic 
	 * Summary Input Panel.
	 */
	private JPanel createBottomPanel()
	{
		
		JPanel panel = new JPanel();
		panel.setLayout(new ModifiedFlowLayout());
		
		//Create buttons
		JButton deleteButton = new JButton("Delete");
		JButton updateButton = new JButton("Update");
		JButton createButton = new JButton("Create");
	
		saveCloudButton = new JButton("Save Image");
		saveCloudButton.setEnabled(false);
		saveCloudButton.setToolTipText("Saves the current cloud as an image file");
		
		//Add actions to buttons
		createButton.addActionListener(createCloudAction);
		deleteButton.addActionListener(deleteCloudAction);
		updateButton.addActionListener(updateCloudAction);
		saveCloudButton.addActionListener(saveCloudAction);
		
		//Add buttons to panel
		panel.add(deleteButton);
		panel.add(updateButton);
		panel.add(createButton);
		panel.add(saveCloudButton);
		
		return panel;
	}
	
	/**
	 * Sets the list of clouds to be for the supplied network.
	 * @param SemanticSummaryParameter - parameter for the network to display.
	 */
	public void setNetworkList(SemanticSummaryParameters params)
	{
		
		//clear current values
		networkLabel.setText("");
		listValues.clear();
		
		//Set new current values
		networkLabel.setText(params.getNetworkName());
		
		//Ensure list is in order of creation
		List<CloudParameters> clouds = new ArrayList<CloudParameters>(params.getClouds().values());
		Collections.sort(clouds);
		
		Iterator<CloudParameters> iter = clouds.iterator();
		while (iter.hasNext())
		{
			CloudParameters curParam = iter.next();
			String curCloud = curParam.getCloudName();
			listValues.addElement(curCloud);
		}
		
		//Update Manager variables
		cloudManager.setCurNetwork(params);
		cloudManager.setCurCloud(
				cloudManager.getNullCloudParameters());
	}
	
	public void setAttributeNames(List<String> names) {
		attributeList.setSelectedItems(names);
		updateAttNames();
	}
	
	/**
	 * Loads the values from a specified cloud in the user supplied parameters
	 * and sets it as the current cloud.
	 * Assumes that parent list is already updated.
	 * @param CloudParameters - of the cloud to load.
	 */
	public void loadCurrentCloud(CloudParameters params)
	{
		List<String> attributeNames = params.getAttributeNames();
		if (attributeNames == null) {
			attributeNames = Collections.emptyList();
		}
		setAttributeNames(attributeNames);
		maxWordsTextField.setValue(params.getMaxWords());
		clusterCutoffTextField.setValue(params.getClusterCutoff());
		cmbStyle.setSelectedItem(params.getDisplayStyle());
		addWordTextField.setText("");
		this.setupNetworkNormalization(params);
		
		
		//Get current network
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		
		if (networkParams.equals(cloudManager.getNullSemanticSummary()))
			{
			addWordTextField.setEditable(false);
			addWordButton.setEnabled(false);
			numExclusion.setEnabled(false);
			useNetworkCounts.setEnabled(false);
			stemmer.setEnabled(false);
			}
		else
		{
			addWordTextField.setEditable(true);
			addWordButton.setEnabled(true);
			numExclusion.setEnabled(true);
			useNetworkCounts.setEnabled(true);
			stemmer.setEnabled(true);
		}
		
		CloudDisplayPanel displayPanel = cloudManager.getCloudWindow();
		
		//Enable button based on cloud
		if (params.equals(cloudManager.getNullCloudParameters()))
		{
			createNetworkButton.setEnabled(false);
			saveCloudButton.setEnabled(false);
			//displayPanel.getSaveCloudButton().setEnabled(false);
		}
		else
		{
			createNetworkButton.setEnabled(true);
			saveCloudButton.setEnabled(true);
			//displayPanel.getSaveCloudButton().setEnabled(true);
		}
		
		cloudManager.setCurCloud(params);
		this.refreshNetworkSettings();
	}
	
	
	/**
	 * Adds the cloud to the list and sets it to be the selected cloud.
	 * @param CloudParameters - cloud to add to list.
	 */
	public void addNewCloud(CloudParameters params)
	{
		//Turn off listener while doing work
		ListSelectionModel listSelectionModel = cloudList.getSelectionModel();
		listSelectionModel.removeListSelectionListener(handler);
		
		//Update current cloud and add to list
		cloudManager.setCurCloud(params);
		String cloudName = params.getCloudName();
		listValues.addElement(cloudName);
		int index = listValues.lastIndexOf(cloudName);
		
		//Set to be selected
		cloudList.setSelectedIndex(index);
		
		//Turn listener back on
		listSelectionModel.addListSelectionListener(handler);
	}
	/**
	 * Sets all user input fields to their default values.
	 */
	public void setUserDefaults()
	{
		CloudParameters params = cloudManager.getCurCloud();
		
		attributeList.setSelectedIndex(0);
		updateAttNames();
		maxWordsTextField.setValue(params.getDefaultMaxWords());
		clusterCutoffTextField.setValue(params.getDefaultClusterCutoff());
		cmbStyle.setSelectedItem(params.getDefaultDisplayStyle());
		
		this.setupNetworkNormalization(params);
		sliderPanel.setNetNormValue(params.getDefaultNetWeight());
		
		this.refreshNetworkSettings();
	}
	
	/**
	 * Update the delimiter combo boxes.
	 */
	private void updateDelimiterCMBs()
	{
		// Add Box
		DefaultComboBoxModel cmb;
		cmb = ((DefaultComboBoxModel)cmbDelimiterAddition.getModel());
		cmb.removeAllElements();
		
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		WordDelimiters curDelimiters = networkParams.getDelimiter();
		
		//Check if we are dealing with the Null SemanticSummaryParameters
		Boolean isNull = false;
		if (networkParams.equals(cloudManager.getNullSemanticSummary()))
				isNull = true;
		
		//Common Delimiters
		cmb.addElement(commonDelimiters);
		
		if (!isNull)
		{	
			//Add alphabetically order list of delimiters
			for(Iterator<String> iter = curDelimiters.getDelimsToAdd().iterator(); iter.hasNext();)
			{
				String curDelim = iter.next();
				cmb.addElement(curDelim);
			}
		}
		
		//User to add
		cmb.addElement(userAddDelimiters);
		
		if (!isNull)
		{
			cmb.addElement(selectMe);
		}
		
		cmbDelimiterAddition.repaint();
		
		//Remove Box
		cmb = ((DefaultComboBoxModel)cmbDelimiterRemoval.getModel());
		cmb.removeAllElements();
		
		//Common Delimiters
		cmb.addElement(commonDelimiters);
		
		if (!isNull)
		{	
			//Add alphabetically order list of delimiters
			for(Iterator<String> iter = curDelimiters.getDelimsInUse().iterator(); iter.hasNext();)
			{
				String curDelim = iter.next();
				cmb.addElement(curDelim);
			}
		}
		
		//User to add
		cmb.addElement(userAddDelimiters);
		
		if (!isNull)
		{
			//Add alphabetically order list of delimiters
			for(Iterator<String> iter = curDelimiters.getUserDelims().iterator(); iter.hasNext();)
			{
				String curDelim = iter.next();
				cmb.addElement(curDelim);
			}
		}
		
		cmbDelimiterRemoval.repaint();
	}
	
	/**
	 * Update the remove word list in the remove combobox.
	 */
	private void updateCMBRemoval()
	{
		DefaultComboBoxModel cmb;
		cmb = ((DefaultComboBoxModel)cmbRemoval.getModel());
		cmb.removeAllElements();
		
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		WordFilter curFilter = networkParams.getFilter();
		
		//Check if we are dealing with the Null SemanticSummaryParameters
		Boolean isNull = false;
		if (networkParams.equals(cloudManager.getNullSemanticSummary()))
				isNull = true;
		
		//Added words
		cmb.addElement(addedSeparator);
		
		if (!isNull)
		{	
			//Add alphabetically order list of words
			ArrayList<String> addedList = new ArrayList<String>();
			for(Iterator<String> iter = curFilter.getAddedWords().iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				addedList.add(curWord);
			}
			
			Collections.sort(addedList);
			for (int i = 0; i < addedList.size(); i++)
			{
				String curWord = addedList.get(i);
				cmb.addElement(curWord);
			}
		}
		
		//Flagged words
		cmb.addElement(flaggedSeparator);
		
		if (!isNull)
		{
			ArrayList<String> flaggedList = new ArrayList<String>();
			for(Iterator<String> iter = curFilter.getFlaggedWords().iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				flaggedList.add(curWord);
			}
			
			Collections.sort(flaggedList);
			for (int i = 0; i < flaggedList.size(); i++)
			{
				String curWord = flaggedList.get(i);
				cmb.addElement(curWord);
			}
		}
		
		//Stop words
		cmb.addElement(stopSeparator);
		
		if (!isNull)
		{
			ArrayList<String> stopList = new ArrayList<String>();
			for(Iterator<String> iter = curFilter.getStopWords().iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				stopList.add(curWord);
			}
			
			Collections.sort(stopList);
			for (int i = 0; i < stopList.size(); i++)
			{
				String curWord = stopList.get(i);
				cmb.addElement(curWord);
			}
		}
	}
	
	/**
	 * Refreshes the list of words that can be removed from the exclusion list.
	 */
	public void refreshRemovalCMB()
	{
		updateCMBRemoval();
		cmbRemoval.repaint();
	}
	
	/**
	 * Sets the numExclusion checkbox based on the current network.
	 */
	private void updateNumExclusionBox()
	{
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		WordFilter curFilter = networkParams.getFilter();
		Boolean val = curFilter.getFilterNums();
		numExclusion.setSelected(val);
	}
	
	/**
	 * Sets the stemming checkbox based on the current network.
	 */
	private void updateStemmingBox()
	{
		SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
		boolean val = networkParams.getIsStemming();
		stemmer.setSelected(val);
	}
	
	/**
	 * Refreshes everything in the input panel that is on the network level.
	 */
	public void refreshNetworkSettings()
	{
		this.refreshRemovalCMB();
		this.updateNumExclusionBox();
		this.updateDelimiterCMBs();
		this.updateStemmingBox();
	}
	
	
	/**
	 * Update the attribute list in the attribute combobox.
	 */
	private void updateCMBAttributes()
	{
		//Turn off listeners
		ListSelectionListener[] listeners = attributeList.getListSelectionListeners();
		for (int i = 0; i < listeners.length; i++)
		{
			ListSelectionListener curListener = listeners[i];
			attributeList.removeListSelectionListener(curListener);
		}
		
		//Updated GUI
		DefaultListModel listModel;
		listModel = ((DefaultListModel)attributeList.getModel());
		listModel.removeAllElements();
		
		
		CyNetwork network = cloudManager.getCurNetwork().getNetwork();
		if (network != null) {
			for (String name : cloudManager.getColumnNames(network, CyNode.class))
			{
				listModel.addElement(name);
			}
		}
		
		//Turn listeners back on
		for (int i = 0; i < listeners.length; i++)
		{
			ListSelectionListener curListener = listeners[i];
			attributeList.addListSelectionListener(curListener);
		}
	}
	
	
	/**
	 * Refreshes the list of attributes
	 */
	public void refreshAttributeCMB()
	{
		updateCMBAttributes();
		CloudParameters curCloud = cloudManager.getCurCloud();
		
		//Updated GUI
		List<String> curAttList;
		if (curCloud == cloudManager.getNullCloudParameters())
			curAttList = curCloud.getAttributeNames();
		else
			//curAttribute = curCloud.getAttributeName();
			curAttList = curCloud.getAttributeNames();
		
		if (curAttList == null) {
			curAttList = cloudManager.getColumnNames(curCloud.getNetworkParams().getNetwork(), CyNode.class);
		}
		
		attributeList.setSelectedItems(curAttList);
		attributeList.repaint();
		//updateAttNames();
		
	}
	
	/**
	 * Builds the combo box of cloud style choices
	 */
	private void buildStyleCMB()
	{
		DefaultComboBoxModel cmb;
		
		cmb = ((DefaultComboBoxModel)cmbStyle.getModel());
		cmb.removeAllElements();
		cmb.addElement(CloudDisplayStyles.CLUSTERED_STANDARD);
		cmb.addElement(CloudDisplayStyles.CLUSTERED_BOXES);
		cmb.addElement(CloudDisplayStyles.NO_CLUSTERING);
		cmbStyle.setSelectedItem(CloudDisplayStyles.DEFAULT_STYLE);
		cmbStyle.repaint();
	}
	
	/**
	 * Handles the activation of the add/remove buttons for the word
	 * exclusion list.
	 * @param ItemEvent - event that triggered this reaction.
	 */
	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getSource();
		
		if (source instanceof JComboBox)
		{
			JComboBox cmb = (JComboBox) source;
			if (cmb == cmbRemoval)
			{
				Object selectObject = cmbRemoval.getSelectedItem();
				if (selectObject != null)
				{
					String selectItem = selectObject.toString();
					if (selectItem.equalsIgnoreCase(addedSeparator) || 
							selectItem.equalsIgnoreCase(flaggedSeparator) ||
							selectItem.equalsIgnoreCase(stopSeparator))
						removeWordButton.setEnabled(false);
						else
							removeWordButton.setEnabled(true);
				}//end if not null
			}//end if cmbRemoval
			
			if (cmb == cmbDelimiterRemoval)
			{
				Object selectObject = cmbDelimiterRemoval.getSelectedItem();
				if (selectObject != null)
				{
					String selectItem = selectObject.toString();
					if (selectItem.equalsIgnoreCase(commonDelimiters) || 
							selectItem.equalsIgnoreCase(userAddDelimiters))
						removeDelimiterButton.setEnabled(false);
						else
							removeDelimiterButton.setEnabled(true);
				}//end if not null
			}//end if cmbDelimiterRemoval
			
			if (cmb == cmbDelimiterAddition)
			{
				Object selectObject = cmbDelimiterAddition.getSelectedItem();
				if (selectObject != null)
				{
					String selectItem = selectObject.toString();
					if (selectItem.equalsIgnoreCase(commonDelimiters) || 
							selectItem.equalsIgnoreCase(userAddDelimiters))
						addDelimiterButton.setEnabled(false);
						else
							addDelimiterButton.setEnabled(true);
				}//end if not null
			}//end if cmbDelimiterRemoval
			
		}//end if combo box
	}
	
	
	/**
	 * Handles button presses in the Input Panel.
	 * @param ActionEvent - event that triggered this method.
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object _actionObject = e.getSource();
		
		//Handle button events
		if (_actionObject instanceof JButton)
		{
			JButton _btn = (JButton)_actionObject;
			
			if (_btn == removeWordButton)
			{
				//Get Selected word
				Object selectObject = cmbRemoval.getSelectedItem();
				if (selectObject != null)
				{
					String selectItem = selectObject.toString();
					if (!selectItem.equalsIgnoreCase(addedSeparator) || 
							!selectItem.equalsIgnoreCase(flaggedSeparator) ||
							!selectItem.equalsIgnoreCase(stopSeparator))
					{
						SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
						WordFilter curFilter = networkParams.getFilter();
						
						//Remove from filter
						curFilter.remove(selectItem);
						
						//Reset flags
						networkParams.networkChanged();
						
						//Refresh word removal list
						this.refreshRemovalCMB();
					}//end if appropriate selection
				}//end if not null selected
			}//end remove word button
			
			if (_btn == addWordButton)
			{
				String value = (String)addWordTextField.getText();
				if (value.matches(""))
					return;
				
				else if (value.matches("[\\w]*"))
				{ 
					//add value to filter and update
					SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
					WordFilter curFilter = networkParams.getFilter();
					value.toLowerCase();
					curFilter.add(value);
					
					//Reset flags
					networkParams.networkChanged();
					
					//Refresh view
					this.refreshRemovalCMB();
					addWordTextField.setText(null);
				}
				else
				{
					addWordTextField.setSelectionStart(0);
					addWordTextField.setSelectionEnd(value.length());
					String message = "You can only add a word that contains letters or numbers and no spaces";
					JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
				}
			}
			
			if (_btn == removeDelimiterButton)
			{
				//Get Selected word
				Object selectObject = cmbDelimiterRemoval.getSelectedItem();
				if (selectObject != null)
				{
					String selectItem = selectObject.toString();
					if (!selectItem.equalsIgnoreCase(commonDelimiters) || 
							!selectItem.equalsIgnoreCase(userAddDelimiters))
					{
						SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
						WordDelimiters curDelimiters = networkParams.getDelimiter();
						
						//Remove from delimiters
						curDelimiters.removeDelimiter(selectItem);
						
						//Reset flags
						networkParams.networkChanged();
						
						//Refresh word removal list
						this.updateDelimiterCMBs();
					}//end if appropriate selection
				}//end if not null selected
			}//end remove delimiter button
			
			if (_btn == addDelimiterButton)
			{
				//Get Selected word
				Object selectObject = cmbDelimiterAddition.getSelectedItem();
				if (selectObject != null)
				{
					String selectItem = selectObject.toString();
					if (!selectItem.equalsIgnoreCase(commonDelimiters) || 
							!selectItem.equalsIgnoreCase(userAddDelimiters))
					{
						//Dialog if user add
						if (selectItem.equals(selectMe))
						{
							Component parent = application.getJFrame();
							AddDelimiterDialog dialog = new AddDelimiterDialog(parent, true);
							dialog.setLocationRelativeTo(parent);
							dialog.setVisible(true);
							
							selectItem = dialog.getNewDelimiter();
						}
						
						if (!selectItem.equals(""))
						{
							SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
							WordDelimiters curDelimiters = networkParams.getDelimiter();
						
							//Add to delimiters
							curDelimiters.addDelimToUse(selectItem);
						
							//Reset flags
							networkParams.networkChanged();
						
							//Refresh word removal list
							this.updateDelimiterCMBs();
						}
					}//end if appropriate selection
				}//end if not null selected
			}//end remove delimiter button
		}//end button	
		
		else if (_actionObject instanceof JCheckBox)
		{
			JCheckBox _box = (JCheckBox)_actionObject;
			
			if (_box == numExclusion)
			{
				Boolean selected = numExclusion.isSelected();
				
				//add value to filter and update
				SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
				WordFilter curFilter = networkParams.getFilter();
				curFilter.setFilterNums(selected);
				
				//Reset flags
				networkParams.networkChanged();
			}
			
			if (_box == useNetworkCounts)
			{
				Boolean selected = useNetworkCounts.isSelected();
				CloudParameters cloudParams = cloudManager.getCurCloud();
				
				if (!cloudParams.equals(cloudManager.getNullCloudParameters()))
						cloudParams.setUseNetNormal(selected);
				
				//Enable or disable slider bar stuff and reset network weights
				if (selected)
				{
					sliderPanel.setVisible(true);
					sliderPanel.setEnabled(true);
					
					Double netNormalization = cloudParams.getNetWeightFactor();
					sliderPanel.setNetNormValue(netNormalization);
				}
				else
				{
					//Confirm continuation
					Component parent = application.getJFrame();
					int value = JOptionPane.NO_OPTION;
					
					value = JOptionPane.showConfirmDialog(parent,"Network normalization will now be set to 0.  Do you want to continue?", 
							"Network Normalization",
							JOptionPane.YES_NO_OPTION);
					
					if (value == JOptionPane.YES_OPTION)
					{
						sliderPanel.setVisible(false);
						sliderPanel.setEnabled(false);
					
						//reset network param
						cloudParams.setNetWeightFactor(0.0);
						sliderPanel.setNetNormValue(0.0);
						
						//Update cloud display
						updateCloudAction.doRealAction();
					}//end if yes option
					else
					{
						//Reset Values
						sliderPanel.setVisible(true);
						sliderPanel.setEnabled(true);
						
						Double netNormalization = cloudParams.getNetWeightFactor();
						sliderPanel.setNetNormValue(netNormalization);
						
						useNetworkCounts.setSelected(true);
						cloudParams.setUseNetNormal(true);
					}
				}//end collapse Network Normalization panel
			}//end of useNetworkCounts
			
			if (_box == stemmer)
			{
				Boolean selected = stemmer.isSelected();
				
				//add value to networkParams and update
				SemanticSummaryParameters networkParams = cloudManager.getCurNetwork();
				networkParams.setIsStemming(selected);
				
				//Reset flags
				networkParams.networkChanged();
			}
		}//end checkboxes
	}
	
	/**
	 * Sets up the network normalization panel for the given cloud.
	 * @param CloudParameter to use
	 */
	private void setupNetworkNormalization(CloudParameters params)
	{
		//Turn off slider listener
		ChangeListener[] listeners = sliderPanel.getSlider().getChangeListeners();
		for (int i = 0; i < listeners.length; i++)
		{
			sliderPanel.getSlider().removeChangeListener(listeners[i]);
		}
		
		Boolean useNetNorm = params.getUseNetNormal();
		useNetworkCounts.setSelected(useNetNorm);
		sliderPanel.setVisible(useNetNorm);
		sliderPanel.setEnabled(useNetNorm);
		sliderPanel.setNetNormValue(params.getNetWeightFactor());
		sliderPanel.setLabel(sliderPanel.getSlider().getValue());
		
		//Turn back on slider listener
		for (int i = 0; i < listeners.length; i++)
		{
			sliderPanel.getSlider().addChangeListener(listeners[i]);
		}
	}
	
	private void updateAttNames()
	{
		StringBuilder buffer = new StringBuilder();
		if (!attributeList.isSelectionEmpty())
		{
			Object[] names = attributeList.getSelectedValues();
			for (int i = 0; i < names.length; i++)
			{
				if (names[i] instanceof String)
				{
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
	
	
	//Getters and Setters
	
	public JFormattedTextField getMaxWordsTextField()
	{
		return maxWordsTextField;
	}
	
	public JFormattedTextField getClusterCutoffTextField()
	{
		return clusterCutoffTextField;
	}

	public JTextField getAddWordTextField()
	{
		return addWordTextField;
	}

	public JButton getAddWordButton()
	{
		return addWordButton;
	}
	
	public JLabel getNetworkLabel()
	{
		return networkLabel;
	}
	
	public DefaultListModel getListValues()
	{
		return listValues;
	}
	
	public JList getCloudList()
	{
		return cloudList;
	}
	
	
	public JComboBox getCMBRemoval()
	{
		return cmbRemoval;
	}
	
	public JComboBox getCMBStyle()
	{
		return cmbStyle;
	}
	
	
	public CloudListSelectionHandler getCloudListSelectionHandler()
	{
		return handler;
	}
	
	public JCheckBox getNumExclusion()
	{
		return numExclusion;
	}
	
	public void setNumExclusion(JCheckBox box)
	{
		numExclusion = box;
	}
	
	public JComboBox getCMBDelimiterRemoval()
	{
		return cmbDelimiterRemoval;
	}
	
	public JComboBox getCMBDelimiterAddition()
	{
		return cmbDelimiterAddition;
	}
	
	public JButton getAddDelimiterButton()
	{
		return addDelimiterButton;
	}
	
	public JButton getRemoveDelimiterButton()
	{
		return removeDelimiterButton;
	}
	
	public JCheckBox getUseNetworkCounts()
	{
		return useNetworkCounts;
	}
	
	public SliderBarPanel getSliderBarPanel()
	{
		return sliderPanel;
	}
	
	public CheckBoxJList getAttributeList()
	{
		return attributeList;
	}
	
	public JPopupMenu getAttributePopupMenu()
	{
		return attributeSelectionPopupMenu;
	}
	
	public JTextArea getAttNames()
	{
		return attNames;
	}

	public JButton getCreateNetworkButton()
	{
		return createNetworkButton;
	}

	public JButton getSaveCloudButton()
	{
		return saveCloudButton;
	}
	
	public JCheckBox getStemmerCheckBox()
	{
		return stemmer;
	}
	
	
	/**
	 * Private Class to ensure that text fields are being set properly
	 */
	private class FormattedTextFieldAction implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			JFormattedTextField source = (JFormattedTextField) e.getSource();
			
			CloudParameters params = cloudManager.getCurCloud();
			
			String message = "The value you have entered is invalid. \n";
			boolean invalid = false;

			
			//Max Words
			if (source == maxWordsTextField)
			{
				Number value = (Number) maxWordsTextField.getValue();
				if ((value != null) && (value.intValue() >= 0))
				{
					//All is well - do nothing
				}
				else
				{
					Integer defaultMaxWords = params.getDefaultMaxWords();
					maxWordsTextField.setValue(defaultMaxWords);
					message += "The maximum number of words to display must be greater than or equal to 0.";
					invalid = true;
				}
			}// end max Words
			
			else if (source == clusterCutoffTextField)
			{
				Number value = (Number) clusterCutoffTextField.getValue();
				if ((value != null) && (value.doubleValue() >= 0.0))
				{
					//All is well - leave it be
				}
				else
				{
					Double defaultClusterCutoff = params.getDefaultClusterCutoff();
					clusterCutoffTextField.setValue(defaultClusterCutoff);
					message += "The cluster cutoff must be greater than or equal to 0";
					invalid = true;
				}
			}
			
			else if (source == addWordTextField)
			{
				String value = (String)addWordTextField.getText();
				if (value.equals("") || value.matches("[\\w]*"))
				{ 
					//All is well, leave it be
				}
				else
				{
					//addWordTextField.setValue("");
					//message += "You can only add a word that contains letters or numbers and no spaces";
					//invalid = true;
				}
			}
			
			if (invalid)
				JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
		}
	}
}