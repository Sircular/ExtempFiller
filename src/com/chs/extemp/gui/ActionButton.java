package com.chs.extemp.gui;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ActionButton extends JButton {
	public ActionButton(final String title, final ActionListener action) {
		super(title);
		this.addActionListener(action);
	}
}
