package org.xxjr.store.web.action.account.config;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.security.MD5Util;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busi.util.store.ApplyInfoUtil;
import org.xxjr.busi.util.store.BusiCustUtil;
import org.xxjr.busi.util.store.StoreRoleUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/***
 * 系统操作员相关
 * @author zqh
 *
 */
@Controller
@RequestMapping("/account/config/operator/")
public class StoreOperatorAction {

	/**
	 * 分页获取用户列表
	 */
	@RequestMapping("queryUserList")
	@ResponseBody
	public AppResult getUserList(HttpServletRequest request) {
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			String userName = request.getParameter("userName");
			if(ValidUtils.validateTelephone(userName)){//加快查询效率
				params.addAttr("telephone", params.removeAttr("userName"));
			}
			params.addAttr("roleTypeIn", "1,2,3,6,7,8,9,10");//只查询管理员及客服、门店人员、外部渠道人员
			params.setService("customerExtService");
			params.setMethod("queryStoreCustList");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);

			//遍历用户等级信息
			if(result.getRows().size() > 0){
				for(int i=0;i<result.getRows().size();i++){
					Map<String,Object> map = result.getRow(i);

					AppParam queryparams = new AppParam("custLevelService","query");
					queryparams.addAttr("customerId", map.get("customerId"));
					queryparams.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult levelResult = RemoteInvoke.getInstance().callNoTx(queryparams);
					if(levelResult.getRows().size() > 0){
						map.put("totalAbility", levelResult.getRow(0).get("totalAbility"));
						map.put("gradeCode", levelResult.getRow(0).get("gradeCode"));
						map.put("isAllotOrder", levelResult.getRow(0).get("isAllotOrder") == null ? '0'
								:levelResult.getRow(0).get("isAllotOrder"));
					}else{
						map.put("totalAbility", 0);
						map.put("gradeCode", 1);
						map.put("isAllotOrder", 0);//不可分
					}
				}
			}

		} catch (Exception e) {
			LogerUtil.error(StoreOperatorAction.class, e, "queryUserList error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}

	
	/**
	 * 修改用户信息门店经理
	 */
	@RequestMapping("updateInfo")
	@ResponseBody
	public AppResult updateInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		String customerId = request.getParameter("customerId");
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		//用户角色
		String roleType = request.getParameter("roleType");
		if (StringUtils.isEmpty(roleType)) {
			result.setSuccess(false);
			result.setMessage("用户角色不能为空");
			return result;
		}else{
			int role = NumberUtil.getInt(roleType);
			if(3 == role || 9 == role){
				String groupName = request.getParameter("groupName");
				if (StringUtils.isEmpty(groupName)) {
					result.setSuccess(false);
					result.setMessage("组名不能为空");
					return result;
				}
				String teamName = request.getParameter("teamName");
				if (StringUtils.isEmpty(teamName)) {
					result.setSuccess(false);
					result.setMessage("队名不能为空");
					return result;
				}
			}else if(8 == role){
				String groupName = request.getParameter("groupName");
				if (StringUtils.isEmpty(groupName)) {
					result.setSuccess(false);
					result.setMessage("组名不能为空");
					return result;
				}
			}
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("isAdmin", "isAdmin");
			params.addAttr("authRole", params.removeAttr("roleId"));
			params.setService("customerService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				//更新用户能力值和等级
				AppParam updateParams = new AppParam();
				updateParams.setService("custLevelService");
				updateParams.setMethod("update");
				updateParams.addAttr("customerId", params.getAttr("customerId"));
				updateParams.addAttr("gradeCode", params.getAttr("gradeCode"));
				updateParams.addAttr("totalAbility", params.getAttr("totalAbility"));
				updateParams.addAttr("isAllotOrder", params.getAttr("isAllotOrder"));
				updateParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(updateParams);
				//清除用户缓存
				CustomerIdentify.refreshIdentifyById(customerId);
				//刷新其他地方信息
				BusiCustUtil.setBusiCustIn(CustomerIdentify.getCustIdentify(customerId), "");

				//刷新其他地方信息
				BusiCustUtil.setBusiCustSum(CustomerIdentify.getCustIdentify(customerId), "");
			}
		} catch (Exception e) {
			LogerUtil.error(StoreOperatorAction.class, e, "updateInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 修改用户信息（管理员）
	 */
	@RequestMapping("checkUpdateInfo")
	@ResponseBody
	public AppResult checkUpdateInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		String customerId = request.getParameter("customerId");
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		//用户角色
		String roleType = request.getParameter("roleType");
		if (StringUtils.isEmpty(roleType)) {
			result.setSuccess(false);
			result.setMessage("用户角色不能为空");
			return result;
		}else{
			int role = NumberUtil.getInt(roleType);
			if(3 == role || 9 == role){
				String groupName = request.getParameter("groupName");
				if (StringUtils.isEmpty(groupName)) {
					result.setSuccess(false);
					result.setMessage("组名不能为空");
					return result;
				}
				String teamName = request.getParameter("teamName");
				if (StringUtils.isEmpty(teamName)) {
					result.setSuccess(false);
					result.setMessage("队名不能为空");
					return result;
				}
			}else if(8 == role){
				String groupName = request.getParameter("groupName");
				if (StringUtils.isEmpty(groupName)) {
					result.setSuccess(false);
					result.setMessage("组名不能为空");
					return result;
				}
			}
		}
		try {
			String telephone = request.getParameter("telephone");
			if (StringUtils.isEmpty(telephone)) {
				result.setSuccess(false);
				result.setMessage("手机号码不能为空");
				return result;
			}else{
				AppParam queryParams = new AppParam("customerService","query");
				queryParams.addAttr("excludeSelf", customerId);
				queryParams.addAttr("telephone", telephone);
				queryParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_cust));
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParams);
				if(queryResult.getRows().size() > 0){
					result.setSuccess(false);
					result.setMessage("该手机号码已被人使用！");
					return result;
				}
			}
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("isAdmin", "isAdmin");
			params.addAttr("authRole", params.removeAttr("roleId"));
			params.setService("customerService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				//更新用户能力值和等级
				AppParam updateParams = new AppParam();
				updateParams.setService("custLevelService");
				updateParams.setMethod("update");
				updateParams.addAttr("customerId", params.getAttr("customerId"));
				updateParams.addAttr("gradeCode", params.getAttr("gradeCode"));
				updateParams.addAttr("totalAbility", params.getAttr("totalAbility"));
				updateParams.addAttr("isAllotOrder", params.getAttr("isAllotOrder"));
				updateParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(updateParams);
				//清除用户缓存
				CustomerIdentify.refreshIdentifyById(customerId);
				//刷新其他地方信息
				BusiCustUtil.setBusiCustIn(CustomerIdentify.getCustIdentify(customerId), "");
				BusiCustUtil.setBusiCustSum(CustomerIdentify.getCustIdentify(customerId), "");
			}
		} catch (Exception e) {
			LogerUtil.error(StoreOperatorAction.class, e, "checkUpdateInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 变更微信号码
	 * @param request
	 * @return
	 */
	@RequestMapping("updateWexinInfo")
	@ResponseBody
	public AppResult updateWexinInfo(HttpServletRequest request){
		AppResult result = new AppResult();

		String customerId = request.getParameter("customerId");
		String telephone = request.getParameter("telephone");
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		if(StringUtils.isEmpty(telephone)){
			result.setSuccess(false);
			result.setMessage("telephone不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			params.addAttr("customerId", customerId);
			params.addAttr("telephone", telephone);
			params.setService("customerExtService");
			params.setMethod("updateWexinInfo");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);

			if(result.isSuccess()){
				//清除用户缓存
				CustomerIdentify.refreshIdentifyById(customerId);			
				//刷新其他地方信息
				BusiCustUtil.setBusiCustIn(CustomerIdentify.getCustIdentify(customerId), "");
				BusiCustUtil.setBusiCustSum(CustomerIdentify.getCustIdentify(customerId), "");
			}

		}catch (Exception e) {
			LogerUtil.error(StoreOperatorAction.class, e, "updateWexinInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 获取用户信息
	 */
	@RequestMapping("queryUserInfo")
	@ResponseBody
	public AppResult queryUserInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			String customerId = request.getParameter("customerId");
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("customerId不能为空");
				return result;
			}
			params.addAttr("customerId", customerId);
			params.setService("customerExtService");
			params.setMethod("queryStoreCustList");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);

		} catch (Exception e) {
			LogerUtil.error(StoreOperatorAction.class, e, "queryUserInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}


	/***
	 * 查询角色
	 * @param request
	 * @return
	 */
	@RequestMapping("queryRoleList")
	@ResponseBody
	public AppResult queryRoleList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			//获取所有角色
			result.addRows(StoreRoleUtils.getAllRoles());
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class, e, "queryRoleList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}	
		return result;
	}
	
	
	
	/**
	 * 分页获取分单信息列表
	 */
	@RequestMapping("queryAllotOrderList")
	@ResponseBody
	public AppResult queryAllotOrderList(HttpServletRequest request) {
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			String userName = request.getParameter("realName");
			if(ValidUtils.validateTelephone(userName)){//加快查询效率
				params.addAttr("telephone", params.removeAttr("realName"));
			}
			params.setService("custLevelService");
			params.setMethod("queryAllotOrderList");
			String orderBy = request.getParameter("orderBy");
			String orderValue = request.getParameter("orderValue");
			params.setOrderBy(orderBy);
			params.setOrderValue(orderValue);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(StoreOperatorAction.class, e, "queryAllotOrderList error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 查询等级配置
	 * @param request
	 * @return
	 */
	@RequestMapping("queryGradeList")
	@ResponseBody
	public AppResult queryGradeList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			//获取所有等级配置信息
			result.addRows(StoreSeparateUtils.getRankConfig());
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class, e, "queryGradeList error");
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
		String curCustomerId = request.getParameter("curCustomerId");
		String password = request.getParameter("password");
		if(StringUtils.isEmpty(curCustomerId) || StringUtils.isEmpty(password)){
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
		if (!ApplyInfoUtil.isAdminAuth(loginCustId)) {
			return CustomerUtil.retErrorMsg("您没有此权限");
		}
		try {
			AppParam params = new AppParam("customerService", "update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			RequestUtil.setAttr(params, request);
			params.addAttr("customerId", curCustomerId);
			params.addAttr("password",  MD5Util.getEncryptPassword(password));
			result = RemoteInvoke.getInstance().call(params);
		} catch(Exception e){
			LogerUtil.error(StoreRoleAction.class, e, "resetPwd error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
