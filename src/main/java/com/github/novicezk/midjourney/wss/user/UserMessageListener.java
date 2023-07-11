package com.github.novicezk.midjourney.wss.user;


import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.wss.handle.ImagineMessageHandler;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import com.github.novicezk.midjourney.wss.handle.ZoomMessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class UserMessageListener implements ApplicationListener<ApplicationStartedEvent> {
	@Resource
	private ProxyProperties properties;
	private final List<MessageHandler> messageHandlers = new ArrayList<>();
	private ZoomMessageHandler zoomMessageHandler;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		Collection<MessageHandler> handlers = event.getApplicationContext().getBeansOfType(MessageHandler.class).values();
		messageHandlers.addAll(handlers);
		for (MessageHandler handler : handlers) {
			if (handler instanceof ZoomMessageHandler) {
				zoomMessageHandler = (ZoomMessageHandler) handler;
			} else {
				messageHandlers.add(handler);
			}
		}
	}

	public void onMessage(DataObject raw) {
		MessageType messageType = MessageType.of(raw.getString("t"));
		if (messageType == null || MessageType.DELETE == messageType) {
			return;
		}
		DataObject data = raw.getObject("d");
		if (ignoreAndLogMessage(data, messageType)) {
			return;
		}
		if (ZoomMessageHandler.isZoom(raw.toString())) {
			zoomMessageHandler.handle(messageType, data);
			return;
		}
		for (MessageHandler messageHandler : this.messageHandlers) {
			messageHandler.handle(messageType, data);
		}
	}

	private boolean ignoreAndLogMessage(DataObject data, MessageType messageType) {
		String channelId = data.getString("channel_id");
		if (!this.properties.getDiscord().getChannelId().equals(channelId)) {
			return true;
		}
		String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
		log.debug("{} - {}: {}", messageType.name(), authorName, data.opt("content").orElse(""));
		return false;
	}

}