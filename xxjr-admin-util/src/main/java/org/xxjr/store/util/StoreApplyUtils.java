package org.xxjr.store.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;

/***
 * 门店查询相关缓存
 * @author loys
 *
 */
public class StoreApplyUtils {
	/** 申请单主表信息缓存**/
	public static final String STORE_APPLY_INFO = "store_apply_info_";
	/** 申请单主表2信息缓存**/
	public static final String BORROW_APPLY_INFO = "borrow_apply_info_";
	
	/** 申请单主要信息缓存**/
	public static final String STORE_APPLY_MAININFO = "store_apply_main_info_";
	/** 申请单基本信息缓存**/
	public static final String STORE_APPLY_BASEINFO = "store_apply_base_info_";
	/** 申请单专属单信息缓存**/
	public static final String STORE_APPLY_EXEC_ORDER = "store_apply_exec_order_";
	
	/** 订单访客记录信息缓存**/
	public static final String STORE_APPLY_VISIT_RECORD = "store_apply_visit_record_";
	/** 订单预约记录信息缓存**/
	public static final String STORE_APPLY_BOOK_RECORD = "store_apply_book_record_";
	
	/** 订单签单历史记录信息缓存**/
	public static final String STORE_APPLY_SIGN_RECORD = "store_apply_sign_record_";
	/** 订单回款记录信息缓存**/
	public static final String STORE_APPLY_RET_RECORD = "store_apply_ret_record_";
	/** 订单进件记录信息缓存**/
	public static final String STORE_APPLY_CONTRACT_RECORD = "store_apply_contract_record_";
	
	/** 订单签单主表记录信息缓存**/
	public static final String STORE_APPLY_TREAT_SIGN_RECORD = "store_apply_treat_sign_record_";
	
	/** 订单通话记录信息缓存**/
	public static final String STORE_CALL_RECORD = "store_call_record_";
	/** 订单通话录音记录信息缓存**/
	public static final String STORE_CALL_AUDIO_RECORD = "store_call_audio_record_";
	
	/** 订单投诉信息缓存**/
	public static final String STORE_COMPLAINT_RECORD = "store_complain_record_";
	
	/** 门店跟进记录缓存**/
	public static final String STORE_FOLLOW_RECORD = "store_follow_record_";
	
	/** 个人通知未读信息缓存**/
	public static final String STORE_CUST_NOTIFY_INFO = "store_cust_nofity_info_";
	
	/** 个人通知已读信息缓存**/
	public static final String STORE_CUST_FINISH_NOTIFY = "store_cust_finish_nofity_";
	
	/** 个人所有通知信息缓存**/
	public static final String STORE_CUST_ALL_NOTIFY_INFO = "store_cust_all_nofity_info_";
	
	/** 查询门店所有组缓存**/
	public static final String STORE_ORG_ALL_GROUP = "store_org_all_group_";
	
	/** 查询门店所有队缓存**/
	public static final String STORE_ORG_ALL_TEAM = "store_org_all_team_";
	
