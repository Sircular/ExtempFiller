package com.chs.extemp.gui.topicview;

import javax.swing.*;

@SuppressWarnings("serial")
public class TopicList extends JList {

	private final DefaultListModel listModel;

	public TopicList() {
		this.listModel = new DefaultListModel();
		this.setModel(listModel);
	}

	public void addTopic(final String topic, final TopicListItem.State state) {
		listModel.addElement(new TopicListItem(topic, state));
		refresh();
	}

	public void removeTopic(final String topic) {
		for (int i = 0; i < listModel.getSize(); i++) {
			final TopicListItem currentTopic = (TopicListItem) listModel.get(i);
			if (currentTopic.getTopic().equals(topic)) {
				listModel.remove(i);
				refresh();
			}
		}
	}

	public void setTopicState(final String topic, final TopicListItem.State state) {
		for (int i = 0; i < this.listModel.size(); i++) {
			final TopicListItem item = (TopicListItem) this.listModel.get(i);
			if (item.getTopic().equals(topic)) {
				item.setState(state);
				refresh();
			}
		}
	}

	public boolean hasTopic(final String topic) {
		for (int i = 0; i < this.listModel.size(); i++) {
			final TopicListItem item = (TopicListItem) this.listModel.get(i);
			if (item.getTopic().equals(topic)) {
				return true;
			}
		}
		return false;
	}

	public void refresh() {
		validate();
		repaint();
	}
}
