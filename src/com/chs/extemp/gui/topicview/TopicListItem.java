package com.chs.extemp.gui.topicview;

public class TopicListItem {
	public static enum State {
		DELETING,
		NOT_RESEARCHED,
		QUEUED_FOR_RESEARCH,
		RESEARCHING,
		RESEARCHED,
		RESEARCH_ERROR,
		// printing specific stuff
		NOT_QUEUED_FOR_PRINTING, // for the print dialog
		QUEUED_FOR_PRINTING,
		PRINTING,
		PRINTED,
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
		return "(" + this.currentState.toString().replace('_', ' ') + ") " + this.topic;
	}

}
