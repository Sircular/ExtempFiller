package com.chs.extemp;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.chs.extemp.evernote.EvernoteClient;
import com.chs.extemp.google.GoogleClient;
import com.chs.extemp.google.GoogleResults;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;

public class Researcher {
	
	private static final int MINIMUM_DOCUMENTS_PER_TOPIC = 8; // used to determine how many topics to get
	
	private EvernoteClient evClient;
	private Logger logger;
	
	private boolean usable = true;
	
	// Used for timing API requests
	private long rateTimer = 0;
	
	// How long to wait between each api request in milliseconds
	private final int TIMER = 1000;
	
	private Notebook HTMLNotebook;
	
	public Researcher() { 
		logger = ExtempLogger.getLogger();
		
		logger.info("Attempting to initialize Evernote Client...");
		
		try {
			evClient = new EvernoteClient();
			logger.info("Evernote Client initialized successfully.");
		} catch (Exception e) {
			logger.severe("Could not initialize Evernote Client: " + e);
			usable = false;
			e.printStackTrace();
			return;
		}
		initChecks();
	}
	
	private void initChecks() {
		try {
			HTMLNotebook = evClient.getNotebook("Web Notes");
			if(HTMLNotebook == null) {
				logger.info("Creating Web Notes Notebook");
				HTMLNotebook = evClient.createNotebook("Web Notes");
			}
		} catch (Exception e) {
			logger.severe("Error while doing initial checks: " + e.getMessage());
		}
	}
	
	public void researchTopic(String topic) throws Exception{
		// Get the next question

		// Check if the question has already been researched
		logger.info("Searching for tag: " + topic);
		Tag tag = evClient.getTag(topic);

		// If it hasn't, research it
		if(tag == null) {
			// Create the tag for the topic
			logger.info("Topic tag not found.");
			logger.info("Creating tag: " + topic);
			tag = evClient.createTag(topic);

			// Create a list to hold web pages to upload
			LinkedList<GoogleResults.Result> totalSearchResults = new LinkedList<GoogleResults.Result>();

			logger.info("Googling topic: " + topic);

			int currentSearchIndex = 0;
			while(totalSearchResults.size() < MINIMUM_DOCUMENTS_PER_TOPIC)  {
				// Always check the rate timer to make sure we do not overburden the server
				checkRateTimer();

				// Search google for web pages
				GoogleResults currentSearchResults = GoogleClient.search(topic, currentSearchIndex);

				// Check if we are being throttled by google
				if(currentSearchResults == null || currentSearchResults.getResponseData() == null || currentSearchResults.getResponseData().getResults() == null) {
					logger.severe("Google connection has been throttled. Waiting 10 minutes.");
					Thread.sleep(1000*60*10);
					continue;
				}

				// Add results
				for (GoogleResults.Result gResult : currentSearchResults.getResponseData().getResults()) {
					// Ignore the topic listing
					if(gResult.getUrl().contains("http://www.nfhs.org/")) {
						continue;
					}
					logger.info("Found page: " + gResult.getUrl());
					// Ignore non-html files
					if(gResult.getUrl().endsWith(".pdf")) {
						logger.info("Filetype is PDF. Excluding page.");
						continue;
					} else if(gResult.getUrl().endsWith(".ppt")) {
						logger.info("Filetype is PPT. Excluding page.");
						continue;
					} else if(gResult.getUrl().endsWith(".docx")) {
						logger.info("Filetype is DOCX. Excluding page.");
						continue;
					} else if(gResult.getUrl().endsWith(".doc")) {
						logger.info("Filetype is DOC. Excluding page.");
						continue;
					} else if(gResult.getUrl().endsWith(".rtf")) {
						logger.info("Filetype is RTF. Excluding page.");
						continue;
					}
					totalSearchResults.add(gResult);
				}

				// Get a new set of data on next search
				currentSearchIndex += 7;
			}

			try {
				for(GoogleResults.Result result : totalSearchResults) {
					// Always check the rate timer to make sure we do not overburden the server
					logger.info("Uploading page: " + result.getUrl());
					checkRateTimer();
					evClient.createHTMLNote(result.getUrl(), HTMLNotebook, Arrays.asList(tag));
				}
			} catch (Exception e) {
				logger.severe("Skipping source");
				e.printStackTrace();
			}
			logger.info("Finished researching topic.");
		} else {
			logger.info("Tag found. Continuing...");
		}
	}
	
	public List<Tag> getCurrentTags() {
		try {
			return evClient.getTags();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// check to see if it initialized successfully
	public boolean isUsable() {
		return usable;
	}
	
	private void checkRateTimer() throws InterruptedException {
		if(Calendar.getInstance().getTimeInMillis() < rateTimer + TIMER) {
			Thread.sleep(rateTimer + TIMER - Calendar.getInstance().getTimeInMillis());
		}
		rateTimer = Calendar.getInstance().getTimeInMillis();
	}

}