	/***
	 * 获取门店跟进记录
	 * @return
	 */
	public static AppResult getStoreFollowRecord(AppParam params){
		AppResult result = new AppResult();
		Object dateFlag = params.getAttr("dateFlag");
		Object isAdmin = params.getAttr("isAdmin");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_FOLLOW_RECORD + params.getAttr("applyId") + "_" + dateFlag + isAdmin);
		if(recordList == null){
			result = refreshStoreFollowRecord(params);
		}else if(recordList.size() > 0){
			int lastLine = recordList.size()-1;
			int totalPage = NumberUtil.getInt(recordList.get(lastLine).get("totalPage"),0);
			int totalRecords = NumberUtil.getInt(recordList.get(lastLine).get("totalRecords"),0);
			result.getPage().setTotalPage(totalPage);
			result.getPage().setTotalRecords(totalRecords);
			// 移除页码一行数据
			recordList.remove(lastLine);
			result.addRows(recordList);
		}
		return result;
	}
	
	/***
	 * 获取投诉记录
	 * @return
	 */
	public static List<Map<String, Object>> getComplaintRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_COMPLAINT_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshComplaintRecord(params);
		}
		return recordList;
	}
	/***
	 * 获取通话记录
	 * @return
	 */
	public static List<Map<String, Object>> getCallRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_CALL_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshCallRecord(params);
		}
		return recordList;
	}
	
	/***
	 * 获取通话录音记录信
	 * @return
	 */
	public static List<Map<String, Object>> getCallAudioRecord(AppParam params){
		Object isAdmin = params.getAttr("isAdmin");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_CALL_AUDIO_RECORD + params.getAttr("applyId") + isAdmin);
		recordList = null;
		if(recordList == null){
			recordList = refreshCallAudioRecord(params);
		}
		return recordList;
	}
	
	
	/***
	 * 获取访客记录信息
	 * @return
	 */
	public static List<Map<String, Object>> getApplyVisitRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_APPLY_VISIT_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshApplyVisitRecord(params);
		}
		return recordList;
	}
	
	/***
	 * 获取预约记录信息
	 * @return
	 */
	public static List<Map<String, Object>> getApplyBookRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_APPLY_BOOK_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshApplyBookRecord(params);
		}
		return recordList;
	}
	
	
	
	/***
	 * 获取签单历史记录信息
	 * @return
	 */
	public static List<Map<String, Object>> getApplySignRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshApplySignRecord(params);
		}
		return recordList;
	}
	
	/***
	 * 获取签单主表记录信息
	 * @return
	 */
	public static List<Map<String, Object>> getApplyTreatSignRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshApplyTreatSign(params);
		}
		return recordList;
	}
	
	/***
	 * 获取回款记录信息
	 * @return
	 */
	public static List<Map<String, Object>> getApplyRetRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_APPLY_RET_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshApplyRetRecord(params);
		}
		return recordList;
	}
	
	/***
	 * 获取进件记录信息
	 * @return
	 */
	public static List<Map<String, Object>> getApplyContractRecord(AppParam params){
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
		.get(STORE_APPLY_CONTRACT_RECORD + params.getAttr("applyId"));
		if(recordList == null){
			recordList = refreshApplyContractRecord(params);
		}
		return recordList;
	}

	/***
	 * 获取专属单信息
	 * @return
	 */
	public static Map<String, Object> getApplyExecOrder(String applyId){
		@SuppressWarnings("unchecked")
		Map<String,Object> applyMap = (Map<String, Object>) RedisUtils.getRedisService()
			.get(STORE_APPLY_EXEC_ORDER + applyId);
		if(applyMap == null){
			applyMap = refreshApplyExecOrder(applyId);
		}
		return applyMap;
	}
	

	/***
	 * 获取单子主表2信息
	 * @return
	 */
	public static Map<String, Object> getApplyInfo(String applyId){
		@SuppressWarnings("unchecked")
		Map<String,Object> applyMap = (Map<String, Object>) RedisUtils.getRedisService()
			.get(BORROW_APPLY_INFO + applyId);
		if(applyMap == null){
			applyMap = refreshApplyInfo(applyId);
		}
		return applyMap;
	}
	
	/***
	 * 获取单子主表信息
	 * @return
	 */
	public static Map<String, Object> getStoreApplyInfo(String applyId){
		@SuppressWarnings("unchecked")
		Map<String,Object> applyMap = (Map<String, Object>) RedisUtils.getRedisService()
			.get(STORE_APPLY_INFO + applyId);
		if(applyMap == null){
			applyMap = refreshStoreApplyInfo(applyId);
		}
		return applyMap;
	}
	
	/***
	 * 获取单子主要信息
	 * @return
	 */
	public static Map<String, Object> getApplyMainInfo(String applyId){
		@SuppressWarnings("unchecked")
		Map<String,Object> applyMap = (Map<String, Object>) RedisUtils.getRedisService()
			.get(STORE_APPLY_MAININFO + applyId);
		if(applyMap == null){
			applyMap = refreshApplyMainInfo(applyId);
		}
		return applyMap;
	}
	
	/***
	 * 获取单子基本信息
	 * @return
	 */
	public static Map<String, Object> getApplyBaseInfo(String applyId){
		@SuppressWarnings("unchecked")
		Map<String,Object> applyMap = (Map<String, Object>) RedisUtils.getRedisService()
			.get(STORE_APPLY_BASEINFO + applyId);
		if(applyMap == null){
			applyMap = refreshApplyBaseInfo(applyId);
		}
		return applyMap;
	}
	
	/***
	 * 获取个人未读通知信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult getCustNotifyInfo(AppParam params){
		AppResult result = new AppResult();
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_CUST_NOTIFY_INFO + params.getAttr("customerId"));
		if(recordList == null || recordList.size() == 0){
			result = refreshCustNofityInfo(params);
		}else if(recordList.size() > 0){
			int lastLine = recordList.size() - 1;
			int totalPage = NumberUtil.getInt(recordList.get(lastLine).get("totalPage"),0);
			int totalRecords = NumberUtil.getInt(recordList.get(lastLine).get("totalRecords"),0);
			result.getPage().setTotalPage(totalPage);
			result.getPage().setTotalRecords(totalRecords);
			// 移除页码一行数据
			recordList.remove(lastLine);
			result.addRows(recordList);
		}
		return result;
	}
	
	/***
	 * 获取个人已读通知信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult getCustFinishNotifyInfo(AppParam params){
		AppResult result = new AppResult();
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_CUST_FINISH_NOTIFY + params.getAttr("customerId"));
		if(recordList == null || recordList.size() == 0){
			result = refreshCustFinishNofity(params);
		}else if(recordList.size() > 0){
			int lastLine = recordList.size() - 1;
			int totalPage = NumberUtil.getInt(recordList.get(lastLine).get("totalPage"),0);
			int totalRecords = NumberUtil.getInt(recordList.get(lastLine).get("totalRecords"),0);
			result.getPage().setTotalPage(totalPage);
			result.getPage().setTotalRecords(totalRecords);
			// 移除页码一行数据
			recordList.remove(lastLine);
			result.addRows(recordList);
		}
		return result;
	}
	
	/***
	 * 获取个人所有通知信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult getCustAllNotifyInfo(AppParam params){
		AppResult result = new AppResult();
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_CUST_ALL_NOTIFY_INFO + params.getAttr("customerId"));
		if(recordList == null || recordList.size() == 0){
			result = refreshCustAllNofityInfo(params);
		}else if(recordList.size() > 0){
			result.addRows(recordList);
		}
		return result;
	}
	
	/***
	 * 获取获取门店所有组
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getStoreOrgGroup(AppParam params){
		String roleTypeFlag = StringUtil.getString(params.getAttr("roleTypeFlag"));
		String groupKey = "";
		if(StringUtils.isEmpty(roleTypeFlag)){
			groupKey = STORE_ORG_ALL_GROUP + params.getAttr("orgId") +"_";
		}else{
			groupKey = STORE_ORG_ALL_GROUP + params.getAttr("orgId") +"_" + roleTypeFlag;
		}
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(groupKey);
		if(recordList == null || recordList.size() == 0){
			params.addAttr("groupKey", groupKey);
			recordList = refreshStoreOrgGroup(params);
		}
		return recordList;
	}
	
	/***
	 * 获取获取门店所有队
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getStoreOrgTeam(AppParam params){
		List<Map<String, Object>> recordList = (List<Map<String, Object>>) RedisUtils.getRedisService()
			.get(STORE_ORG_ALL_TEAM + params.getAttr("orgId") + params.getAttr("groupName"));
		if(recordList == null || recordList.size() == 0){
			recordList = refreshStoreOrgTeam(params);
		}
		return recordList;
	}
	
	/***
	 * 获取门店跟进记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static AppResult refreshStoreFollowRecord(AppParam params) {
		Object dateFlag = params.getAttr("dateFlag");
		Object isAdmin = params.getAttr("isAdmin");
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("totalPage", result.getPage().getTotalPage());
			map.put("totalRecords", result.getPage().getTotalRecords());
			recordList.add(map);
			RedisUtils.getRedisService().set(STORE_FOLLOW_RECORD + params.getAttr("applyId") +"_" + dateFlag + isAdmin,
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
			recordList.remove(map);
		}
		return result;
	}
	
	/***
	 * 获取投诉记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshComplaintRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("suggestRecordService", "queryByPage");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_COMPLAINT_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 7); // 缓存7天
		}
		return recordList;
	}
	
	
	/***
	 * 获取通话记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshCallRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_CALL_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	
	/***
	 * 获取通话录音记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshCallAudioRecord(AppParam params) {
		Object isAdmin = params.getAttr("isAdmin");
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_CALL_AUDIO_RECORD + params.getAttr("applyId") + isAdmin ,
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	
	/***
	 * 获取访客记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshApplyVisitRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("treatVisitDetailService", "queryVisitRecord");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_APPLY_VISIT_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	
	/***
	 * 获取预约记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshApplyBookRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("treatBookDetailService", "queryBookRecord");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_APPLY_BOOK_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	/***
	 * 获取签单记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshApplySignRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("treatInfoHistoryService", "query");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	/***
	 * 获取签单主表记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshApplyTreatSign(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("treatInfoService", "query");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_APPLY_TREAT_SIGN_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	/***
	 * 获取回款记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshApplyRetRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("treatSuccessService", "query");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_APPLY_RET_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	
	/***
	 * 获取进取记录缓存
	 * @param applyId
	 * @param customerId
	 * @return
	 */
	public static List<Map<String, Object>> refreshApplyContractRecord(AppParam params) {
		List<Map<String, Object>> recordList = new ArrayList<>();
		AppParam queryParam = new AppParam("treatContractService", "query");
		queryParam.addAttrs(params.getAttr());
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_APPLY_CONTRACT_RECORD + params.getAttr("applyId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return recordList;
	}
	
	/***
	 * 刷新专属单信息缓存
	 * @param applyId
	 * @return
	 */
	public static Map<String, Object> refreshApplyExecOrder(String applyId) {
		Map<String,Object> execOrderMap = new HashMap<String,Object>();
		AppParam queryParam = new AppParam("exclusiveOrderService", "query");
		queryParam.addAttr("applyId", applyId);
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			execOrderMap = result.getRow(0);
			RedisUtils.getRedisService().set(STORE_APPLY_EXEC_ORDER + applyId,
					(Serializable) execOrderMap , 60 * 60 * 24 * 3); // 缓存3天
		}
		return execOrderMap;
	}
	
	/***
	 * 刷新单子主表信息缓存
	 * @return
	 */
	public static Map<String, Object> refreshStoreApplyInfo(String applyId){
		Map<String,Object> applyMap = new HashMap<String,Object>();
		AppParam queryParam = new AppParam("borrowStoreApplyService", "query");
		queryParam.addAttr("applyId", applyId);
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			applyMap = result.getRow(0);
			RedisUtils.getRedisService().set(STORE_APPLY_INFO + applyId,
					(Serializable) applyMap , 60 * 60 * 24 * 3); // 缓存3天
		}
		return applyMap;
	}
	
	
	/***
	 * 刷新单子主表信息缓存
	 * @return
	 */
	public static Map<String, Object> refreshApplyInfo(String applyId){
		Map<String,Object> applyMap = new HashMap<String,Object>();
		AppParam queryParam = new AppParam("borrowApplyService", "query");
		queryParam.addAttr("applyId", applyId);
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			applyMap = result.getRow(0);
			RedisUtils.getRedisService().set(BORROW_APPLY_INFO + applyId,
					(Serializable) applyMap , 60 * 60 * 24 * 3); // 缓存3天
		}
		return applyMap;
	}
	
	/***
	 * 刷新单子主要信息缓存
	 * @return
	 */
	public static Map<String, Object> refreshApplyMainInfo(String applyId){
		Map<String,Object> applyMap = new HashMap<String,Object>();
		AppParam queryParam = new AppParam("borrowStoreApplyService", "queryMainBaseInfo");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("roleType", 1);
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			applyMap = result.getRow(0);
			RedisUtils.getRedisService().set(STORE_APPLY_MAININFO + applyId,
					(Serializable) applyMap , 60 * 60 * 24 * 3); // 缓存3天
		}
		return applyMap;
	}
	
	/***
	 * 刷新单子基本信息缓存
	 * @return
	 */
	public static Map<String, Object> refreshApplyBaseInfo(String applyId){
		Map<String,Object> applyMap = new HashMap<String,Object>();
		AppParam queryParam = new AppParam("borrowBaseService", "queryBaseInfo");
		queryParam.addAttr("applyId", applyId);
		AppResult result = ServiceKey.doCallNoTx(queryParam, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			applyMap = result.getRow(0);
			RedisUtils.getRedisService().set(STORE_APPLY_BASEINFO + applyId,
					(Serializable) applyMap , 60 * 60 * 24 * 3); // 缓存3天
		}
		return applyMap;
	}

	/***
	 * 刷新个人所有通知信息缓存
	 * @return
	 */
	public static AppResult refreshCustAllNofityInfo(AppParam params){
		List<Map<String, Object>> recordList = new ArrayList<>();
		params.setService("infoNotifyService");
		params.setMethod("queryNotifyAllList");
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_CUST_ALL_NOTIFY_INFO + params.getAttr("customerId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
		}
		return result;
	}
	
	/***
	 * 刷新个人已读通知信息缓存
	 * @return
	 */
	public static AppResult refreshCustFinishNofity(AppParam params){
		List<Map<String, Object>> recordList = new ArrayList<>();
		params.setService("infoNotifyFishService");
		params.setMethod("queryOldNotifyList");
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("totalPage", result.getPage().getTotalPage());
			map.put("totalRecords", result.getPage().getTotalRecords());
			recordList.add(map);
			RedisUtils.getRedisService().set(STORE_CUST_FINISH_NOTIFY + params.getAttr("customerId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
			recordList.remove(map);
		}
		return result;
	}
	
	/***
	 * 刷新个人未读通知信息缓存
	 * @return
	 */
	public static AppResult refreshCustNofityInfo(AppParam params){
		List<Map<String, Object>> recordList = new ArrayList<>();
		params.setService("infoNotifyService");
		params.setMethod("queryNotifyList");
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("totalPage", result.getPage().getTotalPage());
			map.put("totalRecords", result.getPage().getTotalRecords());
			recordList.add(map);
			RedisUtils.getRedisService().set(STORE_CUST_NOTIFY_INFO + params.getAttr("customerId"),
					(Serializable) recordList , 60 * 60 * 24 * 3); // 缓存3天
			recordList.remove(map);
		}
		return result;
	}
	
	/***
	 * 刷新门店所有组缓存
	 * @return
	 */
	public static List<Map<String, Object>> refreshStoreOrgGroup(AppParam params){
		String groupKey = StringUtil.getString(params.getAttr("groupKey"));
		List<Map<String, Object>> recordList = new ArrayList<>();
		params.setService("busiCustService");
		params.setMethod("queryOrgGroupList");
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(groupKey,
					(Serializable) recordList , 60 * 60 * 24 * 7); // 缓存7天
		}
		return recordList;
	}
	
	/***
	 * 刷新门店所有队缓存
	 * @return
	 */
	public static List<Map<String, Object>> refreshStoreOrgTeam(AppParam params){
		List<Map<String, Object>> recordList = new ArrayList<>();
		params.setService("busiCustService");
		params.setMethod("queryOrgTeamList");
		AppResult result = ServiceKey.doCallNoTx(params, ServiceKey.Key_busi_in);
		if(result.isSuccess() && result.getRows().size() >0){
			recordList = result.getRows();
			RedisUtils.getRedisService().set(STORE_ORG_ALL_TEAM + params.getAttr("orgId") + params.getAttr("groupName"),
					(Serializable) recordList , 60 * 60 * 24 * 7); // 缓存7天
		}
		return recordList;
	}
	/**
	 * 刷新门店组和队缓存
	 * @param params
	 */
	public static void refreshStoreOrg(AppParam params){
		String groupName = StringUtil.getString(params.getAttr("groupName"));
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		if(!StringUtils.isEmpty(orgId) && !StringUtils.isEmpty(groupName)){
			AppParam groupParams =  new AppParam();
			groupParams.addAttr("groupKey", StoreApplyUtils.STORE_ORG_ALL_GROUP + orgId + "_");
			groupParams.addAttrs(params.getAttr());
			refreshStoreOrgGroup(groupParams);
			
			AppParam adminParams =  new AppParam();
			adminParams.addAttr("groupKey", StoreApplyUtils.STORE_ORG_ALL_GROUP + orgId + "_" + 1);
			adminParams.addAttrs(params.getAttr());
			refreshStoreOrgGroup(adminParams);
			
			AppParam queryParam = new AppParam();
			queryParam.addAttr("orgId", orgId);
			queryParam.addAttr("groupName",groupName);
			refreshStoreOrgTeam(queryParam);
		}
	}
	
	/**
	 * 判断门店是否有权限上传CFS
	 * @param customerId
	 * @return
	 */
	public static boolean isHaveAuthUpCFS(String orgId){
		List<Map<String, Object>> listMap = OrgUtils.getOrgList();
		for(Map<String, Object> map : listMap){
			String orgTemp = StringUtil.getString(map.get("orgId"));
			if(orgId.equals(orgTemp)){
				String orgFlag = StringUtil.getString(map.get("orgFlag"));
				if("1".equals(orgFlag)){
					return false;
				}
			}
		}
		return true;
	}
}
