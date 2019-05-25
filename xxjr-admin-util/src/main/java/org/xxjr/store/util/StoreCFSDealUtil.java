package org.xxjr.store.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/**
 * CFS相关处理工具类1
 * @author Administrator
 *
 */
public class StoreCFSDealUtil {
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
			AppResult queryResult = null;
			if (SpringAppContext.getBean("treatInfoService") == null) {
				queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			}else{
				queryResult = SoaManager.getInstance().invoke(queryParam);
			}
			
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
		String companyID = StringUtil.getString(queryMap.get("orgNo"));//签约门店
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
		Map<String, Object> resultMap = uploadInfoToCFS(param);
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
	 * 上传CFS
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> uploadInfoToCFS(AppParam param){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Map<String, Object> params = new HashMap<String, Object>();
		String[] paramList = new String[1];
		
		Map<String, Object> paramString = new HashMap<String, Object>();
		paramString.put("SeviceName", param.getAttr("SeviceName"));
		paramString.put("CompanyID", param.getAttr("CompanyID"));
		paramString.put("ContractDate",param.getAttr("ContractDate"));
		paramString.put("EmployeeNo",param.getAttr("EmployeeNo"));
		List<Map<String,Object>> customerList = new ArrayList<Map<String,Object>>();
		Map<String, Object> customers = new HashMap<String, Object>();
		customers.put("Name", param.getAttr("Name"));
		customers.put("Mobile", param.getAttr("Mobile"));
		customers.put("IdCode", param.getAttr("IdCode"));
		customers.put("CardType", param.getAttr("CardType"));
		customerList.add(customers);

		paramString.put("Customers", customerList);
		paramList[0] = JsonUtil.getInstance().object2JSON(paramString);
		
		params.put("ParamString", paramList);
		params.put("TranName", "AddContractBriefInfoTG");
		String jsonstr = JsonUtil.getInstance().object2JSON(params);
		LogerUtil.log("rewqet data ="+jsonstr);
		String reStrJoson = JsonUtil.getInstance().object2JSON(jsonstr);
		CloseableHttpClient client = null;
		CloseableHttpResponse httpResponse = null;
		try {
			client = HttpsClinetTrustAnyUtil.getInstance().getHttpsClient();
			String CFS_DUI_URL = SysParamsUtil.getStringParamByKey("cfs_request_url", "https://www.xxxxjs.cn:2568/DuiService/Tran1");
			LogerUtil.log("CFS请求地址：" + CFS_DUI_URL);
			HttpPut httpPost = new HttpPut(CFS_DUI_URL);
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("xxjrtg", "xxjrtg");
			StringEntity stringEntity = new StringEntity(reStrJoson,"utf-8");
			httpPost.setEntity(stringEntity);
			httpResponse = client.execute(httpPost);
			String treatyNo = StringUtil.getString(param.getAttr("treatyNo"));
			String applyId = StringUtil.getString(param.getAttr("applyId"));
			AppParam updateParam = new AppParam("treatInfoService","updateSignInfo");
			updateParam.addAttr("applyId", applyId);
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppParam historyParam = new AppParam("treatInfoHistoryService","update");
			historyParam.addAttr("treatyNo", treatyNo);
			historyParam.addAttr("applyId", applyId);
			historyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = httpResponse.getEntity();
				String resString =  StringUtil.decodeStr(EntityUtils.toString(entity, Consts.UTF_8));
				EntityUtils.consume(entity);
				resultMap = JsonUtil.getInstance().json2Object(resString, Map.class);
				LogerUtil.log("resultMap:" + resultMap);
				String executeResult = StringUtil.getString(resultMap.get("ExecuteResult"));
				String returnMsg = StringUtil.getString(resultMap.get("ReturnMsg"));
				String returnString = StringUtil.getString(resultMap.get("ReturnString"));
				
				if("true".equals(executeResult)){
					updateParam.addAttr("upStatus", 2);
					updateParam.addAttr("reContractId", returnString);
					updateParam.addAttr("errorMessage", "");
					historyParam.addAttr("upStatus", 2);
					historyParam.addAttr("reContractId", returnString);
					historyParam.addAttr("errorMessage", "");
				}else{
					updateParam.addAttr("upStatus", 3);
					updateParam.addAttr("errorMessage", returnMsg);
					historyParam.addAttr("upStatus", 3);
					historyParam.addAttr("errorMessage", returnMsg);
				}
				
				
			}else{
				updateParam.addAttr("upStatus", 3);
				updateParam.addAttr("errorMessage", "请求CFS失败，请稍后重试！");
				historyParam.addAttr("upStatus", 3);
				historyParam.addAttr("errorMessage", "请求CFS失败，请稍后重试！");
				resultMap.put("errorMsg", "请求CFS失败，请稍后重试！");
			}
			
			AppResult updateResult = null;
			if (SpringAppContext.getBean("treatInfoService") == null) {
				updateResult = RemoteInvoke.getInstance().callNoTx(updateParam);
			}else{
				updateResult = SoaManager.getInstance().invoke(updateParam);
			}
			
			if(updateResult.isSuccess()){
				if (SpringAppContext.getBean("treatInfoHistoryService") == null) {
					RemoteInvoke.getInstance().callNoTx(historyParam);
				}else{
					SoaManager.getInstance().invoke(historyParam);
				}
			}
			
		} catch (Exception e) {
			LogerUtil.error(StoreCFSDealUtil.class, e, "上传CFS失败，请稍后重试！");
			resultMap.put("errorMsg", "上传CFS失败，请稍后重试！");
		}finally{
			try {
				// 关闭连接,释放资源
				if (httpResponse != null) {
					httpResponse.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultMap;
		
	}
	
	/**
	 * 获取贷款进度中的机构
	 * @return
	 */
	public static String getLoanOrg(String applyId,String loanNo){
		String loanOrg = "";
		AppParam queryParam = new AppParam("treatContractService","query");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("loanNo", loanNo);
		AppResult queryResult = null;
		if (SpringAppContext.getBean("treatContractService") == null) {
			queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		}else{
			queryResult = SoaManager.getInstance().invoke(queryParam);
		}
		Map<String,Object> loanMap = null;
		if(queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0))){
			loanMap = queryResult.getRow(0);
			String loanDesc = StringUtil.getString(loanMap.get("loanDesc"));
			if(!StringUtils.isEmpty(loanDesc) && loanDesc.contains("】")){
				String [] orgArray = StringUtils.split(loanDesc, "】");
				String firstStr = orgArray[0];
				if(firstStr.contains("【")){
					String [] secondArray = StringUtils.split(firstStr, "【");
					loanOrg = secondArray[1];
				}
			}
		}
		return loanOrg;
	}
}
