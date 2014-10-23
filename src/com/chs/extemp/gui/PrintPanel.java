package com.chs.extemp.gui;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.chs.extemp.gui.topicview.TopicListItem;
import com.chs.extemp.gui.topicview.TopicListPanel;

@SuppressWarnings("serial")
public class PrintPanel extends JPanel {
	private TopicListPanel topicList;
	private ResearchGUI gui;
	
	public PrintPanel(ResearchGUI gui) {
		this.gui = gui;
		init();
	}
	
	private void init() {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.topicList = new TopicListPanel(this.gui);
		syncLists();	
	}
	
	private void syncLists() {
		List<TopicListItem> topics = gui.getCurrentTopicList();
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
