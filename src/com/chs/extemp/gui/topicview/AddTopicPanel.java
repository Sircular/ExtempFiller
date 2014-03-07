package com.chs.extemp.gui.topicview;

import com.chs.extemp.gui.ResearchGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@SuppressWarnings("serial")
public class AddTopicPanel extends JPanel {
	private JTextField textbox;
	private JButton addButton;

	private final ResearchGUI gui;

	public AddTopicPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}

	private void init() {
		textbox = new JTextField();
		addButton = new JButton("Add Topic");
		addButton.setEnabled(false);

		textbox.addKeyListener(new KeyTypeListener(addButton, this));

		addButton.addActionListener(new AddButtonListener(this));

		setLayout(new BorderLayout());
		add(textbox, BorderLayout.CENTER);
		add(addButton, BorderLayout.LINE_END);
	}

	public void setContentsEnabled(boolean state) {
		this.textbox.setEnabled(state);
	}

	public void addTypedTopic() {
		String topic = this.textbox.getText();
		this.textbox.setText("");
		this.addButton.setEnabled(false);
		this.gui.addTopic(topic);
	}

	private class KeyTypeListener implements KeyListener {

		private final JButton addButton;
		private final AddTopicPanel addPanel;

		public KeyTypeListener(final JButton addButton, final AddTopicPanel addPanel) {
			this.addButton = addButton;
			this.addPanel = addPanel;
		}

		@Override
		public void keyPressed(final KeyEvent e) {
		}

		@Override
		public void keyReleased(final KeyEvent e) {
			String currentText = ((JTextField) e.getSource()).getText();
			// check to see if they pressed enter
			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
				if (currentText.length() > 0) {
					addPanel.addTypedTopic();
					return;
				}
			}
			this.addButton.setEnabled(currentText.length() > 0);
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	private class AddButtonListener implements ActionListener {

		private final AddTopicPanel addPanel;

		public AddButtonListener(final AddTopicPanel addPanel) {
			this.addPanel = addPanel;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			addPanel.addTypedTopic();
		}
	}

	@Override
	public boolean requestFocusInWindow() {
		return textbox.requestFocusInWindow();
	}
}
