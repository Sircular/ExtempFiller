package com.chs.extemp.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class TopicPanel extends JPanel{
	
	public TopicPanel() {
		init();
	}
	
	private void init() {
		// set up some basic aesthetic stuff
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// set up the layout manager
		this.setLayout(new BorderLayout());
		
		ListPanel lp = new ListPanel();
		add(lp, BorderLayout.CENTER);
		AddTopicPanel atp = new AddTopicPanel();
		add(atp, BorderLayout.PAGE_END);
	}

}
