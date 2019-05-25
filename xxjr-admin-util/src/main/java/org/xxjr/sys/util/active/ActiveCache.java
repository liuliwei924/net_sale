package org.xxjr.sys.util.active;

import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.page.PageUtil;
import org.llw.model.cache.RedisUtils;
import org.llw.model.cache.SerializeUtil;

public class ActiveCache {
	public static final String paramKey ="paramKey";
	public static final String paramValue ="paramValue";
	public static final String activeInfo = "activeInfo";
	public static final String activeParam = "activeParam";
	public static final String ruleList = "ruleList";
	public static final String ruleCondition = "ruleCondition";
	
	
	/**活动信息**/
	public final static String Key_activeInfo="activeInfo";
	/**活动参数**/
	public final static String Key_activeParam="activeParam";
	/**活动奖励信息**/
	public final static String Key_activeReward="activeReward";

	
	
	/**有效时长*/
	private static final int VALIDE_TIME = 24*60*60; 
	
	
	/***
	 * 获取条件 获取相应的活动
	 * @param param
	 * @return
	 */
	public static AppResult existsActiveInfo(AppParam param){
		String startKey = Key_activeParam;
		AppResult paramList = (AppResult) RedisUtils.getRedisService().get(startKey);
		if(paramList == null){
			paramList = queryActiveParams(ActiveConstants.PARAM_TYPE_CON);
			if (paramList != null) {
				RedisUtils.getRedisService().set(startKey, paramList, VALIDE_TIME);
			}
		}
		if (paramList == null) {
			return null;
		}
		AppResult  result = new AppResult();
		Map<String,Object>  input = param.getAttr();
		for (Map<String, Object> row : paramList.getRows()) {
			String paramKey = row.get(ActiveCache.paramKey).toString();
			String paramValue = row.get(ActiveCache.paramValue).toString();
			if (input.containsKey(paramKey)
					&& paramValue.equals(input.get(paramKey).toString())) {
				result.addRow(row);
				LogerUtil.log("existsActiveInfo:" + paramKey + " = " + input.get(paramKey) + " active:" + row.get("activeId"));
			}else{
				LogerUtil.log("not existsActiveInfo:" + paramKey + " = " + input.get(paramKey) + " active:" + row.get("activeId"));
			}
		}
		return result;
	}
	/***
	 * 刷新activeParam
	 * @param activeId
	 */
	private static void refreshActiveParam(){
		AppResult paramList = queryActiveParams(ActiveConstants.PARAM_TYPE_CON);
		if (paramList != null) {
			RedisUtils.getRedisService().set(Key_activeInfo, paramList, VALIDE_TIME);
		}
	}
	
	/***
	 * 获 活动ID获取相应的信息
	 * activeInfo 活动信息
	 * activeParam 活动参数
	 * ruleList 活动规则 ruleCondition 规则条件
	 * @param activeId 活动ID
	 * @return
	 */
	public static AppResult getActiveInfoById(Long activeId){
		String startKey = Key_activeInfo;
		AppResult result = (AppResult) RedisUtils.getRedisService().get(startKey + activeId);
		if(result == null){
			result  = queryActiveInfoById(activeId);
			if (result != null) {
				RedisUtils.getRedisService().set(startKey + activeId, result, VALIDE_TIME);
			}
		}
		return result;
	}
	
	/***
	 * 刷新active的信息
	 * @param activeId
	 */
	public static void refreshActiveInfo(Long activeId){
		AppResult result  = queryActiveInfoById(activeId);
		if (result != null) {
			RedisUtils.getRedisService().set(Key_activeInfo
					+ activeId, result, VALIDE_TIME);
			AppResult paramList = queryActiveParams(ActiveConstants.PARAM_TYPE_CON);
			RedisUtils.getRedisService().set(Key_activeParam, paramList, VALIDE_TIME);
		}
	}
	
