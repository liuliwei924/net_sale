package org.xxjr.store.web.action.account.dataset;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.ApplyInfoUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.ServiceKey;

@Controller
@RequestMapping("/account/dataset/storeDownLoad/")
public class StoreDownLoadAction {
	
	/*** 
	 * 导出记录查询
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("query")
	@ResponseBody
	public AppResult query(HttpServletRequest request,HttpServletResponse response){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("exportRecordService","queryByPage");
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			Map<String,Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if (!ApplyInfoUtil.isAdminAuth(customerId)) {
				params.addAttr("exportMan", StringUtil.getString(custInfo.get("userName")));
			}		
			params.addAttr("sysType", 2); // 只查询网销
			params.setOrderBy("createTime");
			params.setOrderValue("DESC");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "导出记录查询错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	
	/*** 
	 * 删除导出记录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public AppResult delete(HttpServletRequest request,HttpServletResponse response){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam("exportRecordService","delete");
			RequestUtil.setAttr(params, request);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "删除导出记录错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	
}
