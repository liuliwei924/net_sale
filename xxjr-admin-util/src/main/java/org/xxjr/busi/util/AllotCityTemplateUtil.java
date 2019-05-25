package org.xxjr.busi.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

public class AllotCityTemplateUtil {
	/**获取城市分单量的key**/
	public static String KEY_APPLY_TEMP = "key_apply_temp";
	/**获取城市模板配置的key**/
	public static String KEY_APPLY_TEMP_CONF = "key_apply_temp_conf";
	/**获取城市模板名称的key**/
	public static String KEY_APPLY_TEMP_NAME = "key_apply_temp_name";
	/**获取城市类型的key**/
	public static String KEY_APPLY_CITY_TYPE = "key_apply_city_type";
	
	/***
	 * 获取城市分单量
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getApplyTemp(){
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get("key_apply_temp");
		if(list == null){
			list = refershApplyTemp();
		}
		return list;
	}
	
	public static List<Map<String, Object>> refershApplyTemp(){
		AppParam params = new AppParam("allotCityTemplateService","query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result=null;
		if (SpringAppContext.getBean("allotCityTemplateService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else{
			result = SoaManager.getInstance().callNoTx(params);
		}
		 List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		 if(result.getRows().size()>0){
			 list = result.getRows();
			 RedisUtils.getRedisService().set(KEY_APPLY_TEMP, (Serializable)list,60*60*24*7);
		 }
		 return list;
	}
	/***
	 * 获取城市模板配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getApplyTempConf(){
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEY_APPLY_TEMP_CONF);
		if(list == null){
			list = refershApplyTempConf();
		}
		return list;
	}
	
	public static List<Map<String, Object>> refershApplyTempConf(){
		AppParam params = new AppParam("allotTemplateConfService","query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result=null;
		if (SpringAppContext.getBean("allotTemplateConfService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else{
			result = SoaManager.getInstance().callNoTx(params);
		}
		 List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		 if(result.getRows().size()>0){
			 list = result.getRows();
			 RedisUtils.getRedisService().set(KEY_APPLY_TEMP_CONF, (Serializable)list,60*60*24*7);
		 }
		 return list;
	}
	/***
	 * 获取城市模板名称
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getApplyTempName(){
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEY_APPLY_TEMP_NAME);
		if(list == null){
			list = refershApplyTempName();
		}
		return list;
	}
	
	public static List<Map<String, Object>> refershApplyTempName(){
		AppParam params = new AppParam("allotTemplateConfService","queryTempName");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result=null;
		if (SpringAppContext.getBean("allotTemplateConfService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else{
			result = SoaManager.getInstance().callNoTx(params);
		}
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		if(result.getRows().size()>0){
			list = result.getRows();
			RedisUtils.getRedisService().set(KEY_APPLY_TEMP_NAME, (Serializable)list,60*60*24*7);
		}
		return list;
	}
	/***
	 * 获取城市类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getApplyCityType(){
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEY_APPLY_CITY_TYPE);
		if(list == null){
			list = refershApplyCityType();
		}
		return list;
	}
	
	public static List<Map<String, Object>> refershApplyCityType(){
		AppParam params = new AppParam("allotKfCityService","query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result=null;
		if (SpringAppContext.getBean("allotKfCityService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else{
			result = SoaManager.getInstance().callNoTx(params);
		}
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		if(result.getRows().size()>0){
			list = result.getRows();
			RedisUtils.getRedisService().set(KEY_APPLY_CITY_TYPE, (Serializable)list,60*60*24*7);
		}
		return list;
	}
	
	public static Boolean checkData(Map<String, Object> map) {
		map.remove("tempName");
		map.remove("grade");
		map.remove("signId");
		
		for (int i = 1; i < 7; i++) {
			int j = 0;
			for (String key : map.keySet()) {
				if (!"tempId".equals(key)&&NumberUtil.getInt(map.get(key))==i) {
					j++;
				}
			}
			if (j>=2) {
				return true;
			}
		}
		return false;
	}
}
