package org.xxjr.job.listener.busi.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateTimeUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.util.JobUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.ApplyAllotUtil;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/***
 * 分单工具类
 * @author liulw
 *
 */
public class OrgAllotUtils {
	/***
	 * 将新单分配池数据分配给门店
	 * @param processId
	 * @return
	 */
	public static AppResult allotOrgNewOrder(Object processId){
		AppResult result = new AppResult();
		int autoAllotStatus = SysParamsUtil.getIntParamByKey("orgNewAllotStatus", 0);
		if(autoAllotStatus == 0){
			result.setMessage("新单分配给门店功能未开启!");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"新单分配给门店功能未开启!");
			return result;
		}
		//获取所有网销门店
		Map<String,Object> baseMap= StoreSeparateUtils.getBaseConfig();
		String cityNames = StringUtil.getString(baseMap.get("allotCitys"));
		if(StringUtils.isEmpty(cityNames)) {
			result.setMessage("新单分配给门店的分单城市为空");
			JobUtil.addProcessExecute(processId,"新单分配给门店的分单城市为空");
			result.setSuccess(false);
			return result;
		}
		
		String[] cityNameArr = cityNames.split(",");
		String todayDate = DateTimeUtil.getCurTimeByParttern(DateTimeUtil.DATE_PATTERN_YYYY_MM_DD);
		int totalNetPoolCount = 0;
		int realAllotedCount = 0;
		try {
			for(int i = 0 ; i<cityNameArr.length; i++) {
				String cityName = cityNameArr[i];
				AppParam countParams = new AppParam();
				countParams.addAttr("orderType", 1);//再分配
				countParams.addAttr("cityName", cityName);
				int allotCityTotalCount = queryStoreAllotCount(countParams);
				totalNetPoolCount += allotCityTotalCount;

			    if(allotCityTotalCount > 0) {
			    	AppParam orgAllotOrderCfgParam = new AppParam("netStorePoolService","queryNetOrgAllotOrderCfg");
			    	orgAllotOrderCfgParam.addAttr("cityName", cityName);
			    	orgAllotOrderCfgParam.addAttr("todayDate", todayDate);
			    	orgAllotOrderCfgParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
					AppResult queryResult = RemoteInvoke.getInstance().callNoTx(orgAllotOrderCfgParam);
					
					if(queryResult.getRows().size() > 0) {
						List<Map<String,Object>> canAllotList = new ArrayList<Map<String,Object>>();
						
						for(Map<String,Object> cfgMap : queryResult.getRows()) {
							
							long allotedCount = NumberUtil.getInt(cfgMap.get("allotedCount"), 0);
							long orgMaxCount = NumberUtil.getInt(cfgMap.get("orgMaxCount"), 0);
							long needCount = orgMaxCount - allotedCount;
							if(needCount > 0) {
								Map<String,Object> map1 = new HashMap<String,Object>();
								map1.put("orgId", cfgMap.get("orgId"));
								map1.put("needCount", needCount);//还需要多少单
								map1.put("allotedCount", allotedCount);// 已经分配多少单
								
								canAllotList.add(map1);
							}
						}
						
						if(canAllotList.size() > 0) {
							int canAllotSize = canAllotList.size();
							canAllotList = canAllotList.stream().sorted(Comparator.comparing(OrgAllotUtils::comparingByAllotedCount)).collect(Collectors.toList());
							
							long avgCount = allotCityTotalCount/canAllotSize;//平均分配的单子
							long modCount = Math.floorMod(allotCityTotalCount,canAllotSize);//取模
							AppParam allotOrgParam = null;
							for(Map<String,Object> map2 : canAllotList) {
								int needCount = NumberUtil.getInt(map2.get("needCount"));
								long neeedAllotCount = 0;
								if(needCount > avgCount && modCount > 0) {
									neeedAllotCount = avgCount +1;
									modCount = modCount -1;
								}else if(needCount < avgCount ) {
									neeedAllotCount = needCount;
									modCount = modCount + (avgCount - needCount);
								}
								
								if(neeedAllotCount > 0) {
									allotOrgParam = new AppParam("netStorePoolService","allotOrgOrder");
									allotOrgParam.addAttr("orgId", map2.get("orgId"));
									allotOrgParam.addAttr("cityName", cityName);
									allotOrgParam.addAttr("neeedAllotCount", neeedAllotCount);
									allotOrgParam.addAttr("orderType", "1");//新单
									allotOrgParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
									AppResult allotResult = RemoteInvoke.getInstance().call(allotOrgParam);
									int updateSize = NumberUtil.getInt(allotResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
									
									if(updateSize > 0) {
										realAllotedCount += updateSize;
										ApplyAllotUtil.saveOrgAllotRecord(todayDate,map2.get("orgId"),cityName,updateSize);
										
									}
								}
								
							}
							
						}
					}
			    	
			    }
				
			}
			
			JobUtil.addProcessExecute(processId, "网销池中未分配的新单随机分给门店 ：成功笔数:"+ realAllotedCount +"，总共笔数：" + totalNetPoolCount);
			LogerUtil.log("网销池中未分配的新单随机分给门店 msg：成功笔数:"+ realAllotedCount +"，总共笔数：" + totalNetPoolCount);
		}catch (Exception e) {
			LogerUtil.error(OrgAllotUtils.class, e, "allotOrgNewOrder >>>>>>>>>>>>>>>>>> error");
			JobUtil.addProcessExecute(processId, "网销池中未分配的新单随机分给门店报错：" + e.getMessage() );
		}
		
		return result;
	}

	
	/***
	 * 获取分配池中的总量
	 * @param params
	 * @return
	 */
	private static int queryStoreAllotCount(AppParam params){
		params.setService("netStorePoolService");
		params.setMethod("queryCount");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(params);
		int size = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
		return size;
	}
	
	private static long comparingByAllotedCount(Map<String, Object> map){
        return Long.parseLong(map.get("allotedCount").toString());
    }
}
