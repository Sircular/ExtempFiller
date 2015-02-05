package com.chs.extemp.gui.topicview;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.chs.extemp.DataReader;
import com.chs.extemp.gui.ResearchGUI;
import com.chs.extemp.gui.topicview.TopicListItem.State;

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
	
	public void setTopics(List<String> newTopics) {
		for (String t : newTopics) {
			if (!topicListPanel.getTopics().contains(t))
				topicListPanel.addTopic(t, TopicListItem.State.RESEARCHED);
		}
		
		for (TopicListItem t : topicListPanel.getTopics()) {
			if (!newTopics.contains(t) &&
					t.getState() != TopicListItem.State.RESEARCHING &&
					t.getState() != TopicListItem.State.QUEUED_FOR_RESEARCH) {
				topicListPanel.removeTopic(t.getTopic());
			}
		}
	}

	public void cleanup() {
		// save the cache
		final List<TopicListItem> topicItems = topicListPanel.getTopics();
		if (topicItems != null && topicItems.size() > 0) {
			final ArrayList<String> topicStrings = new ArrayList<String>();
			
			for (final TopicListItem topicItem : topicItems) {
				final String topicString = topicItem.getTopic();
				final State topicState = topicItem.getState();
			
				if (topicState == State.RESEARCHED || topicState == State.RESEARCHING)
					topicStrings.add(topicString);
			}
			DataReader.saveCacheFile(DataReader.DEFAULT_CACHE_PATH, topicStrings.toArray(new String[]{}));
		}
	}
}
