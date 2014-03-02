package com.chs.extemp.gui.topicview;

public class TopicListItem {
	public static enum State {
		NOT_RESEARCHED,
		RESEARCHED,
		RESEARCH_ERROR
	}
	
	private State currentState;
	private String topic;
	
	public TopicListItem(String topic) {
		this.topic = topic;
		this.currentState = State.NOT_RESEARCHED;
	}
	
	public TopicListItem(String topic, State state) {
		this.topic = topic;
		this.currentState = state;
	}
	
	public State getState() {
		return this.currentState;
	}
	
	public String getTopic() {
		return this.topic;
	}
	
	public void setState(State state) {
		this.currentState = state;
	}
	
	public String toString() {
		String stateString = "";
		switch (this.currentState) {
		case NOT_RESEARCHED:
			stateString = "Not Researched";
			break;
		case RESEARCHED:
			stateString = "Researched";
			break;
		case RESEARCH_ERROR:
			stateString = "Error while researching";
			break;
		}
		return "(" + stateString + ") " + this.topic;
	}
	
}
