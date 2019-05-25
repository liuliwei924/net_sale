package org.xxjr.busi.util.store;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/**
 * CFS相关处理工具类
 * @author Administrator
 *
 */
public class CFSDealUtil {

	/**
	 * 上传CFS处理
	 * @param treatyNo
	 * @param applyId
	 * @return
	 */
	public static AppResult upLoadCFSDeal(String treatyNo,String applyId,Map<String,Object> queryMap){
		AppResult result = new AppResult();
		if(StringUtils.isEmpty(queryMap)) {
			AppParam queryParam = new AppParam();
			queryParam.addAttr("treatyNo", treatyNo);
			queryParam.addAttr("applyId", applyId);
			queryParam.setService("treatInfoService");
			queryParam.setMethod("query");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			if(queryResult.getRows().size() > 0 && !queryResult.getRow(0).isEmpty()){
				queryMap = queryResult.getRow(0);
			}else{
				result.setSuccess(Boolean.FALSE);
				result.setMessage("不存在此合同编号的资料信息!");
				return result;
			}
			String upStatus = StringUtil.getString(queryMap.get("upStatus"));
			
			if(!StringUtils.isEmpty(upStatus) && "2".equals(upStatus)){
				result.setSuccess(Boolean.FALSE);
				result.setMessage("CFS已上传，请勿重新上传!");
				return result;
			}
		}
		
		String seviceName = StringUtil.getString(queryMap.get("realName"));//签约经理
		String companyID = StringUtil.getString(queryMap.get("orgNo"));
		String createTime = StringUtil.getString(queryMap.get("createTime"));
		Date createDate = DateUtil.toDateByString(createTime, 
				DateUtil.DATE_PATTERN_YYYY_MM_DD);
		String contractDate = DateUtil.toStringByParttern(createDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);//签约日期
		String name = StringUtil.getString(queryMap.get("applyName"));//客户姓名
		String mobile = StringUtil.getString(queryMap.get("telephone"));//客户手机
		String idCode = StringUtil.getString(queryMap.get("cardNo"));//客户身份证
		String employeeNo = StringUtil.getString(queryMap.get("employeeNo"));//员工编号
		if(StringUtils.isEmpty(applyId)){
			applyId = StringUtil.getString(queryMap.get("applyId"));
		}
		if(StringUtils.isEmpty(treatyNo)){
			treatyNo = StringUtil.getString(queryMap.get("treatyNo"));
		}
		AppParam param = new AppParam();
		param.addAttr("treatyNo", treatyNo);
		param.addAttr("applyId", applyId);
		param.addAttr("SeviceName", seviceName);
		param.addAttr("CompanyID", companyID);
		param.addAttr("ContractDate", contractDate);
		param.addAttr("Name", name);
		param.addAttr("Mobile", mobile);
		param.addAttr("IdCode", idCode);
		param.addAttr("CardType", "1");
		param.addAttr("EmployeeNo", employeeNo);
		Map<String, Object> resultMap = CFSUtil.uploadInfoToCFS(param);
		String executeResult = StringUtil.getString(resultMap.get("ExecuteResult"));
		String returnMsg = StringUtil.getString(resultMap.get("ReturnMsg"));
		String errorMsg = StringUtil.getString(resultMap.get("errorMsg"));
		if("true".equals(executeResult)){
			result.setMessage("上传CFS成功");
		}else{
			result.setMessage(errorMsg);
			if(StringUtils.isEmpty(errorMsg)) {
				result.setMessage(returnMsg);
			}
			result.setSuccess(Boolean.FALSE);
		}
		return result;
	}
	
