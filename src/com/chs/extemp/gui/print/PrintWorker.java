package com.chs.extemp.gui.print;

import java.util.concurrent.LinkedBlockingQueue;

import com.evernote.edam.type.Note;

public class PrintWorker {
	
	private final LinkedBlockingQueue<Note> downloadQueue;
	private final LinkedBlockingQueue<PrintDocument> printQueue;
	
	private final Thread downloadThread;
	private final Thread spoolerThread;
	
	private final Runnable downloadRunnable; // we need to be able to pass it some messages
	
	public PrintWorker() {
		downloadRunnable = new DownloadRunnable();
		
		downloadThread = new Thread(downloadRunnable, "Download Thread");
		spoolerThread = new Thread(new SpoolerRunnable(), "Spooler Thread");
		
		downloadQueue = new LinkedBlockingQueue<Note>();
		printQueue = new LinkedBlockingQueue<PrintDocument>();
	}
	
	private class DownloadRunnable implements Runnable {
		
		private boolean cancelled = false;

		@Override
		public void run() {
			while(true) {
				try {
					Note note = downloadQueue.take();
					if (!cancelled) {
						String data = note.getContent();
						String title = note.getTitle();
						String tag = "";
						if (note.getTagNamesSize() > 0) {
							tag = note.getTagNames().get(0);
						}
						PrintDocument doc = new PrintDocument(data, "{0} " + title + " - " + tag);
						printQueue.add(doc);
					} else {
						while(downloadQueue.size() > 0)
							downloadQueue.take();
					}
					cancelled = false; // at this point, we've done everything that needs to be done
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void cancelDownloads() {
			this.cancelled = true;
		}
		
	}
	
	private class SpoolerRunnable implements Runnable {
		
		private boolean cancelled = false;

		@Override
		public void run() {
			while (true) {
				try {
					PrintDocument doc = printQueue.take();
					if (!this.cancelled) {
						
					} else {
						while (printQueue.size() > 0)
							printQueue.take();
					}
					this.cancelled = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void cancelJobs() {
			this.cancelled = false;
		}
		
	}
}
