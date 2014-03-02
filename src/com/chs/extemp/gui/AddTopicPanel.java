package com.chs.extemp.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class AddTopicPanel extends JPanel{
	private JTextField textbox;
	private JButton addButton;
	
	private ExtempFillerGUI gui;
	
	public AddTopicPanel(ExtempFillerGUI gui) {
		this.gui = gui;
		init();
	}
	
	private void init() {
		textbox = new JTextField();
		addButton = new JButton("Add Topic");
		addButton.setEnabled(false);
		
		KeyTypeListener keyListener = new KeyTypeListener(addButton);
		textbox.addKeyListener(keyListener);
		
		AddButtonListener addListener = new AddButtonListener(this);
		addButton.addActionListener(addListener);
		
		setLayout(new BorderLayout());
		add(textbox, BorderLayout.CENTER);
		add(addButton, BorderLayout.LINE_END);
		
	}
	
	public void addTypedTopic() {
		String topic = this.textbox.getText();
		this.textbox.setText("");
		this.addButton.setEnabled(false);
		this.gui.addTopic(topic);
	}
	
	private class KeyTypeListener implements KeyListener {
		
		private JButton addButton;
		
		public KeyTypeListener(JButton addButton) {
			this.addButton = addButton;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// not used		
		}

		@Override
		public void keyReleased(KeyEvent e) {
			String currentText = ((JTextField)e.getSource()).getText();
			this.addButton.setEnabled(currentText.length() > 0);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// not used			
		}
		
	}
	
	private class AddButtonListener implements ActionListener {

		private AddTopicPanel addPanel;
		
		public AddButtonListener(AddTopicPanel addPanel) {
			this.addPanel = addPanel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.addPanel.addTypedTopic();
		}
		
	}

}
