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
 * 能力值等级配置
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/config/abilityGrade/")
public class AbilityGradeCfgAction {
	
	/**
	 * 查询能力值等级配置列表
	 * @param request
	 * @return
	 */
	@RequestMapping("queryList")
	@ResponseBody
	public AppResult queryList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam queryParam = new AppParam();
			queryParam.setService("abilityGradeCfgService");
			queryParam.setMethod("queryByPage");
			RequestUtil.setAttr(queryParam, request);
			queryParam.setOrderBy("gradeCode");
			queryParam.setOrderValue("asc");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 保存能力值等级配置
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("save")
	public AppResult save(HttpServletRequest request){
		AppResult result = new AppResult();
		
		String gradeName = request.getParameter("gradeName");
		if (StringUtils.isEmpty(gradeName)) {
			result.setSuccess(false);
        	result.setMessage("能力等级名称不能为空");
        	return result ;
		}
		String minScore = request.getParameter("minScore");
		if (StringUtils.isEmpty(minScore)) {
			result.setSuccess(false);
        	result.setMessage("最低能力值不能为空");
        	return result ;
		}
		String maxScore = request.getParameter("maxScore");
		if (StringUtils.isEmpty(maxScore)) {
			result.setSuccess(false);
        	result.setMessage("最高能力值不能为空");
        	return result ;
		}
		String successCount = request.getParameter("successCount");
		if (StringUtils.isEmpty(successCount)) {
			result.setSuccess(false);
        	result.setMessage("回款成功笔数不能为空");
        	return result ;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("abilityGradeCfgService");
			params.setMethod("save");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "save error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 修改能力值等级配置
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("update")
	public AppResult update(HttpServletRequest request){
		AppResult result = new AppResult();
		String gradeCode = request.getParameter("gradeCode");
		if (StringUtils.isEmpty(gradeCode)) {
			result.setSuccess(false);
        	result.setMessage("能力等级ID不能为空");
        	return result ;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("abilityGradeCfgService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			RemoteInvoke.getInstance().call(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "update error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 删除能力值等级配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public AppResult delete(HttpServletRequest request){
		AppResult result = new AppResult();
		String gradeCode = request.getParameter("gradeCode");
		if (StringUtils.isEmpty(gradeCode)) {
			result.setSuccess(false);
        	result.setMessage("缺少必传参数");
        	return result ;
		}
		try {
			AppParam params = new AppParam();
			params.setService("abilityGradeCfgService");
			params.setMethod("delete");
			RequestUtil.setAttr(params, request);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "delete error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
