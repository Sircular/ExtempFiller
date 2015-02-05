package com.chs.extemp.gui.topicview;

public class TopicListItem {
	public static enum State {
		DELETING,
		NOT_RESEARCHED,
		QUEUED_FOR_RESEARCH,
		RESEARCHING,
		RESEARCHED,
		RESEARCH_ERROR,
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
	
	public boolean equals(Object t2) {
		if (t2 instanceof String)
			return ((String)t2).equals(this.topic);
		else if (t2 instanceof TopicListItem)
			return ((TopicListItem)t2).getTopic().equals(this.topic);
		else // what else could it be?
			return false;
	}

}
