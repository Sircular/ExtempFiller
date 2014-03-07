package com.chs.extemp.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ActionButton extends JButton{
	
	public ActionButton(String title, Runnable function) {
		super(title);
		ActionButtonListener listener = new ActionButtonListener(function);
		addActionListener(listener);
	}
	
	private class ActionButtonListener implements ActionListener{
		private Runnable function;
		
		public ActionButtonListener(Runnable function) {
			this.function = function;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			function.run();
		}
		
		
	}
}
