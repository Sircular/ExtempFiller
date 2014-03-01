package com.chs.extemp.gui;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.chs.extemp.ExtempLogger;
import com.chs.extemp.Researcher;

public class ResearchWorker implements Runnable{
	
	private LinkedBlockingQueue<String> topicQueue;
	private Logger logger;
	private Researcher researcher;

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
				logger.severe("Error researching topic. Retrying...");
			}
		}
	}
	
	public void enqueue(String topic) {
		try { 
			topicQueue.add(topic);
		} catch (Exception e) {
			// let's just not let anyone know this happened...
		}
	}
	
	private void handleTopic() throws InterruptedException {
		String topic = topicQueue.take();
		try {
			researcher.researchTopic(topic);
		} catch (Exception e) {
			logger.severe("Error researching topic \"" + topic + "\": " + e);
		}
	}

}
