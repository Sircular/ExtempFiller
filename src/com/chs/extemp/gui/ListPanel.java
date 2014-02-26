package com.chs.extemp.gui;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Family
 * Date: 2/5/14
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListPanel extends JPanel {
	private JScrollPane scrollpane;
	public ListPanel() {
		scrollpane = new JScrollPane();

	}

	public void init() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
}
