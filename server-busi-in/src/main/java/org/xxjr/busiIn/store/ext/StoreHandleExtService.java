package org.xxjr.busiIn.store.ext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ValidUtils;

/***
 * 门店操作处理业务扩展类
 * @author ZQH
 *
 */
@Lazy
@Service
public class StoreHandleExtService extends BaseService{
	public static final String NAMESPACE = "STOREHANDLEEXT";

	
	/**
	 * 星级处理
	 * 
	 * @param params
	 */
	public AppResult custLabelDeal(AppParam params) {
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//管理员可以执行星级处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
				|| !StringUtils.isEmpty(adminCustomerId)){
			//判断是否已签单，签单了不能更改成2星+或以下星级 
			AppParam signParams = new AppParam("treatInfoService","queryCount");
			signParams.addAttr("applyId", applyId);
			AppResult applyResult = SoaManager.getInstance().invoke(signParams);
			int count = NumberUtil.getInt(applyResult.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
			if(count > 0){
				if(!StoreConstant.STORE_CUST_LABEL_4.equals(custLabel) 
						&& !StoreConstant.STORE_CUST_LABEL_5.equals(custLabel)){
					return CustomerUtil.retErrorMsg("此订单已签单，不能更改成2星+或以下星级！");
				}
			}
			AppParam applyParams = new AppParam("borrowStoreApplyService","update");
			applyParams.addAttr("applyId", applyId);
			applyParams.addAttr("custLabel", custLabel);
			applyParams.addAttr("custLabelUpTime", new Date());
			result = SoaManager.getInstance().invoke(applyParams);
			int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
			if(result.isSuccess() && updateSize == 1) {
				Map<String, Object> dealMap = new HashMap<String, Object>();
				//同步custLabel
				dealMap.put("applyId", applyId);
				dealMap.put("custLabel", custLabel);
				StoreOptUtil.dealStoreOrderByMq(customerId,"custLabelType", dealMap);
				
				// 删除订单缓存
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
			}
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行客户星级处理");
		}
	}

	/***
	 * 放弃跟进订单
	 * @param params
	 * @return
	 */
	public AppResult abandonFollowOrder(AppParam params) {
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId")); 
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//管理员可以执行
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
				|| !StringUtils.isEmpty(adminCustomerId)){
			AppParam queryParam = new AppParam("netStorePoolService","queryCount");
			queryParam.addAttr("applyId", params.getAttr("applyId"));
			AppResult quertResult = SoaManager.getInstance().invoke(queryParam);
			int size = NumberUtil.getInt(quertResult.getAttr(DuoduoConstant.TOTAL_SIZE));
			if(size > 0){
				result.setSuccess(false);
				result.setMessage("订单暂未分配，不能放弃跟进");
				return result;
			}
			AppParam orderParam = new AppParam();
			orderParam.addAttr("applyId", params.getAttr("applyId"));
			AppResult orderResult = this.queryOrderInfo(orderParam);
			if(orderResult.getRows().size() > 0 && !StringUtils.isEmpty(orderResult.getRow(0))){
				Map<String,Object> map = orderResult.getRow(0);
				String orderStatus = StringUtil.getString(map.get("orderStatus"));
				if(StoreConstant.STORE_ORDER_f1.equals(orderStatus) || StoreConstant.STORE_ORDER_0.equals(orderStatus) || 
						StoreConstant.STORE_ORDER_1.equals(orderStatus) || StoreConstant.STORE_ORDER_2.equals(orderStatus)){
					AppParam applyParam = new AppParam("netStorePoolService","insert");
					String orderType = StringUtil.getString(map.get("orderType"));
					if(!StoreConstant.STORE_ORDER_f1.equals(orderStatus)){
						orderType ="2"; // 2是再分配单
					}
					applyParam.addAttr("applyId", map.get("applyId"));
					applyParam.addAttr("cityName", map.get("cityName"));
					applyParam.addAttr("orderType", orderType);
					applyParam.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
					applyParam.addAttr("applyTime", map.get("applyTime"));
					applyParam.addAttr("orgId", map.get("orgId"));
					applyParam.addAttr("lastStore", map.get("lastStore"));
					applyParam.addAttr("nextRecordDate", params.getAttr("nextRecordDate"));
					applyParam.addAttr("gradeType", map.get("grade"));
					result = SoaManager.getInstance().invoke(applyParam);
					if(result.isSuccess()){
						AppParam updateParam = new AppParam("borrowStoreApplyService","update");
						updateParam.addAttr("orderStatus", "-1");
						updateParam.addAttr("lastStore", "");
						updateParam.addAttr("applyId", map.get("applyId"));
						updateParam.addAttr("orderType", orderType);
						SoaManager.getInstance().invoke(updateParam);

						AppParam bookParam = new AppParam("treatBookService","delete");
						bookParam.addAttr("applyId", map.get("applyId"));
						SoaManager.getInstance().invoke(bookParam);

						// 插入操作记录
						StoreOptUtil.insertStoreRecord(applyId,StringUtils.isEmpty(adminCustomerId) ? customerId: adminCustomerId,
								StoreConstant.STORE_OPER_30,"放弃跟进订单", 0, orderType,0,1);
						
						Map<String, Object> dealMap = new HashMap<String, Object>();
						//同步统计
						dealMap.put("recordDate", DateUtil.getSimpleFmt(new Date()));
						StoreOptUtil.dealStoreOrderByMq(lastStore,"countDealType", dealMap);
						
						//同步状态
						dealMap.put("applyId", map.get("applyId"));
						dealMap.put("orderStatus", "-1");
						dealMap.put("lastStore", "-1");//-1代表清除lastStore
						StoreOptUtil.dealStoreOrderByMq(lastStore,"handelOrderType", dealMap);
					}
				}else{
					result.setSuccess(false);
					result.setMessage("当前订单状态不允许放弃跟进处理！");
					return result;
				}
			}else{
				return orderResult;
			}

		}else{
			result.setSuccess(false);
			result.setMessage("你不是当前处理人，不能进行放弃跟进处理");
		}

