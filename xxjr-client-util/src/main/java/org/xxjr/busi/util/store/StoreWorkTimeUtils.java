package org.xxjr.busi.util.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.xxjr.sys.util.ServiceKey;

public class StoreWorkTimeUtils {

	/***
	 * 获取城市的工作时间
	 * @return
	 */
	public static List<Map<String,Object>> getCityWorkTimeList(){
		AppResult result = new AppResult();
		List<Map<String,Object>> cityList = new ArrayList<Map<String,Object>>();
		AppParam params = new AppParam();
		params.setService("worktimeCfgService");
		params.setMethod("query");
		params.setOrderBy("createTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			cityList = result.getRows();
		}
		return cityList;
	}
	
	public static Map<String,Object> getCityWorkTimeByCityName(String cityName){
		Map<String,Object> workMap = new HashMap<String, Object>();
		List<Map<String,Object>> cityList = getCityWorkTimeList();
		for(Map<String,Object> map:cityList){
			if(cityName.equals(map.get("cityName"))){
				workMap.putAll(map);
				break;
			}
		}
		return workMap;
	}

}
