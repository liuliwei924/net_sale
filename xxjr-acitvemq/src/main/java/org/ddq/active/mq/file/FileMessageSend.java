package org.ddq.active.mq.file;

import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class FileMessageSend {

	public void sendInfo(final Map<String, Object> map) {
		XxjrMqSendUtil.sendMessage(map, MQNames.fileUpload);
	}
	
}
