package org.baderlab.wordcloud.internal;

import javax.swing.Action;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ActionNodeViewTaskFactory implements NodeViewTaskFactory {
	private Action action;

	public ActionNodeViewTaskFactory(Action action) {
		this.action = action;
	}
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new AbstractTask() {
			@Override
			public void run(TaskMonitor monitor) throws Exception {
				action.actionPerformed(null);
			}
		});
	}
	
	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return nodeView != null && networkView != null;
	}
}
