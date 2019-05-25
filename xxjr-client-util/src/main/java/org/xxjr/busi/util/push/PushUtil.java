package org.xxjr.busi.util.push;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

public interface PushUtil {
	public int pushData(Map<String, Object> row, Integer pushType);
	
	/**
	 * 记录推送信息，记录未打开 或者 限制条件不通过的情况，方便之后维护数据
	 * @param row 申请信息
	 * @param pushType 推送第三方
	 * @param status 状态
	 * @param message 自定义的错误信息
	 */
	default void addPushRecord(Map<String, Object> row, Object pushType, Object status, Object message){
		LogerUtil.log(getClass(), message);
		AppParam logParam = new AppParam("borrowApplyPushService", "insert");
		logParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		logParam.addAttrs(row);
		logParam.addAttr("status", status);
		logParam.addAttr("pushType", pushType);
		logParam.addAttr("message", message);
		logParam.addAttr("sourceType", NumberUtil.getInt(row.get("sourceType"), 0));
		try {
			RemoteInvoke.getInstance().call(logParam);
		} catch (Exception e) {
			LogerUtil.error(PushUtil.class, e, "PushUtil addPushRecord error");
		}
	}
}
