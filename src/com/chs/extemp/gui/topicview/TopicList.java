package com.chs.extemp.gui.topicview;

import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class TopicList extends JList<TopicListItem> {

	private final DefaultListModel<TopicListItem> listModel;

	public TopicList() {
		this.listModel = new DefaultListModel<TopicListItem>();

		this.setModel(listModel);
	}

	public void addTopic(final String topic, final TopicListItem.State state) {
		listModel.addElement(new TopicListItem(topic, state));
		refresh();
	}

	public void removeTopic(final String topic) {
		for (int i = 0; i < listModel.getSize(); i++) {
			final TopicListItem currentTopic = listModel.get(i);
			if (currentTopic.getTopic().equals(topic)) {
				listModel.remove(i);
				refresh();
			}
		}
	}

	public void setTopicState(final String topic, final TopicListItem.State state) {
		for (int i = 0; i < this.listModel.size(); i++) {
			final TopicListItem item = this.listModel.get(i);
			if (item.getTopic().equals(topic)) {
				item.setState(state);
				refresh();
			}
		}
	}

	public boolean hasTopic(final String topic) {
		for (int i = 0; i < this.listModel.size(); i++) {
			final TopicListItem item = this.listModel.get(i);
			if (item.getTopic().equals(topic))
				return true;
		}
		return false;
	}

	public void refresh() {
		validate();
		repaint();
	}

	public TopicListItem[] getTopics() {
		return Arrays.copyOf(listModel.toArray(), listModel.size(), TopicListItem[].class);
	}

	public List<TopicListItem> getSelectedTopicsList() {
		TopicListItem[] itemArray = this.getSelectedValues();
		return new ArrayList<TopicListItem>(Arrays.asList(itemArray));
	}
}
