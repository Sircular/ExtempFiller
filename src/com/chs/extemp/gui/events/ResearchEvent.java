package com.chs.extemp.gui.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ResearchEvent extends EventObject {
	public enum Type {
		TOPIC_QUEUED_FOR_RESEARCH,
		TOPIC_RESEARCHING,
		TOPIC_RESEARCHED,
		TOPIC_DELETING,
		TOPIC_DELETED,
		TOPIC_LIST_LOADED,
		RESEARCH_ERROR,
		EVERNOTE_CONNECTION_ERROR
	}

	private final Type type;
	private final Object data;

	public ResearchEvent(final Object source, final Type t, final Object d) {
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
