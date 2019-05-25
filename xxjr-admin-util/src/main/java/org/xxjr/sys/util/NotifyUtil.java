package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

/**
 * app版本升级通知
 * @author Administrator
 *
 */
public class NotifyUtil {
	
	public final static String APP_VERSION_NOTIFY_INFO = "app_notify_info_";

	/**
	 * 根据版本号查询通知信息
	 * @param appVersion
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getNotifyByAppVersion(String appVersion){
		Map<String, Object> notifyInfo = (Map<String, Object>) RedisUtils.getRedisService().get(
				APP_VERSION_NOTIFY_INFO + appVersion);
		if (notifyInfo == null) {
			notifyInfo = refreshNotifyInfo(appVersion);
		}
		return notifyInfo;
	}
	 
	/**
	 * 
	 * @param appVersion
	 * @return
	 */
	public static Map<String, Object> refreshNotifyInfo(Object appVersion) {
		Map<String, Object> notifyInfo = new HashMap<String, Object>();
		if (StringUtils.isEmpty(appVersion)) {
			return notifyInfo;
		}
		AppParam param = new AppParam("notifyConfigService", "query");
		param.addAttr("appVersion", appVersion);
		AppResult result = null;
		if (SpringAppContext.getBean("notifyConfigService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			notifyInfo = result.getRow(0);
			RedisUtils.getRedisService().set(APP_VERSION_NOTIFY_INFO + appVersion, (Serializable)notifyInfo, 60 * 60 * 48);
		}
		return notifyInfo;
	}

	/**
	 * 删除缓存
	 * @param appVersion
	 */
	public static void delNotifyInfo(Object appVersion) {
		RedisUtils.getRedisService().del(APP_VERSION_NOTIFY_INFO + appVersion);
	}
}
	
