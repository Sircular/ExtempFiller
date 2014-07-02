package com.chs.extemp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class DataReader {

	public final static String DEFAULT_CACHE_FILE_PATH = "./.extempcache";
	
	// used for working with cached topics

	public static String[] loadCacheFile(String path) {
		final ArrayList<String> topics = new ArrayList<String>();

		try {
			final Scanner fileScanner = new Scanner(new File(path));
			fileScanner.useDelimiter("\n");
			while(fileScanner.hasNext())
				topics.add(fileScanner.next());
			fileScanner.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return topics.toArray(new String[]{});
	}

	public static void saveCacheFile(String path, String[] topics) {
		// we don't want to save an empty file
		if (topics.length == 0) {
			deleteCacheFile(path);
			return;
		}
		try {
			final OutputStream fileStream = new FileOutputStream(new File(path));

			for (final String topic : topics) {
				final String currentTopic = topic+'\n';
				final byte[] byteStream = currentTopic.getBytes();
				fileStream.write(byteStream);
			}

			fileStream.flush();

			fileStream.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void deleteCacheFile(String path) {
		if (cacheFileExists(path))
			new File(path).delete();
	}

	public static boolean cacheFileExists(String path) {
		return new File(path).exists();
	}
	
	// Used to load files containing topic lists
	
	public static String[] readTopicFile(final String filePath) {
		final Logger logger = ExtempLogger.getLogger();
		logger.info("Attempting to read topic file...");
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			final List<String> lines = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null)
				lines.add(line);
			reader.close();
			logger.info("Topic file read successfully.");
			return lines.toArray(new String[lines.size()]);
		} catch (final IOException io) {
			logger.info("Error while reading topic file: " + io.getMessage());
			return null;
		}
	}

}
