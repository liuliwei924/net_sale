package org.xxjr.store.web.action.account.config;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.sys.util.ServiceKey;

/**
 * 对外导出配置
 * @author zenghw
 *
 */
@Controller()
@RequestMapping("/account/config/foreignExportConf/")
public class ForeignExportCfgAction {
	
	
	/**
	 * 查询对外导出配置
	 * @param request
	 * @return
	 */
	@RequestMapping("queryForeginExport")
	@ResponseBody
	public AppResult queryForeginExport(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("thirdExportCfgService");
			params.setMethod("queryForeginExport");
			params.setOrderBy("createTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryForeginExport error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 添加对外导出配置
	 * @param request
	 * @return
	 */
	@RequestMapping("addForeginExport")
	@ResponseBody
	public AppResult addForeginExport(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = request.getParameter("customerId");
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("外部渠道人员不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("thirdExportCfgService");
			params.setMethod("addForeginExport");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "addForeginExport error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 删除对外导出配置
	 * @param request
	 * @return
	 */
	@RequestMapping("deleteForeginExport")
	@ResponseBody
	public AppResult deleteForeginExport(HttpServletRequest request){
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
			params.setService("thirdExportCfgService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "deleteForeginExport error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 更新对外导出配置
	 * @param request
	 * @return
	 */
	@RequestMapping("updateForeginExport")
	@ResponseBody
	public AppResult updateForeginExport(HttpServletRequest request){
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
			params.setService("thirdExportCfgService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateForeginExport error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
