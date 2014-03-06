package com.chs.extemp.gui.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ResearchCommand extends EventObject {
	public enum Type {
		RESEARCH_TOPIC,
		DELETE_TOPIC
	}

	private Type type;
	private String topic;

	public ResearchCommand(Object source, Type t, String topic) {
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
