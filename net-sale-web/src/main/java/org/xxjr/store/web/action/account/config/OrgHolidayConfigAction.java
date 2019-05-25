package org.xxjr.store.web.action.account.config;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/**
 * 门店假期配置
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/config/holidayConf/")
public class OrgHolidayConfigAction {
	
	/**
	 * 查询门店假期配置
	 * @param request
	 * @return
	 */
	@RequestMapping("queryHolidayConf")
	@ResponseBody
	public AppResult queryHolidayConf(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				throw new SysException("用户ID不能为空");
			}
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null){
				String authType =   StringUtil.getString(custInfo.get("roleType"));
				String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
				String orgId = StringUtil.getString(custInfo.get("orgId"));//门店
				//门店管理员和管理可以查看门店
				if(CustConstant.CUST_ROLETYPE_1.equals(authType)
						||CustConstant.CUST_ROLETYPE_6.equals(authType)
						||CustConstant.CUST_ROLETYPE_7.equals(authType)){
					if(!"all".equals(userOrgs) && !StringUtils.isEmpty(userOrgs)){
						params.addAttr("userOrgs",userOrgs);
					}else if(StringUtils.isEmpty(userOrgs)){
						return CustomerUtil.retErrorMsg("没有门店管理权限");
					}
				}else{ //门店业务员 主管 、副主管
					params.addAttr("orgId", orgId);
				}
			}
			params.setService("orgHolidayService");
			params.setOrderBy("myDate");
			params.setMethod("queryByPage");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryHolidayConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 新增门店假期配置
	 * @param request
	 * @return
	 */
	@RequestMapping("addHolidayConf")
	@ResponseBody
	public AppResult addHolidayConf(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			
			AppParam queryParams = new AppParam();
			queryParams.setService("orgHolidayService");
			queryParams.addAttr("orgId", params.getAttr("orgId"));
			queryParams.addAttr("myDate", params.getAttr("myDate"));
			queryParams.setMethod("queryCount");
			queryParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryParams);
			int	count = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE),0);
			if(count > 0){
				result.setSuccess(false);
				result.setMessage("同一个门店的假期日期不能重复设置");
				return result;
			}
			params.setService("orgHolidayService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "addHolidayConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除门店假期
	 * @param request
	 * @return
	 */
	@RequestMapping("deleteHolidayConf")
	@ResponseBody
	public AppResult deleteHolidayConf(HttpServletRequest request){
		AppResult result = new AppResult();
		String holidayId = request.getParameter("holidayId");
		if (StringUtils.isEmpty(holidayId)) {
			result.setSuccess(false);
			result.setMessage("ID不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("orgHolidayService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "deleteHolidayConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 更新门店假期
	 * @param request
	 * @return
	 */
	@RequestMapping("updateHolidayConf")
	@ResponseBody
	public AppResult updateHolidayConf(HttpServletRequest request){
		AppResult result = new AppResult();
		String holidayId = request.getParameter("holidayId");
		if (StringUtils.isEmpty(holidayId)) {
			result.setSuccess(false);
			result.setMessage("ID不能为空空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			AppParam queryParams = new AppParam();
			queryParams.setService("orgHolidayService");
			queryParams.addAttr("orgId", params.getAttr("orgId"));
			queryParams.addAttr("myDate", params.getAttr("myDate"));
			queryParams.setMethod("queryCount");
			queryParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryParams);
			int	count = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE),0);
			if(count > 0){
				result.setSuccess(false);
				result.setMessage("同一个门店的假期日期不能重复设置");
				return result;
			}
			params.setService("orgHolidayService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateHolidayConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
