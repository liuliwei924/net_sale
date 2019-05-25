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
import org.xxjr.store.util.AcedataUtil;
import org.xxjr.store.util.GeoDealUtils;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;

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
		AppResult custResult = new AppResult();//用户分单信息
		int totalSize = 0;//分给业务员的真实笔数
		int isNew = 0;// 是否是新单
		int orderType = NumberUtil.getInt(params.getAttr("orderType"),1);
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		//查询该用户的等级配置信息
		Map<String,Object> gradeMap = new HashMap<String,Object>();//用户等级配置
		Map<String,Object> custMap = new HashMap<String,Object>();//用户基本参数信息
		AppParam queryParams = new AppParam("custLevelService","queryLoginStatus");
		AppResult storeAllotResult = new AppResult();
		String cityName = StringUtil.getString(params.getAttr("cityName"));
		AppParam storeParams = new AppParam("netStorePoolService","getStoreAllotNewOrder");
		storeParams.addAttrs(params.getAttr());
		//限制新单每个门店的总分单数
		if(1 == orderType){
			boolean isOver = StoreAlllotUtils.isOverAllot(orgId);
			if(isOver){
				result.putAttr("realAllotCount", totalSize);
				return result;
			}
			if("上海市".equals(cityName)){
				//上海市的新申请订单不限制门店随机分给符合条件的人
				storeAllotResult = StoreAlllotUtils.queryStoreAllotByCity(storeParams);
			}else{
				//取出需要分配给业务员的新单
				params.setDataBase("main_");
				storeAllotResult = this.getStoreAllotNewOrder(params);
				params.setDataBase(null);
			}
		}else if(2 == orderType){
			if("上海市".equals(cityName)){
				//上海市的再分配订单不限制门店随机分给符合条件的人
				storeParams.setMethod("getStoreAllotAgainOrder");
				storeAllotResult = StoreAlllotUtils.queryStoreAllotByCity(storeParams);
			}else{
				//取出需要分配给业务员的再分配单
				params.setDataBase("main_");
				storeAllotResult = this.getStoreAllotAgainOrder(params);
				params.setDataBase(null);
			}
		}
		int size = storeAllotResult.getRows().size();
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String recordDate = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
		if(size > 0){
			for(Map<String,Object>storeAllotMap : storeAllotResult.getRows()){
				//查询当前用户的基本信息，每次判断是否能分单
				queryParams.addAttr("customerId", customerId);
				custResult =SoaManager.getInstance().invoke(queryParams);
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
				//配置的分配新单总数
				int allotNewCount = 0;
				if((gradeMap == null || gradeMap.size() <=0) && custMap != null ){
					gradeMap = StoreSeparateUtils.getRankConfigByGrade(NumberUtil.getInt(custMap.get("gradeCode")));
					allotNewCount = NumberUtil.getInt(gradeMap.get("allotNewCount"),150);
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
				//集奥多头借贷查询标识
				int geoMuchLoanFlag = SysParamsUtil.getIntParamByKey("geoMuchLoanFlag", 0);
				//只对新单进行多头借贷查询且geoMuchLoanFlag = 1才查询
				if(1 == orderType && 1 == geoMuchLoanFlag){
					AppParam geoParam = new AppParam();
					geoParam.addAttr("applyId", applyId);
					geoParam.addAttr("applyName", applyName);
					geoParam.addAttr("telephone", storeAllotMap.get("telephone"));
					geoParam.addAttr("orderType", orderType);
					geoParam.addAttr("customerId", customerId);
					geoParam.addAttr("delFlag", "1");
					//调用优分多头借贷接口判断是否需要继续分单
					boolean isAllotFlag = this.isContinueByAceData(geoParam);
					if(!isAllotFlag){
						break;
					}
				}
				int isCost = NumberUtil.getInt(storeAllotMap.get("isCost"),1);
				params.addAttr("applyId", applyId);
				//判断是否是二次申请，是则隐藏相关记录
				int applyCount = NumberUtil.getInt(storeAllotMap.get("applyCount"),1);
				if(applyCount > 1 && 1 == orderType && 1 == isCost){
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
					if(!StringUtils.isEmpty(params.getAttr("isHideFlag"))){
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_AUDIO_RECORD + applyId + StoreConstant.IS_ADMIN_FALSE);
					}
					
					// 保存分配的新单,计算分单成本
					if(1 == orderType && 1 == isCost){
						String lastStore = StringUtil.getString(storeAllotMap.get("lastStore"));
						String applyTime = StringUtil.getString(storeAllotMap.get("applyTime"));
						if(!StringUtils.isEmpty(applyTime) && StringUtils.isEmpty(lastStore)){
							AppParam costParam = new AppParam();
							costParam.addAttr("applyTime", applyTime);
							costParam.addAttr("customerId", customerId);
							costParam.addAttr("applyId", applyId);
							costParam.addAttr("orgId", orgId);
							//计算订单成本
							AllotCostUtil.computeAllotOrderCost(costParam);
							isNew = 1;
							//二次申请同步custLabel
							if(applyCount > 1){
								Map<String,Object> dealMap = new HashMap<String, Object>();
								dealMap.put("applyId", applyId);
								dealMap.put("custLabel", "0");
								StoreOptUtil.dealStoreOrderByMq(null,"custLabelType", dealMap);
								//加入mq处理跟进记录变更
								StoreOptUtil.dealStoreOrderByMq(null,"handelRecordType", dealMap);
							}
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
					if(1 == orderType){
						// 业务员当前成本单分单总数
						int allotNewOrderCount = StoreSeparateUtils.queryAllotNewOrderCount(customerId);
						//发送暂停分单提醒
						StoreOptUtil.sendPauseAllotNotify(customerId, allotNewOrderCount,allotNewCount);
					}
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
	 * 新订单立即分配
	 * @param cityName
	 * @param allotFlag
	 */
	public AppResult newOrderNowAllot(AppParam param){
		boolean isInsertNet = false;
		int isNew = 0; // 是否是新单
		AppResult result = new AppResult();
		String cityName =StringUtil.getString(param.getAttr("cityName"));
		// 查询最优分配人
		AppResult allotResult = new AppResult();
		AppParam storeParam = new AppParam("custLevelService", "queryLessOrderAllot");
		storeParam.addAttr("cityName", cityName);
		allotResult = SoaManager.getInstance().invoke(storeParam);
		// 如果不存在未满足基本单量的人，则查询已满足基本单量的人
		if(!allotResult.isSuccess() || allotResult.getRows().size() <= 0){
			storeParam.setMethod("queryMoreOrderAllot");
			allotResult = SoaManager.getInstance().invoke(storeParam);
		}
		if(allotResult.isSuccess() && allotResult.getRows().size() > 0){
			Map<String,Object> allotMap = allotResult.getRow(0);
			String orgId = StringUtil.getString(allotMap.get("orgId"));
			//限制门店分单数
			boolean isOver = StoreAlllotUtils.isOverAllot(orgId);
			if(isOver){
				result.putAttr("isInsertNet", true);
				return result;
			}
			int gradeCode = NumberUtil.getInt(StringUtil.getString(allotMap.get("gradeCode")),1);
			//今日新分配单量
			int allotSeniorCount = NumberUtil.getInt(StringUtil.getString(allotMap.get("allotSeniorCount")),0);
			Map<String,Object> rankMap = StoreSeparateUtils.getRankConfigByGrade(gradeCode);
			if(rankMap == null){
				isInsertNet = true;
				result.putAttr("isInsertNet", isInsertNet);
				return result;
			}
			String strCount = StringUtil.getString(rankMap.get("maxCount"));
			//最大分单量
			int maxCount = 0;
			int days = 1;
			if(strCount.contains("/")){
				String[] array = strCount.split("/");
				maxCount = NumberUtil.getInt(array[0],0);
				days = NumberUtil.getInt(array[1],1);
				maxCount = maxCount / days;
			}else{
				maxCount = NumberUtil.getInt(strCount,0);
			}
			
			if(allotSeniorCount >= maxCount ){
				isInsertNet = true;
				result.putAttr("isInsertNet", isInsertNet);
				return result;
			}
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			String recordDate = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
			AppParam storeApplyParam = new AppParam("borrowStoreApplyService", "query");
			storeApplyParam.addAttr("applyId", applyId);
			AppResult storeApplyResult = SoaManager.getInstance().invoke(storeApplyParam);
			if(storeApplyResult.getRows().size() > 0){
				Map<String,Object> applyMap = storeApplyResult.getRow(0);
				String lastStore = StringUtil.getString(applyMap.get("lastStore"));
				AppParam updateApplyParam = new AppParam();
				updateApplyParam.addAttr("applyId", applyId);
				if(StringUtils.isEmpty(lastStore)){
					String customerId = StringUtil.getString(allotMap.get("customerId"));
					boolean isNowAllotFlag = StoreAlllotUtils.isNowAllotFlag(customerId);
					if(isNowAllotFlag){
						result.putAttr("isInsertNet", true);
						return result;
					}
					updateApplyParam.addAttr("customerId", customerId);
					updateApplyParam.addAttr("orgId", allotMap.get("orgId"));
					//判断是否是二次申请，是则隐藏相关记录
					int applyCount = NumberUtil.getInt(applyMap.get("applyCount"),1);
					if(applyCount > 1){
						updateApplyParam.addAttr("isHideFlag", "1");
						updateApplyParam.addAttr("backStatus","1");//1 未退单
						updateApplyParam.addAttr("backDesc","");
						updateApplyParam.addAttr("backReDesc","");
						updateApplyParam.addAttr("custLabel","0");
					}
					//集奥多头借贷查询标识
					int geoMuchLoanFlag = SysParamsUtil.getIntParamByKey("geoMuchLoanFlag", 0);
					String applyName = StringUtil.getString(param.getAttr("applyName"));
					//只有geoMuchLoanFlag = 1才查询
					if(1 == geoMuchLoanFlag){
						AppParam geoParam = new AppParam();
						geoParam.addAttr("applyId", applyId);
						geoParam.addAttr("applyName", applyName);
						geoParam.addAttr("telephone", applyMap.get("telephone"));
						geoParam.addAttr("customerId", customerId);
						geoParam.addAttr("orderType", "1");
						//调用集奥多头借贷接口判断是否需要继续分单
						boolean isAllotFlag = this.isContinueByAceData(geoParam);
						if(!isAllotFlag){
							result.putAttr("isInsertNet", isInsertNet);
							return result;
						}
					}
					int updateSize = getDao().update(NAMESPACE, "storeAllotOrderByUpdate", updateApplyParam.getAttr(), updateApplyParam.getDataBase());
					if(updateSize > 0){
						// 删除订单缓存
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
						// 如果是二次申请 则删除录音缓存
						if(!StringUtils.isEmpty(updateApplyParam.getAttr("isHideFlag"))){
							RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_AUDIO_RECORD + applyId + StoreConstant.IS_ADMIN_FALSE);
						}
						
						String applyTime = StringUtil.getString(applyMap.get("applyTime"));
						if(!StringUtils.isEmpty(applyTime)){
							AppParam costParam = new AppParam();
							costParam.addAttr("applyTime", applyTime);
							costParam.addAttr("customerId", customerId);
							costParam.addAttr("applyId", applyId);
							costParam.addAttr("orgId", orgId);
							//计算订单成本
							AllotCostUtil.computeAllotOrderCost(costParam);
							isNew = 1;
						}
						//插入门店人员操作记录
						StoreOptUtil.insertStoreRecord(applyId, customerId, BorrowConstant.STORE_OPER_0, 
								"系统立即分单", 0, 1, 0, 1);
					
						Map<String, Object> sendParam = new HashMap<String, Object>();
						sendParam.put("recordDate", recordDate);//记录日期
						StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
						//同步orderStatus
						sendParam.put("applyId", applyId);
						sendParam.put("orderStatus", "-1");
						sendParam.put("lastStore", customerId);
						sendParam.put("orgId", allotMap.get("orgId"));
						if(1 == isNew){
							sendParam.put("isNew", isNew);
						}
						StoreOptUtil.dealStoreOrderByMq(customerId,"handelOrderType", sendParam);
						
						//发送分单消息通知
						AppParam allotParam = new AppParam();
						allotParam.addAttr("customerId", customerId);
						allotParam.addAttr("applyId", applyId);
						allotParam.addAttr("orderType", 1);
						allotParam.addAttr("orgId", allotMap.get("orgId"));
						allotParam.addAttr("applyName", applyName);
						StoreOptUtil.sendAllotMeaasge(allotParam);
						
						AppParam countParam = new AppParam();
						countParam.addAttr("customerId", customerId);
						countParam.addAttr("recordDate", recordDate);
						countParam.addAttr("totalSize", 1);
						countParam.addAttr("orderType", 1);
						//更新分单数量
						StoreAlllotUtils.updateAllotCount(countParam);
						//二次申请同步custLabel
						if(applyCount > 1){
							Map<String,Object> dealMap = new HashMap<String, Object>();
							dealMap.put("applyId", applyId);
							dealMap.put("custLabel", "0");
							StoreOptUtil.dealStoreOrderByMq(null,"custLabelType", dealMap);
							//加入mq处理跟进记录变更
							StoreOptUtil.dealStoreOrderByMq(null,"handelRecordType", dealMap);
						}
					}else{
						isInsertNet = true;
						log.info("can't now alloct applyId:" + applyId);
					}
				}
			}
		}else{
			isInsertNet = true;
		}
		result.putAttr("isInsertNet", isInsertNet);
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
	 * 是否需要继续分单
	 * @param param
	 * @return
	 */
	public boolean isContinueAllotOrder(AppParam param){
		boolean isAllotFlag = true;
		try{
			String applyName = StringUtil.getString(param.getAttr("applyName"));
			String telephone = StringUtil.getString(param.getAttr("telephone"));
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			//查询订单是否已经查询过集奥多头借贷
			AppParam queryParam = new AppParam("borrowRiskRecordService","queryCount");
			queryParam.addAttr("applyId", applyId);
			queryParam.addAttr("riskType", StoreConstant.BORROW_RISK_TYPE_1);
			queryParam.addAttr("platfType", StoreConstant.PLAT_FORM_TYPE_1);
			//-9998未击中 -9999击中
			queryParam.addAttr("respcodeIn", "-9998,-9999");
			AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
			int count = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
			if(count > 0){
				return isAllotFlag;
			}
			AppParam geoParams = new AppParam();
			//校验姓名是否都是汉字，如果不是则为未知
			geoParams.addAttr("applyName", StoreOptUtil.isChinese(applyName));
			geoParams.addAttr("telephone", telephone);
			geoParams.addAttr("applyId", applyId);
			AppResult geoResult = GeoDealUtils.getGeoData(geoParams);
			AppParam updateParam = new AppParam("borrowStoreApplyService","update");
			updateParam.addAttr("applyId", applyId);
			int day180appTimes = 0;
			String respcode = "";
			if(geoResult.isSuccess()){
				respcode = StringUtil.getString(geoResult.getAttr("respcode"));
				if(StoreConstant.GEO_RESPONSE_CODE_YES.equals(respcode)){
					day180appTimes = NumberUtil.getInt(geoResult.getAttr("day180appTimes"),0);
					if(day180appTimes > StoreConstant.STORE_MUCH_LOAN_STATUS_3){
						updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_4);
						updateParam.addAttr("orderStatus", StoreConstant.STORE_ORDER_7);
						updateParam.addAttr("allotDesc", "多头借贷自动转为无效单");
						AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
						if(updateResult.isSuccess()){
							// 插入操作记录
							StoreOptUtil.insertStoreRecord(applyId,param.getAttr("customerId"),StoreConstant.STORE_OPER_27,
									"多头借贷自动转为无效单",0,param.getAttr("orderType"),1,1);
							String delFlag = StringUtil.getString(param.getAttr("delFlag"));
							//判断是否删除分配表的数据
							if(!StringUtils.isEmpty(delFlag)){
								AppParam tmpParam = new AppParam();
								tmpParam.addAttr("applyId", applyId);
								this.delete(tmpParam);
							}
							//同步orderStatus
							Map<String,Object> dealMap = new HashMap<String, Object>();
							dealMap.put("applyId", applyId);
							dealMap.put("orderStatus", StoreConstant.STORE_ORDER_7);
							StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
						}
						isAllotFlag = false;
					}else{
						updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_3);
						SoaManager.getInstance().invoke(updateParam);
					}
				}else if(StoreConstant.GEO_RESPONSE_CODE_NO.equals(respcode)){
					updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_2);
					SoaManager.getInstance().invoke(updateParam);
				}
			}else{
				updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_5);
				SoaManager.getInstance().invoke(updateParam);
			}
			//mq同步风控查询记录表
			Map<String, Object> msgGeoParam = new HashMap<String, Object>();
			msgGeoParam.put("applyId", applyId);
			msgGeoParam.put("applyName", applyName);
			msgGeoParam.put("telephone", telephone);
			msgGeoParam.put("day180appTimes", day180appTimes);
			msgGeoParam.put("respcode", StringUtils.isEmpty(geoResult.getErrorCode()) ? respcode : geoResult.getErrorCode());
			msgGeoParam.put("respMessage", geoResult.getMessage());
			msgGeoParam.put("jsonText", geoResult.getAttr("jsonText"));
			StoreOptUtil.dealStoreOrderByMq(null,"borrowRiskType", msgGeoParam);
		}catch(Exception e){
			log.error("isContinueAllotOrder 判断是否需要继续分单操作 error", e);
		}
		return isAllotFlag;
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
	
	
	
	/**
	 * 查询优分接口判断是继续分单
	 * @param param
	 * @return
	 */
	public boolean isContinueByAceData(AppParam param){
		boolean isAllotFlag = true;
		try{
			String applyName = StringUtil.getString(param.getAttr("applyName"));
			String telephone = StringUtil.getString(param.getAttr("telephone"));
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			//查询订单是否已经查询过优分多头借贷
			AppParam queryParam = new AppParam("borrowRiskRecordService","queryCount");
			queryParam.addAttr("applyId", applyId);
			queryParam.addAttr("riskType", StoreConstant.BORROW_RISK_TYPE_1);
			queryParam.addAttr("platfType", StoreConstant.PLAT_FORM_TYPE_2);
			//2007未击中 2012击中 2013处理失败
			queryParam.addAttr("respcodeIn", "2007,200,2013");
			AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
			int count = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
			if(count > 0){
				return isAllotFlag;
			}
			AppParam aceParams = new AppParam();
			aceParams.addAttr("telephone", telephone);
			AppResult aceResult = AcedataUtil.getAceData(aceParams);
			AppParam updateParam = new AppParam("borrowStoreApplyService","update");
			updateParam.addAttr("applyId", applyId);
			int day180appTimes = 0;
			String respcode = "";
			if(aceResult.isSuccess()){
				respcode = StringUtil.getString(aceResult.getAttr("respcode"));
				if(StoreConstant.ACE_RESPONSE_CODE_YES.equals(respcode)){
					day180appTimes = NumberUtil.getInt(aceResult.getAttr("day180appTimes"),0);
					if(day180appTimes >= StoreConstant.STORE_MUCH_LOAN_STATUS_3){
						updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_4);
						updateParam.addAttr("orderStatus", StoreConstant.STORE_ORDER_7);
						updateParam.addAttr("allotDesc", "多头借贷自动转为无效单");
						AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
						if(updateResult.isSuccess()){
							// 插入操作记录
							StoreOptUtil.insertStoreRecord(applyId,param.getAttr("customerId"),StoreConstant.STORE_OPER_27,
									"多头借贷自动转为无效单",0,param.getAttr("orderType"),1,1);
							String delFlag = StringUtil.getString(param.getAttr("delFlag"));
							//判断是否删除分配表的数据
							if(!StringUtils.isEmpty(delFlag)){
								AppParam tmpParam = new AppParam();
								tmpParam.addAttr("applyId", applyId);
								this.delete(tmpParam);
							}
							//同步orderStatus
							Map<String,Object> dealMap = new HashMap<String, Object>();
							dealMap.put("applyId", applyId);
							dealMap.put("orderStatus", StoreConstant.STORE_ORDER_7);
							StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
						}
						isAllotFlag = false;
					}else{
						updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_3);
						SoaManager.getInstance().invoke(updateParam);
					}
				}else if(StoreConstant.ACE_RESPONSE_CODE_NO.equals(respcode)){
					updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_2);
					SoaManager.getInstance().invoke(updateParam);
				}
			}else{
				updateParam.addAttr("muLoanStatus", StoreConstant.STORE_MUCH_LOAN_STATUS_5);
				SoaManager.getInstance().invoke(updateParam);
			}
			//mq同步风控查询记录表
			Map<String, Object> msgAceParam = new HashMap<String, Object>();
			msgAceParam.put("applyId", applyId);
			msgAceParam.put("applyName", applyName);
			msgAceParam.put("telephone", telephone);
			msgAceParam.put("day180appTimes", day180appTimes);
			msgAceParam.put("respcode", StringUtils.isEmpty(aceResult.getErrorCode()) ? respcode : aceResult.getErrorCode());
			msgAceParam.put("respMessage", aceResult.getMessage());
			msgAceParam.put("jsonText", aceResult.getAttr("jsonText"));
			StoreOptUtil.dealStoreOrderByMq(null,"borrowRiskType", msgAceParam);
		}catch(Exception e){
			log.error("isContinueAllotOrder 判断是否需要继续分单操作 error", e);
		}
		return isAllotFlag;
	}
}
