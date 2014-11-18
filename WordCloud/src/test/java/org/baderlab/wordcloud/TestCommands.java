package org.baderlab.wordcloud;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.baderlab.wordcloud.internal.command.CreateWordCloudCommandHandlerTask;
import org.baderlab.wordcloud.internal.command.DeleteWordCloudCommandHandlerTask;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TestCommands {

	@Rule public ServiceRule serviceRule = new ServiceRule();
	
	private CyTableFactory tableFactory;
	
	
	@Before
	public void before() throws Exception {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		
		when(serviceRule.getCyApplicationManager().getCurrentNetwork()).thenReturn(network);
		
		tableFactory = serviceRule.getTableTestSupport().getTableFactory();
	}
	
	
	@Test
	public void testCreateCommand() {
		// set up the network
		CyApplicationManager applicationManager = serviceRule.getCyApplicationManager();
		CyTableManager tableManager = serviceRule.getTableManager();
		
		CyNetwork network = applicationManager.getCurrentNetwork();
		network.addNode();
		network.addNode();
		network.addNode();
		
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		UIManager uiManager = mock(UIManager.class);
		
		// Create the Task
		CreateWordCloudCommandHandlerTask task = new CreateWordCloudCommandHandlerTask(applicationManager, manager, uiManager, tableManager, tableFactory);
		task.cloudName = "mytask_cloud";
		task.cloudGroupTableName = "cloudGroupTableName";
		
		NodeList nodeList = new NodeList();
		nodeList.setNetwork(network);
		nodeList.setValue(network.getNodeList());
		task.nodeList = nodeList;
		
		network.getDefaultNodeTable().createColumn("attName", String.class, false);
		task.wordColumnName = "attName";

		// Check preconditions
		assertNull(manager.getNetworkParameters(network));
		
		task.run(mock(TaskMonitor.class));
		
		NetworkParameters networkParams = manager.getNetworkParameters(network);
		assertNotNull(networkParams);
		assertTrue(networkParams.containsCloud("mytask_cloud"));
		
		CloudParameters cloudParameters = networkParams.getCloud("mytask_cloud");
		assertNotNull(cloudParameters);
		
		List<String> attributeNames = cloudParameters.getAttributeNames();
		assertEquals(1, attributeNames.size());
		assertEquals("attName", attributeNames.get(0));
		
		cloudParameters.delete();
	}
	
	
	@Test
	public void testBadCreateCommand() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		CyApplicationManager applicationManager = serviceRule.getCyApplicationManager();
		CyTableManager tableManager = serviceRule.getTableManager();
		CyNetwork network = applicationManager.getCurrentNetwork();
		
		UIManager uiManager = mock(UIManager.class);
		
		CreateWordCloudCommandHandlerTask task = new CreateWordCloudCommandHandlerTask(applicationManager, manager, uiManager, tableManager, tableFactory);
		
		try {
			task.run(mock(TaskMonitor.class));
			fail();
		} catch(IllegalArgumentException e) {}
		
		task.cloudName = "my_cloud_name";
		
		try {
			task.run(mock(TaskMonitor.class));
			fail();
		} catch(IllegalArgumentException e) {}
		
		task.cloudGroupTableName = "whatever";
		
		try {
			task.run(mock(TaskMonitor.class));
			fail();
		} catch(IllegalArgumentException e) {}
		
		task.wordColumnName = "attName";
		
		try {
			task.run(mock(TaskMonitor.class));
			fail();
		} catch(IllegalArgumentException e) {}
		
		task.nodeList.setValue(network.getNodeList());
		
		task.run(mock(TaskMonitor.class));
		
		NetworkParameters networkParams = manager.getNetworkParameters(network);
		assertNotNull(networkParams);
		assertTrue(networkParams.containsCloud("my_cloud_name"));
		
		CloudParameters cloudParameters = networkParams.getCloud("my_cloud_name");
		assertNotNull(cloudParameters);
		
		List<String> attributeNames = cloudParameters.getAttributeNames();
		assertEquals(1, attributeNames.size());
		assertEquals("attName", attributeNames.get(0));
		
		cloudParameters.delete();
	}
	
	
	@Test
	public void testDeleteCommand() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		CyApplicationManager applicationManager = serviceRule.getCyApplicationManager();
		
		CyNetwork network = applicationManager.getCurrentNetwork();
		NetworkParameters networkParameters = manager.addNetwork(network);
		networkParameters.getCloudBuilder().setName("my_cloud_name").setNodes(network.getNodeList()).build();
		
		UIManager uiManager = mock(UIManager.class);
		when(uiManager.getCurrentNetwork()).thenReturn(networkParameters);
		
		DeleteWordCloudCommandHandlerTask task = new DeleteWordCloudCommandHandlerTask(uiManager);
		task.cloudName = "my_cloud_name";
		
		task.run(mock(TaskMonitor.class));
		
		assertFalse(networkParameters.containsCloud("my_cloud_name"));
	}
	
	
	@Test
	public void testBadDeleteCommand() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		CyApplicationManager applicationManager = serviceRule.getCyApplicationManager();
		
		CyNetwork network = applicationManager.getCurrentNetwork();
		NetworkParameters networkParameters = manager.addNetwork(network);
		
		UIManager uiManager = mock(UIManager.class);
		when(uiManager.getCurrentNetwork()).thenReturn(networkParameters);
		
		DeleteWordCloudCommandHandlerTask task = new DeleteWordCloudCommandHandlerTask(uiManager);
		task.cloudName = "blah blah blah";
		
		try {
			task.run(mock(TaskMonitor.class));
			fail();
		} catch(IllegalArgumentException e) {}
	}
	
}
