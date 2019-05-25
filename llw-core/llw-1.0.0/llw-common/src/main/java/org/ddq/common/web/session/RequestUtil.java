package org.ddq.common.web.session;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.context.AppParam;
import org.springframework.util.StringUtils;

public class RequestUtil {
	
	/***
	 * 设置请求参数
	 * @param context 需要设置的context
	 * @param request 请求的request
	 */
	public static void setAttr(AppParam context ,HttpServletRequest request){
		Enumeration<String> names =  request.getParameterNames();	
		while(names.hasMoreElements()){
			String key = names.nextElement();
			String value = request.getParameter(key);
			if (value == null) {
				continue;
			}
			value = value.trim();
			if("service".equals(key) && StringUtils.isEmpty(context.getService())){
				context.setService(value);
			}else if("method".equals(key)&& StringUtils.isEmpty(context.getMethod())){
				context.setMethod(value);
			}else if("rmiServiceName".equals(key)|| "rmiName".equals(key)){
				context.setRmiServiceName(value);
			}else if(key.startsWith("row[")){
				
			}else if("dataBase".equals(key)){
				context.setDataBase(value);
			}else if("orderBy".equals(key) || "attr[orderBy]".equals(key)){
				context.setOrderBy(value);
			}else if("orderValue".equals(key) || "attr[orderValue]".equals(key)){
				context.setOrderValue(value);
			}else if("currentPage".equals(key) || "page[currentPage]".equals(key)){
				context.setCurrentPage(Integer.valueOf(value));
			}else if("everyPage".equals(key) || "page[everyPage]".equals(key)){
				//增加每页最大数量的判断，防止出现其他的非法处理
				int everyPage = Integer.valueOf(value);
				if (everyPage > 100) {
					everyPage = 100;
				}
				context.setEveryPage(everyPage);
			}else if(key.startsWith("attr[")){
				context.addAttr(key.substring("attr[".length(),key.length()-1), value);
			}else{
				context.addAttr(key, value);
			}
		}
	}	
	
	/***
	 * 获取request params 
	 * @param request
	 * @param filter 过滤密码，是 过滤
	 */
	public static String getRequestParams(HttpServletRequest request,boolean filter){
		StringBuffer param = new StringBuffer();
		for(String key:request.getParameterMap().keySet()){
			if(filter){
				String lowerKey = key.toLowerCase();
				if(lowerKey.indexOf("password")>=0 || key.indexOf("pwd")>=0){
					continue;
				}
			}
			String value = request.getParameter(key).trim();
			if (value.length() > 0) {
				//if (filter && value.length() >= 300) {
				//	value = value.substring(0,100);
				//}
				param.append("&" + key + "=" + value);
			}
		}
		return param.toString();
	}
	
	/***
	 * 获取request params 
	 * @param request 请求信息
	 * @param filterKey 需要过滤的字段名
	 */
	public static String getRequestParams(HttpServletRequest request,String filterKey){
		StringBuffer param = new StringBuffer();
		for(String key:request.getParameterMap().keySet()){
			if(key.equals(filterKey)){
				continue;
			}
			param.append("&" + key + "=" + request.getParameter(key).trim());
		}
		return param.toString();
	}
	
	/***
	 * 判断 是否手机访问
	 * @param request
	 * @return
	 */
	public static boolean isPhone(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		if(StringUtils.isEmpty(userAgent)) {
			return true;
		}
		userAgent = userAgent.toLowerCase();
		if (userAgent.toLowerCase().matches("(.*)AppleWebKit.*Mobile(.*)".toLowerCase())
				|| userAgent.toLowerCase().matches("(.*)Adr.*UCBrowser.*Mobile".toLowerCase())
				|| (userAgent
						.matches(".*(MIDP|SymbianOS|NOKIA|SAMSUNG|LG|NEC|TCL|Alcatel|BIRD|DBTEL|Dopod|PHILIPS|HAIER|LENOVO|MOT-|Nokia|SonyEricsson|SIE-|Amoi|ZTE).*"
								.toLowerCase()))) {
			// 去掉iPad
			if (!userAgent.matches(".*iPad.*".toLowerCase())) {
				return true;
			}
		}
		return false;

	}
	

	/***
	 * 判断 是否微信访问
	 * @param request
	 * @return
	 */
	public static boolean isWX(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		userAgent = userAgent.toLowerCase();
		if (userAgent.toLowerCase().indexOf("micromessenger")>0) {
			return true;
		}
		return false;

	}
}
