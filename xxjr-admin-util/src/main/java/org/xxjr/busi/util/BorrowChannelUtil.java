package org.xxjr.busi.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 * 贷款渠道工具类
 * @author Administrator
 *
 */
public class BorrowChannelUtil {
	
	/** 所有贷款渠道  **/
	private final static String KEY_BORROW_CHANNEL = "key_borrow_channel";
	
	/** 所有贷款渠道小类  **/
	private final static String KEY_BORROW_CHANNEL_DTL = "key_borrow_channel_dtl";
	
	/**
	 * 查询所有贷款渠道
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAllChannel(){
		List<Map<String, Object>> info = (List<Map<String, Object>>) RedisUtils
				.getRedisService().get(KEY_BORROW_CHANNEL);
		if(info == null || info.size()==0){
			info = refreshBorrowChannel();
		}
		return info;
	}
	
	/**
	 * 查询所有贷款渠道
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAllChannelDtl(){
		List<Map<String, Object>> info = (List<Map<String, Object>>) RedisUtils
				.getRedisService().get(KEY_BORROW_CHANNEL_DTL);
		if(info == null){
			info = refreshBorrowChannelDtl();
		}
		return info;
	}
	/**
	 * 根据渠道代号查询商户信息
	 */
	public static Map<String,Object> getChannelByCode(String channelCode){
		List<Map<String, Object>> list = getAllChannel();
		for (int i = 0; i < list.size(); i++) {
			Map<String,Object> map = list.get(i);
			if(channelCode.equals(map.get("channelCode").toString())){
				return map;
			}
		}
		return new HashMap<String,Object>();
	}
	
	/**
	 * 根据渠道代号查询商户信息
	 */
	public static String getChannelByStartCode(String channelCode){
		if(channelCode==null|| channelCode.trim().length()==0){
			return null;
		}
		List<Map<String, Object>> list = getAllChannel();
		for (int i = 0; i < list.size(); i++) {
			Map<String,Object> map = list.get(i);
			if(channelCode.toLowerCase().startsWith(map.get("channelCode").toString().toLowerCase())){
				return StringUtil.getString(map.get("channelCode"));
			}
		}
		return null;
	}
	
	
	
	/**
	 * 刷新渠道
	 * @return
	 */
	public static List<Map<String, Object>> refreshBorrowChannel(){
		AppParam param = new AppParam();
		param.setService("borrowChannelService");
		param.setMethod("query");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		//若没有相应的对象，使用远程调用 
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("borrowChannelService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size() > 0){
			List<Map<String, Object>> info = result.getRows();
			RedisUtils.getRedisService().set(KEY_BORROW_CHANNEL, (Serializable)info, 3600 * 24 *7);
		}
		return null;
	}
	
	
	/**
	 * 刷新渠道小类
	 * @return
	 */
	public static List<Map<String, Object>> refreshBorrowChannelDtl(){
		AppParam param = new AppParam();
		param.setService("borrowChannelDtlService");
		param.setMethod("query");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		//若没有相应的对象，使用远程调用 
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("borrowChannelDtlService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size() > 0){
			List<Map<String, Object>> info = result.getRows();
			RedisUtils.getRedisService().set(KEY_BORROW_CHANNEL_DTL, (Serializable)info, 3600 * 24 *7);
		}
		return null;
	}
}
