package org.xxjr.sys.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.common.context.AppProperties;
import org.springframework.util.StringUtils;

public class RequestSysUtils {
	/***
	 * 设置跨域处理-- llw
	 * @param request
	 * @return
	 */
	public static void setAccessControl(HttpServletResponse response) {
		//debug模式下可以所有域名访问
		if(AppProperties.isDebug()){
			response.setHeader("Access-Control-Allow-Origin","*");
		}else{
			response.setHeader("Access-Control-Allow-Origin",
			SysParamsUtil.getStringParamByKey("Access-Control-Allow-Origin", "*.xxjr.com"));
		}
		response.setHeader("Access-Control-Allow-Methods","OPTIONS,POST,CONNECT");
		response.setHeader("Access-Control-Max-Age","8800");
		response.setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept".toLowerCase());
		
	}
	
    /**
     * 读取request流
     * @param req
     * @return
     * @author guoyx
     */
    public static String readReqStr(HttpServletRequest request){
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(request
                    .getInputStream(), "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    /**
     * 检验是否为中文
     * @param name
     * @return
     */
    public static boolean checkParamChese(String param) {
    	boolean res = true;
    	if (StringUtils.isEmpty(param)) {
    		return false;
		}
	    char[] cTemp = param.toCharArray();
	    for (int i = 0; i < param.length(); i++) {
	        if (!isChinese(cTemp[i])) {
	            res = false;
	            break;
	        }
	    }
	    return res;
	}
	
	public static boolean isChinese(char c) {
	    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
	    if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
	            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
	            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
	            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
	            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
	            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
	        return true;
	    }
	    return false;
	}
}
