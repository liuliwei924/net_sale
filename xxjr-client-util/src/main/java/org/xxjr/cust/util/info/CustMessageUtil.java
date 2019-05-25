package org.xxjr.cust.util.info;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.xxjr.sys.util.message.MessageConstants;

public class CustMessageUtil {

	public static AppResult delete(String messageId, String customerId) {
		AppParam param = new AppParam();
		param.setService("custMessageService");
		param.setMethod("update");
		param.addAttr("customerId", customerId);
		param.addAttr("flag", MessageConstants.messageFlag_3);
		param.addAttr("messageId", messageId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().callNoTx(param);
	}
	
	/**
	 * 批量删除
	 * @param messageIds
	 * @param customerId
	 * @return
	 */
	public static AppResult batchDel(String messageIds, String customerId) {
		AppParam param = new AppParam();
		param.setService("custMessageService");
		param.setMethod("batchDel");
		param.addAttr("customerId", customerId);
		param.addAttr("messageIds", messageIds);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().callNoTx(param);
	}

	public static Map<String, Object> queryInfo(AppParam params) {
		params.setService("custMessageService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result.getRows().size() > 0 ? result.getRow(0) : result
				.getAttr();
	}

	public static AppResult list(AppParam params) {
		params.setService("custMessageService");
		params.setMethod("queryByPage");
		params.setOrderBy("sendTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	/**
	 * 修改站内信阅读状态
	 * @param custId
	 * @param messageId
	 * @return
	 */
	public static AppResult updateShow(String custId, String messageId) {
		AppParam params = new AppParam();
		params.addAttr("customerId", custId);
		params.addAttr("messageId", messageId);
		params.setService("custMessageService");
		params.setMethod("updateShow");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

	public static AppResult queryMsgCount(String custId) {
		AppParam params = new AppParam();
		params.addAttr("customerId", custId);
		params.setService("custMessageService");
		params.setMethod("queryMsgCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}
}
