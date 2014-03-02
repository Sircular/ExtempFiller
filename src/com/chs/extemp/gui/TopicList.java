package com.chs.extemp.gui;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * Created with IntelliJ IDEA.
 * User: Family
 * Date: 2/5/14
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicList extends JList {
	
	private DefaultListModel listModel;
	private ListSelectionModel selectionModel;
	
	public TopicList() {
		init();
		
	}

	private void init() {
		this.listModel = new DefaultListModel();
		this.selectionModel = getSelectionModel();
	}
	
	public void addTopic(String topic) {
		listModel.addElement(new TopicListItem(topic, TopicListItem.State.NOT_RESEARCHED));
		this.setModel(listModel);
	}
	
	public void addTopic(String topic, TopicListItem.State state) {
		listModel.addElement(new TopicListItem(topic, state));
		this.setModel(listModel);
	}
	
	public void removeTopic(String topic) {
		// as yet unimplemented on the server side,
		// so not implemented on the client side.
		// Currently displays a message box saying
		// as much.
		
		JOptionPane.showMessageDialog(null, "Deletion of a topic through the client is not yet implemented.\n\n"+
				"Please go to evernote.com, sign into the web interface, and delete " +
				"the data manually.");
	}
	
	public void setTopicResearched(String topic, TopicListItem.State state) {
		for(int i = 0; i < this.listModel.size(); i++) {
			TopicListItem item = (TopicListItem)this.listModel.get(i);
			if(item.getTopic() == topic) {
				item.setState(state);
			}
		}
	}
	
	public void addSelectionListener(ListSelectionListener listener) {
		this.selectionModel.addListSelectionListener(listener);
	}
}
