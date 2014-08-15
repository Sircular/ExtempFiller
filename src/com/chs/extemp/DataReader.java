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

	public final static String DEFAULT_CACHE_PATH = "./.extempcache";
	public final static String DEFAULT_DEV_KEY_PATH = "./.extempkey";
	
	// used to save and load dev keys
	// WARNING: CURRENTLY SAVES IN PLAINTEXT
	
	public static String loadDevKey(String path) {
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (scanner == null)
			return "";
		
		scanner.useDelimiter("\n");
		
		// weird, weird, weird... but I ran into a problem that caused this, so I have to fix it.
		if (!scanner.hasNext())
			return "";
		
		final String plaintext = scanner.next();
		
		scanner.close();
		
		return plaintext;
		
		
	}
	
	public static void saveDevKey(String path, String key) {
		
		try {
			final OutputStream fileStream = new FileOutputStream(new File(path));
			fileStream.write((key+'\n').getBytes());
			fileStream.flush();
			fileStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// give us a properly initialized cipher to use for encryption/decryption
	// WARNING: NOT USED AT PRESENT
	/*private static Cipher initKeyCipher(int mode) {		
		if (mode != Cipher.DECRYPT_MODE && mode != Cipher.ENCRYPT_MODE)
			return null;
		
		Cipher cipher = null;
		
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			final byte[] raw_key = SECRET_KEY.getBytes();
			final SecretKeySpec keySpec = new SecretKeySpec(raw_key, "AES");
			
			final byte[] iv = new byte[cipher.getBlockSize()];
			final IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			cipher.init(mode, keySpec, ivSpec);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cipher;
	} */
	
	// used for working with cached topics

	public static String[] loadCacheFile(String path) {
		final ArrayList<String> topics = new ArrayList<String>();

		try {
			final File file = new File(path);
			final Scanner fileScanner = new Scanner(file.getAbsoluteFile());
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
		final File file = new File(path).getAbsoluteFile();
		if(file.exists())
			if(!file.delete())
				ExtempLogger.getLogger().severe("Cannot delete cache.");
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

	public static void deleteAuthTokenFile(String path) {
		final File file = new File(path).getAbsoluteFile();
		if(file.exists())
			if(!file.delete())
				ExtempLogger.getLogger().severe("Cannot delete auth token.");		
	}

}
