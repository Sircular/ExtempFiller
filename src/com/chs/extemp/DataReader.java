package com.chs.extemp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class DataReader {

	public final static String DEFAULT_CACHE_PATH = "./.extempcache";
	public final static String DEFAULT_DEV_KEY_PATH = "./extempkey";
	
	private final static String SECRET_KEY = "speechie";
	
	// used to save and load dev keys
	
	public static String loadDevKey(String path) {
		final Cipher cipher = initKeyCipher(Cipher.DECRYPT_MODE);
		
		try {
			final Scanner scanner = new Scanner(new File(path));
			scanner.useDelimiter("\n");
			
			final String ciphertext = scanner.next();
			final byte[] cipherdata = DatatypeConverter.parseBase64Binary(ciphertext);
			
			scanner.close();
			
			final String plaintext = String.valueOf(cipher.doFinal(cipherdata));
			
			return plaintext;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static void saveDevKey(String path, String key) {
		final Cipher cipher = initKeyCipher(Cipher.ENCRYPT_MODE);
		
		try {
			final String ciphertext = DatatypeConverter.printBase64Binary(cipher.doFinal(key.getBytes()));
			
			final OutputStream fileStream = new FileOutputStream(new File(path));
			fileStream.write((ciphertext+'\n').getBytes());
			fileStream.flush();
			fileStream.close();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// give us a properly initialized cipher to use for encryption/decryption
	private static Cipher initKeyCipher(int mode) {		
		if(mode != Cipher.DECRYPT_MODE && mode != Cipher.ENCRYPT_MODE)
			return null;
		
		Cipher cipher = null;
		
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			byte[] raw_key = SECRET_KEY.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(raw_key, "AES");
			
			byte[] iv = new byte[cipher.getBlockSize()];
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
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
	}
	
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
		final File file = new File(path);
		if(!file.exists())
			file.delete();
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
