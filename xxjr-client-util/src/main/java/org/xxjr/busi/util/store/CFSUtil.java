package org.xxjr.busi.util.store;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.tools.util.HttpsClinetTrustAny;

/**
 * CFS对接工具类
 * @author Administrator
 *
 */
public class CFSUtil {
	
	/** 查询CFS回款次数 **/
	public static String CacheKey_CFS_BACK_COUNT = "queryCFSBackCount";
	/**
	 * 添加合同信息上传CFS
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
			client =HttpsClinetTrustAny.getInstance().getHttpsClient();
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
				
				AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
				if(updateResult.isSuccess()){
					RemoteInvoke.getInstance().call(historyParam);
				}
			}else{
				updateParam.addAttr("upStatus", 3);
				updateParam.addAttr("errorMessage", "请求CFS失败，请稍后重试！");
				AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
				if(updateResult.isSuccess()){
					historyParam.addAttr("upStatus", 3);
					historyParam.addAttr("errorMessage", "请求CFS失败，请稍后重试！");
					RemoteInvoke.getInstance().call(historyParam);
				}
				resultMap.put("errorMsg", "请求CFS失败，请稍后重试！");
			}
			
		} catch (Exception e) {
			LogerUtil.error(CFSUtil.class, e, "上传CFS失败，请稍后重试！");
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
	 * 获取回款信息工具类
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getBackLoanInfo(AppParam param){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String orgId = StringUtil.getString(param.getAttr("orgId"));
		String reContractId = StringUtil.getString(param.getAttr("reContractId"));
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		String[] paramList = {reContractId};
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ParamString", paramList);
		params.put("TranName", "GetLoanGeneralInfo_v2");
		String jsonstr = JsonUtil.getInstance().object2JSON(params);
		LogerUtil.log("rewqet data ="+jsonstr);
		String reStrJoson = JsonUtil.getInstance().object2JSON(jsonstr);
		CloseableHttpClient client = null;
		CloseableHttpResponse httpResponse = null;
		try {
			client = HttpsClinetTrustAny.getInstance().getHttpsClient();
			String CFS_DUI_URL = SysParamsUtil.getStringParamByKey("cfs_request_url", "https://www.xxxxjs.cn:2568/DuiService/Tran1");
			LogerUtil.log("CFS请求地址：" + CFS_DUI_URL);
			HttpPut httpPost = new HttpPut(CFS_DUI_URL);
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("xxjrtg", "xxjrtg");
			StringEntity stringEntity = new StringEntity(reStrJoson,"utf-8");
			httpPost.setEntity(stringEntity);
			httpResponse = client.execute(httpPost);
			AppParam saveParams  = new AppParam();
			saveParams.addAttr("applyId",applyId);
			saveParams.addAttr("customerId",customerId);
			saveParams.addAttr("orgId",orgId);
			saveParams.setService("treatSuccessService");
			saveParams.setMethod("deleteData");
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = httpResponse.getEntity();
				String resString =  StringUtil.decodeStr(EntityUtils.toString(entity, Consts.UTF_8));
				LogerUtil.log("resString：" + resString);
				EntityUtils.consume(entity);
				resultMap = JsonUtil.getInstance().json2Object(resString, Map.class);
				LogerUtil.log("resultMap:" + resultMap);
				String executeResult = StringUtil.getString(resultMap.get("ExecuteResult"));
				String returnMsg = StringUtil.getString(resultMap.get("ReturnMsg"));
				String returnString = StringUtil.getString(resultMap.get("ReturnString"));
				if(executeResult.equals("true")){
					Map<String, Object> returnMap = JsonUtil.getInstance().json2Object(returnString,Map.class);
					List<Map<String,Object>> recordList = (List<Map<String,Object>>) returnMap.get("BooksRecords");
					if(!StringUtils.isEmpty(recordList) && recordList.size() > 0){
						AppParam updateParams  = new AppParam();
						updateParams.addAttr("applyId",applyId);
						updateParams.addAttr("customerId",customerId);
						updateParams.addAttr("orgId",orgId);
						updateParams.addAttr("reContractId",reContractId);
						updateParams.addAttr("recordList",recordList);
						updateParams.setService("treatSuccessService");
						updateParams.setMethod("saveData");
						updateParams.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
						RemoteInvoke.getInstance().call(updateParams);
					}
					
				}else{
					saveParams.addAttr("backType",2); //回款方式1-手动添加 2-查CFS回款
					saveParams.addAttr("queryStatus",3); //查询状态 1-未查询 2-查询成功 3-查询失败
					saveParams.addAttr("errorMessage",returnMsg);
					saveParams.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					RemoteInvoke.getInstance().call(saveParams);
				}
				
			}else{
				saveParams.addAttr("backType",2); //回款方式1-手动添加 2-查CFS回款
				saveParams.addAttr("queryStatus",3); //查询状态 1-未查询 2-查询成功 3-查询失败
				saveParams.addAttr("errorMessage","获取回款信息失败，请稍后重试！");
				saveParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(saveParams);
				resultMap.put("errorMsg", "获取回款信息失败，请稍后重试！");
			}
			
		} catch (Exception e) {
			LogerUtil.error(CFSUtil.class, e, "获取回款信息失败，请稍后重试！");
			resultMap.put("errorMsg", "获取回款信息失败，请稍后重试！");
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
	 * 获取贷款合同信息工具类
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getContractInfo(AppParam param){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String reContractId = StringUtil.getString(param.getAttr("reContractId"));
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		String orgId = StringUtil.getString(param.getAttr("orgId"));
		String[] paramList = {reContractId};
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ParamString", paramList);
		params.put("TranName", "GetLoanGeneralInfo_v2");
		String jsonstr = JsonUtil.getInstance().object2JSON(params);
		LogerUtil.log("rewqet data ="+jsonstr);
		String reStrJoson = JsonUtil.getInstance().object2JSON(jsonstr);
		CloseableHttpClient client = null;
		CloseableHttpResponse httpResponse = null;
		try {
			client = HttpsClinetTrustAny.getInstance().getHttpsClient();
			String CFS_DUI_URL = SysParamsUtil.getStringParamByKey("cfs_request_url", "https://www.xxxxjs.cn:2568/DuiService/Tran1");
			LogerUtil.log("CFS请求地址：" + CFS_DUI_URL);
			HttpPut httpPost = new HttpPut(CFS_DUI_URL);
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("xxjrtg", "xxjrtg");
			StringEntity stringEntity = new StringEntity(reStrJoson,"utf-8");
			httpPost.setEntity(stringEntity);
			httpResponse = client.execute(httpPost);
			AppParam saveParams  = new AppParam("treatOriginInfoService","saveData");
			saveParams.addAttr("applyId",applyId);
			saveParams.addAttr("reContractId",reContractId);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = httpResponse.getEntity();
				String resString =  StringUtil.decodeStr(EntityUtils.toString(entity, Consts.UTF_8));
				LogerUtil.log("resString：" + resString);
				EntityUtils.consume(entity);
				resultMap = JsonUtil.getInstance().json2Object(resString, Map.class);
				LogerUtil.log("resultMap:" + resultMap);
				String executeResult = StringUtil.getString(resultMap.get("ExecuteResult"));
				String returnMsg = StringUtil.getString(resultMap.get("ReturnMsg"));
				String returnString = StringUtil.getString(resultMap.get("ReturnString"));
				if(executeResult.equals("true")){
					Map<String, Object> returnMap = JsonUtil.getInstance().json2Object(returnString,Map.class);
					List<Map<String,Object>> recordList = (List<Map<String,Object>>) returnMap.get("LoanRecords");
					List<Map<String,Object>> customerList = (List<Map<String,Object>>) returnMap.get("Customers");
					String contractStatus = StringUtil.getString(returnMap.get("ContractStatus"));
					String customerName = StringUtil.getString(returnMap.get("ClerkName"));
					//匹配CFS返回的业务员
					AppParam queryParams  = new AppParam();
					queryParams.addAttr("customerId", customerId);
					queryParams.addAttr("customerName", customerName);
					queryParams.addAttr("orgId", orgId);
					customerId = org.xxjr.busi.util.store.ApplyInfoUtil.getOrderCustomerId(queryParams);
					Boolean flag = (Boolean) returnMap.get("IsTG");
					int contractType = 1; // 合同类型（1-手动添加 2-CFS返回）
					if(!flag){
						contractType = 2;
					}
					String applyName = "";
					String telephone = "";
					if(!StringUtils.isEmpty(customerList) && customerList.size() > 0){
						applyName = StringUtil.getString(customerList.get(0).get("Name"));
						telephone = StringUtil.getString(customerList.get(0).get("Mobile")).trim();
					}
					String strSignDate = StringUtil.getString(returnMap.get("SignDate"));
					String signDate = null;
					if(!StringUtils.isEmpty(strSignDate)){
						DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						Date date = sdf.parse(StringUtil.getString(strSignDate));
						SimpleDateFormat simpFormat = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
				        Date payDate =  simpFormat.parse(StringUtil.getString(date));
				        signDate = DateUtil.toStringByParttern(payDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
					}
					List<Map<String,Object>> bookList = (List<Map<String,Object>>) returnMap.get("BooksRecords");
					double feeAmountTotal = getFeeAmount(bookList);
					AppParam originParams  = new AppParam();
					originParams.addAttr("applyId",applyId);
					originParams.addAttr("reContractId",reContractId);
					originParams.addAttr("customerName",customerName);
					originParams.addAttr("customerId",customerId);
					originParams.addAttr("signDate",signDate);
					originParams.addAttr("contractType",contractType);
					originParams.addAttr("applyName",applyName);
					originParams.addAttr("orgId",orgId);
					originParams.addAttr("telephone",telephone);
					originParams.addAttr("jsonText",returnString);
					originParams.addAttr("feeAmountTotal",feeAmountTotal);
					originParams.setService("treatOriginInfoService");
					originParams.setMethod("saveData");
					originParams.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					RemoteInvoke.getInstance().call(originParams);
					
					if(!StringUtils.isEmpty(applyId)){
						if(!StringUtils.isEmpty(recordList) && recordList.size() > 0){
							AppParam updateParams  = new AppParam();
							updateParams.addAttr("applyId",applyId);
							updateParams.addAttr("customerId",customerId);
							updateParams.addAttr("reContractId",reContractId);
							updateParams.addAttr("signDate",signDate);
							updateParams.addAttr("contractStatus",contractStatus);
							updateParams.addAttr("recordList",recordList);
							updateParams.setService("treatContractService");
							updateParams.setMethod("saveData");
							updateParams.setRmiServiceName(AppProperties
									.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
							RemoteInvoke.getInstance().call(updateParams);
							
						}
						
						saveParams.addAttr("status",1); //查询状态 0-未处理 1-处理成功 2-处理失败
						saveParams.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
						RemoteInvoke.getInstance().call(saveParams);
					}
					
					if(!StringUtils.isEmpty(applyId) && !StringUtils.isEmpty(customerId)){
						if(!StringUtils.isEmpty(bookList) && bookList.size() > 0){
							AppParam updateParams  = new AppParam();
							updateParams.addAttr("applyId",applyId);
							updateParams.addAttr("customerId",customerId);
							updateParams.addAttr("orgId",orgId);
							updateParams.addAttr("reContractId",reContractId);
							updateParams.addAttr("recordList",bookList);
							updateParams.setService("treatSuccessService");
							updateParams.setMethod("saveData");
							updateParams.setRmiServiceName(AppProperties
									.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
							RemoteInvoke.getInstance().call(updateParams);
						}
					}
					
				}else{
					saveParams.addAttr("status",0); //查询状态 0-未处理 1-处理成功 2-处理失败
					saveParams.addAttr("errorMessage",returnMsg);
					saveParams.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					RemoteInvoke.getInstance().call(saveParams);
				}
			}else{
				saveParams.addAttr("status",0); //查询状态 0-未处理 1-处理成功 2-处理失败
				saveParams.addAttr("errorMessage","获取合同信息失败，请稍后重试！");
				saveParams.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				RemoteInvoke.getInstance().call(saveParams);
				resultMap.put("errorMsg", "获取合同信息失败，请稍后重试！");
			}
		
		} catch (Exception e) {
			LogerUtil.error(CFSUtil.class, e, "获取合同信息失败，请稍后重试！");
			resultMap.put("errorMsg", "获取合同信息失败，请稍后重试！");
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
	 * 获取有更新的合同信息的合同编号
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getUpdateContract(AppParam param){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		list.add(StringUtil.getString(param.getAttr("orgNo")));
		list.add(StringUtil.getString(param.getAttr("startDate")));
		params.put("TranName", "GetChangedContracts");
		params.put("ParamString", list);
		String jsonstr = JsonUtil.getInstance().object2JSON(params);
		LogerUtil.log("rewqet data ="+jsonstr);
		String reStrJoson = JsonUtil.getInstance().object2JSON(jsonstr);
		CloseableHttpClient client = null;
		CloseableHttpResponse httpResponse = null;
		try {
			client = HttpsClinetTrustAny.getInstance().getHttpsClient();
			String CFS_DUI_URL = SysParamsUtil.getStringParamByKey("cfs_request_url", "https://www.xxxxjs.cn:2568/DuiService/Tran1");
			LogerUtil.log("CFS请求地址：" + CFS_DUI_URL);
			HttpPut httpPost = new HttpPut(CFS_DUI_URL);
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("xxjrtg", "xxjrtg");
			StringEntity stringEntity = new StringEntity(reStrJoson,"utf-8");
			httpPost.setEntity(stringEntity);
			httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = httpResponse.getEntity();
				String resString =  StringUtil.decodeStr(EntityUtils.toString(entity, Consts.UTF_8));
				LogerUtil.log("resString：" + resString);
				EntityUtils.consume(entity);
				Map<String,Object> returnMap = JsonUtil.getInstance().json2Object(resString, Map.class);
				LogerUtil.log("returnMap:" + returnMap);
				String executeResult = StringUtil.getString(returnMap.get("ExecuteResult"));
				String returnMsg = StringUtil.getString(returnMap.get("ReturnMsg"));
				String returnString = StringUtil.getString(returnMap.get("ReturnString"));
				if(executeResult.equals("true")){
					List<String> listString = null;
					if(!"[]".equals(returnString)){
						listString = JsonUtil.getInstance().json2Object(returnString,List.class);
					}
					resultMap.put("listString", listString);
					
				}else{
					resultMap.put("errorMsg", returnMsg);
				}
			}else{
				resultMap.put("errorMsg", "获取有更新的合同信息的合同编号失败，请稍后重试！");
			}
		
		} catch (Exception e) {
			LogerUtil.error(CFSUtil.class, e, "获取有更新的合同信息的合同编号失败，请稍后重试！");
			resultMap.put("errorMsg", "获取有更新的合同信息的合同编号失败，请稍后重试！");
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
	 * 获取回款总额
	 * @param params
	 * @return
	 * @throws ParseException
	 */
	public static double getFeeAmount(List<Map<String, Object>> recordList) throws ParseException {
		double feeAmountTotal = 0.00;
		if(!StringUtils.isEmpty(recordList) && recordList.size() > 0){
			for(Map<String,Object> map : recordList){
				double feeAmount = NumberUtil.getDouble(map.get("RecMoney"),0.00);
				feeAmountTotal += feeAmount;
			}
		}
		return feeAmountTotal;
	}
	
