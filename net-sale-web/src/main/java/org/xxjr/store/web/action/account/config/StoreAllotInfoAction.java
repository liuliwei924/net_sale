package org.xxjr.store.web.action.account.config;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.NumberUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busi.util.store.ApplyInfoUtil;
import org.xxjr.busi.util.store.BusiCustUtil;
import org.xxjr.busi.util.store.CFSDealUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.web.action.account.work.CFSSignDealAction;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/**
 * 分单信息
 * @author zenghw
 *
 */
@Controller
@RequestMapping("/account/config/allotOrderInfo/")
public class StoreAllotInfoAction {
	/**
	 * 分页获取分单信息列表
	 */
	@RequestMapping("queryAllotOrderList")
	@ResponseBody
	public AppResult queryAllotOrderList(HttpServletRequest request) {
		AppResult result = new AppResult();
		String customerId = StoreUserUtil.getCustomerId(request);
		if (StringUtils.isEmpty(customerId)) {
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
			String userName = request.getParameter("realName");
			if(ValidUtils.validateTelephone(userName)){//加快查询效率
				params.addAttr("telephone", params.removeAttr("realName"));
			}
			params.setService("custLevelService");
			params.setMethod("queryAllotOrderList");
			String orderBy = request.getParameter("orderBy");
			String orderValue = request.getParameter("orderValue");
			params.setOrderBy(orderBy);
			params.setOrderValue(orderValue);
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryAllotOrderList error");
			ExceptionUtil.setExceptionMessage(e, result,DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 离职处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("leaveDealWith")
	@ResponseBody
	public AppResult leaveDealWith(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String lastStoreIds = request.getParameter("custIds");
			if (StringUtils.isEmpty(lastStoreIds)) {
				return CustomerUtil.retErrorMsg("门店人员不能为空");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				return CustomerUtil.retErrorMsg("当前登录人不能为空");
			}
			String orgId =  request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				return CustomerUtil.retErrorMsg("门店ID不能为空");
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("离职人用户ID不能为空");
			}
			//门店主管或者经理Id
			String leaderCustId = ApplyInfoUtil.getLeaderCustId(orgId,customerId);
			//创建任务对象调用mq
			StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
			Map<String, Object> msgParam = new HashMap<String, Object>();
			msgParam.put("lastStoreIds", lastStoreIds);
			msgParam.put("custId", custId);
			msgParam.put("orgId", orgId);
			msgParam.put("customerId", customerId);
			msgParam.put("leaderCustId", leaderCustId);
			storeSend.sendStoreMessage(customerId,"leavelDealType" , msgParam);
			// 查询离职用户的信息
			AppParam  queryParam = new AppParam("customerService","query");
			queryParam.addAttr("customerId", customerId);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			AppResult queryResult = RemoteInvoke.getInstance().call(queryParam);
			String openid = "";
			String unionid = "";
			String telephone = "";
			if(queryResult.isSuccess() && queryResult.getRows().size() > 0){
				Map<String,Object> custMap = queryResult.getRow(0);
				openid = StringUtil.getString(custMap.get("openid"));
				unionid = StringUtil.getString(custMap.get("unionid"));
				telephone = StringUtil.getString(custMap.get("telephone"));
			}
			AppParam updateParams  = new AppParam("customerService","newUpdate");
			if(!StringUtils.isEmpty(openid)){
				updateParams.addAttr("openid", customerId + openid);
			}
			if(!StringUtils.isEmpty(unionid)){
				updateParams.addAttr("unionid", customerId + unionid);
			}
			// 在离职人员加上customerId，用于唯一标识
			updateParams.addAttr("customerId", customerId);
			updateParams.addAttr("roleType", "0");
			updateParams.addAttr("telephone", customerId + telephone);
			updateParams.addAttr("teamName", "");
			updateParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(updateParams);
			if(!StringUtils.isEmpty(openid) && !StringUtils.isEmpty(unionid)){
				//注册一个账户给门店新人使用
				AppParam addParams  = new AppParam("customerService","insert");
				addParams.addAttr("userName", "系统注册");
				addParams.addAttr("sourceType", "wx");
				addParams.addAttr("telephone", telephone);
				addParams.addAttr("registerTime", new Date());
				addParams.addAttr("status", "1");
				addParams.addAttr("unionid", unionid);
				addParams.addAttr("openid", openid);
				addParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_cust));
				RemoteInvoke.getInstance().call(addParams);
			}
			//清除用户缓存
			CustomerIdentify.refreshIdentifyById(customerId);
			//刷新其他地方信息
			BusiCustUtil.setBusiCustIn(CustomerIdentify.getCustIdentify(customerId), "");
			BusiCustUtil.setBusiCustSum(CustomerIdentify.getCustIdentify(customerId), "");
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "leaveDealWith error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询能力等级信息
	 */
	@RequestMapping("queryRankList")
	@ResponseBody
	public AppResult queryRankList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try{
			List<Map<String,Object>> rankList = StoreSeparateUtils.getRankConfig();
			result.putAttr("rankList",rankList);
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "queryRankList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}	
		return result;
	}
	
	/**
	 * 修改门店人员员工编号
	 */
	@RequestMapping("updateStoreEmployee")
	@ResponseBody
	public AppResult updateStoreEmployee(HttpServletRequest request) {
		AppResult result = new AppResult();
		try{
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String employeeNo = request.getParameter("employeeNo");
			if (StringUtils.isEmpty(employeeNo)) {
				return CustomerUtil.retErrorMsg("员工编号不能为空");
			}
			
			AppParam queryParams  = new AppParam("busiCustService","query");
			queryParams.addAttr("employeeNo", employeeNo);
			queryParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParams);
			if(queryResult.isSuccess() && queryResult.getRows().size() > 0){
				return CustomerUtil.retErrorMsg("该员工编号已存在，请重新输入！");
			}
			AppParam updateParams  = new AppParam("busiCustService","update");
			updateParams.addAttr("customerId", customerId);
			updateParams.addAttr("employeeNo", employeeNo);
			updateParams.addAttr("queryStatus", "1");
			updateParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParams);
			int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(updateSize > 0){
				updateParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_sum));
				RemoteInvoke.getInstance().call(updateParams);
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "updateStoreEmployee error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}	
		return result;
	}
	
	/**
	 * 批量获取员工编号
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("batchGetEmployeeNo")
	@ResponseBody
	public AppResult batchGetEmployeeNo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> custInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(custInfo)) {
				return CustomerUtil.retErrorMsg("门店人员的基本信息不能为空");
			}
			List<Map<String, Object>> custList = (List<Map<String, Object>>) custInfo
					.get("custInfo");
			if (StringUtils.isEmpty(custList)) {
				return CustomerUtil.retErrorMsg("请传入门店人员的基本信息");
			}
			for (Map<String, Object> custMap : custList) {
				String queryStatus = StringUtil.getString(custMap.get("queryStatus"));
				//1是查询成功
				if("1".equals(queryStatus)){
					continue;
				}
				String customerId = StringUtil.getString(custMap.get("customerId"));
				if(StringUtils.isEmpty(customerId)){
					continue;
				}
				String orgNo = StringUtil.getString(custMap.get("orgNo"));
				if(StringUtils.isEmpty(orgNo)){
					continue;
				}
				String realName = StringUtil.getString(custMap.get("realName"));
				if(StringUtils.isEmpty(realName)){
					continue;
				}
				AppParam params = new AppParam();
				params.addAttr("customerId", customerId);
				params.addAttr("orgNo", orgNo);
				params.addAttr("realName", realName);
				CFSDealUtil.batchGetEmployee(params);
			}
		} catch (Exception e) {
			LogerUtil.error(CFSSignDealAction.class, e, "batchGetEmployeeNo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
