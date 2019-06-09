package org.xxjr.store.web.action.account.user;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.security.MD5Util;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
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
import org.xxjr.sys.util.AreaUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/***
 * 用户信息
 * @author zqh
 *
 */
@Controller()
@RequestMapping("/account/user/info/")
public class UserInfoAction {


	/**
	 * 查询用户菜单
	 * @param request
	 * @return
	 */
	@RequestMapping("queryMenus")
	@ResponseBody
	public AppResult queryMenus(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String userId=StoreUserUtil.getCustomerId(request);
			
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(userId);
			String roleId = "";
			if(custInfo != null){
				roleId =   StringUtil.getString(custInfo.get("authType"));
			}
			//用户菜单
			result.putAttr("menus", StoreUserUtil.getUserMenusTree(roleId));
			//用户审核权限
			result.putAttr("checkMenus", StoreUserUtil.getUserCheckUrls(roleId));
			//用户门店列表
			result.putAttr("userOrgs",OrgUtils.getUserOrgList(userId));
			//门店列表
			result.putAttr("orgList", OrgUtils.getOrgList());
			//城市列表
			result.putAttr("cityList", AreaUtils.getAllCityAndProvince());

		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "queryMenus error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 获取用户信息
	 */
	@RequestMapping("getUserInfo")
	@ResponseBody
	public AppResult getUserInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			// 获取用户的基本信息
			AppParam params = new AppParam();
			params.addAttr("customerId", customerId);
			params.setService("customerExtService");
			params.setMethod("queryStoreCustList");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
			
			if(result.getRows().size() > 0){
				
				Map<String,Object> map = result.getRow(0);

				AppParam queryparams = new AppParam("custLevelService","query");
				queryparams.addAttr("customerId", map.get("customerId"));
				queryparams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult levelResult = RemoteInvoke.getInstance().callNoTx(queryparams);
				if(levelResult.getRows().size() > 0){
					map.put("totalAbility", levelResult.getRow(0).get("totalAbility"));
					map.put("levelType", levelResult.getRow(0).get("levelType"));
					map.put("isRobOrder", levelResult.getRow(0).get("isRobOrder") == null ? '1'
							:levelResult.getRow(0).get("isRobOrder"));
				}else{
					map.put("totalAbility", 0);
					map.put("levelType", 1);
					map.put("isRobOrder", 1);
				}
				
				result.putAttr("userInfo",map);	
			}
			
			//所有统计
			AppParam orderParams = new AppParam();
			StoreUserUtil.dealUserAuthParam(orderParams, customerId, "customerId");
			orderParams.setService("borrowStoreRecordService");
			orderParams.setMethod("queryOrderCount");
			
			orderParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult orderResult = RemoteInvoke.getInstance().callNoTx(orderParams);
			if(orderResult.getRows().size() > 0){
				result.putAttr("orderCount", orderResult.getRow(0));
			}
			
			Map<String, Object> baseConfig = StoreSeparateUtils.getBaseConfig();
			result.putAttr("configCitys", baseConfig.get("allotCitys"));
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "getUserInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 获取今日概况信息
	 * @param request
	 * @return
	 */
	@RequestMapping("queryToDayCase")
	@ResponseBody
	public AppResult getToDayCase(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			// 获取用户的基本信息
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.addAttr("currLoginCustId", customerId);
			List<Map<String, Object>> list = StoreUserUtil.getToDayCase(params);
			if(list.size() > 0){
				result.putAttr("ToDayWork", list.get(0));
				result.putAttr("queryTime", list.get(1).get("queryTime"));
			}
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String authType =   StringUtil.getString(custInfo.get("roleType"));
			double orgBalanceAmt = 0;
			if(CustConstant.CUST_ROLETYPE_6.equals(authType)
					||CustConstant.CUST_ROLETYPE_7.equals(authType)){
				Object orgId  = custInfo.get("orgId");
				if(!StringUtils.isEmpty(orgId)) {
					AppParam workQueryParam = new AppParam("worktimeCfgService","query");
					workQueryParam.addAttr("orgId", orgId);
					workQueryParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					AppResult workResult = RemoteInvoke.getInstance().callNoTx(workQueryParam);
					
					if(workResult.getRows().size() > 0) {
						orgBalanceAmt = NumberUtil.getDouble(workResult.getRow(0).get("balanceAmt"), 0);
					}
				
				}
			
			}
			result.putAttr("orgBalanceAmt",String.format("%.2f", orgBalanceAmt));
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "getToDayCase error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 获取本周工作情况
	 * @param request
	 * @return
	 */
	@RequestMapping("queryThisWeekCase")
	@ResponseBody
	public AppResult getThisWeekCase(HttpServletRequest request){
		AppResult result = new AppResult();
		try {			
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.addAttr("currLoginCustId", customerId);
			List<Map<String, Object>> list = StoreUserUtil.getThisWeekCase(params);
			if(list.size() > 0){
				result.putAttr("YesterDayWork", list.get(0));
				result.putAttr("queryTime", list.get(1).get("queryTime"));
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "getThisWeekCase error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	
	/**
	 * 获取本月工作信息
	 * @param request
	 * @return
	 */
	@RequestMapping("queryToMonthWork")
	@ResponseBody
	public AppResult getToMonthWork(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			AppParam params = new AppParam();
			params.addAttr("recordDate", DateUtil.toStringByParttern(new Date(), "yyyy-MM"));
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			params.addAttr("currLoginCustId", customerId);
			List<Map<String, Object>> list = StoreUserUtil.getToMonthCase(params);
			if(list.size() > 0){
				result.putAttr("ToMonthWork", list.get(0));
				result.putAttr("queryTime", list.get(1).get("queryTime"));
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryToMonthWork error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询系统通知信息
	 * @param request
	 * @return
	 */
	@RequestMapping("querySysNotify")
	@ResponseBody
	public AppResult querySysNotify(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			params.addAttr("customerIds", customerId);
			RequestUtil.setAttr(params, request);
			params.addAttr("status", "1"); //查询有效状态的通知
			params.addAttr("messNotifyType", "1"); // 1系统通知消息
			params.setOrderBy("notifyDate,createTime");
			params.setEveryPage(2);
			params.setOrderValue("desc,desc");
			int currentPage = NumberUtil.getInt(params.getCurrentPage(),1);
			if(currentPage == 1){
				result = StoreUserUtil.getSysNotifyList(params);
				int size = result.getRows().size();
				if(size > 0){
					int lastIndex = size - 1;
					int totalRecords = NumberUtil.getInt(result.getRow(lastIndex).get("totalRecords"),0);
					int totalPages = NumberUtil.getInt(result.getRow(lastIndex).get("totalPages"),0);
					result.getRows().remove(lastIndex);
					result.getPage().setTotalRecords(totalRecords);
					result.getPage().setTotalPage(totalPages);
				}
			}else{
				params.setService("sysNotifyService");
				params.setMethod("queryShow");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "querySysNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询系统反馈信息
	 * @param request
	 * @return
	 */
	@RequestMapping("querySysFeedback")
	@ResponseBody
	public AppResult querySysFeedback(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户ID不存在");
				return result;
			}
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			int currentPage = NumberUtil.getInt(params.getCurrentPage(),1);
			params.addAttr("status", 1); // 1未处理
			params.setOrderBy("t.feedDate");
			params.setEveryPage(2);
			params.setOrderValue("desc");
			if(currentPage == 1){
				result = StoreUserUtil.getFeedBackList(params);
				int size = result.getRows().size();
				if(size > 0){
					int lastIndex = size - 1;
					int totalRecords = NumberUtil.getInt(result.getRow(lastIndex).get("totalRecords"),0);
					int totalPages = NumberUtil.getInt(result.getRow(lastIndex).get("totalPages"),0);
					result.getRows().remove(lastIndex);
					result.getPage().setTotalRecords(totalRecords);
					result.getPage().setTotalPage(totalPages);
				}
			}else{
				params.setService("sysFeedbackService");
				params.setMethod("queryShow");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "querySysFeedback error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 新增系统反馈
	 * @param request
	 * @return
	 */
	@RequestMapping("addSysFeedBack")
	@ResponseBody
	public AppResult addSysFeedBack(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户ID不存在");
				return result;
			}
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("customerId", customerId);
			params.addAttr("feedDate", new Date());
			params.setService("sysFeedbackService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_FEED_BACK_KEY);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "addSysFeedBack error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询滚动系统通知信息
	 * @param request
	 * @return
	 */
	@RequestMapping("queryScrollSysNotify")
	@ResponseBody
	public AppResult queryScrollSysNotify(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			params.addAttr("customerIds", customerId);
			RequestUtil.setAttr(params, request);
			List<Map<String, Object>> list = StoreUserUtil.getScrollNotifyList(params);
			if(list.size() > 0){
				result.addRows(list);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryScrollSysNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 处理反馈
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("dealFeedBack")
	@ResponseBody
	public AppResult dealFeedBack(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户ID不存在");
				return result;
			}
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				throw new SysException("反馈的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				throw new SysException("请传入反馈的基本信息");
			}
			AppParam params = new AppParam();
			params.addAttr("status", 2); //2 已处理
			for(Map<String, Object> map : orders){
				params.setService("sysFeedbackService");
				params.addAttr("feedId", map.get("feedId"));
				params.setMethod("update");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_FEED_BACK_KEY);
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "dealFeedBack error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 重置密码
	 * @param request
	 * @return
	 */
	@RequestMapping("resetPwd")
	@ResponseBody
	public AppResult resetPwd(HttpServletRequest request){
		AppResult result = new AppResult();
		String password = request.getParameter("password");
		if(StringUtils.isEmpty(password)){
			result.setSuccess(false);
			result.setMessage("缺少参数");
			return result;
		}
		if (password.length() < 6 || password.length() > 12) {
			return CustomerUtil.retErrorMsg("密码长度需要在6~12之间");
		}
		if (!ValidUtils.checkPwd(password)) {
			return CustomerUtil.retErrorMsg("密码需要包含字符和数字喔~");
		}
		String loginCustId = StoreUserUtil.getCustomerId(request);//登陆用户ID
		try {
			// update 已经被修改了
			AppParam params = new AppParam("customerService", "newUpdate");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			params.addAttr("customerId", loginCustId);
			params.addAttr("password",  MD5Util.getEncryptPassword(password));
			result = RemoteInvoke.getInstance().call(params);
		} catch(Exception e){
			LogerUtil.error(this.getClass(), e, "resetPwd error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
