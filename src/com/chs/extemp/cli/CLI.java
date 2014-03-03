package com.chs.extemp.cli;

import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.TopicFileReader;

public class CLI {
	private Logger logger;
	private Researcher researcher;
	
	public CLI(String topicsFilePath) {
		//Initialize logger 
		logger = ExtempLogger.getLogger();
		logger.addHandler(new CLILogHandler());
		logger.info("Starting CLI using questions file: " + topicsFilePath);
		
		//Start research
		researcher = new Researcher();
		doResearch(topicsFilePath);
	}

	private void doResearch(String topicsFilePath) {
		String[] topics = TopicFileReader.readTopicFile(topicsFilePath);
		for(int i = 0; i < topics.length; i++) {
			try {
				researcher.researchTopic(topics[i]);
			} catch (Exception e) {
				logger.severe("Error researching topic \"" + topics[i] +"\": " + e.toString());
			}
		}
	}

}
