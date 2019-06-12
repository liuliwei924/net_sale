package org.xxjr.store.web.action.account.work;

import java.io.File;
import java.io.InputStream;
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
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.common.web.util.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.kf.BorrowApplyUtils;
import org.xxjr.busi.util.store.ApplyInfoUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.IDCardValidate;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.FileGroupUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;
import org.xxjr.tools.util.QcloudUploader;

/**
 * 待处理
 * 
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/work/waitDeal/")
public class WaitDealAction {
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
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				throw new SysException("用户ID不能为空");
			}
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				throw new SysException("申请ID不能为空");
			}
			AppParam param = new AppParam("treatBookService", "query");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			Map<String, Object> custInfo = StoreUserUtil
					.getCustomerInfo(customerId);
			result.putAttr("custInfo", custInfo);
			return result;
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "queryBookInfo error");
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
	@RequestMapping("querySingnInfo")
	@ResponseBody
	public AppResult querySingnInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				throw new SysException("申请ID不能为空");
			}
			AppParam param = new AppParam("treatInfoService", "query");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "querySingnInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 查询回款信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBackInfo")
	@ResponseBody
	public AppResult queryBackInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				throw new SysException("申请ID不能为空");
			}
			AppParam param = new AppParam("treatSuccessService", "query");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "queryBackInfo error");
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
	@RequestMapping("queryStoreRecord")
	@ResponseBody
	public AppResult queryStoreRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam applyParam = new AppParam();
			applyParam.setService("borrowStoreRecordService");
			applyParam.setMethod("queryStoreRecord");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "queryStoreRecord error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 订单处理-继续跟进
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("followUpDeal")
	@ResponseBody
	public AppResult followUpDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				throw new SysException("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				throw new SysException("用户ID不能为空");
			}
			String handleDesc = request.getParameter("handleDesc");
			if (StringUtils.isEmpty(handleDesc)) {
				throw new SysException("处理描述不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("followUpDeal");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "followUpDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 订单处理-预约处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("bookDeal")
	@ResponseBody
	public AppResult bookDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String bookTime = request.getParameter("bookTime");
			if (StringUtils.isEmpty(bookTime)) {
				return CustomerUtil.retErrorMsg("预约上门时间不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			applyParam.addAttr("status", StoreConstant.STORE_BOOK_1);//预约中
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("bookHandle");
			
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "bookDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 订单处理-上门处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("visitDeal")
	@ResponseBody
	public AppResult visitDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String visitTime = request.getParameter("visitTime");
			if (StringUtils.isEmpty(visitTime)) {
				return CustomerUtil.retErrorMsg("上门时间不能为空");
			}
			String recCustId = request.getParameter("recCustId");
			if (StringUtils.isEmpty(recCustId)) {
				return CustomerUtil.retErrorMsg("接待人不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			applyParam.addAttr("status", StoreConstant.STORE_BOOK_3);//已上门
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("visitDeal");
			
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "visitDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 订单处理-签单处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("signDeal")
	@ResponseBody
	public AppResult signDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String status = request.getParameter("status");
			if (StringUtils.isEmpty(status)) {
				return CustomerUtil.retErrorMsg("状态不能为空");
			}
			String telephone = request.getParameter("telephone");
			if (StringUtils.isEmpty(telephone)) {
				return CustomerUtil.retErrorMsg("手机号码不能为空");
			}
			// 验证手机号码
			Boolean validResult = ValidUtils.validateTelephone(telephone);
			if (!validResult) {
				return CustomerUtil.retErrorMsg("请输入正确的手机号码");
			}
			String cardNo = request.getParameter("cardNo");
			if (StringUtils.isEmpty(cardNo)) {
				return CustomerUtil.retErrorMsg("身份证不能为空");
			}
			// 验证身份证号码
			String validCardResult = IDCardValidate.validCardNo(cardNo);
			if (!StringUtils.isEmpty(validCardResult)) {
				return CustomerUtil.retErrorMsg(validCardResult);
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null){
				String authType = StringUtil.getString(custInfo.get("roleType"));
				applyParam.addAttr("authType", authType);
			}
			double signAmount = NumberUtil.getDouble(
					request.getParameter("signAmount"), -1);

			if (signAmount >= 1000 || signAmount <= 0) {
				return CustomerUtil.retErrorMsg("签单金额非法，请注意签单金额单位为万元！");
			}
			String failureCause = request.getParameter("failureCause");
			if(StoreConstant.STORE_SIGN_3.equals(status) && StringUtils.isEmpty(failureCause)){
				return CustomerUtil.retErrorMsg("签约失败原因不能为空！");
			}
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("signDeal");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);

		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "signDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 订单处理-回款处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("retLoanDeal")
	@ResponseBody
	public AppResult retLoanDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String loanOrg = request.getParameter("loanOrg");
			if (StringUtils.isEmpty(loanOrg)) {
				return CustomerUtil.retErrorMsg("贷款机构不能为空");
			}
			String applyAmount = request.getParameter("applyAmount");
			if (StringUtils.isEmpty(applyAmount)) {
				return CustomerUtil.retErrorMsg("申请金额不能为空");
			}
			String loanAmount = request.getParameter("loanAmount");
			if (StringUtils.isEmpty(loanAmount)) {
				return CustomerUtil.retErrorMsg("放款金额不能为空");
			}
			String loanDeadline = request.getParameter("loanDeadline");
			if (StringUtils.isEmpty(loanDeadline)) {
				return CustomerUtil.retErrorMsg("放款期限不能为空");
			}
			String loanType = request.getParameter("loanType");
			if (StringUtils.isEmpty(loanType)) {
				return CustomerUtil.retErrorMsg("放款类型不能为空");
			}

			double applyDoubleAmount = NumberUtil.getDouble(applyAmount, -1);

			if (applyDoubleAmount >= 1000 || applyDoubleAmount <= 0) {
				return CustomerUtil.retErrorMsg("申请金额非法，请注意申请金额单位为万元！");
			}

			double loanDoubleAmount = NumberUtil.getDouble(loanAmount, -1);

			if (loanDoubleAmount >= 1000 || loanDoubleAmount <= 0) {
				return CustomerUtil.retErrorMsg("放款金额非法，请注意放款金额单位为万元！");
			}

			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("retLoanDeal");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "retLoanDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 专属单设置
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("orderSet")
	@ResponseBody
	public AppResult orderSet(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("专属单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入专属单的基本信息");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam applyParam = new AppParam();
			applyParam.addAttr("orders", orders);
			applyParam.addAttr("customerId", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("newOrderSet");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "orderSet error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 取消专属单
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("cancleOrder")
	@ResponseBody
	public AppResult cancleOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("专属单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入专属单的基本信息");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam applyParam = new AppParam();
			applyParam.addAttr("orders", orders);
			applyParam.addAttr("customerId", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("cancleOrder");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "cancleOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 转其他信贷经理
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("transOtherXDJL")
	@ResponseBody
	public AppResult transOtherXDJL(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入订单的基本信息");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String orgId = request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				return CustomerUtil.retErrorMsg("门店不能为空");
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("门店人员不能为空");
			}
			AppParam applyParam = new AppParam();
			applyParam.addAttr("orders", orders);
			applyParam.addAttr("orgId", orgId);
			applyParam.addAttr("custId", custId);
			applyParam.addAttr("customerId", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("transOtherXDJL");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "transOtherXDJL error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	
	/***
	 * 更新订单状态
	 * @param request
	 * @return
	 */
	@RequestMapping("updateOrderStatus")
	@ResponseBody
	public AppResult updateOrderStatus(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(false);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String orderStatus = request.getParameter("orderStatus");
			if (StringUtils.isEmpty(orderStatus)) {
				result.setSuccess(false);
				result.setMessage("处理状态不能为空");
				return result;
			}
			// 查询申请信息
			Map<String, Object> applyMap = StoreApplyUtils.getStoreApplyInfo(applyId);
			if(applyMap != null){
				String lastStore = StringUtil.getString(applyMap.get("lastStore"));
				if(StringUtils.isEmpty(lastStore)){
					result.setSuccess(false);
					result.setMessage("此订单暂无处理人或已被回收，暂不能修改处理状态");
					return result;
				}
			}
			AppParam storeParam = new AppParam("borrowStoreRecordService","queryStoreRecord");
			storeParam.addAttr("applyId", applyId);
			// handleTypeFlag 描述类型不等于0的标识
			storeParam.addAttr("handleTypeFlag", "1");
			storeParam.setOrderBy("t.createTime");
			storeParam.setOrderValue("desc");
			storeParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult recordResult = RemoteInvoke.getInstance().callNoTx(storeParam);
			if(recordResult.getRows().size() > 0 && !StringUtils.isEmpty(recordResult.getRow(0))){
				String createTime = StringUtil.getString(recordResult.getRow(0).get("createTime"));
				String nowTime = DateUtil.toStringByParttern(DateUtil.getNextMinutes(new Date(), -10),
						DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
				int dateCompare = createTime.compareTo(nowTime);
				if(dateCompare < 0){
					result.setSuccess(false);
					result.setMessage("请先添加跟进记录再更改订单状态");
					return result;
				}
			}else{
				result.setSuccess(false);
				result.setMessage("请先添加跟进记录再更改订单状态");
				return result;
			}
			AppParam param = new AppParam("storeHandleExtService", "updateOrderStatus");
			RequestUtil.setAttr(param, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(param, customerId, custId);
			} else {
				param.addAttr("customerId", customerId);
			}
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			
			
		}catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "updateOrderStatus");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 客户星级处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("custLabelDeal")
	@ResponseBody
	public AppResult custLabelDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			String custLabel = request.getParameter("custLabel");
			String customerId = StoreUserUtil.getCustomerId(request);
			
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			if (StringUtils.isEmpty(custLabel)) {
				return CustomerUtil.retErrorMsg("客户星级不能为空");
			}
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setService("storeHandleExtService");
			applyParam.setMethod("custLabelDeal");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "custLabelDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 修改订单状态
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("updateDealOrderType")
	@ResponseBody
	public AppResult updateDealOrderType(HttpServletRequest request) {
		AppResult result = new AppResult();
		try{
			String applyId = StringUtil.getString(request.getParameter("applyId"));
			String customerId = StoreUserUtil.getCustomerId(request);
			String dealOrderType = request.getParameter("dealOrderType");

			
			if(StringUtils.isEmpty(applyId)){
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			if(StringUtils.isEmpty(customerId)){
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			if (StringUtils.isEmpty(dealOrderType)) {
				return CustomerUtil.retErrorMsg("订单状态不能为空");
			}
			
			String custId = request.getParameter("customerId");
			AppParam param = new AppParam();
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(param, customerId, custId);
			} else {
				param.addAttr("customerId", customerId);
			}
			param.addAttr("applyId", applyId);
			param.addAttr("dealOrderType", dealOrderType);
			param.setService("storeHandleExtService");
			param.setMethod("updateDealOrderType");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(param);
		}catch (Exception e){
			LogerUtil.error(WaitDealAction.class, e, "updateDealOrderType error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 * 修改订单评分等级
	 * @param request
	 * @return
	 */
	@RequestMapping("updateOrderRate")
	@ResponseBody
	public AppResult updateOrderRate(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String applyId = StringUtil.getString(request.getParameter("applyId"));
			String customerId = StringUtil.getString(request.getParameter("customerId"));
			String orderRate = StringUtil.getString(request.getParameter("orderRate"));
			
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			if (StringUtils.isEmpty(orderRate)) {
				return CustomerUtil.retErrorMsg("订单状态不能为空");
			}
			String custId = request.getParameter("customerId");
			AppParam param = new AppParam();
			RequestUtil.setAttr(param, request);
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(param, customerId, custId);
			} else {
				param.addAttr("customerId", customerId);
			}
			param.addAttr("applyId", applyId);
			param.addAttr("orderRate", orderRate);
			param.setService("storeApplyExtService");
			param.setMethod("updateOrderRate");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "updateOrderRate error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 新增跟进记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("addFollowRecord")
	@ResponseBody
	public AppResult addFollowRecord(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String handleDesc = request.getParameter("handleDesc");
			if (StringUtils.isEmpty(handleDesc)) {
				return CustomerUtil.retErrorMsg("处理描述不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("addfollowRecord");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "addFollowRecord");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	
	/**
	 * 投诉处理-新增投诉描述
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("complaintDeal")
	@ResponseBody
	public AppResult complaintDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String type = request.getParameter("type");
			if (StringUtils.isEmpty(type)) {
				return CustomerUtil.retErrorMsg("内容描述不能为空");
			}
			String detailDesc = request.getParameter("detailDesc");
			if (StringUtils.isEmpty(detailDesc)) {
				return CustomerUtil.retErrorMsg("投诉建议描述不能为空");
			}
			String custName = request.getParameter("custName");
			if (StringUtils.isEmpty(custName)) {
				return CustomerUtil.retErrorMsg("姓名不能为空");
			}
			String custTelephone = request.getParameter("custTelephone");
			if (StringUtils.isEmpty(custTelephone)) {
				return CustomerUtil.retErrorMsg("手机号码不能为空");
			}
			String busiName = request.getParameter("busiName");
			if (StringUtils.isEmpty(busiName)) {
				return CustomerUtil.retErrorMsg("服务经理名称不能为空");
			}
			String busiTelephone = request.getParameter("busiTelephone");
			if (StringUtils.isEmpty(busiTelephone)) {
				return CustomerUtil.retErrorMsg("服务经理手机不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
//			applyParam.addAttr("status", StoreConstant.STORE_BOOK_3);//待处理
			applyParam.setService("suggestRecordService");
			applyParam.setMethod("save");
			
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "complaintDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 投诉处理-处理投诉
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("complaintHandel")
	@ResponseBody
	public AppResult complaintHandel(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String recordId = request.getParameter("recordId");
			if (StringUtils.isEmpty(recordId)) {
				return CustomerUtil.retErrorMsg("投诉编号不能为空");
			}
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String type = request.getParameter("type");
			if (StringUtils.isEmpty(type)) {
				return CustomerUtil.retErrorMsg("内容描述不能为空");
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			applyParam.setService("suggestRecordService");
			applyParam.setMethod("update");
			
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "complaintHandel error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 放弃跟进订单
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("abandonFollowOrder")
	@ResponseBody
	public AppResult abandonFollowOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(false);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String nextRecordDate = request.getParameter("nextRecordDate");
			if (StringUtils.isEmpty(nextRecordDate)) {
				result.setSuccess(false);
				result.setMessage("下次跟进时间不能为空");
				return result;
			}
			AppParam applyParam = new AppParam();
			RequestUtil.setAttr(applyParam, request);
			String custId = request.getParameter("customerId");
			if (!StringUtils.isEmpty(custId)) {
				ApplyInfoUtil.setCustomerId(applyParam, customerId, custId);
			} else {
				applyParam.addAttr("customerId", customerId);
			}
			int count = BorrowApplyUtils.queryExecOrder(applyId);
			if(count > 0){
				result.setSuccess(false);
				result.setMessage("此订单是专属单不能放弃跟进！");
				return result;
			}
			applyParam.setService("storeHandleExtService");
			applyParam.setMethod("abandonFollowOrder");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
		} catch (Exception e) {
			LogerUtil.error(WaitDealAction.class, e, "abandonFollowOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 保存客户相关资料
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("saveCustmaterial")
	@ResponseBody
	public AppResult saveCustmaterial(MultipartHttpServletRequest request) {
		AppResult result = new AppResult();
		File tempFile = null;
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(false);
				result.setMessage("申请ID不能为空");
				return result;
			}
			
			String materialType = request.getParameter("materialType");
			if (StringUtils.isEmpty(materialType)) {
				result.setSuccess(false);
				result.setMessage("资料类型不能为空");
				return result;
			}
			String fileType = request.getParameter("fileType");
			if (StringUtils.isEmpty(fileType)) {
				result.setSuccess(false);
				result.setMessage("文件类型不能为空");
				return result;
			}
			String fileTypes = FileGroupUtil.getFileTypes(fileType);
	    	MultipartFile file = FileUtil.getUploadFiles(request, fileTypes);
			String filePath = QcloudUploader.uploadToTenXun(file, fileTypes);
			if (!StringUtils.isEmpty(filePath)) {
				AppParam params = new AppParam();
				params.addAttr("applyId", applyId);
				params.addAttr("materialType", materialType);
				params.addAttr("path", filePath);
				params.setService("applyFileService");
				params.setMethod("insert");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(params);
				if (result.isSuccess()) {
					result.putAttr("url", filePath);
					result.putAttr("materialType", materialType);
				}
			} else {
				result.setSuccess(false);
				result.setMessage("文件上传不成功，请重新上传");
			}

		} catch (Throwable e) {
			LogerUtil.error(WaitDealAction.class, e, "saveCustmaterial error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		} finally {
			if (tempFile != null)
				FileUtil.deleteQuietly(tempFile);
		}
		return result;
	}
	
	/***
	 * 执行文件 上传处理
	 * 
	 * @param request
	 * @param map
	 * @param fileType
	 * @throws Throwable
	 */
	public static void uploadSave(MultipartHttpServletRequest request,
			Map<String, Object> map, String fileType) throws Throwable {
		String fileTypes = FileGroupUtil.getFileTypes(fileType);
    	MultipartFile file = FileUtil.getUploadFiles(request, fileTypes);
		InputStream inputStream = file.getInputStream();
    	String contentType = file.getContentType();
    	long fileSize = file.getSize();
    	String desCosPath = "/upfile/" + fileType + "/" 
    			+ DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD) + "/";
    	String originaFileName = file.getOriginalFilename();
		String uploadFileType = ".png";
		if(originaFileName.lastIndexOf(".") > 0){
			uploadFileType = originaFileName.substring(originaFileName.lastIndexOf("."));
		}
    	String saveName = StringUtil.getUUID() + uploadFileType;
    	QcloudUploader.createDirOnNotExists(desCosPath);
		boolean isSuccess = QcloudUploader.uploadFile(desCosPath + saveName, inputStream, fileSize, contentType);
		if(isSuccess){
			map.put("url", "https://static.xxjr.com/" + desCosPath + saveName);
			map.put("state", "SUCCESS");
		} else {
			map.put("state", "invalid");
			map.put("statusText", "文件上传不成功，请重新上传");
		}
		if(inputStream != null){
			inputStream.close();
		}
	}
	
	/***
	 * 删除文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("deleteFile")
	@ResponseBody
	public AppResult deleteFile(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String fileId = request.getParameter("fileId");
			if (StringUtils.isEmpty(fileId)) {
				result.setSuccess(false);
				result.setMessage("文件ID不能为空");
				return result;
			}
			
			AppParam params = new AppParam();
			params.addAttr("fileId", fileId);
			params.setService("applyFileService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);

		} catch (Throwable e) {
			LogerUtil.error(WaitDealAction.class, e, "deleteFile error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 找回订单
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("againFollowOrder")
	@ResponseBody
	public AppResult againFollowOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(false);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			
			AppParam params = new AppParam();
			params.addAttr("applyId", applyId);
			params.addAttr("customerId", customerId);
			params.setService("storeHandleExtService");
			params.setMethod("againFollowOrder");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);

		} catch (Throwable e) {
			LogerUtil.error(WaitDealAction.class, e, "againFollowOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 退单处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("backOrderDeal")
	@ResponseBody
	public AppResult backOrderDeal(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				result.setSuccess(false);
				result.setMessage("申请ID不能为空");
				return result;
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String backReDesc = request.getParameter("backReDesc");
			if (StringUtils.isEmpty(backReDesc)) {
				result.setSuccess(false);
				result.setMessage("退单原因不能为空");
				return result;
			}
		
			AppParam params = new AppParam();
			params.addAttr("applyId", applyId);
			params.addAttr("backReDesc", backReDesc);
			params.addAttr("customerId", customerId);
			params.setService("storeHandleExtService");
			params.setMethod("backOrderDeal");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);

		} catch (Throwable e) {
			LogerUtil.error(WaitDealAction.class, e, "backOrderDeal error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
		
	
	/***
	 * 删除签单信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("deleteSignInfo")
	@ResponseBody
	public AppResult deleteSignInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
				return CustomerUtil.retErrorMsg("抱歉您没有删除权限");
			}
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String treatyNo = request.getParameter("treatyNo");
			if (StringUtils.isEmpty(treatyNo)) {
				return CustomerUtil.retErrorMsg("合同编号不能为空");
			}
			String reContractId = request.getParameter("reContractId");
			AppParam params = new AppParam("storeHandleExtService","deleteSignInfo");
			params.addAttr("applyId", applyId);
			params.addAttr("treatyNo", treatyNo);
			params.addAttr("reContractId", reContractId);
			params.addAttr("customerId", customerId);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
		} catch (Throwable e) {
			LogerUtil.error(WaitDealAction.class, e, "deleteSignInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 删除回款信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("deleteBackAmout")
	@ResponseBody
	public AppResult deleteBackAmout(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String roleType = StoreUserUtil.getCustomerRole(customerId);
			if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
				return CustomerUtil.retErrorMsg("抱歉您没有删除权限");
			}
			String applyId = request.getParameter("applyId");
			if (StringUtils.isEmpty(applyId)) {
				return CustomerUtil.retErrorMsg("申请ID不能为空");
			}
			String recordId = request.getParameter("recordId");
			if (StringUtils.isEmpty(recordId)) {
				return CustomerUtil.retErrorMsg("回款编号不能为空");
			}
			AppParam params = new AppParam("storeHandleExtService","deleteBackAmout");
			params.addAttr("applyId", applyId);
			params.addAttr("recordId", recordId);
			params.addAttr("customerId", customerId);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
		} catch (Throwable e) {
			LogerUtil.error(WaitDealAction.class, e, "deleteBackAmout error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}