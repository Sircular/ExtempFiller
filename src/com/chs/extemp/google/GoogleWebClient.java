package com.chs.extemp.google;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chs.extemp.google.GoogleResults.GoogleResponseData;
import com.chs.extemp.google.GoogleResults;
import com.chs.extemp.google.GoogleResults.GoogleResult;

public class GoogleWebClient {
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0";
	private static final String SEARCH_URL = "http://duckduckgo.com/html/?kh=-1&kp=1&q=%s";
	
	public static GoogleResults search(String topic) {
		try {
			URL searchURL = new URL(String.format(SEARCH_URL, URLEncoder.encode(topic, "UTF-8")));
			URLConnection connection = searchURL.openConnection();
			connection.addRequestProperty("User-Agent", USER_AGENT);
			
			connection.connect();
			
			Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
			String rawData = scanner.useDelimiter("\\A").next();
			scanner.close();
			
			List<GoogleResult> results = new LinkedList<GoogleResult>();
			
			Document tree = Jsoup.parse(rawData);
			Elements contentNodes = tree.select("div.links_main.links_deep");
			
			for(int i = 0; i < contentNodes.size(); i++) {
				Element currentNode = contentNodes.get(i);
				
				Element linkNode = currentNode.select("a.large").get(0);
				String linkURL = linkNode.attr("href");
				String linkTitle = linkNode.text();
				
				GoogleResults.GoogleResult newResult = new GoogleResults.GoogleResult();
				newResult.setTitle(linkTitle);
				newResult.setUrl(linkURL);
				
				results.add(newResult);
			}
			
			GoogleResults googleResults = new GoogleResults();
			GoogleResponseData responseData = new GoogleResponseData();
			responseData.setResults(results);
			googleResults.setResponseData(responseData);
			
			return googleResults;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
