package com.chs.extemp.gui;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.TopicFileReader;
import com.chs.extemp.gui.debug.DebugPanel;
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
	private Thread researchWorkerThread;
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
		researchWorkerThread = new Thread(researchWorker);

		// initialize GUI
		init();
		pack();

		researchWorkerThread.start();
		setVisible(true);
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
				if(((JTabbedPane)(e.getSource())).getSelectedIndex() == 0) {
					topicPanel.getAddTopicPanel().requestFocusInWindow();
				}
			}
		});

		add(tabs, BorderLayout.CENTER);
		add(waitingBar, BorderLayout.PAGE_END);

		waitingBar.setIndeterminate(true);

		setJMenuBar(menuBar);

		topicPanel.setContentsEnabled(false);
		menuBar.setContentsEnabled(false);

		setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));

	}

	@Override
	public void dispose() {
		researchWorkerThread.interrupt();
		System.exit(0);
	}

	public void addTopic(String topic) {
		topicPanel.addTopic(topic);
		researchWorker.enqueueTopic(topic);
	}

	public void removeSelectedTopic() {
		// as yet unimplemented on the server side,
		// so not implemented on the client side.
		// Currently displays a message box saying
		// as much.

		JOptionPane.showMessageDialog(null, "Deletion of a topic through the client is not yet implemented.\n\n" +
				"Please go to evernote.com, sign into the web interface, and delete " +
				"the data manually.");
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
					researchWorker.enqueueTopic(currentTopic);
				}
			}
		}
	}

	public void onTopicResearching(String topic) {
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHING);
	}

	public void onTopicResearched(String topic) {
		// more code
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHED);
	}

	public void onRemoteTopicListLoaded(String[] topics) {
		// used to populate the list of
		// already-researched topics
		remove(waitingBar);
		menuBar.setContentsEnabled(true);
		topicPanel.setContentsEnabled(true);
		topicPanel.getAddTopicPanel().requestFocusInWindow();
		revalidate();
		repaint();
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
		JOptionPane.showMessageDialog(null, error);
	}

	public void handleMessageEvent(ResearchEvent e) {
		if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHING) {
			onTopicResearching((String) e.getData());
		} else if (e.getType() == ResearchEvent.Type.TOPIC_RESEARCHED) {
			onTopicResearched((String) e.getData());
		} else if (e.getType() == ResearchEvent.Type.TOPIC_LIST) {
			onRemoteTopicListLoaded((String[]) e.getData());
		} else if (e.getType() == ResearchEvent.Type.RESEARCH_ERROR) {
			onTopicError((String) e.getData());
		} else if (e.getType() == ResearchEvent.Type.EVERNOTE_CONNECTION_ERROR) {
			onEvernoteError();
		}
	}
}
