package org.llw.com.web.session;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.llw.com.context.AppParam;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/***
 * 本地线程的处理
 * @author xcb
 *
 */
@Slf4j
public class DuoduoSession implements java.io.Serializable {
	private static final long serialVersionUID = 8242424936268432231L;

	private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>();

	private static Map<String, Object> getLocalData() {
		Map<String, Object> map = threadLocal.get();
		if (map == null) {
			map = new ConcurrentHashMap<String, Object>();
			threadLocal.set(map);
		}
		return map;
	}
	/***
	 * 清除local信息
	 */
	public static void clearLocalData() {
		Map<String, Object> map = threadLocal.get();
		if (map != null) {
			map.clear();
		}
	}
	/**
	 * 根据key获取数据
	 * @param keyName
	 * @return
	 */
	private static Object getProperty(String keyName) {
		Map<String, Object> map = getLocalData();
		return map.get(keyName);
	}

	
	
	private static void setProperty(String keyName, Object value) {
		if (keyName != null && value != null) {
			Map<String, Object> map = getLocalData();
			map.put(keyName, value);
		}
	}
	/***
	 * 获取用户名
	 * @return
	 */
	public static String getUserName() {
		if (getUser() == null) {
			return null;
		}
		return (String) getUser().getUsername();
	}
	
	/***
	 * 获取Request
	 * @return
	 */
	public static HttpServletRequest getHttpRequest() {
		return (HttpServletRequest) getProperty(ThreadConstants.DUODUO_REQUEST);
	}
	
	/***
	 * 获取用户IP
	 * @return
	 */
	public static String getClientIp() {
		return (String) getProperty(ThreadConstants.CONSTMER_IPADDRESS);
	}
	/***
	 * 设置请求状态
	 * @param status
	 */
	public static void setStatus(String status){
		setProperty(ThreadConstants.DUODUO_STATUS, status);
	}
	/**
	 * 返回请求状态
	 * @return
	 */
	public static String getStatus(){
		return (String) getProperty(ThreadConstants.DUODUO_STATUS);
	}
	
	static Locale  locale = new  Locale("zh","CN"); 
	public static Locale getLocale() {
		return locale;
	}

	public static String getClientHost() {
		return (String) getProperty(ThreadConstants.CONSTMER_HOST);
	}
	/**
	 * 获取用户
	 * @return
	 */
	public static DuoduoUser getUser() {
		return (DuoduoUser) getProperty(ThreadConstants.DUODUO_SESSION);
	}
	
	/***
	 * 设置用户
	 * @param user
	 */
	public static void setUser(DuoduoUser user) {
		getLocalData().put(ThreadConstants.DUODUO_SESSION, user);
	}
	
	/**
	 * 设置参数
	 * @param request 需要设置的参数
	 * @param key 其他参数Key
	 * @param value 其他参数值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> setParams(HttpServletRequest request,String key,String value) {
		Map<String,Object>   params = (Map<String, Object>) getProperty(ThreadConstants.DUODUO_PARAMS);
		if (params == null) {
			params = new HashMap<String, Object>();
			setProperty(ThreadConstants.DUODUO_PARAMS, params);
		}
		if(request!=null){
			Enumeration<String> names =  request.getParameterNames();	
			while(names.hasMoreElements()){
				String rKey = names.nextElement();
				String rValue = request.getParameter(rKey);
				if(!StringUtils.isEmpty(rValue))
					params.put(rKey, rValue);
			}
		}
		if (key != null && value != null) {
			params.put(key, value);
		}
		return params;
	}
	
	/**
	 * 获取数据 DUODUO_PARAMS
	 * @return Map数据
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getParams() {
		Map<String,Object> params = (Map<String, Object>) getProperty(ThreadConstants.DUODUO_PARAMS);
		if (params == null) {
			params = new HashMap<String, Object>();
		}
		return params;
	}
	
	public static void web2Service(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		setProperty(ThreadConstants.CONSTMER_IPADDRESS, getIpAddress(request));
		setProperty(ThreadConstants.CONSTMER_HOST, request.getRemoteHost());
		setProperty(ThreadConstants.DUODUO_REQUEST, request);
		DuoduoUser duoduoUser =  (DuoduoUser) request.getAttribute(ThreadConstants.DUODUO_USER);
		if (duoduoUser != null) {
			setProperty(ThreadConstants.DUODUO_SESSION, duoduoUser);
		}
	}

	/***
	 * 清除session信息
	 */
	public static void clearSessionData(HttpServletRequest request) {
		request.removeAttribute(ThreadConstants.DUODUO_USER);
	}
	
	public static void service2Web(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		request = getHttpRequest();
	}
	
	/****
	 * 获得客户端真实IP地址的方法
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip != null && ip.equals("127.0.0.1")) {
			try {
				InetAddress inet = InetAddress.getLocalHost();
				ip = inet.getHostAddress();
			} catch (Exception e) {
				log.error("getIpAddress error:",e);
			}
		}
		return ip;
	}
	
	/***
	 * 设置前端日志
	 * @param param
	 */
	public static void setShowLog(AppParam param) {
		Map<String, Object> map = getLocalData();
		map.put(ThreadConstants.DUODUO_SHOWLog, param);
	}
	
	/***
	 * 获取前端日志
	 * @param param
	 */
	public static AppParam getShowLog() {
		Map<String, Object> map = getLocalData();
		if(map.containsKey(ThreadConstants.DUODUO_SHOWLog)){
			return (AppParam) map.get(ThreadConstants.DUODUO_SHOWLog);
		}
		return null;
	}
}