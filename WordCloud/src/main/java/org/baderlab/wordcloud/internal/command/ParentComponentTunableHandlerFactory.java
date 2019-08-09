package org.baderlab.wordcloud.internal.command;

import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.work.BasicTunableHandlerFactory;

public class ParentComponentTunableHandlerFactory 
		extends BasicTunableHandlerFactory<ParentComponentTunableHandler>
		implements StringTunableHandlerFactory<ParentComponentTunableHandler> {

	public ParentComponentTunableHandlerFactory() {
		super(ParentComponentTunableHandler.class, ParentComponent.class);
	}

}
