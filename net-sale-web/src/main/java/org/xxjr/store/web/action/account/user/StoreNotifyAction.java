package org.xxjr.store.web.action.account.user;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/***
 * 门店人员系统通知
 * @author ZQH
 *
 */
@Controller
@RequestMapping("/account/user/notify/")
public class StoreNotifyAction {

	/***
	 * 获取门店人员的未读系统通知
	 * @param request
	 * @return
	 */
	@RequestMapping("queryNotifyList")
	@ResponseBody
	public AppResult queryNotifyList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			
			String customerId=StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("customerId", customerId);
			int currentPage = NumberUtil.getInt(params.getCurrentPage(),1);
			if(currentPage == 1){
				result = StoreApplyUtils.getCustNotifyInfo(params);
			}else{
				params.setService("infoNotifyService");
				params.setMethod("queryNotifyList");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreNotifyAction.class,e, "queryNotifyList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 获取门店人员所有需要的通知
	 * @param request
	 * @return
	 */
	@RequestMapping("queryNotifyAllList")
	@ResponseBody
	public AppResult queryNotifyAllList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			
			String customerId=StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			AppParam params = new AppParam();
			params.addAttr("customerId", customerId);
			result = StoreApplyUtils.getCustAllNotifyInfo(params);
		}catch(Exception e){
			LogerUtil.error(StoreNotifyAction.class,e, "queryNotifyAllList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/***
	 * 获取门店人员已读系统通知
	 * @param request
	 * @return
	 */
	@RequestMapping("queryOldNotifyList")
	@ResponseBody
	public AppResult queryOldNotifyList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			
			String customerId=StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("customerId", customerId);
			int currentPage = NumberUtil.getInt(params.getCurrentPage(),1);
			if(currentPage == 1){
				result = StoreApplyUtils.getCustFinishNotifyInfo(params);
			}else{
				params.setService("infoNotifyFishService");
				params.setMethod("queryOldNotifyList");
				params.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreNotifyAction.class,e, "queryOldNotifyList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 处理系统通知
	 * @param request
	 * @return
	 */
	@RequestMapping("dealNotify")
	@ResponseBody
	public AppResult dealNotify(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			
			String notifyIds = request.getParameter("notifyIds");
			if(StringUtils.isEmpty(notifyIds)){
				result.setSuccess(false);
				result.setMessage("通知编号不能为空");
				return result;
			}
			String currentCustId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(currentCustId)){
				result.setSuccess(false);
				result.setMessage("用户Id不能为空");
				return result;
			}
			AppParam params = new AppParam();
			params.addAttr("notifyIds", notifyIds);
			params.addAttr("currentCustId", currentCustId);
			params.setService("infoNotifyService");
			params.setMethod("dealNotify");
			params.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			
			result = RemoteInvoke.getInstance().callNoTx(params);
			
		}catch(Exception e){
			LogerUtil.error(StoreNotifyAction.class,e, "dealNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/***
	 * 获取门店人员关闭分单描述
	 * @param request
	 * @return
	 */
	@RequestMapping("queryCloseAllotDesc")
	@ResponseBody
	public AppResult queryCloseAllotDesc(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			
			String customerId=StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户信息不存在");
				return result;
			}
			
			AppParam params = new AppParam();
			params.addAttr("customerId", customerId);
			params.setService("custLevelService");
			params.setMethod("queryCloseAllotDesc");
			params.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			
			result = RemoteInvoke.getInstance().callNoTx(params);
			
		}catch(Exception e){
			LogerUtil.error(StoreNotifyAction.class,e, "quaryColeseAllotDesc error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
