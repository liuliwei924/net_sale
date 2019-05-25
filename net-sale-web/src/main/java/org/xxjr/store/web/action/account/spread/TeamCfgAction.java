package org.xxjr.store.web.action.account.spread;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.TeamConfigUtil;
import org.xxjr.sys.util.ServiceKey;

@Controller
@RequestMapping("/account/spread/teamCfg/")
public class TeamCfgAction {
	
	@RequestMapping("queryList")
	@ResponseBody
	public AppResult queryList (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("borrowTeamService", "queryByPage");
			param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RequestUtil.setAttr(param, request);
			param.setOrderBy("createTime");
			param.setOrderValue("DESC");
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "TeamCfgAction queryList error");
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
			param.setService("borrowTeamService");
			if (StringUtils.isEmpty(pageType)) {
				result.setSuccess(false);
				result.setMessage("缺少重要参数!");
				return result;
			}
			if ("add".equals(pageType)) {
				AppParam queryParam = new AppParam("borrowTeamService", "query");
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				queryParam.addAttr("teamNo", request.getParameter("teamNo"));
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
			TeamConfigUtil.refreshBorrowTeam();
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "TeamCfgAction add or edit cfg error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("queryChannels")
	@ResponseBody
	public AppResult queryChannels (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("borrowTeamService", "queryChannels");
			if (StringUtils.isEmpty(request.getParameter("teamNo"))) {
				result.setSuccess(false);
				result.setMessage("缺少重要参数!");
				return result;
			}
			param.addAttr("teamNo", request.getParameter("teamNo"));
			result = ServiceKey.doCallNoTx(param, ServiceKey.Key_busi_in);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "TeamCfgAction queryChannels error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	@RequestMapping("updateChannels")
	@ResponseBody
	public AppResult updateChannels (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("borrowTeamService", "updateChannels");
			if (StringUtils.isEmpty(request.getParameter("teamNo")) || StringUtils.isEmpty(request.getParameter("channels"))) {
				result.setSuccess(false);
				result.setMessage("缺少重要参数!");
				return result;
			}
			param.addAttr("channels", JsonUtil.getInstance().json2Object(request.getParameter("channels"), List.class));
			param.addAttr("teamNo", request.getParameter("teamNo"));
			result = ServiceKey.doCall(param, ServiceKey.Key_busi_in);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "TeamCfgAction updateChannels error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
}
