package org.baderlab.wordcloud.internal.model.next;

/**
 * Proxy interface to get a cloud from whatever source implements this interface.
 * (Makes it easier to reuse Actions.)
 * 
 * @author mkucera
 */
public interface CloudProvider {

	/**
	 * Returns a cloud.
	 * @return may return null
	 */
	public CloudParameters getCloud();
}
