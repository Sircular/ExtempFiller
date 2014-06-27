package com.chs.extemp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class CacheFileHandler {
	
	public final static String DEFAULT_CACHE_PATH = ".extempcache";
	
	public static String[] loadCacheFile(String path) {
		ArrayList<String> topics = new ArrayList<String>();

		try {
			InputStream fileStream = new FileInputStream(new File(path));
			Scanner fileScanner = new Scanner(fileStream);
			while(fileScanner.hasNext()) {
				topics.add(fileScanner.next());
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return topics.toArray(new String[]{});
	}
	
	public static void saveCacheFile(String path, String[] topics) {
		try {
			OutputStream fileStream = new FileOutputStream(new File(path));
			
			for(int i = 0; i < topics.length; i++) {
				String currentTopic = topics[i]+'\n';
				byte[] byteStream = currentTopic.getBytes();
				fileStream.write(byteStream);
			}
			
			fileStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean cacheFileExists(String path) {
		return new File(path).exists();
	}

}
