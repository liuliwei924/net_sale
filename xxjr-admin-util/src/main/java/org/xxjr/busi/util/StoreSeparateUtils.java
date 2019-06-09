package org.xxjr.busi.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/***
 * 分单配置相关工具类
 * @author zqh
 *
 */
public class StoreSeparateUtils {
	/** 跟进平台全局配置key**/
	public static final String STORE_BASE_CONFIG_KEY="store_base_configKey_";
	/** 跟进平台分单情况配置key**/
	public static final String STORE_ORDER_CONFIG_KEY="store_order_configKey_";
	/** 跟进平台能力值配置key**/
	public static final String STORE_ABILITY_CONFIG_KEY="store_ability_configKey_";
	/** 跟进平台等级配置key**/
	public static final String STORE_RANK_CONFIG_KEY="store_rank_configKey_";
	/** 门店工作配置key**/
	public static final String STORE_WORK_CONFIG_KEY="store_work_configKey_";
	/**门店人员分配新单计数开始日期key**/
	public static final String STORE_START_ALLOT_RECORDDATE_KEY ="store_start_allot_recordDate_key_";
	/** 缓存7天*/
	public static final int base_cache_time = 60*60*24*7;
	
	/**门店成本维护操作缓存key**/
	public static final String STORE_ORG_COSTDEAL_COUNT_KEY_="store_org_cost_dealCount_key_";
	/**
	 * 获取全局配置
	 * @return
	 */
	public static Map<String,Object> getBaseConfig(){
		@SuppressWarnings("unchecked")
		Map<String,Object> baseMap =(Map<String,Object>)RedisUtils.getRedisService().get(STORE_BASE_CONFIG_KEY);
		if(baseMap == null){
			baseMap = refreshBaseConfig();
		}
		return baseMap;
	}
	