	/**
	 * 批量关联相关处理
	 * @param treatyNo
	 * @param applyId
	 * @return
	 */
	public static void batchRelationDeal(AppParam params){
		String applyName = StringUtil.getString(params.getAttr("applyName"));
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		String reContractId = StringUtil.getString(params.getAttr("reContractId"));
		String signDate = StringUtil.getString(params.getAttr("signDate"));
		AppParam queryParams = new AppParam("treatInfoHistoryService","query");
		queryParams.addAttr("applyName", applyName);
		queryParams.addAttr("telephone", telephone);
		queryParams.setOrderBy("createTime");
		queryParams.setOrderValue("desc");
		queryParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult appResult = RemoteInvoke.getInstance().callNoTx(queryParams);
		Map<String,Object> queryMap = null;
		AppParam updateOrigin = new AppParam("treatOriginInfoService","update");
		updateOrigin.addAttr("reContractId", reContractId);
		updateOrigin.addAttr("dealDate", new Date());
		//关联已签单的订单
		if(appResult.getRows().size() > 0 && !StringUtils.isEmpty(appResult.getRow(0))){
			queryMap = appResult.getRow(0);
			String applyId = StringUtil.getString(queryMap.get("applyId"));
			//更新签单主表
			AppParam upadateParams = new AppParam("treatInfoService","update");
			upadateParams.addAttr("applyId", applyId);
			upadateParams.addAttr("upStatus", 2);
			upadateParams.addAttr("errorMessage", "");
			upadateParams.addAttr("reContractId", reContractId);
			upadateParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			//更新签单历史表
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
				//查询同一个客户是否有多笔签单合同
				AppParam orginParams = new AppParam("treatOriginInfoService","queryCount");
				orginParams.addAttr("applyName", applyName);
				orginParams.addAttr("telephone", telephone);
				orginParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult orginResult = RemoteInvoke.getInstance().callNoTx(orginParams);
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
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String orgId = StringUtil.getString(custInfo.get("orgId"));
			//获取相关合同
			AppParam param = new AppParam();
			param.addAttr("reContractId", reContractId);
			param.addAttr("customerId", customerId);
			param.addAttr("applyId", applyId);
			param.addAttr("orgId", orgId);
			CFSUtil.getContractInfo(param);
		}else{
			// 关联未签单的订单
			AppParam applyParams = new AppParam("borrowStoreApplyService","query");
			applyParams.addAttr("applyName", applyName);
			applyParams.addAttr("telephone", telephone);
			applyParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParams);
			Map<String,Object> applyMap = null;
			if(applyResult.getRows().size() > 0 && !StringUtils.isEmpty(applyResult.getRow(0))){
				AppParam treatParams = new AppParam("storeOptExtService","signDeal");
				applyMap = applyResult.getRow(0);
				String applyId = StringUtil.getString(applyMap.get("applyId"));
				String customerId = StringUtil.getString(applyMap.get("lastStore"));
				AppParam baseParams = new AppParam("borrowBaseService","query");
				baseParams.addAttr("applyId", applyId);
				baseParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult baseResult = RemoteInvoke.getInstance().callNoTx(baseParams);
				if(baseResult.getRows().size() > 0){
					String identifyNo = StringUtil.getString(baseResult.getRow(0).get("identifyNo"));
					treatParams.addAttr("cardNo", StringUtils.isEmpty(identifyNo) ? "" : identifyNo);
				}
				treatParams.addAttr("applyId", applyId);
				treatParams.addAttr("signTime",signDate);
				treatParams.addAttr("applyName", applyMap.get("applyName"));
				treatParams.addAttr("telephone", applyMap.get("telephone"));
				treatParams.addAttr("reContractId", reContractId);
				treatParams.addAttr("customerId", customerId);
				treatParams.addAttr("upStatus",2); // 2 已上传
				treatParams.addAttr("errorMessage", "");
				treatParams.addAttr("flag", 1); //不需要再上传CFS标志
				treatParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult result = RemoteInvoke.getInstance().call(treatParams);
				if(result.isSuccess()){
					updateOrigin.addAttr("status", 1); //处理成功
					updateOrigin.addAttr("errorMessage", "");
					updateOrigin.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					RemoteInvoke.getInstance().call(updateOrigin);
					
					Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
					String orgId =StringUtil.getString(custInfo.get("orgId"));
					//获取合同信息
					AppParam param = new AppParam();
					param.addAttr("reContractId", reContractId);
					param.addAttr("customerId", customerId);
					param.addAttr("applyId", applyId);
					param.addAttr("orgId", orgId);
					CFSUtil.getContractInfo(param);
				}else{
					updateOrigin.addAttr("status", 2); //处理失败
					updateOrigin.addAttr("errorMessage", result.getMessage());
					updateOrigin.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					RemoteInvoke.getInstance().call(updateOrigin);
				}

			}else{
				updateOrigin.addAttr("status", 2); //处理失败
				updateOrigin.addAttr("errorMessage", "关联失败，暂无相关订单信息");
				updateOrigin.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(updateOrigin);
			}
		}
	}
	/**
	 * 批量获取员工编号
	 * @param treatyNo
	 * @param applyId
	 * @return
	 */
	public static void batchGetEmployee(AppParam params){
		String employeeNo = CFSUtil.getStoreEmployeeNo(params);
		//更新门店人员员工编号
		AppParam updateParams = new AppParam("busiCustService","update");
		updateParams.addAttr("customerId", params.getAttr("customerId"));
		if (employeeNo.contains(",") || StringUtils.isEmpty(employeeNo)) {
			updateParams.addAttr("employeeNo", StringUtils.isEmpty(employeeNo) ? null : employeeNo);
			updateParams.addAttr("queryStatus", "2");
		}else{
			updateParams.addAttr("employeeNo", employeeNo);
			updateParams.addAttr("queryStatus", "1");
		}
		updateParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
				+ ServiceKey.Key_busi_in));
		AppResult updateResult = RemoteInvoke.getInstance().call(updateParams);
		int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
		if(updateSize > 0){
			updateParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
					+ ServiceKey.Key_sum));
			RemoteInvoke.getInstance().call(updateParams);
		}
	}
}
