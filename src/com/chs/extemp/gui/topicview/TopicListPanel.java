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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

@SuppressWarnings("serial")
public class TopicListPanel extends JPanel {
	private final ResearchGUI gui;
	private TopicList topicList;
	private JScrollPane topicListScroll;
	private ActionButton deleteButton;
	private ActionButton refreshButton;

	public TopicListPanel(final ResearchGUI gui) {
		this.gui = gui;
		init();
	}

	private void init() {
		setBorder(new EmptyBorder(0, 0, 10, 0));

		topicList = new TopicList();

		deleteButton = new ActionButton("Delete", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.deleteSelectedTopics();
			}
		});

		refreshButton = new ActionButton("Refresh", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.refreshTopics();
			}
		});


		deleteButton.setEnabled(false);
		refreshButton.setEnabled(false);

		topicList.getSelectionModel().addListSelectionListener(new TopicSelectionListener(deleteButton));
		topicList.addKeyListener(new DeleteKeyListener());

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));

		buttonPanel.add(refreshButton);
		buttonPanel.add(deleteButton);

		setLayout(new BorderLayout());

		topicListScroll = new JScrollPane(topicList);
		topicListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		topicListScroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 10, 0), new EtchedBorder()));

		add(topicListScroll, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	public void setContentsEnabled(final boolean state) {
		deleteButton.setEnabled(state);
		refreshButton.setEnabled(state);
		topicList.setEnabled(state);
		topicListScroll.setEnabled(state);
	}

	public void addTopic(final String topic, final TopicListItem.State state) {
		topicList.addTopic(topic, state);
	}

	public void setTopicState(final String topic, final TopicListItem.State state) {
		topicList.setTopicState(topic, state);
	}

	public List<TopicListItem> getSelectedTopics() {
		return (List<TopicListItem>) topicList.getSelectedValuesList();
	}

	public void removeTopic(final String topic) {
		topicList.removeTopic(topic);
	}

	public boolean hasTopic(final String topic) {
		return topicList.hasTopic(topic);
	}

	private class TopicSelectionListener implements ListSelectionListener {
		private JButton deleteButton;

		public TopicSelectionListener(final JButton deleteButton) {
			this.deleteButton = deleteButton;
		}

		@Override
		public void valueChanged(final ListSelectionEvent e) {
			final List<TopicListItem> topicItems = getSelectedTopics();
			if (topicItems == null) {
				deleteButton.setEnabled(false);
				return;
			}
			deleteButton.setEnabled(e.getFirstIndex() >= 0);
		}
	}

	private class DeleteKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				final List<TopicListItem> topicItems = getSelectedTopics();
				if (topicItems != null) {
					gui.deleteSelectedTopics();
				}
			}
		}
	}
}
