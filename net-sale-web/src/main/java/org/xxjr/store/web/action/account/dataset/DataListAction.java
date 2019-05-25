package org.xxjr.store.web.action.account.dataset;


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
import org.xxjr.busi.util.kf.BorrowApplyUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

@Controller()
@RequestMapping("/account/dataset/")
/**
 * 数据列表
 * @author Administrator
 *
 */
public class DataListAction {

	/**
	 * 录音文件列表
	 * @param request
	 * @return
	 */
	@RequestMapping("callAudio/queryCallAudio")
	@ResponseBody
	public AppResult queryCallAudio(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String customerId = StoreUserUtil.getCustomerId(request);
			params.addAttr("audioType", "1");
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			String storeRealName = StringUtil.getString(params.getAttr("storeRealName"));
			if(ValidUtils.validateTelephone(storeRealName)){ //验证是否是手机号
				params.addAttr("borrowTel",params.removeAttr("storeRealName"));
			}
			params.setService("storeCallAudioService");
			params.setMethod("queryCallAudio");
			params.setOrderBy("startCallTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "获取录音文件出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 *  查询所有申请列表
	 * @param request
	 * @return
	 */
	@RequestMapping("allApplyCount/queryAllApplayList")
	@ResponseBody
	public AppResult queryAllApplayList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String applyName = request.getParameter("applyName");
			if(ValidUtils.validateTelephone(applyName)){//加快查询效率
				params.addAttr("telephone", applyName);
				params.removeAttr("applyName");
			}
			//开启渠道类型标识
			int openStoreChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
			if(openStoreChannelFlag == 1){
				//设置查询渠道类型
				String queryChannelType = SysParamsUtil.getStringParamByKey("storeQueryChannelType", "2");
				params.addAttr("queryChannelType", queryChannelType);
			}
			params.addAttr("joinStatus", 1);//不包含已经转化成功的数据
			result = BorrowApplyUtils.queryBorrowList(params);
			params.setService("borrowApplyService");
			params.setMethod("queryAllApplayList");
			params.setOrderBy("applyTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询所有申请列表出错！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 *  查询所有外部渠道人员
	 * @param request
	 * @return
	 */
	@RequestMapping("allApplyCount/queryExternalUser")
	@ResponseBody
	public AppResult queryExternalUser(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("roleType", 10);//外部渠道人员
			params.setService("customerService");
			params.setMethod("query");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询所有外部渠道人员！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 *  查询不合规操作列表
	 * @param request
	 * @return
	 */
	@RequestMapping("violateOrder/queryViolateOrderList")
	@ResponseBody
	public AppResult queryViolateOrderList(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			AppParam param = new AppParam();
			param.setService("orderViolateRecordService");
			param.setMethod("queryShow");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("createTime");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "查询不合规操作列表！");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 命中多头借贷列表查询
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/riskList/queryRiskAllList")
	@ResponseBody
	public AppResult queryRiskAllList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			String customerId = StoreUserUtil.getCustomerId(request);
			if(StringUtils.isEmpty(customerId)){
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			AppParam params=new AppParam("storeHandleExtService","queryRiskAllList");
			RequestUtil.setAttr(params, request);
			params.addAttr("status", "2");//门店锁定
			params.addAttr("muLoanStatus", "4");//命中多头借贷
			StoreUserUtil.dealUserAuthParam(params, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey) && ValidUtils.validateTelephone(searchKey) ){
				params.addAttr("telephone", searchKey);
				params.removeAttr("searchKey");
			}else{
				params.addAttr("applyName", searchKey);
				params.removeAttr("searchKey");
			}
			params.setOrderBy("lastUpdateTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "queryRiskAllList error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}
}
