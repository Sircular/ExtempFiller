package com.chs.extemp.gui.messaging;

import java.util.EventListener;

public interface MessageEventListener extends EventListener{
	void handleMessageEvent(MessageEvent e);
}
