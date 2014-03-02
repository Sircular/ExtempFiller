package com.chs.extemp.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class ResearchMenuItem extends JMenuItem{
	private Runnable function;
	private ResearchMenuItemListener listener;
	
	public ResearchMenuItem(String name, Runnable function) {
		super(name);
		this.function = function;
		init();
	}
	
	private void init() {
		listener = new ResearchMenuItemListener(function);
		addActionListener(listener);
	}
	
	private class ResearchMenuItemListener implements ActionListener, ItemListener {
		private Runnable function;
		
		public ResearchMenuItemListener(Runnable function) {
			this.function = function;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			function.run();			
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
