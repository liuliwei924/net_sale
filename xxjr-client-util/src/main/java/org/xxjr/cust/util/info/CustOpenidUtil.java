package org.xxjr.cust.util.info;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.security.MD5Util;
import org.ddq.common.util.DateUtil;
import org.llw.model.cache.RedisUtils;

public class CustOpenidUtil {
	
	/**
	 * 根据openid从缓存中读取微信信息
	 * @param openid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getWxInfo(String openid){
		return (Map<String, Object>)RedisUtils.getRedisService().get(openid);
	}

	/**
	 * 效验
	 * @param oid
	 * @param mid
	 * @return
	 */
	public static String getOpenid(String oid, String mid){
		String checkMid = MD5Util.getEncryptByKey(
				oid.substring(0, oid.length() - 9),
				"-"
						+ DateUtil.toStringByParttern(new Date(),
								DateUtil.DATE_PATTERN_YYYY_MM_DD));
		if(checkMid.equals(mid)){
			return oid;
		}
		return null;
	}
	
	/**
	 * 登出后解除绑定
	 * @param customerId
	 * @return
	 */
	public static AppResult clearOpenid(Object customerId){
		AppParam param = new AppParam();
		param.addAttr("customerId", customerId);
		param.setService("customerService");
		param.setMethod("clearOpenid");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(param);
	}
	
	
}
