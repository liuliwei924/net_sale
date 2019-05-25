package org.xxjr.job.listener.busi.store;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.llw.job.util.JobUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xxjr.busi.util.ApplyAllotUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;



/**
 * 7天未处理的专属单 提醒门店人员处理 上传成本单量到财务系统
 * 
 * @author loys
 *
 */
@Lazy
@Component
public class StoreExcluNotDealJob implements BaseExecteJob {
	
	/**
	 * 7天未处理的专属单 提醒门店人员处理和
	 * @param processId
	 * @return
	 */
	@Override
	public AppResult executeJob(AppParam param){
		LogerUtil.log("StoreExcluNotDealJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		exclusiveNotDeal(processId);
		netPoolNewOrderAllotOrg(processId);
		LogerUtil.log("StoreExcluNotDealJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		return result;
	}

	/**
	 * 7天未处理的专属单 提醒门店人员处理
	 * @param processId
	 * @return
	 */
	public AppResult exclusiveNotDeal(Object processId) {
		AppResult result = new AppResult();
		int totalSucSize = 0;
		int totalFailSize = 0;
		try{
			// 查询7天未处理的专属单
			AppParam queryParam = new AppParam("exclusiveOrderService","queryNotDeal");
			queryParam.addAttr("orgId", 236);
			queryParam.setEveryPage(50);
			queryParam.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			
			List<Map<String,Object>> orderList = new ArrayList<>();
			AppParam updateParam = new AppParam("storeExcluesiveNotifyService","save");
			updateParam.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			
			// 只处理500笔
			if(result.isSuccess() && result.getRows().size() > 0){
				int totalPage = result.getPage().getTotalPage();
				if(totalPage > 10){
					totalPage = 10;
				}
				for(int i = 1; i<=totalPage; i++){
					// 查询7天未处理的专属单
					queryParam.setCurrentPage(i);
					queryParam.setRmiServiceName(AppProperties.getProperties(
							DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					result = RemoteInvoke.getInstance().callNoTx(queryParam);
					if(result.isSuccess() && result.getRows().size() >0){
						orderList.addAll(result.getRows());
					}
					try{
						updateParam.addAttr("orderList", orderList);
						result  = RemoteInvoke.getInstance().call(updateParam);
						if(result.isSuccess()){
							totalSucSize = totalSucSize + NumberUtil.getInt(result.getAttr("sucSize"),0);
							totalFailSize = totalFailSize + NumberUtil.getInt(result.getAttr("failSize"),0);
						}
					}catch(Exception e){
						LogerUtil.error(StoreExcluNotDealJob.class,e, "exclusiveNotDeal error");
						JobUtil.addProcessExecute(processId, " excluNotDeal 报错：" + e.getMessage() );
					}
					orderList.clear();
				}
			}

		}catch(Exception e){
			LogerUtil.error(StoreExcluNotDealJob.class,e, "exclusiveNotDeal error");
			JobUtil.addProcessExecute(processId, " exclusiveNotDeal 报错：" + e.getMessage() );
		}
		LogerUtil.log("专属单加入通知成功总笔数:"  + totalSucSize + "失败总笔数:" + totalFailSize);
		JobUtil.addProcessExecute(processId, "专属单加入通知成功总笔数:" + totalSucSize + "失败总笔数:" + totalFailSize);
		return result;
	}

	
	
	/***
	 * 网销池中未分配的新单随机分给门店
	 * @param processId
	 * @return
	 */
	public AppResult netPoolNewOrderAllotOrg(Object processId){
		AppResult result = new AppResult();
		int netNewOrderAllotOrgFlag = SysParamsUtil.getIntParamByKey("netNewOrderAllotOrgFlag", 0);
		if(netNewOrderAllotOrgFlag == 0){
			result.setMessage("网销池中未分配的新单随机分给门店暂未开启!");
			result.setSuccess(false);
			return result;
		}
		try{
			AppParam queryParam = new AppParam("netStorePoolService","queryNetOrderGroupByCity");
			String startCreateDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(),-1),
					DateUtil.DATE_PATTERN_YYYY_MM_DD);
			queryParam.addAttr("startCreateDate", startCreateDate);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int totalCount = 0;
			int successCount = 0;
			if(queryResult.getRows().size() > 0){
				try{
					AppParam updateParam = new AppParam("netStorePoolService","updateOrderOrgId");
					for(Map<String, Object> queryMap : queryResult.getRows()){
						String cityName = StringUtil.getString(queryMap.get("cityName"));
						//订单数量
						int orderCount = NumberUtil.getInt(queryMap.get("orderCount"),0);
						totalCount += orderCount;
						String applyIds = StringUtil.getString(queryMap.get("applyIds"));
						List<Map<String,Object>> orglist = OrgUtils.getNetOrgListByCity(cityName);
						//门店数量
						int orgCount = orglist.size();
						if(orgCount == 1){ //只有一个门店
							String orgId = StringUtil.getString(orglist.get(0).get("orgId"));
							updateParam.addAttr("orgId", orgId);
							updateParam.removeAttr("applyId");
							updateParam.addAttr("applyIdIn", applyIds);
							updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
							AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
							int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
							if(updateSize > 0){
								successCount += updateSize;
								for(int i = 0; i < orderCount; i++){
									//记录门店分配订单数量
									ApplyAllotUtil.saveOrgAllotRecord(DateUtil.getSimpleFmt(new Date()),orgId,cityName);
								}
							}
						}else{ //多个门店
							List<String> applyList = Arrays.asList(applyIds.split(","));
							//订单数量大于等于门店数量
							if(orderCount >= orgCount){
								for (int j = 0; j < orderCount; j++) {
									String applyId = applyList.get(j);
									String orgId = "";
									if(j >= orgCount){
										Random random = new Random();// 定义随机类
										int index = random.nextInt(orgCount);
										orgId = StringUtil.getString(orglist.get(index).get("orgId"));
									}else{
										orgId = StringUtil.getString(orglist.get(j).get("orgId"));
									}
									updateParam.addAttr("orgId", orgId);
									updateParam.addAttr("applyId", applyId);
									updateParam.removeAttr("applyIdIn");
									updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
											+ ServiceKey.Key_busi_in));
									AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
									int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
									if(updateSize > 0){
										successCount ++;
										//记录门店分配订单数量
										ApplyAllotUtil.saveOrgAllotRecord(DateUtil.getSimpleFmt(new Date()),orgId,cityName);
									}
								}
							}else{
								//订单数量小于门店数量
								for (int j = 0; j < orderCount; j++) {
									String applyId = applyList.get(j);
									String orgId = StringUtil.getString(orglist.get(j).get("orgId"));
									updateParam.addAttr("orgId", orgId);
									updateParam.addAttr("applyId", applyId);
									updateParam.removeAttr("applyIdIn");
									updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
											+ ServiceKey.Key_busi_in));
									AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
									int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
									if(updateSize > 0){
										successCount ++;
										//记录门店分配订单数量
										ApplyAllotUtil.saveOrgAllotRecord(DateUtil.getSimpleFmt(new Date()),orgId,cityName);
									}
								}
							}
						}
					}
				}catch(Exception e){
					LogerUtil.error(StoreExcluNotDealJob.class, e, "netPoolNewOrderAllotOrg error");
				}
			}
			JobUtil.addProcessExecute(processId, "网销池中未分配的新单随机分给门店 ：成功笔数:"+ successCount +"，总共笔数：" + totalCount);
			LogerUtil.log("网销池中未分配的新单随机分给门店 msg：成功笔数:"+ successCount +"，总共笔数：" + totalCount);
		}catch(Exception e){
			LogerUtil.error(StoreExcluNotDealJob.class, e, "netPoolNewOrderAllotOrg >>>>>>>>>>>>>>>>>> error");
			JobUtil.addProcessExecute(processId, "网销池中未分配的新单随机分给门店报错：" + e.getMessage() );
		}
		return result;
	}
}
