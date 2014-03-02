package com.chs.extemp.gui.topicview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.chs.extemp.gui.ResearchGUI;

@SuppressWarnings("serial")
public class TopicListPanel extends JPanel{
	private ResearchGUI gui;
	private TopicList topicList;
	private JButton deleteButton;
	
	private DeleteButtonListener deleteListener;
	private TopicSelectionListener selectListener;
	
	public TopicListPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}
	
	private void init() {
		topicList = new TopicList();
		deleteButton = new JButton("Delete");
		deleteButton.setEnabled(false);
		
		selectListener = new TopicSelectionListener(deleteButton);
		deleteListener = new DeleteButtonListener(gui);
		
		topicList.addSelectionListener(selectListener);
		deleteButton.addActionListener(deleteListener);
		
		setLayout(new BorderLayout());
		
		add(topicList, BorderLayout.CENTER);
		add(deleteButton, BorderLayout.PAGE_END);
	}
	
	public void addTopic(String topic) {
		addTopic(topic, TopicListItem.State.NOT_RESEARCHED);
	}
	
	public void addTopic(String topic, TopicListItem.State state) {
		topicList.addTopic(topic, state);
	}
	
	public void setTopicState(String topic, TopicListItem.State state) {
		topicList.setTopicState(topic, state);
	}
	
	public String getSelectedTopic() {
		TopicListItem topicItem = (TopicListItem)topicList.getSelectedValue();
		return topicItem.getTopic();
	}
	
	public void removeTopic(String topic) {
		topicList.removeTopic(topic);
	}
	
	public boolean hasTopic(String topic){
		return topicList.hasTopic(topic);
	}
	
	private class TopicSelectionListener implements ListSelectionListener {
		private JButton deleteButton;
		
		public TopicSelectionListener(JButton deleteButton) {
			this.deleteButton = deleteButton;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			deleteButton.setEnabled(e.getFirstIndex() >= 0);	
		}
		
	}
	
	private class DeleteButtonListener implements ActionListener {
		private ResearchGUI gui;
		
		public DeleteButtonListener(ResearchGUI gui) {
			this.gui = gui;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			gui.removeSelectedTopic();
		}
		
	}

}
