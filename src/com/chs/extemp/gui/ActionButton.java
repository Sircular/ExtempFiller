package com.chs.extemp.gui;

import java.awt.event.ActionListener;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ActionButton extends JButton {
	public ActionButton(final String title, final ActionListener action) {
		super(title);
		this.addActionListener(action);
	}
}
