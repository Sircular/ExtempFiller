package com.chs.extemp.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class ResearchMenu extends JMenuBar {

	private final ResearchGUI gui;
	private JMenu fileMenu;
	private JMenu helpMenu;

	public ResearchMenu(ResearchGUI gui) {
		this.gui = gui;
		init();
	}

	private void init() {
		fileMenu = new JMenu("File");

		final ResearchMenuItem chooseFile = new ResearchMenuItem("Load Topics From File",
				new Runnable() {
			public void run() {
				gui.loadTopicsFromFile();
			}
		}
				);

		final ResearchMenuItem cancelResearch = new ResearchMenuItem("Cancel Research", new Runnable() {
			@Override
			public void run() {
				gui.cancelResearch();
			}
		});

		final ResearchMenuItem exit = new ResearchMenuItem("Exit",
		new Runnable() {
			public void run() {
				gui.dispose();
			}
		});

		fileMenu.add(chooseFile);
		fileMenu.add(cancelResearch);
		fileMenu.add(exit);
		add(fileMenu);
		
		helpMenu = new JMenu("Help");
		// not yet implemented in GUI
		/*final ResearchMenuItem help = new ResearchMenuItem("Help Contents",
		new Runnable() {
			public void run() {
				gui.showHelp();
			}
		});*/
		final ResearchMenuItem about = new ResearchMenuItem("About",
		new Runnable() {
			public void run() {
				gui.showAbout();
			}
		});
		helpMenu.add(about);
		add(helpMenu);
	}

	public void setContentsEnabled(boolean state) {
		fileMenu.setEnabled(state);
		helpMenu.setEnabled(state);
	}
}

@SuppressWarnings("serial")
class ResearchMenuItem extends JMenuItem {
	private final Runnable function;

	public ResearchMenuItem(final String name, final Runnable function) {
		super(name);
		this.function = function;
		init();
	}

	private void init() {
		addActionListener(new ResearchMenuItemListener(function));
	}

}

class ResearchMenuItemListener implements ActionListener, ItemListener {
	private final Runnable function;

	public ResearchMenuItemListener(final Runnable function) {
		this.function = function;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		function.run();
	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
	}
}
