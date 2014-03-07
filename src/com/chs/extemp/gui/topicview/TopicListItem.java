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
	private final String topic;

	public TopicListItem(final String topic, final State state) {
		this.topic = topic;
		this.currentState = state;
	}

	public State getState() {
		return this.currentState;
	}

	public String getTopic() {
		return this.topic;
	}

	public void setState(final State state) {
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
				stateString = "Error";
				break;
		}
		return "(" + stateString + ") " + this.topic;
	}

}
