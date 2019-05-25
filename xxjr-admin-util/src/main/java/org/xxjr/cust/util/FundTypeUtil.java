package org.xxjr.cust.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.llw.model.cache.RedisUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 * 操作资金类型
 * @author 1
 *
 */
public class FundTypeUtil {

	/**已经认证的身分信息**/
	public static String fundType_redis_key = "fundType_redis_key";
	
	/**用户数据保留时长为 30 天**/
	public static Integer CACHE_TIME = 60*60*24*30;
	
	/***
	 * 获取资金类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAllFundType(){
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(fundType_redis_key);
		if(list == null){
			list = refershFundType();
		}
		return list;
	}
	
	public static List<Map<String, Object>> refershFundType(){
		AppParam params = new AppParam();
		params.setService("fundTypeService");
		params.setMethod("query");
		params.setOrderBy("createTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		 AppResult result=RemoteInvoke.getInstance().call(params);
		
		 List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		 if(result.getRows().size()>0){
			 list = result.getRows();
			 RedisUtils.getRedisService().set(fundType_redis_key, (Serializable)list, CACHE_TIME);
		 }
		 return list;
	}
	
}
