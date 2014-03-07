package com.chs.extemp.gui.menu;

import com.chs.extemp.gui.ResearchGUI;

import javax.swing.*;

@SuppressWarnings("serial")
public class ResearchMenuBar extends JMenuBar {

	private final ResearchGUI gui;
	private JMenu fileMenu;

	public ResearchMenuBar(ResearchGUI gui) {
		this.gui = gui;
		init();
	}

	private void init() {
		fileMenu = new JMenu("File");

		ResearchMenuItem chooseFile = new ResearchMenuItem("Load Topics From File",
				new Runnable() {
					public void run() {
						gui.loadTopicsFromFile();
					}
				}
		);

		ResearchMenuItem exit = new ResearchMenuItem("Exit",
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

	public void setContentsEnabled(boolean state) {
		fileMenu.setEnabled(state);
	}
}
