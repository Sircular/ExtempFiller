package com.chs.extemp.cli;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.DataReader;

public class CLI {
	private final Logger logger;
	private Researcher researcher;

	public CLI(String topicsFilePath) {
		//Initialize logger
		logger = ExtempLogger.getLogger();
		logger.addHandler(new CLILogHandler());
		logger.info("Starting CLI using questions file: " + topicsFilePath);
		
		Scanner input = new Scanner(System.in);

		String devKey = "";
		// select the auth token (unfinished)
		if (new File(DataReader.DEFAULT_DEV_KEY_PATH).exists()) {
			while (true) {
				logger.config("Do you want to use the default Evernote account? [y/N]");
				String choice = input.next().trim();
				if (choice.equals("") || choice.toLowerCase().charAt(0) == 'n') {
					break;
				} else if (choice.toLowerCase().charAt(0) == 'y') {
					devKey = DataReader.loadDevKey(DataReader.DEFAULT_DEV_KEY_PATH);
					break;
				} else {
					logger.warning("Invalid entry. Please try again.");
				}
			}
		}
		if (devKey.equals("")) {
			logger.config("Please enter the dev key:");
			devKey = input.next().trim();
		}
		
		input.close();

		//Start research
		try {
			researcher = new Researcher(devKey);
			doResearch(topicsFilePath);
		} catch (final Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	private void doResearch(String topicsFilePath) {
		for (final String topic : DataReader.readTopicFile(topicsFilePath))
			try {
				researcher.researchTopic(topic, 12);
			} catch (final Exception e) {
				logger.severe("Error researching topic \"" + topic + "\": " + e.toString());
			}
	}
}
