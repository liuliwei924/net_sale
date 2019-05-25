package org.xxjr.store.web.action.account.work;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.ApplyInfoUtil;
import org.xxjr.busi.util.store.CFSDealUtil;
import org.xxjr.busi.util.store.CFSUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/**
 * CFS签单相关处理
 * 
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/work/cfsSign/")
public class CFSSignDealAction {
	/**
	 * 关联签单合同信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("relationSignInfo")
	@ResponseBody
	public AppResult relationSignInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String telephone = request.getParameter("telephone");
			if(StringUtils.isEmpty(telephone)){
				return CustomerUtil.retErrorMsg("客户手机号码不能为空");
			}
			String applyName = request.getParameter("applyName");
			if(StringUtils.isEmpty(applyName)){
				return CustomerUtil.retErrorMsg("客户姓名不能为空");
			}
			String reContractId = request.getParameter("reContractId");
			if(StringUtils.isEmpty(reContractId)){
				return CustomerUtil.retErrorMsg("合同编号不能为空");
			}
			String signDate = request.getParameter("signDate");
			if(StringUtils.isEmpty(signDate)){
				return CustomerUtil.retErrorMsg("签单时间不能为空");
			}
			
			String custId = StoreUserUtil.getCustomerId(request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(custId);
			String roleType = "";
			if (custInfo != null && custInfo.size() > 0) {
				roleType = StringUtil.getString(custInfo.get("roleType"));
			}
			//CFS查询状态
			String status = request.getParameter("status");
			//1是成功处理
			if("1".equals(status) && !"1".equals(roleType)){
				return CustomerUtil.retErrorMsg("该合同已关联成功无需再关联！");
			}
			AppParam queryParams = new AppParam("treatInfoHistoryService","query");
			queryParams.addAttr("applyName", applyName);
			queryParams.addAttr("telephone", telephone);
			queryParams.setOrderBy("createTime");
			queryParams.setOrderValue("desc");
			queryParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult appResult = RemoteInvoke.getInstance().call(queryParams);
			Map<String,Object> queryMap = null;
			AppParam updateOrigin = new AppParam("treatOriginInfoService","update");
			updateOrigin.addAttr("reContractId", reContractId);
			updateOrigin.addAttr("dealDate", new Date());
			if(appResult.getRows().size() > 0 && !StringUtils.isEmpty(appResult.getRow(0))){
				queryMap = appResult.getRow(0);
				String applyId = StringUtil.getString(queryMap.get("applyId"));
				AppParam upadateParams = new AppParam("treatInfoService","update");
				upadateParams.addAttr("applyId", applyId);
				upadateParams.addAttr("upStatus", 2);
				upadateParams.addAttr("errorMessage", "");
				upadateParams.addAttr("reContractId", reContractId);
				upadateParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				
				AppParam historyParams = new AppParam("treatInfoHistoryService","updateHistory");
				historyParams.addAttr("applyId", applyId);
				historyParams.addAttr("upStatus", 2);
				historyParams.addAttr("errorMessage", "");
				historyParams.addAttr("reContractId", reContractId);
				historyParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				if(appResult.getRows().size() == 1){
					RemoteInvoke.getInstance().call(upadateParams);
					RemoteInvoke.getInstance().call(historyParams);
				}else{
					AppParam orginParams = new AppParam("treatOriginInfoService","queryCount");
					orginParams.addAttr("applyName", applyName);
					orginParams.addAttr("telephone", telephone);
					orginParams.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult orginResult = RemoteInvoke.getInstance().call(orginParams);
					int count = 0;
					if(!StringUtils.isEmpty(orginResult.getAttr(DuoduoConstant.TOTAL_SIZE))){
						count = NumberUtil.getInt(orginResult.getAttr(DuoduoConstant.TOTAL_SIZE));
					}
					
					if(count > 1){
						upadateParams.setMethod("updateSign");
						upadateParams.addAttr("signTime", signDate);
						RemoteInvoke.getInstance().call(upadateParams);
						
						historyParams.setMethod("updateSign");
						historyParams.addAttr("signTime", signDate);
						RemoteInvoke.getInstance().call(historyParams);
					}else{
						RemoteInvoke.getInstance().call(upadateParams);
						RemoteInvoke.getInstance().call(historyParams);
					}
					
				}
				
				updateOrigin.addAttr("status", 1); //处理成功
				updateOrigin.addAttr("errorMessage", "");
				updateOrigin.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(updateOrigin);
				
				String customerId = StringUtil.getString(queryMap.get("customerId"));
				String orgId = StringUtil.getString(queryMap.get("orgId"));
				AppParam param = new AppParam();
				param.addAttr("reContractId", reContractId);
				param.addAttr("customerId", customerId);
				param.addAttr("applyId", applyId);
				param.addAttr("orgId", orgId);
				Map<String, Object> resultMap = CFSUtil.getContractInfo(param);
				String executeResult = StringUtil.getString(resultMap.get("ExecuteResult"));
				String resturnMsg = StringUtil.getString(resultMap.get("ReturnMsg"));
				String errorMsg = StringUtil.getString(resultMap.get("errorMsg"));
				if("true".equals(executeResult)){
					return result;
				}else{
					result.setMessage(errorMsg);
					if(StringUtils.isEmpty(errorMsg)) {
						result.setMessage(resturnMsg);
					}
					result.setSuccess(Boolean.FALSE);
				}
				
			}else{
				updateOrigin.addAttr("status", 2); //处理失败
				updateOrigin.addAttr("errorMessage", "关联失败，暂无相关合同信息");
				updateOrigin.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(updateOrigin);
				
				result.setSuccess(false);
				result.setMessage("关联失败，暂无相关合同信息");
			}
			
		} catch (Exception e) {
			LogerUtil.error(CFSSignDealAction.class, e, "relationSignInfo");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 批量关联
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("batchRelation")
	@ResponseBody
	public AppResult batchRelation(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				result.setSuccess(false);
				result.setMessage("订单的基本信息不能为空");
				return result;
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				result.setSuccess(false);
				result.setMessage("请传入订单的基本信息");
				return result;
			}
			for (Map<String, Object> orderMap : orders) {
				String status = StringUtil.getString(orderMap.get("status"));
				//1是成功处理
				if("1".equals(status)){
					continue;
				}
				String telephone = StringUtil.getString(orderMap.get("telephone"));
				if(StringUtils.isEmpty(telephone)){
					continue;
				}
				String applyName = StringUtil.getString(orderMap.get("applyName"));
				if(StringUtils.isEmpty(applyName)){
					continue;
				}
				String reContractId = StringUtil.getString(orderMap.get("reContractId"));
				if(StringUtils.isEmpty(reContractId)){
					continue;
				}
				String signDate = StringUtil.getString(orderMap.get("signDate"));
				if(StringUtils.isEmpty(signDate)){
					continue;
				}
				AppParam params = new AppParam();
				params.addAttr("telephone", telephone);
				params.addAttr("applyName", applyName);
				params.addAttr("reContractId", reContractId);
				params.addAttr("signDate", orderMap.get("signDate"));
				CFSDealUtil.batchRelationDeal(params);
			}
		} catch (Exception e) {
			LogerUtil.error(CFSSignDealAction.class, e, "batchRelation error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 同步CFS回款信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("updateCFSBackInfo")
	@ResponseBody
	public AppResult updateCFSBackInfo(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			int queryCount = NumberUtil.getInt(RedisUtils.getRedisService().get(
					CFSUtil.CacheKey_CFS_BACK_COUNT + customerId), 0);
			if(queryCount >= 3){
				return CustomerUtil.retErrorMsg("一天只允许同步CFS回款3次，请勿频繁操作！");
			}
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null && custInfo.size() > 0){
				String authType = StringUtil.getString(custInfo.get("roleType")); //角色
				//管理员才有同步回款权限
				if(!CustConstant.CUST_ROLETYPE_1.equals(authType)){
					return CustomerUtil.retErrorMsg("您没有权限同步CFS回款");
				}
			}
			String startDate = request.getParameter("startDate");
			if(StringUtils.isEmpty(startDate)){
				return CustomerUtil.retErrorMsg("请选择开始查询日期");
			}else{
				result = ApplyInfoUtil.compareDateInThisMonth(startDate);
				if(!result.isSuccess()){
					return result;
				}
			}
			String endDate = request.getParameter("endDate");
			if(StringUtils.isEmpty(endDate)){
				return CustomerUtil.retErrorMsg("请选择结束查询日期");
			}else{
				result = ApplyInfoUtil.compareDateInThisMonth(endDate);
				if(!result.isSuccess()){
					return result;
				}
			}
			AppParam queryParam = new AppParam("storeListOptExtService", "queryAllCfsSignList");
			queryParam.addAttr("startDate", startDate);
			queryParam.addAttr("endDate", endDate);
			//1处理成功
			queryParam.addAttr("status", "1");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			if(queryResult.getRows().size() > 0 ){
				AppParam applyParam = new AppParam("treatInfoService", "query");
				for(Map<String,Object> queryMap : queryResult.getRows()){
					String reContractId = StringUtil.getString(queryMap.get("reContractId"));
					if(StringUtils.isEmpty(reContractId)){
						continue;
					}
					String applyId = StringUtil.getString(queryMap.get("applyId"));
					if(StringUtils.isEmpty(applyId)){
						continue;
					}
					applyParam.addAttr("applyId", applyId);
					applyParam.addAttr("reContractId", reContractId);
					applyParam.addAttr("upStatus", "2"); //查询已上传
					applyParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
					if(applyResult.isSuccess() && !StringUtils.isEmpty(applyResult.getRow(0))){
						Map<String,Object> applyMap = applyResult.getRow(0);
						String lastStore = StringUtil.getString(applyMap.get("customerId"));
						if(StringUtils.isEmpty(lastStore)){
							continue;
						}
						Map<String, Object> currenDeal = CustomerIdentify.getCustIdentify(lastStore);
						String orgId = StringUtil.getString(currenDeal.get("orgId"));
						AppParam param = new AppParam();
						param.addAttr("reContractId", reContractId);
						param.addAttr("customerId", lastStore);
						param.addAttr("applyId", applyId);
						param.addAttr("orgId", orgId);
						CFSUtil.getContractInfo(param);
					}
					applyResult = null;
				}
				queryCount ++;
				//设置同步CFS回款次数缓存
				long startTime = System.currentTimeMillis();
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				long endTime = cal.getTimeInMillis();
				int queryTime = NumberUtil.getInt((endTime - startTime) / 1000,0);
				/**
				 * 设置缓存
				 */
				RedisUtils.getRedisService().set(CFSUtil.CacheKey_CFS_BACK_COUNT + customerId,(Serializable) queryCount, queryTime);
			}else{
				return CustomerUtil.retErrorMsg("暂无相关回款数据");
			}
			
		} catch (Exception e) {
			LogerUtil.error(CFSSignDealAction.class, e, "updateCFSBackInfo");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}