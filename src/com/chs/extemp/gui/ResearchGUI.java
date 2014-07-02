package com.chs.extemp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.DataReader;
import com.chs.extemp.auth.AuthTokens;
import com.chs.extemp.gui.debug.DebugPanel;
import com.chs.extemp.gui.events.ResearchCommand;
import com.chs.extemp.gui.events.ResearchEvent;
import com.chs.extemp.gui.topicview.TopicListItem;
import com.chs.extemp.gui.topicview.TopicListItem.State;
import com.chs.extemp.gui.topicview.TopicPanel;

@SuppressWarnings("serial")

public class ResearchGUI extends JFrame implements ResearchListener {
	public static final int GUI_WIDTH = 800;
	public static final int GUI_HEIGHT = 600;

	private final EvernoteWorker evernoteWorker;

	private TopicPanel topicPanel;
	private DebugPanel debugPanel;
	private ResearchMenu menuBar;
	private JProgressBar waitingBar;

	private final Logger log;

	public ResearchGUI() {
		// load the logger
		log = ExtempLogger.getLogger();

		// initialize GUI
		init();
		pack();
		setGUIEnabled(false);
		setVisible(true);

		// choose with auth token to use
		String auth_token = AuthTokens.DEFAULT_EVERNOTE_DEV_TOKEN;

		final int useDefaultToken = JOptionPane.showConfirmDialog(this, "Use the default Evernote account?", "Extemp Filler",
				JOptionPane.YES_NO_OPTION);

		if (useDefaultToken == JOptionPane.NO_OPTION)
			auth_token = JOptionPane.showInputDialog(this, "Please enter your custom auth token.", auth_token);

		// load the evernote client
		evernoteWorker = new EvernoteWorker(auth_token);
		evernoteWorker.registerListener(this);
		evernoteWorker.startWorkerThreads();

		if (DataReader.cacheFileExists(DataReader.DEFAULT_CACHE_PATH)) {
			log.info("Loading topic list from cache file...");
			onTopicListSupplied(DataReader.loadCacheFile(DataReader.DEFAULT_CACHE_PATH));
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
		menuBar = new ResearchMenu(this);
		waitingBar = new JProgressBar();

		tabs.addTab("Topics", topicPanel);
		tabs.addTab("Debug", debugPanel);
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (((JTabbedPane) (e.getSource())).getSelectedIndex() == 0)
					topicPanel.getAddTopicPanel().requestFocusInWindow();
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
		final TopicListItem[] topicItems = topicPanel.getTopics();
		final ArrayList<String> topicStrings = new ArrayList<String>();

		for (final TopicListItem topicItem : topicItems) {
			final String topicString = topicItem.getTopic();
			final State topicState = topicItem.getState();

			if (topicState == State.RESEARCHED || topicState == State.RESEARCHING)
				topicStrings.add(topicString);
		}
		DataReader.saveCacheFile(DataReader.DEFAULT_CACHE_PATH, topicStrings.toArray(new String[]{}));

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
		for (final TopicListItem topic : topics)
			if (topic.getState() != TopicListItem.State.RESEARCHING && topic.getState() != TopicListItem.State.DELETING) {
				if (topic.getState() == TopicListItem.State.QUEUED_FOR_RESEARCH)
					evernoteWorker.enqueueCommand(
							new ResearchCommand(
									this,
									ResearchCommand.Type.UNQUEUE_TOPIC,
									topic.getTopic()
									)
							);
				else
					evernoteWorker.enqueueCommand(
							new ResearchCommand(
									this,
									ResearchCommand.Type.DELETE_TOPIC,
									topic.getTopic()
									)
							);
			} else
				displayError("Please wait until the topic finishes out the current operation.");
	}

	public void cancelResearch() {
		evernoteWorker.cancelResearch();
	}

	public void refreshTopics() {
		setGUIEnabled(false);
		loadTopicsFromEvernote();
	}

	public void deleteCache() {
		DataReader.deleteCacheFile(DataReader.DEFAULT_CACHE_PATH);
		log.info("Cleared topic cache.");
	}

	public void loadTopicsFromFile() {
		final JFileChooser fileChooser = new JFileChooser(".");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (.txt)", "txt", "text"));

		final int choice = fileChooser.showOpenDialog(null);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			final String path = file.getAbsolutePath();
			for (final String currentTopic : DataReader.readTopicFile(path))
				if (!topicPanel.hasTopic(currentTopic))
					addTopic(currentTopic);
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

		for (final String topic : topics) {
			boolean found = false;
			for (final TopicListItem topicListItem : currentTopics)
				if (topic.equals(topicListItem.getTopic()))
					found = true;
			if (!found)
				topicPanel.addTopic(topic, TopicListItem.State.RESEARCHED);
		}
		for (final TopicListItem topicListItem : currentTopics) {
			boolean found = false;
			for (final String topic : topics)
				if (topicListItem.getTopic().equals(topic))
					found = true;
			if (!found
					&& topicListItem.getState() != TopicListItem.State.QUEUED_FOR_RESEARCH
					&& topicListItem.getState() != TopicListItem.State.RESEARCHING)
				topicPanel.removeTopic(topicListItem.getTopic());
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
				if (e.getType() == ResearchEvent.Type.TOPIC_QUEUED_FOR_RESEARCH)
					onTopicQueuedForResearch((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHING)
					onTopicResearching((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHED)
					onTopicResearched((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.TOPIC_DELETING)
					onTopicDeleting((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.TOPIC_DELETED)
					onTopicDeleted((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.TOPIC_LIST_LOADED)
					onTopicListSupplied((String[]) e.getData());
				else if (e.getType() == ResearchEvent.Type.USERNAME)
					onUsernameLoaded((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.RESEARCH_ERROR)
					onTopicError((String) e.getData());
				else if (e.getType() == ResearchEvent.Type.EVERNOTE_CONNECTION_ERROR)
					onEvernoteError();
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
