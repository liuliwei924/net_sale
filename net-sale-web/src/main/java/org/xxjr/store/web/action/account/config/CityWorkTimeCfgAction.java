package org.xxjr.store.web.action.account.config;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ThreadLogUtil;

/**
 * 分单城市工作时间配置
 * @author zenghw
 *
 */
@Controller()
@RequestMapping("/account/config/workTimeConf/")
public class CityWorkTimeCfgAction {
	/**
	 * 查询门店工作配置
	 * @param request
	 * @return
	 */
	@RequestMapping("queryWorkTime")
	@ResponseBody
	public AppResult queryWorkTime(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("worktimeCfgService");
			params.setMethod("queryWorkTime");
			params.setEveryPage(20);
			params.setOrderBy("createTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryWorkTime error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 添加门店工作配置
	 * @param request
	 * @return
	 */
	@RequestMapping("addWorkTime")
	@ResponseBody
	public AppResult addWorkTime(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("worktimeCfgService");
			params.setMethod("insertWorkTime");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreSeparateUtils.STORE_WORK_CONFIG_KEY);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "addWorkTime error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 删除门店工作配置
	 * @param request
	 * @return
	 */
	@RequestMapping("deleteWorkTime")
	@ResponseBody
	public AppResult deleteWorkTime(HttpServletRequest request){
		AppResult result = new AppResult();
		String recordId = request.getParameter("recordId");
		if (StringUtils.isEmpty(recordId)) {
			result.setSuccess(false);
			result.setMessage("记录ID不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("worktimeCfgService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreSeparateUtils.STORE_WORK_CONFIG_KEY);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "deleteWorkTime error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 更新门店工作配置
	 * @param request
	 * @return
	 */
	@RequestMapping("updateWorkTime")
	@ResponseBody
	public AppResult updateWorkTime(HttpServletRequest request){
		AppResult result = new AppResult();
		String recordId = request.getParameter("recordId");
		if (StringUtils.isEmpty(recordId)) {
			result.setSuccess(false);
			result.setMessage("记录ID不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				params.addAttr("customerId", 0);
			}
			params.setService("worktimeCfgService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreSeparateUtils.STORE_WORK_CONFIG_KEY);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateWorkTime error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 门店成本维护
	 * @param request
	 * @return
	 */
	@RequestMapping("checkOrgOrderCost")
	@ResponseBody
	public AppResult checkOrgOrderCost(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String orgId = request.getParameter("orgId");
			if(StringUtils.isEmpty(orgId)){
				return CustomerUtil.retErrorMsg("门店不能为空");
			}
			int count = NumberUtil.getInt(RedisUtils.getRedisService().get(
					StoreSeparateUtils.STORE_ORG_COSTDEAL_COUNT_KEY_ + orgId), 0);
			if(count >= 1){
				return CustomerUtil.retErrorMsg("这个门店已经维护过成本，请勿再次操作！");
			}
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 7);
			//月初第七天
	        String senvenDate = DateUtil.toStringByParttern(calendar.getTime(),DateUtil.DATE_PATTERN_YYYY_MM_DD);
	        String nowDate = DateUtil.getSimpleFmt(new Date());
	        int compareNum = nowDate.compareTo(senvenDate);
	        if(compareNum > 0){
	        	return CustomerUtil.retErrorMsg("当前时间不能执行门店成本维护,成本维护时间为月初前七天");
	        }
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			//上个月第一天
			Date firstDate = DateUtil.getLastMonthFirstSecond(new Date());
			String lastMonFirstDay = DateUtil.toStringByParttern(firstDate,DateUtil.DATE_PATTERN_YYYY_MM_DD);
			//上个月最后一天
			String lastMonEndDay = DateUtil.toStringByParttern(DateUtil.
					getLastMonthEndSecond(new Date()), DateUtil.DATE_PATTERN_YYYY_MM_DD);
			String recordMonth = DateUtil.toStringByParttern(firstDate, DateUtil.DATE_PATTERN_YYYY_MM);
			params.addAttr("recordDate", lastMonFirstDay);
			params.addAttr("endRecordDate", lastMonEndDay);
			params.addAttr("recordMonth", recordMonth);
			params.setService("storeCostRecordService");
			params.setMethod("storeOrgOrderCostDeal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.getRows().size() > 0){
				AppParam sumParams = new AppParam("sumStoreBaseMonthService","updateStoreCost");
				sumParams.addAttr("costList", result.getRows());
				sumParams.addAttr("orgId", orgId);
				sumParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				ThreadLogUtil.sendMessageNewThread(sumParams);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "checkOrgOrderCost error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
