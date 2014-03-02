package com.chs.extemp.gui.debug;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JTextPane;

// Class used to connect the debug output with the logger

public class DebugLogHandler extends Handler{
	
	private JTextPane debuglog;
	
	public DebugLogHandler(JTextPane debuglog) {
		this.debuglog = debuglog;
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		debuglog.repaint();
		
	}

	@Override
	public void publish(LogRecord arg0) {
		Level level    = arg0.getLevel();
		String message = arg0.getMessage();
		String old_log = debuglog.getText();
		debuglog.setText(old_log + "[" + level.getName() + "] " + message+"\n");
		debuglog.setCaretPosition(debuglog.getText().length());
	}

}
