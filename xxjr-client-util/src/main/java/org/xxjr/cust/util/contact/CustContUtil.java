package org.xxjr.cust.util.contact;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;

public class CustContUtil {

	public static AppResult queryContShow(AppParam params) {
		params.setService("custContactService");
		params.setMethod("queryShow");
		params.setOrderBy("createTime");
		params.setOrderValue("DESC");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	public static Map<String, Object> queryContInfo(AppParam params) {
		params.setService("custContactService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result.getRows().size() > 0 ? result.getRow(0) : result
				.getAttr();
	}

	public static AppResult insertContInfo(AppParam params) {
		params.setService("custContactService");
		params.setMethod("insert");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

	public static AppResult updateContInfo(AppParam params) {
		params.setService("custContactService");
		params.setMethod("update");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

	public static AppResult deleteContInfo(String contactId, String customerId) {
		AppParam params = new AppParam();
		params.setService("custContactService");
		params.setMethod("delete");
		params.addAttr("contactId", contactId);
		params.addAttr("customerId", customerId);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

	public static AppResult queryTagShow(AppParam params) {
		params.setService("custTagService");
		params.setMethod("queryShow");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	public static Map<String, Object> queryTagInfo(AppParam params) {
		params.setService("custTagService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result.getRows().size() > 0 ? result.getRow(0) : result
				.getAttr();
	}

	public static AppResult insertTagInfo(AppParam params) {
		params.setService("custTagService");
		params.setMethod("insert");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

	public static AppResult updateTagInfo(AppParam params) {
		params.setService("custTagService");
		params.setMethod("update");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

	public static AppResult deleteTagInfo(String tagId, String customerId) {
		AppParam params = new AppParam();
		params.setService("custTagService");
		params.setMethod("delete");
		params.addAttr("tagId", tagId);
		params.addAttr("customerId", customerId);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}

}
