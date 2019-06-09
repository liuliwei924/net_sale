package org.xxjr.mq.listener.receiver;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.ddq.active.mq.XxjrMqSendUtil;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.mq.listener.consumer.RabbitMqConsumer;
import org.xxjr.mq.listener.util.XxjrInitAnnotation;
import org.xxjr.sms.SendSmsByUrl;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SmsConfigUtil;

@Component
@XxjrInitAnnotation(beanName="storeTaskReceiver",initMethod="init")
public class StoreTaskReceiver extends RabbitMqConsumer{
	
	private static ThreadPoolTaskExecutor taskExecutor;

	public static ThreadPoolTaskExecutor getInstance(){
		if(taskExecutor == null){
			synchronized (StoreTaskReceiver.class) {
				if(taskExecutor == null){
					taskExecutor = SpringAppContext.getApplicationContext().getBean(ThreadPoolTaskExecutor.class);
				}
			}
		}
		return taskExecutor;
	}
	
	@Autowired
	private RabbitMqConfig rabbitMqConfig;
	
	@Value("${rabbit.queue.storeTask}")
	private String queueName;


	public void onMessage(Map<String, Object> messageInfo) {
		try {
			LogerUtil.log("StoreTaskReceiver params:" + messageInfo.toString());
			synProcessing(messageInfo);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class, e, "StoreTaskReceiver mq execute error!");
			XxjrMqSendUtil.saveFailureLog("StoreTaskReceiver", messageInfo);
		}

	}
	 /**
	    * 异步处理
	    * @throws IOException 
	    */
	   public static void synProcessing(Map<String, Object> messageInfo) {
		   getInstance().execute(new Runnable() {
				public void run() {
					try{
						String messageType = StringUtil.getString(messageInfo.get("messageType"));
						LogerUtil.log("messageType:" + messageType);
						if("countDealType".equals(messageType)){//业务员基本信息处理
							updateCustOrder(messageInfo);
						}else if("backDealType".equals(messageType)){//回款数据处理
							updateBackLoanSum(messageInfo);
						}else if("signDealType".equals(messageType)){//签单数据处理
							updateSignSum(messageInfo);
						}else if("custLabelType".equals(messageType)){//星级数据处理
							updateCustLabel(messageInfo);
						}else if("handelOrderType".equals(messageType)){//订单状态处理
							updateOrderStatus(messageInfo);
						}else if("orderNotifyType".equals(messageType)){//订单消息通知处理
							saveOrderNotify(messageInfo);
						}else if("handelRecordType".equals(messageType)){//跟进记录变更
							handelRecordChange(messageInfo);
						}else if("storeCostType".equals(messageType)){//更新门店人员成本统计
							updateStoreCost(messageInfo);
						}else if("borrowRiskType".equals(messageType)){//增加风控查询记录
							addBorrowRiskRecord(messageInfo);
						}else if("leavelDealType".equals(messageType)){//离职处理
							leavelDealWith(messageInfo);
						}else if("dealOrderType".equals(messageType)){//订单类型统计处理
							updateDealOrderType(messageInfo);
						}else if("orderRateType".equals(messageType)){//订单评分统计处理
							updateOrderRate(messageInfo);
						}else if("signFailType".equals(messageType)){//签单失败统计处理
							updateSignFail(messageInfo);
						}
					}catch(Exception e){
						LogerUtil.error(StoreTaskReceiver.class, e, "synProcessing error");
					}
				}
		 });
	 }
	   
	/***
	 * 签单失败原因按申请日期重新统计
	 * @param messageInfo
	 */
	private static void updateSignFail(Map<String, Object> messageInfo) {
		try {
			@SuppressWarnings("unchecked")
			Map<String,Object> dealMap = (Map<String,Object>)messageInfo.get("dealParams");
			String recordDate = StringUtil.getString(dealMap.get("recordDate"));
			String channelCode = StringUtil.getString(dealMap.get("channelCode"));
			
			AppParam queryParam = new AppParam("storeApplyExtService","querySignFailToday");
			queryParam.addAttr("recordDate", recordDate);
			queryParam.addAttr("channelCode", channelCode);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult result = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumSignFailChannelService","save");
				insertParam.addAttr("recordDate", recordDate);
				insertParam.addAttr("channelCode", channelCode);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver countSignFailChannel >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver countupdateOrderRate error");
		}
	}

	@SuppressWarnings("unchecked")
	private static void updateOrderRate(Map<String, Object> messageInfo) {
		try {
			Map<String,Object> dealMap = (Map<String,Object>)messageInfo.get("dealParams");
			String recordDate = StringUtil.getString(dealMap.get("recordDate"));
			String orgId = StringUtil.getString(dealMap.get("orgId"));
			LogerUtil.log("storeTaskReceive updateOrderRate>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("storeApplyExtService","queryRateToday");
			queryParam.addAttr("recordDate", recordDate);
			queryParam.addAttr("orgId", orgId);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			AppResult result = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumOrderRateService","save");
				insertParam.addAttr("list", dataList);
				insertParam.addAttr("recordDate", recordDate);
				insertParam.addAttr("orgId",orgId);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver countupdateOrderRate >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver countupdateOrderRate error");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static void updateDealOrderType(Map<String, Object> messageInfo) {
		Map<String,Object> dealMap = null;
		try {
			dealMap = (Map<String,Object>)messageInfo.get("dealParams");
			String recordDate = StringUtil.getString(dealMap.get("recordDate"));
			String groupName = StringUtil.getString(dealMap.get("groupName"));
			String orgId = StringUtil.getString(dealMap.get("orgId"));
		
			LogerUtil.log("storeTaskReceive updateDealOrderType>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","queryDealOrderTypeToday");
			queryParam.addAttr("recordDate", recordDate);
			queryParam.addAttr("groupName", groupName);
			queryParam.addAttr("orgId", orgId);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			AppResult result = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumDealOrderTypeService","save");
				insertParam.addAttr("list", dataList);
				insertParam.addAttr("recordDate", recordDate);
				insertParam.addAttr("groupName", groupName);
				insertParam.addAttr("orgId", orgId);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver countDealOrderType >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);	
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver countDealOrderType error");
		}
		
		// 按渠道统计订单状态
		try {
			String channelCode = StringUtil.getString(dealMap.get("channelCode"));
			String recordDate = StringUtil.getString(dealMap.get("recordDate"));
			AppParam queryOrderChannel = new AppParam("storeApplyExtService","queryOrderChannelToday");
			queryOrderChannel.addAttr("recordDate", recordDate);
			queryOrderChannel.addAttr("channelCode", channelCode);
			queryOrderChannel.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult result = RemoteInvoke.getInstance().callNoTx(queryOrderChannel);
			
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				AppParam insertParam = new AppParam("sumChannelDealordertypeService","save");
				insertParam.addAttr("recordDate", recordDate);
				insertParam.addAttr("channelCode", channelCode);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver countChannelDealOrderType >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver countChannelDealOrderType error");
		}
		
	}

	@Override
	public void init(String queueName ,RabbitMqConfig rabbitMqConfig) {
		super.init(queueName,rabbitMqConfig);
	}
	
    public void init() {
		init(queueName,rabbitMqConfig);
	}
	
	@PreDestroy
	public void destroy(){
		reaseResource();
	}
	
	
	/**
	 * 处理业务员基本参数或停止分单
	 * @param messageInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult updateCustOrder(Map<String,Object> messageMap){
		String customerId = StringUtil.getString(messageMap.get("customerId"));
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String recordDate = StringUtil.getString(dealMap.get("recordDate"));
		AppParam queryParam = new AppParam();
		queryParam.addAttr("customerId", customerId);
		queryParam.setService("custLevelService");
		queryParam.setMethod("query");
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int gradeCode = 1;
		int isAllotOrder = 0;
		String oldDesc = "";
		if(queryResult.getRows().size() > 0 && !queryResult.getRow(0).isEmpty()){
			gradeCode = NumberUtil.getInt(queryResult.getRow(0).get("gradeCode"),1);
			isAllotOrder = NumberUtil.getInt(queryResult.getRow(0).get("isAllotOrder"),0);//是否可分单
			oldDesc = StringUtil.getString(queryResult.getRow(0).get("allotDesc")); //关闭分单描述
		}
		Map<String,Object> storeMap = StoreSeparateUtils.getRankConfigByGrade(gradeCode);
		// 总单数天数
		int totalDays = 0;
		//已上门天数
		int visitDays = 0;
		//成功签单天数
		int sucDays = 0;
		//回款天数
		int feeDays = 0;
		//回款天数
		int backDays = 0;
		Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
		int orgId = NumberUtil.getInt(userMap.get("orgId"),0);
		if(storeMap !=null){
			String maxCount = StringUtil.getString(storeMap.get("maxCount"));
			String visitCount = StringUtil.getString(storeMap.get("visitCount"));
			String sucCount = StringUtil.getString(storeMap.get("sucCount"));
			String backAmount = StringUtil.getString(storeMap.get("backAmount"));
			String backCount = StringUtil.getString(storeMap.get("backCount"));
			if(maxCount.contains("/")){
				totalDays = NumberUtil.getInt(StringUtils.substringAfter(maxCount,"/"),1);
			}else{
				totalDays = 1;
			}
			
			if(visitCount.contains("/")){
				visitDays = NumberUtil.getInt(StringUtils.substringAfter(visitCount,"/"),1);
			}else{
				visitDays = 1;
			}
			int visit = queryOrgNOTWorkCount(orgId,recordDate,visitDays);
			visitDays += visit;
			
			if(sucCount.contains("/")){
			    sucDays = NumberUtil.getInt(StringUtils.substringAfter(sucCount,"/"),1);
			}else{
				sucDays = 1;
			}
			int suc = queryOrgNOTWorkCount(orgId,recordDate,sucDays);
		    sucDays += suc;
		    
			if(backAmount.contains("/")){
				feeDays = NumberUtil.getInt(StringUtils.substringAfter(backAmount,"/"),1);
			}else{
				feeDays = 1;
			}
			int fee = queryOrgNOTWorkCount(orgId,recordDate,feeDays);
			feeDays += fee;
			
			if(backCount.contains("/")){
				backDays = NumberUtil.getInt(StringUtils.substringAfter(backCount,"/"),1);
			}else{
				backDays = 1;
			}
			int back = queryOrgNOTWorkCount(orgId,recordDate,backDays);
			backDays += back;
		}
		
		AppParam queryOrderParam = new AppParam();
		queryOrderParam.addAttr("customerId", customerId);
		queryOrderParam.addAttr("recordDate", recordDate);
		queryOrderParam.addAttr("totalDays", totalDays);
		queryOrderParam.addAttr("visitDays", visitDays);
		queryOrderParam.addAttr("sucDays", sucDays);
		queryOrderParam.addAttr("feeDays", feeDays);
		queryOrderParam.addAttr("backDays", backDays);
		queryOrderParam.setService("storeHandleExtService");
		queryOrderParam.setMethod("queryCustOrderInfo");
		queryOrderParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult orderResult = RemoteInvoke.getInstance().callNoTx(queryOrderParam);
		int ordTotalCount = 0; //分单总数
		int dealOrderCount = 0; //处理中笔数（尚未签单数）
		int visitCount = 0; //上门单量（已经上门单的数量）
		int signCount = 0; //签单数量（已经签单的笔数）
		AppParam updateParam = new AppParam();
		AppResult result = new AppResult();
		updateParam.addAttr("customerId", customerId);
		if(orderResult.getRows().size() > 0 && !orderResult.getRow(0).isEmpty()){
			Map<String,Object> orderMap = orderResult.getRow(0);
			ordTotalCount = NumberUtil.getInt(orderMap.get("ordTotalCount"),0);
			dealOrderCount = NumberUtil.getInt(orderMap.get("dealOrderCount"),0);
			visitCount = NumberUtil.getInt(orderMap.get("visitCount"),0);
			signCount = NumberUtil.getInt(orderMap.get("signCount"),0);
			updateParam.addAttrs(orderMap);
			updateParam.setService("custLevelService");
			updateParam.setMethod("update");
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParam);
			
			Map<String,Object> curMap = new HashMap<String,Object>();
			curMap.put("ordTotalCount", ordTotalCount);
			curMap.put("dealOrderCount", dealOrderCount);
			curMap.put("visitCount", visitCount);
			curMap.put("signCount", signCount);
			curMap.put("isAllotOrder", isAllotOrder);
			curMap.put("oldDesc", oldDesc);
			// 查询门店人员分配新单总数
			int allotNewOrderCount = StoreSeparateUtils.queryAllotNewOrderCount(customerId);
			curMap.put("allotNewOrderCount", allotNewOrderCount);
			
			AppParam storeApplyParam = new AppParam("borrowStoreApplyService","queryCount");
			storeApplyParam.addAttr("lastStore", customerId);
			storeApplyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult storeApplyResult = RemoteInvoke.getInstance().call(storeApplyParam);
			int storeCount = NumberUtil.getInt(StringUtil.getString(storeApplyResult.getAttr(DuoduoConstant.TOTAL_SIZE)));
			//所有订单数超过20笔才校验分单基本信息（即保证新人可以正常分单）
			if(storeCount > 20){
				compareParam(curMap,storeMap,customerId);
			}
		}
		
		return result;
	}
	
	/**
	 * 比较各项订单数是否达到要求
	 * @param custMap
	 * @param gradeMap
	 * @param customerId
	 */
	public static void compareParam(Map<String,Object> custMap, Map<String,Object> gradeMap,String customerId){
		String maxCount = StringUtil.getString(gradeMap.get("maxCount"));
		String visitCount = StringUtil.getString(gradeMap.get("visitCount"));
		String sucCount = StringUtil.getString(gradeMap.get("sucCount"));
		// 等级要求最大分单数
		int maxCountNum = 0;
		if(maxCount.contains("/")){
			String[] maxCountArr = maxCount.split("/");
			maxCountNum = NumberUtil.getInt(maxCountArr[0],0);
		}else{
			maxCountNum =  NumberUtil.getInt(maxCount,0);
		}
		// 业务员当前总分单数
		int curTotalCountNum = NumberUtil.getInt(custMap.get("ordTotalCount"),0);
		
		// 等级要求最大处理中单数
		int maxDealNum = NumberUtil.getInt(gradeMap.get("dealCount"),0);
		// 业务员当前处理中单数
		int curDealCountNum = NumberUtil.getInt(custMap.get("dealOrderCount"),0);
		
		//等级要求上门数
		int visitNum = 0;
		if(visitCount.contains("/")){
			String[] visitCountArr = visitCount.split("/");
			visitNum = NumberUtil.getInt(visitCountArr[0],0);
		}else{
			visitNum =  NumberUtil.getInt(visitCount,0);
		}
		//业务员当前上门数
		int curVisitNum = NumberUtil.getInt(custMap.get("visitCount"),0);
		
		//等级要求签单数
		int sucCountNum = 0;
		if(sucCount.contains("/")){
			String[] sucCountArr = sucCount.split("/");
			sucCountNum = NumberUtil.getInt(sucCountArr[0],0);
		}else{
			sucCountNum =  NumberUtil.getInt(sucCount,0);
		}
		// 业务员当前签单数
		int curSignNum = NumberUtil.getInt(custMap.get("signCount"),0);
		
		String allotDesc ="";
		
		if(StringUtils.isEmpty(allotDesc) && visitNum > curVisitNum){
			allotDesc ="您当前上门数未达到要求";
		}
		
		if(StringUtils.isEmpty(allotDesc) && sucCountNum > curSignNum){
			allotDesc ="您当前签单数未达到要求";
		}
		
		if(StringUtils.isEmpty(allotDesc) && curDealCountNum > maxDealNum){
			allotDesc ="您当前处理中单数过多";
		}
		
		if(StringUtils.isEmpty(allotDesc) && curTotalCountNum >= maxCountNum){
			allotDesc ="您当前分单总数达到最大";
		}
		
		// 业务员当前新单分单总数
		int allotNewOrderCount = NumberUtil.getInt(custMap.get("allotNewOrderCount"),0);
		// 配置分配新总单数
		int allotNewCount = NumberUtil.getInt(gradeMap.get("allotNewCount"),150);
		boolean isHaveRetFlag = StoreSeparateUtils.isExitRetAmout(customerId);
		if(StringUtils.isEmpty(allotDesc) && allotNewOrderCount >= allotNewCount 
				&& !isHaveRetFlag){
			allotDesc ="您当前回款数未达到要求";
		}
		//成本单量达到150及以上且有回款则从新计算
		if(allotNewOrderCount >= allotNewCount && isHaveRetFlag){
			String startComputeDate = DateUtil.getSimpleFmt(new Date());
			RedisUtils.getRedisService().set(StoreSeparateUtils.STORE_START_ALLOT_RECORDDATE_KEY + customerId,
					(Serializable) startComputeDate);
			//更新开始计算成本单及回款日期
			AppParam custParam = new AppParam("custLevelService","update");
			custParam.addAttr("customerId", customerId);
			custParam.addAttr("stopAllotDate", startComputeDate);
			custParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(custParam);
		}
		
		AppParam updateParam = new AppParam("custLevelService","update");
		updateParam.addAttr("customerId", customerId);
		int isAllotOrder = NumberUtil.getInt(custMap.get("isAllotOrder")); 
		String oldDesc = StringUtil.getString(custMap.get("oldDesc"));
		// 1 是可分单,只有是可分单的时候才改成暂停分单
		if(!StringUtils.isEmpty(allotDesc) && 1 == isAllotOrder){
			updateParam.addAttr("allotDesc", allotDesc);
			updateParam.addAttr("isAllotOrder", 2);
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult result = RemoteInvoke.getInstance().call(updateParam);
			int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(updateSize > 0 && "您当前回款数未达到要求".equals(allotDesc)){
				addStorePauseAllotInfo(customerId);
			}
		}else{
			// 2 是暂停分单
			if(2 == isAllotOrder && StringUtils.isEmpty(allotDesc)){
				updateParam.addAttr("isAllotOrder", 1);
				updateParam.addAttr("allotDesc", "");
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult result = RemoteInvoke.getInstance().call(updateParam);
				int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
				if(updateSize > 0){
					//删除门店人员暂停分单信息
					deletePauseAllotInfo(customerId);
				}
			}else if(1 == isAllotOrder && !StringUtils.isEmpty(oldDesc) && StringUtils.isEmpty(allotDesc)){
				updateParam.addAttr("allotDesc", "");
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(updateParam);
			}else if(2 == isAllotOrder && !StringUtils.isEmpty(allotDesc)){// 暂停分单修改描述
				updateParam.addAttr("allotDesc", allotDesc);
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(updateParam);
				if(!"您当前回款数未达到要求".equals(allotDesc)){
					deletePauseAllotInfo(customerId);
				}
			}else if(4 == isAllotOrder){//4是准分单
				deletePauseAllotInfo(customerId);
			}
		}
	}
	
	/**
	 * 更新回款统计
	 * @param 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void updateBackLoanSum(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String recordDate = StringUtil.getString(dealMap.get("recordDate"));
		String toMonth = DateUtil.toStringByParttern(DateUtil.toDateByString(recordDate, DateUtil.DATE_PATTERN_YYYY_MM_DD),"yyyy-MM");
		retByBase(recordDate);
		retByTeam(recordDate);
		storeRet(recordDate);
		storeRetMonth(toMonth);
	} 
	
	/**
	 * 更新签单统计
	 * @param 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void updateSignSum(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String recordDate = StringUtil.getString(dealMap.get("recordDate"));
		String toMonth = DateUtil.toStringByParttern(DateUtil.toDateByString(recordDate, DateUtil.DATE_PATTERN_YYYY_MM_DD),"yyyy-MM");
		storeSign(recordDate);
		sumTotalSign(recordDate);
		sumTeamSign(recordDate);
		storeSignMonth(toMonth);
	} 
	
	/**
	 * 回款基本统计
	 * @param today
	 */
	public static void retByBase(String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl RetByBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","retByBase");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				Map<String,Object> paramsMap = dataList.get(0);
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumRetBaseService","save");
				insertParam.addAttrs(paramsMap);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().callNoTx(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver RetByBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver RetByBase error");
		}
	}
	
	
	public static void retByTeam(String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl retByTeam>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumTeamExtService","retByBase");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumTotalTeamService","batchSave");
				insertParam.addAttr("dataList", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver RetByTeam >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver retByTeam error");
		}
	}
	
	/**
	 * 按门店回款统计
	 * @param today
	 */
	public static void storeRet(String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl storeRet>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","retSumaryByStore");
				queryParam.addAttr("recordDate", today);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumRetStoreService","save");
					insertParam.addAttr("today", today);
					insertParam.addAttr("orgId", orgId);
					insertParam.addAttr("list", dataList);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().callNoTx(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("StoreTaskReceiver storeRet >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver storeRet error");
		}
	}
	
	
	/**
	 * 按门店回款月度统计
	 * @param today
	 */
	public static void storeRetMonth(String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl storeRetMonth>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeRetMonth");
				queryParam.addAttr("toMonth", toMonth);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumRetStoreMonthService","save");
					insertParam.addAttr("toMonth", toMonth);
					insertParam.addAttr("list", dataList);
					insertParam.addAttr("orgId", orgId);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().callNoTx(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("StoreTaskReceiver storeRetMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver storeRetMonth error");
		}
	}
	
	/**
	 * 查询门店不上班时间
	 * @param orgId
	 * @param endDate
	 * @param days
	 * @return
	 */
	public static int queryOrgNOTWorkCount(int orgId,String endDate,int days){
		AppParam queryParam = new AppParam("orgHolidayService", "queryCount");
		Date newEndDate = DateUtil.toDateByString(endDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
		String startDate = DateUtil.getSimpleFmt(DateUtil.getNextDay(newEndDate, -days));
		queryParam.addAttr("startDate", startDate);
		queryParam.addAttr("endDate", endDate);
		queryParam.addAttr("orgId", orgId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qureyResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int	count = NumberUtil.getInt(qureyResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		return count;
	}
	
	/**
	 * 门店经理签单统计
	 * @param processId
	 * @param today
	 */
	public static void storeSign(String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("StoreTaskReceiver storeSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeSign");
				queryParam.addAttr("today", today);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumSignStoreService","save");
					insertParam.addAttr("today", today);
					insertParam.addAttr("list", dataList);
					insertParam.addAttr("orgId", orgId);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("StoreTaskReceiver storeSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver storeSign error");
		}
	}
	
	/**
	 * 总的签单统计(按处理时间) 
	 * @param processId
	 * @param today
	 */
	public static void sumTotalSign(String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("StoreTaskReceiver sumTotalSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","sumTotalSign");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = dataList.size();
			if(size>0){
				Map<String,Object> paramsMap = dataList.get(0);
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumSignBaseService","save");
				insertParam.addAttrs(paramsMap);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver sumTotalSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver sumTotalSign error");
		}
	}
	
	public static void sumTeamSign(String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("StoreTaskReceiver sumTeamSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumTeamExtService","sumTotalSign");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = dataList.size();
			if(size>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumTotalTeamService","batchSave");
				insertParam.addAttr("dataList", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("StoreTaskReceiver sumTeamSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver sumTeamSign error");
		}
	}
	
	
	/**
	 * 门店经理签单月度统计
	 * @param processId
	 * @param today
	 */
	public static void storeSignMonth(String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("StoreTaskReceiver storeSignMonth>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeSignMonth");
				queryParam.addAttr("toMonth", toMonth);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumSignStoreMonthService","save");
					insertParam.addAttr("toMonth", toMonth);
					insertParam.addAttr("list", dataList);
					insertParam.addAttr("orgId", orgId);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("StoreTaskReceiver storeSignMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreTaskReceiver.class,e, "StoreTaskReceiver storeSignMonth error");
		}
	}
	

	/**
	 * 更新客户星级
	 * @param messageInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void updateCustLabel(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String applyId = StringUtil.getString(dealMap.get("applyId"));
		String custLabel = StringUtil.getString(dealMap.get("custLabel"));
		AppParam updateParam = new AppParam();
		updateParam.addAttr("applyId", applyId);
		updateParam.addAttr("custLabel", custLabel);
		updateParam.setService("borrowApplyService");
		updateParam.setMethod("update");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		RemoteInvoke.getInstance().call(updateParam);
	}
	
	
	/**
	 * 更新客户状态
	 * @param messageInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void updateOrderStatus(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String applyId = StringUtil.getString(dealMap.get("applyId"));
		String orderStatus = StringUtil.getString(dealMap.get("orderStatus"));
		String lastStore = StringUtil.getString(dealMap.get("lastStore"));
		String orgId = StringUtil.getString(dealMap.get("orgId"));
//		int isNew = NumberUtil.getInt(dealMap.get("isNew"),0);
		AppParam updateParam = new AppParam();
		updateParam.addAttr("applyId", applyId);
		if(!StringUtils.isEmpty(orderStatus)){
			updateParam.addAttr("orderStatus", orderStatus);
		}
		if(!StringUtils.isEmpty(lastStore)){
			//-1 代表清掉lastStore
			updateParam.addAttr("lastStore", "-1".equals(lastStore) == true ? "" : lastStore);
		}
		if(!StringUtils.isEmpty(orgId)){
			updateParam.addAttr("orgId", orgId);
		}
		updateParam.setService("borrowApplyService");
		updateParam.setMethod("update");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		RemoteInvoke.getInstance().call(updateParam);
		
		// 发送短信
//		if(1 == isNew){
//			sendMessage(applyId,lastStore);
//		}
		
	}
	
	/***
	 * 发送短信通知
	 * @param applyId
	 * @param lastStore
	 */
	@SuppressWarnings("unused")
	private static void sendMessage(String applyId, String lastStore) {
		try{
			// 先查询30天内是否已经发送过消息
			AppParam queryParam = new AppParam("storeMessageService","query");
			queryParam.addAttr("applyId", applyId);
			queryParam.addAttr("startCreateTime", new Date());
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));;
			AppResult result = RemoteInvoke.getInstance().call(queryParam);
			if(result.getRows().size() > 0){
				return;
			}
			
			// 获取用户相关信息
			AppParam queryApplyParam = new AppParam("borrowStoreApplyService","queryMessageInfo");
			queryApplyParam.addAttr("applyId", applyId);
			queryApplyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryApplyParam);
			if(result.isSuccess() && result.getRows().size()>0){
				Map<String,Object> applyMap = result.getRow(0);
				String telephone = StringUtil.getString(applyMap.get("telephone"));
				String applyName = StringUtil.getString(applyMap.get("applyName"));
				String manaName = StringUtil.getString(applyMap.get("manaName"));
				if(!StringUtils.isEmpty(manaName)){
					manaName = manaName.substring(0,1);
				}
				String manaTel = StringUtil.getString(applyMap.get("manaTel"));
				String channelTypeText = StringUtil.getString(applyMap.get("channelTypeText"));
				StringBuffer strBuf = new StringBuffer();
				if("API接口".equals(channelTypeText)){
					strBuf.append("尊敬的").append(applyName).append("，您的专属顾问")
					.append(manaName).append("经理").append("（").append(manaTel).append("）")
					.append("稍后将与您联系，不需要请回复N。");
				}else{
					strBuf.append("尊敬的").append(applyName).append("客户，您已提交了申请，感谢信任！您的专属顾问")
					.append(manaName).append("（经理）").append(manaTel)
					.append("将在第一时间与您联系。祝您办理成功!");
				}
				String smsUrl = SmsConfigUtil.getSMSInfo(strBuf.toString(), telephone);
				SendSmsByUrl.sendSMSInfo(smsUrl, strBuf.toString(), telephone, null);
				
				// 保存记录
				AppParam updateParam = new AppParam("storeMessageService","insert");
				updateParam.addAttr("applyId", applyId);
				updateParam.addAttr("lastStore", lastStore);
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));;
				RemoteInvoke.getInstance().call(updateParam);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreTaskReceiver.class, e, "send Message error");
		}
		
	}

	/**
	 * 保存订单消息通知
	 * @param messageInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void saveOrderNotify(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		AppParam insertParam = new AppParam();
		insertParam.addAttrs(dealMap);
		insertParam.setService("sysNotifyService");
		insertParam.setMethod("insert");
		insertParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		RemoteInvoke.getInstance().call(insertParam);
	}
	
	/**
	 * 跟进记录变更
	 * @param messageInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void handelRecordChange(Map<String,Object> messageMap){
		LogerUtil.log("StoreTaskReceiver handelRecordChange>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String applyId = StringUtil.getString(dealMap.get("applyId"));
		String customerId = StringUtil.getString(dealMap.get("customerId"));
		String handDesc = StringUtil.getString(dealMap.get("handDesc"));
		String orgId = StringUtil.getString(dealMap.get("orgId"));
		if(StringUtils.isEmpty(applyId)){
			return;
		}
		AppParam queryParam = new AppParam("borrowStoreRecordService","query");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().call(queryParam);
		if(queryResult.isSuccess() && queryResult.getRows().size() > 0){
			AppParam recordParam = new AppParam("borrowRecordHistoryService","insertByRecord");
			AppParam storeParam = new AppParam("borrowStoreRecordService","delete");
			storeParam.addAttr("curDate", DateUtil.getSimpleFmt(new Date()));
			for(Map<String,Object> map : queryResult.getRows()){
				recordParam.addAttrs(map);
				String curDate = DateUtil.getSimpleFmt(new Date());
				String createTime = StringUtil.getString(map.get("createTime"));
				String handleType = StringUtil.getString(map.get("handleType"));
				if(createTime.compareTo(curDate) >-1 && "0".equals(handleType)){
					continue;
				}
				recordParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult insertResult = RemoteInvoke.getInstance().call(recordParam);
				int insertSize = NumberUtil.getInt(insertResult.getAttr(DuoduoConstant.DAO_Insert_SIZE),0);
				if(insertSize > 0){
					storeParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					storeParam.addAttr("recordId",map.get("recordId"));
					RemoteInvoke.getInstance().call(storeParam);
				}
			}
			
			// 删除前五个月的门店跟进记录
			for(int i= 1;i<=5;i++){
				Date curDate = DateUtil.getNextMonth(new Date(), -i);
				// 插入到历史记录中
				AppParam insertParam = new AppParam("borrowRecordHistoryService","insertBySelect");
				insertParam.addAttr("curDate", DateUtil.getSimpleFmt(curDate));
				insertParam.addAttr("applyId", applyId);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult result = RemoteInvoke.getInstance().call(insertParam);
				if(result.isSuccess()){
					AppParam deleteParam = new AppParam("borrowStoreRecordService","deleteByApplyId");
					deleteParam.addAttr("curDate", DateUtil.getSimpleFmt(curDate));
					deleteParam.addAttr("applyId", applyId);
					deleteParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					RemoteInvoke.getInstance().call(deleteParam);
				}
			}
		}
		if(!StringUtils.isEmpty(handDesc) && !StringUtils.isEmpty(orgId)){
			// 插入操作记录
			insertStoreRecord(applyId,customerId, StoreConstant.STORE_OPER_0,handDesc, 0,1,0,1,orgId);
		}
		LogerUtil.log("StoreTaskReceiver handelRecordChange>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
	}
	
	/**
	 * 插入门店人员操作记录
	 */
	public static AppResult insertStoreRecord(Object applyId, Object storeBy,
			String handleType, Object handleDesc, Object robWay, 
			Object orderType,int isFeedback,int readFlag,String orgId) {
		AppParam recordParams = new AppParam("borrowStoreRecordService","insert");
		recordParams.addAttr("applyId", applyId);
		recordParams.addAttr("storeBy", storeBy);
		recordParams.addAttr("handleType", handleType);
		recordParams.addAttr("robWay", robWay);
		recordParams.addAttr("isFeedback", isFeedback);
		recordParams.addAttr("readFlag", "1");//默认已读
		recordParams.addAttr("orderType", orderType);
		recordParams.addAttr("handleDesc", handleDesc);
		recordParams.addAttr("orgId", orgId);
		recordParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().call(recordParams);
		if(result.isSuccess()){
			AppParam handleParams = new AppParam("storeHandleRecordService","update");
			handleParams.addAttr("applyId", applyId);
			handleParams.addAttr("customerId", storeBy);
			handleParams.addAttr("lastTime", new Date());
			handleParams.addAttr("handleDesc", handleDesc);
			handleParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(handleParams);
			
			AppParam params = new AppParam("borrowStoreApplyService","update");
			params.addAttr("applyId", applyId);
			params.addAttr("lastUpdateTime", new Date());
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(params);
		}
		return result;
	}
	
	/**
	 * 更新门店人员成本
	 * @param 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void updateStoreCost(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String recordDate = StringUtil.getString(dealMap.get("recordDate"));
		String customerId = StringUtil.getString(dealMap.get("customerId"));
		AppParam recordParams = new AppParam("orgCostRecordService","queryCostCount");
		recordParams.addAttr("recordDate", recordDate);
		recordParams.addAttr("endRecordDate", recordDate);
		recordParams.addAttr("customerId", customerId);
		recordParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(recordParams);
		int totalCount = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		AppParam updateParams = new AppParam("sumStoreBaseService","update");
		updateParams.addAttr("recordDate", recordDate);
		updateParams.addAttr("customerId", customerId);
		updateParams.addAttr("costCount", totalCount);
		updateParams.addAttr("backCostFlag", "1");
		updateParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_sum));
		RemoteInvoke.getInstance().call(updateParams);
	}
	
	/**
	 * 增加风控查询记录
	 * @param 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void addBorrowRiskRecord(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String applyId = StringUtil.getString(dealMap.get("applyId"));
		String applyName = StringUtil.getString(dealMap.get("applyName"));
		String telephone = StringUtil.getString(dealMap.get("telephone"));
		String day180appTimes = StringUtil.getString(dealMap.get("day180appTimes"));
		String respcode = StringUtil.getString(dealMap.get("respcode"));
		String respMessage = StringUtil.getString(dealMap.get("respMessage"));
		String jsonText = StringUtil.getString(dealMap.get("jsonText"));
		AppParam riskParams = new AppParam("borrowRiskRecordService","insert");
		riskParams.addAttr("applyId", applyId);
		riskParams.addAttr("applyName", applyName);
		riskParams.addAttr("telephone", telephone);
		riskParams.addAttr("riskType", StoreConstant.BORROW_RISK_TYPE_1);
		riskParams.addAttr("platfType", StoreConstant.PLAT_FORM_TYPE_2);
		riskParams.addAttr("day180appTimes", day180appTimes);
		riskParams.addAttr("respcode", respcode);
		riskParams.addAttr("respMessage", respMessage);
		riskParams.addAttr("jsonText", jsonText);
		riskParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		RemoteInvoke.getInstance().call(riskParams);
	}
	
	/**
	 * 离职处理
	 * @param 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void leavelDealWith(Map<String,Object> messageMap){
		Map<String,Object> dealMap = (Map<String,Object>)messageMap.get("dealParams");
		String lastStoreIds = StringUtil.getString(dealMap.get("lastStoreIds"));
		String custId = StringUtil.getString(dealMap.get("custId"));
		String orgId = StringUtil.getString(dealMap.get("orgId"));
		String customerId = StringUtil.getString(dealMap.get("customerId"));
		String leaderCustId = StringUtil.getString(dealMap.get("leaderCustId"));
		AppParam applyParam = new AppParam("storeHandleExtService","leaveDealWith");
		applyParam.addAttr("lastStoreIds", lastStoreIds);
		applyParam.addAttr("custId", custId);
		applyParam.addAttr("orgId", orgId);
		applyParam.addAttr("customerId", customerId);
		applyParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().call(applyParam);
		if(result.isSuccess()){
			if(!StringUtils.isEmpty(leaderCustId)){
				AppParam recordParams = new AppParam("storeHandleExtService","orderTransToManager");
				recordParams.addAttr("leaderCustId", leaderCustId);
				recordParams.addAttr("customerId", customerId);
				recordParams.addAttr("custId", custId);
				recordParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(recordParams);
			}
			//删除离职人名下的专属订单
			AppParam exParams = new AppParam("exclusiveOrderService","deleteByCustId");
			exParams.addAttr("customerId", customerId);
			exParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(exParams);
		}
	}
	
	
	/**
	 * 增加门店人员暂停分单信息
	 * @param 
	 * @return
	 */
	public static void addStorePauseAllotInfo(String customerId){
		//获取用户信息
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		String pauseDate = DateUtil.getSimpleFmt(new Date());
		String orgId = StringUtil.getString(custInfo.get("orgId"));
		//加入暂停分单表
		AppParam insertParam = new AppParam("storePauseAllotService","saveOrUpdate");
		insertParam.addAttr("customerId", customerId);
		insertParam.addAttr("orgId", orgId);
		insertParam.addAttr("pauseDate",pauseDate);
		insertParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		RemoteInvoke.getInstance().call(insertParam);
		
		//加入暂停分单统计表
		AppParam sumParam = new AppParam("sumStorePauseAllotService","saveOrUpdate");
		sumParam.addAttr("customerId", customerId);
		sumParam.addAttr("orgId", orgId);
		sumParam.addAttr("pauseDate",pauseDate);
		sumParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_sum));
		RemoteInvoke.getInstance().call(sumParam);
	}
	
	/**
	 * 删除门店人员暂停分单信息
	 * @param 
	 * @return
	 */
	public static void deletePauseAllotInfo(String customerId){
		AppParam queryParam = new AppParam("storePauseAllotService","queryCount");
		queryParam.addAttr("customerId", customerId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int count = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
		if(count >= 1){
			AppParam deleteParam = new AppParam("storePauseAllotService","delete");
			deleteParam.addAttr("customerId", customerId);
			deleteParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(deleteParam);
		}
	}
}
