package org.baderlab.wordcloud.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class CreateCloudNoDisplayAction extends AbstractSemanticSummaryAction 
{

	//VARIABLES
	private static final long serialVersionUID = -5065616290485908393L;
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private SemanticSummaryManager cloudManager;
	private SemanticSummaryParametersFactory parametersFactory;
	private String nameColumnName;
	private String clusterColumnName;
	private List<CloudWordInfo> wordInfo;
	private Integer clusterNumber;
	
	//CONSTRUCTORS
	
		/**
	 * CreateCloudAction constructor.
	 * @param pluginAction 
	 */
	public CreateCloudNoDisplayAction(CyApplicationManager applicationManager, CySwingApplication application, SemanticSummaryManager cloudManager, SemanticSummaryParametersFactory parametersFactory)
	{
		super("Create Cloud Without");
		this.applicationManager = applicationManager;
		this.application = application;
		this.cloudManager = cloudManager;
		this.parametersFactory = parametersFactory;
	}
	
	public void setAttributeColumn(String columnName) {
		this.nameColumnName = columnName;
	}
	
	public void setClusterColumn(String columnName) {
		this.clusterColumnName = columnName;
	}
	
	public void setClusterNumber(Integer clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	
	public List<CloudWordInfo> getWordInfo() {
		return this.wordInfo;
	}
	
	//METHODS
	
	/**
	 * Method called when a Create Cloud action occurs.
	 * @param nameColumnName 
	 * 
	 * @param ActionEvent - event created when choosing Create Cloud from 
	 * any of its various locations.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		//Initialize the Semantic Summary Panels
		pluginAction.actionPerformed(ae);
		
		
		CyNetwork network = applicationManager.getCurrentNetwork();
		if (network == null) {
			return;
		}

		//If no nodes are selected
		if (!SelectionUtils.hasSelectedNodes(network))
		{
			JOptionPane.showMessageDialog(application.getJFrame(), 
					"Please select one or more nodes.");
			return;
		}
		
		Set<CyNode> nodes = SelectionUtils.getSelectedNodes(network);
		
		//Check if network is already in our list
		SemanticSummaryParameters params;
		
		//Get SemanticSummaryParameters or Register if necessary
		if(cloudManager.isSemanticSummary(network))
		{
			params = cloudManager.getParameters(network);
			
			//Update if necessary
			if (params.networkHasChanged(network));
				params.updateParameters(network);
		}
		else
		{
			params = parametersFactory.createSemanticSummaryParameters();
			params.updateParameters(network);
			cloudManager.registerNetwork(network, params);
		}
		
		//Create CloudParameters
		CloudParameters cloudParams = new CloudParameters(params);
		cloudParams.setCloudNum(params.getCloudCount());
		cloudParams.setCloudName(params.getNextCloudName());
		cloudParams.setSelectedNodes(nodes);
		
		//Add to list of clouds
		params.addCloud(cloudParams.getCloudName(), cloudParams);
		
		// Select all attributes by default
		SemanticSummaryInputPanel inputPanel = cloudManager.getInputWindow();
		inputPanel.setAttributeNames(cloudManager.getColumnNames(network, CyNode.class));
		
		if (nameColumnName != null) {
			ArrayList<String> attributeNames = new ArrayList<String>();
			attributeNames.add(nameColumnName);
			inputPanel.setAttributeNames(attributeNames);
		}

		
		//Retrieve values from input panel
		cloudParams.retrieveInputVals(inputPanel);
		
		cloudParams.updateRatios();
		cloudParams.calculateFontSizes();

		this.wordInfo = cloudParams.getCloudWordInfoList();
		List<CyRow> table = network.getDefaultNodeTable().getAllRows();
		for (CyRow row : table) {
			if (row.get(clusterColumnName, Integer.class) == clusterNumber) {
				ArrayList<String> wordInfoString = new ArrayList<String>();
				for (CloudWordInfo cloud : wordInfo) {
					wordInfoString.add(cloud.toStringHuman());
				}
				row.set("Word Info", wordInfoString);
			}
		}
	}
}