package com.chs.extemp.gui;

import com.chs.extemp.ExtempLogger;
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
import java.util.logging.Logger;

@SuppressWarnings("serial")

public class ResearchGUI extends JFrame implements ResearchListener {
	public static final int GUI_WIDTH = 800;
	public static final int GUI_HEIGHT = 600;

	private ResearchWorker researchWorker;
	private Logger logger;

	private TopicPanel topicPanel;
	private DebugPanel debugPanel;
	private ResearchMenuBar menuBar;
	private JProgressBar waitingBar;

	public ResearchGUI() {
		logger = ExtempLogger.getLogger();
		logger.info("Initialized GUI.");

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
		JTabbedPane tabs = new JTabbedPane();
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

	public void addTopic(String topic) {
		topicPanel.addTopic(topic);
		researchWorker.enqueueCommand(
				new ResearchCommand(
						this,
						ResearchCommand.Type.RESEARCH_TOPIC,
						topic
				)
		);
	}

	public void deleteSelectedTopic() {
		TopicListItem topic = topicPanel.getSelectedTopic();
		if (topic.getState() != TopicListItem.State.RESEARCHING) {
			researchWorker.enqueueCommand(
					new ResearchCommand(
							this,
							ResearchCommand.Type.DELETE_TOPIC,
							topicPanel.getSelectedTopic().getTopic()
					)
			);
		} else {
			displayError("Please wait until the topic finishes being researched.");
		}
	}

	public void refreshTopics() {
		setGUIEnabled(false);
		loadTopicsFromEvernote();
	}

	public void loadTopicsFromFile() {
		JFileChooser fileChooser = new JFileChooser();

		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Text Files (.txt)", "txt", "text");
		fileChooser.setFileFilter(fileFilter);

		int choice = fileChooser.showOpenDialog(null);
		if (choice == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String path = file.getAbsolutePath();
			for (String currentTopic : TopicFileReader.readTopicFile(path)) {
				if (!topicPanel.hasTopic(currentTopic)) {
					topicPanel.addTopic(currentTopic);
					researchWorker.enqueueCommand(
							new ResearchCommand(
									this,
									ResearchCommand.Type.RESEARCH_TOPIC,
									currentTopic
							)
					);
				}
			}
		}
	}

	private void loadTopicsFromEvernote() {
		researchWorker.enqueueCommand(new ResearchCommand(this, ResearchCommand.Type.LOAD_TOPICS, null));
	}

	public void onTopicResearching(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHING);
	}

	public void onTopicResearched(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHED);
	}

	public void onTopicDeleting(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.DELETING);
	}

	public void onTopicDeleted(String topic) {
		topicPanel.removeTopic(topic);
	}

	public void onRemoteTopicListLoaded(String[] topics) {
		// used to populate the list of
		// already-researched topics

		topicPanel.clearTopicList();
		setGUIEnabled(true);
		topicPanel.getAddTopicPanel().requestFocusInWindow();

		for (String topic : topics) {
			topicPanel.addTopic(topic, TopicListItem.State.RESEARCHED);
		}
	}

	public void onTopicError(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCH_ERROR);
		displayError("Error while researching topic: " + topic +
				". Please see debug log for details.");
	}

	public void onEvernoteError() {
		displayError("There was an error connecting to evernote.\n" +
				"Please close the program, check your internet settings, " +
				"and try again.");
	}

	public void displayError(String error) {
		// displays an error message in
		// a message box
		JOptionPane.showMessageDialog(this, error);
	}

	public void handleMessageEvent(ResearchEvent e) {
		if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHING) {
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

	private void setGUIEnabled(boolean state) {
		if (state) {
			waitingBar.setIndeterminate(false);
			remove(waitingBar);
			topicPanel.setContentsEnabled(true);
			menuBar.setContentsEnabled(true);
			//NPE HAPPENS HERE....
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
