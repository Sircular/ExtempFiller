package com.chs.extemp.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;
import com.chs.extemp.gui.messaging.MessageEvent;
import com.chs.extemp.gui.messaging.MessageEventListener;

public class ResearchWorker implements Runnable{
	
	private LinkedBlockingQueue<String> topicQueue;
	private Logger logger;
	private Researcher researcher;
	
	private List<MessageEventListener> listeners;
	
	public ResearchWorker() {
		listeners = new ArrayList<MessageEventListener>();
	}

	@Override
	public void run(){
		logger = ExtempLogger.getLogger();
		logger.info("Starting research thread...");
		
		topicQueue = new LinkedBlockingQueue<String>();
		researcher = new Researcher();
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
	
	public void enqueue(String topic) {
		try { 
			topicQueue.add(topic);
			logger.info("Added topic to research queue.");
		} catch (Exception e) {
			logger.severe("Error adding topic to research queue.");
		}
	}
	
	public void addListener(MessageEventListener l) {
		listeners.add(l);
	}
	
	private void handleTopic() throws InterruptedException {
		String topic = topicQueue.take();
		try {
			researcher.researchTopic(topic);
			dispatchEvent(MessageEvent.Type.ERROR, topic);
		} catch (Exception e) {
			logger.severe("Error researching topic \"" + topic + "\": " + e);
		}
	}
	
	private void dispatchEvent(MessageEvent.Type eventType, Object data) {
		MessageEvent event = new MessageEvent(this, eventType, data);
		for(int i = 0; i < listeners.size(); i++) {
			listeners.get(i).handleMessageEvent(event);
		}
	}

}
