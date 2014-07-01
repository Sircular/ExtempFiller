package com.chs.extemp.gui.debug;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import com.chs.extemp.ExtempLogger;

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
		final JTextPane log = new JTextPane();
		initLogger(log);
		final JScrollPane scroll = new JScrollPane(log);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll, BorderLayout.CENTER);
	}

	private void initLogger(final JTextPane log) {
		// set up the log panel
		log.setEditable(false);

		// set up the log handler
		final DebugLogHandler handler = new DebugLogHandler(log);
		ExtempLogger.getLogger().addHandler(handler);
	}
}
