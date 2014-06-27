package com.chs.extemp.google;

import java.util.List;

/**
 * A class to hold the Google AJAX api JSON results. Used by GSON.
 *
 * @author Logan Lembke
 */
public class GoogleResults {
	private GoogleResponseData responseData;

	public GoogleResponseData getResponseData() {
		return responseData;
	}

	public void setResponseData(final GoogleResponseData responseData) {
		this.responseData = responseData;
	}

	public String toString() {
		return "ResponseData[" + responseData + "]";
	}

	public static class GoogleResponseData {
		private List<GoogleResult> results;

		public List<GoogleResult> getResults() {
			return results;
		}

		public void setResults(final List<GoogleResult> results) {
			this.results = results;
		}

		public String toString() {
			return "Results[" + results + "]";
		}
	}

	public static class GoogleResult {
		private String url;
		private String title;

		public String getUrl() {
			return url;
		}

		public String getTitle() {
			return title;
		}

		public void setUrl(final String url) {
			this.url = url;
		}

		public void setTitle(final String title) {
			this.title = title;
		}

		public String toString() {
			return "Result[url:" + url + ",title:" + title + "]";
		}
	}
}