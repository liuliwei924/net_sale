package org.xxjr.store.web.action.common;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;


/**
 * 提供外部调用接口(CFS查询成本接口)
 * @author chencx
 *
 */
@Controller
@RequestMapping("/externalCall/")
public class ExternalCallAction {

	/**
	 * 查询门店人员月度成本
	 * 
	 * @param request
	 * @return
	 */	
	@RequestMapping("/storeCost/queryStoreMonthCost")
	@ResponseBody
	public AppResult queryStoreMonthCost(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("sumStoreBaseMonthService", "queryStoreMonthCost");
			String startRecordMonth = request.getParameter("startRecordMonth");
			String endRecordMonth = request.getParameter("endRecordMonth");
			if(StringUtils.isEmpty(startRecordMonth) && StringUtils.isEmpty(endRecordMonth)){
				return CustomerUtil.retErrorMsg("查询日期不能为空");
			}
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_sum));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(ExternalCallAction.class, e, "queryStoreMonthCost error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
