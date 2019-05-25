package org.ddq.active.mq.message;

import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class CustMessageSend {
    
    /***
     * 发送客户消息
     * @param customerId
     * @param openid
     * @param gzhId
     * @param messageType
     * @param params
     */
    public void sendCustMessage(String customerId,
    		String messageType,Map<String,Object> params) {
    	Map<String,Object> param = new HashMap<String,Object>();
    	
    	param.put("customerId", customerId);
    	param.put("messageType", messageType);
    	param.put("params", params);
    	
    	this.sendInfo(param);
    }
    
    
	/***
     * 发送客户消息
     * @param customerId
     * @param openid
     * @param gzhId
     * @param messageType
     * @param params
     */
    public void sendCustMessage(String customerId,String openid,
    		String gzhId,String messageType,Map<String,Object> params) {
    	Map<String,Object> param = new HashMap<String,Object>();
    	
    	param.put("customerId", customerId);
    	param.put("openid", openid);
    	param.put("gzhId", gzhId);
    	param.put("messageType", messageType);
    	
    	param.put("params", params);
    	
    	this.sendInfo(param);
    }
    
	private void sendInfo(final Map<String,Object> map) {
		XxjrMqSendUtil.sendMessage(map, MQNames.custMessage);
    }
	
}
