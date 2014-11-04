package org.baderlab.wordcloud;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.baderlab.wordcloud.internal.model.CloudModelListener;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Rule;
import org.junit.Test;

public class TestCloudModel {

	@Rule public ServiceRule serviceRule = new ServiceRule();
	
	
	@Test
	public void testCreateNetworkParameters() {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		CyNetwork network = networkTestSupport.getNetwork();
		
		assertTrue(manager.getNetworks().isEmpty());
		assertNull(manager.getNetworkParameters(network));
		assertFalse(manager.isManaged(network));	
		
		assertNull(manager.getNetworkParameters(null));
		assertFalse(manager.isManaged(null));
				
		NetworkParameters networkParameters = manager.addNetwork(network);
		assertNotNull(networkParameters);
		assertEquals(1, manager.getNetworks().size());
		assertTrue(manager.isManaged(network));
		assertEquals(network, networkParameters.getNetwork());
		assertTrue(networkParameters.getClouds().isEmpty());
		assertEquals(networkParameters, manager.getNetworkParameters(network));
		assertEquals(manager, networkParameters.getManager());
		
		// Add same network again?
		NetworkParameters sameReference = manager.addNetwork(network);
		assertEquals(networkParameters, sameReference);
		assertEquals(1, manager.getNetworks().size());
	}
	
	
	@Test
	public void testCreateCloudParameters() {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		CyNetwork network = networkTestSupport.getNetwork();
		network.addNode();
		network.addNode();
		network.addNode();
		assertEquals(3, network.getNodeCount());
		
		NetworkParameters networkParams = manager.addNetwork(network);
		assertEquals(0, networkParams.getClouds().size());
		
		CloudParameters cloudParams1 = networkParams.createCloud(network.getNodeList(), "mycloud_1");
		assertNotNull(cloudParams1);
		assertEquals("mycloud_1", cloudParams1.getCloudName());
		assertEquals(1, networkParams.getClouds().size());
		assertEquals(3, cloudParams1.getSelectedNodes().size());
		
		CloudParameters cloudParams2 = networkParams.createCloud(network.getNodeList().subList(0, 1), "mycloud_2");
		assertNotNull(cloudParams2);
		assertEquals("mycloud_2", cloudParams2.getCloudName());
		assertEquals(2, networkParams.getClouds().size());
		assertEquals(1, cloudParams2.getSelectedNodes().size());
		
		network.getDefaultNodeTable().createColumn("attName", String.class, false);
		CloudParameters cloudParams3 = networkParams.createCloud(network.getNodeList(), "mycloud_3", "attName", null);
		assertNotNull(cloudParams3);
		assertEquals("mycloud_3", cloudParams3.getCloudName());
		assertEquals(3, networkParams.getClouds().size());
		assertEquals(3, cloudParams3.getSelectedNodes().size());
		assertEquals(1, cloudParams3.getAttributeNames().size());
		assertEquals("attName", cloudParams3.getAttributeNames().get(0));
	}
	
	
	@Test
	public void testCloudDelete() {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		CyNetwork network = networkTestSupport.getNetwork();
		NetworkParameters networkParams = manager.addNetwork(network);
		
		CloudParameters cloudParams1 = networkParams.createCloud(network.getNodeList(), "mycloud_1");
		CloudParameters cloudParams2 = networkParams.createCloud(network.getNodeList(), "mycloud_2");
		CloudParameters cloudParams3 = networkParams.createCloud(network.getNodeList(), "mycloud_3");
		assertEquals(3, networkParams.getClouds().size());
		
		cloudParams1.delete();
		assertEquals(2, networkParams.getClouds().size());
		
		// deleting a cloud again does nothing
		cloudParams1.delete();
		assertEquals(2, networkParams.getClouds().size());
		
		cloudParams2.delete();
		cloudParams3.delete();
		assertEquals(0, networkParams.getClouds().size());
	}
	
	
	@Test
	public void testNullNetworkAndNullCloud() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		NetworkParameters nullNetwork = manager.getNullNetwork();
		assertNotNull(nullNetwork);
		assertTrue(nullNetwork.isNullNetwork());
		assertTrue(manager.getNetworks().isEmpty());
		assertNull(nullNetwork.getNetwork());
		
		CloudParameters nullCloud = nullNetwork.getNullCloud();
		assertNotNull(nullCloud);
		assertTrue(nullCloud.isNullCloud());
		assertTrue(nullNetwork.getClouds().isEmpty());
	}
	
	
	@Test
	public void testNullNetworkTryToAddCloud() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		NetworkParameters nullNetwork = manager.getNullNetwork();
		List<CyNode> nodes = Collections.emptyList();
		
		try {
			nullNetwork.createCloud(nodes);
			fail();
		} catch(IllegalStateException e) {}
		
		try {
			nullNetwork.createCloud(nodes, "Cloudy");
			fail();
		} catch(IllegalStateException e) {}
		
		try {
			nullNetwork.createCloud(nodes, "Cloudy", null, null);
			fail();
		} catch(IllegalStateException e) {}
	}
	
	
	@Test
	public void testCreateCloudInvalidParameters() {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		CyNetwork network = networkTestSupport.getNetwork();
		List<CyNode> nodes = network.getNodeList();
		NetworkParameters networkParams = manager.addNetwork(network);
		
		try {
			networkParams.createCloud(null);
			fail();
		} catch(NullPointerException e) {}
		
		try {
			networkParams.createCloud(null, "blah");
			fail();
		} catch(NullPointerException e) {}
		
		try {
			networkParams.createCloud(nodes, null);
			fail();
		} catch(NullPointerException e) {}
		
		try {
			networkParams.createCloud(null, "blah", "attName", null);
			fail();
		} catch(NullPointerException e) {}
		
		try {
			networkParams.createCloud(nodes, null, "attName", null);
			fail();
		} catch(NullPointerException e) {}
		
		try {
			networkParams.createCloud(nodes, "blah", null, null);
			fail();
		} catch(NullPointerException e) {}
		
		networkParams.createCloud(nodes, "taken_name");
		
		try {
			networkParams.createCloud(nodes, "taken_name");
			fail();
		} catch(IllegalArgumentException e) {}
		
		try {
			networkParams.createCloud(nodes, "taken_name", "attName", null);
			fail();
		} catch(IllegalArgumentException e) {}
	}
	
	
	@Test
	public void testCloudModelListener() {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		CloudModelListener listener = mock(CloudModelListener.class);
		manager.addListener(listener);
		
		CyNetwork network = networkTestSupport.getNetwork();
		NetworkParameters networkParams = manager.addNetwork(network);
		
		List<CyNode> nodes = networkParams.getNetwork().getNodeList();
		CloudParameters cloudParameters = networkParams.createCloud(nodes);
		cloudParameters.rename("a new name");
		cloudParameters.delete();
		manager.removeNetwork(network);
		
		verify(listener).cloudAdded(cloudParameters);
		verify(listener).cloudModified(cloudParameters);
		verify(listener).cloudDeleted(cloudParameters);
		verify(listener).networkRemoved(networkParams);
	}
	
	
	@Test(expected=NullPointerException.class)
	public void testCreateNetworkParametersProperFail() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		manager.addNetwork(null);
	}
	
}
