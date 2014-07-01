package com.chs.extemp.cli;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CLILogHandler extends Handler {

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		final Level level = record.getLevel();
		final String message = record.getMessage();

		if (level == Level.WARNING || level == Level.SEVERE)
			System.err.println(message);
		else
			System.out.println(message);

		if (record.getThrown() != null) {
			final StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
			for (final StackTraceElement element : stackTrace)
				publish(new LogRecord(Level.SEVERE, element.toString()));
		}
	}
}