	/**
	 * 添加访客记录上传CFS
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> addVisitToCFS(AppParam param){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("CompanyID", param.getAttr("orgNo"));
		map.put("CustomerName", param.getAttr("custName"));
		map.put("MobilePhone", param.getAttr("custTel"));
		map.put("SalesPhoneNumber", param.getAttr("receiverTel"));
		map.put("SalesName", param.getAttr("realName"));
		map.put("DemandPrice", param.getAttr("demandPrice"));
		map.put("LoanType", param.getAttr("loanType"));
		map.put("VisitTime", param.getAttr("visitTime"));
		map.put("EmployeeNo", param.getAttr("employeeNo"));
		String jsonString = JsonUtil.getInstance().object2JSON(map);
		LogerUtil.log("request params ="+ jsonString);
		HttpClient httpClient = null;
		HttpResponse httpResponse = null;
		try {
			String addVisitCfsUrl = SysParamsUtil.getStringParamByKey("add_visit_cfs_url",
					"http://www.xxxxjs.cn:8084/api/VisitMsg/Visit/CustomerVisit");
			LogerUtil.log("添加访客记录到CFS请求地址：" + addVisitCfsUrl);
			httpClient = HttpClients.createDefault();
	        HttpPost httpPost = new HttpPost(addVisitCfsUrl);
	        // 解决中文乱码问题
	        StringEntity stringEntity = new StringEntity(jsonString,"UTF-8");
	        stringEntity.setContentEncoding("UTF-8");    
	        stringEntity.setContentType("application/json");
	        httpPost.setEntity(stringEntity);
	        httpResponse = httpClient.execute(httpPost);
			String recordId = StringUtil.getString(param.getAttr("recordId"));
			String detailId = StringUtil.getString(param.getAttr("detailId"));
			AppParam updateParam = new AppParam("custVisitService","update");
			if(!StringUtils.isEmpty(recordId)){
				updateParam.addAttr("recordId", recordId);
			}else if(!StringUtils.isEmpty(detailId)){
				updateParam.setService("treatVisitDetailService");
				updateParam.setMethod("updateByDetailId");
				updateParam.addAttr("detailId", detailId);
			}
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = httpResponse.getEntity();
				String resString =  StringUtil.decodeStr(EntityUtils.toString(entity, Consts.UTF_8));
				EntityUtils.consume(entity);
				resultMap = JsonUtil.getInstance().json2Object(resString, Map.class);
				LogerUtil.log("resultMap:" + resultMap);
				String messageCode = StringUtil.getString(resultMap.get("MessageCode"));
				String message = StringUtil.getString(resultMap.get("Message"));
				if("200".equals(messageCode)){
					updateParam.addAttr("upStatus", 2);
					updateParam.addAttr("errorMessage", "");
				}else{
					updateParam.addAttr("upStatus", 3);
					updateParam.addAttr("errorMessage", message);
				}
				RemoteInvoke.getInstance().call(updateParam);
			}else{
				updateParam.addAttr("upStatus", 3);
				updateParam.addAttr("errorMessage", "添加访客记录上传CFS失败，请稍后重试！");
				RemoteInvoke.getInstance().call(updateParam);
				resultMap.put("Message", "添加访客记录上传CFS失败，请稍后重试！");
			}
		} catch (Exception e) {
			LogerUtil.error(CFSUtil.class, e, "添加访客记录上传CFS失败，请稍后重试！");
			resultMap.put("Message", "添加访客记录上传CFS失败，请稍后重试！");
		}
		return resultMap;
	}
	
	/**
	 * 获取业务员的员工编号
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static String getStoreEmployeeNo(AppParam param){
		String employeeNo = "";
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			Map<String, Object> sendMap = new HashMap<String, Object>();
			sendMap.put("TranName", "GetTGInfo");
			sendMap.put("Function", "GetUerInfo");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("UserName", param.getAttr("realName"));
			map.put("CompanyID", param.getAttr("orgNo"));
			sendMap.put("Json", JsonUtil.getInstance().object2JSON(map));
			String jsonsParam = JsonUtil.getInstance().object2JSON(sendMap);
			String CFS_DUI_URL = SysParamsUtil.getStringParamByKey("cfs_getEmployInfo_request_url", 
					"https://www.xxxxjs.cn:2568/DuiService/Tran");
			LogerUtil.log("CFS请求地址：" + CFS_DUI_URL);
			HttpPut httpPut = new HttpPut(CFS_DUI_URL);
			httpPut.setHeader("Content-type", "application/json");
			httpPut.setHeader("xxjrtg", "xxjrtg");
			StringEntity stringEntity = new StringEntity(jsonsParam,"utf-8");
			httpPut.setEntity(stringEntity);
			httpClient = HttpsClinetTrustAny.getInstance().getHttpsClient();
			httpResponse = httpClient.execute(httpPut);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = httpResponse.getEntity();
				String returnJson = StringUtil.decodeStr(EntityUtils.toString(entity, Consts.UTF_8));
				LogerUtil.log("returnJson：" + returnJson);
				EntityUtils.consume(entity);
				Map<String,Object> returnMap = JsonUtil.getInstance().json2Object(returnJson, Map.class);
				LogerUtil.log("returnMap:" + returnMap);
				String executeResult = StringUtil.getString(returnMap.get("ExecuteResult"));
				String returnDataTable = StringUtil.getString(returnMap.get("ReturnDataTable"));
				if(executeResult.equals("true") && !"[]".equals(returnDataTable)){
					List<Map<String,Object>> resultDataMap = JsonUtil.getInstance().json2Object(returnDataTable,List.class);
					int size = resultDataMap.size();
					if(size == 1){
						employeeNo = StringUtil.getString(resultDataMap.get(0).get("EmployeeNo"));
					}else if(size >= 2){
						for (int i = 0; i < size; i++) {
							if(i == size - 1){
								employeeNo += resultDataMap.get(i).get("EmployeeNo");
							}else{
								employeeNo += resultDataMap.get(i).get("EmployeeNo") + ",";
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LogerUtil.error(CFSUtil.class, e, "获取业务员的员工编号失败，请稍后重试！");
		}finally{
			try {
				// 关闭连接,释放资源
				if (httpResponse != null) {
					httpResponse.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return employeeNo;
	}
}
