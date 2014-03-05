package com.chs.extemp.gui.topicview;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class TopicList extends JList {

	private DefaultListModel listModel;

	public TopicList() {
		this.listModel = new DefaultListModel();
		this.setModel(listModel);
	}

	public void addTopic(String topic) {
		listModel.addElement(new TopicListItem(topic, TopicListItem.State.NOT_RESEARCHED));
	}

	public void addTopic(String topic, TopicListItem.State state) {
		listModel.addElement(new TopicListItem(topic, state));
		refresh();
	}

	public void removeTopic(String topic) {
		for (int i = 0; i < listModel.getSize(); i++) {
			TopicListItem currentTopic = (TopicListItem) listModel.get(i);
			if (currentTopic.getTopic().equals(topic)) {
				listModel.remove(i);
				refresh();
			}
		}
	}

	public void setTopicState(String topic, TopicListItem.State state) {
		for (int i = 0; i < this.listModel.size(); i++) {
			TopicListItem item = (TopicListItem) this.listModel.get(i);
			if (item.getTopic().equals(topic)) {
				item.setState(state);
				refresh();
			}
		}
	}

	public boolean hasTopic(String topic) {
		for (int i = 0; i < this.listModel.size(); i++) {
			TopicListItem item = (TopicListItem) this.listModel.get(i);
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

	public void addSelectionListener(ListSelectionListener listener) {
		getSelectionModel().addListSelectionListener(listener);
	}
}
