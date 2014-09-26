package com.chs.extemp.gui.settings;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
// import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.chs.extemp.gui.ResearchGUI;
import com.chs.extemp.gui.events.SettingsEvent;
import com.chs.extemp.gui.events.SettingsEvent.Type;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {
	
	private final ResearchGUI gui;
	
	private JSlider sourceCountSlider;
	private JCheckBox uploadImageBox;
	
	public SettingsPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}
	
	// just habit at this point, it makes no sense
	public void init() {
		//aesthetic stuff, although the controls should never approach the edge anyway.
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		sourceCountSlider = new JSlider(3, 28, 12);
		sourceCountSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int val = sourceCountSlider.getValue();
				gui.handleSettingsEvent(new SettingsEvent(sourceCountSlider, 
						Type.MAX_SOURCES_SET, val)); // YAAAY AUTOBOXING
			}
		});
		this.add(new JLabel("Number of Articles to Research"));
		this.add(sourceCountSlider);
		
		this.add(Box.createVerticalStrut(20)); // to visually separate these items
		
		uploadImageBox = new JCheckBox("Upload Images (Not Yet Implemented)", false);
		// this is not yet used, but eventually will be when uploading images is possible
		uploadImageBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				boolean val = uploadImageBox.isSelected();
				gui.handleSettingsEvent(new SettingsEvent(uploadImageBox,
						Type.UPLOAD_IMAGES_SET, val));
			}
			
		});
		uploadImageBox.setEnabled(false); // functionality not yet implemented
		this.add(uploadImageBox);
	}
	
	// convenience function to quickly make nice-looking layout
	// not used at present, may be removed in the future
	/*private JPanel createLayoutPanel(int spacing, JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		
		for (int i = 0; i < components.length; i++) {
			if (i > 0) // we don't want spacing at the beginning, silly!
				panel.add(Box.createHorizontalStrut(spacing));
			panel.add(components[i]);
		}
		
		return panel;
	}*/

}
