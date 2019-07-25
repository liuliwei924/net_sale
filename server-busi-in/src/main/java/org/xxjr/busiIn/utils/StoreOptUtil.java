package org.xxjr.busiIn.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.store.StoreAppSend;
import org.ddq.active.mq.store.StorePcSend;
import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.web.util.IdentifyUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreOptUtil {


	/**
	 * 签单处理
	 * @param params
	 * @return
	 */
	public static AppResult signDeal(AppParam params) {
		AppResult result = new AppResult();
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		String applyId = StringUtil.objectToStr(params.getAttr("applyId"));
		String status = StringUtil.getString(params.getAttr("status"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		//管理员用户Id
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		//当前处理人
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//角色类型
		String authType = StringUtil.getString(params.getAttr("authType"));
		//批量关联
		String flag = StringUtil.getString(params.getAttr("flag"));
		//管理员可以执行签单
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
				|| !StringUtils.isEmpty(adminCustomerId)){
			//提交按揭中
			if(StringUtils.isEmpty(status)){
				status = StoreConstant.STORE_SIGN_1;
			}
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			if (userMap != null && !StringUtils.isEmpty(userMap)) {
				params.addAttr("orgId", userMap.get("orgId"));
			}
			AppParam queryParam = new AppParam("treatInfoService", "query");
			queryParam.addAttr("applyId", applyId);
			result = SoaManager.getInstance().invoke(queryParam);
			int size = result.getRows().size();
			AppParam updateParams = new AppParam("treatInfoService", "insert");
			updateParams.addAttrs(params.getAttr());
			AppParam historyParams = new AppParam("treatInfoHistoryService", "insert");
			historyParams.addAttrs(params.getAttr());
			String signTime = StringUtil.getString(params.getAttr("signTime"));
			//申请时间
			String applyTime = StringUtil.getString(applyInfo.get("applyTime"));
			//渠道代号
			String channelCode = StringUtil.getString(applyInfo.get("channelCode"));
			//来自详细渠道
			String channelDetail = StringUtil.getString(applyInfo.get("channelDetail"));
			if (size > 0) {
				Map<String, Object> queryMap = result.getRow(0);
				String statusTmp = StringUtil.getString(queryMap.get("status"));
				String oldSignTime = StringUtil.getString(queryMap.get("signTime"));
				//当签单状态是全部结案的，新的状态为提交按揭中时，则新增一笔合同到主表和签单历史表中
				if(StoreConstant.STORE_SIGN_2.equals(statusTmp) && StoreConstant.STORE_SIGN_1.equals(status)){
					AppParam deleteParams = new AppParam("treatInfoService", "delete");
					deleteParams.addAttr("applyId", applyId);
					SoaManager.getInstance().callNewTx(deleteParams);
					//新增主表
					String orgNo = StringUtil.getString(userMap.get("orgNo"));
					String date = DateUtil.toStringByParttern(new Date(), "yyyyMMdd");
					String treatyNo = orgNo + date + IdentifyUtil.getRandNum(5);// 合同编号
					updateParams.addAttr("treatyNo", treatyNo);// 合同编号
					updateParams.addAttr("upStatus", 1);
					//新增历史表
					if(StringUtils.isEmpty(signTime)){
						updateParams.addAttr("signTime", new Date());
						historyParams.addAttr("signTime", new Date());
					}else{
						if(oldSignTime.equals(signTime)){
							updateParams.addAttr("signTime", new Date());
							historyParams.addAttr("signTime", new Date());
						}
					}
					historyParams.addAttr("treatyNo", treatyNo);
					historyParams.addAttr("upStatus", 1);//上传状态
					historyParams.addAttr("applyTime", applyTime);
					historyParams.addAttr("channelCode", channelCode);
					historyParams.addAttr("channelDetail", channelDetail);
				}else{
					//管理员可更改签单时间
					if(CustConstant.CUST_ROLETYPE_1.equals(authType)){
						if(StringUtils.isEmpty(updateParams.getAttr("signTime")) && !StringUtils.isEmpty(oldSignTime)){
							updateParams.addAttr("signTime", oldSignTime);
							historyParams.addAttr("signTime", oldSignTime);
						}else{
							updateParams.addAttr("createTime", updateParams.getAttr("signTime"));
							historyParams.addAttr("createTime", updateParams.getAttr("signTime"));
						}
					}else{
						if(StringUtils.isEmpty(flag) && !StringUtils.isEmpty(queryMap.get("signTime")) && 
								!queryMap.get("signTime").equals(updateParams.getAttr("signTime"))){
							result.setSuccess(false);
							result.setMessage("签单时间不能修改");
							return result;
						}
					}
					updateParams.setMethod("updateSignInfo");
					historyParams.addAttr("treatyNo", queryMap.get("treatyNo"));
					historyParams.setMethod("update");
				}
			}else {
				String orgNo = StringUtil.getString(userMap.get("orgNo"));
				String date = DateUtil.toStringByParttern(new Date(), "yyyyMMdd");
				String treatyNo = orgNo + date + IdentifyUtil.getRandNum(5);// 合同编号
				String upStatus = StringUtil.getString(params.getAttr("upStatus"));
				updateParams.addAttr("treatyNo", treatyNo);
				historyParams.addAttr("treatyNo", treatyNo);
				if(StringUtils.isEmpty(signTime)){
					updateParams.addAttr("signTime", new Date());
					historyParams.addAttr("signTime", updateParams.getAttr("signTime"));
				}
				if(StringUtils.isEmpty(upStatus)){
					updateParams.addAttr("upStatus", 1);//上传状态
					historyParams.addAttr("upStatus", 1);//上传状态
				}
				historyParams.addAttr("applyTime", applyTime);
				historyParams.addAttr("channelCode", channelCode);
				historyParams.addAttr("channelDetail", channelDetail);
			}
			//加入签单的描述
			StringBuffer strBuf = new StringBuffer();
			String treatyName = StringUtil.getString(params.getAttr("treatyName"));
			String feeRate = StringUtil.getString(params.getAttr("feeRate"));
			String feeDesc = "比例:";
			if(StringUtils.isEmpty(feeRate)){
				feeRate = StringUtil.getString(params.getAttr("feeAmount"));
				feeDesc = "点数:";
			}
			strBuf.append("合同项目:").append(treatyName).append(" ")
			.append(feeDesc).append(feeRate).append(" ")
			.append("状态:").append(getStatusName(status));
			String handleDesc = strBuf.toString();
			updateParams.addAttr("handleDesc", handleDesc);
			result = SoaManager.getInstance().invoke(updateParams);
			if(result.isSuccess()){
				historyParams.addAttr("handleDesc", handleDesc);
				SoaManager.getInstance().invoke(historyParams);
				String handleType = StoreConstant.STORE_OPER_3;
				// 插入操作记录
				insertStoreRecord(applyId,StringUtils.isEmpty(adminCustomerId) ? customerId: adminCustomerId,
						handleType,handleDesc, 0, applyInfo.get("orderType"),1,0);
				//签单后处理
				AppParam dealParam = new AppParam();
				dealParam.addAttr("newTreatyNo", result.getAttr("treatyNo"));
				dealParam.addAttr("customerId", customerId);
				dealParam.addAttr("signTime", signTime);
				dealParam.addAttr("flag", flag);
				dealParam.addAttr("applyId", applyId);
				dealParam.addAttr("custLabel", applyInfo.get("custLabel"));
				dealParam.addAttr("orderType", applyInfo.get("orderType"));
				dealParam.addAttr("status", status);
				dealParam.addAttr("orgId", params.getAttr("orgId"));
				signOrderDeal(dealParam);
			}
		}else{
			result.setSuccess(false);
			if(StringUtils.isEmpty(lastStore)){
				result.setMessage("订单没有当前处理人，暂时不能进行签单处理！");
			}else{
				result.setMessage("你不是当前处理人，不能进行签单处理！");
			}
		}
		return result;
	}

	/**
	 * 签单后处理操作
	 * 
	 * @param applyId
	 * @return
	 */
	public static void signOrderDeal(AppParam param) {
		// 合同编号
		String newTreatyNo = StringUtil.getString(param.getAttr("newTreatyNo"));
		// 用户编号
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		// 签单时间
		String signTime = StringUtil.getString(param.getAttr("signTime"));

		// 申请编号
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		//客户等级
		String custLabel = StringUtil.getString(param.getAttr("custLabel"));
		//订单类型
		String orderType = StringUtil.getString(param.getAttr("orderType"));
		//记录日期
		String recordDate = "";
		if(!StringUtils.isEmpty(newTreatyNo) && !StringUtils.isEmpty(signTime)){
			recordDate = DateUtil.getSimpleFmt(DateUtil.toDateByString(signTime, DateUtil.DATE_PATTERN_YYYY_MM_DD));
		}else{
			recordDate = DateUtil.getSimpleFmt(new Date());
		}
		Map<String, Object> sendParam = new HashMap<String, Object>();
		sendParam.put("recordDate", recordDate);
		//同步签单
		StoreOptUtil.dealStoreOrderByMq(customerId,"signDealType", sendParam);
		if(!StringUtils.isEmpty(newTreatyNo)){
			//同步基本信息
			StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType" , sendParam);
			//签单后自动更改处理状态为已上门签约
			String orderStatus = StoreConstant.STORE_ORDER_3;
			AppParam updateParam = new AppParam("borrowStoreApplyService","update");
			updateParam.addAttr("applyId", applyId);
			//判断当前星级是否是3星、4星
			if(!StoreConstant.STORE_CUST_LABEL_4.equals(custLabel) &&
					!StoreConstant.STORE_CUST_LABEL_5.equals(custLabel)){
				updateParam.addAttr("custLabel", StoreConstant.STORE_CUST_LABEL_4);
			}
			updateParam.addAttr("orderStatus", orderStatus);
			AppResult queryResult = SoaManager.getInstance().invoke(updateParam);
			int updateSize = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(updateSize > 0){
				insertStoreRecord(applyId,customerId,
						StoreConstant.STORE_OPER_23,"系统自动改成已上门签约状态", 0, orderType,1,1);
			}
			//同步orderStatus
			Map<String, Object> dealMap = new HashMap<String, Object>();
			dealMap.put("applyId", applyId);
			dealMap.put("orderStatus", orderStatus);
			StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
			if(!StringUtils.isEmpty(updateParam.getAttr("custLabel"))){
				//同步星级
				dealMap.put("custLabel", StoreConstant.STORE_CUST_LABEL_4);
				StoreOptUtil.dealStoreOrderByMq(customerId,"custLabelType", dealMap);
			}
		}else{
			// 如果是修改为结案，则同步mq统计失败原因
			String status = StringUtil.getString(param.getAttr("status"));
			if(StoreConstant.STORE_SIGN_2.equals(status)){
				AppParam queryParam = new AppParam("borrowStoreApplyService","query");
				queryParam.addAttr("applyId", applyId);
				AppResult result = SoaManager.getInstance().invoke(queryParam);
				if(result.isSuccess() && result.getRows().size()>0){
					Map<String, Object> applyMap = result.getRow(0);
					String channelCode = StringUtil.getString(applyMap.get("channelCode"));
					String applyTime = StringUtil.getString(applyMap.get("applyTime"));
					if(!StringUtils.isEmpty(channelCode) && !StringUtils.isEmpty(applyTime)){
						Date applyDate = DateUtil.toDateByString(applyTime, DateUtil.DATE_PATTERN_YYYY_MM_DD);
						recordDate = DateUtil.toStringByParttern(applyDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
						//同步签单失败统计
						Map<String, Object> dealMap = new HashMap<String, Object>();
						dealMap.put("recordDate", recordDate);
						dealMap.put("channelCode", channelCode);
						StoreOptUtil.dealStoreOrderByMq(null,"signFailType", dealMap);
					}
				}		
			}
		}
	}

	/**
	 * 查询贷款信息
	 * 
	 * @param applyId
	 * @return
	 */
	public static Map<String, Object> queryByApplyId(Object applyId) {
		AppParam queryParam = new AppParam();
		queryParam.addAttr("applyId", applyId);
		queryParam.setService("borrowStoreApplyService");
		queryParam.setMethod("query");
		AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
		if (queryResult.getRows().size() == 0) {
			throw new SysException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		return queryResult.getRow(0);
	}

	/**
	 * 插入或者更新门店人员处理记录
	 */
	public static AppResult storeHandleRecord(Object applyId, Object customerId,Object handleDesc) {
		if(!StringUtils.isEmpty(customerId)){
			
			AppParam recordParams = new AppParam("storeHandleRecordService",
					"update");
			recordParams.addAttr("applyId", applyId);
			recordParams.addAttr("customerId", customerId);
			recordParams.addAttr("lastTime", new Date());
			recordParams.addAttr("handleDesc", handleDesc);
			return SoaManager.getInstance().invoke(recordParams);
		}
        
		return new AppResult();
	}
	/**
	 * 插入门店人员操作记录
	 * @param applyId 申请Id
	 * @param storeBy 处理人
	 * @param handleType 处理类型
	 * @param handleDesc 处理描述
	 * @param robWay 抢单类型
	 * @param orderType 单子类型
	 * @param isFeedback 是否为反馈
	 * @param readFlag 是否已读
	 * @return
	 */
	public static AppResult insertStoreRecord(Object applyId, Object storeBy,
			String handleType, Object handleDesc, Object robWay, 
			Object orderType,int isFeedback,int readFlag) {

		String orgId = ""; 
		// 通过customerId获取orgId
		AppParam queryParams = new AppParam("busiCustService","query");
		queryParams.addAttr("customerId", storeBy);
		AppResult custResult = SoaManager.getInstance().invoke(queryParams);
		if(custResult.isSuccess() && custResult.getRows().size() > 0){
			orgId = StringUtil.getString(custResult.getRow(0).get("orgId"));
		}
		AppParam recordParams = new AppParam("borrowStoreRecordService",
				"insert");
		recordParams.addAttr("applyId", applyId);
		recordParams.addAttr("storeBy", storeBy);
		recordParams.addAttr("orgId", orgId);
		recordParams.addAttr("handleType", handleType);
		recordParams.addAttr("robWay", robWay);
		recordParams.addAttr("isFeedback", isFeedback);
		recordParams.addAttr("readFlag", "1");//默认已读
		recordParams.addAttr("orderType", orderType);
		recordParams.addAttr("handleDesc", handleDesc);
		AppResult result = SoaManager.getInstance().invoke(recordParams);
		if(result.isSuccess()){
			//不加入最近处理描述
			if(StoreConstant.STORE_OPER_23.equals(handleType) 
					&& !StringUtils.isEmpty(handleDesc)){
				handleDesc = null;
			}else if(StoreConstant.STORE_OPER_33.equals(handleType) 
					&& !StringUtils.isEmpty(handleDesc)){
				handleDesc = null;
			}
			storeHandleRecord(applyId,storeBy,handleDesc);
			updateStoreApplyLastTime(applyId,storeBy);	
		}
		return result;
	}

	/**
	 * 删除预约、签单、回款信息
	 */
	public static AppResult deleteInfo(AppParam param) {
		AppResult result = new AppResult();
		AppParam bookParams = new AppParam("treatBookService",
				"delete");
		bookParams.addAttr("applyId", param.getAttr("applyId"));
		bookParams.addAttr("customerId", param.getAttr("customerId"));
		result =  SoaManager.getInstance().invoke(bookParams);
		if(result.isSuccess()){
			AppParam signParams = new AppParam("treatInfoService",
					"deleteSign");
			signParams.addAttr("applyId", param.getAttr("applyId"));
			signParams.addAttr("customerId", param.getAttr("customerId"));
			SoaManager.getInstance().invoke(signParams);

			AppParam backParams = new AppParam("treatSuccessService",
					"deleteBack");
			backParams.addAttr("applyId", param.getAttr("applyId"));
			backParams.addAttr("customerId", param.getAttr("customerId"));
			SoaManager.getInstance().invoke(backParams);
		}
		return result;
	}

	public static boolean IntSaleCityJudge(String cityName,double loanAmount,int houseType){
		String tsAllotCitys = SysParamsUtil.getStringParamByKey("tsAllotCitys", "深圳市,上海市,广州市");
		int minAllotLoanAmount = SysParamsUtil.getIntParamByKey("minAllotLoanAmount", 4);

		if(tsAllotCitys.indexOf(cityName) >= 0){
			if(houseType !=0 && houseType != 2){
				return true;
			}

			if(loanAmount > minAllotLoanAmount){
				return true;
			}
		}
		return false;
	}

	public static String getIntAutoCitys(){
		String tsAllotCitys = SysParamsUtil.getStringParamByKey("tsAllotCitys", "深圳市,上海市,广州市");
		Map<String,Object> baseCfg = StoreSeparateUtils.getBaseConfig();
		String allotCitys = StringUtil.getString(baseCfg.get("allotCitys"));
		StringBuilder sb = new StringBuilder();
		if(StringUtils.hasText(allotCitys)){
			String[] allotCitysArr = allotCitys.split(",");
			for(int i=0;i<allotCitysArr.length; i++){
				String cityTmp = allotCitysArr[i];
				if(tsAllotCitys.indexOf(cityTmp) < 0){
					sb.append(cityTmp).append(",");
				}
			}
		}

		String str = sb.toString();
		if(StringUtils.hasText(str)){
			return str.substring(0, str.length()-1);
		}else{
			return "not city";

		}
	}

	/**
	 * 用户是否有操作权限
	 * @param custoemrId
	 * @return
	 */
	public static boolean isDealAuth(String customerId) {
		// 获取用户信息
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId);
		if (custInfo != null) {
			String authType = StringUtil.getString(custInfo.get("roleType"));
			if (CustConstant.CUST_ROLETYPE_1.equals(authType)
					|| CustConstant.CUST_ROLETYPE_6.equals(authType)
					|| CustConstant.CUST_ROLETYPE_7.equals(authType)
					|| CustConstant.CUST_ROLETYPE_8.equals(authType)
					|| CustConstant.CUST_ROLETYPE_9.equals(authType)) {
				return true;
			}
		}
		return false;
	}

	private static String getStatusName(String status){
		switch(Integer.parseInt(status)){
		case 1 : return "提交按揭中";
		case 2 : return "结案";
//		case 3 : return "已结束";
//		case 4 : return "无贷款";
		case 5 : return "贷款未提交";
//		case 6 : return "部分结案";
		default: return "";
		}
	}

	/**
	 * 查询用户等级
	 */
	public static AppResult queryCustLevel(String customerId) {
		AppParam recordParams = new AppParam("custLevelService","query");
		recordParams.addAttr("customerId", customerId);
		return SoaManager.getInstance().invoke(recordParams);
	}

	/**
	 * 查询签单历史数据,判断是否可删除数据
	 */
	public static AppResult querySignHostory(AppParam param) {
		AppResult result = new AppResult();
		AppParam recordParams = new AppParam("treatInfoHistoryService","query");
		recordParams.setOrderValue("desc");
		recordParams.setOrderBy("createTime");
		recordParams.addAttrs(param.getAttr());
		AppResult  queryResult = SoaManager.getInstance().invoke(recordParams);
		if(queryResult.getRows().size() > 0 ){
			if(queryResult.getRows().size() == 1){
				return result;
			}else{
				String reContractId = StringUtil.getString(queryResult.getRow(0).get("reContractId"));
				result.putAttr("reContractId", reContractId);
			}
		}
		return result;
	}


	/**
	 * 更新门店申请表最后一次处理时间
	 */
	public static AppResult updateStoreApplyLastTime(Object applyId, Object customerId) {
		AppParam params = new AppParam("borrowStoreApplyService",
				"update");
		params.addAttr("applyId", applyId);
		params.addAttr("lastUpdateTime", new Date());
		return SoaManager.getInstance().invoke(params);
	}


	/**
	 * 回收订单消息提醒
	 */
	public static void sendRecyclingMeaasge(AppParam param){
		try{
			StringBuffer buffer = new StringBuffer();
			buffer.append("尊敬的");
			buffer.append(param.getAttr("realName"));
			buffer.append("，您有一笔");
			buffer.append(param.getAttr("desc"));
			buffer.append("，订单的客户姓名是：");
			buffer.append(param.getAttr("applyName"));
			buffer.append("，回收时间：");
			buffer.append(DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
			buffer.append("。");
			StoreAppSend  storeAppSend = SpringAppContext.getBean(StoreAppSend.class);//发送APP消息通知
			Map<String, Object> sendParam = new HashMap<String, Object>();
			String customerId = StringUtil.getString(param.getAttr("customerId"));
			sendParam.put("customerId", customerId);
			sendParam.put("message", buffer.toString());
			sendParam.put("notifyType", "1");
			sendParam.put("cmdName", "0006"); // 发送个人消息
			sendParam.put("success", "true");
			String uuid = StringUtil.getString(RedisUtils.getRedisService().get("app" +customerId));
			if(!StringUtils.isEmpty(uuid)){
				storeAppSend.sendAppMessage(uuid,"storeCmdType", sendParam);
			}
			StorePcSend  storePcSend = SpringAppContext.getBean(StorePcSend.class);	//发送PC消息通知
			Map<String, Object> sendPCParam = new HashMap<String, Object>();	
			String sessionId = StringUtil.getString(RedisUtils.getRedisService().get("pc" + customerId));
			sendPCParam.put("message", buffer.toString());
			sendPCParam.put("customerId", customerId);
			sendPCParam.put("notifyType", "1");
			sendPCParam.put("success", "true");
			sendPCParam.put("cmdName","0006"); // 个人消息
			if(!StringUtils.isEmpty(sessionId)){
				storePcSend.sendPcMessage(sessionId, "storeCmdType", sendPCParam);
			}

			//加入mq保存回收消息通知
			StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
			Map<String, Object> msgParam = new HashMap<String, Object>();
			String orgId = StringUtil.getString(param.getAttr("orgId"));
			msgParam.put("notifyText", buffer.toString());
			msgParam.put("notifyDate", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			msgParam.put("customerId", customerId);
			msgParam.put("orgId", orgId);
			msgParam.put("messNotifyType", "2"); //2 个人通知消息
			storeSend.sendStoreMessage(customerId,"orderNotifyType" , msgParam);
		}catch(Exception e){
			log.error("StoreOptUtil 发送回收消息通知 error", e);
		}
	}

	/**
	 * 分单消息提醒
	 */
	public static void sendAllotMeaasge(AppParam param){
		//发送分单消息通知
		try{
			
			String customerId = StringUtil.getString(param.getAttr("customerId"));
			String applyName = StringUtil.getString(param.getAttr("applyName"));
			int orderType = NumberUtil.getInt(param.getAttr("orderType"),1);
		
			StringBuffer buffer = new StringBuffer();
			if( 1 == orderType){
				buffer.append("您刚刚分配了一笔新申请订单，订单的客户姓名是：");
			}else{
				buffer.append("您刚刚分配了一笔再分配订单，订单的客户姓名是：");
			}
			buffer.append(applyName);
			buffer.append("，分单时间：");
			buffer.append(DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
			buffer.append("，请及时处理！");
			
			/*
			StoreAppSend  storeAppSend = SpringAppContext.getBean(StoreAppSend.class);//发送app消息通知
			StorePcSend  storePcSend = SpringAppContext.getBean(StorePcSend.class);	//发送PC消息通知
			Map<String, Object> sendParam = new HashMap<String, Object>();	
			
			sendParam.put("customerId", customerId);
			sendParam.put("message", buffer.toString());
			sendParam.put("notifyType", "1");
			sendParam.put("cmdName", "0006"); // 个人消息
			sendParam.put("success", "true");
			String uuid = StringUtil.getString(RedisUtils.getRedisService().get("app" +customerId));
			if(!StringUtils.isEmpty(uuid)){
				storeAppSend.sendAppMessage(uuid,"storeCmdType", sendParam);
			}
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			Map<String, Object> sendPCParam = new HashMap<String, Object>();	
			String sessionId = StringUtil.getString(RedisUtils.getRedisService().get("pc" + customerId));
			sendPCParam.put("message", buffer.toString());
			sendPCParam.put("applyId", applyId);
			sendPCParam.put("customerId", customerId);
			sendPCParam.put("notifyType", "1");
			sendPCParam.put("success", "true");
			sendPCParam.put("cmdName","0006"); // 个人消息
			if(!StringUtils.isEmpty(sessionId)){
				storePcSend.sendPcMessage(sessionId, "storeCmdType", sendPCParam);
			}
			*/
			
			//加入mq保存分单消息通知
			StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
			Map<String, Object> msgParam = new HashMap<String, Object>();
			String orgId = StringUtil.getString(param.getAttr("orgId"));
			msgParam.put("notifyText", buffer.toString());
			msgParam.put("notifyDate", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			msgParam.put("customerId", customerId);
			msgParam.put("orgId", orgId);
			msgParam.put("custTelephone", param.getAttr("custTelephone"));
			msgParam.put("messNotifyType", "2"); //2 个人通知消息
			storeSend.sendStoreMessage(customerId,"orderNotifyType" , msgParam);
			
		}catch(Exception e){
			log.error("StoreOptUtil 发送分单消息通知 error", e);
		}
	}

	/***
	 * 同步处理t_borrow_store_apply与t_borrow_apply的数据
	 * @param customerId 用户id
	 * @param dealType 处理类型
	 * @param msgParam 处理参数
	 */
	public static void dealStoreOrderByMq(String customerId,String dealType,Map<String,Object> msgParam){
		try{
			//创建任务对象
			StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
			storeSend.sendStoreMessage(customerId, dealType , msgParam);
		}catch(Exception e){
			log.error("StoreOptUtil 同步mq error", e);
		}
	}

	/***
	 * 判断是否加入最近处理描述
	 * @param handleTpye 处理参数
	 */
	public static boolean isAddHandleDesc(String handleTpye){
		boolean flag = true;
		if(StoreConstant.STORE_OPER_23.equals(handleTpye)
				|| StoreConstant.STORE_OPER_25.equals(handleTpye)){
			flag = false;
		}
		return flag;
	}

	/**
	 * 判断字符串是否都是汉字
	 * @param applyName
	 * @return
	 */
	public static String isChinese(String applyName){
		if(!StringUtils.isEmpty(applyName)){
			String regex = "^[\u4e00-\u9fa5]+$";
			if(applyName.matches(regex)){
				return applyName;
			}
		}
		return "未知";
	}


	/**
	 * PC页面及小小云APP消息提醒
	 */
	public static void sendNotify(AppParam param){
		//发送分单消息通知
		try{
			StoreAppSend  storeAppSend = SpringAppContext.getBean(StoreAppSend.class);//发送app消息通知
			StorePcSend  storePcSend = SpringAppContext.getBean(StorePcSend.class);	//发送PC消息通知
			String applyName = StringUtil.getString(param.getAttr("applyName"));
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			String dealType = StringUtil.getString(param.getAttr("dealType"));
			String backDesc = StringUtil.getString(param.getAttr("backDesc"));
			String customerId;

			StringBuffer buffer = new StringBuffer();
			if("1".equals(dealType)){
				customerId = StringUtil.getString(param.getAttr("customerId"));
				buffer.append("您有一笔订单退单审核失败订单姓名是:");
				buffer.append(applyName);
				buffer.append("  审核失败原因是:");
				buffer.append(backDesc);
			}else{
				customerId = StringUtil.getString(param.getAttr("recCustId"));
				buffer.append("您有一笔专属订单超过七天未处理,订单姓名是:");
				buffer.append(applyName);
				buffer.append("，请及时处理！");
			}

			// 发送至APP端
			Map<String, Object> sendAppParam = new HashMap<String, Object>();	
			sendAppParam.put("customerId", customerId);
			sendAppParam.put("message", buffer.toString());
			sendAppParam.put("notifyType", "1");
			sendAppParam.put("cmdName", "0006"); // 个人消息
			sendAppParam.put("success", "true");
			String uuid = StringUtil.getString(RedisUtils.getRedisService().get("app" +customerId));
			if(!StringUtils.isEmpty(uuid)){
				storeAppSend.sendAppMessage(uuid,"storeCmdType", sendAppParam);
			}

			// 发送至PC端
			Map<String, Object> sendPcParam = new HashMap<String, Object>();	
			String sessionId = StringUtil.getString(RedisUtils.getRedisService().get("pc" + customerId));
			sendPcParam.put("message", buffer.toString());
			sendPcParam.put("applyId", applyId);
			sendPcParam.put("customerId", customerId);
			sendPcParam.put("notifyType", "1");
			sendPcParam.put("cmdName","0006"); // 个人消息
			sendPcParam.put("success", "true");
			if(!StringUtils.isEmpty(sessionId)){
				storePcSend.sendPcMessage(sessionId, "storeCmdType", sendPcParam);
			}

			//加入mq保存分单消息通知
			StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
			Map<String, Object> msgParam = new HashMap<String, Object>();
			String orgId = StringUtil.getString(param.getAttr("orgId"));
			msgParam.put("notifyText", buffer.toString());
			msgParam.put("notifyDate", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			msgParam.put("customerId", customerId);
			msgParam.put("orgId", orgId);
			msgParam.put("messNotifyType", "2"); //2 个人通知消息
			storeSend.sendStoreMessage(customerId,"orderNotifyType" , msgParam);
		}catch(Exception e){
			log.error("StoreOptUtil 发送分单消息通知 error", e);

		}
	}
	

	/***
	 * 获取表名
	 * @param curDate
	 * @return
	 */
	public static String getTableName(String curDate){
		String tableName = "t_borrow_store_record";
		if(StringUtils.isEmpty(curDate)){
			curDate = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYYMM);
		}else{
			if(curDate.length() == 7){
				Date date = DateUtil.toDateByString(curDate, DateUtil.DATE_PATTERN_YYYY_MM);
				curDate = DateUtil.toStringByParttern(date, DateUtil.DATE_PATTERN_YYYYMM);
			}else{
				Date date = DateUtil.toDateByString(curDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
				curDate = DateUtil.toStringByParttern(date, DateUtil.DATE_PATTERN_YYYYMM);
			}
		}
		return tableName + curDate;
	}
	
	/**
	 * 发送暂停分单提醒
	 * @param customerId
	 * @return
	 */
	public static void sendPauseAllotNotify(String customerId,int allotNewOrderCount,int allotNewCount){
		try{
			// 暂停分单提醒单差值
			int pauseAllotNotifyCount = SysParamsUtil.getIntParamByKey("pauseAllotNotifyCount",30);
			int realPauseAllotNotify = allotNewCount - pauseAllotNotifyCount;
			// 是否有回款标识
			boolean isHaveRetFlag = StoreSeparateUtils.isExitRetAmout(customerId);
			if(allotNewOrderCount >= realPauseAllotNotify && !isHaveRetFlag){
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
				StringBuffer buffer = new StringBuffer();
				buffer.append("您新申请订单已满");
				buffer.append(realPauseAllotNotify).append("单，如满");
				buffer.append(allotNewCount).append("单且期间无回款将停止分配新申请单！");
				StoreAppSend  storeAppSend = SpringAppContext.getBean(StoreAppSend.class);//发送APP消息通知
				Map<String, Object> sendParam = new HashMap<String, Object>();
				sendParam.put("customerId", customerId);
				sendParam.put("message", buffer.toString());
				sendParam.put("notifyType", "1");
				sendParam.put("cmdName", "0006"); // 发送个人消息
				sendParam.put("success", "true");
				String uuid = StringUtil.getString(RedisUtils.getRedisService().get("app" +customerId));
				if(!StringUtils.isEmpty(uuid)){
					storeAppSend.sendAppMessage(uuid,"storeCmdType", sendParam);
				}
				StorePcSend  storePcSend = SpringAppContext.getBean(StorePcSend.class);	//发送PC消息通知
				Map<String, Object> sendPCParam = new HashMap<String, Object>();	
				String sessionId = StringUtil.getString(RedisUtils.getRedisService().get("pc" + customerId));
				sendPCParam.put("message", buffer.toString());
				sendPCParam.put("customerId", customerId);
				sendPCParam.put("notifyType", "1");
				sendPCParam.put("success", "true");
				sendPCParam.put("cmdName","0006"); // 个人消息
				if(!StringUtils.isEmpty(sessionId)){
					storePcSend.sendPcMessage(sessionId, "storeCmdType", sendPCParam);
				}

				//加入mq保存回收消息通知
				StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
				Map<String, Object> msgParam = new HashMap<String, Object>();
				String orgId = StringUtil.getString(custInfo.get("orgId"));
				msgParam.put("notifyText", buffer.toString());
				msgParam.put("notifyDate", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
				msgParam.put("customerId", customerId);
				msgParam.put("orgId", orgId);
				msgParam.put("messNotifyType", "2"); //2 个人通知消息
				storeSend.sendStoreMessage(customerId,"orderNotifyType" , msgParam);
			}
		}catch(Exception e){
			log.error("StoreOptUtil 发送暂停分单提醒通知 error", e);
		}
	}
}
