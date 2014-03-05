package com.chs.extemp.evernote;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.evernote.util.HtmlToENMLifier;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bridges the gap between the Evernote API provided at
 * https://github.com/evernote/evernote-sdk-csharp and the main program.
 *
 * @author Logan Lembke
 */
public class EvernoteClient {

	private static final String AUTH_TOKEN = "S=s1:U=8d68f:E=14a374824b4:C=142df96f8b7:P=1cd:A=en-devtoken:V=2:H=519ee13f68920aa525e0c8348eb54fdb";

	// Used for Authentication
	private UserStoreClient userStore;

	// Used for interacting with user data
	private NoteStoreClient noteStore;

	// Used for timing api requests
	private long rateTimer = 0;

	// How long to wait between each api request in milliseconds
	private final int TIMER = 250;

	private Logger logger = ExtempLogger.getLogger();

	/**
	 * Creates a new instance of an Evernote client.
	 *
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public EvernoteClient() throws Exception {

		// Set up the UserStore client and check that we can speak to the server
		// Make sure to change the EvernoteService argument to either SANDBOX or PRODUCTION
		final EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.SANDBOX, AUTH_TOKEN);
		final ClientFactory factory = new ClientFactory(evernoteAuth);

		userStore = factory.createUserStoreClient();

		// Check that API is current with standards
		final boolean versionOk = userStore.checkVersion("CHS Extemp Filler (Java)",
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
				com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
		if (!versionOk) {
			throw new RuntimeException("Incompatible Evernote client protocol version");
		}

		// Set up the NoteStore client
		noteStore = factory.createNoteStoreClient();

		// Start api request timing
		checkRateTimer();
	}

	/**
	 * Retrieves a list of notes from the Evernote service matching a filter
	 *
	 * @param filter Limits the selection of notes. NoteFilters can filter by tags, notebooks, date, etc.
	 * @param amount The amount of notes to return
	 * @return A list of notes held on the server matching the filter
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Note> getNotes(final NoteFilter filter, final int amount) throws Exception {
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			return noteStore.findNotes(filter, 0, amount).getNotes();
		} catch (EDAMSystemException edam) {
			// We are being throttled by Evernote
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				return getNotes(filter, amount);
			} else {
				throw edam;
			}
		}
	}

	/**
	 * Retrieves a list of all the notes held by the Evernote service
	 *
	 * @param amount The amount of notes to return. The list is sorted by creation date.
	 *               Eg. 10 returns the first 10 notes created.
	 * @return A list of notes held on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Note> getNotes(final int amount) throws Exception {
		final NoteFilter filter = new NoteFilter();
		filter.setOrder(NoteSortOrder.CREATED.getValue());
		filter.setAscending(true);
		return getNotes(filter, amount);
	}

	/**
	 * Retrieves a list of notes from the Evernote service contained within a given notebook
	 *
	 * @param notebook The containing notebook
	 * @param amount   The amount of notes to return
	 * @return A list of notes held on the server within the notebook
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Note> getNotesInNotebook(final Notebook notebook, final int amount) throws Exception {
		final NoteFilter filter = new NoteFilter();
		filter.setNotebookGuid(notebook.getGuid());
		filter.setOrder(NoteSortOrder.CREATED.getValue());
		filter.setAscending(true);
		return getNotes(filter, amount);
	}

	/**
	 * Retrieves a list of notes from the Evernote service marked with a given tag
	 *
	 * @param tag    The marked tag
	 * @param amount The amount of notes to return
	 * @return A list of notes held on the server marked with given tag
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Note> getNotesByTag(final Tag tag, final int amount) throws Exception {
		final NoteFilter filter = new NoteFilter();
		filter.setTagGuids(Arrays.asList(tag.getGuid()));
		filter.setOrder(NoteSortOrder.CREATED.getValue());
		filter.setAscending(true);
		return getNotes(filter, amount);
	}

	/**
	 * Retrieves a list of the notebooks from the Evernote service
	 *
	 * @return A list of the notebooks held on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Notebook> getNotebooks() throws Exception {
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			return noteStore.listNotebooks();
		} catch (EDAMSystemException edam) {
			// We are being throttled
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				return getNotebooks();
			} else {
				throw edam;
			}
		}
	}

	/**
	 * Retrieves a notebook from the Evernote service with a given name
	 *
	 * @param name The name of the desired notebook
	 * @return The notebook
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Notebook getNotebook(String name) throws Exception {
		//Notebooks are limited to titles of 100 characters or less
		if (name.length() > 100) {
			name = name.substring(0, 99).trim();
		}
		for (Notebook notebook : getNotebooks()) {
			if (notebook.getName().equalsIgnoreCase(name)) {
				return notebook;
			}
		}
		return null;
	}

	/**
	 * Retrieves a list of the notebooks held by the Evernote service within a given stack
	 *
	 * @param stack The containing stack
	 * @return A list of notebooks held on the server within the stack
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Notebook> getNotebooksInStack(final String stack) throws Exception {
		final List<Notebook> notebooks = getNotebooks();
		final List<Notebook> toReturn = new LinkedList<Notebook>();
		for (Notebook notebook : notebooks) {
			if (notebook.getStack().equalsIgnoreCase(stack)) {
				toReturn.add(notebook);
			}
		}
		return toReturn;
	}

	/**
	 * Retrieves a list of tags from the Evernote service
	 *
	 * @return A list of the tags held on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public List<Tag> getTags() throws Exception {
		try {
			checkRateTimer();
			return noteStore.listTags();
		} catch (EDAMSystemException edam) {
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				return getTags();
			} else {
				throw edam;
			}
		}
	}

	/**
	 * Retrieves a tag from the Evernote service with a given name
	 *
	 * @param name The name of the desired tag
	 * @return The tag
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Tag getTag(String name) throws Exception {
		if (name.length() > 100) {
			name = name.substring(0, 99).trim();
		}
		if (name.contains(",")) {
			name = name.replace(",", "");
		}
		for (Tag tag : getTags()) {
			if (tag.getName().equalsIgnoreCase(name))
				return tag;
		}
		return null;
	}

	/**
	 * Creates a note on the Evernote servers from an hyper-text link within a given notebook and with given tags
	 *
	 * @param link     The URL of the html page to parse as a note
	 * @param notebook The notebook to contain the note
	 * @param tags     The tags to apply to the note
	 * @return The note created on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Note createHTMLNote(final String link, final Notebook notebook, final List<Tag> tags) throws Exception {
		// Translate HTML to ENML
		HtmlToENMLifier translator = new HtmlToENMLifier(link);

		// Create a local note
		Note note = translator.toNote();

		// Set the notebook and tags
		note.setNotebookGuid(notebook.getGuid());
		final List<String> tagGuids = new LinkedList<String>();
		for (Tag tag : tags) {
			tagGuids.add(tag.getGuid());
		}
		note.setTagGuids(tagGuids);

		// Create an uninitialized note to hold the server note
		Note newNote;
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			newNote = noteStore.createNote(note);
		} catch (EDAMUserException edam) {
			// The translator failed to remove an error causing html tag
			if (edam.getErrorCode() == EDAMErrorCode.ENML_VALIDATION) {
				logger.severe("ENML_VALIDATION ERROR: " + edam.getParameter());
				logger.severe("TRYING TO FIX!");
				return createTroubledHTMLNote(translator, edam, notebook, tags);
			}
			// The title of the note is bad
			else if (edam.getErrorCode() == EDAMErrorCode.BAD_DATA_FORMAT && edam.getParameter().equals("Note.title")) {
				//Try website domain for title
				String domain = link.replace("http://", "");
				domain = domain.substring(0, domain.indexOf("/"));
				note.setTitle(domain);
				// Always check the rate timer to make sure we do not overburden the server
				checkRateTimer();
				newNote = noteStore.createNote(note);
			} else {
				throw edam;
			}
		} catch (EDAMSystemException edam) {
			// We are being throttled by Evernote
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				checkRateTimer();
				newNote = noteStore.createNote(note);
			} else {
				throw edam;
			}
		}
		logger.info("Successfully created a new note with GUID: "
				+ newNote.getGuid() + " and name: " + newNote.getTitle());
		return newNote;
	}

	/**
	 * Attempts to translate a bad html document to enml and upload it within a given notebook and given tags
	 *
	 * @param translator The HtmlToENMLifier object that had failed to translate the html
	 * @param edam       The exception giving why the html failed to be valid enml
	 * @param notebook   The notebook to contain the note
	 * @param tags       The tags to apply to the note
	 * @return The note created on the server
	 * @throws Exception All exceptions are thrown ot hte calling program
	 */
	private Note createTroubledHTMLNote(final HtmlToENMLifier translator, final EDAMUserException edam,
	                                    final Notebook notebook, final List<Tag> tags) throws Exception {
		// Try to fix the html to enml error
		translator.fixEdam(edam);
		// Finish the translation
		Note note = translator.toNote();

		// Set the notebook and tags
		note.setNotebookGuid(notebook.getGuid());
		final List<String> tagGuids = new LinkedList<String>();
		for (Tag tag : tags) {
			tagGuids.add(tag.getGuid());
		}
		note.setTagGuids(tagGuids);

		// Create an uninitialized note to hold the server note
		Note newNote;
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			newNote = noteStore.createNote(note);
		} catch (EDAMUserException edam2) {
			// The translator failed to remove an error causing html tag
			if (edam2.getErrorCode() == EDAMErrorCode.ENML_VALIDATION) {
				logger.severe("ENML_VALIDATION ERROR: " + edam.getParameter());
				logger.severe("TRYING TO FIX!");
				//Recursively call this function to fix other translation errors
				return createTroubledHTMLNote(translator, edam2, notebook, tags);
			}
			// The title of the note is bad
			else if (edam2.getErrorCode() == EDAMErrorCode.BAD_DATA_FORMAT && edam2.getParameter().equals("Note.title")) {
				//Try website domain for title
				String domain = translator.getURL().replace("http://", "");
				domain = domain.substring(0, domain.indexOf("/"));
				note.setTitle(domain);
				checkRateTimer();
				newNote = noteStore.createNote(note);
			} else {
				throw edam2;
			}
		} catch (EDAMSystemException edam3) {
			// We are being throttled by Evernote
			if (edam3.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam3.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam3.getRateLimitDuration() * 1000);
				// Always check the rate timer to make sure we do not overburden the server
				checkRateTimer();
				newNote = noteStore.createNote(note);
			} else {
				throw edam3;
			}
		}
		logger.info("Successfully created a new note with GUID: "
				+ newNote.getGuid() + " and name: " + newNote.getTitle());
		return newNote;
	}

	/**
	 * Creates a note on the Evernote servers with a given title and given content within a given notebook and with given tags
	 *
	 * @param title    The title to name the note
	 * @param content  The content to be held within the note
	 * @param notebook The notebook to contain the note
	 * @param tags     The tags to apply to the note
	 * @return The note created on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Note createTextNote(final String title, final String content, final Notebook notebook, final List<Tag> tags) throws Exception {
		// Create a local note
		final Note note = new Note();

		// Set the title, notebook, and tags
		note.setTitle(title);
		note.setNotebookGuid(notebook.getGuid());
		final List<String> tagGuids = new LinkedList<String>();
		for (Tag tag : tags) {
			tagGuids.add(tag.getGuid());
		}
		note.setTagGuids(tagGuids);

		// Construct the skeleton enml
		final String code = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
				+ "<en-note>"
				+ "<p>" + content
				+ "</p>"
				+ "</en-note>";
		note.setContent(code);

		// Create an uninitialized note to hold the server note
		Note newNote;
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			newNote = noteStore.createNote(note);
		} catch (EDAMSystemException edam) {
			// We are being throttled by Evernote
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				// Always check the rate timer to make sure we do not overburden the server
				checkRateTimer();
				newNote = noteStore.createNote(note);
			} else {
				throw edam;
			}
		}
		logger.info("Successfully created a new note with GUID: "
				+ newNote.getGuid() + " and name: " + newNote.getTitle());
		return newNote;
	}

	/**
	 * Creates a notebook on the Evernote servers with a given title
	 *
	 * @param desiredTitle The title to name the notebook
	 * @return The notebook created on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Notebook createNotebook(final String desiredTitle) throws Exception {
		// Create a local notebook
		final Notebook notebook = new Notebook();
		// Set the title; the title can be 100 characters max
		String realTitle;
		if (desiredTitle.length() > 100) {
			realTitle = desiredTitle.substring(0, 99).trim();
		} else {
			realTitle = desiredTitle;
		}
		notebook.setName(realTitle);

		// Create an uninitialized notebook to hold the server notebook
		Notebook newNotebook;
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			newNotebook = noteStore.createNotebook(notebook);
		} catch (EDAMSystemException edam) {
			// We are being throttled by Evernote
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				// Always check the rate timer to make sure we do not overburden the server
				checkRateTimer();
				newNotebook = noteStore.createNotebook(notebook);
			} else {
				throw edam;
			}
		}
		logger.info("Successfully created a new notebook with GUID: "
				+ newNotebook.getGuid() + " and name: " + newNotebook.getName());

		// If we had to shorten the title, create a note in the folder with the desired folder
		if (!realTitle.equals(desiredTitle)) {
			createTextNote("Desired Title", desiredTitle, newNotebook, new LinkedList<Tag>());
		}
		return newNotebook;
	}

	/**
	 * Creates a tag on the Evernote servers with a given title
	 *
	 * @param desiredName The name to title the tag
	 * @return The tag created on the server
	 * @throws Exception All exceptions are thrown to the calling program
	 */
	public Tag createTag(final String desiredName) throws Exception {
		// Create a local tag
		final Tag tag = new Tag();

		// Set the name; the name can be 100 characters max; the name cannot contain commas
		String realName;
		if (desiredName.length() > 100) {
			realName = desiredName.substring(0, 99).trim();
		} else {
			realName = desiredName;
		}
		if (realName.contains(",")) {
			realName = realName.replace(",", "");
		}

		tag.setName(realName);

		// Create an uninitialized notebook to hold the server notebook
		Tag newTag;
		try {
			// Always check the rate timer to make sure we do not overburden the server
			checkRateTimer();
			newTag = noteStore.createTag(tag);
		} catch (EDAMSystemException edam) {
			// We are being throttled by Evernote
			if (edam.getErrorCode() == EDAMErrorCode.RATE_LIMIT_REACHED) {
				logger.severe("Waiting " + edam.getRateLimitDuration() + " seconds to continue...");
				Thread.sleep(edam.getRateLimitDuration() * 1000);
				// Always check the rate timer to make sure we do not overburden the server
				checkRateTimer();
				newTag = noteStore.createTag(tag);
			} else {
				throw edam;
			}
		}

		logger.info("Successfully created a new tag with GUID: "
				+ newTag.getGuid() + " and name: " + newTag.getName());

		// Create a notebook for holding notes with desired tag names
		Notebook tagNotebook = getNotebook("Tag Names");
		if (tagNotebook == null) {
			logger.info("Creating Tag Names Notebook");
			tagNotebook = createNotebook("Tag Names");
		}

		// If we had to shorten the name, create a note with the desired name
		createTextNote("Desired Tag Name", desiredName, tagNotebook, Arrays.asList(newTag));
		return newTag;
	}

	/**
	 * Regulates how often the api is called
	 *
	 * @throws InterruptedException Interrupted whilst sleeping
	 */
	private void checkRateTimer() throws InterruptedException {
		if (Calendar.getInstance().getTimeInMillis() < rateTimer + TIMER) {
			Thread.sleep(rateTimer + TIMER - Calendar.getInstance().getTimeInMillis());
		}
		rateTimer = Calendar.getInstance().getTimeInMillis();
	}

}


