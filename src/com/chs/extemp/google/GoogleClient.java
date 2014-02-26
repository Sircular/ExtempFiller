package com.chs.extemp.google;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Client for the AJAX Google Search API
 * @author Logan Lembke
 */
public class GoogleClient {
	/**
	 * Returns EIGHT google results for a given query.
	 * @param query The search query.
	 * @param start The result to start listing from starting at 0. More than eight results
	 *              can be obtained by calling multiple searches starting at different numbers.
	 *              Eg. 0, 7, 14, 21 ...
	 * @return A GoogleResults object with the results of the search
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public static GoogleResults search(final String query, final int start) throws Exception{
		final URL url = new URL(
				"http://ajax.googleapis.com/ajax/services/search/web?v=1.0&rsz=8&start=" + start +
						"&q=" + URLEncoder.encode(query, "UTF-8"));
		final URLConnection connection = url.openConnection();
		connection.addRequestProperty("Referrer", "CHS Extemp Filler");
		final InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
		final GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
		reader.close();
		return results;
	}
}
