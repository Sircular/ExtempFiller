package com.chs.extemp.gui.messaging;

import java.util.EventObject;

@SuppressWarnings("serial")
public class MessageEvent extends EventObject{
	
	public enum Type {
		TOPIC_RESEARCHED,
		TOPIC_LIST,
		ERROR
	}
	
	private Type type;
	private Object data;

	public MessageEvent(Object source, Type t, Object d) {
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
