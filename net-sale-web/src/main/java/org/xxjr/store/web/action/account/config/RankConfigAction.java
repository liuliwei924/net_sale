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
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 * 等级配置
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/config/rankConf/")
public class RankConfigAction {
	
	/**
	 * 查询等级配置
	 * @param request
	 * @return
	 */
	@RequestMapping("queryRankConf")
	@ResponseBody
	public AppResult queryRankConf(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowRankCfgService");
			params.setMethod("queryByPage");
			params.setOrderBy("gradeCode");
			params.setOrderValue("asc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryRankConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 新增等级配置
	 * @param request
	 * @return
	 */
	@RequestMapping("addRankConf")
	@ResponseBody
	public AppResult addRankConf(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowRankCfgService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				StoreSeparateUtils.refreshRankConfig();
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "addRankConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除等级配置
	 * @param request
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public AppResult delete(HttpServletRequest request){
		AppResult result = new AppResult();
		String rankId = request.getParameter("rankId");
		if (StringUtils.isEmpty(rankId)) {
			result.setSuccess(false);
			result.setMessage("等级ID不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowRankCfgService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				StoreSeparateUtils.refreshRankConfig();
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "delete error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 更新等级配置
	 * @param request
	 * @return
	 */
	@RequestMapping("updateRankConf")
	@ResponseBody
	public AppResult updateRankConf(HttpServletRequest request){
		AppResult result = new AppResult();
		String rankId = request.getParameter("rankId");
		if (StringUtils.isEmpty(rankId)) {
			result.setSuccess(false);
			result.setMessage("等级ID不能为空空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("borrowRankCfgService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				StoreSeparateUtils.refreshRankConfig();
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateRankConf error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
