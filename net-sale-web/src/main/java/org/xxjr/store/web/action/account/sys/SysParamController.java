package org.xxjr.store.web.action.account.sys;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xxjr.store.web.action.BaseController;
import org.xxjr.store.web.action.account.user.UserInfoAction;
import org.xxjr.sys.util.ServiceKey;

@RestController
@RequestMapping("/account/config/")
public class SysParamController extends BaseController{
	
	/**
	 * 查询系统参数列表
	 * @param request
	 * @return
	 */
	@RequestMapping("paramset/queryList")
	public AppResult queryList(){
		AppResult result = new AppResult();
		try {	
			AppParam param = new AppParam("sysParamsService","queryByPage");
			RequestUtil.setAttr(param, request);
			param.setOrderBy("createTime");
			param.setOrderValue("DESC");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sys));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "查询系统参数列表错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除系统参数
	 * @param request
	 * @return
	 */
	@RequestMapping("paramset/delete")
	public AppResult delete(){
		AppResult result = new AppResult();
		try {	
			String paramCode = request.getParameter("paramCode");
			if (StringUtils.isEmpty(paramCode)) {
				result.setSuccess(false);
				result.setMessage("paramCode不能为空");
				return result;
			}
			AppParam param = new AppParam("sysParamsService","delete");
			param.addAttr("paramCode", paramCode);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sys));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "删除系统参数错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 修改系统参数
	 * @param request
	 * @return
	 */
	@RequestMapping("paramset/update")
	public AppResult update(){
		AppResult result = new AppResult();
		try {	
			String paramCode = request.getParameter("paramCode");
			if (StringUtils.isEmpty(paramCode)) {
				result.setSuccess(false);
				result.setMessage("请至少选择一项进行修改");
				return result;
			}
			AppParam param = new AppParam("sysParamsService","update");
			RequestUtil.setAttr(param, request);
			param.addAttr("updateBy", "admin");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sys));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "修改系统参数错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 增加系统参数
	 * @param request
	 * @return
	 */
	@RequestMapping("paramset/insert")
	public AppResult insert(){
		AppResult result = new AppResult();
		try {	
			String paramCode = request.getParameter("paramCode");
			String paramValue = request.getParameter("paramValue");
			if (StringUtils.isEmpty(paramValue) || StringUtils.isEmpty(paramCode)) {
				result.setSuccess(false);
				result.setMessage("缺少必要参数");
				return result;
			}
			AppParam param = new AppParam("sysParamsService","insert");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sys));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "增加系统参数错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
