package org.xxjr.cust.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.LogerUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

public class CustAmountUtil {

	/**已经认证的身分信息**/
	public static String CacheKey_PASS = "CustAmount";
	
	/***
	 * 获取用户 可用金额 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getCustAmount(String customerId){
		Map<String, Object> userMap = ((Map<String, Object>) RedisUtils.getRedisService().get(CacheKey_PASS + customerId));
		if(userMap == null){
			userMap = refershAmount(customerId);
		}
		return userMap;
	}
	
	
	/***
	 * 获取用户 可用金额 
	 * @return
	 */
	public static Map<String,Object> refershAmount(String customerId){
		AppParam param = new AppParam();
		param.setService("custAmountService");
		param.setMethod("query");
		param.addAttr("customerId", customerId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = null;
		
		if (SpringAppContext.getBean("custAmountService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size()==0){
			return new HashMap<String,Object>();
		}
		Map<String, Object> userMap = result.getRow(0);
		RedisUtils.getRedisService().set(CacheKey_PASS + customerId, (Serializable)userMap, CustomerUtil.CACHE_TIME);
		return userMap;
	}
		
	/***
	 * 清除个人积分
	 * @param customerId
	 * @return
	 */
	public static void removeByCustId(String customerId){
		RedisUtils.getRedisService().del(CacheKey_PASS + customerId);
	}
	/***
	 * 清除个人积分
	 * @param openId
	 * @return
	 */
	public static void removeByOpenId(String openId){
		RedisUtils.getRedisService().del(CacheKey_PASS + openId);
	}
	
	/***
	 * 个人可用积分
	 * @param custId
	 * @return
	 */
	public static Integer custTotalScore(String custId){
		Map<String,Object> custAmount = getCustAmount(custId);
		if(custAmount==null || custAmount.get("totalScore")==null){
			return 0;
		}
		return Integer.valueOf(custAmount.get("totalScore").toString());
	}
	
	/**
	 * 资金变动
	 * @param params
	 */
	public static AppResult updateAmount(AppParam params){
		AppResult result = new AppResult();
		AppParam amountParam = new AppParam();
		amountParam.addAttrs(params.getAttr());
		amountParam.setService("custAmountService");
		amountParam.setMethod("updateCustAmount");
		amountParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("custAmountService") == null) {
			result = RemoteInvoke.getInstance().call(amountParam);
		}else{
			result = SoaManager.getInstance().invoke(amountParam);
		}
		return result;
	}
	
	/**
	 * 可抢优质单资金变动
	 * @param params
	 */
	public static AppResult updateSeniorAmount(AppParam params){
		AppResult result = new AppResult();
		AppParam amountParam = new AppParam();
		amountParam.addAttrs(params.getAttr());
		amountParam.setService("custAmountService");
		amountParam.setMethod("updateSeniorAmount");
		amountParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("custAmountService") == null) {
			result = RemoteInvoke.getInstance().call(amountParam);
		}else{
			result = SoaManager.getInstance().invoke(amountParam);
		}
		return result;
	}
	
	/**
	 * 查询已赠送金额
	 * @param params
	 */
	public static Map<String,Object> querySeniorAmount(String customerId){
		AppResult result = new AppResult();
		AppParam amountParam = new AppParam();
		amountParam.addAttr("customerId", customerId);
		amountParam.setService("custAmountService");
		amountParam.setMethod("querySeniorAmount");
		amountParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("custAmountService") == null) {
			result = RemoteInvoke.getInstance().call(amountParam);
		}else{
			result = SoaManager.getInstance().invoke(amountParam);
		}
		
