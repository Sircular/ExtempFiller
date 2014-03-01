package com.chs.extemp;

import java.util.logging.Logger;

import com.chs.extemp.evernote.EvernoteClient;
import com.evernote.edam.type.Notebook;

public class Researcher {
	
	private EvernoteClient evClient;
	private Logger logger;
	
	private boolean usable = true;
	
	public Researcher() { 
		logger = ExtempLogger.getLogger();
		
		logger.info("Attempting to initialize Evernote Client...");
		
		try {
			evClient = new EvernoteClient();
			logger.info("Evernote Client initialized successfully.");
		} catch (Exception e) {
			logger.severe("Could not initialize Evernote Client: " + e.getMessage());
			usable = false;
			e.printStackTrace();
			return;
		}
		initChecks();
	}
	
	private void initChecks() {
		try {
			Notebook HTMLNotebook = evClient.getNotebook("Web Notes");
			if(HTMLNotebook == null) {
				logger.info("Creating Web Notes Notebook");
				HTMLNotebook = evClient.createNotebook("Web Notes");
			}
		} catch (Exception e) {
			logger.severe("Error while doing initial checks: " + e.getMessage());
		}
	}
	
	// check to see if it initialized successfully
	public boolean isUsable() {
		return usable;
	}

}
