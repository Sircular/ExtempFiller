package com.chs.extemp.gui.print;

import java.util.concurrent.LinkedBlockingQueue;

import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;

import com.chs.extemp.gui.EvernoteWorker;
import com.chs.extemp.gui.ResearchListener;
import com.chs.extemp.gui.events.ResearchEvent;
import com.evernote.edam.type.Note;

public class PrintWorker implements ResearchListener{
	
	private final LinkedBlockingQueue<Note> downloadQueue;
	private final LinkedBlockingQueue<PrintDocument> printQueue;
	
	private final Thread downloadThread;
	private final Thread spoolerThread;
	
	private final DownloadRunnable downloadRunnable; // we need to be able to pass it some messages
	private final SpoolerRunnable spoolerRunnable;
	
	private final EvernoteWorker evWorker;
	
	public PrintWorker(EvernoteWorker evWorker) {
		this.evWorker = evWorker;
		
		downloadRunnable = new DownloadRunnable();
		spoolerRunnable = new SpoolerRunnable();
		
		downloadThread = new Thread(downloadRunnable, "Download Thread");
		spoolerThread = new Thread(spoolerRunnable, "Spooler Thread");
		
		downloadQueue = new LinkedBlockingQueue<Note>();
		printQueue = new LinkedBlockingQueue<PrintDocument>();
	}
	
	public void startPrintThreads() {
		downloadThread.start();
		spoolerThread.start();
	}
	
	public boolean threadsStarted() {
		return downloadThread.isAlive() && spoolerThread.isAlive();
	}
	
	public void beginPrinting() {
		
		spoolerRunnable.beginPrinting();
	}
	
	public void cancelPrinting() {
		downloadRunnable.cancelDownloads();
		spoolerRunnable.cancelJobs();
	}
	
	@Override
	public void handleResearchEvent(ResearchEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private class DownloadRunnable implements Runnable {
		
		private boolean running = true;

		@Override
		public void run() {
			while(true) {
				try {
					Note note = downloadQueue.take();
					if (running) {
						String data = note.getContent();
						// parsing code here
						String title = note.getTitle();
						String tag = "";
						if (note.getTagNamesSize() > 0) {
							tag = note.getTagNames().get(0);
						}
						PrintDocument doc = new PrintDocument(data, "{0} " + title + " - " + tag);
						printQueue.add(doc);
					} else {
						downloadQueue.clear();
					}
					running = true; // at this point, we've done everything that needs to be done
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void cancelDownloads() {
			this.running = false;
		}
		
	}
	
	private class SpoolerRunnable implements Runnable {
		
		private boolean running = false;
		private final Object trigger = new Object(); // used as a lock
		private PrintService service = null;
		private PrintRequestAttributeSet attributes = null;

		@Override
		public void run() {
			while (true) {
				try {
					if (!running)
						synchronized (trigger) { trigger.wait(); }
					
					PrintDocument doc = printQueue.take();
					if (this.running && service != null && attributes != null) {
						doc.print(this.service, this.attributes);
					} else {
						printQueue.clear();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void setPrintSettings(PrintService service, PrintRequestAttributeSet attributes) {
			this.service = service;
			this.attributes = attributes;
		}
		
		public boolean beginPrinting() {
			if (this.service == null || this.attributes == null)
				return false;
			this.running = true;
			trigger.notify();
			return true;
		}
		
		public void cancelJobs() {
			this.running = false;
		}
		
	}
}
