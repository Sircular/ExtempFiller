package com.chs.extemp;

import com.chs.extemp.evernote.EvernoteClient;
import com.chs.extemp.google.GoogleWebClient;
import com.chs.extemp.google.GoogleResults;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Researcher {
	
	// DuckDuckGo supplies 28 results; we don't need all of these
	private final int MAXIMUM_ARTICLE_COUNT = 12;
	
	private final EvernoteClient evernoteClient;
	private final Logger logger;

	// Used for timing API requests
	private long rateTimer = 0;

	// How long to wait between each api request in milliseconds
	private static final int TIMER = 1000;

	private Notebook HTMLNotebook;

	public Researcher() throws Exception {
		logger = ExtempLogger.getLogger();
		logger.info("Attempting to initialize Evernote Client...");

		try {
			evernoteClient = new EvernoteClient();
			logger.info("Evernote Client initialized successfully.");
		} catch (final Exception e) {
			logger.severe("Could not initialize Evernote Client: " + e);
			throw e;
		}
		initHTMLNotebook();
	}

	private void initHTMLNotebook() throws Exception {
		try {
			HTMLNotebook = evernoteClient.getNotebook("Web Notes");
			if (HTMLNotebook == null) {
				logger.info("Creating Web Notes Notebook");
				HTMLNotebook = evernoteClient.createNotebook("Web Notes");
			}
		} catch (final Exception e) {
			logger.severe("Error while creating the Web Notes Notebook: " + e.getMessage());
			throw e;
		}
	}

	public void researchTopic(final String topic) throws Exception {
		// Check if the question has already been researched
		logger.info("Searching for tag: " + topic);
		Tag tag = evernoteClient.getTag(topic);

		// If it hasn't, research it
		if (tag == null) {

			// Create the tag for the topic
			logger.info("Topic tag not found.");
			logger.info("Creating tag: " + topic);
			tag = evernoteClient.createTag(topic);
			int topicCount = 0;
			
			// Create a list to hold web pages to upload
			LinkedList<GoogleResults.GoogleResult> totalSearchResults;
			logger.info("Googling topic: " + topic);
			checkRateTimer();
			totalSearchResults = googleTopic(topic);

			// Get a new set of data on next search
			for (GoogleResults.GoogleResult result : totalSearchResults) {
				try {
					logger.info("Uploading page: " + result.getUrl());
	
					// Always check the rate timer to make sure we do not overburden the server
					checkRateTimer();
					evernoteClient.createHTMLNote(result.getUrl(), HTMLNotebook, Arrays.asList(tag));
					topicCount++;
				} catch (final Exception e) {
					logger.log(Level.SEVERE, "Skipping source", e);
				}
				if(topicCount >= MAXIMUM_ARTICLE_COUNT)
					break;
			}
			
			logger.info("Found "+String.valueOf(topicCount)+" sources.");
			logger.info("Finished researching topic: " + topic);

		} else {
			logger.info("Tag found. Continuing...");
		}
	}

	private LinkedList<GoogleResults.GoogleResult> googleTopic(final String topic) throws Exception {
		final LinkedList<GoogleResults.GoogleResult> results = new LinkedList<GoogleResults.GoogleResult>();

		// Search google for web pages
		final GoogleResults googleResults = GoogleWebClient.search(topic);

		// Check if we are being throttled by google
		if (googleResults == null || googleResults.getResponseData() == null || googleResults.getResponseData().getResults() == null) {
			logger.severe("Google connection has been throttled. Waiting 10 minutes.");
			Thread.sleep(1000 * 60 * 10);
			return results;
		}

		// Add results
		for (GoogleResults.GoogleResult gResult : googleResults.getResponseData().getResults()) {
			// Ignore the topic listing
			if (gResult.getUrl().contains("http://www.nfhs.org/")) {
				continue;
			}
			logger.info("Found page: " + gResult.getUrl());
			// Ignore non-html files
			if (gResult.getUrl().endsWith(".pdf")) {
				logger.info("Filetype is PDF. Excluding page.");
				continue;
			} else if (gResult.getUrl().endsWith(".ppt")) {
				logger.info("Filetype is PPT. Excluding page.");
				continue;
			} else if (gResult.getUrl().endsWith(".docx")) {
				logger.info("Filetype is DOCX. Excluding page.");
				continue;
			} else if (gResult.getUrl().endsWith(".doc")) {
				logger.info("Filetype is DOC. Excluding page.");
				continue;
			} else if (gResult.getUrl().endsWith(".rtf")) {
				logger.info("Filetype is RTF. Excluding page.");
				continue;
			}
			results.add(gResult);
		}
		return results;
	}

	private void checkRateTimer() throws InterruptedException {
		if (Calendar.getInstance().getTimeInMillis() < rateTimer + TIMER) {
			Thread.sleep(rateTimer + TIMER - Calendar.getInstance().getTimeInMillis());
		}
		rateTimer = Calendar.getInstance().getTimeInMillis();
	}

	public EvernoteClient getEvernoteClient() {
		return evernoteClient;
	}
}
