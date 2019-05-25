package org.xxjr.job.listener.busi.store;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.util.JobUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busi.util.store.OrderRecyclingUtil;
import org.xxjr.busi.util.store.StoreWorkTimeUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/***
 * 分单工具类
 * @author ZQH
 *
 */
public class StoreAutoAllotUtils {
	/***
	 * 将新单分配池数据分配
	 * @param processId
	 * @return
	 */
	public static AppResult allotStoreNewOrder(Object processId, String cityNames){
		AppResult result = new AppResult();
		int autoAllotStatus = SysParamsUtil.getIntParamByKey("storeNewAllotStatus", 0);
		if(autoAllotStatus == 0){
			result.setMessage("新单分单功能未开启!");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"新单分单StoreAutoAllotUtils msg:" + result.getMessage());
			return result;
		}

		int allotTotalCount = 0;//待分配业务员总单数
		int realTotalCount = 0;//实际分配业务员总单数

		//获取所有待分配数据
		AppParam countParams = new AppParam();
		countParams.addAttr("orderType", 1);//新分配
		countParams.addAttr("cityNames", cityNames);
		allotTotalCount = queryStoreAllotCount(countParams);
		LogerUtil.log("新分配待分给业务员总单数："  + allotTotalCount);

		if(allotTotalCount > 0){//新单分配池中有数据
			// 新单有剩余的对应的城市和剩余单数
			Map<String,Object> cityOrder = new HashMap<String,Object>();
			//分配未满足基本单量
			result = newStoreAllotOrder(1,false,cityOrder, cityNames);
			if(result.isSuccess()){
				realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
			}
			//分配已满足基本单量
			result = newStoreAllotOrder(1,true,cityOrder, cityNames);
			if(result.isSuccess()){
				realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
			}
		}else{
			result.setMessage("新单分配池中没有数据");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"新单分单StoreAutoAllotUtils msg:" + result.getMessage());
			return result;
		}
		LogerUtil.log("新分配实际业务员总单数："  + realTotalCount);
		JobUtil.addProcessExecute(processId, "新单待分配给业务员总单数" + allotTotalCount + "实际分配业务员总单数：" + realTotalCount);
		return result;
	}



	/***
	 * 将再分配池数据分配
	 * @param processId
	 * @return
	 */
	public static AppResult allotStoreAgainOrder(Object processId,String cityNames){
		AppResult result = new AppResult();
		int autoAllotStatus = SysParamsUtil.getIntParamByKey("storeAgainAllotStatus", 0);
		if(autoAllotStatus == 0){
			result.setMessage("再分配分单功能未开启!");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"再分配分单StoreAutoAllotUtils msg:" + result.getMessage());
			return result;
		}
		
		int allotTotalCount = 0;//待分配业务员总单数
		int realTotalCount = 0;//实际分配业务员总单数
		//获取所有待分配数据
		AppParam countParams = new AppParam();
		countParams.addAttr("orderType", 2);//再分配
		countParams.addAttr("cityNames", cityNames);
		allotTotalCount = queryStoreAllotCount(countParams);
		LogerUtil.log("再分配待分给业务员总单数："  + allotTotalCount);
		if(allotTotalCount > 0){//再分配池中有数据
			Map<String,Object> baseCfg = StoreSeparateUtils.getBaseConfig();
			int dayCount = NumberUtil.getInt(baseCfg.get("dayCount"),8);
			//1分配未满足基本单量
			result = storeAllotOrder(dayCount, false, 2, cityNames);
			if(result.isSuccess()){
				realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
			}
			//2分配已满足基本单量
			result = storeAllotOrder(dayCount, true, 2, cityNames);
			if(result.isSuccess()){
				realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
			}

		}else{
			result.setMessage("再分配池中没有数据");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"再分配分单StoreAutoAllotUtils msg:" + result.getMessage());
			return result;
		}
		LogerUtil.log("再分配实际分配业务员总单数："  + realTotalCount);
		JobUtil.addProcessExecute(processId, "再分配单待分配给业务员总单数" + allotTotalCount + "实际分配业务员总单数：" + realTotalCount);
		return result;
	}


	/**
	 * 业务员分配新单
	 * @param baseCount 基础分单数
	 * @param isOverFlag 是否满足基本单
	 * @param orderType 1-新分配 2-再分配
	 * @return
	 */
	private static AppResult storeAllotOrder(int baseCount, boolean isOverFlag,int orderType,
			String cityNames){
		int totalSize = 0;//实际分配给业务的总单数
		String isOverFlagDesc = "";//是否满足分单描述
		String orderDesc = "";//新单或再分配单描述
		
		//分城市查询，各个城市笔数
		AppParam queryParams = new AppParam("netStorePoolService", "queryGroupByCity");
		queryParams.addAttr("orderType", orderType);
		queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryParams);

		//待分配人员初始化，查询新单或再分单人员
		AppParam  optParams = new AppParam("netStorePoolService", "storeAllotNewOrder");
		optParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));

		//遍历城市
		for(Map<String, Object> cityMap : result.getRows()){
			try{
				int totalCount = NumberUtil.getInt(cityMap.get("totalCount"));//总单量
				if(totalCount <= 0){
					continue;
				}
				if(totalCount >500){//一次最多处理500条
					totalCount = 500;
				}
				Object cityName = cityMap.get("cityName");
				int avgCount = 0;//分单平均数
				int allotOrderCount = 0;//分单数
				int qmCount = 0;//剩余数
				int allotDifferCount = 0;
				String startAllotTime = "";//开始分单时间
				String endAllotTime = "";//结束分单时间
				//判断该城市是否是分单时间
				Map<String,Object> cityWorkMap = StoreWorkTimeUtils.getCityWorkTimeByCityName(cityName.toString());//配置过工作时间的城市列表	

				if(cityWorkMap.size() > 0){
					//当前时间
					long nowTime = new Date().getTime();
					startAllotTime = StringUtil.getString(cityWorkMap.get("startAllotTime"));
					endAllotTime = StringUtil.getString(cityWorkMap.get("endAllotTime"));
					if(!StringUtils.isEmpty(startAllotTime) && !StringUtils.isEmpty(endAllotTime)){
						long longStartAllotTime = OrderRecyclingUtil.getLongTimes(startAllotTime); 
						long longEndAllotTime = OrderRecyclingUtil.getLongTimes(endAllotTime);
						if(nowTime < longStartAllotTime || nowTime > longEndAllotTime){
							continue;
						}
					}
				}
				//获取每个城市的可分单的人数
				queryParams = new AppParam("custLevelService", !isOverFlag ? "queryLessBaseCust": "queryMoreBaseCust");
				queryParams.addAttr("baseCount", baseCount);
				queryParams.addAttr("cityName", cityName);
				queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				AppResult custResult = RemoteInvoke.getInstance().callNoTx(queryParams);

				int size = custResult.getRows().size();
				isOverFlagDesc = isOverFlag?",已满足基本单量,":",未满足基本单量,";
				LogerUtil.log(cityName +"总分单数:"+totalCount+ isOverFlagDesc+"分配人数:"  + size);


				//进行分单
				if(custResult.getRows().size() > 0){
					qmCount = totalCount%size;//剩余数
					avgCount = (totalCount-qmCount)/size;
					int tmpCount =0;
					for(Map<String, Object> map1 : custResult.getRows()){//开始分单
						try{
							if(tmpCount>=totalCount){
								break;
							}
							allotOrderCount = avgCount;
							if(qmCount > 0){
								allotOrderCount = allotOrderCount + 1;
								qmCount--;
							}

							//换成每次只分一单
							allotOrderCount = 1;

							//开始执行分单
							optParams.addAttr("customerId", map1.get("customerId"));
							optParams.addAttr("orgId", map1.get("orgId"));
							optParams.addAttr("cityName", cityName);
							optParams.addAttr("allotOrderCount", allotOrderCount);//待分配的单数
							optParams.addAttr("orderType", orderType);//新单或再分配单
							AppResult optResult = RemoteInvoke.getInstance().call(optParams);

							if(optResult.isSuccess()){
								int realAllotCount = NumberUtil.getInt(optResult.getAttr("realAllotCount"));
								allotDifferCount = allotOrderCount-realAllotCount;
								allotOrderCount = realAllotCount;
								orderDesc = orderType == 1?"新分配总单数:":"再分配总单数:";
								LogerUtil.log("业务员:"+map1.get("customerId") + orderDesc + allotOrderCount);
							}
							qmCount = qmCount +allotDifferCount;
							tmpCount = tmpCount + allotOrderCount;

							allotDifferCount = 0;
							optResult = null;

							totalSize = totalSize + allotOrderCount;//累计总单数
						}catch(Exception e){
							LogerUtil.error(StoreAutoAllotUtils.class, e, "StoreAutoAllotUtils 信贷经理分单error");
						}
					}
				}
			}catch(Exception e){
				LogerUtil.error(StoreAutoAllotUtils.class, e, "StoreAutoAllotUtils 信贷经理分单error");
			}

		}
		result.putAttr("totalSize", totalSize);
		optParams = null;
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
	
	
	/**
	 * 业务员分单(新)
	 * @param orderType 1-新分配 2-再分配
	 * @param cityList 新单有剩余的城市集合
	 * @return
	 */
	private static AppResult newStoreAllotOrder(int orderType,boolean isOverFlag,Map<String, Object> cityOrder
			,String cityNames){
		int totalSize = 0;//实际分配给业务的总单数
		String isOverFlagDesc = "";//是否满足分单描述
		String orderDesc = "";//新单或再分配单描述
		
		//分城市查询，各个城市笔数
		AppParam queryParams = new AppParam("netStorePoolService", "queryGroupByCity");
		queryParams.addAttr("orderType", orderType);
		queryParams.addAttr("cityNames", cityNames);
		queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryParams);

		//待分配人员初始化，查询新单或再分单人员
		AppParam  optParams = new AppParam("netStorePoolService", "storeAllotNewOrder");
		optParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		//遍历城市
		for(Map<String, Object> cityMap : result.getRows()){
			try{
				int totalCount = NumberUtil.getInt(cityMap.get("totalCount"));//总单量
				if(totalCount <= 0){
					continue;
				}
				String cityName = StringUtil.getString(cityMap.get("cityName"));
				int allotOrderCount = 0;//分单数
				String startAllotTime = "";//开始分单时间
				String endAllotTime = "";//结束分单时间
				//判断该城市是否是分单时间
				Map<String,Object> cityWorkMap = StoreWorkTimeUtils.getCityWorkTimeByCityName(cityName);

				if(cityWorkMap.size() > 0){
					//当前时间
					long nowTime = new Date().getTime();
					startAllotTime = StringUtil.getString(cityWorkMap.get("startAllotTime"));
					endAllotTime = StringUtil.getString(cityWorkMap.get("endAllotTime"));
					if(!StringUtils.isEmpty(startAllotTime) && !StringUtils.isEmpty(endAllotTime)){
						long longStartAllotTime = OrderRecyclingUtil.getLongTimes(startAllotTime); 
						long longEndAllotTime = OrderRecyclingUtil.getLongTimes(endAllotTime);
						if(nowTime < longStartAllotTime || nowTime > longEndAllotTime){
							continue;
						}
					}
				}
				
				isOverFlagDesc ="未满足基本单量,";
				// 获取剩余单数
				int limitCount = NumberUtil.getInt(cityOrder.get(cityName),0);
				if(limitCount > 0){
					queryParams.setMethod("queryMoreBaseCust");
					queryParams.addAttr("limitCount", limitCount);
					isOverFlagDesc ="已满足基本单量,";
				}else if(isOverFlag){
					continue;
				}
				//获取每个城市的可分单的人数
				queryParams = new AppParam("custLevelService", "queryLessBaseCust");
				queryParams.addAttr("cityName", cityName);
				queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				AppResult custResult = RemoteInvoke.getInstance().callNoTx(queryParams);
				int allotCount = custResult.getRows().size();
				LogerUtil.log(cityName +"总分单数:"+totalCount+ isOverFlagDesc+"分配人数:"  + allotCount);
				// 未满足基本分单量累计单数
				int needAllotCount = 0;
				//进行分单
				if(custResult.getRows().size() > 0){
					for(Map<String, Object> map1 : custResult.getRows()){//开始分单
						try{
							// 新分配单量
							int allotSeniorCount = NumberUtil.getInt(map1.get("allotSeniorCount"),0);
							// 基本分单量
							int baseCount = NumberUtil.getInt(map1.get("baseCount"),0);
							//还未达到基本分单量累计的单数
							if(baseCount > allotSeniorCount){
								needAllotCount += (baseCount - allotSeniorCount);
							}
							//换成每次只分一单
							allotOrderCount = 1;
							//开始执行分单
							optParams.addAttr("customerId", map1.get("customerId"));
							optParams.addAttr("orgId", map1.get("orgId"));
							optParams.addAttr("cityName", cityName);
							optParams.addAttr("allotOrderCount", allotOrderCount);//待分配的单数
							optParams.addAttr("orderType", orderType);//新单或再分配单
							AppResult optResult = RemoteInvoke.getInstance().call(optParams);

							if(optResult.isSuccess()){
								int realAllotCount = NumberUtil.getInt(optResult.getAttr("realAllotCount"),0);
								allotOrderCount = realAllotCount;
								orderDesc = orderType == 1 ? "新分配总单数:":"再分配总单数:";
								LogerUtil.log("业务员:"+map1.get("customerId") + orderDesc + allotOrderCount);
							}
							optResult = null;
							totalSize = totalSize + allotOrderCount;//累计总单数
						}catch(Exception e){
							LogerUtil.error(StoreAutoAllotUtils.class, e, "StoreAutoAllotUtils 信贷经理分单error");
						}
					}
				}
				// 比较新单总数和未满足基本分单量累计单数，大于则进行已满足基本单分单
				if(totalCount > needAllotCount && !isOverFlag){
					//剩余单量
					int leftCount = totalCount - needAllotCount;
					cityOrder.put(cityName, leftCount);
				}
			}catch(Exception e){
				LogerUtil.error(StoreAutoAllotUtils.class, e, "StoreAutoAllotUtils 信贷经理分单error");
			}
		}
		result.putAttr("totalSize", totalSize);
		optParams = null;
		return result;
	}

}
