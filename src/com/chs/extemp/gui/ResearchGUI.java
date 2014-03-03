package com.chs.extemp.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.TopicFileReader;
import com.chs.extemp.gui.debug.DebugPanel;
import com.chs.extemp.gui.menu.ResearchMenuBar;
import com.chs.extemp.gui.messaging.ResearchMessage;
import com.chs.extemp.gui.messaging.ResearchMessageListener;
import com.chs.extemp.gui.topicview.TopicListItem;
import com.chs.extemp.gui.topicview.TopicPanel;

@SuppressWarnings("serial")

public class ResearchGUI extends JFrame{
	public static final int GUI_WIDTH  = 800;
	public static final int GUI_HEIGHT = 600;
	
	private ResearchWorker researchWorker;
	private GUIMessageListener listener;
	private Logger logger;
	
	private JPanel mainContainer;
	private TopicPanel topicPanel;
	private DebugPanel debugPanel;
	private ResearchMenuBar menuBar;
	private JProgressBar waitingBar;
	
	public ResearchGUI() {
		init();
		pack();
		setVisible(true);
		
		logger = ExtempLogger.getLogger();
		logger.info("Initialized GUI.");
		
	}	
	
	public void init() {
		// initialize research worker
		researchWorker = new ResearchWorker();
		Thread researchThread = new Thread(researchWorker);
		researchThread.start();
		
		listener = new GUIMessageListener(this);
		researchWorker.addListener(listener);
		
		setTitle("CHS Extemp Filler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		// set up some tabs
		JTabbedPane tabs = new JTabbedPane();
		
		mainContainer = new JPanel();
		topicPanel = new TopicPanel(this);
		debugPanel = new DebugPanel();
		menuBar = new ResearchMenuBar(this);
		waitingBar = new JProgressBar();
		
		mainContainer.setLayout(new BorderLayout());
		
		tabs.addTab("Topics", topicPanel);
		tabs.addTab("Debug", debugPanel);
		mainContainer.add(tabs);
		
		add(mainContainer, BorderLayout.CENTER);
		add(waitingBar, BorderLayout.PAGE_END);
		
		waitingBar.setIndeterminate(true);
		
		setJMenuBar(menuBar);
		
		topicPanel.setContentsEnabled(false);
		menuBar.setContentsEnabled(false);
		
	}
	
	@Override
	public void dispose() {
		
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
		
		JOptionPane.showMessageDialog(null, "Deletion of a topic through the client is not yet implemented.\n\n"+
				"Please go to evernote.com, sign into the web interface, and delete " +
				"the data manually.");
		
	}
	
	public void loadTopicsFromFile() {
		JFileChooser fileChooser = new JFileChooser();
		
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Text Files", "txt", "text");
		fileChooser.setFileFilter(fileFilter);
		
		int choice = fileChooser.showOpenDialog(null);
		if(choice == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String path = file.getAbsolutePath();
			String[] newTopics = TopicFileReader.readTopicFile(path);
			for(int i = 0; i < newTopics.length; i++) {
				String currentTopic = newTopics[i];
				System.out.println(topicPanel.hasTopic(currentTopic));
				if(!topicPanel.hasTopic(currentTopic)) {
					topicPanel.addTopic(currentTopic);
					researchWorker.enqueueTopic(currentTopic);
				}
			}
		}
	}
	
	public void onTopicResearched(String topic) {
		// more code
		logger.info("Finished researching topic: " + topic);
		topicPanel.setTopicState(topic, TopicListItem.State.RESEARCHED);
	}
	
	public void onRemoteTopicListLoaded(String[] topics) {
		// used to populate the list of
		// already-researched topics
		remove(waitingBar);
		topicPanel.setContentsEnabled(true);
		menuBar.setContentsEnabled(true);
		System.out.println(topics);
		for(int i = 0; i < topics.length; i++) {
			topicPanel.addTopic(topics[i], TopicListItem.State.RESEARCHED);
		}
		pack();
		repaint();
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
	
	public class GUIMessageListener implements ResearchMessageListener {
		
		private ResearchGUI gui;
		
		public GUIMessageListener(ResearchGUI gui) {
			this.gui = gui;
		}

		@Override
		public void handleMessageEvent(ResearchMessage e) {
			if(e.getType() == ResearchMessage.Type.TOPIC_RESEARCHED) {
				gui.onTopicResearched((String)e.getData());
			}else if(e.getType() == ResearchMessage.Type.TOPIC_LIST) {
				gui.onRemoteTopicListLoaded((String[])e.getData());
			}else if(e.getType() == ResearchMessage.Type.RESEARCH_ERROR) {
				gui.onTopicError((String)e.getData());
			}else if(e.getType() == ResearchMessage.Type.EVERNOTE_CONNECTION_ERROR) {
				gui.onEvernoteError();
			}
		}
		
	}
}
