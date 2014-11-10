package org.baderlab.wordcloud.internal.ui;

import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.baderlab.wordcloud.internal.cluster.CloudInfo;
import org.baderlab.wordcloud.internal.model.CloudParameters;


/**
 * Manages the tasks that compute the contents of a cloud.
 * 
 * It can take a while to compute the contents of a cloud for a large network.
 * This class manages the tasks that do that.
 * 
 * @author mkucera
 *
 */
public class CloudTaskManager {
	
	public interface Callback {
		void onFinish(CloudInfo cloudInfo);
	}
	
	/**
	 * Locks are used to serialize the computation for each CloudParameters object.
	 * The results of a cloud are usually cached so if the same cloud is submitted
	 * several times it should only be recalculated if something actually changed.
	 */
	private final WeakHashMap<CloudParameters, Object> locks = new WeakHashMap<CloudParameters, Object>();
	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	
	private synchronized Object getLock(CloudParameters cloudParams) {
		Object lock = locks.get(cloudParams);
		if(lock == null) {
			lock = new Object();
			locks.put(cloudParams, lock);
		}
		return lock;
	}
	
	public synchronized void disposeAll() {
		executor.shutdown();
		locks.clear(); 
	}
	
	
	/**
	 * Submits the task.
	 * Runs on a non UI thread.
	 * Callback runs on the UI thread.
	 * 
	 * If the cloud has already been computed then the callback usually executes immediately.
	 */
	public void submit(final CloudParameters cloudParams, final Callback callback) {
		Runnable task = new Runnable() {
			public void run() {
				
				final CloudInfo cloudInfo;
				synchronized(getLock(cloudParams)) {
					cloudInfo = cloudParams.calculateCloud(); // long running
				}
				
				if(callback != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							callback.onFinish(cloudInfo);
						}
					});
				}
				
			}
		};
		
		executor.submit(task);
	}
	

}
