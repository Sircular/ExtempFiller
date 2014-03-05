package com.chs.extemp.gui.topicview;

public class TopicListItem {
	public static enum State {
		DELETING,
		RESEARCHING,
		NOT_RESEARCHED,
		RESEARCHED,
		RESEARCH_ERROR
	}

	private State currentState;
	private String topic;

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
			case DELETING:
				stateString = "Deleting...";
				break;
			case NOT_RESEARCHED:
				stateString = "Not Researched";
				break;
			case RESEARCHING:
				stateString = "Researching...";
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
