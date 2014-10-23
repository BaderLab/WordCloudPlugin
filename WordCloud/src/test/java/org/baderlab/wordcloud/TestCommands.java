package org.baderlab.wordcloud;

import static org.baderlab.wordcloud.TestCloudModel.emptyStream;
import static org.junit.Assert.*;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.baderlab.wordcloud.internal.command.CreateWordCloudCommandHandlerTask;
import org.baderlab.wordcloud.internal.command.DeleteWordCloudCommandHandlerTask;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.ui.UIManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestCommands {

	@Mock private CyApplicationManager applicationManager;
	@Mock private CyTableManager tableManager;
	@Mock private StreamUtil streamUtil;
	@Mock private CyProperty<Properties> cyProperties;
	
	private CyTableFactory tableFactory;
	private CyNetworkManager networkManager;
	
	
	@Before
	public void before() throws Exception {
		// inject mock objects for fields with @Mock annotation
		MockitoAnnotations.initMocks(this);
		// stub out methods that get called somewhere in the CloudModelManager
		when(streamUtil.getInputStream(endsWith("StopWords.txt"))).thenReturn(emptyStream());
		when(streamUtil.getInputStream(endsWith("FlaggedWords.txt"))).thenReturn(emptyStream());
		when(cyProperties.getProperties()).thenReturn(new Properties());
		
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		networkManager = networkTestSupport.getNetworkManager();
		
		when(applicationManager.getCurrentNetwork()).thenReturn(network);
		
		TableTestSupport tableTestSupport = new TableTestSupport();
		tableFactory = tableTestSupport.getTableFactory();
	}
	
	@Test
	public void testCreateCommand() {
		// set up the network
		CyNetwork network = applicationManager.getCurrentNetwork();
		network.addNode();
		network.addNode();
		network.addNode();
		
		CloudModelManager manager = new CloudModelManager(networkManager, tableManager, streamUtil, cyProperties); 
		
		// Create the Task
		CreateWordCloudCommandHandlerTask task = new CreateWordCloudCommandHandlerTask(applicationManager, manager, tableManager, tableFactory);
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
		CloudModelManager manager = new CloudModelManager(networkManager, tableManager, streamUtil, cyProperties); 
		CyNetwork network = applicationManager.getCurrentNetwork();
		
		CreateWordCloudCommandHandlerTask task = new CreateWordCloudCommandHandlerTask(applicationManager, manager, tableManager, tableFactory);
		
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
		CloudModelManager manager = new CloudModelManager(networkManager, tableManager, streamUtil, cyProperties); 
		CyNetwork network = applicationManager.getCurrentNetwork();
		NetworkParameters networkParameters = manager.addNetwork(network);
		networkParameters.createCloud(network.getNodeList(), "my_cloud_name");
		
		UIManager uiManager = mock(UIManager.class);
		when(uiManager.getCurrentNetwork()).thenReturn(networkParameters);
		
		DeleteWordCloudCommandHandlerTask task = new DeleteWordCloudCommandHandlerTask(uiManager);
		task.cloudName = "my_cloud_name";
		
		task.run(mock(TaskMonitor.class));
		
		assertFalse(networkParameters.containsCloud("my_cloud_name"));
	}
	
	
	@Test
	public void testBadDeleteCommand() {
		CloudModelManager manager = new CloudModelManager(networkManager, tableManager, streamUtil, cyProperties); 
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
