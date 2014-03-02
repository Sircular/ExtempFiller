package com.chs.extemp.gui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import com.chs.extemp.gui.ResearchGUI;

@SuppressWarnings("serial")
public class ResearchMenuBar extends JMenuBar{
	
	private ResearchGUI gui;
	
	public ResearchMenuBar(ResearchGUI gui) {
		this.gui = gui;
		init();
	}
	
	private void init() {
		JMenu fileMenu = new JMenu("File");
		
		// if anyone can suggest an alternative
		// to anonymous runnables that requires
		// no API and no special classes, then 
		// I will adjust the code.
		
		// Actually, if I were using JDK8, I
		// would have used lambdas here. Oh
		// well. Too bad that's not standard
		// yet.
		
		ResearchMenuItem chooseFile = new ResearchMenuItem ("Load Topics From File",
				new Runnable() {
					public void run() {
						gui.loadTopicsFromFile();
					}
				}
		);
		
		ResearchMenuItem exit = new ResearchMenuItem ("Exit",
				new Runnable() {
					public void run() {
						gui.dispose();
					}
				}
		);
		
		fileMenu.add(chooseFile);
		fileMenu.add(exit);
		add(fileMenu);
	}

}
