package org.xxjr.store.web.action.account.work;

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
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/***
 * 用户接待处理
 * @author ZQH
 *
 */
@Controller()
@RequestMapping("/account/work/custVisit/")
public class CustVisitAction {
	
	/***
	 * 查询用户到访列表
	 * @return
	 */
	@RequestMapping("queryVisitList")
	@ResponseBody
	public AppResult queryVisitList(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		String custName = request.getParameter("custName");
		String recName = request.getParameter("recName");
		
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			
			if(ValidUtils.validateTelephone(custName)){//加快查询效率
				params.addAttr("telephone", params.removeAttr("custName"));
			}
			if(ValidUtils.validateTelephone(recName)){//加快查询效率
				params.addAttr("receiverTel", params.removeAttr("recName"));
			}
			params.setService("custVisitService");
			params.setMethod("queryVisitList");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(CustVisitAction.class, e, "queryVisitList error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return  result;
	}
	
	
	/***
	 * 关联申请ID至到访列表
	 * @return
	 */
	@RequestMapping("relationDeal")
	@ResponseBody
	public AppResult relationDeal(HttpServletRequest request){
		AppResult result = new AppResult();
		String telephone = request.getParameter("telephone");
		String recordId = request.getParameter("recordId");
	
		if(!ValidUtils.validateTelephone(telephone)){
			result.setSuccess(false);
			result.setMessage("关联手机号为空或不合法");
			return result;
		}else if(StringUtils.isEmpty(recordId)){
			result.setSuccess(false);
			result.setMessage("记录ID不能为空");
			return result;
		}
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("custVisitService");
			params.setMethod("relationDeal");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_busi_in));
			
			result = RemoteInvoke.getInstance().callNoTx(params);
			return result;
			
		}catch(Exception e){
			LogerUtil.error(CustVisitAction.class, e, "relationDeal error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return  result;
	}
	
	
	/***
	 * 转单处理
	 * @return
	 */
	@RequestMapping("checkTransDeal")
	@ResponseBody
	public AppResult checkTransDeal(HttpServletRequest request){
		AppResult result = new AppResult();
		String recordId = request.getParameter("recordId");
		if(StringUtils.isEmpty(recordId)){
			result.setSuccess(false);
			result.setMessage("记录ID不能为空");
			return result;
		}
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("custVisitService");
			params.setMethod("checkTransDeal");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(CustVisitAction.class, e, "relationDeal error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		
		return  result;
	}
	
}
