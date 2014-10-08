package org.baderlab.wordcloud.internal.model;

public interface CloudModelListener {

	/**
	 * Event that indicates that a new CloudParameters object was created in the model.
	 * Not fired for "Null" clouds.
	 */
	void cloudAdded(CloudParameters cloud);

	/**
	 * Event that signifies that the given CloudParameters object has been removed from the model.
	 * Warning, don't do much with the given cloud, it has been deleted already.
	 */
	void cloudDeleted(CloudParameters cloud);
	
	/**
	 * Event that indicates that a network (and all its clouds) have been removed from the model.
	 * Caution, the networkParams parameter has already been removed from the model.
	 * The results of making modifications are not defined.
	 */
	void networkRemoved(NetworkParameters networkParams);
	
	
	/**
	 * Event that indicates some aspect of the network has been modified (selected nodes for example)
	 * and needs to be repainted.
	 */
	void networkModified(NetworkParameters networkParams);

	
	/**
	 * Event that indicates that the given CloudParameters object has a new name.
	 */
	void cloudRenamed(CloudParameters cloudParameters);

}
