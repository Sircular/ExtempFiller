package com.chs.extemp.gui;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.gui.events.ResearchCommand;
import com.chs.extemp.gui.events.ResearchEvent;
import com.evernote.edam.type.Tag;

public class EvernoteWorker {

	private final String EVERNOTE_TOKEN;

	private final LinkedBlockingQueue<String> researchQueue;
	private final LinkedBlockingQueue<String> deleteQueue;
	private final LinkedBlockingQueue<ResearchListener> listeners;

	private final Thread researchThread;
	private final Thread deletionThread;

	private final Logger logger;

	private Researcher researcher;

	public EvernoteWorker(String evernoteToken) {
		EVERNOTE_TOKEN = evernoteToken;

		logger = ExtempLogger.getLogger();
		researchQueue = new LinkedBlockingQueue<String>();
		deleteQueue = new LinkedBlockingQueue<String>();
		researchThread = new Thread(new ResearchRunnable(), "Research Thread");
		deletionThread = new Thread(new DeletionRunnable(), "Deletion Thread");
		listeners = new LinkedBlockingQueue<ResearchListener>();
	}

	public void startWorkerThreads() {
		logger.info("Starting worker threads...");
		try {
			researcher = new Researcher(EVERNOTE_TOKEN);
			dispatchEvent(ResearchEvent.Type.USERNAME, researcher.getEvernoteClient().getUsername());
		} catch (final Exception e) {
			dispatchEvent(ResearchEvent.Type.EVERNOTE_CONNECTION_ERROR, null);
			return;
		}
		researchThread.start();
		deletionThread.start();
	}

	public void interruptWorkerThreads() {
		researchThread.interrupt();
		deletionThread.interrupt();
	}

	public void registerListener(final ResearchListener listener) {
		listeners.add(listener);
	}

	public void enqueueCommand(final ResearchCommand command) {
		try {
			switch (command.getType()) {
			case RESEARCH_TOPIC:
				researchQueue.put(command.getTopic());
				dispatchEvent(ResearchEvent.Type.TOPIC_QUEUED_FOR_RESEARCH, command.getTopic());
				logger.info("Added topic to research queue.");
				break;
			case UNQUEUE_TOPIC:
				new Thread(
						new Runnable() {
							@Override
							public void run() {
								unqueueTopic(command.getTopic());
							}
						}
						, "Unqueue Thread").start();
				break;
			case DELETE_TOPIC:
				dispatchEvent(ResearchEvent.Type.TOPIC_DELETING, command.getTopic());
				deleteQueue.put(command.getTopic());
				logger.info("Added topic to deletion queue.");
				break;
			case LOAD_TOPICS:
				new Thread(
						new Runnable() {
							@Override
							public void run() {
								loadTopics();
							}
						}
						, "Topic Download Thread").start();
				break;
			}
		} catch (final Exception e) {
			logger.severe("Error adding topic to queue.");
		}
	}

	private class ResearchRunnable implements Runnable {
		@Override
		public void run() {
			while (true)
				try {
					final String topic = researchQueue.take();
					if (!deleteQueue.contains(topic))
						researchTopic(topic);
					else {

						// This should never happen, but just in case...
						logger.info("Removing from research queue: " + topic);
						dispatchEvent(ResearchEvent.Type.TOPIC_DELETED, topic);
					}
				} catch (final InterruptedException ie) {
					logger.severe("Thread stopped.");
					return;
				} catch (final Exception e) {
					logger.severe("Error researching topic.");
				}
		}
	}

	private class DeletionRunnable implements Runnable {
		@Override
		public void run() {
			while (true)
				try {
					final String topic = deleteQueue.take();
					deleteTopic(topic);
				} catch (final InterruptedException ie) {
					logger.severe("Thread stopped.");
					return;
				} catch (final Exception e) {
					logger.severe("Error researching topic.");
				}
		}
	}

	private boolean researchTopic(final String topic) {
		try {
			dispatchEvent(ResearchEvent.Type.TOPIC_RESEARCHING, topic);
			researcher.researchTopic(topic);
			dispatchEvent(ResearchEvent.Type.TOPIC_RESEARCHED, topic);
			return true;
		} catch (final Exception e) {
			logger.severe("Error researching topic \"" + topic + "\": " + e);
			dispatchEvent(ResearchEvent.Type.RESEARCH_ERROR, topic);
		}
		return false;
	}

	private boolean deleteTopic(final String topic) {
		try {
			final Tag tag = researcher.getEvernoteClient().getTag(topic);
			if (tag != null) {
				logger.info("Deleting notes from Evernote for topic: " + topic);
				researcher.getEvernoteClient().deleteTag(tag);
				logger.info("Finished deleting notes from Evernote for topic: " + topic);
				dispatchEvent(ResearchEvent.Type.TOPIC_DELETED, topic);
				return true;
			}
		} catch (final Exception e) {
			dispatchEvent(ResearchEvent.Type.RESEARCH_ERROR, topic);
			logger.log(Level.SEVERE, "Could not remove topic.", e);
		}
		return false;
	}

	private boolean unqueueTopic(final String topic) {
		if (researchQueue.remove(topic)) {
			logger.info("Removing from research queue: " + topic);
			dispatchEvent(ResearchEvent.Type.TOPIC_DELETED, topic);
			return true;
		}
		return false;
	}

	private boolean loadTopics() {
		try {
			logger.info("Attempting to load topic list...");
			final List<String> tagList = researcher.getEvernoteClient().getFullyNamedTags();
			final String[] tagNames = tagList.toArray(new String[tagList.size()]);
			logger.info("Loaded topic list from Evernote.");
			dispatchEvent(ResearchEvent.Type.TOPIC_LIST_LOADED, tagNames);
		} catch (final Exception e) {
			logger.severe("Error while loading topic list: " + e);
			dispatchEvent(ResearchEvent.Type.EVERNOTE_CONNECTION_ERROR, null);
		}
		return false;
	}

	public void cancelResearch() {
		String topic;
		while ((topic = researchQueue.poll()) != null)
			dispatchEvent(ResearchEvent.Type.TOPIC_DELETED, topic);
	}

	private void dispatchEvent(ResearchEvent.Type eventType, Object data) {
		final ResearchEvent event = new ResearchEvent(this, eventType, data);
		for (final ResearchListener listener : listeners)
			listener.handleMessageEvent(event);
	}
}