	/***
	 * 删除活动的key
	 * @param activeId
	 */
	public static void deleteActiveCache(Long activeId){
		String startKey = Key_activeInfo;
		AppResult result = (AppResult) RedisUtils.getRedisService().get(startKey + activeId);
		if (result != null) {
			RedisUtils.getRedisService().del(startKey + activeId);
			AppResult paramList = queryActiveParams(ActiveConstants.PARAM_TYPE_CON);
			RedisUtils.getRedisService().set(Key_activeParam, paramList, VALIDE_TIME);
		}
	}
	/***
	 * 刷新ActiveReward
	 * @param activeId
	 */
	public static void refreshActiveReward(Long rewardId){
		Map<String,Object> result = getRewardById(rewardId);
		if (result != null) {
			RedisUtils.getRedisService().set(Key_activeReward+ rewardId,
					SerializeUtil.serialize(result), VALIDE_TIME);			
		}
		refreshActiveParam();
	}
	/***
	 * 获 活动ID获取相应的信息
	 * activeInfo 活动信息
	 * activeParam 动规则 ruleCondition 规则条件
	 * @param activeId 活动ID
	 * @return
	 */
	public static Map<String,Object> getActiveRewardById(Long rewardId){
		return getRewardById(rewardId);
	}
	/***
	 * 从数据库查寻活动信息
	 * @param activeId
	 * @return
	 */
	private static AppResult queryActiveInfoById(Long activeId){
		AppResult result = new AppResult();
		
		AppParam queryPrams = new AppParam();
		queryPrams.setMethod("query");
		queryPrams.setService("activityService");
		queryPrams.addAttr("activeId", activeId);
		AppResult active = RemoteInvoke.getInstance().call(queryPrams);
		if(active.getRows().size() <= 0) {
			return null;
		}
		result.putAttr(ActiveCache.activeInfo, active.getRow(0));
		//取活动参数
		queryPrams = new AppParam();
		queryPrams.setService("activityParamService");
		queryPrams.setMethod("query");
		queryPrams.addAttr("activeId", activeId);
		AppResult paramResult = RemoteInvoke.getInstance().call(queryPrams);
		result.putAttr(ActiveCache.activeParam, paramResult.getRows());
		
		//得到活动规则
		AppParam queryRules = new AppParam();
		queryRules.setService("ruleService");
		queryRules.setMethod("query");
		queryRules.addAttr("activeId", activeId);
		queryRules.setOrderBy("ruleIndex");
		queryRules.setOrderValue(PageUtil.ORDER_ASC);
		AppResult ruleList = RemoteInvoke.getInstance().call(queryRules);
		//判断规则是否成立，成立返回成立的规则信息
		for(Map<String,Object> activeRule:ruleList.getRows()){
			//invoke reward method
			AppParam queryCondition = new AppParam();
			queryCondition.setService("ruleConditionService");
			queryCondition.setMethod("query");
			queryCondition.addAttr("ruleId", activeRule.get("ruleId"));
			queryCondition.setOrderBy("conditionIndex");
			queryCondition.setOrderValue(PageUtil.ORDER_ASC);
			AppResult ruleCondition = RemoteInvoke.getInstance().call(queryCondition);
			//设置条件
			activeRule.put(ActiveCache.ruleCondition, ruleCondition.getRows());
		}
		result.putAttr(ActiveCache.ruleList, ruleList.getRows());
		return result;
	}
	
	/***
	 * 从数据库查寻活动信息
	 * @param activeId
	 * @return
	 */
	private static AppResult queryActiveParams(int paramType){
		AppParam queryPrams = new AppParam();
		queryPrams.setService("activityParamService");
		queryPrams.setMethod("query");
		queryPrams.addAttr("paramType", paramType);
		return RemoteInvoke.getInstance().call(queryPrams);
	}
	
	/***
	 * 从数据库查寻活动信息
	 * @param activeId
	 * @return
	 */
	private static Map<String,Object> getRewardById(Object rewardId){
		AppParam queryPrams = new AppParam();
		queryPrams.setService("ruleRewardService");
		queryPrams.setMethod("query");
		queryPrams.addAttr("rewardId", rewardId);
		List<Map<String,Object>>  list = RemoteInvoke.getInstance().call(queryPrams).getRows();
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
}
