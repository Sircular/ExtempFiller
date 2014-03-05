package com.chs.extemp.cli;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.TopicFileReader;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CLI {
	private Logger logger;
	private Researcher researcher;

	public CLI(String topicsFilePath) {
		//Initialize logger 
		logger = ExtempLogger.getLogger();
		logger.addHandler(new CLILogHandler());
		logger.info("Starting CLI using questions file: " + topicsFilePath);

		//Start research
		try {
			researcher = new Researcher();
			doResearch(topicsFilePath);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	private void doResearch(String topicsFilePath) {
		for (String topic : TopicFileReader.readTopicFile(topicsFilePath)) {
			try {
				researcher.researchTopic(topic);
			} catch (Exception e) {
				logger.severe("Error researching topic \"" + topic + "\": " + e.toString());
			}
		}
	}
}
