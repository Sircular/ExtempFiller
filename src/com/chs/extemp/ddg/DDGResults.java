package com.chs.extemp.ddg;

import java.util.List;

/**
 * A class to hold the Google AJAX api JSON results. Used by GSON.
 *
 * @author Logan Lembke
 */
public class DDGResults {
	private DDGResponseData responseData;

	public DDGResponseData getResponseData() {
		return responseData;
	}

	public void setResponseData(final DDGResponseData responseData) {
		this.responseData = responseData;
	}

	public String toString() {
		return "ResponseData[" + responseData + "]";
	}

	public static class DDGResponseData {
		private List<DDGResult> results;

		public List<DDGResult> getResults() {
			return results;
		}

		public void setResults(final List<DDGResult> results) {
			this.results = results;
		}

		public String toString() {
			return "Results[" + results + "]";
		}
	}

	public static class DDGResult {
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