package com.chs.extemp.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import com.chs.extemp.DataReader;
import com.chs.extemp.ExtempLogger;
import com.chs.extemp.gui.debug.DebugPanel;
import com.chs.extemp.gui.events.ResearchCommand;
import com.chs.extemp.gui.events.ResearchEvent;
import com.chs.extemp.gui.events.SettingsEvent;
import com.chs.extemp.gui.topicview.TopicListItem;
import com.chs.extemp.gui.topicview.TopicListItem.State;
import com.chs.extemp.gui.topicview.TopicPanel;

@SuppressWarnings("serial")

public class ResearchGUI extends JFrame implements ResearchListener {
	public static final int GUI_WIDTH = 800;
	public static final int GUI_HEIGHT = 600;
	
	public static final String VERSION = "0.3b";

	private final EvernoteWorker evernoteWorker;

	private TopicPanel topicPanel;
	private DebugPanel debugPanel;
	private PrintPanel printPanel;
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
		String authToken = "";

		int useDefaultToken = JOptionPane.NO_OPTION;
		
		if (new File(DataReader.DEFAULT_DEV_KEY_PATH).exists()) // if it doesn't exist, we need to force the user to enter one
			useDefaultToken = JOptionPane.showConfirmDialog(this, "Use the default Evernote account?", "Extemp Filler",
					JOptionPane.YES_NO_OPTION);

		if (useDefaultToken == JOptionPane.NO_OPTION) {
			authToken = JOptionPane.showInputDialog(this, "Please enter your account auth token.", authToken);
			DataReader.saveDevKey(DataReader.DEFAULT_DEV_KEY_PATH, authToken);
		} else
			authToken = DataReader.loadDevKey(DataReader.DEFAULT_DEV_KEY_PATH);
		
		if (authToken == null || authToken.equals("")) {
			displayError("No auth token entered.\nPlease enter a valid auth token and try again.", true);
		}
		
		// load the evernote client
		evernoteWorker = new EvernoteWorker(authToken);
		evernoteWorker.registerListener(this);
		evernoteWorker.startWorkerThreads();
		
