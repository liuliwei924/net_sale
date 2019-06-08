package org.xxjr.store.web.action.account.dataset;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.kefu.AfterMethodUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.web.util.ExportParamUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

@Controller()
@RequestMapping("/account/dataset/")
/**
 * 数据统计
 * @author Administrator
 *
 */
public class DataStatisticsAction {
	
	/**
	 * 门店人员今日统计
	 */
	@RequestMapping("storeCount/queryStoreToDay")
	@ResponseBody
	public AppResult queryStoreToDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);

		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumUtilExtService");
			params.setMethod("queryStoreToDay");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryStoreToDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	

	/**
	 * 门店人员日统计
	 */
	@RequestMapping("storeCount/queryStorePerSumary")
	@ResponseBody
	public AppResult storePersionSumary(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStoreBaseService");
			params.setMethod("queryShow");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryStorePerSumary data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 门店人员月统计
	 */
	@RequestMapping("storeCount/queryStorePerMonth")
	@ResponseBody
	public AppResult queryStorePerMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);	
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStoreBaseMonthService");
			params.setMethod("queryStoreBaseMonth");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryStorePerMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 门店今日统计
	 */
	@RequestMapping("orgCount/queryOrgToDay")
	@ResponseBody
	public AppResult queryOrgToDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumUtilExtService");
			params.setMethod("queryOrgToDay");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgToDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	
	/**
	 * 门店本月统计
	 */
	@RequestMapping("orgCount/queryOrgDay")
	@ResponseBody
	public AppResult queryOrgDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumOrgBaseService");
			params.setMethod("queryByStore");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 门店月统计
	 */
	@RequestMapping("orgCount/queryOrgMonth")
	@ResponseBody
	public AppResult queryOrgMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);	
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumOrgBaseService");
			params.setMethod("queryStoreMonth");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 总的今日统计
	 */
	@RequestMapping("allCount/queryTotalToDay")
	@ResponseBody
	public AppResult queryTotalToDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtService");
			params.setMethod("queryTotalToDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryTotalToDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 每天总的统计
	 */
	@RequestMapping("allCount/querySumaryDay")
	@ResponseBody
	public AppResult querySumaryDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumTotalBaseService");
			params.setMethod("queryDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySumaryDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 每月总的统计
	 */
	@RequestMapping("allCount/querySumaryMonth")
	@ResponseBody
	public AppResult querySumaryMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumTotalBaseService");
			params.setMethod("queryMonth");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySumaryMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店分组统计
	 * 
	 */
	@RequestMapping("groupCount/queryGroupCount")
	@ResponseBody
	public AppResult queryGroupCount(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		String orgId = request.getParameter("orgId");
		String comparam = request.getParameter("comparam");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String methodName = "";
		
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}else if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("orgId不能为空");
			return result;
		}else if(StringUtils.isEmpty(comparam)){
			result.setSuccess(false);
			result.setMessage("比较参数不能为空");
			return result;
		}else if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) ){
			result.setSuccess(false);
			result.setMessage("开始日期或结束日期不能为空");
			return result;
		}		
		try {
			
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
			if(StringUtils.isEmpty(userOrgs) || 
					(!"all".equalsIgnoreCase(userOrgs) && !userOrgs.contains(orgId))){
				result.setSuccess(false);
				result.setMessage("没有查询该门店分组统计的权限");
				return result;
			}
			
			switch(Integer.parseInt(comparam)){
			case BorrowConstant.COMPWAY_1: 	methodName = "queryGroupCountByDeal";
											params.addAttr("status", "2");
											params.addAttr("orderStatus", "-1");break;
			case BorrowConstant.COMPWAY_2: 	params.addAttr("handleType", "0");
										   	methodName = "queryGroupByRecOrBack";break;
			case BorrowConstant.COMPWAY_3: 	params.addAttr("isFeedback", 1);
										   	methodName = "queryGroupByRecOrBack";break;
			case BorrowConstant.COMPWAY_4: 	params.addAttr("status", 3);
										   	methodName = "queryGroupCountByBook";break;
			case BorrowConstant.COMPWAY_5: 	params.addAttr("isCount", 1);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_6: 	methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_7: 	params.addAttr("status", 1);
										   	params.addAttr("isCount", 1);
										   	methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_8: 	params.addAttr("status", 1);
										   	methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_9:  params.addAttr("status", 2);
										    params.addAttr("isCount", 1);
										    methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_10: params.addAttr("status", 2);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_11: params.addAttr("status", 3);
											params.addAttr("isCount", 1);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_12: params.addAttr("status", 3);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_13: params.addAttr("status", 2);
											params.addAttr("isCount", 1);
											methodName = "queryGroupCountByRet";break;
			case BorrowConstant.COMPWAY_14: params.addAttr("status", 2);
											methodName = "queryGroupCountByRet";break;
			case BorrowConstant.COMPWAY_15: params.addAttr("status", 1);
											params.addAttr("isCount", 1);
											methodName = "queryGroupCountByRet";break;
			case BorrowConstant.COMPWAY_16: params.addAttr("status", 1);
											methodName = "queryGroupCountByRet";break;
			default:  result.setSuccess(false);
					  result.setMessage("比较参数不存在");return result;
			        
			}
			params.setService("sumUtilExtService");;
			params.setMethod(methodName);
			params.addAttr("groupByGroup", "groupByGroup");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryGroupCount data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 门店分组统计
	 * 
	 */
	@RequestMapping("teamCount/queryTeamCount")
	@ResponseBody
	public AppResult queryTeamCount(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		String orgId = request.getParameter("orgId");
		String comparam = request.getParameter("comparam");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String methodName = "";
		
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}else if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("orgId不能为空");
			return result;
		}else if(StringUtils.isEmpty(comparam)){
			result.setSuccess(false);
			result.setMessage("比较参数不能为空");
			return result;
		}else if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) ){
			result.setSuccess(false);
			result.setMessage("开始日期或结束日期不能为空");
			return result;
		}		
		try {
			
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
			if(StringUtils.isEmpty(userOrgs) || 
					(!"all".equalsIgnoreCase(userOrgs) && !userOrgs.contains(orgId))){
				result.setSuccess(false);
				result.setMessage("没有查询该门店分队统计的权限");
				return result;
			}
			
			switch(Integer.parseInt(comparam)){
			case BorrowConstant.COMPWAY_1: 	methodName = "queryGroupCountByDeal";
											params.addAttr("status", "2");
											params.addAttr("orderStatus", "-1");break;
			case BorrowConstant.COMPWAY_2: 	params.addAttr("handleType", "0");
										   	methodName = "queryGroupByRecOrBack";break;
			case BorrowConstant.COMPWAY_3: 	params.addAttr("isFeedback", 1);
										   	methodName = "queryGroupByRecOrBack";break;
			case BorrowConstant.COMPWAY_4: 	params.addAttr("status", 3);
										   	methodName = "queryGroupCountByBook";break;
			case BorrowConstant.COMPWAY_5: 	params.addAttr("isCount", 1);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_6: 	methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_7: 	params.addAttr("status", 1);
										   	params.addAttr("isCount", 1);
										   	methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_8: 	params.addAttr("status", 1);
										   	methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_9:  params.addAttr("status", 2);
										    params.addAttr("isCount", 1);
										    methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_10: params.addAttr("status", 2);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_11: params.addAttr("status", 3);
											params.addAttr("isCount", 1);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_12: params.addAttr("status", 3);
											methodName = "queryGroupCountBySign";break;
			case BorrowConstant.COMPWAY_13: params.addAttr("status", 2);
											params.addAttr("isCount", 1);
											methodName = "queryGroupCountByRet";break;
			case BorrowConstant.COMPWAY_14: params.addAttr("status", 2);
											methodName = "queryGroupCountByRet";break;
			case BorrowConstant.COMPWAY_15: params.addAttr("status", 1);
											params.addAttr("isCount", 1);
											methodName = "queryGroupCountByRet";break;
			case BorrowConstant.COMPWAY_16: params.addAttr("status", 1);
											methodName = "queryGroupCountByRet";break;
			default:  result.setSuccess(false);
					  result.setMessage("比较参数不存在");
					  return result;
			        
			}
			params.setService("sumUtilExtService");;
			params.setMethod(methodName);
			params.addAttr("groupByTeam", "groupByTeam");
			params.addAttr("roleTypeNot", "8");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryGroupCount data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	

	
	/**
	 * 门店人员今天实时的通话记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("queryRealDay")
	@ResponseBody
	public AppResult queryRealDay(HttpServletRequest request){
		
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
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
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员今天实时的通话记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 门店人员本月的通话记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("queryDay")
	@ResponseBody
	public AppResult queryDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStoreCallService");
			params.setMethod("queryShow");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员本月的通话记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 门店人员月度的通话记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("queryMonth")
	@ResponseBody
	public AppResult queryMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStoreCallService");
			params.setMethod("queryMonth");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员月度的通话记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 再分配池情况统计
	 * @param request
	 * @return
	 */
	@RequestMapping("allotPondCount/queryAllotPondStatics")
	@ResponseBody
	public AppResult queryAllotPondStatics(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("netStorePoolService");
			params.setMethod("queryAllotPondStatics");
			params.setEveryPage(20);
			params.setOrderBy("cityName");
			params.setOrderValue("asc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "再分配池情况统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店分组统计排名
	 * @param request
	 * @return
	 */
	@RequestMapping("groupRank/queryOrgGroupRank")
	@ResponseBody
	public AppResult queryOrgGroupRank(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumUtilExtService");
			params.setMethod("queryOrgGroupRank");
			String groupName = StringUtil.getString(params.getAttr("groupName"));
			if(StringUtils.isEmpty(groupName)){
				params.addAttr("groupNameType", "1");
			}
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			
			params.setMethod("queryOrgGroupDuration");
			AppResult newResult = RemoteInvoke.getInstance().callNoTx(params);
			if(newResult.getRows().size() > 0){
				result.putAttr("growthDurRateTotal",newResult.getRow(0).get("growthDurRate"));
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店分组统计排名报错");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店分队统计排名
	 * @param request
	 * @return
	 */
	@RequestMapping("teamRank/queryOrgTeamRank")
	@ResponseBody
	public AppResult queryOrgTeamRank(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumUtilExtService");
			params.setMethod("queryOrgTeamRank");
			String teamName = StringUtil.getString(params.getAttr("teamName"));
			if(StringUtils.isEmpty(teamName)){
				params.addAttr("teamNameType", "1");
			}
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			params.setMethod("queryOrgTeamDuration");
			AppResult newResult = RemoteInvoke.getInstance().callNoTx(params);
			if(newResult.getRows().size() > 0){
				result.putAttr("growthDurRateTotal",newResult.getRow(0).get("growthDurRate"));
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店分队统计排名报错");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店分队统计排名求和
	 * @param request
	 * @return
	 */
	@RequestMapping("teamRank/queryOrgSumRank")
	@ResponseBody
	public AppResult queryOrgSumRank(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumUtilExtService");
			params.setMethod("queryOrgSumRank");
			params.addAttr("roleTypeIn", "3,9");
			String teamName = StringUtil.getString(params.getAttr("teamName"));
			if(StringUtils.isEmpty(teamName)){
				params.addAttr("teamNameType", "1");
			}
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店统计排名求和报错");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店分组统计排名求和
	 * @param request
	 * @return
	 */
	@RequestMapping("groupRank/queryOrgGroupSumRank")
	@ResponseBody
	public AppResult queryOrgGroupSumRank(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumUtilExtService");
			params.setMethod("queryOrgSumRank");
			params.addAttr("roleTypeIn", "3,8,9");
			String groupName = StringUtil.getString(params.getAttr("groupName"));
			if(StringUtils.isEmpty(groupName)){
				params.addAttr("groupNameType", "1");
			}
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店分组统计排名求和报错");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 城市今日分单情况
	 */
	@RequestMapping("cityAllotCount/queryCityAllotDay")
	@ResponseBody
	public AppResult queryCityAllotDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtService");
			params.setMethod("queryCityAllotDay");
			params.setEveryPage(20);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryCityAllotDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 城市本周分单情况
	 */
	@RequestMapping("cityAllotCount/queryCityAllotWeek")
	@ResponseBody
	public AppResult queryCityAllotWeek(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtService");
			params.setMethod("queryCityAllotWeek");
			params.setEveryPage(20);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryCityAllotWeek data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 城市本月分单情况
	 */
	@RequestMapping("cityAllotCount/queryCityAllotMonth")
	@ResponseBody
	public AppResult queryCityAllotMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtService");
			params.setMethod("queryCityAllotMonth");
			params.setEveryPage(20);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryCityAllotMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询风控今日统计
	 */
	@RequestMapping("riskCount/queryRiskToDay")
	@ResponseBody
	public AppResult queryRiskToDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtService");
			params.setMethod("queryRiskToDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRiskToDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询风控日统计
	 */
	@RequestMapping("riskCount/queryRiskDay")
	@ResponseBody
	public AppResult queryRiskDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumRiskBaseService");
			params.setMethod("queryRiskDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRiskDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询风控月度统计
	 */
	@RequestMapping("riskCount/queryRiskMonth")
	@ResponseBody
	public AppResult queryRiskMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumRiskBaseService");
			params.setMethod("queryRiskMonth");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRiskMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询城市本周统计
	 */
	@RequestMapping("cityAnalyCount/queryThisWeekByCity")
	@ResponseBody
	public AppResult queryThisWeekByCity(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryThisWeekByCity");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryWeekCountByCity data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询城市本月统计
	 */
	@RequestMapping("cityAnalyCount/queryThisMonthByCity")
	@ResponseBody
	public AppResult queryThisMonthByCity(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryThisMonthByCity");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryThisMonthByCity data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询城市月度统计
	 */
	@RequestMapping("cityAnalyCount/queryMonthlyByCity")
	@ResponseBody
	public AppResult queryMonthlyByCity(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryMonthlyByCity");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryMonthlyByCity data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 查询门店本周统计
	 */
	@RequestMapping("orgAnalyCount/queryThisWeekByOrg")
	@ResponseBody
	public AppResult queryThisWeekByOrg(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryThisWeekByOrg");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryThisWeekByOrg data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询门店本月统计
	 */
	@RequestMapping("orgAnalyCount/queryThisMonthByOrg")
	@ResponseBody
	public AppResult queryThisMonthByOrg(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryThisMonthByOrg");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryThisMonthByOrg data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询门店月度统计
	 */
	@RequestMapping("orgAnalyCount/queryMonthlyByOrg")
	@ResponseBody
	public AppResult queryMonthlyByOrg(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryMonthlyByOrg");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryMonthlyByOrg data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 按日期维度查询相关数据统计
	 */
	@RequestMapping("dateAnalyCount/queryDataAnalyByDate")
	@ResponseBody
	public AppResult queryDataAnalyByDate(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryDataAnalyByDate");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryDataAnalyByDate data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店订单状态今日统计 按队名分组
	 */
	@RequestMapping("dealOrderTypeCount/queryDealOrderTypeToday")
	@ResponseBody
	public AppResult queryDealOrderTypeToday(HttpServletRequest request){
		AppResult result =  new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setService("storeListOptExtService");
			params.setMethod("queryTypeTodayByPage");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryDealOrderTypeToday error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店订单状态月统计 按队名分组
	 */
	
	@RequestMapping("dealOrderTypeCount/queryDealOrderTypeDay")
	@ResponseBody
	public AppResult queryDealOrderTypeDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumDealOrderTypeService");
			params.setMethod("queryDealOrderTypeDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryDealordertypeDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 门店订单状态月度统计
	 */
	@RequestMapping("dealOrderTypeCount/queryDealOrderTypeMonth")
	@ResponseBody
	public AppResult queryDealOrderTypeMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumDealOrderTypeService");
			params.setMethod("queryDealOrderTypeMonth");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryMonthlyByOrg data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店订单评分今日统计
	 */
	@RequestMapping("orderRateCount/queryRateToday")
	@ResponseBody
	public AppResult queryRateToday(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, customerId);
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setService("storeApplyExtService");
			params.setMethod("queryRateTodayByPage");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRateToday data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店订单评分月统计
	 */
	
	@RequestMapping("orderRateCount/queryRateDay")
	@ResponseBody
	public AppResult queryRateDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setService("sumOrderRateService");
			params.setMethod("queryRateDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRateDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店订单评月度统计
	 */
	@RequestMapping("orderRateCount/queryRateMonth")
	@ResponseBody
	public AppResult queryRateMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setService("sumOrderRateService");
			params.setMethod("queryRateMonth");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRateMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态渠道 今日统计
	 */
	@RequestMapping("orderChannelCount/queryOrderChannelToday")
	@ResponseBody
	public AppResult queryOrderChannelToday(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storeApplyExtService");
			params.setMethod("queryOrderChannelTodayByPage");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrderChannelToday data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态渠道  本月统计
	 */
	@RequestMapping("orderChannelCount/queryOrderChannelDay")
	@ResponseBody
	public AppResult queryOrderChannelDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumChannelDealordertypeService");
			params.setMethod("queryChannelDealordertypeDay");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrderChannelDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态渠道月度统计
	 */
	@RequestMapping("orderChannelCount/queryOrderChannelMonth")
	@ResponseBody
	public AppResult queryOrderChannelMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumChannelDealordertypeService");
			params.setMethod("queryChannelDealordertypeMonth");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));			
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrderChannelMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 签单失败渠道今日统计
	 */
	@RequestMapping("signFailCount/querySignFailToday")
	@ResponseBody
	public AppResult querySignFailToday(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, customerId);
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setService("storeApplyExtService");
			params.setMethod("querySignFailTodayByPage");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySignFailToday data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 签单失败渠道月统计
	 */
	
	@RequestMapping("signFailCount/querySignFailDay")
	@ResponseBody
	public AppResult querySignFailDay(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumSignFailChannelService");
			params.setMethod("querySignFailDay");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySignFailDay data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 签单失败渠道月度统计
	 */
	@RequestMapping("signFailCount/querySignFailMonth")
	@ResponseBody
	public AppResult querySignFailMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumSignFailChannelService");
			params.setMethod("querySignFailMonth");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySignFailMonth data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}	
	
	
	/**
	 * 门店今日通话记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("queryStoreCallToday")
	@ResponseBody
	public AppResult queryStoreCallToday(HttpServletRequest request){
		
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storeCallRecordService");
			params.setMethod("sumOrgCallByPage");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店今日通话记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店本月的通话记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("queryStoreCallDay")
	@ResponseBody
	public AppResult queryStoreCallDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStoreCallService");
			params.setMethod("queryStoreCallDay");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店本月的通话记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 门店月度的通话记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("queryStoreCallMonth")
	@ResponseBody
	public AppResult queryStoreCallMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStoreCallService");
			params.setMethod("queryStoreCallMonth");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店月度的通话记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态分组统计（门店） 今日
	 * @param request
	 * @return
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealOrderToday")
	@ResponseBody
	public AppResult queryOrgDealOrderToday(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storeListOptExtService");
			params.setMethod("queryOrgDealOrderTodayByPage");
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "订单状态分组今日统计（门店） 出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态分组统计（门店） 本月
	 * @param request
	 * @return
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealOrderDay")
	@ResponseBody
	public AppResult queryOrgDealOrderDay(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumOrgDealOrderService");
			params.setMethod("queryOrgDealOrderDay");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "订单状态分组本月统计（门店）出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 订单状态分组统计（门店） 月度
	 * @param request
	 * @return
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealOrderMonth")
	@ResponseBody
	public AppResult queryOrgDealOrderMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumOrgDealOrderService");
			params.setMethod("queryOrgDealOrderMonth");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "订单状态分组月度统计（门店）出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询订单状态统计详情
	 * @param request
	 * @return
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealDetail")
	@ResponseBody
	public AppResult queryOrgDealDetail(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			String orgId = request.getParameter("orgId");
			if(StringUtils.isEmpty(orgId)){
				return CustomerUtil.retErrorMsg("门店Id不能为空");
			}
			String groupName = request.getParameter("groupName");
			if(StringUtils.isEmpty(groupName)){
				return CustomerUtil.retErrorMsg("组名不能为空");
			}
			String columnName = request.getParameter("columnName");
			if(StringUtils.isEmpty(columnName)){
				return CustomerUtil.retErrorMsg("列名不能为空");
			}else{
				StoreUserUtil.dealOrderTypeParam(params, columnName);
			}
			RequestUtil.setAttr(params, request);
			params.setService("storeApplyExtService");
			params.setMethod("queryOrgDealDetail");
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询订单状态统计详情！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 按门店维度查询门店回款相关数据统计
	 */
	@RequestMapping("orgRepayAnalyCount/queryOrgRepaymentByDate")
	@ResponseBody
	public AppResult queryOrgRepaymentByDate(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String curentMonth = StringUtil.getString(params.getAttr("curentMonth"));
			if(StringUtils.isEmpty(curentMonth)){
				return CustomerUtil.retErrorMsg("月份不能为空");
			}
			String lastMonth = DateUtil.toStringByParttern(DateUtil.getNextMonth(
					DateUtil.toDateByString(curentMonth, "yyyy-MM"), -1),"yyyy-MM");
			params.addAttr("lastMonth",lastMonth);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryOrgRepaymentByDate");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgRepaymentByDate data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 按城市维度查询城市回款相关数据统计
	 */
	@RequestMapping("cityRepayAnalyCount/queryCityRepaymentByDate")
	@ResponseBody
	public AppResult queryCityRepaymentByDate(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			String cityName = StringUtil.getString(params.getAttr("cityName"));
			if(!CustConstant.CUST_ROLETYPE_1.equals(roleType) && StringUtils.isEmpty(cityName)){
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
				String orgId = StringUtil.getString(custInfo.get("orgId"));
				params.addAttr("cityName", OrgUtils.getCityNameByOrgId(orgId));
			}
			String curentMonth = StringUtil.getString(params.getAttr("curentMonth"));
			if(StringUtils.isEmpty(curentMonth)){
				return CustomerUtil.retErrorMsg("月份不能为空");
			}
			String lastMonth = DateUtil.toStringByParttern(DateUtil.getNextMonth(
					DateUtil.toDateByString(curentMonth, "yyyy-MM"), -1),"yyyy-MM");
			params.addAttr("lastMonth",lastMonth);
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryCityRepaymentByDate");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryCityRepaymentByDate data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店人员暂停情况统计(实时)
	 * @param request
	 * @return
	 */
	@RequestMapping("storePauseCount/queryStorePauseCount")
	@ResponseBody
	public AppResult queryStorePauseCount(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storePauseAllotService");
			params.setMethod("queryStorePauseAllotList");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员暂停情况统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店人员暂停情况统计(历史)
	 * @param request
	 * @return
	 */
	@RequestMapping("storePauseCount/queryStorePauseHisCount")
	@ResponseBody
	public AppResult queryStorePauseHisCount(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStorePauseAllotService");
			params.setMethod("queryStorePauseAllotList");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员暂停情况统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店暂停情况统计(实时)
	 * @param request
	 * @return
	 */
	@RequestMapping("orgPauseCount/queryOrgPauseCount")
	@ResponseBody
	public AppResult queryOrgPauseCount(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storePauseAllotService");
			params.setMethod("queryOrgPauseAllotList");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店暂停情况统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店暂停情况统计(历史)
	 * @param request
	 * @return
	 */
	@RequestMapping("orgPauseCount/queryOrgPauseHisCount")
	@ResponseBody
	public AppResult queryOrgPauseHisCount(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStorePauseAllotService");
			params.setMethod("queryOrgPauseAllotList");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店暂停情况统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询今日本月操作记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("handleRecordCount/queryHandRecordByDate")
	@ResponseBody
	public AppResult queryHandRecordByDate(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("sumHandleRecordService","queryHandRecordByDate");
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询操作记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询月度操作记录统计
	 * @param request
	 * @return
	 */
	@RequestMapping("handleRecordCount/queryHandRecordMonth")
	@ResponseBody
	public AppResult queryHandRecordMonth(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("sumHandleRecordService","queryHandRecordMonth");
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询月度操作记录统计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 大渠道基本情况统计
	 * @param request
	 * @return
	 */
	@RequestMapping("channelBase/query")
	@ResponseBody
	public AppResult channelBase(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			ExportParamUtil.getInstance().channelBase(params, result, request);
			if(result.isSuccess()){
				result = RemoteInvoke.getInstance().callNoTx(params);
				result = AfterMethodUtil.getInstance().addRecordCount(result, params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询基本情况统计数据出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 金额资质详细情况统计
	 * @param request
	 * @return
	 */
	@RequestMapping("channelDtl/query")
	@ResponseBody
	public AppResult channelDtl(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			ExportParamUtil.getInstance().channelDtl(params, result, request);
			if(result.isSuccess()){
				result = RemoteInvoke.getInstance().callNoTx(params);
				result = AfterMethodUtil.getInstance().analyzedAssetCountToPage(result, params);
				result = AfterMethodUtil.getInstance().addRecordCount(result, params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, " 金额资质详细情况统计！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 网销门店情况统计
	 * @param request
	 * @return
	 */
	@RequestMapping("channelNet/query")
	@ResponseBody
	public AppResult channelNet(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("channelModifySumService", "channelNet");
			RequestUtil.setAttr(params, request);
			ExportParamUtil.getInstance().channelNet(params, result, request);
			if(result.isSuccess()){
				result = RemoteInvoke.getInstance().callNoTx(params);
				result = AfterMethodUtil.getInstance().addRecordCount(result, params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "网销门店情况统计！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 渠道城市统计
	 * @param request
	 * @return
	 */
	@RequestMapping("channelCity/query")
	@ResponseBody
	public AppResult channelCity(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			ExportParamUtil.getInstance().channelCity(params, result, request);
			if(result.isSuccess()){
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "渠道城市统计！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
