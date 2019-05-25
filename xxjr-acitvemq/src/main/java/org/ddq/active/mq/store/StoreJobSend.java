package org.ddq.active.mq.store;

import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Lazy
@Component
public class StoreJobSend {
	
    public StoreJobSend() {

	}
    

	/***
     * 发送job数据
     * @param jobId
     * @param messageType 消息类型
     * @param params 处理参数
     */
    public void sendJobMessage(String jobId,String messageType,Map<String,Object> params) {
    	Map<String,Object> param = new HashMap<String,Object>();
    	param.put("jobId", jobId);
    	param.put("messageType", messageType);
    	param.put("dealParams", params);
    	this.sendInfo(param);
    }
    
    private void sendInfo(final Map<String,Object> map) {
    	XxjrMqSendUtil.sendMessage(map, MQNames.resetJob);
	 }
}
