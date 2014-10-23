package com.chs.extemp.gui.topicview;

import java.util.ArrayList;
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
	
	public TopicList(DefaultListModel<TopicListItem> model) {
		this.listModel = model;
		this.setModel(model);
		this.refresh();
	}

	public void addTopic(final String topic, final TopicListItem.State state) {
		listModel.addElement(new TopicListItem(topic, state));
		this.refresh();
	}

	public void removeTopic(final String topic) {
		for (int i = 0; i < listModel.getSize(); i++) {
			final TopicListItem currentTopic = listModel.get(i);
			if (currentTopic.getTopic().equals(topic)) {
				listModel.remove(i);
				this.refresh();
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
		this.validate();
		this.repaint();
	}

	public List<TopicListItem> getTopics() {
		return Arrays.asList((TopicListItem[])this.listModel.toArray());
	}

	public List<TopicListItem> getSelectedTopicsList() {
		@SuppressWarnings("deprecation") // this function is necessary for Java 6
		TopicListItem[] itemArray = (TopicListItem[]) this.getSelectedValues();
		return new ArrayList<TopicListItem>(Arrays.asList(itemArray));
	}
}
