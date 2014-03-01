package com.chs.extemp.gui;

import java.util.logging.Logger;

import javax.swing.*;

import com.chs.extemp.ExtempLogger;

public class ExtempFillerGUI extends JFrame{
	public static final int GUI_WIDTH  = 800;
	public static final int GUI_HEIGHT = 600;
	
	public ResearchWorker researchWorker;
	
	public ExtempFillerGUI() {
		init();
		pack();
		setVisible(true);
		
		Logger logger = ExtempLogger.getLogger();
		
		
	}
	public void init() {
		// initialize research worker
				researchWorker = new ResearchWorker();
				Thread researchThread = new Thread(researchWorker);
				researchThread.start();
		
		// Tries to make the app look native
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setTitle("CHS Extemp Filler");
		
		// set up some tabs
		JTabbedPane tabs = new JTabbedPane();
		
		DebugPanel debugpanel = new DebugPanel(GUI_WIDTH, GUI_HEIGHT);
		tabs.addTab("Extemp Filler", null);
		tabs.addTab("Debug", debugpanel);
		add(tabs);
	}
}
