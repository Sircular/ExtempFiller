package com.chs.extemp.readability;

/**
 * A class to hold the Readability Parser api JSON results. Used by GSON.
 *
 * @author Logan Lembke
 */
public class ReadabilityResults {
	private String content;
	private String url;
	private String title;
	private String date_published;

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDate_published() {
		return date_published;
	}

	public void setDate_published(final String date_published) {
		this.date_published = date_published;
	}
}
