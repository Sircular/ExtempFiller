package com.chs.extemp.gui;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.gui.events.ResearchEvent;
import com.evernote.edam.type.Tag;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ResearchWorker implements Runnable {

	private LinkedBlockingQueue<String> topicQueue;
	private LinkedBlockingQueue<ResearchListener> listeners;
	private Logger logger;
	private Researcher researcher;

	public ResearchWorker() {
		logger = ExtempLogger.getLogger();
		topicQueue = new LinkedBlockingQueue<String>();
		listeners = new LinkedBlockingQueue<ResearchListener>();
	}

	@Override
	public void run() {
		logger.info("Starting research thread...");
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
		while (true) {
			try {
				handleTopic();
			} catch (InterruptedException ie) {
				return;
			} catch (Exception e) {
				logger.severe("Error researching topic.");
			}
		}
	}

	public void registerListener(ResearchListener listener) {
		listeners.add(listener);
	}

	public void enqueueTopic(String topic) {
		try {
			topicQueue.add(topic);
			logger.info("Added topic to research queue.");
		} catch (Exception e) {
			logger.severe("Error adding topic to research queue.");
		}
	}

	private void handleTopic() throws InterruptedException {
		String topic = topicQueue.take();
		try {
			dispatchEvent(ResearchEvent.Type.TOPIC_RESEARCHING, topic);
			researcher.researchTopic(topic);
			dispatchEvent(ResearchEvent.Type.TOPIC_RESEARCHED, topic);
		} catch (Exception e) {
			logger.severe("Error researching topic \"" + topic + "\": " + e);
			dispatchEvent(ResearchEvent.Type.RESEARCH_ERROR, topic);
		}
	}

	private void dispatchEvent(ResearchEvent.Type eventType, Object data) {
		ResearchEvent event = new ResearchEvent(this, eventType, data);
		for (ResearchListener listener : listeners) {
			listener.handleMessageEvent(event);
		}
	}

	public void removeTopicFromQueue(String topic) {
		topicQueue.remove(topic);
	}
}
