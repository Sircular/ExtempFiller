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

	private LinkedBlockingQueue<String> researchQueue;
	private LinkedBlockingQueue<String> deleteQueue;
	private LinkedBlockingQueue<ResearchListener> listeners;

	private Thread researchThread;
	private Thread deletionThread;

	private Logger logger;

	private Researcher researcher;

	public ResearchWorker() {
		logger = ExtempLogger.getLogger();
		researchQueue = new LinkedBlockingQueue<String>();
		deleteQueue = new LinkedBlockingQueue<String>();
		researchThread = new Thread(new ResearchRunnable());
		deletionThread = new Thread(new DeletionRunnable());
		listeners = new LinkedBlockingQueue<ResearchListener>();
	}

	public void startWorkerThreads() {
		logger.info("Starting worker threads...");
		try {
			researcher = new Researcher();

			// get the list of already researched tags
			// and send them to the GUI so they can be
			// added to the list.
			List<Tag> tagList = researcher.getEvernoteClient().getTags();
			String[] tagNames = new String[tagList.size()];
			for (int i = 0; i < tagList.size(); i++) {
				tagNames[i] = tagList.get(i).getName();
			}
			dispatchEvent(ResearchEvent.Type.TOPIC_LIST_LOADED, tagNames);
		} catch (Exception e) {
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

	public void registerListener(ResearchListener listener) {
		listeners.add(listener);
	}

	public void enqueueCommand(ResearchCommand command) {
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
			}
		} catch (Exception e) {
			logger.severe("Error adding topic to queue.");
		}
	}

	private class ResearchRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					String topic = researchQueue.take();
					researchTopic(topic);
				} catch (InterruptedException ie) {
					logger.severe("Thread stopped.");
					return;
				} catch (Exception e) {
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
				} catch (InterruptedException ie) {
					logger.severe("Thread stopped.");
					return;
				} catch (Exception e) {
					logger.severe("Error researching topic.");
				}
			}
		}
	}

	private boolean deleteTopic(String topic) {
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

	private boolean researchTopic(String topic) {
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

	private void dispatchEvent(ResearchEvent.Type eventType, Object data) {
		ResearchEvent event = new ResearchEvent(this, eventType, data);
		for (ResearchListener listener : listeners) {
			listener.handleMessageEvent(event);
		}
	}
}
