package com.chs.extemp.gui.topicview;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.chs.extemp.gui.ResearchGUI;

@SuppressWarnings("serial")
public class TopicPanel extends JPanel{
	
	private ResearchGUI gui;
	
	private TopicList topicList;
	private AddTopicPanel atp;
	
	public TopicPanel(ResearchGUI gui) {
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
		atp = new AddTopicPanel(this.gui);
		add(atp, BorderLayout.PAGE_END);
	}
	
	public void setContentsEnabled(boolean state) {
		topicList.setEnabled(state);
		atp.setContentsEnabled(state);
	}
	
	public void addTopic(String topic) {
		topicList.addTopic(topic);
		topicList.repaint();
	}
	
	public void addTopic(String topic, TopicListItem.State state){
		topicList.addTopic(topic, state);
		topicList.repaint();
	}
	
	public void setTopicState(String topic, TopicListItem.State state) {
		topicList.setTopicState(topic, state);
		topicList.repaint();
	}

}
