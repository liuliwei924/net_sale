package org.xxjr.sys.util.active;

public class ActiveConstants {

	/**判断方式 =**/
	public final static int PARAM_JUDY_1 = 1;
	/**判断方式 >**/
	public final static int PARAM_JUDY_2 = 2;
	/**判断方式 >=**/
	public final static int PARAM_JUDY_3 = 3;
	/**判断方式 <**/
	public final static int PARAM_JUDY_4 = 4;
	/**判断方式 <=**/
	public final static int PARAM_JUDY_5 = 5;
	/**判断方式 in **/
	public final static int PARAM_JUDY_6 = 6;
	/**判断方式 not in**/
	public final static int PARAM_JUDY_7 = 7;
	
	
	/**无后续操作0*/
	public final static int PARAM_APPEND_NO = 0;
	/**and 与操作1*/
	public final static int PARAM_APPEND_AND = 1;
	/**or 或操作 2*/
	public final static int PARAM_APPEND_OR = 2;
	
	
	/**参数类型 0 规则参数*/
	public final static int PARAM_TYPE_IN = 0;
	/**参数类型 1 活动参数*/
	public final static int PARAM_TYPE_CON = 1;
	/**参数类型 2 定义参数*/
	public final static int PARAM_TYPE_OUT = 2;
	
	
	/**状态 初始**/
	public final static int ACTIVE_STATUS_0 = 0;
	/**状态 正在使用**/
	public final static int ACTIVE_STATUS_1 = 1;
	/**状态 已经完成**/
	public final static int ACTIVE_STATUS_2 = 2;
	/**状态 已经作废**/
	public final static int ACTIVE_STATUS_3 = 3;
	
	
	/** 系统的参数KEY operator 操作类型 **/
	public final static String KEY_PARAM_operator = "operator";
	
	/** 系统的参数KEY condition 属于某活动的条件**/
	public final static String KEY_PARAM_condition = "condition";
	/** 系统的参数KEY 活动的 code  activeCode**/
	public final static String KEY_PARAM_activeCode = "activeCode";
	
	/** 操作类型之 注册 **/
	public final static String Operator_REGISTER = "register";
	/** 操作类型之 签到**/
	public final static String Operator_SIGN = "sign";
	/** 操作类型之 维护客户资料**/
	public final static String Operator_CONTACT = "contact";
	/** 操作类型之 分享 **/
	public final static String Operator_SHARE = "share";
	/** 操作类型之 交单 **/
	public final static String Operator_SUBMIT = "submit";
	/** 操作类型之 登陆 **/
	public final static String Operator_LOGIN = "login";
	/** 操作类型之 投资 **/
	public final static String Operator_INVEST = "invest";
	/** 操作类型之 首次投资 **/
	public final static String Operator_FIRST_INVEST = "firstInvest";
	/** 操作类型之 推荐人**/
	public final static String Operator_Referrer = "referrer";
	/** 操作类型之 实名认证**/
	public final static String Operator_Identify = "identify";
	/** 操作类型之 工作认证**/
	public final static String Operator_IdentifyCard = "identifyCard";
	/** 操作类型之 赠送单子**/
	public final static String Operator_IdentifyCardOrder = "identifyCardOrder";
	/** 操作类型之 赠送抢单券 **/
	public final static String Operator_IdentifyCardTicket = "identifyCardTicket";
	/** 操作类型之 邮箱谁认证**/
	public final static String Operator_Email = "emaill";
	/** 操作类型之 首次抢单 */
	public final static String Operator_First_Rob = "firstRob";
	/** 操作类型之 充值 */
	public final static String Operator_Recharge = "recharge";
	/** 马甲包操作类型之 充值 */
	public final static String Mjb_Operator_Recharge = "mjbRecharge";
	/** 操作类型之 抢单 */
	public final static String Operator_Rob = "rob";
	/** 操作类型之 完成工作认证和充值赠送抢单券 **/
	public final static String Operator_IdentifyCardAndRecharge = "identifyCardAndRecharge";
	/** 操作类型之 首次充值 **/
	public final static String Operator_First_Recharge = "firstRecharge";
	
}
