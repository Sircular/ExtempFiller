package com.chs.extemp.gui.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ResearchCommand extends EventObject {
	public enum Type {
		RESEARCH_TOPIC,
		UNQUEUE_TOPIC,
		DELETE_TOPIC,
		LOAD_TOPICS
	}

	private final Type type;
	private final String topic;

	public ResearchCommand(final Object source, final Type t, final String topic) {
		super(source);
		this.type = t;
		this.topic = topic;
	}

	public Type getType() {
		return type;
	}

	public String getTopic() {
		return topic;
	}
}
