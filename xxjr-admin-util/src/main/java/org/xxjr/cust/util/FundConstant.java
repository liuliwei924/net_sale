package org.xxjr.cust.util;

/***
 * 资金相关的常量
 * @author Administrator
 *
 */
public class FundConstant {
	
	/**资金类型 其他，如冻结*/
	public static final int AMOUNT_TYPE_0_OTHER = 0;
	/**资金类型 其他，充值*/
	public static final int AMOUNT_TYPE_1 = 1;
	/**资金类型 其他，提现*/
	public static final int AMOUNT_TYPE_2 = 2;
	/**资金类型 其他，投资*/
	public static final int AMOUNT_TYPE_3 = 3;
	/**资金类型 其他，回款*/
	public static final int AMOUNT_TYPE_4 = 4;
	/**资金类型 其他，奖励*/
	public static final int AMOUNT_TYPE_5 = 5;
	
	/**充值状态(0 未完成)*/
	public static final int RECHARGE_STATUS_0 = 0;
	/***充值状态(1 成功) */
	public static final int RECHARGE_STATUS_1 = 1;
	/***新浪充值失败*/
	public static final int RECHARGE_STATUS_2 = 2;
	
	
	/**提现状态(0 待审核)*/
	public static final int WITHDRAW_STATUS_0 = 0;
	/**提现状态 1 待确定 */
	public static final int WITHDRAW_STATUS_1 = 1;
	/**提现状态(2 完成) */
	public static final int WITHDRAW_STATUS_2 = 2;
	/**提现状态(3 取消) */
	public static final int WITHDRAW_STATUS_3 = 3;
	
	/**审核状态 0 不通过 */
	public static final int CHECK_PASS_NO_0 = 0;
	/**审核状态 0 通过 */
	public static final int CHECK_PASS_1 = 1;
	
	/**用户资金  可用金额*/
	public static final String CUST_AMOUT_usableAmount = "usableAmount";
	/**用户资金  冻结金额*/
	public static final String CUST_AMOUT_freezeAmount ="freezeAmount";
	/**用户资金  待收金额*/
	public static final String CUST_AMOUT_dueinAmount ="dueinAmount";
	/**用户资金  待还金额*/
	public static final String CUST_AMOUT_dueoutAmount ="dueoutAmount";
	
	/**用户充值类型 充值 */
	public static final String RECHARGE_TYPE_R ="r";
	/**用户充值类型 优质单充值 */
	public static final String RECHARGE_TYPE_S ="s";
	/**用户充值类型  VIP购买 */
	public static final String RECHARGE_TYPE_V ="v";
	/**用户充值类型 房价评估次数购买 */
	public static final String RECHARGE_TYPE_F ="f";
	
	
	public static final String key_fundType = "fundType";
	
	/**冻结或其他信息相关**/
	/*** 提现 ***/
	/*** 冻结提现金额 ***/
	public static final String FundType_withdrawApply = "withdrawApply";
	/*** 取消冻结提现金额 ***/
	public static final String FundType_withdrawCancel = "withdrawCancel";
	/*** 提现成功 ***/
	public static final String FundType_withdraw = "withdraw";
	/*** 登记看房现金奖励 ***/
	public static final String FundType_lookReward = "lookReward";
	/*** 草签奖励 ***/
	public static final String FundType_initialSign = "initialSign";
	/*** 完全成交奖励 ***/
	public static final String FundType_completeDeal = "completeDeal";
	
	/*** 抢单消费 ***/
	public static final String FundType_AMT_ROB = "amtRob";
	/*** 招聘置顶消费 ***/
	public static final String FundType_AMT_TOP = "amtTop";
	/*** 抢优质单消费 ***/
	public static final String FundType_Senior_ROB = "seniorRob";
	
	/*** 推广获客付款消费 ***/
	public static final String FUNDTYPE_TG_PAYAMT = "tgPayAmt";
	
	/*** 门店人员退款 ***/
	public static final String FundType_store_back = "storeback";
	
	/*** 抢优质单提成 ***/
	public static final String FundType_Senior_Reward = "seniorReward";
	/*** 充值 ***/
	public static final String FundType_RECHARGE = "recharge";
	/*** 充值赠送 ***/
	public static final String FundType_RECHARGE_Reward = "rechargeAward";
	
	/***充值券赠送 ***/
	public static final String COUPON_RECHARGE = "couponRecharge";
	
	/*** 优质单充值 ***/
	public static final String FundType_SENIOR_RECHARGE = "seniorRecharge";
	/*** 抢单退款***/
	public static final String FundType_backRecord = "backRecord";
	/*** VIP退款***/
	public static final String FundType_VIP_BACK = "vipBackRecord";
	
	
	
	/*** 提成 ***/
	public static final String FundType_rewardOrder = "rewardOrder";
	/*** 师傅提成 ***/
	public static final String FundType_rewardOrderParent = "rewardOrderParent";
	
	
	/*** 奖励相关*/
	/*** 活动奖励 ***/
	public static final String FundType_rewardActivity = "rewardActivity";
	/*** 好友邀请奖励 ***/
	public static final String FundType_rewardRef = "rewardRef";
	
	/*** 推荐好友首次抢单奖励 ***/
	public static final String FundType_referer_rob = "refererFirstRob";
	
	/*** 连连支付，认证支付 ***/
	public static final String LIAN_RZPAY = "llrz";
	
	/*** 连连支付，快捷支付 ***/
	public static final String LIAN_KJPAY = "llkj";
	
	/*** 连连支付，认证支付 ***/
	public static final String LIAN_DDRZPAY = "ddllrz";
	
	/*** 连连支付，快捷支付 ***/
	public static final String LIAN_DDKJPAY = "ddllkj";
	
	/*** 微店出价推广产品 ***/
	public static final String FundType_BID_PROMOTION = "bidPromotion";
	/*** 微店加价推广产品 ***/
	public static final String FundType_MARKUP_PROMOTION = "markupPromotion";
	
	/*** 微店推广产品退款 ***/
	public static final String FundType_WD_TG_BACK = "wdTgBack";
	/*** 微店推广产品扣款 ***/
	public static final String FundType_WD_TG_PAY = "wdTgPay";
	
	/*** 接单类型 微店产品推广 ***/
	public static final String receiveType_WD_PROD_TG = "3";

}
