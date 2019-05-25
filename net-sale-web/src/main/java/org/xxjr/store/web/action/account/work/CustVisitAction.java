package org.xxjr.store.web.action.account.work;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.CFSUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
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
			if(result.getRows().size() > 0){
				for(Map<String,Object> map : result.getRows()){
					boolean orgCFSFlag = StoreApplyUtils.isHaveAuthUpCFS(StringUtil.getString(map.get("orgId")));
					map.put("orgCFSFlag", orgCFSFlag);
				}
			}
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
	
	/**
	 * 访客记录上传CFS
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("uploadCFS")
	@ResponseBody
	public AppResult uploadCFS(HttpServletRequest request) {
		AppResult result = new AppResult();
		int autoAddVisitToCFSFlag = SysParamsUtil.getIntParamByKey("autoAddVisitToCFSFlag", 1);
		if(autoAddVisitToCFSFlag == 0){
			result.setMessage("添加访客记录上传到CFS暂未开启!");
			result.setSuccess(false);
			return result;
		}
		try {
			String recordId = request.getParameter("recordId");
			String detailId = request.getParameter("detailId");
			if (StringUtils.isEmpty(recordId) && StringUtils.isEmpty(detailId)) {
				return CustomerUtil.retErrorMsg("记录ID不能为空");
			}
			String upStatus = request.getParameter("upStatus");
			if (StringUtils.isEmpty(upStatus)) {
				return CustomerUtil.retErrorMsg("上传状态不能为空");
			}
			if("2".equals(upStatus)){
				return CustomerUtil.retErrorMsg("已上传成功的无需再次上传");
			}
			String orgNo = "";
			String custName = "";
			String custTel = "";
			String receiverTel = "";
			String realName = "";
			String visitTime = "";
			AppParam params = new AppParam("custVisitService","queryVisitByRecordId");
			//查询访客登记
			if(!StringUtils.isEmpty(recordId)){
				params.addAttr("recordId", recordId);
				params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
				if(result.getRows().size() > 0 && !StringUtils.isEmpty(result.getRow(0))){
					Map<String,Object> queryMap = result.getRow(0);
					orgNo = StringUtil.getString(queryMap.get("orgNo"));
					custName= StringUtil.getString(queryMap.get("custName"));
					custTel= StringUtil.getString(queryMap.get("custTel"));
					receiverTel = StringUtil.getString(queryMap.get("receiverTel"));
					realName = StringUtil.getString(queryMap.get("realName"));
					visitTime = StringUtil.getString(queryMap.get("createTime"));
				}
			}else{//查询手动添加上门
				params.addAttr("detailId", detailId);
				params.setService("treatVisitDetailService");
				params.setMethod("queryHandleVisit");
				params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(params);
				if(result.getRows().size() > 0 && !StringUtils.isEmpty(result.getRow(0))){
					Map<String,Object> queryMap = result.getRow(0);
					orgNo = StringUtil.getString(queryMap.get("orgNo"));
					custName= StringUtil.getString(queryMap.get("applyName"));
					custTel= StringUtil.getString(queryMap.get("telephone"));
					receiverTel = StringUtil.getString(queryMap.get("receiverTel"));
					realName = StringUtil.getString(queryMap.get("realName"));
					visitTime = StringUtil.getString(queryMap.get("visitTime"));
				}
			}
			AppParam addparams = new AppParam();
			addparams.addAttr("orgNo", orgNo);
			addparams.addAttr("custName", custName);
			addparams.addAttr("custTel", custTel);
			addparams.addAttr("receiverTel", receiverTel);
			addparams.addAttr("realName", realName);
			addparams.addAttr("loanType", "0");
			addparams.addAttr("visitTime", visitTime);
			addparams.addAttr("recordId", recordId);
			addparams.addAttr("detailId", detailId);
			addparams.addAttr("employeeNo", request.getParameter("employeeNo"));
			Map<String,Object> resultMap = CFSUtil.addVisitToCFS(addparams);
			String messageCode = StringUtil.getString(resultMap.get("MessageCode"));
			String returnMsg = StringUtil.getString(resultMap.get("Message"));
			if("200".equals(messageCode)){
				return result;
			}else{
				result.setSuccess(false);
				result.setMessage(returnMsg);
				return result;
			}
		} catch (Exception e) {
			LogerUtil.error(CustVisitAction.class, e, "uploadCFS");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
