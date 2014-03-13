package com.chs.extemp.readability;

import com.chs.extemp.ExtempLogger;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Client for the Readability Parser API
 *
 * @author Logan Lembke
 */
public class ReadabilityClient {
	private final static String API = "https://readability.com/api/content/v1/";
	private final static String AUTH = "7dfac7d927e842450e174100a83a65039ad52bcb";

	private static int retries = 1;
	private final static Logger logger = ExtempLogger.getLogger();

	/**
	 * Retrieves the main content from any web page using the Readability Parser API
	 *
	 * @param address The web page to reduce/ parse
	 * @return A ReadabilityResults object containing the results of the api call
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public static ReadabilityResults getReadableContent(final String address) throws Exception {
		try {
			final URL parserURL = new URL(API + "parser?url=" + address + "&token=" + AUTH);
			final InputStreamReader parserReader = new InputStreamReader(parserURL.openConnection().getInputStream(), "UTF-8");
			final ReadabilityResults parserResults = new Gson().fromJson(parserReader, ReadabilityResults.class);
			parserReader.close();
			retries = 1;
			return parserResults;
		} catch (IOException io) {
			if (retries <= 5) {
				logger.info("Readability error. Retrying (" + retries + " of 5): " + io.getMessage());
				retries++;
				Thread.sleep(1000);
				return getReadableContent(address);
			} else {
				throw io;
			}
		}
	}
}
