package org.llw.com.context;

import org.llw.com.core.SpringAppContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * spring中获取资源文件
 * 
 * @author liulw
 * 
 */
public class AppProperties {
	
	public static Environment environment;
	
	/**debug状态*/
	public final static String DEBUG_FLAG ="system.debug";
	
	/**密码是否添加加密串*/
	public final static String PASSWORD_IS_ENCRYPT = "password.encrypt";
	/**加密处理多加串*/
	public final static String PASSWORD_MD5_KEY = "password.encrypt.key";
	

	public static Environment getEnvironment() {
		if (environment == null) {
			environment = initEnvironment();
		}
		return environment;
	}
	
	public static Environment initEnvironment() {
		ApplicationContext applicationContext = SpringAppContext
				.getApplicationContext();
		Environment environment = applicationContext.getEnvironment();
		return environment;
	}
	
	public static String getProperties(String name) {
		return getEnvironment().getProperty(name);
	}

	
	/***
	 * 系统是否debug状态
	 * @return
	 */
	public static boolean isDebug(){
		String debug =  getEnvironment().getProperty(DEBUG_FLAG);
		if(StringUtils.isEmpty(debug)){
			return false;
		}
		return Boolean.valueOf(debug);
	}
	
}