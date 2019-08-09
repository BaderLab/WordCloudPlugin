package org.baderlab.wordcloud.internal.command;

import java.awt.Component;

public class ParentComponent {
	
	private final Component component;

	public ParentComponent(Component component) {
		this.component = component;
	}

	public Component getComponent() {
		return component;
	}

	@Override
	public String toString() {
		return "ParentComponent [component=" + component + "]";
	}
	
}
