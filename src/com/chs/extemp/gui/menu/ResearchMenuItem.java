package com.chs.extemp.gui.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

@SuppressWarnings("serial")
public class ResearchMenuItem extends JMenuItem {
	private final Runnable function;

	public ResearchMenuItem(final String name, final Runnable function) {
		super(name);
		this.function = function;
		init();
	}

	private void init() {
		addActionListener(new ResearchMenuItemListener(function));
	}

	private class ResearchMenuItemListener implements ActionListener, ItemListener {
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
}
