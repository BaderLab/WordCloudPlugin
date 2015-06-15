package org.baderlab.wordcloud;

import static org.junit.Assert.*;

import java.util.Set;

import org.baderlab.wordcloud.internal.cluster.CloudDisplayStyles;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;


/**
 * Previous versions of WordCloud would create a column for each cloud
 * in the default node table. This was very problematic because the column
 * would be shared by all subnetworks, which caused clouds with the same name
 * to write over each other.
 * 
 * In WordCloud 3.0 this was changed so that each cloud creates a column
 * in the subnetwork's local table. However, backwards compatibility must be
 * maintained. 
 * 
 * This test suite ensures that the new behavior is correct and is backwards
 * compatible with WordCloud 2.X.
 * 
 * @author mkucera
 */
public class TestColumns {

	@Rule public ServiceRule serviceRule = new ServiceRule();
	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	private CloudModelManager manager;
	private CyNetworkFactory networkFactory;
	
	@Before
	public void before() {
		manager = serviceRule.getCloudModelManager();
		networkFactory = serviceRule.getNetworkTestSupport().getNetworkFactory();
	}
	
	
	@Test
	public void testCloudsWithSameName() {
		// Set up two subnetworks that share some nodes.
		CySubNetwork network1 = (CySubNetwork) networkFactory.createNetwork();
		CyNode node1 = network1.addNode();
		CyNode node2 = network1.addNode();
		
		CyRootNetwork rootNetwork = network1.getRootNetwork();
		CySubNetwork network2 = rootNetwork.addSubNetwork();
		assertTrue(rootNetwork.containsNetwork(network2));
		
		// subnetworks share one node
		network2.addNode(node1);
		
		manager.addNetwork(network1);
		manager.addNetwork(network2);
		
		manager.getNetworkParameters(network1).getCloudBuilder().setName("CloudName").setNodes(network1.getNodeList()).build();
		
		// Sanity check
		Boolean node2CloudState = network1.getRow(node2).get("CloudName", Boolean.class);
		assertTrue(Boolean.TRUE.equals(node2CloudState));
		
		manager.getNetworkParameters(network2).getCloudBuilder().setName("CloudName").setNodes(network2.getNodeList()).build(); // use same name
		
		// Creating a cloud with the same name in another subnetwork should not have the side effect
		// of changing node state in the first subnetwork.
		node2CloudState = network1.getRow(node2).get("CloudName", Boolean.class);
		assertTrue(Boolean.TRUE.equals(node2CloudState));
	}
	
	
	@Test
	public void testBackwardsCompatibility() {
		CySubNetwork network1 = (CySubNetwork) networkFactory.createNetwork();
		CyNode node1 = network1.addNode();
		network1.addNode();
		
		CloudParameters cloud1 = manager.addNetwork(network1).getCloudBuilder().setName("MyCloud").setNodes(network1.getNodeList()).build();
		
		// We want the column to be in the default node table
		CyTable localTable = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		localTable.deleteColumn("MyCloud");
		CyTable defaultTable = network1.getDefaultNodeTable();
		defaultTable.createColumn("MyCloud", Boolean.class, false);
		network1.getRow(node1).set("MyCloud", Boolean.TRUE);
		
		// The cloud should still be usable when the column is in the default table
		Set<CyNode> cloudNodes = cloud1.getSelectedNodes();
		assertEquals(1, cloudNodes.size());
		assertTrue(cloudNodes.contains(node1));
		
		// Can't use that name for other clouds though
		CyRootNetwork rootNetwork = network1.getRootNetwork();
		CySubNetwork network2 = rootNetwork.addSubNetwork();

		try {
			manager.addNetwork(network2).getCloudBuilder().setName("MyCloud").setNodes(network2.getNodeList()).build();
			fail();
		} catch(IllegalArgumentException e) { }
	}
	
	
	@Test
	public void testAutoName() {
		CySubNetwork network1 = (CySubNetwork) networkFactory.createNetwork();
		network1.addNode();
		
		CyTable localTable = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		localTable.createColumn("Cloud_1", Boolean.class, false);
		localTable.createColumn("Cloud_2", Boolean.class, false);
		localTable.createColumn("Cloud_3", Boolean.class, false);
		
		CloudParameters cloud = manager.addNetwork(network1).getCloudBuilder().setNodes(network1.getNodeList()).build();
		assertEquals("Cloud_4", cloud.getCloudName());
	}
	
	
	@Test
	public void testDeleteCloudDeletesColumn() {
		CySubNetwork network1 = (CySubNetwork) networkFactory.createNetwork();
		network1.addNode();
		
		CloudParameters cloud = manager.addNetwork(network1).getCloudBuilder().setNodes(network1.getNodeList()).build();
		
		CyColumn column = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(cloud.getCloudName());
		assertNotNull(column);
		
		cloud.delete();
		
		column = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(cloud.getCloudName());
		assertNull(column);
	}
	
	
	@Test
	public void testRenameCloudRenamesColumn() {
		CySubNetwork network1 = (CySubNetwork) networkFactory.createNetwork();
		network1.addNode();
		
		CloudParameters cloud = manager.addNetwork(network1).getCloudBuilder().setNodes(network1.getNodeList()).build();
		final String oldName = cloud.getCloudName();
		final String newName = "MyNewName";
		cloud.rename(newName);
		
		assertEquals(newName, cloud.getCloudName());
		
		CyColumn column = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(oldName);
		assertNull(column);
		column = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(newName);
		assertNotNull(column);
	}
	
	
	@Test
	public void testUserDeletesColumnForExistingCloud() {
		CySubNetwork network1 = (CySubNetwork) networkFactory.createNetwork();
		network1.addNode();
		
		CloudParameters cloud = manager.addNetwork(network1).getCloudBuilder().setNodes(network1.getNodeList()).build();
		
		CyTable table = network1.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		table.deleteColumn(cloud.getCloudName());
		
		assertTrue(cloud.getSelectedNodes().isEmpty());
		assertTrue(cloud.calculateCloud().getCloudWordInfoList().isEmpty());
		
		// updating the cloud should not blow up
		cloud.setSelectedNodes(network1.getNodeList());
		cloud.setDisplayStyle(CloudDisplayStyles.CLUSTERED_BOXES);
		
		assertTrue(cloud.getSelectedNodes().isEmpty());
	}
	
}
