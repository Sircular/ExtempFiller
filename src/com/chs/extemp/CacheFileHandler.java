package com.chs.extemp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class CacheFileHandler {

	public final static String DEFAULT_CACHE_PATH = "./.extempcache";

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

}