		if (evernoteWorker.workerThreadsStarted()) {
			if (new File(DataReader.DEFAULT_CACHE_PATH).exists()) {
				log.info("Loading topic list from cache file...");
				onTopicListSupplied(DataReader.loadCacheFile(DataReader.DEFAULT_CACHE_PATH));
			} else {
				log.info("No cache file found, requesting topics from Evernote...");
				loadTopicsFromEvernote();
			}
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
		printPanel = new PrintPanel(this);
		menuBar = new ResearchMenu(this);
		waitingBar = new JProgressBar();

		tabs.addTab("Topics", topicPanel);
		tabs.addTab("Print", printPanel);
		tabs.addTab("Debug", debugPanel);
		
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (((JTabbedPane) (e.getSource())).getSelectedIndex() == 0)
					topicPanel.getAddTopicPanel().requestFocusInWindow();
				else if (((JTabbedPane) (e.getSource())).getSelectedIndex() == 1)
					printPanel.syncLists();
			}
		});
		
		// quick and dirty handler for keyboard shortcuts
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(new KeyEventDispatcher() {

				@Override
				public boolean dispatchKeyEvent(KeyEvent e) {
					if (e.getID() == KeyEvent.KEY_PRESSED && 
							e.getKeyCode() == KeyEvent.VK_F1 && 
							!e.isConsumed()) {
						showHelp();
						e.consume();
					}
					return false;
				}
				
			});;
		
		add(tabs, BorderLayout.CENTER);

		setJMenuBar(menuBar);

		setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
	}

	@Override
	public void dispose() {
		
		if (evernoteWorker != null)
			evernoteWorker.interruptWorkerThreads();

		// save the cache
		final List<TopicListItem> topicItems = topicPanel.getTopics();
		if (topicItems != null && topicItems.size() > 0) {
			final ArrayList<String> topicStrings = new ArrayList<String>();
	
			for (final TopicListItem topicItem : topicItems) {
				final String topicString = topicItem.getTopic();
				final State topicState = topicItem.getState();
	
				if (topicState == State.RESEARCHED || topicState == State.RESEARCHING)
					topicStrings.add(topicString);
			}
			DataReader.saveCacheFile(DataReader.DEFAULT_CACHE_PATH, topicStrings.toArray(new String[]{}));
		}

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
				displayError("Please wait until the topic finishes out the current operation.", false);
	}

	public void cancelResearch() {
		evernoteWorker.cancelResearch();
	}

	public void refreshTopics() {
		setGUIEnabled(false);
		loadTopicsFromEvernote();
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
	
	public void showAbout() {
		JOptionPane.showMessageDialog(this, "Extemp Filler\nVersion "+VERSION+"\nLogan Lembke, Walt Mays 2014",
				"About Extemp Filler", JOptionPane.PLAIN_MESSAGE);
	}
	
	public void showHelp() {
		// display the manual (in the form of a PDF)
		// to do this, we have to actually copy the file to the filesystem,
		// then open it
		try {
			final File dstFile = new File(DataReader.MANUAL_PATH);
			if(!dstFile.exists()) { // we have to copy it
				InputStream readStream = this.getClass().getResourceAsStream("/com/chs/extemp/data/Manual.pdf");
				FileOutputStream writeStream = new FileOutputStream(dstFile);
				
				byte[] byteBuffer = new byte[1024*1024]; // 1mb should be a good buffer size
				int numRead = 0;
				
				do {
					numRead = readStream.read(byteBuffer);
					writeStream.write(byteBuffer, 0, numRead);
				} while(numRead >= 0);
				
				writeStream.flush();
				
				writeStream.close();
				readStream.close();
			}
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(dstFile);
				log.info("Opened manual.");
			} else {
				log.severe("Error opening manual; open the file "+dstFile.getAbsolutePath());
			}
		} catch (IOException e) {
			log.severe("Error copying manual.");
			log.severe(e.getMessage());
		}
	}
	
	// The following functions are all handlers for individual research events

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
		List<TopicListItem> currentTopics = topicPanel.getTopics();
		if (currentTopics == null) // WHY?
			currentTopics = new ArrayList<TopicListItem>();

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
	
	public List<TopicListItem> getCurrentTopicList() {
		return topicPanel.getTopics();
	}

	public void onUsernameLoaded(final String username) {
		setTitle(getTitle() + " @ " + username);
	}

	public void onTopicError(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCH_ERROR);
		displayError("Error while editing topic: " + topic +
				". Please see debug log for details.", false);
	}

	public void onEvernoteConnectionError(Exception e) {
		String msg = "An unknown Evernote connection error occured. See debug log for details.";
		if(e == null) {
			log.info("Evernote Connection Error: Unknown");
		} else {
			String data = e.toString();
			log.info("Evernote Connection Error: "+data);
			if (data.contains("UnknownHostException")) {
				msg = "You are not connected to the internet.\n" +
						"Please close the program, check your connection, and try again.";
			} else if (data.contains("EDAMUserException") && data.contains("parameter:authenticationToken")) {
				msg = "You have entered an invalid authentication token.\n" +
						"Please close the program, double-check your authentication token,\n" +
						"and try again.";
				// we want to get rid of the bad auth token
				DataReader.deleteAuthTokenFile(DataReader.DEFAULT_DEV_KEY_PATH);
			}
		}
		displayError(msg, true); // we do want it to close afterwards
	}

	public void displayError(final String error, final boolean terminal) {
		// displays an error message in
		// a message box
		JOptionPane.showMessageDialog(this, error, "ExtempFiller", JOptionPane.ERROR_MESSAGE);
		if (terminal)
			this.dispose();
	}

	public void handleResearchEvent(final ResearchEvent e) {
		// YAY CONCURRENCY!
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
					onEvernoteConnectionError((Exception)e.getData());
			}
		});
	}
	
	// the following functions are all handlers for individual settings events
	
	private void onArticleCountSet(int num) {
		evernoteWorker.setMaxArticleCount(num);
	}
	
	public void handleSettingsEvent(final SettingsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				if (e.getType() == SettingsEvent.Type.MAX_SOURCES_SET)
					onArticleCountSet((Integer)e.getData());
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
