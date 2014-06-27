package com.chs.extemp.gui;

import com.chs.extemp.CacheFileHandler;
import com.chs.extemp.ExtempLogger;
import com.chs.extemp.TopicFileReader;
import com.chs.extemp.gui.debug.DebugPanel;
import com.chs.extemp.gui.events.ResearchCommand;
import com.chs.extemp.gui.events.ResearchEvent;
import com.chs.extemp.gui.menu.ResearchMenuBar;
import com.chs.extemp.gui.topicview.TopicListItem;
import com.chs.extemp.gui.topicview.TopicListItem.State;
import com.chs.extemp.gui.topicview.TopicPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("serial")

public class ResearchGUI extends JFrame implements ResearchListener {
	public static final int GUI_WIDTH = 800;
	public static final int GUI_HEIGHT = 600;

	private EvernoteWorker evernoteWorker;

	private TopicPanel topicPanel;
	private DebugPanel debugPanel;
	private ResearchMenuBar menuBar;
	private JProgressBar waitingBar;
	
	private Logger log;

	public ResearchGUI() {
		// load the logger
		log = ExtempLogger.getLogger();
		
		// load the evernote client
		evernoteWorker = new EvernoteWorker();
		evernoteWorker.registerListener(this);

		// initialize GUI
		init();
		pack();
		setGUIEnabled(false);
		setVisible(true);
		evernoteWorker.startWorkerThreads();
		if(CacheFileHandler.cacheFileExists(CacheFileHandler.DEFAULT_CACHE_PATH)) {
			log.info("Loading topic list from cache file...");
			onTopicListSupplied(CacheFileHandler.loadCacheFile(CacheFileHandler.DEFAULT_CACHE_PATH));
		}else{
			log.info("No cache file found, requesting topics from Evernote...");
			loadTopicsFromEvernote();
		}
	}

	public void init() {
		setTitle("CHS Extemp Filler");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
		evernoteWorker.interruptWorkerThreads();
		
		// save the cache
		TopicListItem[] topicItems = topicPanel.getTopics();
		ArrayList<String> topicStrings = new ArrayList<String>();
		
		for(int i = 0; i < topicItems.length; i++) {
			String topicString = topicItems[i].getTopic();
			State topicState = topicItems[i].getState();
			
			if(topicState == State.RESEARCHED)
				topicStrings.add(topicString);
		}
		CacheFileHandler.saveCacheFile(CacheFileHandler.DEFAULT_CACHE_PATH, topicStrings.toArray(new String[]{}));
		
		System.exit(0);
	}

	public void addTopic(final String topic) {
		topicPanel.addTopic(topic);
		evernoteWorker.enqueueCommand(
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
				if (topic.getState() == TopicListItem.State.QUEUED_FOR_RESEARCH) {
					evernoteWorker.enqueueCommand(
							new ResearchCommand(
									this,
									ResearchCommand.Type.UNQUEUE_TOPIC,
									topic.getTopic()
							)
					);
				} else {
					evernoteWorker.enqueueCommand(
							new ResearchCommand(
									this,
									ResearchCommand.Type.DELETE_TOPIC,
									topic.getTopic()
							)
					);
				}
			} else {
				displayError("Please wait until the topic finishes out the current operation.");
			}
		}
	}

	public void cancelResearch() {
		evernoteWorker.cancelResearch();
	}

	public void refreshTopics() {
		setGUIEnabled(false);
		loadTopicsFromEvernote();
	}
	
	public void deleteCache() {
		CacheFileHandler.deleteCacheFile(CacheFileHandler.DEFAULT_CACHE_PATH);
		log.info("Cleared topic cache.");
	}

	public void loadTopicsFromFile() {
		final JFileChooser fileChooser = new JFileChooser(".");
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
		evernoteWorker.enqueueCommand(new ResearchCommand(this, ResearchCommand.Type.LOAD_TOPICS, null));
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

	public void onTopicListSupplied(final String[] topics) {
		final TopicListItem[] currentTopics = topicPanel.getTopics();

		for (String topic : topics) {
			boolean found = false;
			for (TopicListItem topicListItem : currentTopics) {
				if (topic.equals(topicListItem.getTopic())) {
					found = true;
				}
			}
			if (!found) {
				topicPanel.addTopic(topic, TopicListItem.State.RESEARCHED);
			}
		}
		for (TopicListItem topicListItem : currentTopics) {
			boolean found = false;
			for (String topic : topics) {
				if (topicListItem.getTopic().equals(topic)) {
					found = true;
				}
			}
			if (!found
					&& topicListItem.getState() != TopicListItem.State.QUEUED_FOR_RESEARCH
					&& topicListItem.getState() != TopicListItem.State.RESEARCHING) {
				topicPanel.removeTopic(topicListItem.getTopic());
			}
		}
		setGUIEnabled(true);
		topicPanel.getAddTopicPanel().requestFocusInWindow();
		log.info("Successfully loaded topic list.");
	}

	public void onUsernameLoaded(final String username) {
		setTitle(getTitle() + " @ " + username);
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
					onTopicListSupplied((String[]) e.getData());
				} else if (e.getType() == ResearchEvent.Type.USERNAME) {
					onUsernameLoaded((String) e.getData());
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
