package com.chs.extemp.gui;

import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.chs.extemp.ExtempLogger;

@SuppressWarnings("serial")

public class ExtempFillerGUI extends JFrame{
	public static final int GUI_WIDTH  = 800;
	public static final int GUI_HEIGHT = 600;
	
	public ResearchWorker researchWorker;
	private Logger logger;
	
	public ExtempFillerGUI() {
		init();
		pack();
		setVisible(true);
		
		logger = ExtempLogger.getLogger();
		logger.info("Initialied GUI.");
	}	
	
	public void init() {
		// initialize research worker
		researchWorker = new ResearchWorker();
		Thread researchThread = new Thread(researchWorker);
		researchThread.start();
		
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
}
