package com.chs.extemp.gui;

import com.chs.extemp.TopicFileReader;
import com.chs.extemp.gui.debug.DebugPanel;
import com.chs.extemp.gui.events.ResearchCommand;
import com.chs.extemp.gui.events.ResearchEvent;
import com.chs.extemp.gui.menu.ResearchMenuBar;
import com.chs.extemp.gui.topicview.TopicListItem;
import com.chs.extemp.gui.topicview.TopicPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

@SuppressWarnings("serial")

public class ResearchGUI extends JFrame implements ResearchListener {
	public static final int GUI_WIDTH = 800;
	public static final int GUI_HEIGHT = 600;

	private ResearchWorker researchWorker;

	private TopicPanel topicPanel;
	private DebugPanel debugPanel;
	private ResearchMenuBar menuBar;
	private JProgressBar waitingBar;

	public ResearchGUI() {
		researchWorker = new ResearchWorker();
		researchWorker.registerListener(this);

		// initialize GUI
		init();
		pack();
		setGUIEnabled(false);
		setVisible(true);
		researchWorker.startWorkerThreads();
		loadTopicsFromEvernote();
	}

	public void init() {
		setTitle("CHS Extemp Filler");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// set up some tabs
		final JTabbedPane tabs = new JTabbedPane();
		topicPanel = new TopicPanel(this);
		debugPanel = new DebugPanel();
		menuBar = new ResearchMenuBar(this);
		waitingBar = new JProgressBar();

		tabs.addTab("Topics", topicPanel);
		tabs.addTab("Debug", debugPanel);
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (((JTabbedPane) (e.getSource())).getSelectedIndex() == 0) {
					topicPanel.getAddTopicPanel().requestFocusInWindow();
				}
			}
		});

		add(tabs, BorderLayout.CENTER);

		setJMenuBar(menuBar);

		setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
	}

	@Override
	public void dispose() {
		researchWorker.interruptWorkerThreads();
		System.exit(0);
	}

	public void addTopic(final String topic) {
		topicPanel.addTopic(topic);
		researchWorker.enqueueCommand(
				new ResearchCommand(
						this,
						ResearchCommand.Type.RESEARCH_TOPIC,
						topic
				)
		);
	}

	public void deleteSelectedTopics() {
		final List<TopicListItem> topics = topicPanel.getSelectedTopics();
		for (TopicListItem topic : topics) {
			if (topic.getState() != TopicListItem.State.RESEARCHING && topic.getState() != TopicListItem.State.DELETING) {
				researchWorker.enqueueCommand(
						new ResearchCommand(
								this,
								ResearchCommand.Type.DELETE_TOPIC,
								topic.getTopic()
						)
				);
			} else {
				displayError("Please wait until the topic finishes out the current operation.");
			}
		}
	}

	public void cancelResearch() {
		researchWorker.cancelResearch();
	}

	public void refreshTopics() {
		setGUIEnabled(false);
		loadTopicsFromEvernote();
	}

	public void loadTopicsFromFile() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (.txt)", "txt", "text"));

		final int choice = fileChooser.showOpenDialog(null);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			final String path = file.getAbsolutePath();
			for (String currentTopic : TopicFileReader.readTopicFile(path)) {
				if (!topicPanel.hasTopic(currentTopic)) {
					addTopic(currentTopic);
				}
			}
		}
	}

	private void loadTopicsFromEvernote() {
		researchWorker.enqueueCommand(new ResearchCommand(this, ResearchCommand.Type.LOAD_TOPICS, null));
	}

	public void onTopicQueuedForResearch(final String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.QUEUED_FOR_RESEARCH);
	}

	public void onTopicResearching(final String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHING);
	}

	public void onTopicResearched(final String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHED);
	}

	public void onTopicDeleting(final String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.DELETING);
	}

	public void onTopicDeleted(final String topic) {
		topicPanel.removeTopic(topic);
	}

	public void onRemoteTopicListLoaded(final String[] topics) {
		for (String topic : topics) {
			if (!topicPanel.hasTopic(topic)) {
				topicPanel.addTopic(topic, TopicListItem.State.RESEARCHED);
			}
		}
		setGUIEnabled(true);
		topicPanel.getAddTopicPanel().requestFocusInWindow();
	}

	public void onTopicError(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCH_ERROR);
		displayError("Error while editing topic: " + topic +
				". Please see debug log for details.");
	}

	public void onEvernoteError() {
		displayError("There was an error connecting to evernote.\n" +
				"Please close the program, check your internet settings, " +
				"and try again.");
	}

	public void displayError(final String error) {
		// displays an error message in
		// a message box
		JOptionPane.showMessageDialog(this, error);
	}

	public void handleMessageEvent(final ResearchEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (e.getType() == ResearchEvent.Type.TOPIC_QUEUED_FOR_RESEARCH) {
					onTopicQueuedForResearch((String) e.getData());
				} else if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHING) {
					onTopicResearching((String) e.getData());
				} else if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHED) {
					onTopicResearched((String) e.getData());
				} else if (e.getType() == ResearchEvent.Type.TOPIC_DELETING) {
					onTopicDeleting((String) e.getData());
				} else if (e.getType() == ResearchEvent.Type.TOPIC_DELETED) {
					onTopicDeleted((String) e.getData());
				} else if (e.getType() == ResearchEvent.Type.TOPIC_LIST_LOADED) {
					onRemoteTopicListLoaded((String[]) e.getData());
				} else if (e.getType() == ResearchEvent.Type.RESEARCH_ERROR) {
					onTopicError((String) e.getData());
				} else if (e.getType() == ResearchEvent.Type.EVERNOTE_CONNECTION_ERROR) {
					onEvernoteError();
				}
			}
		});
	}

	private void setGUIEnabled(final boolean state) {
		if (state) {
			waitingBar.setIndeterminate(false);
			remove(waitingBar);
			topicPanel.setContentsEnabled(true);
			menuBar.setContentsEnabled(true);
			validate();
			repaint();
		} else {
			add(waitingBar, BorderLayout.PAGE_END);
			waitingBar.setIndeterminate(true);
			topicPanel.setContentsEnabled(false);
			menuBar.setContentsEnabled(false);
			validate();
			repaint();
		}
	}
}
