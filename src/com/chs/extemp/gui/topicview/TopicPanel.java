package com.chs.extemp.gui.topicview;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.chs.extemp.gui.ResearchGUI;

@SuppressWarnings("serial")
public class TopicPanel extends JPanel{
	
	private ResearchGUI gui;
	
	private TopicListPanel topicListPanel;
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
		
		topicListPanel = new TopicListPanel(gui);
		add(topicListPanel, BorderLayout.CENTER);
		atp = new AddTopicPanel(this.gui);
		add(atp, BorderLayout.PAGE_END);
	}
	
	public void setContentsEnabled(boolean state) {
		topicListPanel.setEnabled(state);
		atp.setContentsEnabled(state);
	}
	
	public void addTopic(String topic) {
		addTopic(topic, TopicListItem.State.NOT_RESEARCHED);
	}
	
	public void addTopic(String topic, TopicListItem.State state){
		topicListPanel.addTopic(topic, state);
	}
	
	public void setTopicState(String topic, TopicListItem.State state) {
		topicListPanel.setTopicState(topic, state);
		topicListPanel.repaint();
	}
	
	public boolean hasTopic(String topic) {
		return topicListPanel.hasTopic(topic);
	}

}
