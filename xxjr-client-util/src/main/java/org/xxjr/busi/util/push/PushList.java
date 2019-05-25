package org.xxjr.busi.util.push;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ddq.common.core.SpringAppContext;
import org.ddq.common.util.LogerUtil;


/**
 * 推送辅助类
 * @author Administrator
 *
 */
public class PushList {

	private static final Map<Integer, PushUtil> pushMap = new HashMap<Integer, PushUtil>();
	
	/**
	 * 获取对应的推送类
	 * @param pushType
	 * @return
	 */
	@SuppressWarnings("all")
	public static PushUtil getPushByCode(Integer pushType) {
		PushUtil pushUtil = pushMap.get(pushType);
		if (pushUtil == null) {//如果不存在就去加载类
			try {
				Map<String, Object> beans = SpringAppContext.getApplicationContext().getBeansWithAnnotation(PushCode.class);
				for (Entry<String, Object> entry : beans.entrySet()) {
					Object value = entry.getValue();
					PushCode pushCode = value.getClass().getAnnotation(PushCode.class);
					if (pushCode != null && pushCode.value() == pushType) {
						pushMap.put(pushCode.value(), (PushUtil)value);
						break;
					}
				}
				LogerUtil.log("PushList " + pushMap.get(pushType) +" class loader success。。。");
			} catch (Exception e) {
				LogerUtil.error(PushList.class, e, "PushList class loader error");
			}
		}
		return pushMap.get(pushType);
	}
}