		return result.getRows().size() > 0 ? result.getRow(0) : result.getAttr();
	}
	
	/**
	 * 优质单回款确认 
	 * @param params
	 * @return
	 */
	public static AppResult seniorRepayCheck(AppParam params){
		AppResult result = new AppResult();
		String customerId = params.getAttr("customerId").toString();
		String recordId = params.getAttr("recordId").toString();
		double feeAmount = NumberUtil.getDouble(params.getAttr("feeAmount"));
		
		// 还钱
		AppParam amtParams = new AppParam();
		amtParams.addAttr("customerId", customerId);
		amtParams.addAttr("amount", 0);
		amtParams.addAttr("leftRepayAmount", -feeAmount);
		amtParams.addAttr("fundType", FundConstant.FundType_Senior_Reward);
		amtParams.addAttr("orderId", recordId);
		amtParams.addAttr("recordDesc", "抢优质单提成还款");
		result = updateSeniorAmount(amtParams);
		
		return result;
	}
	
	/**
	 * 修改用户折扣
	 */
	public static AppResult updateUserDiscount(String customerId){
		AppResult result = new AppResult();
		AppParam params = new AppParam("custAmountService","updateUserDiscount");
		params.addAttr("customerId", customerId);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("custAmountService") == null) {
			result = RemoteInvoke.getInstance().call(params);
		}else{
			result = SoaManager.getInstance().invoke(params);
		}
		return result;
	}
	
	/**
	 * 更新可抢优质单或剩餘待還金額及提成金额
	 * @param params
	 * @return
	 */
	public static AppResult updateRewardAmount(Object customerId, Object recordId, 
			Object rewardVal,Object leftRepayAmount){
		AppParam amtParams = new AppParam();
		amtParams.addAttr("customerId", customerId);
		amtParams.addAttr("amount", rewardVal);
		amtParams.addAttr("recordDesc", "抢优质单提成");
		if(!StringUtils.isEmpty(leftRepayAmount)){
			amtParams.addAttr("leftRepayAmount", leftRepayAmount);
			amtParams.addAttr("recordDesc", "抢优质单提成还款");
		}
		amtParams.addAttr("fundType", FundConstant.FundType_Senior_Reward);
		amtParams.addAttr("orderId", recordId);
		AppResult amountResult = updateSeniorAmount(amtParams);
		
		// 记录回款提成
		if(amountResult.isSuccess()){
			AppParam treatParam = new AppParam();
			treatParam.setService("treatSuccessService");
			treatParam.setMethod("update");
			treatParam.addAttr("rewardAmount", rewardVal);
			treatParam.addAttr("recordId", recordId);
			treatParam.addAttr("customerId", customerId);
			treatParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			if (SpringAppContext.getBean("treatSuccessService") == null) {
				RemoteInvoke.getInstance().call(treatParam);
			}else{
				SoaManager.getInstance().invoke(treatParam);
			}
		}
		return new AppResult();
	}
	
	/**
	 * 注册时保持积分统一
	 * @param params
	 * @return
	 */
	public static AppResult updateRegisterAmount(AppParam params){
		AppResult result = new AppResult();
		Object openid = params.getAttr("openid");
		Object customerId = params.getAttr("customerId");
		Object gzhId = params.getAttr("gzhId");
		if(!StringUtils.isEmpty(openid) && !StringUtils.isEmpty(customerId)
				&& !StringUtils.isEmpty(gzhId)){
			// 查询是否已绑定
			AppParam amountParam = new AppParam();
			amountParam.addAttr("openid", openid);
			amountParam.addAttr("customerId", customerId);
			amountParam.setService("custScoreService");
			amountParam.setMethod("queryBinded");
			amountParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
			AppResult openidAmounts = null;
			if (SpringAppContext.getBean("custScoreService") == null) {
				openidAmounts = RemoteInvoke.getInstance().call(amountParam);
			}else{
				openidAmounts = SoaManager.getInstance().invoke(amountParam);
			}
			if(Integer.valueOf(openidAmounts.getAttr(DuoduoConstant.TOTAL_SIZE).toString()) == 0){
				amountParam = new AppParam();
				//原数据的customerId
				amountParam.addAttr("newCustomerId", customerId);
				amountParam.addAttr("openid", openid);
				amountParam.addAttr("gzhId", gzhId);
				amountParam.setService("custScoreService");
				amountParam.setMethod("updateNewScore");
				amountParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
				if (SpringAppContext.getBean("custScoreService") == null) {
					RemoteInvoke.getInstance().call(amountParam);
				}else{
					SoaManager.getInstance().invoke(amountParam);
				}
				//修改总积分
				amountParam.addAttr("customerId", customerId);
				amountParam.setService("custAmountService");
				amountParam.setMethod("updateNewScore");
				amountParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
				if (SpringAppContext.getBean("custAmountService") == null) {
					RemoteInvoke.getInstance().call(amountParam);
				}else{
					SoaManager.getInstance().invoke(amountParam);
				}
				//修改其他平台总积分
				AppParam scoreAmount = new AppParam();
				scoreAmount.setService("custWxinfoService");
				scoreAmount.setMethod("updateTotalScore");
				scoreAmount.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + "wx"));
				scoreAmount.addAttr("openid", openid);
				scoreAmount.addAttr("gzhId", gzhId);
				scoreAmount.addAttr("score", CustAmountUtil.custTotalScore(customerId.toString()));
				try{
					RemoteInvoke.getInstance().call(scoreAmount);
				}catch(Exception e){
					LogerUtil.error(CustAmountUtil.class, e, "updateTotalScore custWxinfoService error");
				}
			}
		}
		return result;
	}
	
	/**
	 * 查询用户未使用的赠送金额
	 * @param customerId
	 * @param usableAmount
	 * @return
	 */
	public static double getUnusedAmount(String customerId, Object usableAmount) {
		double differ = 0;// 还未用完的赠送金额
		AppParam param = new AppParam();
		param.setService("rechargeService");
		param.setMethod("queryRechargeRecords");
		param.addAttr("startDate", SysParamsUtil.getStringParamByKey("rechargeActivityDate", "2018-02-28"));
		param.addAttr("customerId", customerId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(param);
		List<Map<String,Object>> records = result.getRows();
		double custUsableAmount = NumberUtil.getDouble(usableAmount, 0);
		if (records.size() == 0 && custUsableAmount > 0) {
			differ = custUsableAmount;
		}else{
			Map<String,Object> rechargeSummary = records.get(0);
			double totalAmount = NumberUtil.getDouble(rechargeSummary.get("totalAmount"), 0);
			if(totalAmount <= 0){
				differ = custUsableAmount;
			}else{
				if(custUsableAmount > totalAmount){
					differ = custUsableAmount - totalAmount;
				}
			}
		}
		return differ;
	}
}
