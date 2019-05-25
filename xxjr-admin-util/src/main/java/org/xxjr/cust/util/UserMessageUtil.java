package org.xxjr.cust.util;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.xxjr.sys.util.message.MessageConstants;
import org.xxjr.sys.util.message.MessageTemplateUtil;

public class UserMessageUtil {
	
	
	/**
	 *添加用户消息
	 * @param params
	 * @param messageKey
	 * @return
	 */
	public static AppResult sendAdminMessage(Map<String, Object> param,String messageKey) {
		
		Map<String, Object> msgConfig = MessageTemplateUtil.getMessageTemplate(messageKey);
		String messageContent = (String) msgConfig.get(MessageConstants.KEY_messageContent);
	
		AppParam messageInfo  = new AppParam();
		param.put("createDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM));
		messageInfo.addAttrs(param);
		messageContent = MessageTemplateUtil.getMessageContent(param, messageContent);
		//添加站内信息
		AppParam addMessage  = new AppParam();
		addMessage.setMethod("insert");
		addMessage.setService("userMessageService");
		addMessage.addAttr("msgTo", param.get("userName"));
		addMessage.addAttr("userId", param.get("userId"));
		addMessage.addAttr("msgFrom", param.get("sendBy")==null?"admin":param.get("sendBy"));
		addMessage.addAttr("sendTime", new Date());
		addMessage.addAttr("subject", msgConfig.get(MessageConstants.KEY_messageTitle));
		addMessage.addAttr("content", messageContent);
		addMessage.addAttr("flag", MessageConstants.messageFlag_1);
		addMessage.addAttr("sendFlag", "0");
		addMessage.addAttr("sendParams", MessageTemplateUtil.getParamByMap(param));
		addMessage.addAttr("messageType", messageKey);
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("userMessageService") == null) {
			addMessage.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
			result = RemoteInvoke.getInstance().call(addMessage);
		}else{
			result = SoaManager.getInstance().invoke(addMessage);
		}
		return result;
	}
	
	
	

	/***
	 *  修改状态
	 * @param messageId
	 * @param status
	 */
	public static void updateStatus(Object messageId,int status){
		AppParam updateParam = new AppParam();
		updateParam.setService("userMessageService");
		updateParam.setMethod("update");
		updateParam.addAttr("messageId", messageId);
		updateParam.addAttr("resendTime", new Date());
		updateParam.addAttr("sendFlag", status+"");
		if (SpringAppContext.getBean("userMessageService") == null) {
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
			RemoteInvoke.getInstance().call(updateParam);
		}else{
			SoaManager.getInstance().invoke(updateParam);
		}
		
	}
	
}
