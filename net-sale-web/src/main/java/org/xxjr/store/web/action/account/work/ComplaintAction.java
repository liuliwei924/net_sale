package org.xxjr.store.web.action.account.work;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;

/***
 * 投诉
 * @author
 *
 */
@Controller()
@RequestMapping("/account/work/custComplaint")
public class ComplaintAction {
	
	/**
	 * 查询投诉列表
	 * @param request
	 * @return
	 */
	@RequestMapping("queryComplaintList")
	@ResponseBody
	public AppResult queryComplaintList(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		try {
			AppParam queryParam = new AppParam();
			queryParam.setService("suggestRecordService");
			queryParam.setMethod("querySuggestList");
			RequestUtil.setAttr(queryParam, request);
			StoreUserUtil.dealUserAuthParam(queryParam, customerId, "customerId");
			queryParam.addAttr("type", "2");//1-建议 2-投诉 
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryComplaintList error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}

}
