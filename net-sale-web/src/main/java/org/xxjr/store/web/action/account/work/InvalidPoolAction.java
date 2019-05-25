package org.xxjr.store.web.action.account.work;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.JsonUtil;
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
import org.xxjr.sys.util.ValidUtils;

/**
 * 无效池列表
 * @author yuany
 *
 */
@Controller()
@RequestMapping("/account/work/invalidStorePool")
public class InvalidPoolAction {
	/**
	 *  查询无效池列表信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryInvalidPoolList")
	@ResponseBody
	public AppResult queryInvalidPoolList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			AppParam param = new AppParam("invalidStorePoolService","queryInvalidPool");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!searchKey.isEmpty()){
				if(ValidUtils.validateTelephone(searchKey)){
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!storeSearchKey.isEmpty()){
				if(ValidUtils.validateTelephone(storeSearchKey)){
					param.addAttr("storeTelephone", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("storeRealName", storeSearchKey);
					param.removeAttr("searchKey");
				}
			}
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			
		} catch (Exception e) {
			LogerUtil.error(InvalidPoolAction.class, e, "queryInvalidPoolList error");
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
	@RequestMapping("checkTransOtherXDJL")
	@ResponseBody
	public AppResult checkTransOtherXDJL(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				throw new SysException("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				throw new SysException("请传入订单的基本信息");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				throw new SysException("用户ID不能为空");
			}
			String orgId = request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				throw new SysException("门店不能为空");
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				throw new SysException("门店人员不能为空");
			}

			AppParam param = new AppParam("invalidStorePoolService","invalidOrderAllot");
			param.addAttr("orders", orders);
			param.addAttr("custId", custId);
			param.addAttr("orgId", orgId);
			param.addAttr("customerId", customerId);
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			
		} catch (Exception e) {
			LogerUtil.error(AllotPandAction.class, e, "transOtherXDJL error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}

