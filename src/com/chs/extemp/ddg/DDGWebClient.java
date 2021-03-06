package com.chs.extemp.ddg;

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

import com.chs.extemp.ddg.DDGResults.DDGResult;

public class DDGWebClient {
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0";
	private static final String SEARCH_URL = "http://duckduckgo.com/html/?kh=-1&kp=1&q=%s";

	public static DDGResults search(String topic) {
		try {
			final URL searchURL = new URL(String.format(SEARCH_URL, URLEncoder.encode(topic, "UTF-8")));
			final URLConnection connection = searchURL.openConnection();
			connection.addRequestProperty("User-Agent", USER_AGENT);

			connection.connect();

			final Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
			final String rawData = scanner.useDelimiter("\\A").next();
			scanner.close();

			final List<DDGResult> results = new LinkedList<DDGResult>();

			final Document tree = Jsoup.parse(rawData);
			final Elements contentNodes = tree.select("div.links_main.links_deep");

			// we want to skip the first result; it is invariably duckduckgo.com
			for (int i = 1; i < contentNodes.size(); i++) {
				final Element currentNode = contentNodes.get(i);

				final Element linkNode = currentNode.select("a.large").get(0);
				final String linkURL = linkNode.attr("href");
				final String linkTitle = linkNode.text();

				final DDGResults.DDGResult newResult = new DDGResults.DDGResult();
				newResult.setTitle(linkTitle);
				newResult.setUrl(linkURL);

				results.add(newResult);
			}

			final DDGResults googleResults = new DDGResults();
			googleResults.setResults(results);

			return googleResults;

		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
