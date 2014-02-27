package com.chs.extemp;

import com.chs.extemp.evernote.EvernoteClient;
import com.chs.extemp.google.GoogleClient;
import com.chs.extemp.google.GoogleResults;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;
import com.chs.extemp.cli.CLI;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * ExtempFiller program
 * @author Logan Lembke
 */
public class Main {
	private static final int MINIMUM_DOCUMENTS_PER_TOPIC = 8;

	// Used for timing api requests
	private static long rateTimer = 0;

	// How long to wait between each api request in milliseconds
	private static final int TIMER = 1000;

	public static void main(final String[] args) {
		try {
			System.out.println("CHS Extemporaneous Researcher");
			if(args.length > 0) {
				filename = args[0];
				System.out.println("Starting CLI using questions-list \"" + filename + ".\"");
				
				return;
			} else {
				System.out.println("No questions list specified. Starting GUI interface...");
				// Will eventually start the GUI
				return;
			}
			// Create a new Evernote Client
			final EvernoteClient evernoteClient = new EvernoteClient();

			// Create a notebook for our notes
			Notebook HTMLNotebook = evernoteClient.getNotebook("Web Notes");
			if(HTMLNotebook == null) {
				System.out.println("Creating Web Notes Notebook");
				HTMLNotebook = evernoteClient.createNotebook("Web Notes");
			}

			// Questions must be in UTF-8 encoding for special characters
			final FileInputStream questions = new FileInputStream(filename);
			final Scanner scanner = new Scanner(questions, "UTF-8");
			while(scanner.hasNext()) {
				// Get the next question
                final String topic = scanner.nextLine();

				// Check if the question has already been researched
				System.out.println("SEARCHING FOR TAG: " + topic);
				Tag tag = evernoteClient.getTag(topic);

				// If it hasn't, research it
				if(tag == null) {
					// Create the tag for the topic
					System.out.println("TAG NOT FOUND");
					System.out.println();
					System.out.println("CREATING TAG: " + topic);
					tag = evernoteClient.createTag(topic);

					// Create a list to hold web pages to upload
					LinkedList<GoogleResults.Result> totalSearchResults = new LinkedList<GoogleResults.Result>();

					System.out.println("GOOGLING: " + topic);

					int currentSearchIndex = 0;
					while(totalSearchResults.size() < MINIMUM_DOCUMENTS_PER_TOPIC)  {
						// Always check the rate timer to make sure we do not overburden the server
						checkRateTimer();

						// Search google for web pages
						GoogleResults currentSearchResults = GoogleClient.search(topic, currentSearchIndex);

						// Check if we are being throttled by google
						if(currentSearchResults == null || currentSearchResults.getResponseData() == null || currentSearchResults.getResponseData().getResults() == null) {
							System.err.println("Google connection has been throttled.");
							System.err.println("Waiting 10 minutes!");
							Thread.sleep(1000*60*10);
							continue;
						}

						// Add results
						for (GoogleResults.Result gResult : currentSearchResults.getResponseData().getResults()) {
							// Ignore the topic listing
							if(gResult.getUrl().contains("http://www.nfhs.org/")) {
								continue;
							}
							System.out.println("FOUND!: " + gResult.getUrl());
							// Ignore non-html files
							if(gResult.getUrl().endsWith(".pdf")) {
								System.out.println("TYPE: PDF. *EXCLUDING*");
								continue;
							} else if(gResult.getUrl().endsWith(".ppt")) {
								System.out.println("TYPE: PPT. *EXCLUDING*");
								continue;
							} else if(gResult.getUrl().endsWith(".docx")) {
								System.out.println("TYPE: DOCX. *EXCLUDING*");
								continue;
							} else if(gResult.getUrl().endsWith(".doc")) {
								System.out.println("TYPE: DOC. *EXCLUDING*");
								continue;
							} else if(gResult.getUrl().endsWith(".rtf")) {
								System.out.println("TYPE: RTF. *EXCLUDING*");
								continue;
							}
							totalSearchResults.add(gResult);
						}

						// Get a new set of data on next search
						currentSearchIndex += 7;
					}

					System.out.println();

					try {
						for(GoogleResults.Result result : totalSearchResults) {
							// Always check the rate timer to make sure we do not overburden the server
							System.out.println("UPLOADING: " + result.getUrl());
							checkRateTimer();
							evernoteClient.createHTMLNote(result.getUrl(), HTMLNotebook, Arrays.asList(tag));
						}
					} catch (Exception e) {
						System.err.println("SKIPPING SOURCE");
						e.printStackTrace();
						System.err.println();
					}
				} else {
					System.out.println("TAG FOUND. CONTINUING.");
					System.out.println();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Regulates how often the api is called
	 * @throws InterruptedException Interrupted whilst sleeping
	 */
	private static void checkRateTimer() throws InterruptedException {
		if(Calendar.getInstance().getTimeInMillis() < rateTimer + TIMER) {
			Thread.sleep(rateTimer + TIMER - Calendar.getInstance().getTimeInMillis());
		}
		rateTimer = Calendar.getInstance().getTimeInMillis();
	}
}
