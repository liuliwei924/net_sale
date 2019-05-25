package org.xxjr.busi.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.llw.model.cache.RedisUtils;
import org.xxjr.sys.util.ServiceKey;

/***
 * 再配置相关工具类
 * @author zqh
 *
 */
public class StoreAgainCfgUtils {
	/** 新定单配置key**/
	public static final String STORE_NEW_ALLOT_CFGKEY="store_new_allot_cfgKey";
	/** 再分配配置key**/
	public static final String STORE_AGAIN_ALLOT_CFGKEY="store_again_allot_cfgKey";
	/** 缓存7天*/
	public static final int base_cache_time = 60*60*24*7;
	
	/**
	 * 获取新定单配置
	 * @return
	 */
	public static Map<String,Object> getNewAllotCfg(){
		@SuppressWarnings("unchecked")
		Map<String,Object> baseMap =(Map<String,Object>)RedisUtils.getRedisService().get(STORE_NEW_ALLOT_CFGKEY);
		if(baseMap == null){
			baseMap = refreshNewAllotCfg();
		}
		return baseMap;
	}
	
	/**
	 * 刷新新定单配置
	 * @return
	 */
	private static Map<String,Object> refreshNewAllotCfg(){
		AppParam params  = new AppParam();
		params.addAttr("againType", 0);
		params.setService("borrowAgainCfgService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult baseResult = RemoteInvoke.getInstance().callNoTx(params);
		
		Map<String,Object> baseMap =  new HashMap<String,Object>();
		if(baseResult.getRows().size()>0){
			baseMap =  (Map<String, Object>) baseResult.getRow(0);
			RedisUtils.getRedisService().set(STORE_NEW_ALLOT_CFGKEY,(Serializable) baseMap, base_cache_time);
		}
		return baseMap;
	}
	
	
	/**
	 * 获取再分配配置
	 * @return
	 */
	public static Map<String,Object> getAgainAllotCfg(){
		@SuppressWarnings("unchecked")
		Map<String,Object> baseMap =(Map<String,Object>)RedisUtils.getRedisService().get(STORE_AGAIN_ALLOT_CFGKEY);
		if(baseMap == null){
			baseMap = refreshAgainAllotCfg();
		}
		return baseMap;
	}
	
	/**
	 * 刷新再分配配置
	 * @return
	 */
	private static Map<String,Object> refreshAgainAllotCfg(){
		AppParam params  = new AppParam();
		params.addAttr("againType", 1);
		params.setService("borrowAgainCfgService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult baseResult = RemoteInvoke.getInstance().callNoTx(params);
		
		Map<String,Object> baseMap =  new HashMap<String,Object>();
		if(baseResult.getRows().size()>0){
			baseMap =  (Map<String, Object>) baseResult.getRow(0);
			RedisUtils.getRedisService().set(STORE_AGAIN_ALLOT_CFGKEY,(Serializable) baseMap, base_cache_time);
		}
		return baseMap;
	}
}
