package org.xxjr.store.web.action.account.dataset;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;

@Controller()
@RequestMapping("/account/dataset/")
/**
 * 数据统计总计
 * @author Administrator
 *
 */
public class DataStatisticsSumAction {
	
	/**
	 * 总的统计本月总计
	 */
	@RequestMapping("allCount/queryDaySumTotal")
	@ResponseBody
	public AppResult queryDaySum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumTotalBaseService");
			params.setMethod("queryDaySumTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryDaySumTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 总的统计月度总计
	 */
	@RequestMapping("allCount/queryMonthSumTotal")
	@ResponseBody
	public AppResult queryMonthSumTotal(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumTotalBaseService");
			params.setMethod("queryDaySumTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryMonthSumTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店日统计
	 */
	@RequestMapping("orgCount/queryOrgToDayTotal")
	@ResponseBody
	public AppResult queryOrgToDayTotal(HttpServletRequest request){
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
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryOrgToDayTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgToDayTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店本月统计总计
	 */
	@RequestMapping("orgCount/queryByStoreTotal")
	@ResponseBody
	public AppResult queryByStoreTotal(HttpServletRequest request){
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
			params.setMethod("queryByStoreTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryByStoreTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店月度统计总计
	 */
	@RequestMapping("orgCount/queryOrgMonthTotal")
	@ResponseBody
	public AppResult queryOrgMonthTotal(HttpServletRequest request){
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
			params.setMethod("queryOrgMonthTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgMonthTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店人员今日统计总计
	 */
	@RequestMapping("storeCount/queryStoreToDayTotal")
	@ResponseBody
	public AppResult queryStoreToDayTotal(HttpServletRequest request){
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
			params.setService("sumUtilExtTotalService");
			params.setMethod("queryStoreToDayTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryStoreToDayTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 门店人员本月统计总计
	 */
	@RequestMapping("storeCount/queryStoreDayTotal")
	@ResponseBody
	public AppResult queryStoreDayTotal(HttpServletRequest request){
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
			params.setMethod("queryStoreDayTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryStoreDayTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 门店人员月统计总计
	 */
	@RequestMapping("storeCount/queryStoreMonthTotal")
	@ResponseBody
	public AppResult queryStoreMonthTotal(HttpServletRequest request){
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
			params.setMethod("queryStoreMonthTotal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryStoreMothTotal data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 风控统计本月总计
	 */
	@RequestMapping("riskCount/queryRiskDaySum")
	@ResponseBody
	public AppResult queryRiskDaySum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumRiskBaseService");
			params.setMethod("queryRiskDaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRiskDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 风控统计月度总计
	 */
	@RequestMapping("riskCount/queryRiskMonthSum")
	@ResponseBody
	public AppResult queryRiskMonthSum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sumRiskBaseService");
			params.setMethod("queryRiskMonthSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRiskMonthSum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态今日统计总计
	 */
	@RequestMapping("dealOrderTypeCount/queryDealOrderTypeTodaySum")
	@ResponseBody
	public AppResult queryDealOrderTypeTodaySum(HttpServletRequest request){
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
			params.setService("storeListOptExtService");
			params.setMethod("queryDealOrderTypeTodaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryDealOrderTypeDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态本月统计总计
	 */
	@RequestMapping("dealOrderTypeCount/queryDealOrderTypeDaySum")
	@ResponseBody
	public AppResult queryDealOrderTypeDaySum(HttpServletRequest request){
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
			params.setService("sumDealOrderTypeService");
			params.setMethod("queryDealOrderTypeDaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryDealOrderTypeDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态统计月度总计
	 */
	@RequestMapping("dealOrderTypeCount/queryDealOrderTypeMonthSum")
	@ResponseBody
	public AppResult queryDealOrderTypeMonthSum(HttpServletRequest request){
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
			params.setService("sumDealOrderTypeService");
			params.setMethod("queryDealOrderTypeMonthSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRiskMonthSum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单评分今日统计总计
	 */
	@RequestMapping("orderRateCount/queryRateTodaySum")
	@ResponseBody
	public AppResult queryRateTodaySum(HttpServletRequest request){
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
			params.setService("storeApplyExtService");
			params.setMethod("queryRateTodaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRateTodaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 订单评分本月统计总计
	 */
	@RequestMapping("orderRateCount/queryRateDaySum")
	@ResponseBody
	public AppResult queryRateDaySum(HttpServletRequest request){
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
			params.setService("sumOrderRateService");
			params.setMethod("queryRateDaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRateDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 订单评分统计月度总计
	 */
	@RequestMapping("orderRateCount/queryRateMonthSum")
	@ResponseBody
	public AppResult queryRateMonthSum(HttpServletRequest request){
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
			params.setService("sumOrderRateService");
			params.setMethod("queryRateMonthSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryRateMonthSum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态渠道今日统计总计
	 */
	@RequestMapping("orderChannelCount/queryOrderChannelTodaySum")
	@ResponseBody
	public AppResult queryOrderChannelTodaySum(HttpServletRequest request){
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
			params.setService("storeApplyExtService");
			params.setMethod("queryOrderChannelTodaySum");
			params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrderChannelTodaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 *  订单状态渠道本月统计总计
	 */
	@RequestMapping("orderChannelCount/queryOrderChannelDaySum")
	@ResponseBody
	public AppResult queryOrderChannelDaySum(HttpServletRequest request){
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
			params.setService("sumChannelDealordertypeService");
			params.setMethod("queryChannelDealordertypeDaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrderChannelDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态渠道本月统计总计
	 */
	@RequestMapping("orderChannelCount/queryOrderChannelMonthSum")
	@ResponseBody
	public AppResult queryOrderChannelMonthSum(HttpServletRequest request){
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
			params.setService("sumChannelDealordertypeService");
			params.setMethod("queryChannelDealordertypeMonthSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrderChannelMonthSum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 签单失败渠道今日统计总计
	 */
	@RequestMapping("signFailCount/querySignFailTodaySum")
	@ResponseBody
	public AppResult querySignFailTodaySum(HttpServletRequest request){
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
			params.setService("storeApplyExtService");
			params.setMethod("querySignFailTodaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySignFailTodaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 签单失败渠道本月统计总计
	 */
	@RequestMapping("signFailCount/querySignFailDaySum")
	@ResponseBody
	public AppResult querySignFailDaySum(HttpServletRequest request){
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
			params.setService("sumSignFailChannelService");
			params.setMethod("querySignFailDaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySignFailDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 签单失败渠道月度统计总计
	 */
	@RequestMapping("signFailCount/querySignFailMonthSum")
	@ResponseBody
	public AppResult querySignFailMonthSum(HttpServletRequest request){
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
			params.setService("sumSignFailChannelService");
			params.setMethod("querySignFailMonthSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count querySignFailMonthSum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 订单状态分组统计总计（门店） 今日
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealOrderTodaySum")
	@ResponseBody
	public AppResult queryOrgDealOrderTodaySum(HttpServletRequest request){
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
			params.setService("storeListOptExtService");
			params.setMethod("queryOrgDealOrderTodaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgDealOrderTodaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 *  订单状态分组统计总计（门店）本月
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealOrderDaySum")
	@ResponseBody
	public AppResult queryOrgDealOrderDaySum(HttpServletRequest request){
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
			params.setService("sumOrgDealOrderService");
			params.setMethod("queryOrgDealOrderDaySum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgDealOrderDaySum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单状态分组统计总计（门店） 月度
	 */
	@RequestMapping("orgDealOrderCount/queryOrgDealOrderMonthSum")
	@ResponseBody
	public AppResult queryOrgDealOrderMonthSum(HttpServletRequest request){
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
			params.setService("sumOrgDealOrderService");
			params.setMethod("queryOrgDealOrderMonthSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "count queryOrgDealOrderMonthSum data error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店人员暂停情况统计统计(实时)
	 * @param request
	 * @return
	 */
	@RequestMapping("storePauseCount/queryStorePauseCountSum")
	@ResponseBody
	public AppResult queryStorePauseCountSum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storePauseAllotService");
			params.setMethod("queryStorePauseAllotSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员暂停情况统计总计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店人员暂停情况统计总计(历史)
	 * @param request
	 * @return
	 */
	@RequestMapping("storePauseCount/queryStorePauseHisSum")
	@ResponseBody
	public AppResult queryStorePauseHisSum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStorePauseAllotService");
			params.setMethod("queryStorePauseAllotSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店人员暂停情况统计总计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店暂停情况统计总计(实时)
	 * @param request
	 * @return
	 */
	@RequestMapping("orgPauseCount/queryOrgPauseCountSum")
	@ResponseBody
	public AppResult queryOrgPauseCountSum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("storePauseAllotService");
			params.setMethod("queryOrgPauseAllotSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店暂停情况统计总计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店暂停情况统计总计(历史)
	 * @param request
	 * @return
	 */
	@RequestMapping("orgPauseCount/queryOrgPauseHisSum")
	@ResponseBody
	public AppResult queryOrgPauseHisSum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setService("sumStorePauseAllotService");
			params.setMethod("queryOrgPauseAllotSum");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "门店暂停情况统计总计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询操作记录统计总计
	 * @param request
	 * @return
	 */
	@RequestMapping("handleRecordCount/queryHandleRecordSum")
	@ResponseBody
	public AppResult queryHandleRecordSum(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("sumHandleRecordService","queryHandleRecordSum");
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.setOrderBy(request.getParameter("orderBy"));
			params.setOrderValue(request.getParameter("orderValue"));
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询操作记录统计总计出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
