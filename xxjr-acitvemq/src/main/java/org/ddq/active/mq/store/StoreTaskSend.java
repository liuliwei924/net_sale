package org.ddq.active.mq.store;

import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class StoreTaskSend {


	/***
     * 添加业务员数据
     * @param customerId
     * @param messageType 处理参数
     * @param recordDate 日期
     * @param params
     */
    public void sendStoreMessage(String customerId,String messageType,Map<String,Object> params) {
    	Map<String,Object> param = new HashMap<String,Object>();
    	param.put("customerId", customerId);
    	param.put("messageType", messageType);
    	param.put("dealParams", params);
    	
    	this.sendInfo(param);
    }
    
	private void sendInfo(final Map<String,Object> map) {
		XxjrMqSendUtil.sendMessage(map, MQNames.storeTask);
	}
	
}
