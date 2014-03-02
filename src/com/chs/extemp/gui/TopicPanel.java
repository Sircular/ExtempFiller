package com.chs.extemp.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class TopicPanel extends JPanel{
	
	private ExtempFillerGUI gui;
	
	private TopicList topicList;
	
	public TopicPanel(ExtempFillerGUI gui) {
		this.gui = gui;
		init();
	}
	
	private void init() {
		// set up some basic aesthetic stuff
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// set up the layout manager
		this.setLayout(new BorderLayout());
		
		topicList = new TopicList();
		add(topicList, BorderLayout.CENTER);
		AddTopicPanel atp = new AddTopicPanel(this.gui);
		add(atp, BorderLayout.PAGE_END);
		
		topicList.addTopic("custom topic", TopicListItem.State.RESEARCHED);
	}
	
	public void addTopic(String topic) {
		topicList.addTopic(topic);
	}
	
	public void addTopic(String topic, TopicListItem.State state){
		topicList.addTopic(topic, state);
	}
	
	public void setTopicResearched(String topic, TopicListItem.State state) {
		topicList.setTopicResearched(topic, state);
	}

}
