package org.xxjr.sys.util.active;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.AppException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;


public class ActiveUtil {
	

	
	/***
	 * 根据activeCode 调用一个活动处理
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static AppResult invokeOneActive(AppParam param) throws Exception {
		LogerUtil.log("start invokeOneActive:" + param.getAttr());
		Long activeId  = null;
		//活动参数是否存在
		if(StringUtils.isEmpty(param.getAttr("activeCode")) && StringUtils.isEmpty(
				param.getAttr("activeId"))){
			throw new AppException(" not found ActiveCode or acitveId:" + param.getAttr());
		}else{
			if(StringUtils.isEmpty(param.getAttr("activeId"))){
				AppParam getActivity = new AppParam();
				getActivity.addAttr("activeCode", param.getAttr("activeCode"));
				getActivity.setService("activityService");
				getActivity.setMethod("query");
				AppResult activeityList = RemoteInvoke.getInstance().call(getActivity);
				if (activeityList.getRows().size() == 0) {
					throw new AppException(" not found ActiveCode" + param.getAttr("activeCode"));
				}
				activeId = Long.valueOf(activeityList.getRow(0).get("activeId").toString());
			}else{
				activeId = Long.valueOf(param.getAttr("activeId").toString());
			}
		}
		
		AppResult activeInfo = ActiveCache.getActiveInfoById(activeId);
		
		//验证相应参数是否正确，并返回后续需要的数据，若不正确，直接报错
		Map<String, Object> rewardParams = validParams(param, activeInfo);
		// 判断规则条件是否成立，返回相应的奖励信息
		AppResult rewardInfo = getRewardInfo(param, activeInfo);
		if (!rewardInfo.isSuccess()) {
			return rewardInfo;
		}
		if(StringUtils.isEmpty(rewardInfo.getAttr("rewardService"))){
			LogerUtil.log("invokeOneActive end not found rewardService:" + rewardInfo.getAttr("activeCode"));
			return rewardInfo;
		}
		AppResult executeReward = executeReward(param, rewardParams,
				rewardInfo);
		LogerUtil.log("end invokeOneActive:" + executeReward.isSuccess());
		return executeReward;
	}
	
	/***
	 * 根据activeCode 调用一个活动处理查询等级
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static AppResult getGrade(AppParam param)  {
		AppResult rewardInfo = new AppResult();
		try {
			LogerUtil.log("start getGrade:" + param.getAttr());
			Long activeId  = null;
			AppParam getActivity = new AppParam();
			getActivity.addAttr("activeCode", "applyGrade");
			getActivity.setService("activityService");
			getActivity.setMethod("query");
			AppResult activeityList = RemoteInvoke.getInstance().call(getActivity);
			if (activeityList.getRows().size() == 0) {
				throw new AppException(" not found ActiveCode" + param.getAttr("activeCode"));
			}
			activeId = Long.valueOf(activeityList.getRow(0).get("activeId").toString());
			
			
			AppResult activeInfo = ActiveCache.getActiveInfoById(activeId);

			// 判断规则条件是否成立，返回相应的奖励信息
			rewardInfo = getRewardInfo(param, activeInfo);
			if (!rewardInfo.isSuccess()) {
				return rewardInfo;
			}
			return rewardInfo;
		} catch (Exception e) {
			LogerUtil.error(ActiveUtil.class, e, "getGrade error params: " + param.getAttr());
		}
		return rewardInfo;
		
	}
	
	/***
	 * 验证相应的处理是否满足活动规则，满足规则后，直接返回奖励信息
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static AppResult invokeActive(AppParam param) throws Exception {
		LogerUtil.log("start invokeActive:" + param.getAttr());
		AppResult  existActive = ActiveCache.existsActiveInfo(param);
		if (existActive == null || existActive.getRows().size() <= 0) {
			AppResult result = new AppResult();
			result.setSuccess(Boolean.FALSE);
			result.setMessage("没有找相应的活动配置");
			return result;
		}
		AppResult result = new AppResult();
		for(Map<String,Object> activeparam :existActive.getRows()){
			AppResult activeInfo = 
					ActiveCache.getActiveInfoById(Long.valueOf(activeparam.get("activeId").toString()));
			try{
				activeparam.put("activeCode",
						((Map<String,Object>)activeInfo.getAttr(ActiveCache.activeInfo)).get("activeCode"));
				//验证相应参数是否正确，并返回后续需要的数据，若不正确，直接报错
				Map<String, Object> rewardParams = validParams(param, activeInfo);
				
				// 判断规则条件是否成立，返回相应的奖励信息
				AppResult rewardInfo = getRewardInfo(param, activeInfo);
				if (!rewardInfo.isSuccess()) {
					activeparam.put("success", rewardInfo.isSuccess());
					activeparam.put("message", rewardInfo.getMessage());
					continue;
				}
				//对于服务和相应的数据不存在时的处理
				if (StringUtils.isEmpty(rewardInfo.getAttr("rewardService"))) {
					activeparam.put("success", true);
					activeparam.put("rewardValue", rewardInfo.getAttr("rewardValue"));
					continue;
				}
				AppResult executeReward = executeReward(param, rewardParams,
						rewardInfo);
				activeparam.put("success", executeReward.isSuccess());
				activeparam.put("message", executeReward.getMessage());
			}catch(Exception e){
				LogerUtil.error(ActiveUtil.class, e, "Execute active error!");
				activeparam.put("success", false);
				activeparam.put("message", e.getMessage());
			}
			
		}
		result.addRows(existActive.getRows());
		LogerUtil.log("end invokeActive:" + existActive.getRows());
		return result;
	}
	
	/**执行奖励处理
	 * 
	 * @param param
	 * @param rewardParams
	 * @param rewardInfo
	 * @return
	 */
	private static AppResult executeReward(AppParam param,
			Map<String, Object> rewardParams, AppResult rewardInfo) {
		//执行奖励服务
		AppParam executeReward = new AppParam();
		executeReward.setService(rewardInfo.getAttr("rewardService").toString());
		executeReward.setMethod(rewardInfo.getAttr("rewardMethod").toString());
		rewardParams.put("rewardValue", rewardInfo.getAttr("rewardValue"));
		executeReward.addAttrs(rewardParams);
		executeReward.setDataBase(param.getDataBase());
		executeReward.setRmiServiceName(
				AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + 
				rewardInfo.getAttr("rewardRmi").toString()));
		return RemoteInvoke.getInstance().call(executeReward);
	}

	/***
	 * 根据param 获取相应的奖励信息
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static AppResult getRewardInfo(AppParam param,AppResult activeInfo){
		List<Map<String,Object>>  ruleList = (List<Map<String, Object>>) activeInfo.getAttr(ActiveCache.ruleList);
		Map<String,Object>  ruleResult = null;
		//判断规则是否成立，成立返回成立的规则信息
		for(Map<String,Object> activeRule: ruleList){
			if(paramJudyCheck((List<Map<String, Object>>)activeRule.get(ActiveCache.ruleCondition),param)){
				ruleResult  = activeRule;
				break;
			}
		}
		Object activeId = ((Map<String,Object>)activeInfo.getAttr(ActiveCache.activeInfo)).get("activeId") ;
		//规则不正确，直接返回false
		if (ruleResult == null) {
			LogerUtil.log("activeId:" + activeId + " params:" + param.getAttr() + " is not have reward rule!");
			AppResult result = new AppResult();
			result.setSuccess(Boolean.FALSE);
			result.setMessage("Rule is not exists.");
			return result;
		}
		//根据条件打印获取的规则信息
		LogerUtil.log("activeId:" + activeId + " params:" + param.getAttr() + " is have reward rule:" + ruleResult.get("ruelId")+
				ruleResult.get("rewardDesc"));
		Map<String,Object> reward  = ActiveCache.getActiveRewardById(Long.valueOf(ruleResult.get("rewardId").toString()));
		AppResult result = new AppResult();
		result.setAttr(reward);
		result.putAttr("rewardValue", ruleResult.get("rewardValue"));
		result.setSuccess(Boolean.TRUE);
		return result;
				
				
	}

	/***
	 * 验证参数是否正确
	 * @param param
	 * @param activeId
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> validParams(AppParam param , AppResult activeInfo) throws ParseException {
		//activeId
		Map<String,Object> activeRow = (Map<String, Object>) activeInfo.getAttr(ActiveCache.activeInfo);
		
		//判断活动时间
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
		
		Date startDate  = simpleDateFormat.parse(String.valueOf(activeRow.get("startDate")));
		if(startDate.getTime() > System.currentTimeMillis()) {
			throw new AppException("ActiveCode startDate is invalid :" + activeRow);
		}
		if(activeRow.get("endDate") != null && !"".equals(activeRow.get("endDate"))) {
			Date endDate = simpleDateFormat.parse(String.valueOf(activeRow.get("endDate")));
			if(endDate.getTime() < System.currentTimeMillis()) {
				throw new AppException("ActiveCode startDate is invalid :" + activeRow);
			}
		}
		//end 活动状态
		Map<String,Object> rewardParams = new HashMap<String,Object>();
		rewardParams.putAll(param.getAttr());
		//activeId
		List<Map<String,Object>> activeParams = (List<Map<String,Object>>) activeInfo.getAttr(ActiveCache.activeParam);
		//判段 参数 
		for(Map<String,Object> activeParam:activeParams) {
			int paramType = Integer.valueOf(activeParam.get("paramType").toString());
			switch(paramType){
				case ActiveConstants.PARAM_TYPE_OUT:
					rewardParams.put(activeParam.get(ActiveCache.paramKey).toString(), activeParam.get(ActiveCache.paramValue));
					break;
			}
		}
		return rewardParams;
	}
	
	
	
	
	/***
	 * 判断activeparam key 和 activeCondition Key
	 * @author	shaoyongyang
	 * @param	activeParam ruleCondition
	 * @return	AppResult
	 * **/
	private static Boolean paramJudyCheck(List<Map<String,Object>> conditionList,AppParam value) {
		boolean judyPass = true;;
		int parentJudy = -1;
		boolean haveOk = false;
		for (Map<String, Object> codition : conditionList) {
			int judy = Integer.valueOf(codition.get("paramJudy").toString());
			int paramAppend = Integer.valueOf(codition.get("paramAppend").toString());
			Object conditonValue = codition.get("paramValue").toString();
      			Object paramValue =  value.getAttr(codition.get("paramKey").toString());
			if (paramValue == null && paramAppend != ActiveConstants.PARAM_APPEND_OR) {
				return false;
			}
			//
			boolean isOk = judyCodittion(judy, conditonValue, paramValue);
			//条件成立
			if (isOk) {
				if (parentJudy ==-1 && paramAppend == ActiveConstants.PARAM_APPEND_OR) {
					return true;
				}else if(parentJudy == ActiveConstants.PARAM_APPEND_OR){
					return true;
				}
				parentJudy = paramAppend;
				haveOk = true;
			}else{
				judyPass = false;
				if(parentJudy ==-1  && parentJudy == ActiveConstants.PARAM_APPEND_AND){
					return false;
				}else if(parentJudy == ActiveConstants.PARAM_APPEND_AND){
					return false;
				}
				parentJudy = paramAppend;
			}
		}
		if(haveOk){
			return judyPass;
		}else{
			return false;
		}
		
	}

	private static boolean judyCodittion(int judy, Object conditonValue, Object paramValue) {
		boolean isOk = false;
		if(StringUtils.isEmpty(paramValue)){
			return false;
		}
		switch(judy){
			//等于
			case ActiveConstants.PARAM_JUDY_1:
				isOk = conditonValue.equals(paramValue.toString());
			break;
			//大于
			case ActiveConstants.PARAM_JUDY_2:
			//大于等于
			case ActiveConstants.PARAM_JUDY_3:
			//小于
			case ActiveConstants.PARAM_JUDY_4:
			//小于等于
			case ActiveConstants.PARAM_JUDY_5:
				isOk = paramJudyNumber(judy,paramValue,conditonValue);
			break;
			//包含
			case ActiveConstants.PARAM_JUDY_6:
				for(String ca: conditonValue.toString().split(",")){
					if (ca.equalsIgnoreCase(StringUtil.getString(paramValue))) {
						isOk = true;
						break;
					}
				}
				break;
			//不包含
			case ActiveConstants.PARAM_JUDY_7:
				for(String ca: conditonValue.toString().split(",")){
					if (ca.equalsIgnoreCase(StringUtil.getString(paramValue))) {
						isOk = true;
						break;
					}
				}
				isOk=!isOk;
				break;
		}
		return isOk;
	}
	
	//
	private static Boolean paramJudyNumber(int judy,Object paramValue,Object conditonValue){
		double intValue = Double.valueOf(paramValue.toString());
		int intCodition = Integer.valueOf(conditonValue.toString());
		switch(judy){
			case ActiveConstants.PARAM_JUDY_2:
				//大于
				if(intValue > intCodition){
					return true;
				}
				break;
			case ActiveConstants.PARAM_JUDY_3:
				//大于等于
				if(intValue >= intCodition){
					return true;
				}
				break;
			case ActiveConstants.PARAM_JUDY_4:
				//小于
				if(intValue < intCodition){
					return true;
				}
				break;
			case ActiveConstants.PARAM_JUDY_5:
				//小于等于
				if(intValue <= intCodition){
					return true;
				}
				break;
		}
		return false;
	}
}


