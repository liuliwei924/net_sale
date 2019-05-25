package org.ddq.active.mq.activity;

import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


/***
 * 奖励管理 发送消息
 * @author qinxcb
 *
 */
@Lazy
@Component
public class MqActivetitySend {
	
	
    public void sendInfo(final Map<String,Object> map) {
    	XxjrMqSendUtil.sendMessage(map, MQNames.custActive);
    }
}
