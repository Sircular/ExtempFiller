package com.chs.extemp.cli;

import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.TopicFileReader;

public class CLI {
	private CLILogHandler cliHandler;
	private Logger logger;
	private Researcher researcher;
	
	private String topicsFilePath;
	
	public CLI(String questionsFilePath) {
		this.topicsFilePath = questionsFilePath;
		init();
		doResearch();
	}
	
	private void init() {
		logger = ExtempLogger.getLogger();
		cliHandler = new CLILogHandler();
		logger.addHandler(cliHandler);
		logger.info("Starting CLI using questions file: " + topicsFilePath);
		researcher = new Researcher();
	}
	
	private void doResearch() {
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
