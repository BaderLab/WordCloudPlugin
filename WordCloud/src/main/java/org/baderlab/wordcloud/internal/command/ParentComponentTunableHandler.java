package org.baderlab.wordcloud.internal.command;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cytoscape.command.AbstractStringTunableHandler;
import org.cytoscape.work.Tunable;

public class ParentComponentTunableHandler extends AbstractStringTunableHandler {

	public ParentComponentTunableHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
	}

	public ParentComponentTunableHandler(Method get, Method set, Object o, Tunable t) {
		super(get, set, o, t);
	}

	@Override
	public ParentComponent processArg(String arg) {
		return null;
	}
	
	@Override
	public void setValue(Object value) throws IllegalAccessException, InvocationTargetException {
		if(value instanceof Component) {
			super.setValue(new ParentComponent((Component)value));
		}
	}

}
