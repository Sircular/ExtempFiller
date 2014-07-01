package com.chs.extemp.gui.debug;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JTextPane;

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
	public synchronized void flush() {
		debuglog.validate();
		debuglog.repaint();
	}

	@Override
	public synchronized void publish(LogRecord record) {
		debuglog.setText(debuglog.getText() + "[" + record.getLevel().getName() + "] " + record.getMessage() + "\n");
		debuglog.setCaretPosition(debuglog.getText().length());
		if (record.getThrown() != null) {
			final StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
			for (final StackTraceElement element : stackTrace)
				publish(new LogRecord(Level.SEVERE, element.toString()));
		}
		flush();
	}
}
