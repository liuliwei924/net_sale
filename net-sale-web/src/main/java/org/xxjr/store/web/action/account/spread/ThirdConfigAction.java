package org.xxjr.store.web.action.account.spread;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.push.PushPlatformUtils;
import org.xxjr.sys.util.ServiceKey;

@Controller
@RequestMapping("/account/spread/third/")
public class ThirdConfigAction {
	@RequestMapping("queryList")
	@ResponseBody
	public AppResult queryList (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("thirdDataCfgService", "queryByPage");
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RequestUtil.setAttr(param, request);
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "ThirdConfigAction queryList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("/{pageType}")
	@ResponseBody
	public AppResult add (@PathVariable("pageType") String pageType, HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam();
			param.setService("thirdDataCfgService");
			if (StringUtils.isEmpty(pageType)) {
				result.setSuccess(false);
				result.setMessage("缺少重要参数!");
				return result;
			}
			if ("add".equals(pageType)) {
				AppParam queryParam = new AppParam("thirdDataCfgService", "query");
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				queryParam.addAttr("channelCode", request.getParameter("channelCode"));
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
				if (queryResult.getRows().size() > 0) {
					result.setSuccess(false);
					result.setMessage("编号已存在!");
					return result;
				}
				param.setMethod("insert");
			}else {
				param.setMethod("update");
			}
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RequestUtil.setAttr(param, request);
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "ThirdConfigAction add or edit cfg error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("del")
	@ResponseBody
	public AppResult del (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("thirdDataCfgService", "delete");
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RequestUtil.setAttr(param, request);
			result = RemoteInvoke.getInstance().call(param);
			PushPlatformUtils.refreshPushConfig();
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "del pushconfig error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

}
