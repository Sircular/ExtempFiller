package com.chs.extemp.gui.debug;

import com.chs.extemp.ExtempLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class DebugPanel extends JPanel {

	public DebugPanel() {
		init();
	}

	private void init() {
		// set up some basic aesthetic stuff
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// set up the layout manager
		this.setLayout(new BorderLayout());

		// add debug log
		JTextPane log = new JTextPane();
		initLogger(log);
		JScrollPane scroll = new JScrollPane(log);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll, BorderLayout.CENTER);

	}

	private void initLogger(JTextPane log) {
		// set up the log panel
		log.setEditable(false);

		// set up the log handler
		Logger logger = ExtempLogger.getLogger();
		DebugLogHandler handler = new DebugLogHandler(log);
		logger.addHandler(handler);
	}
}
