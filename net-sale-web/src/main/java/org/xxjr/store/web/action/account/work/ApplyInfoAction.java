package org.xxjr.store.web.action.account.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.ApplyInfoUtil;
import org.xxjr.busi.util.store.IdCardResolveUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.IDCardValidate;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/**
 * 录单
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/work/applyInfo/")
public class ApplyInfoAction {

	/**
	 * 保存录单信息
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("saveAllInfo")
	@ResponseBody
	public AppResult saveAllInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String applyId = request.getParameter("applyId");
			
			Map<String, Object> mainInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("mainInfo"), Map.class);
			AppParam applyParam = new AppParam();
			if(!StringUtils.isEmpty(mainInfo)){
				String applyName = StringUtil.getString(mainInfo.get("applyName"));
				if (StringUtils.isEmpty(applyName)) {
					return CustomerUtil.retErrorMsg("姓名不能为空");
				}
				String telephone = StringUtil.getString(mainInfo.get("telephone"));
				if (StringUtils.isEmpty(telephone)) {
					return CustomerUtil.retErrorMsg("手机号码不能为空");
				}
				
				String loanAmount = StringUtil.getString(mainInfo.get("loanAmount"));
				if (StringUtils.isEmpty(loanAmount)) {
					return CustomerUtil.retErrorMsg("贷款金额不能为空");
				}
				double loanDoubleAmount = NumberUtil.getDouble(loanAmount, -1);
				if (loanDoubleAmount >= 1000 || loanDoubleAmount <= 0) {
					return CustomerUtil.retErrorMsg("贷款额度非法，请注意贷款额度单位为万元！");
				}
				
				if (StringUtils.isEmpty(applyId)) {
					// 验证手机号码
					Boolean validResult = ValidUtils.validateTelephone(telephone);
					if (!validResult) {
						return CustomerUtil.retErrorMsg("请输入正确的手机号码");
					}
				}else{
					applyParam.addAttr("applyId",applyId);
				}
				applyParam.addAttr("mainInfo",mainInfo);
			}else{
				return CustomerUtil.retErrorMsg("主要信息不能为空");
			}
			Map<String, Object> baseInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("baseInfo"), Map.class);
			if(!StringUtils.isEmpty(baseInfo)){
				String identifyNo = StringUtil.getString(baseInfo.get("identifyNo"));
				if (!StringUtils.isEmpty(identifyNo)) {
					// 验证身份证号码
					String validCardResult = IDCardValidate.validCardNo(identifyNo);
					if (!StringUtils.isEmpty(validCardResult)) {
						return CustomerUtil.retErrorMsg(validCardResult);
					}
					int authAge = IdCardResolveUtil.getAge(identifyNo); 
					if(authAge != -1){
						baseInfo.put("age", authAge);
					}
					int authSex = IdCardResolveUtil.getSex(identifyNo);
					if(authSex != -1){
						baseInfo.put("sex", authSex == 2 ? 0 : authSex);
					}
				}
				applyParam.addAttr("baseInfo",baseInfo);
			}else{
				return CustomerUtil.retErrorMsg("基本信息不能为空");
			}
			Map<String, Object> otherInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("otherInfo"), Map.class);
			if(!StringUtils.isEmpty(otherInfo)){
				applyParam.addAttr("otherInfo",otherInfo);
			}else{
				return CustomerUtil.retErrorMsg("其他信息不能为空");
			}
			
			applyParam.addAttr("customerId", customerId);
			applyParam.addAttr("lastStore", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("saveAllInfo");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "saveAllInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 保存主要信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("saveMainInfo")
	@ResponseBody
	public AppResult saveMainInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				throw new SysException("用户ID不能为空");
			}
			String applyName = request.getParameter("applyName");
			if (StringUtils.isEmpty(applyName)) {
				throw new SysException("姓名不能为空");
			}
			String telephone = request.getParameter("telephone");
			if (StringUtils.isEmpty(telephone)) {
				throw new SysException("手机号码不能为空");
			}
			
			String loanAmount = request.getParameter("loanAmount");
			if (StringUtils.isEmpty(loanAmount)) {
				throw new SysException("贷款金额不能为空");
			}
			
			double loanDoubleAmount = NumberUtil.getDouble(loanAmount, -1);
			if (loanDoubleAmount >= 1000 || loanDoubleAmount <= 0) {
				return CustomerUtil.retErrorMsg("贷款额度非法，请注意贷款额度单位为万元！");
			}
			AppParam applyParam = new AppParam();
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				// 验证手机号码
				Boolean validResult = ValidUtils.validateTelephone(telephone);
				if (!validResult) {
					return CustomerUtil.retErrorMsg("请输入正确的手机号码");
				}
			}else{
				applyParam.addAttr("applyId",applyId);
			}
			
			RequestUtil.setAttr(applyParam, request);
			applyParam.addAttr("customerId", customerId);
			applyParam.addAttr("lastStore", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("saveMainInfo");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "saveMainInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 保存基本信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("saveBaseAllInfo")
	@ResponseBody
	public AppResult saveBaseAllInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				throw new SysException("申请ID不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("saveBaseAllInfo");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "saveBaseAllInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 保存其它基本信息(包括车产、房产、保险、征信)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("saveOtherInfo")
	@ResponseBody
	public AppResult saveOtherInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				throw new SysException("申请ID不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("saveOtherInfo");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "saveOtherInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 查询主要信息和基本信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryMainBaseInfo")
	@ResponseBody
	public AppResult queryMainBaseInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请Id不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			
			// 查询用户信息
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			
			//查询主要信息
			Map<String, Object> mainInfo = StoreApplyUtils.getApplyMainInfo(applyId);
			
//			String hideFlag = request.getParameter("hideFlag"); // 退单列表详情手机号隐藏标识

			// 查询申请信息
			Map<String, Object> applyMap = StoreApplyUtils.getStoreApplyInfo(applyId);
			
			String custId = StringUtil.getString(applyMap.get("lastStore"));
//			if (custInfo != null && mainInfo != null) {详情页直接显示号码
//				String authType = StringUtil.getString(custInfo.get("roleType"));
				// 管理员默认隐藏手机号码 以及管理员退单列表设置可显示的手机号码
//				if(CustConstant.CUST_ROLETYPE_1.equals(authType)){
//					mainInfo.put("telephone", StringUtil.getHideTelphone(
//							StringUtil.getString(mainInfo.get("telephone"))));
//				}
//				if(CustConstant.CUST_ROLETYPE_1.equals(authType)  && StringUtils.isEmpty(hideFlag) ){
//					mainInfo.remove("noHideMobile");
//				}else if(!CustConstant.CUST_ROLETYPE_1.equals(authType) && !StringUtils.isEmpty(hideFlag)){
//					mainInfo.remove("noHideMobile");
//				}
//			}

			// 设置主要信息
			result.putAttr("mainInfo", mainInfo);
			// 设置基本信息
			result.putAttr("baseInfo",StoreApplyUtils.getApplyBaseInfo(applyId));
			
			// 设置其它信息
			result.putAttr("orderStatus", applyMap.get("orderStatus"));
			result.putAttr("customerId", custId);
			result.putAttr("orderType",  applyMap.get("orderType"));
			result.putAttr("backStatus", applyMap.get("backStatus"));
			result.putAttr("backDesc", applyMap.get("backDesc"));
			
			// 设置用户信息
			result.putAttr("custInfo", custInfo);
			// 专属单
			Map<String,Object> execMap = StoreApplyUtils.getApplyExecOrder(applyId);
			result.putAttr("execOders", StringUtils.isEmpty(execMap.get("applyId")) == true ? 0 : 1);
			return result;
		} catch (Exception e) {
			LogerUtil
					.error(ApplyInfoAction.class, e, "queryMainBaseInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询下一笔订单
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryNextOrder")
	@ResponseBody
	public AppResult queryNextOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请Id不能为空");
				return result;
			}
			
			String orderStatus = request.getParameter("orderStatus");
			if (StringUtils.isEmpty(orderStatus)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("处理状态不能为空");
				return result;
			}
			AppParam param = new AppParam();
			RequestUtil.setAttr(param, request);
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			AppParam queryParam = new AppParam("borrowStoreApplyService", "query");
			queryParam.addAttr("applyId", applyId);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult handleResult =  RemoteInvoke.getInstance().callNoTx(queryParam);
			if(handleResult.getRows().size() > 0){
				param.addAttr("lastUpdateTime", StringUtil.getString(handleResult.getRow(0).get("lastUpdateTime")));
			}
			if(!StringUtils.isEmpty(param.getAttr("lastUpdateTime"))){
				param.setOrderBy("lastUpdateTime");
				param.setOrderValue("desc");
				param.removeAttr("applyId");
			}else{
				param.setOrderBy("applyId");
				param.setOrderValue("desc");
			}
			param.setService("storeHandleExtService");
			param.setMethod("queryNextOrder");
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			if(result.isSuccess() && result.getRows().size() == 0){
				result.setSuccess(Boolean.FALSE);
				result.setMessage("抱歉，您暂时没有下一笔订单");
				return result;
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryNextOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询跟进记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryFollowRecord")
	@ResponseBody
	public AppResult queryFollowRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			String dateFlag = request.getParameter("dateFlag"); // 日期标记
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//跟进记录
			AppParam storeParam = new AppParam("borrowStoreRecordService","queryStoreRecord");
			RequestUtil.setAttr(storeParam, request);
			
			//设置三个月内的表
			Date curDate = new Date(); 
			if(!StringUtils.isEmpty(dateFlag)){
				curDate = DateUtil.minu(
						DateUtil.toLocalDateTimeByDate(curDate), NumberUtil.getInt(dateFlag,0), DateUtil.ChronoUnit_MONTHS);
			}
			storeParam.addAttr("curDate", DateUtil.getSimpleFmt(curDate));
			storeParam.addAttr("lastMonth", DateUtil.getSimpleFmt(
					DateUtil.minu(DateUtil.toLocalDateTimeByDate(curDate), 1, DateUtil.ChronoUnit_MONTHS)));
			storeParam.addAttr("lastTwoMonth", DateUtil.getSimpleFmt(
					DateUtil.minu(DateUtil.toLocalDateTimeByDate(curDate), 2, DateUtil.ChronoUnit_MONTHS)));
			
			
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(storeParam, applyId);
			storeParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if(custInfo != null){
				orgId = StringUtil.getString(custInfo.get("orgId"));//门店
			}
			if(!ApplyInfoUtil.isAdminAuth(customerId)){
				storeParam.addAttr("orgId", orgId);
				storeParam.addAttr("isAdmin", false);
			}else{
				storeParam.addAttr("isAdmin", true);
			}
			storeParam.addAttr("applyId", applyId);
			storeParam.setOrderBy("t.createTime");
			storeParam.setOrderValue("desc");
			
			int currentPage = NumberUtil.getInt(storeParam.getCurrentPage(),1);
			if(1 == currentPage){
				result = StoreApplyUtils.getStoreFollowRecord(storeParam);
			}else{
				result = RemoteInvoke.getInstance().callNoTx(storeParam);
			}
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryFollowRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询预约信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBookInfo")
	@ResponseBody
	public AppResult queryBookInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			AppParam applyParam = new AppParam("borrowStoreApplyService", "query");
			applyParam.addAttr("applyId", applyId);
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(applyParam);
			String custId = StringUtil.getString(result.getRow(0).get("lastStore"));
			//预约信息
			AppParam queryParam = new AppParam("treatBookService", "query");
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(queryParam, applyId);
			queryParam.addAttr("customerId", custId);
			queryParam.addAttr("applyId", applyId);
			queryParam.setOrderBy("createTime");
			queryParam.setOrderValue("desc");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryBookInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询签单信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("querySignInfo")
	@ResponseBody
	public AppResult querySignInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,null);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//签单信息
			AppParam signParam = new AppParam();
			signParam.addAttr("applyId", applyId);
			signParam.setOrderBy("createTime");
			signParam.setOrderValue("desc");
			List<Map<String,Object>> recordList = StoreApplyUtils.getApplyTreatSignRecord(signParam);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			Map<String,Object> applyMap = StoreApplyUtils.getApplyInfo(applyId);
			String custId = StringUtil.getString(applyMap.get("lastStore"));
			Map<String, Object> resultMap = null;
			if (custInfo != null && recordList.size() > 0) {
				String authType = StringUtil.getString(custInfo.get("roleType"));
				// 门店主管和门店副主管 隐藏订单的手机号码和身份证信息（本人除外）
				if((CustConstant.CUST_ROLETYPE_8.equals(authType) 
						|| CustConstant.CUST_ROLETYPE_9.equals(authType)) && (!custId.equals(customerId))){
					resultMap = recordList.get(0);
					resultMap.put("telephone", StringUtil.getHideTelphone(StringUtil.getString(resultMap.get("telephone"))));
					resultMap.put("cardNo", StringUtil.getHideIdentify(StringUtil.getString(resultMap.get("cardNo"))));
					result.addRow(resultMap);
				}else{
					result.addRow(recordList.get(0));
				}
			}
			
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "querySignInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询预约记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBookRecord")
	@ResponseBody
	public AppResult queryBookRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//预约信息
			AppParam queryParam = new AppParam();
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(queryParam, applyId);
			queryParam.addAttr("applyId", applyId);
			queryParam.setCurrentPage(1);
			queryParam.setEveryPage(10);
			queryParam.setOrderBy("createTime");
			queryParam.setOrderValue("desc");
		
			List<Map<String,Object>> recordList = StoreApplyUtils.getApplyBookRecord(queryParam);
			result.addRows(recordList);
			
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryBookRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 查询上门记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryVisitRecord")
	@ResponseBody
	public AppResult queryVisitRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//上门信息
			AppParam queryParam = new AppParam();
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(queryParam, applyId);
			queryParam.addAttr("applyId", applyId);
			queryParam.setCurrentPage(1);
			queryParam.setEveryPage(10);
			queryParam.setOrderBy("createTime");
			queryParam.setOrderValue("desc");
		
			List<Map<String,Object>> recordList = StoreApplyUtils.getApplyVisitRecord(queryParam);
			result.addRows(recordList);

		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryVisitRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 查询签单记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("querySignRecord")
	@ResponseBody
	public AppResult querySignRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//签单信息
			AppParam signParam = new AppParam("treatInfoHistoryService", "query");
			signParam.addAttr("applyId", applyId);
			signParam.setCurrentPage(1);
			signParam.setEveryPage(10);
			signParam.setOrderBy("createTime");
			signParam.setOrderValue("desc");
			
			List<Map<String,Object>> recordList = StoreApplyUtils.getApplySignRecord(signParam);
			
			// 获取用户信息
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			
			Map<String,Object> applyMap = new HashMap<>();
			
			// 获取订单信息
			if("1".equals(searchDetailFlag)){
				applyMap = StoreApplyUtils.getApplyInfo(applyId);
			}else{
				applyMap = StoreApplyUtils.getStoreApplyInfo(applyId);
			}
			String custId = StringUtil.getString(applyMap.get("lastStore"));
			if (custInfo != null && recordList.size() >0) {
				String authType = StringUtil.getString(custInfo.get("roleType"));
				// 门店主管和门店副主管 隐藏订单的手机号码和身份证信息（本人除外）
				if((CustConstant.CUST_ROLETYPE_8.equals(authType) 
						|| CustConstant.CUST_ROLETYPE_9.equals(authType)) && (!custId.equals(customerId))){
					for(Map<String, Object> resultMap : recordList){
						resultMap.put("telephone", StringUtil.getHideTelphone(StringUtil.getString(resultMap.get("telephone"))));
						resultMap.put("cardNo", StringUtil.getHideIdentify(StringUtil.getString(resultMap.get("cardNo"))));
						result.addRow(resultMap);
					}
				}else{
					result.addRows(recordList);
				}
			}

		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "querySignRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询回款记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryRetRecord")
	@ResponseBody
	public AppResult queryRetRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//回款信息
			AppParam backParam = new AppParam();
			backParam.addAttr("applyId", applyId);
			backParam.setCurrentPage(1);
			backParam.setEveryPage(10);
			backParam.setOrderBy("createTime");
			backParam.setOrderValue("desc");
			
			List<Map<String,Object>> retList = StoreApplyUtils.getApplyRetRecord(backParam);
			result.setRows(retList);

		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryRetRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询进件记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryLoanRecord")
	@ResponseBody
	public AppResult queryLoanRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//进件信息
			AppParam backParam = new AppParam();
			backParam.addAttr("applyId", applyId);
			backParam.setCurrentPage(1);
			backParam.setEveryPage(10);
			backParam.setOrderBy("createTime");
			backParam.setOrderValue("desc");
		
			List<Map<String,Object>> retList = StoreApplyUtils.getApplyContractRecord(backParam);
			result.setRows(retList);

		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryLoanRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 查询投诉记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryComplaintRecord")
	@ResponseBody
	public AppResult queryComplaintRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//查询投诉记录
			AppParam queryParam = new AppParam();
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(queryParam, applyId);
			queryParam.addAttr("applyId", applyId);
			queryParam.setCurrentPage(1);
			queryParam.setEveryPage(10);
			queryParam.setOrderBy("createTime");
			queryParam.setOrderValue("desc");
			
			List<Map<String,Object>> retList = StoreApplyUtils.getComplaintRecord(queryParam);
			result.setRows(retList);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryComplaintRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 查询通话记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryCallRecord")
	@ResponseBody
	public AppResult queryCallRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//查询通话记录
			AppParam queryParam = new AppParam("storeCallRecordService", "queryCallRecord");
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(queryParam, applyId);
			queryParam.addAttr("applyId", applyId);
			queryParam.setCurrentPage(1);
			queryParam.setEveryPage(10);
			queryParam.setOrderBy("startCallTime");
			queryParam.setOrderValue("desc");
	
			List<Map<String,Object>> retList = StoreApplyUtils.getCallRecord(queryParam);
			result.setRows(retList);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryCallRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 取消预约记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("cancelBookRecord")
	@ResponseBody
	public AppResult cancelBookRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			
			//取消预约
			AppParam queryParam = new AppParam("treatBookDetailService", "updateBookStatus");
			queryParam.addAttr("applyId", applyId);
			queryParam.addAttr("bookStatus", 1);
			queryParam.addAttr("customerId", customerId);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			if(result.getRows().size() > 0){
				//改变预约主表状态
				AppParam bookParam = new AppParam("treatBookService", "update");
				bookParam.addAttr("applyId", applyId);
				bookParam.addAttr("status", 2);
				bookParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(bookParam);
			}
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "cancelBookRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}


	/**
	 * 查询客户的资料文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryCustMaterial")
	@ResponseBody
	public AppResult queryCustMaterial(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			AppParam queryParam = new AppParam("applyFileService", "query");
			queryParam.addAttr("applyId", applyId);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
			AppResult newResult = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> houseList = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> creditList = new ArrayList<Map<String,Object>>(); 
			List<Map<String,Object>> contractList = new ArrayList<Map<String,Object>>(); 
			if(newResult.getRows().size() > 0){
				for(Map<String,Object> map : newResult.getRows()){
					String materialType = StringUtil.getString(map.get("materialType"));
					if("house".equals(materialType)){
						map.put("url",map.get("path"));
						houseList.add(map);
					}else if("credit".equals(materialType)){
						map.put("url",map.get("path"));
						creditList.add(map);
					}else if("contract".equals(materialType)){
						map.put("url",map.get("path"));
						contractList.add(map);
					}
				}
			}
			result.putAttr("houseList", houseList);
			result.putAttr("creditList", creditList);
			result.putAttr("contractList", contractList);
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e,"queryCustMaterial error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询通话录音记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryCallAudioRecord")
	@ResponseBody
	public AppResult queryCallAudioRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//通话录音记录
			AppParam storeParam = new AppParam("storeCallAudioService","queryShow");
			RequestUtil.setAttr(storeParam, request);
			//判断订单是否是二次申请
			ApplyInfoUtil.isAgainApplyOrder(storeParam, applyId);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if(custInfo != null){
				orgId = StringUtil.getString(custInfo.get("orgId"));//门店
			}
			if(!ApplyInfoUtil.isAdminAuth(customerId)){
				storeParam.addAttr("orgId", orgId);
				storeParam.addAttr("isAdmin", false);
			}else{
				storeParam.addAttr("isAdmin", true);
			}
			storeParam.addAttr("applyId", applyId);
			storeParam.setCurrentPage(1);
			storeParam.setEveryPage(10);
			storeParam.setOrderBy("startCallTime");
			storeParam.setOrderValue("desc");
	
			List<Map<String,Object>> retList = StoreApplyUtils.getCallAudioRecord(storeParam);
			result.setRows(retList);
			
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoAction.class, e, "queryCallAudioRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询主要信息和基本信息(t_borrow_apply为主表查询)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBorrowMainInfo")
	@ResponseBody
	public AppResult queryBorrowMainInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("申请Id不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(Boolean.FALSE);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String searchDetailFlag = request.getParameter("searchDetailFlag");
			//查询用户是否有权限查看详情
			AppResult authResult = ApplyInfoUtil.isQueryDetailAuth(customerId, applyId,searchDetailFlag);
			if(!authResult.isSuccess()){
				return authResult;
			}
			//查询主要信息
			AppParam param = new AppParam("storeListOptExtService",
					"queryBorrowMainInfo");
			param.addAttr("applyId", applyId);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			Map<String, Object> resultMap = null;
		
			// 查询申请信息
			Map<String, Object> applyMap = StoreApplyUtils.getApplyInfo(applyId);
			String custId = StringUtil.getString(applyMap.get("lastStore"));
			if (custInfo != null && result.getRows().size() > 0) {
				String authType = StringUtil.getString(custInfo.get("roleType"));
				// 门店负责人和门店经理可以查看相应分公司所有手机号码
				if(!CustConstant.CUST_ROLETYPE_6.equals(authType)
						&& !CustConstant.CUST_ROLETYPE_7.equals(authType) && !custId.equals(customerId)){
					resultMap = result.getRow(0);
					resultMap.put("telephone", StringUtil.getHideTelphone(StringUtil.getString(resultMap.get("telephone"))));
					result.addRow(resultMap);
				}
			}
			AppResult newResult = new AppResult();
			newResult.putAttr("mainInfo",
					result.getRows().size() > 0 ? result.getRow(0) : null);
			// 设置基本信息
			result.putAttr("baseInfo",StoreApplyUtils.getApplyBaseInfo(applyId));
			// 设置其它信息
			result.putAttr("orderStatus", applyMap.get("orderStatus"));
			result.putAttr("customerId", custId);
			result.putAttr("orderType",  applyMap.get("orderType"));
			result.putAttr("backStatus", applyMap.get("backStatus"));
			result.putAttr("backDesc", applyMap.get("backDesc"));
			//获取用户信息
			newResult.putAttr("custInfo", custInfo);
			//专属单
			Map<String,Object> execMap = StoreApplyUtils.getApplyExecOrder(applyId);
			result.putAttr("execOders", StringUtils.isEmpty(execMap.get("applyId")) == true ? 0 : 1);
			
			return newResult;
		} catch (Exception e) {
			LogerUtil
					.error(ApplyInfoAction.class, e, "queryMainBaseInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
