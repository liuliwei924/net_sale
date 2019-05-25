package org.ddq.active.mq.message;

import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 转单处理
 * @author Administrator
 *
 */
@Lazy
@Component
public class TransferSaleSend {

	 /**
     * 发送消息
     * @param params
     */
    public void sendExecuteMessage(Map<String,Object> params) {
    	this.sendInfo(params);
    }
    
	private void sendInfo(final Map<String,Object> map) {
		XxjrMqSendUtil.sendMessage(map, MQNames.transferSale);
    
    }
	
}
