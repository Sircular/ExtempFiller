package com.chs.extemp.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.chs.extemp.gui.topicview.TopicList;
import com.chs.extemp.gui.topicview.TopicListItem;

@SuppressWarnings("serial")
public class PrintPanel extends JPanel {
	private TopicList topicList;
	private JScrollPane topicListScroll;
	private ResearchGUI gui;
	
	public PrintPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}
	
	private void init() {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.setLayout(new BorderLayout());
		
		this.topicList = new TopicList();
		
		topicListScroll = new JScrollPane(topicList);
		topicListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		topicListScroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 10, 0), new EtchedBorder()));
		
		this.add(topicListScroll, BorderLayout.CENTER);
		syncLists();	
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
}
