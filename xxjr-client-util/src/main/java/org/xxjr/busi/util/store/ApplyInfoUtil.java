package org.xxjr.busi.util.store;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ValidUtils;

/***
 * 录单工具类
 *
 */
public class ApplyInfoUtil {

	/**
	 * 将空字符串转成null值
	 * 
	 * @param param
	 * @return
	 */
	public static void conversionNull(Map<String, Object> param) {
		for (String key : param.keySet()) {
			if (StringUtils.isEmpty(param.get(key))) {
				param.put(key, null);
			} else {
				param.put(key, param.get(key));
			}
		}
	}

	/**
	 * 联系人校验
	 * 
	 * @param param
	 * @param type
	 */
	public static void validate(Map<String, Object> param) {
		if (StringUtils.isEmpty(param.get("name"))) {
			throw new SysException("姓名不能为空");
		}

		if (StringUtils.isEmpty(param.get("telephone"))) {
			throw new SysException("手机号码不能为空");
		} else {
			boolean validValue = ValidUtils.validValue("telephone",
					param.get("telephone").toString());
			if (!validValue) {
				throw new SysException("手机号码格式错误");
			}
		}
	}

	/**
	 * 查询回款信息
	 * 
	 * @param param
	 * @param type
	 */
	public static AppResult queryRetLoanInfo(AppParam params) {
		AppResult result = new AppResult();
		try {
			params.setService("treatSuccessService");
			params.setMethod("query");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			return result;
		} catch (Exception e) {
			LogerUtil.error(ApplyInfoUtil.class, e, "queryRetLoanInfo error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 设置customerId
	 * @param queryParam
	 * @param customerId
	 * @param custId
	 */
	public static void setCustomerId(AppParam queryParam,String customerId,String custId){
		//获取用户信息
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if(custInfo != null){
			String authType =   StringUtil.getString(custInfo.get("roleType"));
			//总部管理员及门店经理及门店负责人及主管，副主管可以操作非本人单
			if(CustConstant.CUST_ROLETYPE_1.equals(authType)
					|| CustConstant.CUST_ROLETYPE_6.equals(authType)
					|| CustConstant.CUST_ROLETYPE_7.equals(authType)
					|| CustConstant.CUST_ROLETYPE_8.equals(authType)
					|| CustConstant.CUST_ROLETYPE_9.equals(authType)){
				if (StringUtils.isEmpty(custId)) {
					throw new SysException("用户ID不能为空");
				}
				queryParam.addAttr("customerId", custId);
				queryParam.addAttr("adminCustomerId", customerId);
			}else{
				queryParam.addAttr("customerId", customerId);
			}
		}
	}

	/**
	 * 用户是否有数据权限
	 * @param custoemrId
	 * @return
	 */
	public static boolean isStoreAuth(String customerId) {
		// 获取用户信息
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId);
		if (custInfo != null) {
			String authType = StringUtil.getString(custInfo.get("roleType"));
			if (CustConstant.CUST_ROLETYPE_1.equals(authType)
					|| CustConstant.CUST_ROLETYPE_6.equals(authType)
					|| CustConstant.CUST_ROLETYPE_7.equals(authType)
					|| CustConstant.CUST_ROLETYPE_8.equals(authType)
					|| CustConstant.CUST_ROLETYPE_9.equals(authType)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 用户是否为管理员
	 * @param custoemrId
	 * @return
	 */
	public static boolean isAdminAuth(String customerId) {
		// 获取用户信息
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId);
		if (custInfo != null) {
			String authType = StringUtil.getString(custInfo.get("roleType"));
			if (CustConstant.CUST_ROLETYPE_1.equals(authType)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 用户是否有查询详情权限
	 * @param customerId
	 * @param applyId
	 * @param searchDetailFlag 查询主表的标志
	 * @return
	 */
	public static AppResult isQueryDetailAuth(String customerId,String applyId,String searchDetailFlag) {
		AppResult result = new AppResult();
		// 获取用户信息
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if (custInfo != null) {
			String authType = StringUtil.getString(custInfo.get("roleType"));
			Map<String,Object> resultMap = new HashMap<String,Object>();
			if("1".equals(searchDetailFlag)){
				resultMap = StoreApplyUtils.getApplyInfo(applyId);
			}else{
				resultMap = StoreApplyUtils.getStoreApplyInfo(applyId);
			}
			Map<String,Object> lastStoreMap = new HashMap<String,Object>();
			if(resultMap != null){
				String lastStore = StringUtil.getString(resultMap.get("lastStore"));
				if(customerId.equals(lastStore)){
					return result;
				}
				lastStoreMap = CustomerIdentify.getCustIdentify(lastStore);
			}else{
				return CustomerUtil.retErrorMsg("没有此订单信息！");
			}

			if (CustConstant.CUST_ROLETYPE_1.equals(authType)) {//管理员
				String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
				String orgId = StringUtil.getString(lastStoreMap.get("orgId"));
				if(!StringUtils.isEmpty(userOrgs) && ("all".equals(userOrgs) || userOrgs.contains(orgId))){
					return result;
				}
			}else if(CustConstant.CUST_ROLETYPE_6.equals(authType) 
					|| CustConstant.CUST_ROLETYPE_7.equals(authType)){ //门店负责人或门店管理员
				String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
				String orgId = StringUtil.getString(lastStoreMap.get("orgId"));
				if(userOrgs.contains(orgId)){
					return result;
				}
			}else if(CustConstant.CUST_ROLETYPE_8.equals(authType)){ //门店主管
				String orgId = StringUtil.getString(lastStoreMap.get("orgId"));
				String groupName = StringUtil.getString(lastStoreMap.get("groupName"));
				String currentOrgId =  StringUtil.getString(custInfo.get("orgId"));
				String currentGroupName =  StringUtil.getString(custInfo.get("groupName"));
				if(currentOrgId.equals(orgId) && currentGroupName.equals(groupName)){
					return result;
				}
			}else if(CustConstant.CUST_ROLETYPE_9.equals(authType)){//门店副主管
				String orgId = StringUtil.getString(lastStoreMap.get("orgId"));
				String groupName = StringUtil.getString(lastStoreMap.get("groupName"));
				String teamName = StringUtil.getString(lastStoreMap.get("teamName"));
				String currentOrgId =  StringUtil.getString(custInfo.get("orgId"));
				String currentGroupName =  StringUtil.getString(custInfo.get("groupName"));
				String currentTeamName =  StringUtil.getString(custInfo.get("teamName"));
				if(currentOrgId.equals(orgId) && currentGroupName.equals(groupName)
						&& currentTeamName.equals(teamName)){
					return result;
				}
			}
		}
		result.setSuccess(false);
		result.setMessage("您没有权限查询此订单详情！");
		return result;
	}


	/**
	 * 用户是否有查询客服详情权限
	 * @param custoemrId
	 * @return
	 */
	public static boolean isQueryKfDetailAuth(String customerId,String applyId) {
		// 获取用户信息
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId);
		if (custInfo != null) {
			String authType = StringUtil.getString(custInfo.get("roleType"));
			AppResult result = new AppResult();
			AppParam params = new AppParam();
			params.addAttr("applyId", applyId);
			params.setService("kfBusiOptExtService");
			params.setMethod("queryOrderInfo");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			Map<String,Object> resultMap = null;
			if(result.getRows().size() > 0 &&  !StringUtils.isEmpty(result.getRow(0))){
				resultMap = result.getRow(0);
				String lastKf = StringUtil.getString(resultMap.get("lastKf"));
				if(customerId.equals(lastKf)){
					return true;
				}else if(CustConstant.CUST_ROLETYPE_1.equals(authType)) {//管理员
					return true;
				}
			}else{
				throw new SysException("没有此订单信息！");
			}
		}

		return false;
	}
	
	/**
	 * 查询门店主管或门店经理ID
	 * @param orgId 离职人部门ID
	 * @param customerId 离职人ID
	 */
	public static String getLeaderCustId(String orgId,String customerId) {
		//主管或门店经理ID
		String leaderCustId = "";
		//查询离职人员
		AppParam custParams  = new AppParam("busiCustService","query");
		custParams.addAttr("orgId", orgId);
		custParams.addAttr("customerId", customerId);
		custParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult custResult = RemoteInvoke.getInstance().callNoTx(custParams);
		if(custResult.getRows().size() == 0){
			return leaderCustId;
		}
		String roleType = StoreUserUtil.getCustomerRole(customerId);
		//离职人所属组名
		String groupName = StringUtil.getString(custResult.getRow(0).get("groupName"));
		if(!StringUtils.isEmpty(groupName) && !CustConstant.CUST_ROLETYPE_8.equals(roleType)){
			//查询门店主管
			custParams.removeAttr("customerId");
			custParams.addAttr("groupName", groupName);
			custParams.addAttr("roleType", CustConstant.CUST_ROLETYPE_8); //8 主管
			AppResult groupResult = RemoteInvoke.getInstance().callNoTx(custParams);
			if(groupResult.getRows().size() > 0){
				leaderCustId = StringUtil.getString(groupResult.getRow(0).get("customerId"));
			}
		}
		//离职人所属队名
		String teamName = StringUtil.getString(custResult.getRow(0).get("teamName"));
		if(StringUtils.isEmpty(leaderCustId) && !StringUtils.isEmpty(teamName) 
				&& !CustConstant.CUST_ROLETYPE_8.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_9.equals(roleType)){
			//查询门店副主管
			AppParam managerParams  = new AppParam("busiCustService","query");
			managerParams.addAttr("orgId", orgId);
			managerParams.addAttr("roleType", CustConstant.CUST_ROLETYPE_9); //9 门店副主管
			managerParams.addAttr("teamName", teamName);
			managerParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult managerResult = RemoteInvoke.getInstance().callNoTx(managerParams);
			if(managerResult.getRows().size() > 0){
				leaderCustId = StringUtil.getString(managerResult.getRow(0).get("customerId"));
			}
		}
		if(StringUtils.isEmpty(leaderCustId)){
			//查询门店经理
			AppParam managerParams  = new AppParam("busiCustService","query");
			managerParams.addAttr("orgId", orgId);
			managerParams.addAttr("roleType", CustConstant.CUST_ROLETYPE_7); //7 经理
			managerParams.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult managerResult = RemoteInvoke.getInstance().callNoTx(managerParams);
			if(managerResult.getRows().size() == 0){
				return leaderCustId;
			}
			for (Map<String, Object> map : managerResult.getRows()) {
				leaderCustId = StringUtil.getString(map.get("customerId"));
				if(!customerId.equals(leaderCustId)){
					break;
				}
			}
		}
		return leaderCustId;
	}
	
	/**
	 * 开始通话时间校验
	 * @param currentTime 当前时间
	 * @param startCallTime 开始通话时间
	 * @return
	 */
	public static String  verifyStartCallTime(String currentTime,String startCallTime){
		try{
			String[] currentStr = currentTime.split(" ");
			String currentDate ="";
			String currTime ="";
			if(currentStr.length == 2){
				currentDate = currentStr[0];
				currTime = currentStr[1];
			}
			String[] startCallStr = startCallTime.split(" ");
			String startDate = "";
			String startTime = "";
			if(startCallStr.length == 2){
				startDate = startCallStr[0];
				startTime = startCallStr[1];
				//当前日期
				long currentDateLong = DateUtil.toDateByString(currentDate, DateUtil.DATE_PATTERN_YYYY_MM_DD).getTime();
				//开始通话日期
				long startCallDateLong = DateUtil.toDateByString(startDate, DateUtil.DATE_PATTERN_YYYY_MM_DD).getTime();
				//开始通话日期与当前日期比较
				if(startCallDateLong > currentDateLong){
					long currTimeLong = OrderRecyclingUtil.getLongTimes(currTime);
					long startTimeLong = OrderRecyclingUtil.getLongTimes(startTime);
					//开始通话时间与当前时间比较
					if(startTimeLong > currTimeLong){
						startCallTime = currentTime;
					}else{
						startCallTime = currentDate + " " + startTime;
					}
				}
			}
		}catch (Exception e) {
			LogerUtil.error(ApplyInfoUtil.class, e, "valideStartCallTime error");
		}
		return startCallTime;
	}
	
	
	/**
	 * 开始通话时间校验(新)
	 * @param currentTime 当前时间
	 * @param startCallTime 开始通话时间
	 * @return
	 */
	public static String verifyStartCallDate(String currentTime,String startCallTime){
		try{
			
			//当前日期
			String currentDate = DateUtil.toStringByParttern(DateUtil.toDateByString(currentTime, 
					DateUtil.DATE_PATTERN_YYYY_MM_DD),DateUtil.DATE_PATTERN_YYYY_MM_DD);
			//开始通话日期
			String startCallDate = DateUtil.toStringByParttern(DateUtil.toDateByString(startCallTime, 
					DateUtil.DATE_PATTERN_YYYY_MM_DD),DateUtil.DATE_PATTERN_YYYY_MM_DD);
			//比较日期大小
			int dateCompare = startCallDate.compareTo(currentDate);
			if(dateCompare > 0){
				//格式化时间
				SimpleDateFormat timeForMat = new SimpleDateFormat("HH:mm:ss");
				//当前时间
				String currTime = timeForMat.format(DateUtil.toDateByString(currentTime, 
						DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
				//开始通话时间
				String startTime = timeForMat.format(DateUtil.toDateByString(startCallTime, 
						DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
				//比较时间大小
				int timeCompare = startTime.compareTo(currTime);
				if(timeCompare > 0){
					return currentTime;
				}else{
					StringBuffer buffer = new StringBuffer();
					buffer.append(currentDate);
					buffer.append(" ");
					buffer.append(startTime);
					return buffer.toString();
				}
			}
		}catch (Exception e) {
			LogerUtil.error(ApplyInfoUtil.class, e, "verifyStartCallDate error");
		}
		return startCallTime;
	}
	
	/**
	 * 判断订单是否是二次申请
	 * @param queryParam
	 * @param customerId
	 * @param custId
	 */
	public static void isAgainApplyOrder(AppParam param,String applyId){
		Map<String,Object> applyMap = StoreApplyUtils.getStoreApplyInfo(applyId);
		if(applyMap != null){
			param.addAttr("applyTime", applyMap.get("applyTime"));
		}
	}
	
	/**
	 * 判断日期是否是在上个月与这个月之间
	 * @param startDate
	 * @return
	 */
	public static AppResult compareDateInThisMonth(String querDate){
		AppResult result = new AppResult();
		String firstDate = DateUtil.toStringByParttern(DateUtil.getNextMonthFirstSecond(LocalDateTime.now().minus(2,
				DateUtil.ChronoUnit_MONTHS)), DateUtil.DATE_PATTERN_YYYY_MM_DD);
		String lastDate = DateUtil.toStringByParttern(DateUtil.getMonthLastSecond(LocalDateTime.now()), 
				DateUtil.DATE_PATTERN_YYYY_MM_DD);
		int minDate = firstDate.compareTo(querDate);
		if(minDate > 0){
			return CustomerUtil.retErrorMsg("查询日期不能小于上个月第一天");
		}
		int maxDate = querDate.compareTo(lastDate);
		if(maxDate > 0){
			return CustomerUtil.retErrorMsg("查询日期不能大于本月最后一天");
		}
		return result;
	}
	
	/**
	 * 匹配返回的签约经理
	 * @param
	 * @return
	 */
	public static String getOrderCustomerId(AppParam param){
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		if(StringUtils.isEmpty(customerId)){
			return customerId;
		}
		String customerName = StringUtil.getString(param.getAttr("customerName"));
		if(StringUtils.isEmpty(customerName)){
			return customerId;
		}
		String orgId = StringUtil.getString(param.getAttr("orgId"));
		AppParam custParams  = new AppParam("busiCustService","query");
		custParams.addAttr("customerId", customerId);
		custParams.addAttr("orgId", orgId);
		custParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult custResult = RemoteInvoke.getInstance().callNoTx(custParams);
		if(custResult.getRows().size() > 0){
			String realName = StringUtil.getString(custResult.getRow(0).get("realName"));
			if(customerName.equals(realName)){
				return customerId;
			}else{
				custParams.removeAttr("customerId");
				custParams.addAttr("realName",customerName);
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(custParams);
				if(queryResult.getRows().size() > 0){
					customerId = StringUtil.getString(custResult.getRow(0).get("customerId"));
				}
			}
		}
		return customerId;
	}
	
	/**
	 * 判断订单是否有上门记录或签单记录
	 * @param applyId
	 */
	public static boolean isVisitOrSignRecord(String applyId){
		boolean notHideFlag = false;
		//查询是否有上门记录
		AppParam applyParams  = new AppParam("treatVisitDetailService","queryCount");
		applyParams.addAttr("applyId", applyId);
		applyParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(applyParams);
		int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
		if(count > 0){
			notHideFlag = true;
			return notHideFlag;
		}
		//查询是否有签单记录
		AppParam signParams  = new AppParam("treatInfoService","queryCount");
		signParams.addAttr("applyId", applyId);
		signParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult signResult = RemoteInvoke.getInstance().callNoTx(signParams);
		int signCount = NumberUtil.getInt(signResult.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
		if(signCount > 0){
			notHideFlag = true;
			return notHideFlag;
		}
		return notHideFlag;
	}
}
