package org.xxjr.sys.util.message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;

public class EmailConfigUtil {

	/**Email设置*/
	public final static String Key_configEmail="Email_Config";
	/***
	 * 刷新web短信服务
	 * @return
	 */
	public static List<Map<String, Object>> refreshConfig(){
		AppParam param = new AppParam();
		param.setService("sysEmailService");
		param.setMethod("query");
		param.addAttr("enable", "1");
		AppResult emailConf =null;
		if (SpringAppContext.getBean("sysEmailService") == null) {
			emailConf = RemoteInvoke.getInstance().call(param);
		}else{
			emailConf = SoaManager.getInstance().invoke(param);
		}
		
		RedisUtils.getRedisService().set(Key_configEmail,(Serializable)emailConf.getRows(),60*60*8);
		return emailConf.getRows();
	}
	/***
	 * 获取web短信服务
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>>  getConfig(){
		List<Map<String, Object>> list = (List<Map<String, Object>>)RedisUtils.getRedisService().get(Key_configEmail);
		if(list == null){
			list = refreshConfig();
		}
		return list;
	}
}
