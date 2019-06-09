package org.xxjr.store.web.action.account.work;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

/**
 * 退单列表
 * @author chencx
 *
 */
@Controller()
@RequestMapping("/account/work/backOrder/")
public class BackOrderAction {
	/**
	 *  查询退单列表信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryBackOrderList")
	@ResponseBody
	public AppResult queryBackOrderList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			AppParam param = new AppParam();
			param.setService("storeListOptExtService");
			param.setMethod("queryBackOrderList");
			param.addAttr("status", "2");//门店锁定
			param.addAttr("backStatusIn", "2,3,4");
			RequestUtil.setAttr(param, request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null && custInfo.size() > 0){
				String authType = StringUtil.getString(custInfo.get("roleType")); //角色
				//管理员才有权限查询
				if(!CustConstant.CUST_ROLETYPE_1.equals(authType)){
					return CustomerUtil.retErrorMsg("您没有权限查询");
				}
				param.addAttr("roleType", authType);
			}
			//开启渠道类型标识
			int openStoreChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
			if(openStoreChannelFlag == 1){
				//设置查询渠道类型
				String queryChannelType = SysParamsUtil.getStringParamByKey("storeQueryChannelType", "2");
				param.addAttr("queryChannelType", queryChannelType);
			}
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
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
			param.setOrderBy("lastUpdateTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
			return result;
		} catch (Exception e) {
			LogerUtil.error(MyOrderAction.class, e, "queryBackOrderList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 *  批量审核退单
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("batchCheckBackOrder")
	@ResponseBody
	public AppResult batchCheckBackOrder(HttpServletRequest request) {
		AppResult result = new AppResult();
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
					return CustomerUtil.retErrorMsg("您没有权限审核退单");
				}
			}
			String backStatus = request.getParameter("backStatus");
			if (StringUtils.isEmpty(backStatus)) {
				return CustomerUtil.retErrorMsg("退单状态不能为空");
			}
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
			
	        String backDesc = request.getParameter("backDesc");
			if(StoreConstant.STORE_BACK_STATUS_4.equals(backStatus)
					&& StringUtils.isEmpty(backDesc)){
				return CustomerUtil.retErrorMsg("退单失败原因不能为空");
			}
			
			AppParam param = new AppParam("storeHandleExtService","batchCheckBackOrder");
			param.addAttr("orders", orders);
			param.addAttr("backStatus", backStatus);
			param.addAttr("customerId", customerId);
			param.addAttr("backDesc", backDesc);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(BackOrderAction.class, e, "batchCheckBackOrder error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
