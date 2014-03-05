package com.chs.extemp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TopicFileReader {
	public static String[] readTopicFile(String filePath) {
		Logger logger = ExtempLogger.getLogger();
		logger.info("Attempting to read topic file...");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			List<String> lines = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
			logger.info("Topic file read successfully.");
			return lines.toArray(new String[lines.size()]);
		} catch (IOException e) {
			logger.info("Error while reading topic file: " + e.getMessage());
			return null;
		}
	}
}
