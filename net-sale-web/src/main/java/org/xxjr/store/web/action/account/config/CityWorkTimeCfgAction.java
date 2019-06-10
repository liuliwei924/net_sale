package org.xxjr.store.web.action.account.config;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

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
	 * 更新门店工作配置
	 * @param request
	 * @return
	 */
	@RequestMapping("orgCharge")
	@ResponseBody
	public AppResult orgCharge(HttpServletRequest request){
		AppResult result = new AppResult();
		String orgId = request.getParameter("orgId");
		double amount = NumberUtil.getDouble(request.getParameter("amount"),0);
		if (StringUtils.isEmpty(orgId)) {
			result.setSuccess(false);
			result.setMessage("门店ID不能为空");
			return result;
		}
		
		if (amount <= 0) {
			result.setSuccess(false);
			result.setMessage("充值金额必须大于0");
			return result;
		}
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null && custInfo.size() > 0){
				String authType = StringUtil.getString(custInfo.get("roleType")); //角色
				//管理员才有审核退单权限
				if(!CustConstant.CUST_ROLETYPE_1.equals(authType)){
					return CustomerUtil.retErrorMsg("此操作你没有相应的权限");
				}
			}else {
				return CustomerUtil.retErrorMsg("你登录信息已经失效，请重新登录");
			}
			
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
		
			params.addAttr("optBy", customerId);
			params.addAttr("optName", custInfo.get("userName"));
			params.setService("worktimeCfgService");
			params.setMethod("orgCharge");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "orgCharge error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
