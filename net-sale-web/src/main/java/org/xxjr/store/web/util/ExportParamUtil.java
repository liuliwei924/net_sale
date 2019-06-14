package org.xxjr.store.web.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.NumberUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

public class ExportParamUtil {
	public volatile static ExportParamUtil exportParamUtil = null;
	public static ExportParamUtil getInstance(){
		if(exportParamUtil == null){
			synchronized (ExportParamUtil.class) {
				if(exportParamUtil == null){
					exportParamUtil = new ExportParamUtil();
				}
			}
		}
		return exportParamUtil;
	}

	public static final String P_DAY = "day";
	public static final String P_MONTH = "month";
	public static final String P_RANGE = "range";
	public static final String P_DAY_PATTERN = "%Y-%m-%d";
	public static final String P_MONTH_PATTERN = "%Y-%m";
	public static final String P_COUNT_METHOD = "countMethod";
	
	/**
	 * 封装参数
	 * @param exportType
	 * @param param
	 * @param result
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static void  packParams(String exportType,AppParam params, 
			AppResult result,HttpServletRequest request) throws Exception {
		Class<ExportParamUtil> clazz = ExportParamUtil.class;
        Method method = clazz.getMethod(exportType,AppParam.class,AppResult.class,HttpServletRequest.class);
        method.invoke(getInstance(),params,result,request);
	}
	/***
	 * 新申请导出查询
	 * @param params
	 * @return
	 */
	public void storeWaitDeals(AppParam params,AppResult result,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "lastStore");
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryAllList");
		params.addAttr("orderStatus", "-1");
		params.addAttr("orderType", "1");//新单
		params.addAttr(P_COUNT_METHOD, "queryAllListCount");
		params.setOrderBy("lastTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	/***
	 * 所有订单导出查询
	 * @param params
	 * @return
	 */
	public void storeAllOrder(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		params.addAttr("status", "2");//门店锁定
		StoreUserUtil.dealUserAuthParam(params, customerId, "lastStore");
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String orderStatus = StringUtil.getString(params.getAttr("orderStatus"));
		if("0".equals(orderStatus)){
			params.addAttr("orderStatus","0");
		}
		params.removeAttr("custLabel");
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		String roleType = StoreUserUtil.getCustomerRole(customerId);
		params.addAttr("roleType",roleType);
		//资产信息
		String assetInfoIn = StringUtil.getString(params.getAttr("assetInfoIn"));
		if(!StringUtils.isEmpty(assetInfoIn)){
			String[] assetInfoArr = assetInfoIn.split(",");
			List<String> assetInfoList = Arrays.asList(assetInfoArr);
			for(String str : assetInfoList){
				// 1-房产 2-车产 3-保单 4-社保5-公积金 6-微粒贷
				if("1".equals(str)){
					params.addAttr("houseType","1");
				}else if("2".equals(str)){
					params.addAttr("carType","2");
				}else if("3".equals(str)){
					params.addAttr("insure","3");
				}else if("4".equals(str)){
					params.addAttr("socialType","4");
				}else if("5".equals(str)){
					params.addAttr("fundType","5");
				}else if("6".equals(str)){
					params.addAttr("havePinan","6");
				}
			}
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryAllList");
		params.addAttr(P_COUNT_METHOD, "queryAllListCount");
		params.setOrderBy("lastUpdateTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	/***
	 * 预约中导出查询
	 * @param params
	 * @return
	 */
	public void storeReserving(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "lastStore");
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		params.setService("storeHandleExtService");
		params.setMethod("queryBookOrderList");
		params.addAttr(P_COUNT_METHOD, "queryBookOrderListCount");
		params.addAttr("orderStatusNotIn", "7,8");//不查询无效客户和空号/错号客户
		params.addAttr("bookStatus", 1);
		params.setOrderBy("bookTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	/***
	 * 已上门导出查询
	 * @param params
	 * @return
	 */
	public void storeVisitOrder(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "lastStore");
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		params.addAttr("bookStatus", 3);
		params.setService("storeHandleExtService");
		params.setMethod("queryBookOrderList");
		params.addAttr(P_COUNT_METHOD, "queryBookOrderListCount");
		params.setOrderBy("visitTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	/***
	 * 签单列表导出查询
	 * @param params
	 * @return
	 */
	public void storeSignOrder(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		String signSearchKey = StringUtil.getString(params.getAttr("signSearchKey"));
		if(!StringUtils.isEmpty(signSearchKey)){
			if(ValidUtils.validateTelephone(signSearchKey)){//加快查询效率
				params.addAttr("signMobile", signSearchKey);
				params.removeAttr("signSearchKey");
			}else{
				params.addAttr("signOrderName", signSearchKey);
				params.removeAttr("signSearchKey");
			}
		}
		String roleType = StoreUserUtil.getCustomerRole(customerId);
		params.addAttr("roleType", roleType);
		params.setOrderBy("t.createTime");
		params.setService("storeListOptExtService");
		params.setMethod("querySigned");
		params.addAttr(P_COUNT_METHOD, "querySignedCount");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	/***
	 * 签单结案导出查询
	 * @param params
	 * @return
	 */
	public void storeSignEnd(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		String signSearchKey = StringUtil.getString(params.getAttr("signSearchKey"));
		if(!StringUtils.isEmpty(signSearchKey)){
			if(ValidUtils.validateTelephone(signSearchKey)){//加快查询效率
				params.addAttr("signMobile", signSearchKey);
				params.removeAttr("signSearchKey");
			}else{
				params.addAttr("signOrderName", signSearchKey);
				params.removeAttr("signSearchKey");
			}
		}
		String roleType = StoreUserUtil.getCustomerRole(customerId);
		params.addAttr("roleType", roleType);
		params.addAttr("signStatus", 2);
		params.setOrderBy("t.createTime");
		params.setService("storeListOptExtService");
		params.setMethod("querySigned");
		params.addAttr(P_COUNT_METHOD, "querySignedCount");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	/**
	 * 回款列表查询
	 * @param request
	 * @return
	 */
	public void storeBackAmount(AppParam params, AppResult result,HttpServletRequest request) {
		RequestUtil.setAttr(params, request);
		String searchKey = StringUtil.getString(params.getAttr("searchKey"));
		if(!StringUtils.isEmpty(searchKey)){
			if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
		}
		String storeSearchKey = StringUtil.getString(params.getAttr("storeSearchKey"));
		if(!StringUtils.isEmpty(storeSearchKey)){
			if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
				params.addAttr("mobile", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}else{
				params.addAttr("realName", storeSearchKey);
				params.removeAttr("storeSearchKey");
			}
		}
		String reLoanSearchKey = StringUtil.getString(params.getAttr("reLoanSearchKey"));
		if(!StringUtils.isEmpty(reLoanSearchKey)){
			if(ValidUtils.validateTelephone(reLoanSearchKey)){//加快查询效率
				params.addAttr("reLoanMobile", reLoanSearchKey);
				params.removeAttr("reLoanSearchKey");
			}else{
				params.addAttr("reLoanName", reLoanSearchKey);
				params.removeAttr("reLoanSearchKey");
			}
		}
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		String custLabel = StringUtil.getString(params.getAttr("custLabel"));
		if("0".equals(custLabel)){
			params.addAttr("custLabel","0");
		}
		String roletype = StoreUserUtil.getCustomerRole(StringUtil.getString(customerId));
		params.addAttr("roleType",roletype);
		params.setOrderBy("t.feeAmountDate,t.recordId");
		params.setOrderValue("desc,asc");
		params.setMethod("queryReLoan");
		params.addAttr(P_COUNT_METHOD, "queryReLoanCount");
		params.setService("storeListOptExtService");;
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	
	/***
	 * 门店人员今日统计查询
	 * @param params
	 * @return
	 */
	public void storePersonToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumUtilExtService");
		params.setMethod("queryStoreToDay");
		params.addAttr(P_COUNT_METHOD, "queryStoreToDayCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 * 门店人员本月统计查询
	 * @param request
	 * @return
	 */
	public void storePersonDay(AppParam params, AppResult result,HttpServletRequest request) {
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStoreBaseService");
		params.setMethod("queryShow");
		params.addAttr(P_COUNT_METHOD, "queryShowCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/**
	 * 门店人员月度统计查询
	 * @param request
	 * @return
	 */
	public void storePersonMonth(AppParam params, AppResult result,HttpServletRequest request) {
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStoreBaseMonthService");
		params.setMethod("queryStoreBaseMonth");
		params.addAttr(P_COUNT_METHOD, "queryStoreBaseMonthCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/***
	 * 总的今日统计查询
	 * @param params
	 * @return
	 */
	public void storeAllToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumUtilExtService");
		params.setMethod("queryTotalToDay");
		params.addAttr(P_COUNT_METHOD, "queryTotalToDayCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	/***
	 * 总的本月统计查询
	 * @param params
	 * @return
	 */
	public void storeAllDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumTotalBaseService");
		params.setMethod("queryDay");
		params.addAttr(P_COUNT_METHOD, "queryDayCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 总的月度统计查询
	 * @param params
	 * @return
	 */
	public void storeAllMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumTotalBaseService");
		params.setMethod("queryMonth");
		params.addAttr(P_COUNT_METHOD, "queryMonthCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店今日统计查询
	 * @param params
	 * @return
	 */
	public void storeOrgToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumUtilExtService");
		params.setMethod("queryOrgToDay");
		params.addAttr(P_COUNT_METHOD, "queryOrgToDayCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 门店本月统计查询
	 * @param params
	 * @return
	 */
	public void storeOrgDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumOrgBaseService");
		params.setMethod("queryByStore");
		params.addAttr(P_COUNT_METHOD, "queryByStoreCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店月度统计查询
	 * @param params
	 * @return
	 */
	public void storeOrgMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumOrgBaseService");
		params.setMethod("queryStoreMonth");
		params.addAttr(P_COUNT_METHOD, "queryStoreMonthCount");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 订单状态今日统计列表查询
	 * @param params
	 * @return
	 */
	public void storeDealOrderToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("storeListOptExtService");
		params.setMethod("queryTypeTodayByPage");
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.addAttr(P_COUNT_METHOD, "queryDealOrderTypeCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 订单状态本月统计列表查询
	 * @param params
	 * @return
	 */
	public void storeDealOrderDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumDealOrderTypeService");
		params.setMethod("queryDealOrderTypeDay");
		params.addAttr(P_COUNT_METHOD, "queryDealOrderTypeDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 订单状态月度统计列表查询
	 * @param params
	 * @return
	 */
	public void storeDealOrderMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumDealOrderTypeService");
		params.setMethod("queryDealOrderTypeMonth");
		params.addAttr(P_COUNT_METHOD, "queryDealOrderTypeMonthCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 订单状态渠道今日统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrderChannelToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("storeApplyExtService");
		params.setMethod("queryOrderChannelTodayByPage");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.addAttr(P_COUNT_METHOD, "queryOrderChannelTodayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 订单状态渠道本月统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrderChannelDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumChannelDealordertypeService");
		params.setMethod("queryChannelDealordertypeDay");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryChannelDealordertypeDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 订单状态渠道月度统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrderChannelMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumChannelDealordertypeService");
		params.setMethod("queryChannelDealordertypeMonth");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryChannelDealordertypeDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/***
	 * 订单评分今日统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrderRateToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.setService("storeApplyExtService");
		params.setMethod("queryRateTodayByPage");
		params.addAttr(P_COUNT_METHOD, "queryRateTodayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	/***
	 * 订单评分本月统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrderRateDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumOrderRateService");
		params.setMethod("queryRateDay");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryRateDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 订单评分月度统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrderRateMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumOrderRateService");
		params.setMethod("queryRateMonth");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryRateMonthCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 签单失败今日统计列表查询
	 * @param params
	 * @return
	 */
	public void storeSignFailToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.setService("storeApplyExtService");
		params.setMethod("querySignFailTodayByPage");
		params.addAttr(P_COUNT_METHOD, "querySignFailTodayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 签单失败本月统计列表查询
	 * @param params
	 * @return
	 */
	public void storeSignFailDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.setService("sumSignFailChannelService");
		params.setMethod("querySignFailDay");
		params.addAttr(P_COUNT_METHOD, "querySignFailDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 签单失败月度统计列表查询
	 * @param params
	 * @return
	 */
	public void storeSignFailMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.setService("sumSignFailChannelService");
		params.setMethod("querySignFailMonth");
		params.addAttr(P_COUNT_METHOD, "querySignFailMonthCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店通话今日统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrgCallToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("storeCallRecordService");
		params.setMethod("sumOrgCallByPage");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "sumOrgCallCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 门店通话本月统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrgCallDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStoreCallService");
		params.setMethod("queryStoreCallDay");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryStoreCallDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店通话月度统计列表查询
	 * @param params
	 * @return
	 */
	public void storeOrgCallMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStoreCallService");
		params.setMethod("queryStoreCallMonth");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryStoreCallMonthCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/***
	 * 订单状态分组今日统计（门店）列表查询
	 * @param params
	 * @return
	 */
	public void storeOrgDealOrderToDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("storeListOptExtService");
		params.setMethod("queryOrgDealOrderTodayByPage");
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryOrgDealOrderTodayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 订单状态分组本月统计（门店）列表查询
	 * @param params
	 * @return
	 */
	public void storeOrgDealOrderDay(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumOrgDealOrderService");
		params.setMethod("queryOrgDealOrderDay");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryOrgDealOrderDayCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 订单状态分组月度统计（门店）列表查询
	 * @param params
	 * @return
	 */
	public void storeOrgDealOrderMonth(AppParam params,AppResult result,HttpServletRequest request){
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumOrgDealOrderService");
		params.setMethod("queryOrgDealOrderMonth");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryOrgDealOrderMonthCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店人员通话日统计列表查询
	 * @param params
	 * @return
	 */
	public void storeCallDay(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.addAttr("recordDate", DateUtil.toStringByParttern(new Date(),
				DateUtil.DATE_PATTERN_YYYY_MM_DD));
		params.addAttr("realFlag", 1);	
		String realName = StringUtil.getString(params.getAttr("realName"));
		if(ValidUtils.validateTelephone(realName)){ //验证是否是手机号
			params.addAttr("telephone",params.removeAttr("realName"));
		}
		params.setService("storeCallRecordService");
		params.setMethod("sumStoreCallByPage");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "sumStoreCallCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 门店人员通话本月统计列表查询
	 * @param params
	 * @return
	 */
	public void storeCallMonth(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStoreCallService");
		params.setMethod("queryShow");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryShowCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店人员通话月度统计列表查询
	 * @param params
	 * @return
	 */
	public void storeCallMonthly(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStoreCallService");
		params.setMethod("queryMonth");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryShowCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店人员暂停情况统计列表查询(实时)
	 * @param params
	 * @return
	 */
	public void storePauseReal(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("storePauseAllotService");
		params.setMethod("queryStorePauseAllotList");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryStorePauseAllotCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 门店人员暂停情况统计列表查询(历史)
	 * @param params
	 * @return
	 */
	public void storePauseHistory(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStorePauseAllotService");
		params.setMethod("queryStorePauseAllotList");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryStorePauseAllotCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/***
	 * 门店暂停情况统计列表查询(实时)
	 * @param params
	 * @return
	 */
	public void orgPauseReal(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("storePauseAllotService");
		params.setMethod("queryOrgPauseAllotList");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryOrgPauseAllotCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/***
	 * 门店暂停情况统计列表查询(历史)
	 * @param params
	 * @return
	 */
	public void orgPauseHistory(AppParam params,AppResult result,HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setService("sumStorePauseAllotService");
		params.setMethod("queryOrgPauseAllotList");
		params.setOrderBy(request.getParameter("orderBy"));
		params.setOrderValue(request.getParameter("orderValue"));
		params.addAttr(P_COUNT_METHOD, "queryOrgPauseAllotCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/**
	 * 大渠道统计（已跟进）-基本情况统计
	 * @param params
	 * @return
	 */
	public void channelBase(AppParam params, AppResult result, HttpServletRequest request) {
		params.setService("channelModifySumService");
		params.setMethod("channelBase");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(P_DAY.equals(dateType)){
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.addAttr("datePattern", P_DAY_PATTERN);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else if (P_RANGE.equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
			params.addAttr("datePattern", P_MONTH_PATTERN);
			params.addAttr("startRecordDate", startRecordMonth+"-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	
	/**
	 * 
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelDtl(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.setService("channelModifySumService");
		params.setMethod("channelDtl");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	
	/**
	 * 网销门店情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelNet(AppParam params, AppResult result, HttpServletRequest request){
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.setService("channelModifySumService");
		params.setMethod("channelNet");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else{//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	
	/**
	 * 渠道城市情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelCity(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		String configCitys = StringUtil.getString(params.getAttr("configCitys"));
		if (!StringUtils.isEmpty(configCitys)) {
			params.addAttr("configCitys", configCitys.split(","));
		}
		
		params.setService("channelModifySumService");
		params.setMethod("channelCityDate");
		params.addAttr(P_COUNT_METHOD, "channelCityDateCount");
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");

		}else if ("range".equals(dateType)) {
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			params.addAttr("endDate", endRecordDate);
			params.addAttr("datePattern", "%Y-%m-%d");
			params.setMethod("channelCity");
			params.addAttr(P_COUNT_METHOD, "channelCityCount");
		}else {//按月
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordDate+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}
		
		if (1 == NumberUtil.getInt(request.getParameter("isReal"), 0)) {
			params.setService("channelMsSectionSumService");
			if("day".equals(dateType) || "month".equals(dateType)){
				params.setMethod("realChannelCityDate");
				params.addAttr(P_COUNT_METHOD, "realChannelCityDateCount");
			}else {
				params.setMethod("realChannelCitySec");
				params.addAttr(P_COUNT_METHOD, "realChannelCitySecCount");
			}
		}
		
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 * 门店成本统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void orgCost(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		
		params.setService("orgCostRecordService");
		params.setMethod("queryOrgCost");
		params.addAttr(P_COUNT_METHOD, "queryOrgCostCount");
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");

		}else if ("range".equals(dateType)) {
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("datePattern", "%Y-%m-%d");
			params.setMethod("queryOrgCostRange");
			params.addAttr(P_COUNT_METHOD, "queryOrgCostRangeCount");
		}else {//按月
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordDate+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}
		
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 * 门店人员成本统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void storeCost(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		
		params.setService("orgCostRecordService");
		params.setMethod("queryStoreCost");
		params.addAttr(P_COUNT_METHOD, "queryStoreCostCount");
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");

		}else if ("range".equals(dateType)) {
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("datePattern", "%Y-%m-%d");
			params.setMethod("queryStoreCostRange");
			params.addAttr(P_COUNT_METHOD, "queryStoreCostRangeCount");
		}else {//按月
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordDate+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}
		
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	
	/**
	 * 渠道数据成本统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelCost(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		
		String customerId = StoreUserUtil.getCustomerId(request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		
		params.setService("orgCostRecordService");
		params.setMethod("queryChannelCost");
		params.addAttr(P_COUNT_METHOD, "queryChannelCostCount");
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");

		}else if ("range".equals(dateType)) {
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("datePattern", "%Y-%m-%d");
			params.setMethod("queryChannelCostRange");
			params.addAttr(P_COUNT_METHOD, "queryChannelCostRangeCount");
		}else {//按月
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordDate+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}
		
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 * 渠道统计（第三方）
	 * @param params
	 * @param result
	 * @param request
	 */
	public void thirdChannel(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.addAttr(P_COUNT_METHOD, "thirdChannelCount");
		params.setService("channelDtlModifySumService");
		params.setMethod("thirdChannel");
		params.addAttr("datePattern", P_DAY_PATTERN);
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.addAttr("datePattern", P_MONTH_PATTERN);
			startRecordDate = startRecordDate + "-01";
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.setMethod("thirdChannelSect");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}
		
		String customerId = StoreUserUtil.getCustomerId(request); 
		Map<String,Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		String roleType =   StringUtil.getString(custInfo.get("roleType"));
		
		if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)) {
			Object fixChannels = custInfo.get("sourceType");
			params.addAttr("fixChannels", fixChannels);//固定渠道
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
}
