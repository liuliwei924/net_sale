package org.xxjr.busiIn.kf.config;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.active.mq.store.StoreAppSend;
import org.ddq.active.mq.store.StorePcSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.DateTimeUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busiIn.utils.AllotCostUtil;
import org.xxjr.busiIn.utils.StoreAlllotUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class NetStorePoolService extends BaseService {
	private static final String NAMESPACE = "NETSTOREPOOL";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * getStoreAllotNewOrder
	 * @param params
	 * @return
	 */
	public AppResult getStoreAllotNewOrder(AppParam params) {
		return super.query(params, NAMESPACE,"getStoreAllotNewOrder");
	}
	
	/**
	 * getStoreAllotAgainOrder
	 * @param params
	 * @return
	 */
	public AppResult getStoreAllotAgainOrder(AppParam params) {
		return super.query(params, NAMESPACE,"getStoreAllotAgainOrder");
	}
	
	/**
	 * queryNetOrderGroupByCity
	 * @param params
	 * @return
	 */
	public AppResult queryNetOrderGroupByCity(AppParam params) {
		return super.query(params, NAMESPACE,"queryNetOrderGroupByCity");
	}
	/**
	 * queryOverTimeToSale
	 * @param params
	 * @return
	 */
	public AppResult queryOverTimeToSale(AppParam params) {
		return super.query(params, NAMESPACE, "queryOverTimeToSale");
	}
	
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * queryNetCount
	 * @param params
	 * @return
	 */
	public AppResult queryNetCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryNetCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("createTime", new Date());
		return super.update(params, NAMESPACE);
	}
	/**
	 * updateNetPoolOrgId(纯净更新)
	 * @param params
	 * @return
	 */
	public AppResult updateNetPool(AppParam params) {
		return super.update(params, NAMESPACE);
	}
	/**
	 * updateOrderOrgId
	 * @param params
	 * @return
	 */
	public AppResult updateOrderOrgId(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateOrderOrgId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	/**
	 * saveOrUpdate
	 * @param params
	 * @return
	 */
	public AppResult saveOrUpdate(AppParam params) {
		AppResult updateResult = super.update(params, NAMESPACE);
		int count  = (Integer)updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if(count <= 0){
			updateResult = this.insert(params);
		}
		return updateResult;
	}
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("applyId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * queryGroupByCity
	 * @param params
	 * @return
	 */
	public AppResult queryGroupByCity(AppParam params) {
		return super.query(params, NAMESPACE, "queryGroupByCity");
	}

	/**
	 * updateAllotFlag
	 * @param params
	 * @return
	 */
	public AppResult updateAllotFlag(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateAllotFlag", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	

	/**
	 * deleteAll
	 * @param params
	 * @return
	 */
	public AppResult deleteAll(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteAll", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return backContext;
	}
	

	/**
	 *业务员分单
	 * @param params
	 * @return
	 */
	public AppResult storeAllotNewOrder(AppParam params){
		AppResult result = new AppResult();

		int totalSize = 0;//分给业务员的真实笔数
		int isNew = 0;// 是否是新单
		int orderType = NumberUtil.getInt(params.getAttr("orderType"),1);
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		AppResult storeAllotResult = new AppResult();
		
		if(1 == orderType) {
			storeAllotResult = this.getStoreAllotNewOrder(params);
		}else {
			storeAllotResult = this.getStoreAllotAgainOrder(params);
		}

		int size = storeAllotResult.getRows().size();
		if(size > 0){
			String customerId = StringUtil.getString(params.getAttr("customerId"));
			String recordDate = DateTimeUtil.toStringByParttern(new Date(), DateTimeUtil.DATE_PATTERN_YYYY_MM_DD);
			
			AppParam queryParams = new AppParam("custLevelService","queryLoginStatus");
			//查询该用户的等级配置信息
			Map<String,Object> gradeMap = new HashMap<String,Object>();//用户等级配置
			Map<String,Object> custMap = new HashMap<String,Object>();//用户基本参数信息
			
			for(Map<String,Object>storeAllotMap : storeAllotResult.getRows()){
				//查询当前用户的基本信息，每次判断是否能分单
				queryParams.addAttr("customerId", customerId);
				AppResult custResult =SoaManager.getInstance().invoke(queryParams);
				//更新分配数量的记录
				int allotCount = 0;
				AppParam queryParam = new AppParam("storeAllotRecordService","query");
				queryParam.addAttr("customerId", customerId);
				queryParam.addAttr("recordDate", recordDate);
				AppResult queryAloteResult = SoaManager.getInstance().invoke(queryParam);
				if(queryAloteResult.getRows().size()>0){
					allotCount = NumberUtil.getInt(queryAloteResult.getRow(0).get("allotNotFillCount"));
				}
				
				if(custResult.isSuccess() && custResult.getRows().size() > 0){
					custMap = custResult.getRow(0);
					custMap.put("agAllotCount", allotCount);
					custMap.put("orderType", orderType);
				}
				
				if((gradeMap == null || gradeMap.size() <=0) && custMap != null ){
					gradeMap = StoreSeparateUtils.getRankConfigByGrade(NumberUtil.getInt(custMap.get("gradeCode")));
				}
				
				AppParam storeApplyParam = new AppParam("borrowStoreApplyService","queryCount");
				storeApplyParam.addAttr("lastStore", customerId);
				AppResult storeApplyResult = SoaManager.getInstance().invoke(storeApplyParam);
				int storeCount = NumberUtil.getInt(StringUtil.getString(storeApplyResult.getAttr(DuoduoConstant.TOTAL_SIZE)));
				//所有分单数超过20笔才校验分单基本信息（即保证新人可以正常分单）
				if(storeCount > 20) {
					if(NumberUtil.getInt(custMap.get("isAllotOrder")) == 1){
						//对比该业务员的基本参数,不满足要求则退出分单
						if(!StoreAlllotUtils.compareParam(custMap, gradeMap)){
							log.info("can't alloct customerId:" + customerId);
							break;
						}
					}else{
						//准分单 分单数量限制
						if(!StoreAlllotUtils.compareSureParam(custMap, gradeMap)){
							log.info("can't sureAllot customerId:" + customerId);
							break;
						}
					}
				}
				
				String applyId = StringUtil.getString(storeAllotMap.get("applyId"));
				String applyName = StringUtil.getString(storeAllotMap.get("applyName"));

				int isCost = NumberUtil.getInt(storeAllotMap.get("isCost"),1);
				params.addAttr("applyId", applyId);
				//判断是否是二次申请，是则隐藏相关记录
				int applyCount = NumberUtil.getInt(storeAllotMap.get("applyCount"),1);
				if(applyCount > 1 && 1 == orderType){
					params.addAttr("isHideFlag", "1");
					params.addAttr("backStatus","1");//1 未退单
					params.addAttr("backDesc","");
					params.addAttr("backReDesc","");
					params.addAttr("custLabel","0");
				}
				int updateSize = getDao().update(NAMESPACE, "storeAllotOrderByUpdate", params.getAttr(), params.getDataBase());
				if(updateSize > 0){
					// 删除订单缓存
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
					
					// 如果是二次申请 则删除录音缓存
//					if(!StringUtils.isEmpty(params.getAttr("isHideFlag"))){
//						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_AUDIO_RECORD + applyId + StoreConstant.IS_ADMIN_FALSE);
//					}
					
					// 保存分配的新单,计算分单成本
					if(1 == orderType && 1 == isCost && !StringUtils.isEmpty(orgId)){
						AppParam updateCostParam = new AppParam("orgCostRecordService","update");
						updateCostParam.addAttr("orgId", orgId);
						updateCostParam.addAttr("applyId", applyId);
						updateCostParam.addAttr("customerId", customerId);
						SoaManager.getInstance().invoke(updateCostParam);
						
						if(applyCount > 1){
							Map<String,Object> dealMap = new HashMap<String, Object>();
							dealMap.put("applyId", applyId);
							dealMap.put("custLabel", "0");
							StoreOptUtil.dealStoreOrderByMq(null,"custLabelType", dealMap);
							//加入mq处理跟进记录变更
							StoreOptUtil.dealStoreOrderByMq(null,"handelRecordType", dealMap);
						}
					}
					
					//插入门店人员操作记录
					StoreOptUtil.insertStoreRecord(applyId, customerId, BorrowConstant.STORE_OPER_0, 
							"系统自动分单", 0, orderType, 0, 1);
					//删除分配表里面的数据
					AppParam tmpParam = new AppParam(); 
					tmpParam.addAttr("applyId", applyId);
					tmpParam.addAttr("orderType", orderType);
					this.delete(tmpParam);
					
					Map<String, Object> sendParam = new HashMap<String, Object>();		
					sendParam.put("recordDate", recordDate);//记录日期
					StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
					//同步orderStatus
					sendParam.put("applyId", applyId);
					sendParam.put("orderStatus", "-1");
					sendParam.put("orgId", params.getAttr("orgId"));
					sendParam.put("lastStore", customerId);
					if(1==isNew){
						sendParam.put("isNew", 1);
					}
					StoreOptUtil.dealStoreOrderByMq(customerId,"handelOrderType", sendParam);
					
					//发送分单消息通知
					AppParam allotParam = new AppParam();
					allotParam.addAttr("customerId", customerId);
					allotParam.addAttr("applyId", applyId);
					allotParam.addAttr("orderType", orderType);
					allotParam.addAttr("applyName", applyName);
					allotParam.addAttr("orgId", params.getAttr("orgId"));
					StoreOptUtil.sendAllotMeaasge(allotParam);
					
					totalSize ++;
					tmpParam = null;
					if(1== isCost){
						AppParam countParam = new AppParam();
						countParam.addAttr("customerId", customerId);
						countParam.addAttr("recordDate", recordDate);
						countParam.addAttr("totalSize", totalSize);
						countParam.addAttr("orderType", orderType);
						//更新分单数量
						StoreAlllotUtils.updateAllotCount(countParam);
					}
					
				}else{
					log.info("can't alloct applyId:" + applyId);
				}
			}
		}
		result.putAttr("realAllotCount", totalSize);
		return result;
	}

	/**
	 * 插入订单分配池
	 * @param treatyNo
	 * @param applyId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult insertAllot(AppParam params){
		AppResult result = new AppResult();
		String orderType = StringUtil.getString(params.getAttr("orderType"));
		int allotCount = 0;
		List<Map<String,Object>> listMap = (List<Map<String,Object>>) params.getAttr("orderList");
		String notSign = StringUtil.getString(params.getAttr("notSign"));
		for(Map<String,Object> map : listMap){
			AppParam queryParam = new AppParam();
			queryParam.addAttr("applyId", map.get("applyId"));
			AppResult quertResult = this.queryCount(queryParam);
			if(quertResult.isSuccess()){
				int size = Integer.valueOf(StringUtil.getString(quertResult.getAttr(DuoduoConstant.TOTAL_SIZE)));
				if(size == 0){
					AppParam applyParam = new AppParam();
					applyParam.addAttr("applyId", map.get("applyId"));
					applyParam.addAttr("cityName", map.get("cityName"));
					applyParam.addAttr("orderType", StringUtils.isEmpty(orderType) ? map.get("orderType") : orderType);
					applyParam.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
					applyParam.addAttr("applyTime", map.get("applyTime"));
					applyParam.addAttr("orgId", map.get("orgId"));
					applyParam.addAttr("lastStore", map.get("lastStore"));
					applyParam.addAttr("nextRecordDate", DateUtil.getSimpleFmt(new Date()));
					applyParam.addAttr("gradeType", map.get("grade"));
					result = this.insert(applyParam);
					allotCount ++ ;
					if(result.isSuccess()){
						AppParam updateParam = new AppParam("borrowStoreApplyService","update");
						updateParam.addAttr("applyId", map.get("applyId"));
						updateParam.addAttr("orderStatus", "-1");
						updateParam.addAttr("lastStore", "");
						if(!StringUtils.isEmpty(orderType)){
							updateParam.addAttr("orderType", orderType);
							SoaManager.getInstance().invoke(updateParam);
						}else{
							SoaManager.getInstance().invoke(updateParam);
						}
					
						Map<String, Object> msgParam = new HashMap<String, Object>();
						//同步orderStatus
						msgParam.put("applyId", map.get("applyId"));
						msgParam.put("orderStatus", "-1");
						msgParam.put("lastStore", "-1");//-1代表清除lastStore
						StoreOptUtil.dealStoreOrderByMq(StringUtil.getString(map.get("lastStore")),"handelOrderType", msgParam);
						
						//没签单的订单删除预约记录
						if(!StringUtils.isEmpty(notSign)){
							AppParam bookParam = new AppParam("treatBookService","delete");
							bookParam.addAttr("applyId", map.get("applyId"));
							SoaManager.getInstance().invoke(bookParam);
						}
						String lastStore = StringUtil.getString(map.get("lastStore"));
						//查询用户等级
						AppResult appResult = StoreOptUtil.queryCustLevel(lastStore);
						int dealOrderCount = 0;
						if(appResult.getRows().size() > 0 && !StringUtils.isEmpty(appResult.getRow(0))){
							dealOrderCount = NumberUtil.getInt(appResult.getRow(0).get("dealOrderCount"));
						}
						AppParam levelParam = new AppParam("custLevelService","update");
						levelParam.addAttr("customerId", lastStore);
						//处理中笔数不包含处理状态为-1的订单
						if(dealOrderCount > 0 && !"-1".equals(StringUtil.getString(map.get("orderStatus")))){
							dealOrderCount --;
							levelParam.addAttr("dealOrderCount", dealOrderCount);
							SoaManager.getInstance().invoke(levelParam);
						}
						// 插入操作记录
						StoreOptUtil.insertStoreRecord(map.get("applyId"),map.get("lastStore"),
								StoreConstant.STORE_OPER_29,params.getAttr("desc"), 0,  
								StringUtils.isEmpty(orderType) ? map.get("orderType") : orderType,0,1);
						
						//发送回收订单消息通知
						AppParam sendParam = new AppParam();
						sendParam.addAttr("realName", map.get("realName"));
						sendParam.addAttr("desc", params.getAttr("desc"));
						sendParam.addAttr("customerId", map.get("lastStore"));
						sendParam.addAttr("applyName", map.get("applyName"));
						sendParam.addAttr("orgId", map.get("orgId"));
						StoreOptUtil.sendRecyclingMeaasge(sendParam);
					}
				}
			}
		}
		result.putAttr("allotCount", allotCount);
		return result;
	}
	
	/**
	 *网销门店单子没有分配出去的去挂卖
	 * @param treatyNo
	 * @param applyId
	 * @return
	 */
	public AppResult storeOrderToSale(AppParam params){
		AppResult result = new AppResult();
		int size = getDao().update(NAMESPACE, "storeOrderToSale", params.getAttr(), params.getDataBase());
		if(size == 1){
			result = this.delete(params);
		}else{
			result.setSuccess(false);
			result.setMessage("无更新操作!");
		}
		return result;
	}

	
	/**
	 * 查询分配池统计情况
	 * @param params
	 * @return
	 */
	public AppResult queryAllotPondStatics(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryAllotPondStatics", "queryAllotPondStaticsCount");
	}
	
	/**
	 * 查询门店成本计算所需信息
	 * @param param
	 * @return
	 */
	public AppResult queryOrgCostInfo (AppParam param) {
		return super.query(param, NAMESPACE, "queryOrgCostInfo");
	}
	
	public AppResult queryTransferData (AppParam param) {
		return super.query(param, NAMESPACE, "queryTransferData");
	}
	
	public AppResult updateTransStatus(AppParam param) {
		AppResult result = new AppResult();
		int count = getDao().update(NAMESPACE, "updateTransStatus", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		return result;
	}
	
	public AppResult updateNetStatus (AppParam param) {
		AppResult result = new AppResult();
		result = this.delete(param);
		int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Delete_SIZE), 0);
		if (count > 0) {
			return result;
		}
		count = getDao().update(NAMESPACE, "updateNetStatus", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		return result;
	}
	
	/**
	 * 发送消息提醒
	 */
	public static void sendRemindMessage(AppParam param){
		try{
			String customerId = StringUtil.getString(param.getAttr("customerId"));
			String message = StringUtil.getString(param.getAttr("message"));
			StoreAppSend  storeAppSend = SpringAppContext.getBean(StoreAppSend.class);//发送app消息通知
			Map<String, Object> sendParam = new HashMap<String, Object>();
			sendParam.put("message", message);
			sendParam.put("notifyType", "1");
			sendParam.put("cmdName", "0006"); // 个人消息
			sendParam.put("success", "true");
			String uuid = StringUtil.getString(RedisUtils.getRedisService().get("app" +customerId));
			if(!StringUtils.isEmpty(uuid)){
				storeAppSend.sendAppMessage(uuid,"storeCmdType", sendParam);
			}
			
			StorePcSend  storePcSend = SpringAppContext.getBean(StorePcSend.class);	//发送PC消息通知
			Map<String, Object> sendPCParam = new HashMap<String, Object>();	
			String sessionId = StringUtil.getString(RedisUtils.getRedisService().get("pc" + customerId));
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			sendPCParam.put("message", message);
			sendPCParam.put("applyId", applyId);
			sendPCParam.put("customerId", customerId);
			sendPCParam.put("notifyType", "1");
			sendPCParam.put("success", "true");
			sendPCParam.put("cmdName","0006"); // 个人消息
			if(!StringUtils.isEmpty(sessionId)){
				storePcSend.sendPcMessage(sessionId, "storeCmdType", sendPCParam);
			}
			
			//保存通知消息
			Map<String, Object> saveParam = new HashMap<String, Object>();
			saveParam.put("notifyText", message);
			saveParam.put("notifyDate", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			saveParam.put("customerId", customerId);
			saveParam.put("orgId", StringUtil.getString(param.getAttr("orgId")));
			saveParam.put("messNotifyType", "2"); //2 个人通知消息
			StoreOptUtil.dealStoreOrderByMq(customerId,"orderNotifyType", saveParam);
		}catch(Exception e){
			log.error("NetStorePoolService sendRemindMessage 发送消息提醒 error", e);
		}
	}
	
	/**
	 * 二次分单处理
	 * @param param
	 */
	public static void againAllotOrderDeal(AppParam param){
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		AppParam updateApplyParam = new AppParam("borrowStoreApplyService", "update");
		//修改orderStatus
		updateApplyParam.addAttr("applyId", applyId);
		updateApplyParam.addAttr("orderStatus",param.getAttr("orderStatus"));
		updateApplyParam.addAttr("lastStore",customerId);
		updateApplyParam.addAttr("orgId",param.getAttr("orgId"));
		updateApplyParam.addAttr("backStatus","1");//1 未退单
		updateApplyParam.addAttr("backReDesc","");
		updateApplyParam.addAttr("backDesc","");
		updateApplyParam.addAttr("allotTime",new Date());
		AppResult updateResult = SoaManager.getInstance().invoke(updateApplyParam);
		int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
		if(updateSize > 0){
			updateApplyParam.setService("borrowApplyService");
			SoaManager.getInstance().invoke(updateApplyParam);
			//发送分单消息提醒
			sendRemindMessage(param);
		}
	}
	
	
	/**
	 * 二次申请订单不分单处理
	 * @param param
	 */
	public static void againNotAllotDeal(AppParam param){
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		AppParam updateApplyParam = new AppParam("borrowStoreApplyService", "update");
		//更新订单信息
		updateApplyParam.addAttr("applyId", applyId);
		updateApplyParam.addAttr("orderStatus","-1");
		updateApplyParam.addAttr("lastStore","");
		updateApplyParam.addAttr("custLabel","0");
		updateApplyParam.addAttr("orgId", "");
		AppResult updateResult = SoaManager.getInstance().invoke(updateApplyParam);
		int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
		if(updateSize > 0){
			Map<String,Object> dealMap = new HashMap<String, Object>();
			//同步custLabel
			dealMap.put("applyId", applyId);
			dealMap.put("custLabel", "0");
			StoreOptUtil.dealStoreOrderByMq(null,"custLabelType", dealMap);
			//加入mq处理跟进记录变更
			StoreOptUtil.dealStoreOrderByMq(null,"handelRecordType", dealMap);
			//同步处理人、状态、门店
			dealMap.put("orderStatus", "-1");
			dealMap.put("lastStore", "-1");
			dealMap.put("orgId", "");
			StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
		}
	}
	
	
	
	/************************************************ 再次开发******************************/
	
	public AppResult queryNetOrgAllotOrderCfg (AppParam param) {
		return super.query(param, NAMESPACE, "queryNetOrgAllotOrderCfg");
	}
	
	public AppResult queryOrgAllotOrder (AppParam param) {
		return super.query(param, NAMESPACE, "queryOrgAllotOrder");
	}
	/**
	 * allotOrgOrder
	 * @param params
	 * @return
	 */
	public AppResult allotOrgOrder(AppParam params) {
		AppResult result = new AppResult();
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		int orderType = NumberUtil.getInt(params.getAttr("orderType"),2);
		int allotOrderType = NumberUtil.getInt(params.getAttr("allotOrderType"),2);
		long needAllotCount = NumberUtil.getLong(params.getAttr("needAllotCount"),0);
		Object cityName = params.getAttr("cityName");
		if(!StringUtils.isEmpty(orgId) && !StringUtils.isEmpty(cityName) && needAllotCount > 0) {
			AppParam applyIdsParam = new AppParam();
			applyIdsParam.setDataBase(params.getDataBase());
			applyIdsParam.addAttr("orderType", orderType);
			applyIdsParam.addAttr("limitSize", needAllotCount);
			applyIdsParam.addAttr("cityName", cityName);
			
			if(allotOrderType == 1) {
				applyIdsParam.addAttr("channelTypeIN", "2,3");// 实时数据
			}else if(allotOrderType == 2){
				applyIdsParam.addAttr("channelType", "4");//历史数据
			}
			
			AppResult allotApplyIdsR = queryOrgAllotOrder(applyIdsParam);
			int allotSucSize = 0;
			if(allotApplyIdsR.getRows().size() > 0) {
				for(Map<String,Object> allotApplyIdsMap : allotApplyIdsR.getRows()) {
					try {
							Object applyId = allotApplyIdsMap.get("applyId");
							int isCost = NumberUtil.getInt(allotApplyIdsMap.get("isCost"), 0);
							boolean costFlag = true;
							
							if(isCost == 1 && orderType ==1) {// 先扣余额
								int channelType = NumberUtil.getInt(allotApplyIdsMap.get("channelType"));
								Object channelCode = allotApplyIdsMap.get("channelCode");
								
								costFlag = AllotCostUtil.saveOrgAllotOrderCost(orgId, applyId, channelType, channelCode,null).isSuccess();
							}
							
							if(costFlag) {
								AppParam allotParam = new AppParam();
								allotParam.addAttr("applyId", applyId);
								allotParam.addAttr("orgId", orgId);
								applyIdsParam.setDataBase(params.getDataBase());
								AppResult updateResult = this.updateOrderOrgId(allotParam);
								int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
								allotSucSize = allotSucSize + updateSize;
							}
							
					}catch (Exception e) {
						log.error("门店分单失败", e);
					}	
				}

			}
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, allotSucSize);
		}else {
			result.setSuccess(false);
			result.setMessage("分单缺少必要参数或者分单数为0");
		}

		return result;
	}
	
	/**
	 * queryGroupByOrgId
	 * @param params
	 * @return
	 */
	public AppResult queryGroupByOrgId(AppParam params) {
		return super.query(params, NAMESPACE, "queryGroupByOrgId");
	}
}
