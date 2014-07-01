package com.chs.extemp.cli;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.TopicFileReader;
import com.chs.extemp.evernote.util.AuthTokens;

public class CLI {
	private final Logger logger;
	private Researcher researcher;

	public CLI(String topicsFilePath) {
		//Initialize logger
		logger = ExtempLogger.getLogger();
		logger.addHandler(new CLILogHandler());
		logger.info("Starting CLI using questions file: " + topicsFilePath);

		// select the auth token (unfinished)
		logger.config("Do you want to use the default Evernote account? [y/N]");

		//Start research
		try {
			// will soon implement option for custom auth token
			researcher = new Researcher(AuthTokens.DEFAULT_EVERNOTE_AUTH_TOKEN);
			doResearch(topicsFilePath);
		} catch (final Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	private void doResearch(String topicsFilePath) {
		for (final String topic : TopicFileReader.readTopicFile(topicsFilePath))
			try {
				researcher.researchTopic(topic);
			} catch (final Exception e) {
				logger.severe("Error researching topic \"" + topic + "\": " + e.toString());
			}
	}
}
