package com.chs.extemp.gui;

<<<<<<< HEAD
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
		
		
=======
import javax.swing.*;
import java.awt.event.ActionListener;

public class ActionButton extends JButton {
	public ActionButton(final String title, final ActionListener action) {
		super(title);
		this.addActionListener(action);
>>>>>>> a3a81ea0023f73a21c9d39a3ac672d145a71c925
	}
}
