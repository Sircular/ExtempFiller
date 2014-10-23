package com.chs.extemp.gui.topicview;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.chs.extemp.gui.ResearchGUI;

@SuppressWarnings("serial")
public class TopicPanel extends JPanel {

	private final ResearchGUI gui;

	private TopicListPanel topicListPanel;
	private AddTopicPanel addTopicPanel;

	public TopicPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}

	private void init() {
		// set up some basic aesthetic stuff
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// set up the layout manager
		this.setLayout(new BorderLayout());

		topicListPanel = new TopicListPanel(gui);
		add(topicListPanel, BorderLayout.CENTER);
		addTopicPanel = new AddTopicPanel(this.gui);
		add(addTopicPanel, BorderLayout.PAGE_END);
	}

	public void setContentsEnabled(final boolean state) {
		topicListPanel.setContentsEnabled(state);
		addTopicPanel.setContentsEnabled(state);
	}

	public void addTopic(final String topic) {
		addTopic(topic, TopicListItem.State.NOT_RESEARCHED);
	}

	public void addTopic(final String topic, final TopicListItem.State state) {
		topicListPanel.addTopic(topic, state);
	}

	public void setTopicState(final String topic, final TopicListItem.State state) {
		topicListPanel.setTopicState(topic, state);
	}

	public boolean hasTopic(final String topic) {
		return topicListPanel.hasTopic(topic);
	}

	public AddTopicPanel getAddTopicPanel() {
		return addTopicPanel;
	}

	public List<TopicListItem> getSelectedTopics() {
		return topicListPanel.getSelectedTopics();
	}

	public void removeTopic(final String topic) {
		topicListPanel.removeTopic(topic);
	}

	public List<TopicListItem> getTopics() {
		return topicListPanel.getTopics();
	}
}
