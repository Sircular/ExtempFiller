package com.chs.extemp.gui.print;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.chs.extemp.gui.ResearchGUI;
import com.chs.extemp.gui.topicview.TopicList;
import com.chs.extemp.gui.topicview.TopicListItem;

@SuppressWarnings("serial")
public class PrintPanel extends JPanel {
	private TopicList topicList;
	private JScrollPane topicListScroll;
	private JButton printButton;
	
	private ResearchGUI gui;
	private PrintWorker prWorker;
	
	public PrintPanel(ResearchGUI gui, PrintWorker prWorker) {
		this.gui = gui;
		this.prWorker = prWorker;
		init();
	}
	
	private void init() {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.setLayout(new BorderLayout());
		
		this.topicList = new TopicList();
		
		this.topicListScroll = new JScrollPane(topicList);
		this.topicListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.topicListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.topicListScroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 10, 0), new EtchedBorder()));
		
		this.printButton = new JButton("Print");
		this.printButton.setEnabled(false); // only enabled once we select something
		
		this.topicList.addListSelectionListener(new PrintButtonListener(printButton));
		
		this.add(topicListScroll, BorderLayout.CENTER);
		syncLists();	
	}
	
	public void setContentsEnabled(boolean value) {
		this.printButton.setEnabled(value && this.topicList.getSelectedTopicsList() != null &&
				this.topicList.getSelectedTopicsList().size() != 0);
		this.topicList.setEnabled(value);
	}
	
	public void syncLists() {
		List<TopicListItem> topics = gui.getCurrentTopicList();
		if (topics == null) // no topics to load yet
			return;
		for (TopicListItem t : topics) {
			if(!this.topicList.hasTopic(t.getTopic()) && 
					t.getState() == TopicListItem.State.RESEARCHED) { // we want to make sure that the research is done
				this.topicList.addTopic(t.getTopic(), TopicListItem.State.NOT_QUEUED_FOR_PRINTING);
			}
		}
		for (TopicListItem t : this.topicList.getTopics()) {
			for(TopicListItem t2 : topics) { // I don't like that I need a double loop
				if(t.getTopic().equals(t2.getTopic()))
					break;
				if(t2 == topics.get(topics.size()-1))
					this.topicList.removeTopic(t.getTopic());
			}
		}
	}
	
	private class PrintButtonListener implements ActionListener, ListSelectionListener {
		
		private JButton printButton;
		
		public PrintButtonListener(JButton printButton) {
			this.printButton = printButton;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			TopicList topicList = (TopicList)e.getSource();
			printButton.setEnabled(topicList.getSelectedTopicsList().size() > 0);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			prWorker.beginPrinting();	
		}
		
	}
}
