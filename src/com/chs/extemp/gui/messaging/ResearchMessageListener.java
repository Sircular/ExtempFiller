package com.chs.extemp.gui.messaging;

import java.util.EventListener;

public interface ResearchMessageListener extends EventListener{
	void handleMessageEvent(ResearchMessage e);
}
