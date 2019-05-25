package org.xxjr.busi.util;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.IDCardValidate;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;



public class ApplyAllotUtil {
	
	/**
	 * 判断去向标志
	 * @param params
	 * @return
	 */
	public static int allot(AppParam params) {
		String grade = StringUtil.getString(params.getAttr("grade"));
		String cityName = StringUtil.getString(params.getAttr("cityName"));
		int insure = NumberUtil.getInt(params.getAttr("insure"),0);
		double loanAmount = NumberUtil.getDouble(params.getAttr("loanAmount"), 0);
		
		return allot(grade,cityName,insure,loanAmount);
    }
	
	
	/**
	 * 判断去向标志
	 * @param params
	 * @return
	 */
	public static int allot(String grade, String cityName, int insure, double loanAmount) {//
		if (StringUtils.isEmpty(cityName)) {//未填城市 直接给客服
			return 4;
		}
		AppParam param = new AppParam();
		param.setMethod("query2allot");
		param.addAttr("cityName", cityName);
		param.addAttr("grade", grade);
		
		param.setService("allotCityTemplateService");
		AppResult cityTempResult = SoaManager.getInstance().invoke(param);
		List<Map<String, Object>> rows = cityTempResult.getRows();
		String tempName = null;
		int cityType = 0;
		Map<String, Object> cityTemp = null;
		if (rows.size()>0) {
			cityTemp = cityTempResult.getRow(0);//获取城市分单数量
			tempName = StringUtil.getString(cityTemp.remove("tempName"));//优先级模板名称
		}else {//无匹配的分单量模板
			param.setMethod("queryCityType");
			param.setService("allotCityTemplateService");
			AppResult cityTypeResult = SoaManager.getInstance().callNoTx(param);
			cityType = NumberUtil.getInt(cityTypeResult.getAttr("cityType"), 3);
			
			AppParam appParam = new AppParam("allotCityTemplateService","query2allot");
			appParam.addAttr("cityName", getCityName(cityType));// 0-网销城市，1-电销城市，2-贷超城市，3-无处理城市
			AppResult cityTempResult2 = SoaManager.getInstance().callNoTx(appParam);
			cityTemp = cityTempResult2.getRow(0);//获取城市分单数量
			tempName = StringUtil.getString(cityTemp.remove("tempName"));//优先级模板名称
		}
		
		param.addAttr("tempName", tempName);
		param.setService("allotTemplateConfService");
		param.setMethod("query2allot");
		AppResult cityTempConf = SoaManager.getInstance().invoke(param);
		Map<String, Object> tempConf = cityTempConf.getRow(0);//获取城市模板优先级
		
		if (insure!=1) {//未勾选保险
			cityTemp.put("insuranceCount", 0);
			tempConf.put("insurance", 0);
		}
		int k = 0;
		int n = 0;
		for (String key : cityTemp.keySet()) {//量都是0的情况
			if (NumberUtil.getInt(cityTemp.get(key))==0) {
				k++;
			}
			n++;
		}
		if (n==k) {
			return 0;
		}
		AppParam paramCount = new AppParam("allotCountService","query");
		paramCount.addAttr("recordDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
		paramCount.addAttr("cityName", cityName);
		AppResult countResult = SoaManager.getInstance().invoke(paramCount);//查询分单数记录
		List<Map<String, Object>> list = countResult.getRows();//分单记录
		if (list.size()>0) {
			Map<String, Object> countMap = list.get(0);
			for (int i = 1; i < 7; i++) {
				String key = getPriority(tempConf,i);//获得优先级
				if (key==null) {
					continue;
				}
				if (NumberUtil.getInt(countMap.get(key+"Count"))<NumberUtil.getInt(cityTemp.get(key+"Count"))) {
					int allotFlag = getAllotFlag(key);
					if (allotFlag == 2 && loanAmount <= 0) {
						allotFlag = 4;
					}
					return allotFlag;
				}
			}
		}else {
			String key = null;
			for (int i = 1; i < 7; i++) {
				key = getPriority(tempConf,i);//获得优先级
				if (key!=null) {
					break;
				}
			}
			int allotFlag = getAllotFlag(key);
			if (allotFlag == 2 && loanAmount <= 0) {
				allotFlag = 4;
			}
			return allotFlag;
		}
		
		String allotFlagKey = getAllotFlagKey(cityTemp,tempConf);//剩余的单
		if ("noDeal".equals(allotFlagKey)) {
			return 0;
		}
		int allotFlag = getAllotFlag(allotFlagKey);
		if (allotFlag == 2 && loanAmount <= 0) {
			allotFlag = 4;
		}
		return allotFlag;
    }
	
	/**
	 * 获得优先级对应的KEY
	 * @param map
	 * @param i
	 * @return
	 */
	private static String getPriority(Map<String, Object> map,int i) {
		for (String key : map.keySet()) {
			if (NumberUtil.getInt(map.get(key))==i) {
				return key;
			}
		}
		return null;
	}
	/**
	 * 获得KEY对应的分单类型
	 * @param map
	 *      1-netSales, 网销
			2-loanShop, 贷超
			3-electSales, 电销
			4-custService, 客服
			5-smallLoan, 小贷
			6-insurance 保险
	 * @return
	 */
	private static int getAllotFlag(String key) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("netSales", 1);
		map.put("loanShop", 2);
		map.put("electSales", 3);
		map.put("custService", 4);
		map.put("smallLoan", 5);
		map.put("insurance", 6);
		return map.get(key);
	}
	/**
	 * 获得KEY对应的分单类型
	 * @param map
	 *      1-netSales, 网销
			2-loanShop, 贷超
			3-electSales, 电销
			4-custService, 客服
			5-smallLoan, 小贷
			6-insurance 保险
	 * @return
	 */
	public static String getAllotFlag(int value) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("netSales", 1);
		map.put("loanShop", 2);
		map.put("electSales", 3);
		map.put("custService", 4);
		map.put("smallLoan", 5);
		map.put("insurance", 6);
		for (String	key : map.keySet()) {
			if (map.get(key)==value) {
				return key;
			}
		}
		return null;
	}
	/**
	 * 0-网销城市，1-电销城市，2-贷超城市，3-无处理城市
	 * @param cityType
	 * @return
	 */
	private static String getCityName(int cityType) {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(0, "网销城市");
		map.put(1, "电销城市");
		map.put(2, "贷超城市");
		map.put(3, "无处理城市");
		return map.get(cityType);
	}
	/**
	 * 剩余的单按随机数分配
	 * @param map
	 *      1-netSales, 网销
			2-loanShop, 贷超
			3-electSales, 电销
			4-custService, 客服
			5-smallLoan, 小贷
			6-insurance 保险
	 * @return
	 */
	private static String getAllotFlagKey(Map<String, Object> cityTemp,Map<String, Object> tempConf) {
		for (String temp : tempConf.keySet()) {
			if (NumberUtil.getInt(tempConf.get(temp))==0) {
				cityTemp.remove(temp+"Count");
			}
		}
		int sum = 0;
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for (String key : cityTemp.keySet()) {
			sum+=NumberUtil.getInt(cityTemp.get(key));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(key, sum);
			list.add(map);
		}
		int random = (int)(Math.random()*(sum-1))+1;
		for (Map<String, Object> map2 : list) {
			for (String key : map2.keySet()) {
				if (NumberUtil.getInt(map2.get(key))>=random) {
					return key.substring(0, key.length()-5);
				}
			}
		}
		return "noDeal";
	}
	
	/**
	 * 分配数量统计
	 * @param cityName
	 * @param allotFlag
	 *@param testNetChannel 网销测试渠道 1-是
	 */
	public static void allotCount(String cityName,int allotFlag,int insureFlag,int testNetChannel) {
		if (StringUtils.isEmpty(cityName)) {
			cityName = "未填写";
		}
		String allotFlag2 = getAllotFlag(allotFlag);
		if (StringUtils.isEmpty(allotFlag2)) {
			return ;
		}
		AppParam paramCount = new AppParam("allotCountService","queryCount");
		paramCount.addAttr("recordDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
		paramCount.addAttr("cityName", cityName);
		AppResult countResult = SoaManager.getInstance().invoke(paramCount);//查询分单数记录\
		int countSize = NumberUtil.getInt(countResult.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
		
		AppParam appParam = new AppParam();
		appParam.setService("allotCountService");
		appParam.addAttr("cityName", cityName);
		appParam.addAttr("recordDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
	
		if(testNetChannel == 1 && allotFlag == 1){
			appParam.addAttr("testNetCount", 1);
		}else{
			appParam.addAttr(allotFlag2+"Count", 1);
		}
		
		if(insureFlag == 1){
			appParam.addAttr("insuranceCount", 1);
		}
		
		
		if (countSize > 0) {
			appParam.setMethod("update");
		}else {
			appParam.setMethod("insert");
		}
		SoaManager.getInstance().invoke(appParam);
	}
	
	public static AppResult saveBorrowApplyInfo(Map<String, Object> applyMap,int allotFlag) {

		Object applyId = applyMap.get("applyId");
        
		String applyStatus = BorrowConstant.apply_status_2;

		Object borrowApplyId = null;
		int applyCount = 0;
		AppParam queryParam = new AppParam("borrowApplyService", "query");
		queryParam.addAttr("telephone", applyMap.get("telephone"));
		AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
		if (queryResult.getRows().size() > 0) {
			AppParam clearParam = new AppParam("borrowApplyService","clearData");
			clearParam.addAttr("applyId", queryResult.getRow(0).get("applyId"));
			SoaManager.getInstance().invoke(clearParam);// 还原表数据

			Map<String,Object> borrowMap = queryResult.getRow(0);
			borrowApplyId = borrowMap.get("applyId");
			applyCount = NumberUtil.getInt(borrowMap.get("applyCount"), 0);
		}
		
		AppParam applyParam = new AppParam();
		applyParam.addAttrs(applyMap);
		applyCount++;
		applyParam.addAttr("applyCount", applyCount);
		applyParam.addAttr("status", applyStatus);

		applyParam.setService("borrowApplyService");
		if (StringUtils.isEmpty(borrowApplyId)) {
			applyParam.removeAttr("applyId");
			applyParam.setMethod("insert");
		} else {
			applyParam.addAttr("applyId", borrowApplyId);
			applyParam.addAttr("createTime", new Date());
			applyParam.setMethod("update");
		}

		AppResult result = SoaManager.getInstance().invoke(applyParam);//插入t_borrow_apply表
		if (StringUtils.isEmpty(borrowApplyId)) {
			borrowApplyId = result.getAttr("applyId");//拿borrowApply的id主键作为条件
		}
		
		if(StringUtils.isEmpty(borrowApplyId)){
			return result;
		}
		
		double loanAmount = NumberUtil.getDouble(applyMap.get("loanAmount"), 0);
		int insurType = NumberUtil.getInt(applyMap.get("insurType"),0);
		
		if(applyCount > 1){//二次申请的处理
			//清除基本数据 
			AppParam deleteParam = new AppParam();
			deleteParam.setService("borrowBaseService");
			deleteParam.setMethod("delete");
			deleteParam.addAttr("applyId", borrowApplyId);
			SoaManager.getInstance().invoke(deleteParam);
			//清除收入
			deleteParam.setService("borrowIncomeService");
			SoaManager.getInstance().invoke(deleteParam);
			//车辆情况
			deleteParam.setService("borrowCarService");
			SoaManager.getInstance().invoke(deleteParam);
			//房产情况
			deleteParam.setService("borrowHouseService");
			SoaManager.getInstance().invoke(deleteParam);
			
			//清除保险数据 
			deleteParam.setService("borrowInsureService");
			SoaManager.getInstance().invoke(deleteParam);
		}
		
		// 保存基本数据
		AppParam baseParams = new AppParam("borrowBaseService", "insert");//保存基本
		baseParams.addAttrs(applyMap);
		baseParams.addAttr("applyId", borrowApplyId);
		SoaManager.getInstance().invoke(baseParams);
		
		if(loanAmount > 0){
			//保存收入
			baseParams.setService("borrowIncomeService");
			baseParams.setMethod("insert");
			SoaManager.getInstance().invoke(baseParams);//保存收入信息	
	
			//保存房产信息
			baseParams.setService("borrowHouseService");
			baseParams.setMethod("insert");
			SoaManager.getInstance().invoke(baseParams);
			
			//保存车产信息
			baseParams.setService("borrowCarService");
			baseParams.setMethod("insert");
			SoaManager.getInstance().invoke(baseParams);
			
			if(insurType > 0){//保存保险信息
				baseParams.setService("borrowInsureService");
				baseParams.setMethod("insert");
				SoaManager.getInstance().invoke(baseParams);
			}
		}
		
		// 分流到各大分单池
		AppResult tranResult = tranPools(borrowApplyId,applyMap,allotFlag);
		
		
		if(!StringUtils.isEmpty(applyId) && result.isSuccess()){
			int applyType = NumberUtil.getInt(applyMap.get("applyType"), 2);
			AppParam updateParam = new AppParam();//修改状态
			updateParam.addAttr("applyId", applyId);
			updateParam.addAttr("borrowApplyId", borrowApplyId);
			updateParam.addAttr("applyType", applyType);
			updateParam.addAttr("grade", applyMap.get("grade"));//更新客户等级
			updateParam.setService("applyService");
			updateParam.setMethod("update");
			updateParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			if (applyType == 1 || applyType == 6) {
				updateParam.addAttr("status", "1");
			}else {
				updateParam.addAttr("status", "2");
			}
			
			SoaManager.getInstance().invoke(updateParam);
		}
		
		
		if(tranResult.isSuccess() && allotFlag > 0 ){
			String cityName = StringUtil.getString(applyMap.get("cityName"));
			int insureFlag = NumberUtil.getInt(tranResult.getAttr("insureFlag"), 0);
			int testNetChannel = NumberUtil.getInt(applyMap.get("testNetChannel"), 0);
			ApplyAllotUtil.allotCount(cityName, allotFlag,insureFlag,testNetChannel);//分单统计
			result.putAttr("borrowId", tranResult.getAttr("borrowId"));
		}
		
		result.putAttr("applyId", borrowApplyId);
		
		return result;
	}
	
	
	/**
	 * 分单转移到各个池中
	 * @param borrowApplyId 申请ID
	 * @param now 原生数据
	 * @param allotFlag  0-未分配 1-网销门店 2-贷超 3-电销 4-客服 5-小贷 6-保险 7-小贷/保险
	 * @return
	 */
	public static AppResult tranPools(Object borrowApplyId, Map<String, Object> now, int allotFlag) {
		AppParam param = new AppParam();
		AppResult retResult = new AppResult();		
		param.addAttrs(now);
		param.addAttr("applyId", borrowApplyId);
		param.addAttr("gradeType", now.get("grade"));//客户等级
		
		AppParam queryParam = new AppParam();
		queryParam.setMethod("query");
		queryParam.addAttr("applyId", borrowApplyId);
	
		retResult = newOrderAllotOrNet(param);
		return retResult;
	}
	
	
	/**旧表和新表类型和名字不一样需要转换**/
	public static void conversionType (Map<String, Object> now) {
		int carType = NumberUtil.getInt(now.get("carType"), 0);
		int houseType = NumberUtil.getInt(now.get("houseType"), 0);
		double income = NumberUtil.getDouble(now.get("income"), 0);
		String identifyNo = StringUtil.getString(now.get("identifyNo"));
		
		String applyIp = StringUtil.getString(now.get("applyIp"));
		if (!StringUtils.isEmpty(applyIp)) {
			int index = applyIp.indexOf(",");//多次反向代理后会有多个ip值，第一个ip才是真实ip
	        if(index != -1){
	        	now.put("applyIp", applyIp.substring(0,index));
	        }
		}
		
		if(houseType == 3){
			now.put("houseMortgage", 1);
		}
		if(houseType == 4){
			now.put("houseMortgage", 2);
		}
		
		if(carType == 3){
			now.put("carMortgage", 2);
		}
		if(carType == 4){
			now.put("carMortgage", 4);
		}

		if (StringUtils.isEmpty(now.get("loanDeadline"))) {
			now.put("loanDeadline", "12");//贷款期限默认一年
		}
		double loanAmount = 0;
         if(StringUtils.isEmpty(now.get("loanAmount"))){
        	 loanAmount = NumberUtil.getDouble(now.get("applyAmount"),0);
         }else{
        	 loanAmount = NumberUtil.getDouble(now.get("loanAmount"),0);
         }
		if(loanAmount > 999){
			loanAmount = loanAmount/10000;
		}
		
		int wagesType = NumberUtil.getInt(now.get("wagesType"), 0);
		if (wagesType == 2 && income > 0) {
			now.put("cashMonth", now.get("income"));
		}
		
		if (StringUtils.isEmpty(now.get("age"))) {//有几个页面有性别和年龄，如果存在不需要
			now.put("age", IDCardValidate.getCardAge(identifyNo));//根据身份证获取年龄
		}
		if (StringUtils.isEmpty(now.get("sex"))) {
			now.put("sex", IDCardValidate.getCardSex(identifyNo));//根据身份证获取性别
		}
		if (StringUtils.isEmpty(now.get("applyTime"))) {
			now.put("applyTime", new Date());//根据身份证获取性别
		}

		now.put("applyType", 1);
		now.put("loanAmount",loanAmount);
		now.put("income", income);
		now.put("incomeMonth", now.get("totalAmount"));//总经营流水/月
		now.put("cashMonth", now.get("caseAmount"));//现金收入
		now.put("workCmp", now.get("cmpType"));//企业类型
		now.put("pubManageLine", now.get("pubAmount"));//对公账户流水/月
		now.put("havePinan", now.get("haveWeiLi"));
	}
	
	/**
	 * 时间转换成long类型
	 * @param workTime
	 * @return
	 */
	public static long getTimeToLong(String allotTime){
		String[] timeStr = allotTime.split(":");
		long time = 0L;
		if(timeStr.length > 0){
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStr[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(timeStr[1]));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND,0);                                                                                                                                                                                       
			time = cal.getTime().getTime(); 
		}
		return time;
	}
	
	/**
	 * 判断当前时间是否是分单时间
	 * @return
	 */
	public static Boolean isAllotTime(String cityName){
		boolean allotflag = false;
		if(StringUtils.isEmpty(cityName)){
			return allotflag;
		}
		// 查询城市分单时间
		AppParam params = new AppParam();
		params.setService("worktimeCfgService");
		params.setMethod("query");
		params.addAttr("cityName", cityName);
		params.setOrderBy("createTime");
		params.setOrderValue("desc");
		AppResult result =ServiceKey.doCall(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size()>0){
			Map<String,Object> cityWorkMap = result.getRow(0);
			//当前时间
			long nowTime = new Date().getTime();
			String startAllotTime = StringUtil.getString(cityWorkMap.get("startAllotTime"));
			String endAllotTime = StringUtil.getString(cityWorkMap.get("endAllotTime"));
			if(!StringUtils.isEmpty(startAllotTime) && !StringUtils.isEmpty(endAllotTime)){
				long longStartAllotTime = getTimeToLong(startAllotTime); 
				long longEndAllotTime = getTimeToLong(endAllotTime);
				if(nowTime > longStartAllotTime && nowTime < longEndAllotTime){
					allotflag = true;
				}
			}
		}
		return allotflag;
	}
	
	
	/**
	 * 是否网销城市
	 * @param cityName
	 * @return
	 */
	
	public static boolean isNetCity(String cityName){
		Map<String, Object> baseConfig = StoreSeparateUtils.getBaseConfig();
		String allotCitys = StringUtil.getString(baseConfig.get("allotCitys"));
		return allotCitys.contains(cityName);
	}
	
	
	/**
	 * 新数据立即分单或者加入网销池
	 * @return
	 */
	public static AppResult newOrderAllotOrNet(AppParam param) {
		AppResult newResult = new AppResult();
		String applyId = StringUtil.getString(param.getAttr("applyId"));
		AppParam queryParam = new AppParam("netStorePoolService","query");
		queryParam.addAttr("applyId", applyId);
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if (result.getRows().size() == 0) {
			//查询该笔单是否存在
			AppParam queryStoreParam = new AppParam("borrowStoreApplyService", "query");
			queryStoreParam.addAttr("applyId", applyId);
			AppResult queryResult = ServiceKey.doCallNoTx(queryStoreParam, ServiceKey.Key_busi_in);
			//加入网销申请表
			AppParam applyParam = new AppParam();
			applyParam.addAttr("applyId", applyId);
			applyParam.setService("borrowStoreApplyService");
			//更新或插入标识
			boolean updateOrInsert = false;
			String lastStore = "";
			String orderStatus = "";
			String currentOrgId = "";
			if (queryResult.getRows().size() > 0) {
				//离职人的订单清除当前处理人
				lastStore = StringUtil.getString(queryResult.getRow(0).get("lastStore"));
				orderStatus = StringUtil.getString(queryResult.getRow(0).get("orderStatus"));
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(lastStore);
				String roleType = "";
				if (custInfo != null && custInfo.size() > 0) {
					roleType = StringUtil.getString(custInfo.get("roleType"));
					currentOrgId = StringUtil.getString(custInfo.get("orgId"));
					if(CustConstant.CUST_ROLETYPE_0.equals(roleType)){
						AppParam updateParam = new AppParam("borrowStoreApplyService", "update");
						updateParam.addAttr("lastStore","");
						updateParam.addAttr("isHideFlag","1");//1 隐藏记录
						updateParam.addAttr("applyId",applyId);
						ServiceKey.doCall(updateParam, ServiceKey.Key_busi_in);
						
						updateParam.setService("borrowApplyService");
						ServiceKey.doCall(updateParam, ServiceKey.Key_busi_in);
						lastStore = "";
					}
				}
				applyParam.setMethod("updateByBorrowApply");
			}else{
				updateOrInsert = true;
				applyParam.setMethod("insertByBorrowApply");
			}
			AppResult storeResult = ServiceKey.doCall(applyParam, ServiceKey.Key_busi_in);
			int applySize = 0;
			if(updateOrInsert) {
				applySize = NumberUtil.getInt(storeResult.getAttr(DuoduoConstant.DAO_Insert_SIZE),0);
			}else{
				applySize = NumberUtil.getInt(storeResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			}
			//加入无效池标识
			boolean invalidFlag = invalidOrderAddPool(lastStore,orderStatus,queryResult.getRows());
			if(invalidFlag){
				return newResult;
			}
			//是否加入网销池标识
			boolean netFlag = false; 
			//是否插入网销池
			boolean isInsertNet = false;
			//立即分单标识
			String nowOrderAllotFlag = StringUtil.getString(param.getAttr("nowOrderAllotStatus"));
			int nowOrderAllotStatus = 0;
			if(StringUtils.isEmpty(nowOrderAllotFlag)){
				nowOrderAllotStatus = SysParamsUtil.getIntParamByKey("nowOrderAllotStatus", 0);
			}else{
				nowOrderAllotStatus = NumberUtil.getInt(nowOrderAllotFlag,0);
			}
			String cityName = StringUtil.getString(param.getAttr("cityName"));
			//是否是分单时间
			boolean isAllotFlag = isAllotTime(cityName);
			if(applySize > 0 && nowOrderAllotStatus == 1 && isAllotFlag && StringUtils.isEmpty(lastStore)){
				try{
					AppParam storeParam = new AppParam("netStorePoolService", "newOrderNowAllot");
					storeParam.addAttrs(param.getAttr());
					newResult = ServiceKey.doCall(storeParam, ServiceKey.Key_busi_in); //满足条件新订单立即分配
					if(newResult.isSuccess()){
						isInsertNet = Boolean.valueOf(StringUtil.getString(newResult.getAttr("isInsertNet")));
						netFlag = true;
					}
				}catch(Exception e){
					LogerUtil.error(ApplyAllotUtil.class, e, "ApplyAllotUtil allotTransfer 新订单立即分单 error");
				}
			}
			//获取申请渠道
			String channelCode = StringUtil.getString(param.getAttr("channelCode"));
			if(!netFlag || isInsertNet || nowOrderAllotStatus == 0){
				//判断该笔单是否有处理人，有则为二次申请
				if(queryResult.getRows().size() > 0 ){
					// lnde渠道的二次申请不分给当前处理人
					String notAllotChannel = SysParamsUtil.getStringParamByKey("storeNotAllotChannel", "lnde");
					if(!StringUtils.isEmpty(lastStore) && !notAllotChannel.equals(channelCode)){
						String applyName = StringUtil.getString(queryResult.getRow(0).get("applyName"));
						String orgId = StringUtil.getString(queryResult.getRow(0).get("orgId"));
						if(!StringUtils.isEmpty(currentOrgId) && !orgId.equals(currentOrgId)){
							orgId = currentOrgId;
						}
						AppParam sendParam = new AppParam("netStorePoolService", "againAllotOrderDeal");
						StringBuffer buffer = new StringBuffer();
						buffer.append("您的客户");
						buffer.append(applyName);
						buffer.append("进行了二次申请贷款。");
						sendParam.addAttr("customerId",lastStore);
						sendParam.addAttr("orgId",orgId);
						sendParam.addAttr("applyId",applyId);
						sendParam.addAttr("orderStatus",orderStatus);
						sendParam.addAttr("message", buffer.toString());
						ServiceKey.doCall(sendParam, ServiceKey.Key_busi_in);
						return newResult;
					}
				}
				AppParam applyQueryParam = new AppParam("borrowStoreApplyService", "query");
				applyQueryParam.addAttr("applyId", applyId);
				AppResult applyResult = ServiceKey.doCallNoTx(applyQueryParam, ServiceKey.Key_busi_in);
				if(applyResult.getRows().size() > 0){
					//判断是否是二次申请，是则隐藏相关记录
					int applyCount = NumberUtil.getInt(applyResult.getRow(0).get("applyCount"),1);
					if(applyCount > 1){
						updateStoreApplyInfo(applyId);
					}
				}
				String orgId = "";
				// lnde渠道数据特殊处理
				if("lnde".equals(channelCode)){
					orgId = getOrgIdByChannel(cityName);
				}else{
					orgId = getOrgIdByCityCount(cityName);
				}
				if(!StringUtils.isEmpty(orgId)){
					param.addAttr("orgId", orgId);
				}
				String recordDate = DateUtil.toStringByParttern(LocalDateTime.now(),DateUtil.DATE_PATTERN_YYYY_MM_DD);
				//加入网销池
				param.addAttr("recordDate", recordDate);
				param.addAttr("orderType", "1");//一手单
				param.setService("netStorePoolService");
				param.setMethod("saveOrUpdate");
				newResult = ServiceKey.doCall(param, ServiceKey.Key_busi_in);
				int insertSize = NumberUtil.getInt(newResult.getAttr(DuoduoConstant.DAO_Insert_SIZE),0);
				if(insertSize >= 1 && !StringUtils.isEmpty(orgId) && !"lnde".equals(channelCode)){
					//记录门店分配订单数量
					saveOrgAllotRecord(recordDate,orgId,cityName);
				}
			}
		}else{
			//二次申请更改ordertype为1即新申请单
			AppParam netParam = new AppParam("netStorePoolService", "update");
			netParam.addAttr("orderType","1");
			netParam.addAttr("gradeType",param.getAttr("gradeType"));
			netParam.addAttr("applyTime",param.getAttr("applyTime"));
			netParam.addAttr("applyId",applyId);
			ServiceKey.doCall(netParam, ServiceKey.Key_busi_in);
			
			netParam.setService("borrowStoreApplyService");
			netParam.setMethod("updateByBorrowApply");
			ServiceKey.doCall(netParam, ServiceKey.Key_busi_in);
			// 隐藏相关记录
			updateStoreApplyInfo(applyId);
		}
		return newResult;
	}
	/**
	 * 隐藏相关记录更新网销申请表
	 * @param recordDate 记录日期
	 * @param orgId 门店Id
	 * @param cityName 城市
	 */
	public static void updateStoreApplyInfo(String applyId){
		AppParam applyParam = new AppParam("borrowStoreApplyService", "update");
		applyParam.addAttr("applyId",applyId);
		applyParam.addAttr("isHideFlag", "1");
		applyParam.addAttr("backStatus","1");
		applyParam.addAttr("backReDesc","");
		applyParam.addAttr("backDesc","");
		applyParam.addAttr("lastStore","");
		ServiceKey.doCall(applyParam, ServiceKey.Key_busi_in);
	}
	/**
	 * 无效单转移到无效池
	 * @param lastStore 当前处理人
	 * @param orderStatus 处理状态
	 * @param List<Map<String,Object>> 订单信息
	 */
	public static boolean invalidOrderAddPool(String lastStore,String orderStatus,List<Map<String,Object>> orderList){
		if((StoreConstant.STORE_ORDER_7.equals(orderStatus) || StoreConstant.STORE_ORDER_8.equals(orderStatus)) 
				&& orderList.size() > 0){
			//如果订单没有处理人直接把无效单转移到无效池，如果有则不分单也不进网销
			if(StringUtils.isEmpty(lastStore)){
				for(Map<String,Object> map : orderList){
					map.put("orderStatus", orderStatus);
					map.put("invalidDesc", "无效单自动转移到无效订单池");
				}
				AppParam poolParam = new AppParam("invalidStorePoolService","batchInsertInvalidPool");
				poolParam.addAttr("orderList", orderList);
				ServiceKey.doCall(poolParam, ServiceKey.Key_busi_in);
			}
			return true;
		}
		return false;
	}
	/**
	 * 记录门店分配订单数量
	 * @param recordDate 记录日期
	 * @param orgId 门店Id
	 * @param cityName 城市
	 */
	public static void saveOrgAllotRecord(String recordDate,String orgId,String cityName){
		AppParam allotParam = new AppParam("orgAllotRecordService", "saveOrUpdate");
		allotParam.addAttr("recordDate", recordDate);
		allotParam.addAttr("orgId", orgId);
		allotParam.addAttr("cityName", cityName);
		allotParam.addAttr("addApplyCount", "1");
		ServiceKey.doCall(allotParam, ServiceKey.Key_busi_in);
	}
	/***
	 * 根据各个门店分单情况确定网销池分配的门店ID
	 * 如果各个门店分单总数均达到最大值
	 * 则不指定门店Id，否则指定分配最少的门店
	 * @return
	 */
	public static String getOrgIdByCityCount(String cityName){
		// 通过城市获取网销门店
		List<Map<String,Object>> orglist = OrgUtils.getNetOrgListByCity(cityName);
		if(orglist.size() == 1){ // 一个门店直接返回orgId
			return StringUtil.getString(orglist.get(0).get("orgId"));
		}
		// 查询分配表中各个门店分单总数
		AppParam queryParam = new AppParam("orgAllotRecordService","queryOrgGroupByCity");
		queryParam.addAttr("cityName", cityName);
		AppResult result  = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			int totalSize = result.getRows().size();
			//  一个门店直接返回orgId
			if(totalSize == 1){
				return StringUtil.getString(result.getRow(0).get("orgId"));
			}
			for(int i = 0 ; i < totalSize; i++){
				// 取出最小分配的门店Id
				String orgId = StringUtil.getString(result.getRow(i).get("orgId"));
				int applyCount = NumberUtil.getInt(result.getRow(i).get("applyCount"),0);
				// 获取配置的门店最大分单量
				Map<String,Object> orgMap = StoreSeparateUtils.getOrgWorkByOrgId(NumberUtil.getInt(orgId,0));
				if(orgMap != null){
					int orgAllotMaxCount = NumberUtil.getInt(orgMap.get("orgMaxCount"),0);
					if(applyCount < orgAllotMaxCount){
						return orgId;
					}else if(applyCount >= orgAllotMaxCount  && i == (totalSize - 1)){
						return "";
					}
				}
			}
		}else{
			if(orglist.size() > 0){
				AppParam allotParam = new AppParam("orgAllotRecordService", "saveOrUpdate");
				allotParam.addAttr("recordDate", DateUtil.toStringByParttern(LocalDate.now(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
				allotParam.addAttr("cityName", cityName);
				allotParam.addAttr("applyCount", "0");
				for(Map<String,Object> map : orglist){
					allotParam.addAttr("orgId", map.get("orgId"));
					ServiceKey.doCall(allotParam, ServiceKey.Key_busi_in);
				}
				return StringUtil.getString(orglist.get(0).get("orgId"));
			}
		}
		return "";
	}
	
	/***
	 * 上海市的直接分给上海同盛门店
	 * 北京市的北京1门店分800，北京2门店分150
	 * @return
	 */
	public static String getOrgIdByChannel(String cityName){
		// 上海市的直接分给上海同盛门店
		if("上海市".equals(cityName)){
			return "239";
		}else if("北京市".equals(cityName)){
			//北京朝外1门店分单数量缓存
			int bj1AllotCount = NumberUtil.getInt(RedisUtils.getRedisService().get("storeOrgAllotCount_207"), 0);
			//北京朝外2门店分单数量缓存
			int bj2AllotCount = NumberUtil.getInt(RedisUtils.getRedisService().get("storeOrgAllotCount_237"), 0);
			Calendar cal = Calendar.getInstance();
			// 当前小时数
			int currentHours = cal.get(Calendar.HOUR_OF_DAY);
			// 当天剩余小时数
			int lastHours = 24 - currentHours + 1;
			if(bj2AllotCount > bj1AllotCount){
				bj1AllotCount ++;
				RedisUtils.getRedisService().set("storeOrgAllotCount_207", (Serializable) bj1AllotCount, 60 * 60 * lastHours);
				return "207";
			}else{
				//北京2最多分150单
				if(bj2AllotCount > 150){
					bj1AllotCount ++;
					RedisUtils.getRedisService().set("storeOrgAllotCount_207", (Serializable) bj1AllotCount,60 * 60 * lastHours);
					return "207";
				}else{
					bj2AllotCount ++;
					RedisUtils.getRedisService().set("storeOrgAllotCount_237", (Serializable) bj2AllotCount,60 * 60 * lastHours);
					return "237";
				}
			}
		}
		return "";
	}

	/**
	 * 转换挂卖的字段
	 * @param changeMap
	 */
	public static Map<String,Object> conveSaleType(Map<String, Object> changeMap) {
		
		int workType = NumberUtil.getInt(changeMap.get("workType"),1);
		String carType = StringUtil.getString(changeMap.get("carType"));
		String channelDetail = StringUtil.getString(changeMap.get("channelDetail"));
		int loanDeadline = NumberUtil.getInt(changeMap.get("loanDeadline"),12);
		double income =  NumberUtil.getDouble(changeMap.get("income"),0);
		String grade = StringUtil.getString(changeMap.remove("grade"));
		double pubManageLine = NumberUtil.getDouble(changeMap.remove("pubManageLine"),0);
		double incomeMonth = NumberUtil.getDouble(changeMap.remove("incomeMonth"),0);
		int isAutoSale = NumberUtil.getInt(changeMap.remove("isAutoSale"),1);//默认自动
		int isSeniorCust = NumberUtil.getInt(changeMap.remove("isSeniorCust"),0);
		
		double loanAmount = NumberUtil.getDouble(changeMap.get("loanAmount"),0);
		
		if(StringUtils.isEmpty(changeMap.get("loanAmount"))){
			changeMap.put("loanAmount", NumberUtil.getDouble(changeMap.get("applyAmount"),0));
		}
		
		/*if(isSeniorCust == 0 && isAutoSale == 1){//自动判断不用
			isSeniorCust = SeniorCfgUtils.getSeniorCustType(changeMap);
		}*/
		
		int robType = SeniorCfgUtils.judgeRobType(loanAmount,isSeniorCust, grade, isAutoSale);
		
		changeMap.put("isSeniorCust", isSeniorCust);
		changeMap.put("income", SeniorCfgUtils.randomIncome(workType, income));
		changeMap.put("robType", robType);
		changeMap.put("sourceName", channelDetail);
		changeMap.put("wagesType", NumberUtil.getInt(changeMap.get("wagesType"), 0));//wagesType可能不存在需要默认值,没有传0有传本身
		if (!StringUtils.isEmpty(changeMap.get("isDataSys"))) {
			changeMap.put("sourceType", 8);
		} else {
			changeMap.put("sourceType", 5);
		}
		changeMap.put("pubAmount", SeniorCfgUtils.tranPubLine(pubManageLine));
		changeMap.put("totalAmount", SeniorCfgUtils.tranTotalLine(incomeMonth));
		changeMap.put("loanDeadline", loanDeadline <= 0 ? 12 : loanDeadline);//贷款期限默认12
		
		if(BorrowConstant.work_type_1.equals(workType)){
			changeMap.put("workType", "5");//自由创业者
		}else if(BorrowConstant.work_type_5.equals(workType)){
			changeMap.put("workType", "6");//学生
		}
		
		if(StringUtils.isEmpty(carType) || "0".equals(carType)){
			changeMap.put("carType", "2");//无车
		}
		
		if(StringUtils.isEmpty(changeMap.get("caseAmount"))){
			changeMap.put("caseAmount", changeMap.get("cashMonth"));
		}
		if(StringUtils.isEmpty(changeMap.get("cmpType"))){
			changeMap.put("cmpType", changeMap.get("workCmp"));
		}
		if(StringUtils.isEmpty(changeMap.get("haveWeiLi"))){
			changeMap.put("haveWeiLi", changeMap.get("havePinan"));
		}
		if(StringUtils.isEmpty(changeMap.get("realName"))){
			changeMap.put("realName", changeMap.get("applyName"));
		}
		if(StringUtils.isEmpty(changeMap.get("loanDesc"))){
			changeMap.put("loanDesc", changeMap.get("desc"));
		}
		if(StringUtils.isEmpty(changeMap.get("applyTime"))){
			changeMap.put("applyTime", new Date());
		}
		
		return changeMap;
	}
}
