package com.chs.extemp.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.gui.messaging.ResearchMessage;
import com.chs.extemp.gui.messaging.ResearchMessageListener;
import com.evernote.edam.type.Tag;

public class ResearchWorker implements Runnable{
	
	private LinkedBlockingQueue<String> topicQueue;
	private Logger logger;
	private Researcher researcher;
	
	private List<ResearchMessageListener> listeners;
	
	public ResearchWorker() {
		listeners = new ArrayList<ResearchMessageListener>();
	}

	@Override
	public void run(){
		logger = ExtempLogger.getLogger();
		logger.info("Starting research thread...");
		
		topicQueue = new LinkedBlockingQueue<String>();
		researcher = new Researcher();
		
		// check to see if the initialization was
		// successful; no need to do anything more
		// if not.
		
		if(!researcher.isUsable()) {
			dispatchEvent(ResearchMessage.Type.EVERNOTE_CONNECTION_ERROR, null);
			return;
		}
		
		// get the list of already researched tags
		// and send them to the GUI so they can be
		// added to the list.
		List<Tag> taglist = researcher.getCurrentTags();
		String[] tagnames = new String[taglist.size()];
		for(int i = 0; i < taglist.size(); i++) {
			tagnames[i] = taglist.get(i).getName();
		}
		
		dispatchEvent(ResearchMessage.Type.TOPIC_LIST, tagnames);
		
		if(!researcher.isUsable()) {
			return;
		}
		while(true) {
			try {
				handleTopic();
			} catch (Exception e) {
				logger.severe("Error researching topic.");
			}
		}
	}
	
	public void enqueueRequest(String topic) {
		try { 
			topicQueue.add(topic);
			logger.info("Added topic to research queue.");
		} catch (Exception e) {
			logger.severe("Error adding topic to research queue.");
		}
	}
	
	public void addListener(ResearchMessageListener l) {
		listeners.add(l);
	}
	
	private void handleTopic() throws InterruptedException {
		String topic = topicQueue.take();
		try {
			researcher.researchTopic(topic);
			dispatchEvent(ResearchMessage.Type.TOPIC_RESEARCHED, topic);
		} catch (Exception e) {
			logger.severe("Error researching topic \"" + topic + "\": " + e);
			dispatchEvent(ResearchMessage.Type.RESEARCH_ERROR, topic);
		}
	}
	
	private void dispatchEvent(ResearchMessage.Type eventType, Object data) {
		ResearchMessage event = new ResearchMessage(this, eventType, data);
		for(int i = 0; i < listeners.size(); i++) {
			listeners.get(i).handleMessageEvent(event);
		}
	}

}
