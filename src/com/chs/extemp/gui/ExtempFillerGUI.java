package com.chs.extemp.gui;

import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.gui.messaging.MessageEvent;
import com.chs.extemp.gui.messaging.MessageEventListener;

@SuppressWarnings("serial")

public class ExtempFillerGUI extends JFrame{
	public static final int GUI_WIDTH  = 800;
	public static final int GUI_HEIGHT = 600;
	
	public ResearchWorker researchWorker;
	private GUIMessageListener listener;
	private Logger logger;
	
	public ExtempFillerGUI() {
		init();
		pack();
		setVisible(true);
		
		logger = ExtempLogger.getLogger();
		logger.info("Initialied GUI.");
		
		researchWorker.enqueue("Here is my topic.");
	}	
	
	public void init() {
		// initialize research worker
		researchWorker = new ResearchWorker();
		Thread researchThread = new Thread(researchWorker);
		researchThread.start();
		
		listener = new GUIMessageListener(this);
		researchWorker.addListener(listener);	
		
		// Tries to make the app look native
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			logger.info(e.toString());
		} catch (InstantiationException e) {
			logger.info(e.toString());
		} catch (IllegalAccessException e) {
			logger.info(e.toString());
		} catch (UnsupportedLookAndFeelException e) {
			logger.info(e.toString());
		}
		
		setTitle("CHS Extemp Filler");
		
		// set up some tabs
		JTabbedPane tabs = new JTabbedPane();
		
		TopicPanel topicpanel = new TopicPanel();
		DebugPanel debugpanel = new DebugPanel();
		
		tabs.addTab("Topics", topicpanel);
		tabs.addTab("Debug", debugpanel);
		add(tabs);
	}
	
	public void topicResearched(String topic) {
		// more code
	}
	
	public class GUIMessageListener implements MessageEventListener {
		
		private ExtempFillerGUI gui;
		
		public GUIMessageListener(ExtempFillerGUI gui) {
			this.gui = gui;
		}

		@Override
		public void handleMessageEvent(MessageEvent e) {
			if(e.getType() == MessageEvent.Type.TOPIC_RESEARCHED) {
				gui.topicResearched((String)e.getData());
			}
		}
		
	}
}
