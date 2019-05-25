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
public class StorePcSend {
	
    

	/***
     * 发送PC数据
     * @param customerId
     * @param messageType 处理参数
     * @param recordDate 日期
     * @param params
     */
    public void sendPcMessage(String pcSignId,String messageType,Map<String,Object> params) {
    	Map<String,Object> param = new HashMap<String,Object>();
    	param.put("sessionId", pcSignId);
    	param.put("signId", pcSignId);
    	param.put("messageType", messageType);
    	param.put("customerId", params.get("customerId"));
    	param.put("success", params.get("success"));
    	param.put("cmdName", params.get("cmdName"));//指令
    	param.put("message", params.get("message"));
    	if(!StringUtils.isEmpty(params.get("userName"))){
    		param.put("userName", params.get("userName"));//用户名
    	}
    	if(!StringUtils.isEmpty(params.get("userRole"))){
    		param.put("userRole", params.get("userRole"));//用户角色
    	}
    	if(!StringUtils.isEmpty(params.get("authRole"))){
    		param.put("authRole", params.get("authRole"));//权限角色
    	}
    	if(!StringUtils.isEmpty(params.get("allOrgs"))){
    		param.put("allOrgs", params.get("allOrgs"));//管理门店
    	}
    	if(!StringUtils.isEmpty(params.get("userOrgId"))){
    		param.put("userOrgId", params.get("userOrgId"));//所属门店
    	}
    	if(!StringUtils.isEmpty(params.get("notifyType"))){
    		param.put("notifyType", params.get("notifyType"));//消息类型
    	}
    	if(!StringUtils.isEmpty(params.get("applyId"))){
    		param.put("applyId", params.get("applyId"));//订单编号
    	}
    	this.sendInfo(param);
    }
    
	private void sendInfo(final Map<String,Object> map) {
		XxjrMqSendUtil.sendFanOutMsg(map,MQNames.storePc);
	}
}
