package com.chs.extemp.gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class AddTopicPanel extends JPanel{
	private JTextField textbox;
	private JButton button;
	
	public AddTopicPanel() {
		init();
	}
	
	private void init() {
		textbox = new JTextField();
		button = new JButton("Add Topic");
		button.setEnabled(false);
		
		setLayout(new BorderLayout());
		add(textbox, BorderLayout.CENTER);
		add(button, BorderLayout.LINE_END);
		
	}

}
