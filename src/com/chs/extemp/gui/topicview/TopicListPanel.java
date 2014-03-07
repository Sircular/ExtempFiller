package com.chs.extemp.gui.topicview;

import com.chs.extemp.gui.ActionButton;
import com.chs.extemp.gui.ResearchGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class TopicListPanel extends JPanel {
	private ResearchGUI gui;
	private TopicList topicList;
	private JScrollPane topicListScroll;
	private ActionButton deleteButton;
	private ActionButton refreshButton;

	public TopicListPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}

	private void init() {
		setBorder(new EmptyBorder(0, 0, 10, 0));

		topicList = new TopicList();

		deleteButton = new ActionButton("Delete", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.deleteSelectedTopic();
			}
		});

		refreshButton = new ActionButton("Refresh", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.refreshTopics();
			}
		});


		deleteButton.setEnabled(false);
		refreshButton.setEnabled(false);

		topicList.addSelectionListener(new TopicSelectionListener(deleteButton));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));

		buttonPanel.add(deleteButton);
		buttonPanel.add(refreshButton);

		setLayout(new BorderLayout());

		topicListScroll = new JScrollPane(topicList);
		topicListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		topicListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		topicListScroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 10, 0), new EtchedBorder()));

		add(topicListScroll, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	public void setContentsEnabled(boolean state) {
		deleteButton.setEnabled(state);
		refreshButton.setEnabled(state);
		topicList.setEnabled(state);
		topicListScroll.setEnabled(state);
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

	public TopicListItem getSelectedTopic() {
		return (TopicListItem) topicList.getSelectedValue();
	}

	public void removeTopic(String topic) {
		topicList.removeTopic(topic);
	}

	public boolean hasTopic(String topic) {
		return topicList.hasTopic(topic);
	}

	public void clearTopicList() {
		topicList.clearTopicList();
	}

	private class TopicSelectionListener implements ListSelectionListener {
		private JButton deleteButton;

		public TopicSelectionListener(JButton deleteButton) {
			this.deleteButton = deleteButton;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			TopicListItem topicItem = getSelectedTopic();
			if (topicItem == null) {
				deleteButton.setEnabled(false);
				return;
			}
			deleteButton.setEnabled(e.getFirstIndex() >= 0);
		}
	}
}
