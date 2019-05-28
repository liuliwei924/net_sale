package org.xxjr.job.listener.busi.store;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.util.JobUtil;
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
	public static AppResult allotStoreNewOrder(Object processId){
		AppResult result = new AppResult();
		int autoAllotStatus = SysParamsUtil.getIntParamByKey("storeNewAllotStatus", 0);
		if(autoAllotStatus == 0){
			result.setMessage("新单分单功能未开启!");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"新单分单StoreAutoAllotUtils msg:" + result.getMessage());
			return result;
		}
		
		int realTotalCount = 0;//待分配业务员总单数
		//分配未满足基本单量
		result = newStoreAllotOrder(1,false);
		if(result.isSuccess()){
			realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
		}
		//分配已满足基本单量
		result = newStoreAllotOrder(1,true);
		if(result.isSuccess()){
			realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
		}
		
		LogerUtil.log("新分配实际业务员总单数："  + realTotalCount);
		JobUtil.addProcessExecute(processId,"新分配实际业务员总单数："  + realTotalCount);
		return result;
	}



	/***
	 * 将再分配池数据分配
	 * @param processId
	 * @return
	 */
	public static AppResult allotStoreAgainOrder(Object processId){
		AppResult result = new AppResult();
		int autoAllotStatus = SysParamsUtil.getIntParamByKey("storeAgainAllotStatus", 0);
		if(autoAllotStatus == 0){
			result.setMessage("再分配分单功能未开启!");
			result.setSuccess(false);
			JobUtil.addProcessExecute(processId,"再分配分单StoreAutoAllotUtils msg:" + result.getMessage());
			return result;
		}
		
		int realTotalCount = 0;//待分配业务员总单数
		//分配未满足基本单量
		result = newStoreAllotOrder(2,false);
		if(result.isSuccess()){
			realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
		}
		//分配已满足基本单量
		result = newStoreAllotOrder(2,true);
		if(result.isSuccess()){
			realTotalCount = realTotalCount + NumberUtil.getInt(result.getAttr("totalSize"));
		}
		
		LogerUtil.log("再分配实际分配业务员总单数："  + realTotalCount);
		JobUtil.addProcessExecute(processId, "再分配实际分配业务员总单数：" + realTotalCount);
		return result;
	}


	

	/**
	 * 业务员分单(新)
	 * @param orderType 1-新分配 2-再分配
	 * @param isOverFlag 是否满足基本量 true：已
	 * @return
	 */
	private static AppResult newStoreAllotOrder(int orderType,boolean isOverFlag){
		int totalSize = 0;
		//门店分组，查询各门店的单数
		AppParam queryParams = new AppParam("netStorePoolService", "queryGroupByOrgId");
		queryParams.addAttr("orderType", orderType);
		queryParams.addAttr("orgFlag", 1);//0-不自动分单 1-自动分单
		queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryParams);

		//待分配人员初始化，查询新单或再分单人员
		AppParam  optParams = new AppParam("netStorePoolService", "storeAllotNewOrder");
		optParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		//遍历城市
		for(Map<String, Object> orgMap : result.getRows()){
			try{
				int totalCount = NumberUtil.getInt(orgMap.get("totalCount"));//总单量
				if(totalCount <= 0){
					continue;
				}
				String orgId = StringUtil.getString(orgMap.get("orgId"));
				String orgName = StringUtil.getString(orgMap.get("orgName"));
				String isOverFlagDesc = "未满足基本单量,";
				//获取每个门店的可分单的人数
				queryParams = new AppParam("custLevelService", "queryLessBaseCust");
	
				if(isOverFlag){
					queryParams.setMethod("queryMoreBaseCust");
					isOverFlagDesc ="已满足基本单量,";
				}
				
				queryParams.addAttr("orgId", orgId);
				queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				AppResult custResult = RemoteInvoke.getInstance().callNoTx(queryParams);
				int allotCount = custResult.getRows().size();
				LogerUtil.log(orgName +"总分单数:"+totalCount+ "," + isOverFlagDesc+"分配人数:"  + allotCount);
				// 未满足基本分单量累计单数
				int allotedTotalCount = 0;
				//进行分单
				if(custResult.getRows().size() > 0){
					for(Map<String, Object> map1 : custResult.getRows()){//开始分单
						try{
							//本门店的单子已经分完了
							if(allotedTotalCount >= totalCount) {
								break;
							}
							//换成每次只分一单
							int allotOrderCount = 1;
							//开始执行分单
							optParams.addAttr("orgId", orgId);
							optParams.addAttr("customerId", map1.get("customerId"));
							optParams.addAttr("cityName", map1.get("cityName"));
							optParams.addAttr("allotOrderCount", allotOrderCount);//待分配的单数
							optParams.addAttr("orderType", orderType);//新单或再分配单
							if(orderType == 2) {
								optParams.addAttr("againFlag", 1);
							}
							
							AppResult optResult = RemoteInvoke.getInstance().call(optParams);

							if(optResult.isSuccess()){
								int realAllotCount = NumberUtil.getInt(optResult.getAttr("realAllotCount"),0);
								allotedTotalCount += realAllotCount;
								String orderDesc = orderType == 1 ? "新分配总单数:":"再分配总单数:";
								LogerUtil.log("业务员:"+map1.get("customerId") + orderDesc + realAllotCount);
							}
							optResult = null;
						}catch(Exception e){
							LogerUtil.error(StoreAutoAllotUtils.class, e, "StoreAutoAllotUtils 信贷经理分单error");
						}
					}
				}
				
				totalSize += allotedTotalCount;
			}catch(Exception e){
				LogerUtil.error(StoreAutoAllotUtils.class, e, "StoreAutoAllotUtils 信贷经理分单error");
			}
		}
		result.putAttr("totalSize", totalSize);
		optParams = null;
		return result;
	}

}
