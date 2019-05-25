package org.xxjr.cust.util.info;

import java.util.UUID;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.security.MD5Util;
import org.ddq.common.security.md5.Md5;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustTokenConstant;

public class CustomerPwdUtil {
	/** 用户sessionKey **/
	private static String CacheKey_SESSIONKEY = "CustSessionKey_";
	
	/**用户sessionKey缓存时间为10天**/
	public static Integer USER_SESSIONKEY_TIME = 10*24*60*60;
	
	/** 用户密码登录错误次数 **/
	private static String CacheKey_CUST_LOGIN_ERROR_COUNT = "CustLoginErrorCount_";
	
	/** 用户密码验证状态*/
	public static String VERIFYSTATUS ="verifyStatus";
	/** 用户密码验证状态  - 没有设置密码*/
	public static int VERIFY_STATUS_NO_PASS = 0;
	/** 用户密码验证状态 - 验证成功*/
	public static int VERIFY_STATUS_SUCCESS = 1;
	/** 用户密码验证状态  - 验证失败*/
	public static int VERIFY_STATUS_FAIL = 2;
	/** 用户密码验证状态  - 用户不存在*/
	public static int VERIFY_STATUS_NO_USER = 3;
	
	/**
	 * 获取用户登录错误次数
	 * @param telephone
	 * @return
	 */
	public static int getCustLoginErrorCount(String telephone){
		Object errCount = RedisUtils.getRedisService().get(
				CacheKey_CUST_LOGIN_ERROR_COUNT + telephone);
		return errCount == null ? 0 : (int) errCount;
	}
	
	public static void setCustLoginErrorCount(String telephone){
		Object errCount =  RedisUtils.getRedisService().get(CacheKey_CUST_LOGIN_ERROR_COUNT+telephone);
		RedisUtils.getRedisService().set(CacheKey_CUST_LOGIN_ERROR_COUNT+telephone,errCount == null ? 1 : (int) errCount+1,60*60*24);
	}

	/**
	 * 获取用户 密码sessioKey
	 * 
	 * @param sessionType
	 * @param customerId
	 * @return
	 */
	public static String getCustSessionKey(String sessionType, String customerId) {
		String sessionKey = (String) RedisUtils.getRedisService().get(
				CacheKey_SESSIONKEY + customerId);
		if (sessionKey == null) {
			sessionKey = refreshCustSessionKey(sessionType, customerId);
		}
		return sessionKey;
	}

	/**
	 * 刷新密码sessionKey
	 * 
	 * @param sessionType
	 * @param customerId
	 * @return
	 */
	public static String refreshCustSessionKey(String sessionType,
			String customerId) {
		AppParam param = new AppParam();
		param.addAttr("customerId", customerId);
		param.addAttr("sessionType", sessionType);
		param.setService("custSessionService");
		param.setMethod("query");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = null;
		String sessionKey = null;
		// 若没有相应的对象，使用远程调用
		if (SpringAppContext.getBean("custSessionService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		} else {
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			sessionKey = (String) result.getRow(0).get("sessionKey");
		}
		RedisUtils.getRedisService().set(CacheKey_SESSIONKEY + customerId,
				sessionKey, USER_SESSIONKEY_TIME);
		return sessionKey;
	}

	/**
	 * 根据用户id获取sessionKey加密密码
	 * 
	 * @param customerId
	 * @param password
	 * @return
	 */
	public static String getEncryPwd(String sessionType, String customerId,
			String password) {
		if (!StringUtils.isEmpty(customerId)) {
			String sessionKey = getCustSessionKey(sessionType, customerId);
			String encrtPassword = MD5Util
					.getEncryptByKey(password, sessionKey);
			return encrtPassword;
		}
		return null;
	}

	/**
	 * 保存sessionKey
	 * 
	 * @param params
	 * @return
	 */
	public static AppResult saveSessionKey(AppParam params) {
		RedisUtils.getRedisService().del(CacheKey_SESSIONKEY + params.getAttr("customerId"));
		params.setService("custSessionService");
		params.setMethod("saveSessionKey");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);
	}
	
	public static AppResult updatePwd(String custId,String password){
		AppResult result = new AppResult();
		AppParam param = new AppParam();
    	String random = UUID.randomUUID().toString();
		String sessionKey = Md5.getInstance().encrypt(random);
		String passwordMd5 = MD5Util.getEncryptByKey(password, sessionKey);
		// 插入sessionKey
		AppParam sessionParam = new AppParam();
		sessionParam.addAttr("customerId", custId);
		sessionParam.addAttr("sessionType", CustTokenConstant.CUST_SESSION_TYPE_PWD);
		sessionParam.addAttr("sessionKey", sessionKey);
		CustomerPwdUtil.saveSessionKey(sessionParam);
    	param.addAttr("customerId", custId);
    	param.addAttr("password", passwordMd5);
    	param.setService("customerService");
    	param.setMethod("update");
    	param.setRmiServiceName(AppProperties.
    			getProperties(DuoduoConstant.RMI_SERVICE_START+"cust"));
		result = RemoteInvoke.getInstance().call(param);
		return result;
	}
}
