package com.chs.extemp.gui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class PrintPanel extends JPanel{
	public PrintPanel() {
		init();
	}
	
	private void init() {
		setBorder(new EmptyBorder(10, 10, 10, 10));
	}
}
