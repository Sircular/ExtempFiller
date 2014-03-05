package com.chs.extemp.gui.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ResearchEvent extends EventObject {
	public enum Type {
		TOPIC_RESEARCHING,
		TOPIC_RESEARCHED,
		TOPIC_DELETING,
		TOPIC_DELETED,
		TOPIC_LIST_LOADED,
		RESEARCH_ERROR,
		EVERNOTE_CONNECTION_ERROR,
	}

	private Type type;
	private Object data;

	public ResearchEvent(Object source, Type t, Object d) {
		super(source);

		this.type = t;
		this.data = d;
	}

	public Type getType() {
		return this.type;
	}

	public Object getData() {
		return this.data;
	}
}
