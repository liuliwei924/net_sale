package org.xxjr.cust.fund;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustAmountUtil;
import org.xxjr.cust.util.FundConstant;
import org.xxjr.sys.util.DBConst;


@Lazy
@Service
public class CustAmountService extends BaseService {
	private static final String NAMESPACE = "CUSTAMOUNT";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**查询已赠送金额
	 * querySeniorAmount
	 * @param params
	 * @return
	 */
	public AppResult querySeniorAmount(AppParam params) {
		return super.query(params, NAMESPACE,"querySeniorAmount");
	}
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * queryView
	 * @param params
	 * @return
	 */
	public AppResult queryView(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryView", "queryViewCount");
	}
	
	/**
	 * queryViewCount
	 * @param params
	 * @return
	 */
	public AppResult queryViewCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryViewCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * updateStatus
	 * @param params
	 * @return
	 */
	public AppResult updateStatus(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateStatus",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	
	/**
	 * 修改用户折扣
	 * @param params
	 * @return
	 */
	public AppResult updateUserDiscount(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateUserDiscount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		AppResult result = super.update(params, NAMESPACE);
		if (params.getAttr("customerId") != null) {
			CustAmountUtil.removeByCustId(params.getAttr("customerId").toString());
		}
		return result;
	}
	
	
	/***
	 * 提现申请
	 * @param params
	 * @return
	 */
	public AppResult withdrawApply(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "withdrawApply", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 提现取消
	 * @param params
	 * @return
	 */
	public AppResult withdrawCancel(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "withdrawCancel", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 提现成功
	 * @param params
	 * @return
	 */
	public AppResult withdrawCheck(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "withdrawCheck", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 提成处理
	 * @param params
	 * @return
	 */
	public AppResult rewardOrder(AppParam params) {
		params.addAttr(FundConstant.key_fundType, FundConstant.FundType_rewardOrder);
		int size = super.getDao().update(NAMESPACE, "rewardOrder", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 修改用户积分
	 * @param params
	 * @return
	 */
	public AppResult updateNewScore(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "updateNewScore", params.getAttr(), params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		if(params.getAttr("customerId")!=null){
			CustAmountUtil.refershAmount(params.getAttr("customerId").toString());
		}
		return result;
	}
	/**
	 * 修改积分
	 * updateScore
	 * @param params
	 * @return
	 */
	public AppResult updateScore(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "updateScore", params.getAttr(), params.getDataBase());
		if(size==0){
			return  this.insert(params);
		}
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		if(params.getAttr("customerId")!=null){
			CustAmountUtil.refershAmount(params.getAttr("customerId").toString());
		}
		return result;
	}
	
	
	/**
	 * 退款处理
	 * @param context
	 * @return
	 */
	public AppResult returnAmount(AppParam context){
		AppParam amountParam = new AppParam();
		amountParam.setService("custAmountService");
		amountParam.setMethod("updateCustAmount");
		amountParam.addAttr("orderId", context.getAttr("orderId"));
		amountParam.addAttr("recordDesc", "充值退款");
		amountParam.addAttr("customerId", context.getAttr("customerId"));
		amountParam.addAttr("amount", -Double.valueOf(context.getAttr("amount").toString()));
		amountParam.addAttr("fundType", FundConstant.FundType_VIP_BACK);
		amountParam.setDataBase(DBConst.Key_cust_DB);
		this.updateCustAmount(amountParam);
			
		if(!StringUtils.isEmpty(context.getAttr("rechargeReward"))&&
				Double.valueOf(context.getAttr("rechargeReward").toString())>1){
			AppParam rewardAmountParam = new AppParam();
			rewardAmountParam.setService("custAmountService");
			rewardAmountParam.setMethod("updateCustAmount");
			rewardAmountParam.addAttr("orderId", context.getAttr("orderId"));
			rewardAmountParam.addAttr("recordDesc", "赠送退还");
			rewardAmountParam.addAttr("customerId", context.getAttr("customerId"));
			rewardAmountParam.addAttr("amount", -Double.valueOf(context.getAttr("rechargeReward").toString()));
			rewardAmountParam.addAttr("fundType", FundConstant.FundType_RECHARGE_Reward);
			rewardAmountParam.setDataBase(DBConst.Key_cust_DB);
			this.updateCustAmount(rewardAmountParam);
		}
		if(!StringUtils.isEmpty(context.getAttr("orderId"))){
			
			AppParam  fund = new AppParam();
			fund.addAttr("rechargeId", context.getAttr("orderId"));
			fund.addAttr("status", "4");
			fund.addAttr("errCodeDesc", "已经退款处理");
			fund.setService("rechargeService");
			fund.setMethod("update");
			SoaManager.getInstance().invoke(fund);
		}
		return new AppResult();
	}
	
	/**
	 * 修改用户金额以及记录
	 * @param context
	 * @return
	 */
	public AppResult updateCustAmount(AppParam context){
		Object customerId = context.getAttr("customerId");
		if(StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		int size = super.getDao().update(NAMESPACE, "updateCustAmount", context.getAttr(), context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		
		AppParam queryCust = new AppParam();
		queryCust.addAttr("customerId", customerId);
		queryCust.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(queryCust);
		Double usableAmount = Double.parseDouble(queryResult.getRow(0).get("usableAmount").toString());
		
		AppParam  fund = new AppParam();
		fund.setAttr(context.getAttr());
		fund.addAttr("usableAmount", usableAmount);
		fund.setService("fundRecordService");
		fund.setMethod("insert");
		result = SoaManager.getInstance().invoke(fund);
		
		if(result.isSuccess()){
			CustAmountUtil.refershAmount(customerId.toString());
		}
		
		result.putAttr("usableAmount", usableAmount);
		return result;
	}
	
	/**
	 * 修改用户可抢优质单金额以及资金记录
	 * @param context
	 * @return
	 */
	public AppResult updateSeniorAmount(AppParam context){
		Object customerId = context.getAttr("customerId");
		if(StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		int size = super.getDao().update(NAMESPACE, "updateSeniorAmount", context.getAttr(), context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		
		AppParam queryCust = new AppParam();
		queryCust.addAttr("customerId", customerId);
		queryCust.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(queryCust);
		Double seniorAmount = Double.parseDouble(queryResult.getRow(0).get("seniorAmount").toString());
		
		AppParam  fund = new AppParam();
		fund.setAttr(context.getAttr());
		fund.addAttr("seniorAmount", seniorAmount);
		fund.setService("fundRecordSeniorService");
		fund.setMethod("insert");
		result = SoaManager.getInstance().invoke(fund);
		
		if(result.isSuccess()){
			RedisUtils.getRedisService().del(CustAmountUtil.CacheKey_PASS + customerId);
		}
		
		result.putAttr("seniorAmount", seniorAmount);
		return result;
	}
	
	/**
	 * 清空用户金额以及记录
	 * @param context
	 * @return
	 */
	public AppResult clearUsableAmount(AppParam context){
		Object customerId = context.getAttr("customerId");
		if(StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppParam queryCust = new AppParam();
		queryCust.addAttr("customerId", customerId);
		queryCust.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(queryCust);
		Double usableAmount = Double.parseDouble(queryResult.getRow(0).get("usableAmount").toString());
		
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam();
		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("usableAmount", 0);
		updateParam.setDataBase(DBConst.Key_cust_DB);
		this.update(updateParam);
		
		AppParam  fund = new AppParam();
		fund.setAttr(context.getAttr());
		fund.addAttr("usableAmount", 0);
		fund.addAttr("amount", -usableAmount);
		fund.setService("fundRecordService");
		fund.setMethod("insert");
		SoaManager.getInstance().invoke(fund);
		
		result.putAttr("usableAmount", 0);
		return result;
	}
	
	/***
	 * 金融管家抽奖
	 * @param params
	 * @return
	 */
	public AppResult stewardDraw(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "stewardDraw", params.getAttr(), params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 活动奖励添加 推荐好友赠送
	 * @param context
	 * @return
	 */
	public AppResult insertActivity(AppParam context){
		AppParam amountParam = new AppParam();
		amountParam.addAttr("customerId", context.getAttr("customerId"));
		amountParam.addAttr("amount", context.getAttr("rewardValue"));
		amountParam.addAttr("fundType", context.getAttr("fundType"));
		amountParam.addAttr("recordDesc", context.getAttr("recordDesc"));
		amountParam.setDataBase(DBConst.Key_cust_DB);
		this.updateCustAmount(amountParam);
		
		return new AppResult();
	}
	
	
	/**
	 * 修改用户佣金
	 * @param params
	 * @return
	 */
	public AppResult updateRewardAmount(AppParam params) {
		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		Object method = params.getAttr("methodName");
		if(StringUtils.isEmpty(customerId) || StringUtils.isEmpty(method)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		// 更新用户资金
		int size = super.getDao().update(NAMESPACE, method.toString(), params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		
		// 刷新资金缓存
		if(result.isSuccess()){
			CustAmountUtil.refershAmount(customerId.toString());
		}
		return result;
	}
}
