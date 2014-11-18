package org.baderlab.wordcloud;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.baderlab.wordcloud.internal.cluster.CloudWordInfo;
import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.baderlab.wordcloud.internal.model.WordDelimiters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TestCloudWords {

	@Rule public ServiceRule serviceRule = new ServiceRule();
	
	private final String WORD_COL = "TestWordCol";
	
	private CyNetwork network;
	
	
	@Before
	public void before() {
		NetworkTestSupport networkTestSupport = serviceRule.getNetworkTestSupport();
		CloudModelManager manager = serviceRule.getCloudModelManager();
		
		network = networkTestSupport.getNetwork();
		CyTable table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		table.createColumn(WORD_COL, String.class, false);
		
		CyNode node1 = network.addNode();
		network.getRow(node1).set(WORD_COL, "node1");
		CyNode node2 = network.addNode();
		network.getRow(node2).set(WORD_COL, "node2");
		CyNode node3 = network.addNode();
		network.getRow(node3).set(WORD_COL, "node3");
		
		manager.addNetwork(network);
	}
	
	private static List<String> getWords(Collection<CloudWordInfo> wordInfos) {
		List<String> words = new ArrayList<String>();
		for(CloudWordInfo info : wordInfos)
			words.add(info.getWord());
		return words;
	}
	
	
	@Test
	public void testCloudContents() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		NetworkParameters networkParameters = manager.getNetworkParameters(network);
		CloudParameters cloud = networkParameters.getCloudBuilder().setNodes(network.getNodeList()).setAllAttributes().build();
		List<CloudWordInfo> wordInfos = cloud.calculateCloud().getCloudWordInfoList();
		assertEquals(3, wordInfos.size());
		
		List<String> words = getWords(wordInfos);

		assertTrue(words.contains("node1"));
		assertTrue(words.contains("node2"));
		assertTrue(words.contains("node3"));
	}
	
	
	@Test
	public void testWordFilter() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		NetworkParameters networkParameters = manager.getNetworkParameters(network);
		CloudParameters cloud = networkParameters.getCloudBuilder().setNodes(network.getNodeList()).setAllAttributes().build();
		
		networkParameters.getFilter().add("node1");
		
		List<CloudWordInfo> wordInfos = cloud.calculateCloud().getCloudWordInfoList();
		assertEquals(2, wordInfos.size());
		
		List<String> words = getWords(wordInfos);

		assertFalse(words.contains("node1"));
		assertTrue(words.contains("node2"));
		assertTrue(words.contains("node3"));
	}
	
	
	@Test
	public void testCustomDelimeter() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		NetworkParameters networkParameters = manager.getNetworkParameters(network);
		CloudParameters cloud = networkParameters.getCloudBuilder().setNodes(network.getNodeList()).setAllAttributes().build();
		
		CyNode node = network.getNodeList().get(0);
		network.getRow(node).set(WORD_COL, "axbxcxdxe");
		
		assertEquals(3, cloud.calculateCloud().getCloudWordInfoList().size());
		
		networkParameters.getDelimeters().addDelimToUse("x");
		cloud.invalidate();
		
		List<CloudWordInfo> wordInfos = cloud.calculateCloud().getCloudWordInfoList();
		assertEquals(7, wordInfos.size());
		
		List<String> words = getWords(wordInfos);

		assertTrue(words.contains("a"));
		assertTrue(words.contains("b"));
		assertTrue(words.contains("c"));
		assertTrue(words.contains("d"));
		assertTrue(words.contains("e"));
	}
	
	
	@Test
	public void testDelimeterSplit() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		NetworkParameters networkParameters = manager.getNetworkParameters(network);
		WordDelimiters wordDelimeters = networkParameters.getDelimeters();
		
		String input = "mutS homolog 2, colon   cancer, nonpolyposis type 1 (E. coli)".toLowerCase();
		Set<String> result = wordDelimeters.split(input);
		
		assertEquals(10, result.size());
		assertTrue(result.contains("muts"));
		assertTrue(result.contains("homolog"));
		assertTrue(result.contains("2"));
		assertTrue(result.contains("colon"));
		assertTrue(result.contains("cancer"));
		assertTrue(result.contains("nonpolyposis"));
		assertTrue(result.contains("type"));
		assertTrue(result.contains("1"));
		assertTrue(result.contains("e"));
		assertTrue(result.contains("coli"));
	}
	
	
	@Test
	public void testDelimeterCloudWordInfo() {
		CloudModelManager manager = serviceRule.getCloudModelManager();
		NetworkParameters networkParameters = manager.getNetworkParameters(network);
		CloudParameters cloud = networkParameters.getCloudBuilder().setNodes(network.getNodeList()).setAllAttributes().build();
		
		String input = "mutS homolog 2, colon    cancer, nonpolyposis type 1 (E. coli)";
		
		CyNode node = network.getNodeList().get(0);
		network.getRow(node).set(WORD_COL, input);
		
		
		List<CloudWordInfo> wordInfos = cloud.calculateCloud().getCloudWordInfoList();
		List<String> result = getWords(wordInfos);

		assertEquals(12, result.size());
		
		assertTrue(result.contains("node1"));
		assertTrue(result.contains("node2"));
		assertTrue(result.contains("muts"));
		assertTrue(result.contains("homolog"));
		assertTrue(result.contains("2"));
		assertTrue(result.contains("colon"));
		assertTrue(result.contains("cancer"));
		assertTrue(result.contains("nonpolyposis"));
		assertTrue(result.contains("type"));
		assertTrue(result.contains("1"));
		assertTrue(result.contains("e"));
		assertTrue(result.contains("coli"));
	}
}