		return result;
	}

	/**
	 * 查询所有列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryAllOrderList(AppParam params) {
		return super.queryByPage(params,NAMESPACE,"queryAllOrderList","queryAllOrderListCount");
	}


	/**
	 * 查询预约中或已上门列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryBookOrderList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryBookOrderList", "queryBookOrderListCount");
	}
	
	/**
	 * queryBookOrderListCount
	 * @param params
	 * @return
	 */
	public AppResult queryBookOrderListCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryBookOrderListCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/***
	 * 变更订单状态
	 * @param params
	 * @return
	 */
	public AppResult updateOrderStatus(AppParam params){
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));

		//查询订单信息
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String reCustomerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));		
		//管理员可以执行修改状态处理
		if((StringUtils.hasText(lastStore) && reCustomerId.equals(lastStore)) || !StringUtils.isEmpty(adminCustomerId)){
			int size = this.getDao().update(NAMESPACE, "updateOrderStatus", params.getAttr(), params.getDataBase());
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
			if(size >0){
				String orderStatus = StringUtil.getString(params.getAttr("orderStatus"));
				String handleType = StoreConstant.STORE_OPER_21;
				if(StoreConstant.STORE_ORDER_0.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_21;
				}else if(StoreConstant.STORE_ORDER_1.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_32;
				}else if(StoreConstant.STORE_ORDER_2.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_22;
				}else if(StoreConstant.STORE_ORDER_3.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_23;
				}else if(StoreConstant.STORE_ORDER_4.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_24;
				}else if(StoreConstant.STORE_ORDER_5.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_25;
				}else if(StoreConstant.STORE_ORDER_6.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_26;
				}else if(StoreConstant.STORE_ORDER_7.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_27;
				}else if(StoreConstant.STORE_ORDER_8.equals(orderStatus)){
					handleType = StoreConstant.STORE_OPER_28;
				}

				// 插入操作记录
				StoreOptUtil.insertStoreRecord(applyId,StringUtils.isEmpty(adminCustomerId) ? customerId : adminCustomerId, handleType,
						null, 0,applyInfo.get("orderType"),1,1);
				//当状态不为0、1、2时删除专属单
				if(!StoreConstant.STORE_ORDER_0.equals(orderStatus) && 
						!StoreConstant.STORE_ORDER_1.equals(orderStatus) &&
						!StoreConstant.STORE_ORDER_2.equals(orderStatus)){
					AppParam deleteParam = new AppParam("exclusiveOrderService","delete");
					deleteParam.addAttr("applyId", applyId);
					SoaManager.getInstance().invoke(deleteParam);
				}
				//同步处理
				if(result.isSuccess()){
					Map<String,Object> dealMap = new HashMap<String, Object>();
					//同步统计
					dealMap.put("recordDate", DateUtil.getSimpleFmt(new Date()));
					StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType" , dealMap);

					//同步orderStatus
					dealMap.put("applyId", applyId);
					dealMap.put("orderStatus", orderStatus);
					StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
				}
				
				// 删除订单缓存
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);

			}
		}else{
			result.setSuccess(false);
			result.setMessage("你不是当前处理人，不能进行状态处理");
		}
		return result;
	}

	/***
	 * 查询我的所有订单
	 * @param params
	 * @return
	 */
	public AppResult queryAllList(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryAllList", "queryAllListCount");
	}
	/**
	 * queryAllListCount
	 * @param params
	 * @return
	 */
	public AppResult queryAllListCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryAllListCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/***
	 * 风控查询列表   查询所有订单
	 * @param params
	 * @return
	 */
	public AppResult queryRiskAllList(AppParam params){
		if(params.getCurrentPage()==-1){
			params.setEveryPage(500);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE, "queryRiskAllList", "queryRiskAllListCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "queryRiskAllList", "queryRiskAllListCount");
		}
	}

	/***
	 * 查询进件项目列表
	 * @param params
	 * @return
	 */
	public AppResult queryContractList(AppParam params){
		return super.queryByPage(params,NAMESPACE,"queryContractList","queryContractListCount");
	}

	/***
	 * 查询我的所有订单统计
	 * @param params
	 * @return
	 */
	public AppResult queryAllSummary(AppParam params){
		return super.query(params,NAMESPACE,"queryAllSummary");
	}

	/**
	 * 查询下一笔订单
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNextOrder(AppParam params) {
		return super.query(params, NAMESPACE, "queryNextOrder");
	}
	/**
	 * 查询没有处理的订单
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNotDealOrder(AppParam params) {
		return super.query(params, NAMESPACE, "queryNotDealOrder");
	}

	/**
	 * 查询没有上门的订单
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNotVisitOrder(AppParam params) {
		return super.query(params, NAMESPACE, "queryNotVisitOrder");
	}

	/**
	 * 查询没有签单的订单
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNotSignOrder(AppParam params) {
		return super.query(params, NAMESPACE, "queryNotSignOrder");
	}

	/**
	 * 查询客户订单
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryCustOrderInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryCustOrderInfo");
	}

	/**
	 * 查询订单基本信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrderInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrderInfo");
	}

	/**
	 * 查询没有处理的订单（按处理人分组）
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNotDealOrderGroup(AppParam params) {
		return super.query(params, NAMESPACE, "queryNotDealOrderGroup");
	}

	/**
	 * 查询没有上门的订单（按处理人分组）
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNotVisitOrderGroup(AppParam params) {
		return super.query(params, NAMESPACE, "queryNotVisitOrderGroup");
	}

	/**
	 * 查询没有签单的订单（按处理人分组）
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryNotSignOrderGroup(AppParam params) {
		return super.query(params, NAMESPACE, "queryNotSignOrderGroup");
	}
	/**
	 * 导入门店历史数据
	 * @param params
	 * @return
	 */
	public AppResult batchImportData(AppParam params){
		AppResult sucResult = new AppResult();

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> dataList = (List<Map<String,Object>>)params.getAttr("dataList");
		StringBuilder serIdSb = new StringBuilder();
		int errCount = 0;
		int sucCount = 0;
		for(Map<String, Object> dataMap : dataList){
			String id = StringUtil.getString(dataMap.get("id"));
			String applyName = StringUtil.getString(dataMap.get("applyName"));
			String telephone = StringUtil.getString(dataMap.get("telephone"));
			String desc = StringUtil.getString(dataMap.get("desc"));

			if(StringUtils.isEmpty(id)){//忽略计算
				continue;
			}

			if(StringUtils.isEmpty(applyName) || 
					!ValidUtils.validateTelephone(telephone)){
				serIdSb.append(id).append("号码或名字有误").append(",");
				errCount++;
				continue;
			}

			// 效验手机号 存在借款记录给出提示
			AppParam queryParam = new AppParam();
			queryParam.addAttr("telephone", telephone);
			queryParam.setService("borrowApplyService");
			queryParam.setMethod("querySimpleInfo");
			AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
			if (queryResult.getRows().size() > 0) {
				serIdSb.append(id).append("记录已存在").append(",");
				errCount++;
				continue;
			}


			//保存第三方数据
			AppParam applyParam = new AppParam();
			applyParam.setService("borrowApplyService");
			applyParam.setMethod("insert");
			applyParam.addAttr("applyName", applyName);
			applyParam.addAttr("applyType", 1);
			applyParam.addAttr("orgId", params.getAttr("orgId"));
			applyParam.addAttr("telephone", telephone);
			applyParam.addAttr("storeStatus", 1);
			applyParam.addAttr("status", 2);
			applyParam.addAttr("haveDetail", 1);
			applyParam.addAttr("orderType", 2);//再分配
			applyParam.addAttr("stageStatus", 1);
			applyParam.addAttr("channelDetail", "netSale");
			applyParam.addAttr("channelCode", "netSale");
			applyParam.addAttr("applyTime", new Date());
			AppResult result = SoaManager.getInstance().invoke(applyParam);
			int applyId = NumberUtil.getInt(result.getAttr("applyId"));
			//保存第三方数据
			AppParam newApplyParam = new AppParam();
			newApplyParam.setService("borrowStoreApplyService");
			newApplyParam.setMethod("insertStoreApply");
			newApplyParam.addAttr("applyId", applyId);
			newApplyParam.addAttr("applyName", applyName);
			newApplyParam.addAttr("applyType", 1);
			newApplyParam.addAttr("orgId", params.getAttr("orgId"));
			newApplyParam.addAttr("telephone", telephone);
			newApplyParam.addAttr("status", 2);
			newApplyParam.addAttr("haveDetail", 1);
			newApplyParam.addAttr("orderType", 2);//再分配
			newApplyParam.addAttr("channelDetail", "netSale");
			newApplyParam.addAttr("channelCode", "netSale");
			newApplyParam.addAttr("applyTime", new Date());
			SoaManager.getInstance().invoke(newApplyParam);

			AppParam reParams = new AppParam();
			// 增加贷款基本信息
			if (result.isSuccess()) {
				reParams.addAttr("applyId", applyId);
				reParams.addAttr("cityName", params.getAttr("cityName"));
				reParams.addAttr("desc", desc);
				reParams.setService("borrowBaseService");
				reParams.setMethod("update");
				sucResult = SoaManager.getInstance().invoke(reParams);
				if(sucResult.isSuccess()){
					//查询分配池中是否存在记录
					AppParam alltoParam = new AppParam("netStorePoolService","queryCount");
					alltoParam.addAttr("applyId", applyId);
					AppResult quertAllotResult = SoaManager.getInstance().invoke(alltoParam);
					if (quertAllotResult.getRows().size() > 0) {
						serIdSb.append(id).append(",");
						errCount++;
						continue;
					}

					//增加分配池信息
					AppParam alltoParam2 = new AppParam("netStorePoolService","insert");
					alltoParam2.addAttr("applyId", applyId);
					alltoParam2.addAttr("cityName", params.getAttr("cityName"));
					alltoParam2.addAttr("orderType", 2);
					alltoParam2.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
					alltoParam2.addAttr("applyTime", new Date());
					alltoParam2.addAttr("orgId", params.getAttr("orgId"));
					alltoParam2.addAttr("lastStore", params.getAttr("customerId"));
					alltoParam2.addAttr("nextRecordDate", DateUtil.getSimpleFmt(new Date()));
					AppResult allotResult =  SoaManager.getInstance().invoke(alltoParam2);
					if (allotResult.isSuccess()) {
						// 插入操作记录
						AppResult sucRes = StoreOptUtil.insertStoreRecord(applyId,params.getAttr("customerId"),
								StoreConstant.STORE_OPER_31,"手动导入门店历史数据", 0,2,0,1);
						if(sucRes.isSuccess()){
							sucCount ++ ;
						}
					}
				}
			}
		}
		sucResult.putAttr("errCount", errCount);
		sucResult.putAttr("sucCount", sucCount);
		sucResult.setMessage(serIdSb.toString());
		return sucResult;
	}

	/**
	 * 离职处理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult leaveDealWith(AppParam params) {
		AppResult result = new AppResult();
		//离职人的用户ID
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		//当前登录人ID
		String custId = StringUtil.objectToStr(params.getAttr("custId"));
		String lastStoreIds = StringUtil.objectToStr(params.getAttr("lastStoreIds"));
		String orgId = StringUtil.objectToStr(params.getAttr("orgId"));
		String[] lastStoreArr = lastStoreIds.split(",");
		AppParam queryParams  = new AppParam("borrowStoreApplyService","query");
		queryParams.addAttr("lastStore", customerId);
		queryParams.addAttr("inOrderStatus", "-1,0,1,2,7,8");
		AppResult queryResult = SoaManager.getInstance().invoke(queryParams);
		List<String> listApplyIds = new ArrayList<String>();
		if(queryResult.getRows().size() > 0){
			for(Map<String, Object> orderMap : queryResult.getRows()){
				listApplyIds.add(StringUtil.getString(orderMap.get("applyId")));
			}
			//订单数量
			int orderSize = listApplyIds.size();
			//转信贷经理人数
			int custSize = lastStoreArr.length;
			AppParam updateOrder = new AppParam("borrowStoreApplyService","update");
			updateOrder.addAttr("status", 2);
			updateOrder.addAttr("orderType", 2); //2是再分配
			updateOrder.addAttr("orgId", orgId);
			updateOrder.addAttr("allotBy", custId);
			updateOrder.addAttr("allotDesc", "离职转信贷经理");
			if(custSize == 1){
				for (int j = 0; j < orderSize; j++) {
					String applyId = listApplyIds.get(j);
					String lastSore = lastStoreArr[0];
					updateOrder.addAttr("applyId", applyId);
					updateOrder.addAttr("lastStore", lastSore);
					updateOrder.addAttr("allotTime", new Date());
					result = SoaManager.getInstance().invoke(updateOrder);

					// 插入操作记录
					StoreOptUtil.insertStoreRecord(applyId,lastSore, StoreConstant.STORE_OPER_0,
							"离职转信贷经理[CUSTID=]" + custId, 0,2,0,1);

					//同步处理
					if(result.isSuccess()){
						Map<String,Object> dealMap = new HashMap<String, Object>();
						//同步orderStatus
						dealMap.put("applyId", applyId);
						dealMap.put("lastStore", lastSore);
						StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
					}
				}
			}else{
				if(orderSize >= custSize){
					for (int j = 0; j < orderSize; j++) {
						String applyId = listApplyIds.get(j);
						String lastSore = "";
						if(j >= custSize){
							Random random = new Random();// 定义随机类
							int index = random.nextInt(custSize);
							lastSore = lastStoreArr[index];
						}else{
							lastSore = lastStoreArr[j];
						}
						updateOrder.addAttr("applyId", applyId);
						updateOrder.addAttr("lastStore", lastSore);
						updateOrder.addAttr("allotTime", new Date());
						result = SoaManager.getInstance().invoke(updateOrder);
						// 插入操作记录
						StoreOptUtil.insertStoreRecord(applyId,lastSore, StoreConstant.STORE_OPER_0,
								"离职转信贷经理[CUSTID=]" + custId, 0,2,0,1);
						//同步处理
						if(result.isSuccess()){
							Map<String,Object> dealMap = new HashMap<String, Object>();
							dealMap.put("applyId", applyId);
							dealMap.put("lastStore", lastSore);
							StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
						}
					}

				}else{
					for (int j = 0; j < orderSize; j++) {
						String applyId = listApplyIds.get(j);
						String lastSore = lastStoreArr[j];
						updateOrder.addAttr("applyId", applyId);
						updateOrder.addAttr("lastStore", lastSore);
						updateOrder.addAttr("allotTime", new Date());
						result = SoaManager.getInstance().invoke(updateOrder);
						// 插入操作记录
						StoreOptUtil.insertStoreRecord(applyId,lastSore, StoreConstant.STORE_OPER_0,
								"离职转信贷经理[CUSTID=]" + custId, 0,2,0,1);

						//同步处理
						if(result.isSuccess()){
							Map<String,Object> dealMap = new HashMap<String, Object>();
							//同步orderStatus
							dealMap.put("applyId", applyId);
							dealMap.put("lastStore", lastSore);
							StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
						}
					}
				}
			}
		}
		return result;
	}

	/***
	 * 查询无效客户列表
	 * @param params
	 * @return
	 */
	public AppResult queryInvalidCustList(AppParam params){
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params,NAMESPACE,"queryInvalidCustList","queryInvalidCustCount");
	}

	/***
	 * 查询无效订单信息
	 * @param params
	 * @return
	 */
	public AppResult queryInvalidOrderInfo(AppParam params){
		return super.query(params,NAMESPACE,"queryInvalidOrderInfo");
	}

	/***
	 * 订单找回
	 * @param params
	 * @return
	 */
	public AppResult againFollowOrder(AppParam params){
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		AppParam queryParams  = new AppParam("borrowApplyService","query");
		queryParams.addAttr("applyId", applyId);
		AppResult queryResult = SoaManager.getInstance().invoke(queryParams);
		Map<String, Object> applyInfo = null;
		if(queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0))){
			applyInfo = queryResult.getRow(0);
		}
		//判断是否是当前处理人
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		if(!customerId.equals(lastStore) && !StoreOptUtil.isDealAuth(customerId)){
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能找回订单！");
		}
		//查询是否存在网销申请表
		queryParams.setService("borrowStoreApplyService");
		AppResult applyResult = SoaManager.getInstance().invoke(queryParams);
		if(applyResult.getRows().size() == 0){
			AppParam insertParam = new AppParam("borrowStoreApplyService","insertByBorrowApply");
			insertParam.addAttr("applyId", applyId);
			result = SoaManager.getInstance().invoke(insertParam);
			if(result.isSuccess() && NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE),0) > 0){
				AppParam deleteParam = new AppParam("invalidStorePoolService","delete");
				deleteParam.addAttr("applyId", applyId);
				SoaManager.getInstance().invoke(deleteParam);
				//更新订单处理人
				AppParam updateApplyParam = new AppParam("borrowStoreApplyService", "update");
				updateApplyParam.addAttr("applyId", applyId);
				updateApplyParam.addAttr("lastStore", lastStore);
				SoaManager.getInstance().invoke(updateApplyParam);
			}
		}
		return result;
	}
	
	/***
	 * 退单处理
	 * @param params
	 * @return
	 */
	public AppResult backOrderDeal(AppParam params){
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String backReDesc = StringUtil.getString(params.getAttr("backReDesc"));
		AppParam applyParams  = new AppParam("borrowStoreApplyService","query");
		applyParams.addAttr("applyId", applyId);
		AppResult queryResult = SoaManager.getInstance().invoke(applyParams);
		Map<String, Object> applyInfo = null;
		if(queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0))){
			applyInfo = queryResult.getRow(0);
		}else{
			return CustomerUtil.retErrorMsg("暂无此订单信息");
		}
		
		// 非无效单不允许退单
		String orderStatus =  StringUtil.getString(applyInfo.get("orderStatus"));
		if (!StoreConstant.STORE_ORDER_7.equals(orderStatus)
				&& !StoreConstant.STORE_ORDER_8.equals(orderStatus)) {
			result.setSuccess(false);
			result.setMessage("非无效订单不允许退单！");
			return result;
		}
		
		//判断是否是当前处理人
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		if(!customerId.equals(lastStore) && !StoreOptUtil.isDealAuth(customerId)){
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行退单处理！");
		}
		//更新退单状态
		applyParams.setMethod("update");
		applyParams.addAttr("backStatus", StoreConstant.STORE_BACK_STATUS_2);
		applyParams.addAttr("backReDesc", backReDesc);
		applyParams.addAttr("backDesc", "");
		AppResult applyResult = SoaManager.getInstance().invoke(applyParams);
		int updateSize = NumberUtil.getInt(applyResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
		if(updateSize > 0){
			String orderType = StringUtil.getString(applyInfo.get("orderType"));
			// 插入操作记录
			StoreOptUtil.insertStoreRecord(applyId,lastStore, StoreConstant.STORE_OPER_33,
					"退单原因" + backReDesc, 0,orderType,0,1);
		}
		return result;
	}
	
	
	/***
	 * 批量审核退单
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult batchCheckBackOrder(AppParam params){
		AppResult result = new AppResult();
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		String backStatus = StringUtil.objectToStr(params.getAttr("backStatus"));
		String backDesc = StringUtil.objectToStr(params.getAttr("backDesc"));
		String showBackStatus = backStatus; // 最终变更的状态
		
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params.getAttr("orders");
		AppParam updateParams  = new AppParam("borrowStoreApplyService","update");
		Map<String,Object> dealMap = new HashMap<String, Object>();
		for (Map<String, Object> orderMap : orders) {
			String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
			Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
			// 退单成功的订单不能再次审核
			if(StoreConstant.STORE_BACK_STATUS_3.equals(
					StringUtil.getString(applyInfo.get("backStatus")))){
				result.setSuccess(false);
				result.setMessage("退单成功的订单无需再次审核");
				continue;
			}
			
			// 先修改审核成功的单子成本状态为失效，成功则修改退单状态为退单成功
			if(StoreConstant.STORE_BACK_STATUS_3.equals(backStatus)){
				String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
				AppParam recordParams = new AppParam("orgCostRecordService","query");
				recordParams.addAttr("customerId", lastStore);
				recordParams.addAttr("applyId", applyId);
				recordParams.setOrderBy("createTime");
				recordParams.setOrderValue("desc");
				AppResult recorResult = SoaManager.getInstance().callNoTx(recordParams);
				
				if(recorResult.getRows().size() > 0){
					AppParam updateCostParams = new AppParam("orgCostRecordService","updateCostByApplyId");
					updateCostParams.addAttr("orgId", recorResult.getRow(0).get("orgId"));
					updateCostParams.addAttr("applyId", applyId);
					updateCostParams.addAttr("status", "0");
					
					AppResult updateResult = SoaManager.getInstance().invoke(updateCostParams);
					int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
					if(updateSize > 0){
						//更新门店人员成本统计
						String recordDate = StringUtil.getString(recorResult.getRow(0).get("createTime"));
						recordDate = DateUtil.getSimpleFmt(DateUtil.toDateByString(recordDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
						dealMap.put("customerId", lastStore);
						dealMap.put("recordDate", recordDate);
						StoreOptUtil.dealStoreOrderByMq(lastStore,"storeCostType", dealMap);
						
						showBackStatus = StoreConstant.STORE_BACK_STATUS_3;
						backDesc = "";
					}else{
						showBackStatus = StoreConstant.STORE_BACK_STATUS_4;
						backDesc = "非第一处理人申请或该订单未计算成本";
					}
				}else{
					showBackStatus = StoreConstant.STORE_BACK_STATUS_4;
					backDesc = "非第一处理人申请或该订单未计算成本";
				}
			}
			// 插入操作记录
			StoreOptUtil.insertStoreRecord(applyId,customerId, StoreConstant.STORE_OPER_33,
								StringUtils.isEmpty(backDesc) ? "退单成功":"退单失败原因"+ backDesc, 0, applyInfo.get("orderType"),0,1);
			
			// 退单审核失败通知处理
			if(!backDesc.isEmpty()){
				//给相关门店人员发送消息
				params.addAttr("backDesc", backDesc);
				params.addAttr("applyId", StringUtil.getString(applyInfo.get("applyId")));
				params.addAttr("applyName", StringUtil.getString(applyInfo.get("applyName")));
				params.addAttr("customerId", StringUtil.getString(applyInfo.get("lastStore")));
				params.addAttr("dealType", 1);
				StoreOptUtil.sendNotify(params);
			}
			
			// 更新门店申请表的退单状态
			updateParams.addAttr("applyId", applyId);
			updateParams.addAttr("backStatus", showBackStatus);
			updateParams.addAttr("backDesc", backDesc);
			result = SoaManager.getInstance().invoke(updateParams);
			int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(updateSize > 0){
				applyInfo = null;
			}
		}
		return result;
	}
	
	/***
	 * 离职处理(离职人名下的已签单或已放款单转给主管或者门店经理)
	 * @param params
	 * @return
	 */
	public AppResult orderTransToManager(AppParam params){
		AppResult result = new AppResult();
		//离职人Id
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		//当前登录人Id
		String custId = StringUtil.objectToStr(params.getAttr("custId"));
		//主管或者门店经理ID
		String leaderCustId = StringUtil.objectToStr(params.getAttr("leaderCustId"));
		AppParam applyParams = new AppParam("borrowStoreApplyService","query");
		applyParams.setDataBase("main_");
		applyParams.addAttr("lastStore", customerId);
		result = SoaManager.getInstance().callNoTx(applyParams);
		applyParams.setDataBase(null);
		applyParams.removeAttr("lastStore");
		if(result.isSuccess() && result.getRows().size() > 0){
			Map<String,Object> dealMap = new HashMap<String, Object>();
			dealMap.put("lastStore", leaderCustId);
			for(Map<String,Object> map : result.getRows()){
				String applyId = StringUtil.getString(map.get("applyId"));
				applyParams.setMethod("update");
				applyParams.addAttr("lastStore", leaderCustId);
				applyParams.addAttr("applyId", applyId);
				AppResult newResult = SoaManager.getInstance().invoke(applyParams);
				int updateSize = NumberUtil.getInt(newResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
				if(updateSize > 0){
					// 插入操作记录
					StoreOptUtil.insertStoreRecord(applyId,leaderCustId, StoreConstant.STORE_OPER_0,
							"离职转信贷经理[CUSTID=]" + custId, 0,2,0,1);
					//同步处理人
					dealMap.put("applyId", applyId);
					StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 订单类型处理
	 * 
	 * @param params
	 */
	public AppResult updateDealOrderType(AppParam params) {
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String dealOrderType = StringUtil.getString(params.getAttr("dealOrderType"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//管理员可以执行订单类型处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
				|| !StringUtils.isEmpty(adminCustomerId)){
			AppParam applyParams = new AppParam("borrowStoreApplyService","update");
			applyParams.addAttr("applyId", applyId);
			applyParams.addAttr("dealOrderType", dealOrderType);
			applyParams.addAttr("orderTypeUpdateTime", new Date());
			result = SoaManager.getInstance().invoke(applyParams);
			int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
			if(result.isSuccess() && updateSize == 1) {
				// 根据lastStore 查询处理人信息
				AppParam queryParam = new AppParam("busiCustService","query");
				queryParam.addAttr("customerId", lastStore);
				result = SoaManager.getInstance().invoke(queryParam);
				if(result.isSuccess() && result.getRows().size()>0){
					Map<String, Object> custMap = result.getRow(0);
					String orgId = StringUtil.getString(custMap.get("orgId"));
					String groupName = StringUtil.getString(custMap.get("groupName"));
					if(!StringUtils.isEmpty(orgId) && !StringUtils.isEmpty(groupName)){
						Map<String, Object> dealMap = new HashMap<String, Object>();
						Date applyDate = DateUtil.toDateByString(applyInfo.get("applyTime").toString(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
						String recordDate = DateUtil.toStringByParttern(applyDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
						dealMap.put("recordDate",recordDate);
						dealMap.put("orgId", orgId);
						dealMap.put("groupName", groupName);
						dealMap.put("channelCode", applyInfo.get("channelCode"));
						StoreOptUtil.dealStoreOrderByMq(customerId,"dealOrderType", dealMap);
					}
				}
				
				// 删除订单缓存
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
			}
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行订单类型处理");
		}
	}
	
	/***
	 * 删除签单以及回款信息
	 * @param params
	 * @return
	 */
	public AppResult deleteSignInfo(AppParam params){
		AppResult result = new AppResult();
		//申请ID
		String applyId = StringUtil.objectToStr(params.getAttr("applyId"));
		//合同编号
		String treatyNo = StringUtil.objectToStr(params.getAttr("treatyNo"));
		//CFS的合同编号
		String reContractId = StringUtil.objectToStr(params.getAttr("reContractId"));
		//查询签单
		AppParam queryhistoryParams = new AppParam("treatInfoHistoryService","queryShow");
		queryhistoryParams.addAttr("treatyNo", treatyNo);
		queryhistoryParams.addAttr("applyId", applyId);
		AppResult signResult = SoaManager.getInstance().invoke(queryhistoryParams);
		//查询回款
		AppParam queryRepayParams = new AppParam("treatSuccessService","query");
		queryRepayParams.addAttr("reContractId", reContractId);
		queryRepayParams.addAttr("applyId", applyId);
		AppResult repayResult = SoaManager.getInstance().invoke(queryRepayParams);
		//删除签单历史表
		AppParam historyParams = new AppParam("treatInfoHistoryService","deleteBy");
		historyParams.addAttr("treatyNo", treatyNo);
		historyParams.addAttr("applyId", applyId);
		result = SoaManager.getInstance().invoke(historyParams);
		int deleteSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Delete_SIZE), 0);
		if(deleteSize > 0){
			//删除签单表
			AppParam signParams = new AppParam("treatInfoService","delete");
			signParams.addAttr("applyId", applyId);
			SoaManager.getInstance().invoke(signParams);
			//删除回款历史表
			AppParam repayHistoryParams = new AppParam("treatSuccessHistoryService","deleteBack");
			repayHistoryParams.addAttr("applyId", applyId);
			repayHistoryParams.addAttr("reContractId", reContractId);
			SoaManager.getInstance().invoke(repayHistoryParams);
			//删除回款表
			AppParam repayParams = new AppParam("treatSuccessService","deleteBack");
			repayParams.addAttr("applyId", applyId);
			repayParams.addAttr("reContractId", reContractId);
			SoaManager.getInstance().invoke(repayParams);
			//删除放款记录表
			AppParam loanParams = new AppParam("treatLoanRecordService","deleteBy");
			loanParams.addAttr("applyId", applyId);
			loanParams.addAttr("reContractId", reContractId);
			SoaManager.getInstance().invoke(loanParams);
			//删除CFS贷款合同表
			AppParam contractParams = new AppParam("treatContractService","deleteBy");
			contractParams.addAttr("applyId", applyId);
			contractParams.addAttr("reContractId", reContractId);
			SoaManager.getInstance().invoke(contractParams);
			if(signResult.getRows().size() > 0){
				String customerId = StringUtil.getString(signResult.getRow(0).get("customerId"));
				String recordDate = DateUtil.getSimpleFmt(DateUtil.toDateByString(
						StringUtil.getString(signResult.getRow(0).get("signTime")), DateUtil.DATE_PATTERN_YYYY_MM_DD));
				if(!StringUtils.isEmpty(customerId) && !StringUtils.isEmpty(recordDate)){
					Map<String, Object> sendParam = new HashMap<String, Object>();
					sendParam.put("recordDate", recordDate);
					//同步签单
					StoreOptUtil.dealStoreOrderByMq(customerId,"signDealType", sendParam);
					StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
				}
			}
			if(repayResult.getRows().size() > 0){
				String customerId = StringUtil.getString(repayResult.getRow(0).get("customerId"));
				String recordDate = StringUtil.getString(repayResult.getRow(0).get("feeAmountDate"));
				if(!StringUtils.isEmpty(customerId) && !StringUtils.isEmpty(recordDate)){
					//同步回款数据
					Map<String, Object> sendParam = new HashMap<String, Object>();
					sendParam.put("recordDate",recordDate);//记录日期
					StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
					StoreOptUtil.dealStoreOrderByMq(customerId,"backDealType", sendParam);
				}
			}
			Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
			// 插入操作记录
			StoreOptUtil.insertStoreRecord(applyId,params.getAttr("customerId"), StoreConstant.STORE_OPER_35,
					"删除签单及回款信息", 0,StringUtil.getString(applyInfo.get("orderType")),0,1);
		}
		return result;
	}
	
	/***
	 * 删除回款信息
	 * @param params
	 * @return
	 */
	public AppResult deleteBackAmout(AppParam params){
		AppResult result = new AppResult();
		//申请ID
		String applyId = StringUtil.objectToStr(params.getAttr("applyId"));
		//回款编号
		String recordId = StringUtil.objectToStr(params.getAttr("recordId"));
		//查询回款
		AppParam queryRepayParams = new AppParam("treatSuccessService","query");
		queryRepayParams.addAttr("recordId", recordId);
		queryRepayParams.addAttr("applyId", applyId);
		AppResult repayResult = SoaManager.getInstance().invoke(queryRepayParams);
		//删除回款表
		AppParam repayParams = new AppParam("treatSuccessService","delete");
		repayParams.addAttr("recordId", recordId);
		repayParams.addAttr("applyId", applyId);
		result = SoaManager.getInstance().invoke(repayParams);
		int deleteSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Delete_SIZE), 0);
		if(deleteSize > 0){
			//删除回款历史表
			AppParam repayHistoryParams = new AppParam("treatSuccessHistoryService","deleteByRepayId");
			repayHistoryParams.addAttr("applyId", applyId);
			repayHistoryParams.addAttr("repayId", recordId);
			SoaManager.getInstance().invoke(repayHistoryParams);
			if(repayResult.getRows().size() > 0){
				String customerId = StringUtil.getString(repayResult.getRow(0).get("customerId"));
				String recordDate = StringUtil.getString(repayResult.getRow(0).get("feeAmountDate"));
				if(!StringUtils.isEmpty(customerId) && !StringUtils.isEmpty(recordDate)){
					//同步回款数据
					Map<String, Object> sendParam = new HashMap<String, Object>();
					sendParam.put("recordDate",recordDate);//记录日期
					StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
					StoreOptUtil.dealStoreOrderByMq(customerId,"backDealType", sendParam);
				}
			}
			Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
			// 插入操作记录
			StoreOptUtil.insertStoreRecord(applyId,params.getAttr("customerId"), StoreConstant.STORE_OPER_36,
					"删除回款信息", 0,StringUtil.getString(applyInfo.get("orderType")),0,1);
		}
		return result;
	}

}
