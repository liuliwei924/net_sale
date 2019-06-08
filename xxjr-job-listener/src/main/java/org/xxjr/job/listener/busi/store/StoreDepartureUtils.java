package org.xxjr.job.listener.busi.store;

import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.util.JobUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/***
 * 门店人员离职统计工具类
 * @author ZQH
 *
 */
public class StoreDepartureUtils {

	/**
	 * 门店经理离职签单基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeSignDeparture(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("storeSignDeparture >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int size = 0;
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","storeSignDeparture");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			for(Map<String,Object> map : result.getRows()){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumStoreBaseService","insertStoreBase");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("customerId", map.get("customerId"));
				insertParam.addAttr("orgId", map.get("orgId"));
				insertParam.addAttr("orgName", map.get("orgName"));
				insertParam.addAttr("cityName", map.get("cityName"));
				insertParam.addAttr("realName", map.get("realName"));
				insertParam.addAttr("isNet", "1");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("storeSignDeparture >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreDepartureUtils.class,e, "storeSignDeparture error");
			JobUtil.addProcessExecute(processId, "统计门店经理签单基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理离职签单月度基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeSignDepartMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("storeSignDepartMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int size = 0;
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","storeSignDepartMonth");
			queryParam.addAttr("toMonth", toMonth);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			for(Map<String,Object> map : result.getRows()){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumStoreBaseMonthService","insertStoreBaseMonth");
				insertParam.addAttr("recordDate", toMonth);
				insertParam.addAttr("customerId", map.get("customerId"));
				insertParam.addAttr("orgId", map.get("orgId"));
				insertParam.addAttr("orgName", map.get("orgName"));
				insertParam.addAttr("cityName", map.get("cityName"));
				insertParam.addAttr("realName", map.get("realName"));
				insertParam.addAttr("isNet", "1");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("storeSignDepartMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreDepartureUtils.class,e, "storeSignDepartMonth error");
			JobUtil.addProcessExecute(processId, "统计门店经理签单月度基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理离职回款基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeReLoanDeparture(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("storeReLoanDeparture >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int size = 0;
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","storeReLoanDeparture");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			for(Map<String,Object> map : result.getRows()){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumStoreBaseService","insertStoreBase");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("customerId", map.get("customerId"));
				insertParam.addAttr("orgId", map.get("orgId"));
				insertParam.addAttr("orgName", map.get("orgName"));
				insertParam.addAttr("cityName", map.get("cityName"));
				insertParam.addAttr("realName", map.get("realName"));
				insertParam.addAttr("isNet", "1");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("storeReLoanDeparture >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreDepartureUtils.class,e, "storeReLoanDeparture error");
			JobUtil.addProcessExecute(processId, "统计门店经理回款基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理离职回款月度基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeReloanDepartMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("storeReloanDepartMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int size = 0;
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","storeReLoanDepartMonth");
			queryParam.addAttr("toMonth", toMonth);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			for(Map<String,Object> map : result.getRows()){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumStoreBaseMonthService","insertStoreBaseMonth");
				insertParam.addAttr("recordDate", toMonth);
				insertParam.addAttr("customerId", map.get("customerId"));
				insertParam.addAttr("orgId", map.get("orgId"));
				insertParam.addAttr("orgName", map.get("orgName"));
				insertParam.addAttr("cityName", map.get("cityName"));
				insertParam.addAttr("realName", map.get("realName"));
				insertParam.addAttr("isNet", "1");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("storeReloanDepartMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreDepartureUtils.class,e, "storeReloanDepartMonth error");
			JobUtil.addProcessExecute(processId, "统计门店经理回款月度基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理离职成本基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeCostDeparture(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("storeCostDeparture >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int size = 0;
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","storeCostDeparture");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			for(Map<String,Object> map : result.getRows()){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumStoreBaseService","insertStoreBase");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("customerId", map.get("customerId"));
				insertParam.addAttr("orgId", map.get("orgId"));
				insertParam.addAttr("orgName", map.get("orgName"));
				insertParam.addAttr("cityName", map.get("cityName"));
				insertParam.addAttr("realName", map.get("realName"));
				insertParam.addAttr("costCount", map.get("costCount"));
				insertParam.addAttr("isNet", "1");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("storeCostDeparture >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreDepartureUtils.class,e, "storeCostDeparture error");
			JobUtil.addProcessExecute(processId, "统计门店经理离职成本基本数据报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理离职成本月度基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeCostDepartMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("storeCostDepartMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int size = 0;
			//获取统计数据
			AppParam queryParam = new AppParam("storeListOptExtService","storeCostDepartMonth");
			queryParam.addAttr("toMonth", toMonth);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			for(Map<String,Object> map : result.getRows()){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumStoreBaseMonthService","insertStoreBaseMonth");
				insertParam.addAttr("recordDate", toMonth);
				insertParam.addAttr("customerId", map.get("customerId"));
				insertParam.addAttr("orgId", map.get("orgId"));
				insertParam.addAttr("orgName", map.get("orgName"));
				insertParam.addAttr("cityName", map.get("cityName"));
				insertParam.addAttr("realName", map.get("realName"));
				insertParam.addAttr("costCount", map.get("costCount"));
				insertParam.addAttr("isNet", "1");
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("storeCostDepartMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(StoreDepartureUtils.class,e, "storeCostDepartMonth error");
			JobUtil.addProcessExecute(processId, "统计门店经理离职成本月度基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 业务员用户登录状态
	 * @param processId
	 */
	public static void dealStoreOffline(Object processId) {
		AppParam queryParams = new AppParam("busiCustService", "queryCust");
		queryParams.addAttr("loginStatus", "1");
		queryParams.addAttr("orgIdNotNull", "1");
		queryParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParams);
		List<Map<String,Object>> crmList = queryResult.getRows();
		for(Map<String,Object> map : crmList){
			String customerId = "";
			try{
				customerId = StringUtil.getString(map.get("customerId"));
				String signId = (String)RedisUtils.getRedisService()
						.get(StoreUserUtil.USER_KEY + customerId);
				int loginStatus = NumberUtil.getInt(map.get("loginStatus"),0);
				if(StringUtils.isEmpty(signId) && loginStatus != 0 && loginStatus != 4){
					//更新用户登陆状态
					StoreUserUtil.updateUserLoginStatus(customerId, 0);
					//添加退出记录
					StoreUserUtil.addStoreOnlineRecord(customerId, 0, "无操作自动退出");
				}
			}catch(Exception e){
				LogerUtil.error(StoreDepartureUtils.class, e, "用户离线判断报错 customerId=" + customerId);
			}
		}
	}
}