	/**
	 * 刷新全局配置
	 * @return
	 */
	private static Map<String,Object> refreshBaseConfig(){
		AppParam params  = new AppParam();
		params.setService("baseCfgService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult baseResult = RemoteInvoke.getInstance().callNoTx(params);
		
		Map<String,Object> baseMap =  new HashMap<String,Object>();
		if(baseResult.getRows().size()>0){
			baseMap =  (Map<String, Object>) baseResult.getRow(0);
			RedisUtils.getRedisService().set(STORE_BASE_CONFIG_KEY,(Serializable) baseMap, base_cache_time);
		}
		return baseMap;
	}
	
	/**
	 * 获取分单情况配置
	 * @return
	 */
	public static List<Map<String,Object>> getOrderConfig(){
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> orderList = (List<Map<String,Object>>)RedisUtils.getRedisService().get(STORE_ORDER_CONFIG_KEY);
		if(orderList == null){
			orderList = refreshOrderConfig();
		}
		return orderList;
	}
	
	/**
	 * 刷新分单情况配置
	 * @return
	 */
	private static List<Map<String,Object>> refreshOrderConfig(){
		AppParam params  = new AppParam();
		params.setService("transOrderCfgService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult orderResult = RemoteInvoke.getInstance().callNoTx(params);
		
		List<Map<String,Object>> orderList =  new ArrayList<Map<String,Object>>();
		if(orderResult.getRows().size()>0){
			orderList =  (List<Map<String,Object>>) orderResult.getRows();
			RedisUtils.getRedisService().set(STORE_ORDER_CONFIG_KEY,(Serializable) orderList, base_cache_time);
		}
		return orderList;
	}
	
	
	/**
	 * 获取能力值配置
	 * @return
	 */
	public static Map<String,Object> getAbilityConfig(){
		@SuppressWarnings("unchecked")
		Map<String,Object> abilMap =(Map<String,Object>)RedisUtils.getRedisService().get(STORE_ABILITY_CONFIG_KEY);
		if(abilMap == null){
			abilMap = refreshAbilityConfig();
		}
		return abilMap;
	}
	
	/**
	 * 刷新能力值配置
	 * @return
	 */
	private static Map<String,Object> refreshAbilityConfig(){
		AppParam params  = new AppParam();
		params.setService("abilityValueCfgService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult abilResult = RemoteInvoke.getInstance().callNoTx(params);
		
		Map<String,Object> abilMap =  new HashMap<String,Object>();
		if(abilResult.getRows().size()>0){
			abilMap =  (Map<String, Object>) abilResult.getRow(0);
			RedisUtils.getRedisService().set(STORE_ABILITY_CONFIG_KEY,(Serializable) abilMap, base_cache_time);
		}
		return abilMap;
	}
	

	/**
	 * 获取转单时间
	 * @param orderLevel 1-优质单 2-未填写单
	 * @param orderType 1-一手单  2-二手单  3-三手单  4-四手单 
	 * @return
	 */
	public static Date getTransTime(int orderLevel,int orderType){
		
		List<Map<String, Object>> orderCfgList = getOrderConfig();
		Map<String, Object> orderCfgMap = null;
		for(Map<String,Object> map : orderCfgList){
			int orderLevelTmp = NumberUtil.getInt(map.get("orderLevel"));
			if(orderLevel == orderLevelTmp){
				orderCfgMap = map;
				break;
			}
		}
		int transOrderDay = 3;
		if(orderCfgMap != null){
			transOrderDay = NumberUtil.getInt(orderCfgMap.get("transOrderDay"+orderType),3);
		}
		
		return DateUtil.getNextDay(new Date(),transOrderDay);
	}
	
	/**
	 * 查询我的专属订单数量
	 * @return
	 */
	public static int queryMayOrderCount(String customerId,int orderType){
		AppParam queryOrder = new AppParam("exclusiveOrderService", "queryCount");
		queryOrder.addAttr("customerId", customerId);
		queryOrder.addAttr("orderType", orderType);
		queryOrder.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult baseResult = RemoteInvoke.getInstance().callNoTx(queryOrder);
		int count = NumberUtil.getInt(baseResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		return count;
	}
	
	/**
	 * 查询专属订单数量
	 * @return
	 */
	public static int queryOrderCount(String customerId){
		AppParam queryOrder = new AppParam("exclusiveOrderService", "queryCount");
		queryOrder.addAttr("customerId", customerId);
		queryOrder.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult baseResult = RemoteInvoke.getInstance().callNoTx(queryOrder);
		int count = NumberUtil.getInt(baseResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		return count;
	}
	
	/**
	 * 查询专属订单
	 * @param applyId
	 * @return
	 */
	public static int queryExeOrderCount(Object applyId){
		AppParam queryParam = new AppParam("exclusiveOrderService", "queryCount");
		queryParam.addAttr("applyId", applyId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int count = NumberUtil.getInt(qeuryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		return count;
	}
	
	/**
	 * 获取等级配置
	 * @return
	 */
	public static List<Map<String,Object>> getRankConfig(){
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> rankList =(List<Map<String,Object>>)RedisUtils.getRedisService().get(STORE_RANK_CONFIG_KEY);
		if(rankList == null){
			rankList = refreshRankConfig();
		}
		return rankList;
	}
	
	/**
	 * 刷新等级配置
	 * @return
	 */
	public static List<Map<String,Object>> refreshRankConfig(){
		AppParam params = new AppParam();
		params.setService("borrowRankCfgService");
		params.setMethod("query");
		params.setOrderBy("gradeCode");
		params.setOrderValue("asc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		List<Map<String,Object>> rankList =  new ArrayList<Map<String,Object>>();
		if(result.getRows().size() > 0 ){
			rankList =  (List<Map<String,Object>>) result.getRows();
			RedisUtils.getRedisService().set(STORE_RANK_CONFIG_KEY,(Serializable) rankList, base_cache_time);
		}
		return rankList;
	}
	
	/**
	 * 根据能力等级获取等级配置
	 * @return
	 */
	public static Map<String,Object> getRankConfigByGrade(int gradeCode){
		List<Map<String,Object>> rankList = getRankConfig();
		Map<String,Object> gradeMap = null;
		for(Map<String,Object> map : rankList){
			int gradeCodeTemp = NumberUtil.getInt(map.get("gradeCode"));
			if(gradeCode == gradeCodeTemp){
				gradeMap = map;
			}
		}
		return gradeMap;
	}
	
	/**
	 * 获取门店工作配置信息
	 * @return
	 */
	public static List<Map<String,Object>> getOrgWorkConfig(){
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> orderList = (List<Map<String,Object>>)RedisUtils.getRedisService().get(STORE_WORK_CONFIG_KEY);
		if(orderList == null){
			orderList = refreshOrgWorkConfig();
		}
		return orderList;
	}
	
	/***
	 * 刷新门店配置信息
	 * @return
	 */
	public static List<Map<String,Object>> refreshOrgWorkConfig(){
		AppResult result = new AppResult();
		List<Map<String,Object>> cityList = new ArrayList<Map<String,Object>>();
		AppParam params = new AppParam();
		params.setService("worktimeCfgService");
		params.setMethod("query");
		params.setOrderBy("createTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			cityList = result.getRows();
			RedisUtils.getRedisService().set(STORE_WORK_CONFIG_KEY,(Serializable) cityList, base_cache_time);
		}
		return cityList;
	}
	
	/**
	 * 根据门店Id获取门店工作配置
	 * @return
	 */
	public static Map<String,Object> getOrgWorkByOrgId(int orgId){
		List<Map<String,Object>> orgList = getOrgWorkConfig();
		Map<String,Object> orgMap = null;
		for(Map<String,Object> map : orgList){
			int orgIdTemp = NumberUtil.getInt(map.get("orgId"),0);
			if(orgId == orgIdTemp){
				orgMap = map;
			}
		}
		return orgMap;
	}

	/***
	 * 查询门店人员是否有回款
	 * @param orgId
	 * @return
	 */
	public static boolean isExitRetAmout(String customerId){
		// 查询是否有回款标识 0-未开启 1-开启
		int storeIsHaveAmoutFlag = SysParamsUtil.getIntParamByKey("storeIsHaveAmoutFlag", 0);
		if(storeIsHaveAmoutFlag == 0){
			return true;
		}
		//查询时间从2019年1月1日开始
		String startRecordDate = (String) RedisUtils.getRedisService().get(STORE_START_ALLOT_RECORDDATE_KEY + customerId);
		String storeAllotRecordDate = SysParamsUtil.getStringParamByKey("storeStartAllotRecordDate", "2019-01-01");
		if(StringUtils.isEmpty(startRecordDate)){
			AppParam custParam  = new AppParam("custLevelService","query");
			custParam.addAttr("customerId", customerId);
			custParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult custResult = RemoteInvoke.getInstance().callNoTx(custParam);
			if(custResult.getRows().size() > 0){
				String stopAllotDate = StringUtil.getString(custResult.getRow(0).get("stopAllotDate"));
				if(!StringUtils.isEmpty(stopAllotDate)){
					startRecordDate = stopAllotDate;
				}
			}
			String date = StringUtils.isEmpty(startRecordDate) ? storeAllotRecordDate : startRecordDate;
			RedisUtils.getRedisService().set(STORE_START_ALLOT_RECORDDATE_KEY + customerId,(Serializable) date);
		}
		int dateCompare = storeAllotRecordDate.compareTo(startRecordDate);
		if(dateCompare > 0){
			RedisUtils.getRedisService().set(STORE_START_ALLOT_RECORDDATE_KEY + customerId,(Serializable) storeAllotRecordDate);
			return true;
		}
		AppParam queryParam  = new AppParam("treatSuccessService","queryCount");
		queryParam.addAttr("customerId", customerId);
		queryParam.addAttr("status", "2");
		queryParam.addAttr("startRecordDate", startRecordDate);
		queryParam.addAttr("endRecordDate", DateUtil.getSimpleFmt(new Date()));
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int totalCount = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		if(totalCount > 0){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断用户是否有权限转单
	 * @param applyId
	 * @return
	 */
	public static boolean isSignOrderFlag(String applyId,String roleType){
		if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
			AppParam queryParam = new AppParam("treatInfoService", "queryCount");
			queryParam.addAttr("applyId", applyId);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult qeuryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int count = NumberUtil.getInt(qeuryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
			if(count >= 1){
				return false;
			}
		}
		return true;
	}
	
	/***
	 * 查询门店人员分配新单总数(成本单)
	 * @param orgId
	 * @return
	 */
	public static int queryAllotNewOrderCount(String customerId){
		//查询时间从2019年1月1日开始
		String startRecordDate = (String) RedisUtils.getRedisService().get(STORE_START_ALLOT_RECORDDATE_KEY + customerId);
		String storeAllotRecordDate = SysParamsUtil.getStringParamByKey("storeStartAllotRecordDate", "2019-01-01");
		if(StringUtils.isEmpty(startRecordDate)){
			AppParam custParam  = new AppParam("custLevelService","query");
			custParam.addAttr("customerId", customerId);
			custParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult custResult = RemoteInvoke.getInstance().callNoTx(custParam);
			if(custResult.getRows().size() > 0){
				String stopAllotDate = StringUtil.getString(custResult.getRow(0).get("stopAllotDate"));
				if(!StringUtils.isEmpty(stopAllotDate)){
					startRecordDate = stopAllotDate;
				}
			}
			String date = StringUtils.isEmpty(startRecordDate) ? storeAllotRecordDate : startRecordDate;
			RedisUtils.getRedisService().set(STORE_START_ALLOT_RECORDDATE_KEY + customerId,(Serializable) date);
		}
		int dateCompare = storeAllotRecordDate.compareTo(startRecordDate);
		if(dateCompare > 0){
			RedisUtils.getRedisService().set(STORE_START_ALLOT_RECORDDATE_KEY + customerId,(Serializable) storeAllotRecordDate);
			return 0;
		}
		AppParam queryParam  = new AppParam("orgCostRecordService","queryCostCount");
		queryParam.addAttr("customerId", customerId);
		queryParam.addAttr("recordDate", startRecordDate);
		queryParam.addAttr("endRecordDate", DateUtil.getSimpleFmt(new Date()));
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
		int allotNewOrderCount = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
		return allotNewOrderCount;
	}
}
