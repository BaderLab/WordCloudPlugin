package org.baderlab.wordcloud.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
	
	// Create one thread per CloudParameters, this will serialize each cloud but allow multiple clouds to be calculated concurrently.
	private Map<CloudParameters,ExecutorService> threads = Collections.synchronizedMap(new HashMap<CloudParameters, ExecutorService>());
	
	private synchronized ExecutorService getThread(CloudParameters cloud) {
		ExecutorService executor = threads.get(cloud);
		if(executor == null) {
			executor = Executors.newSingleThreadExecutor(); // one thread per cloud
			threads.put(cloud, executor);
		}
		return executor;
	}
	
	public synchronized void dispose(CloudParameters cloud) {
		ExecutorService executor = threads.remove(cloud);
		if(executor != null) {
			try {
				executor.shutdown();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void disposeAll() {
		Collection<ExecutorService> executors = new ArrayList<ExecutorService>(threads.values());
		threads.clear();
		
		for(ExecutorService executor : executors) {
			try {
				executor.shutdown();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Submits the task.
	 * Runs on a non UI thread.
	 * Callback runs on the UI thread.
	 * 
	 * If the cloud has already been computed then the callback executes immediately.
	 * 
	 * 
	 * @param cloudParams
	 * @param callback Will only get
	 */
	public void submit(final CloudParameters cloudParams, final Callback callback) {
		Runnable task = new Runnable() {
			public void run() {
				
				final CloudInfo cloudInfo = cloudParams.calculateCloud(); // long running
				
				if(callback != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							callback.onFinish(cloudInfo);
						}
					});
				}
				
			}
		};
		
		getThread(cloudParams).submit(task);
	}
	

}
