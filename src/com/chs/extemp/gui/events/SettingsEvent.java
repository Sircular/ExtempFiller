package com.chs.extemp.gui.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class SettingsEvent extends EventObject {
	
	private Type type;
	private Object data;
	
	public enum Type {
		MAX_SOURCES_SET,
		UPLOAD_IMAGES_SET
	}

	public SettingsEvent(Object source, Type t, Object d) {
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
