package org.baderlab.wordcloud;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.baderlab.wordcloud.internal.model.CloudModelManager;
import org.baderlab.wordcloud.internal.model.CloudParameters;
import org.baderlab.wordcloud.internal.model.NetworkParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.property.CyProperty;
import org.junit.rules.ExternalResource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Sets up service mocks and test support objects and makes them available to the test suites.
 * 
 * Creates a new instance of each service for every test method and then cleans up after.
 * 
 * @author mkucera
 */
public class ServiceRule extends ExternalResource {

	@Mock private CyApplicationManager applicationManager;
	@Mock private CyTableManager tableManager;
	@Mock private StreamUtil streamUtil;
	@Mock private CyProperty<Properties> cyProperties;
	
	private NetworkTestSupport networkTestSupport;
	private TableTestSupport tableTestSupport;
	private CloudModelManager manager;
	
	
	@Override
	public void before() throws Exception {
		// inject mock objects for fields with @Mock annotation
		MockitoAnnotations.initMocks(this);
		
		// stub out methods that get called somewhere in the CloudModelManager
		when(streamUtil.getInputStream(endsWith("StopWords.txt"))).thenReturn(emptyStream());
		when(streamUtil.getInputStream(endsWith("FlaggedWords.txt"))).thenReturn(emptyStream());
		when(cyProperties.getProperties()).thenReturn(new Properties());
	
		networkTestSupport = new NetworkTestSupport();
		
		CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
		manager = new CloudModelManager(networkManager, tableManager, streamUtil, cyProperties); 
		
		tableTestSupport = new TableTestSupport();
	}
	
	@Override
	public void after() {
		for(NetworkParameters networkParams : manager.getNetworks()) {
			for(CloudParameters cloudParameters : networkParams.getClouds()) {
				cloudParameters.delete(); // deletes any columns that were created
			}
		}
	}
	
	public static InputStream emptyStream() {
		return new ByteArrayInputStream("".getBytes());
	}

	
	public CyTableManager getTableManager() {
		return tableManager;
	}

	public StreamUtil getStreamUtil() {
		return streamUtil;
	}

	public CyProperty<Properties> getCyProperties() {
		return cyProperties;
	}
	
	public NetworkTestSupport getNetworkTestSupport() {
		return networkTestSupport;
	}

	public CloudModelManager getCloudModelManager() {
		return manager;
	}

	public TableTestSupport getTableTestSupport() {
		return tableTestSupport;
	}
	
	public CyApplicationManager getCyApplicationManager() {
		return applicationManager;
	}
	
}
