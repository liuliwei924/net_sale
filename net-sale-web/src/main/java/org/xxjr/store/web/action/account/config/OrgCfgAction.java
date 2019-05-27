package org.xxjr.store.web.action.account.config;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/**
 * 门店配置
 * @author liulw
 *
 */
@Controller()
@RequestMapping("/account/config/orgCfg/")
public class OrgCfgAction {

	/**
	 * 查询门店配置
	 * @param request
	 * @return
	 */
	@RequestMapping("queryList")
	@ResponseBody
	public AppResult queryList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
	
			AppParam params = new AppParam("orgService","queryByPage");
			RequestUtil.setAttr(params, request);
			params.setOrderBy("createTime");
			params.setOrderValue("DESC");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 新增门店配置
	 * @param request
	 * @return
	 */
	@RequestMapping("add")
	@ResponseBody
	public AppResult add(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			
			if(StringUtils.isEmpty(params.getAttr("orgNo")) 
					|| StringUtils.isEmpty(params.getAttr("orgName"))
					|| StringUtils.isEmpty(params.getAttr("cityName"))) {
				
				result.setSuccess(false);
				result.setMessage("门店编号，门店名称，城市 缺一不可!");
				return result;
			}
			String managerTel = StringUtil.getString(params.getAttr("managerTel"));
			if(!StringUtils.isEmpty(managerTel) &&
					!ValidUtils.validateTelephone(managerTel)) {
				result.setSuccess(false);
				result.setMessage("负责人手机格式不正确");
				return result;
			}
			
			params.setService("orgService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			int	count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE),0);
			if(count > 0){
				// 刷新缓存
				OrgUtils.refreshOrgList();
				
				Object orgId = result.getAttr("orgId");
				//同步 busiIn，summary 信息
				params.setService("busiinOrgService");
				params.addAttr("orgId", orgId);
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(params);
				
				params.setService("sumOrgService");
				params.addAttr("orgId", orgId);
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				RemoteInvoke.getInstance().call(params);
				
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "add error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 更新门店配置
	 * @param request
	 * @return
	 */
	@RequestMapping("update")
	@ResponseBody
	public AppResult update(HttpServletRequest request){
		AppResult result = new AppResult();
		String orgId = request.getParameter("orgId");
		if (StringUtils.isEmpty(orgId)) {
			result.setSuccess(false);
			result.setMessage("缺少必要参数");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			
			if(StringUtils.isEmpty(params.getAttr("orgNo")) 
					|| StringUtils.isEmpty(params.getAttr("orgName"))
					|| StringUtils.isEmpty(params.getAttr("cityName"))) {
				
				result.setSuccess(false);
				result.setMessage("门店编号，门店名称，城市 缺一不可!");
				return result;
			}
			String managerTel = StringUtil.getString(params.getAttr("managerTel"));
			if(!StringUtils.isEmpty(managerTel) &&
					!ValidUtils.validateTelephone(managerTel)) {
				result.setSuccess(false);
				result.setMessage("负责人手机格式不正确");
				return result;
			}
			
			params.setService("orgService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			int	count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(count > 0){
				// 刷新缓存
				OrgUtils.refreshOrgList();
				//同步 busiIn，summary 信息
				params.setService("busiinOrgService");
				params.addAttr("orgId", orgId);
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(params);
				
				params.setService("sumOrgService");
				params.addAttr("orgId", orgId);
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				RemoteInvoke.getInstance().call(params);
				
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "update error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
}
