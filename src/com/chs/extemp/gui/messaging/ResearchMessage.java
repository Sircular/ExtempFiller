package com.chs.extemp.gui.messaging;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ResearchMessage extends EventObject{
	
	public enum Type {
		// these types are used server-to-client
		TOPIC_RESEARCHED,
		TOPIC_LIST,
		RESEARCH_ERROR,
		EVERNOTE_CONNECTION_ERROR,
		// no client-to-server message types
		// (let's burn that bridge when we
		// come to it)
	}
	
	private Type type;
	private Object data;

	public ResearchMessage(Object source, Type t, Object d) {
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
