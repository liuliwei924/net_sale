package org.ddq.active.mq.store;

import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.MQNames;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Lazy
@Component
public class StoreAppSend {
	/***
     * 发送App数据
     * @param customerId
     * @param messageType 处理参数
     * @param recordDate 日期
     * @param params
     */
    public void sendAppMessage(String appSignId,String messageType,Map<String,Object> params) {
    	Map<String,Object> param = new HashMap<String,Object>();
    	param.put("uuid", appSignId);
    	param.put("messageType", messageType);
    	param.put("customerId", params.get("customerId"));
    	param.put("message", params.get("message"));
    	param.put("success", params.get("success"));
    	param.put("cmdName", params.get("cmdName"));//指令 
    	if(!StringUtils.isEmpty(params.get("telephone"))){
    		param.put("telephone", params.get("telephone"));//手机号
    	}
    	if(!StringUtils.isEmpty(params.get("notifyType"))){ //消息类型
    		param.put("notifyType", params.get("notifyType"));
    	}
    	this.sendInfo(param);
    }
    
	private void sendInfo(final Map<String,Object> map) {
		XxjrMqSendUtil.sendFanOutMsg(map, MQNames.storeApp);
	}
}
