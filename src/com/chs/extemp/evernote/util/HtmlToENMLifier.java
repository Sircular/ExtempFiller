package com.chs.extemp.evernote.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.chs.extemp.readability.ReadabilityClient;
import com.chs.extemp.readability.ReadabilityResults;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Note;

/**
 * Parses HTML and transforms it to ENML
 * @author Logan Lembke
 */
public class HtmlToENMLifier {
	// URL of the Hyper-Text file
	private final String URL;

	// The response from the Readability API
	private ReadabilityResults readabilityResults;

	// The malleable xml document holding the html/enml contents
	private Document document;

	/**
	 * Initializes the translator with a given url
	 * @param url A link to a hyper-text file
	 */
	public HtmlToENMLifier(String url) {
		this.URL = url;
	}

	/**
	 * Calls the Readability Parser API and stores the response
	 * @throws Exception
	 */
	private void getReadabilityResults() throws Exception{
		this.readabilityResults = ReadabilityClient.getReadableContent(URL);
	}

	/**
	 * Initializes the document with the response from Readability
	 */
	private void initializeDocument() {
		this.document = Jsoup.parseBodyFragment(readabilityResults.getContent());
	}

	/**
	 * Removes html tags and attributes that are note valid in enml
	 */
	private void cleanDocument() {
		final Elements idElements = document.select("[id]");
		for (final Element idElement : idElements)
			idElement.removeAttr("id");
		final Elements classElements = document.select("[class]");
		for (final Element classElement : classElements)
			classElement.removeAttr("class");
		final Elements altElements = document.select("[alt]");
		for (final Element altElement : altElements)
			altElement.removeAttr("alt");
		final Elements relElements = document.select("[rel]");
		for (final Element relElement : relElements)
			relElement.removeAttr("rel");
		final Elements scoreElements = document.select("[score]");
		for (final Element scoreElement : scoreElements)
			scoreElement.removeAttr("score");
		final Elements tableColumns = document.select("table[cols]");
		for (final Element tableColumn : tableColumns)
			tableColumn.removeAttr("cols");
		final Elements tableHeights = document.select("table[height]");
		for (final Element tableHeight : tableHeights)
			tableHeight.removeAttr("height");
		final Elements linkElements = document.select("a[href]");
		for (final Element linkElement : linkElements)
			linkElement.removeAttr("href");
		final Elements sectionElements = document.select(
				"figcaption, hgroup, noscript, header, footer, section, nav, article, " +
				"figure, aside, attr, more, fieldset, content, recommendations-bar");
		for (final Element sectionElement : sectionElements) {
			final Attributes attributes = sectionElement.attributes();
			sectionElement.replaceWith(new Element(Tag.valueOf("div"), "", attributes));
		}
		//Just for the freaking new york times.... >.<
		final Elements nytElements = document.select(
				"nyt_byline, nyt_text, nyt_correction_top, " +
						"nyt_correction_bottom, nyt_update_top," +
				" nyt_update_bottom, nyt_author_id");
		for (final Element nytElement : nytElements)
			nytElement.replaceWith(new Element(Tag.valueOf("div"), ""));
		final Elements divElements = document.select("div");
		for (final Element divElement : divElements) {
			final Attributes attributes = divElement.attributes();
			for (final Attribute attribute : attributes)
				divElement.removeAttr(attribute.getKey());
		}
		final Elements removeElements = document.select("iframe, noframe, meta, plusone");
		for (final Element removeElement : removeElements)
			removeElement.remove();
		final Elements h3Elements = document.select("time, label");
		for (final Element h3Element : h3Elements)
			h3Element.replaceWith(new Element(Tag.valueOf("h3"), "").text(h3Element.text()));
	}

	/**
	 * Transforms the web page referenced by this object's URL field into a local Evernote note.
	 * @return A local Evernote note with the content provided at the web page referenced by this object's url
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Note toNote() throws Exception {
		if (document == null) {
			getReadabilityResults();
			initializeDocument();
			cleanDocument();
		}

		final StringBuilder noteBody = new StringBuilder();
		noteBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		noteBody.append("<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">");
		noteBody.append("<en-note>");
		noteBody.append(document.html()
				.replace("<html>", "")
				.replace("<head>", "")
				.replace("</head>\n", "")
				.replace("<body>\n", "")
				.replace("</body>\n", "")
				.replace("</html>", "")
				.replace("<nobr>", "")
				.replace("</nobr>", ""));
		noteBody.append("<h3>").append(URL);
		if (readabilityResults.getDate_published() != null)
			noteBody.append(" @ ").append(readabilityResults.getDate_published());
		noteBody.append("</h3>");
		noteBody.append("</en-note>");
		final Note note = new Note();
		note.setTitle(readabilityResults.getTitle());
		note.setContent(noteBody.toString());
		return note;
	}

	/**
	 * Given a ENML_VALIDATION exception, try to fix this object's document in order to pass validation
	 * @param edam the ENML_VALIDATION exception given by the Evernote server
	 */
	public void fixEdam(EDAMUserException edam) {
		final String parameter = edam.getParameter();
		if (parameter.startsWith("Element")) { // remove an invalid element
			final String identifier = parameter.substring(parameter.indexOf("\"") + 1, parameter.indexOf("\"", parameter.indexOf("\"") + 1));
			final Elements troubleElements = document.select(identifier);
			for (final Element troubleElement : troubleElements)
				if (troubleElement.children().size() == 0) {
					if (troubleElement.hasText())
						troubleElement.replaceWith(new Element(Tag.valueOf("p"), ""));
					else
						troubleElement.remove();
				} else {
					troubleElement.replaceWith(new Element(Tag.valueOf("div"), ""));
					final Attributes attributes = troubleElement.attributes();
					for (final Attribute attribute : attributes)
						troubleElement.removeAttr(attribute.getKey());
				}
		} else if (parameter.startsWith("Attribute")) { // remove an invalid attribute
			final String identifier = parameter.substring(parameter.indexOf("\"") + 1, parameter.indexOf("\"", parameter.indexOf("\"") + 1));
			final Elements troubleElements = document.select("[" + identifier + "]");
			for (final Element troubleElement : troubleElements)
				troubleElement.removeAttr(identifier);
		} else if (parameter.startsWith("The reference")) { // we have an unescaped HTML entity
			// the best way is to use a really lenient Jsoup.clean call
			final String oldHTML = document.html();
			final String newHTML = Jsoup.clean(oldHTML, Whitelist.relaxed());
			document.html(newHTML);
		}
	}

	/**
	 * @return The URL this object references
	 */
	public String getURL() {
		return URL;
	}
}
