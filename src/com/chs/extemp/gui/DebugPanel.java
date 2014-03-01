package com.chs.extemp.gui;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class DebugPanel extends JPanel{
	
	public DebugPanel(int width, int height) {
		init(width, height);
	}
	
	private void init(int width, int height) {
		// set up some basic aesthetic stuff
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// set up the layout manager
		this.setLayout(new BorderLayout());
		
		// add debug log
		JTextPane log = new JTextPane();
		initLogger(log, width, height);
		add(log, BorderLayout.CENTER);
		
	}
	
	private void initLogger(JTextPane log, int width, int height) {
		// set up the log panel
		log.setEditable(false);
		
		// set up the log handler
		Logger logger = Logger.getLogger("CHS-Extemp");
		DebugHandler handler = new DebugHandler(log);
		logger.addHandler(handler);
		
	}
}
