package com.chs.extemp.cli;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CLILogHandler extends Handler{

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord arg0) {
		Level level    = arg0.getLevel();
		String message = arg0.getMessage();
		
		if(level == Level.WARNING || level == Level.SEVERE) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
		
	}

}
