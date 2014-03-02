package com.chs.extemp.gui.topicview;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class TopicList extends JList {
	
	private DefaultListModel listModel;
	private ListSelectionModel selectionModel;
	
	public TopicList() {
		init();
		
	}

	private void init() {
		this.listModel = new DefaultListModel();
		this.selectionModel = getSelectionModel();
	}
	
	public void addTopic(String topic) {
		listModel.addElement(new TopicListItem(topic, TopicListItem.State.NOT_RESEARCHED));
		this.setModel(listModel);
	}
	
	public void addTopic(String topic, TopicListItem.State state) {
		listModel.addElement(new TopicListItem(topic, state));
		this.setModel(listModel);
	}
	
	public void removeTopic(String topic) {
		for(int i = 0; i < listModel.getSize(); i++) {
			TopicListItem currentTopic = (TopicListItem) listModel.get(i);
			if(currentTopic.getTopic() == topic) {
				listModel.remove(i);
			}
		}
	}
	
	public void setTopicState(String topic, TopicListItem.State state) {
		for(int i = 0; i < this.listModel.size(); i++) {
			TopicListItem item = (TopicListItem)this.listModel.get(i);
			if(item.getTopic() == topic) {
				item.setState(state);
			}
		}
	}
	
	public boolean hasTopic(String topic) {
		for(int i = 0; i < this.listModel.size(); i++) {
			TopicListItem item = (TopicListItem)this.listModel.get(i);
			if(item.getTopic() == topic) {
				return true;
			}
		}
		return false;
	}
	
	public void addSelectionListener(ListSelectionListener listener) {
		this.selectionModel.addListSelectionListener(listener);
	}
}
