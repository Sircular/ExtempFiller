package com.chs.extemp.gui;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.gui.events.ResearchCommand;
import com.chs.extemp.gui.events.ResearchEvent;
import com.evernote.edam.type.Tag;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResearchWorker {

	private final LinkedBlockingQueue<String> researchQueue;
	private final LinkedBlockingQueue<String> deleteQueue;
	private final LinkedBlockingQueue<ResearchListener> listeners;

	private final Thread researchThread;
	private final Thread deletionThread;

	private final Logger logger;

	private Researcher researcher;

	public ResearchWorker() {
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
			researcher = new Researcher();
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
					logger.info("Added topic to research queue.");
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
			while (true) {
				try {
					String topic = researchQueue.take();
					if (!deleteQueue.contains(topic)) {
						researchTopic(topic);
					}
				} catch (final InterruptedException ie) {
					logger.severe("Thread stopped.");
					return;
				} catch (final Exception e) {
					logger.severe("Error researching topic.");
				}
			}
		}
	}

	private class DeletionRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					String topic = deleteQueue.take();
					deleteTopic(topic);
				} catch (final InterruptedException ie) {
					logger.severe("Thread stopped.");
					return;
				} catch (final Exception e) {
					logger.severe("Error researching topic.");
				}
			}
		}
	}

	private boolean deleteTopic(final String topic) {
		try {
			Tag tag = researcher.getEvernoteClient().getTag(topic);
			if (tag != null) {
				researcher.getEvernoteClient().deleteTag(tag);
				dispatchEvent(ResearchEvent.Type.TOPIC_DELETED, topic);
				return true;
			} else {
				if (researchQueue.remove(topic)) {
					dispatchEvent(ResearchEvent.Type.TOPIC_DELETED, topic);
					return true;
				}
			}
		} catch (Exception e) {
			dispatchEvent(ResearchEvent.Type.RESEARCH_ERROR, topic);
			logger.log(Level.SEVERE, "Could not remove topic.", e);
		}
		return false;
	}

	private boolean researchTopic(final String topic) {
		try {
			dispatchEvent(ResearchEvent.Type.TOPIC_RESEARCHING, topic);
			researcher.researchTopic(topic);
			dispatchEvent(ResearchEvent.Type.TOPIC_RESEARCHED, topic);
			return true;
		} catch (Exception e) {
			logger.severe("Error researching topic \"" + topic + "\": " + e);
			dispatchEvent(ResearchEvent.Type.RESEARCH_ERROR, topic);
		}
		return false;
	}

	private boolean loadTopics() {
		try {
			logger.info("Attempting to load topic list...");
			final List<Tag> tagList = researcher.getEvernoteClient().getTags();
			final String[] tagNames = new String[tagList.size()];
			for (int i = 0; i < tagList.size(); i++) {
				tagNames[i] = tagList.get(i).getName();
			}
			logger.info("Loaded topic list from Evernote.");
			dispatchEvent(ResearchEvent.Type.TOPIC_LIST_LOADED, tagNames);
		} catch (Exception e) {
			logger.severe("Error while loading topic list: " + e);
			dispatchEvent(ResearchEvent.Type.EVERNOTE_CONNECTION_ERROR, null);
		}
		return false;
	}

	private void dispatchEvent(ResearchEvent.Type eventType, Object data) {
		final ResearchEvent event = new ResearchEvent(this, eventType, data);
		for (ResearchListener listener : listeners) {
			listener.handleMessageEvent(event);
		}
	}
}
