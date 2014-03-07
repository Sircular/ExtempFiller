package com.chs.extemp.gui.debug;

import javax.swing.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Class used to connect the debug output with the logger

public class DebugLogHandler extends Handler {
	private final JTextPane debuglog;

	public DebugLogHandler(JTextPane debuglog) {
		this.debuglog = debuglog;
	}

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
		debuglog.validate();
		debuglog.repaint();
	}

	@Override
	public void publish(LogRecord arg0) {
		final Level level = arg0.getLevel();
		final String message = arg0.getMessage();
		final String old_log = debuglog.getText();
		debuglog.setText(old_log + "[" + level.getName() + "] " + message + "\n");
		debuglog.setCaretPosition(debuglog.getText().length());
		flush();
	}
}
