package org.xxjr.open;

/**
 * 开发平台常量
 * @author Administrator
 *
 */
public class OpenConstant {

	//审批结论：10-审批通过，30-审批需重填资料,40-审批不通过
	public static final int APPROVE_PASS = 10;
	public static final int APPROVE_AGAIN = 30;
	public static final int APPROVE_NO_PASS = 40;
	
	//订单状态：0未推送，1推送成功、审核中，2推送失败，3审核成功，4审核失败， 5审核退回，6贷款取消，7放款成功，8放款失败，9逾期，10贷款结清
	public static final int ORDER_STATUS_UNDONE = 0;
	public static final int ORDER_STATUS_PUSH_SUCCESS = 1;
	public static final int ORDER_STATUS_PUSH_FAIL = 2;
	public static final int ORDER_STATUS_CHECK_SUCCESS = 3;
	public static final int ORDER_STATUS_CHECK_FAIL = 4;
	public static final int ORDER_STATUS_CHECK_BACK = 5;
	public static final int ORDER_STATUS_LOAN_CANCEL = 6;
	public static final int ORDER_STATUS_LOAN_SUCCESS = 7;
	public static final int ORDER_STATUS_LOAN_FAIL = 8;
	public static final int ORDER_STATUS_LOAN_OVERDUE = 9;
	public static final int ORDER_STATUS_LOAN_SETTLE = 10;
	
	//绑卡状态：0绑定中，1绑定成功，2绑定失败
	public static final int BINDING_STATUS = 0;
	public static final int BIND_STATUS_SUCCESS = 1;
	public static final int BIND_STATUS_FAIL = 2;
	
	//000-请求成功  999-请求失败 001-请求IP未报备 002-参数缺失 003-签名错误 005-相关记录查询不到
	public final static String REQUEST_SUCCESS = "000";
	public final static String REQUEST_FAIL = "999";
	public final static String REQUEST_IP_ERROR = "001";
	public final static String REQUEST_PARAM_LACK = "002";
	public final static String REQUEST_SIGN_ERROR = "003";
	public final static String REQUEST_QUERY_FAIL = "005";
	
	//平台请求接口类型
	//（1老客户申请 2申请订单 3个人信息补充 4银行卡绑卡5运营商数据6订单状态7审批结果8审批用户确认9贷款合同10还款计划11展期申请12试算13当期还款状态14主动还款15获取展期详情）
	public final static int INTERFACE_TYPE_GET_OLD_CUST = 1;
	public final static int INTERFACE_TYPE_PUST_BASE_INFO = 2;
	public final static int INTERFACE_TYPE_PUSH_ADDITION_INFO = 3;
	public final static int INTERFACE_TYPE_PUSH_BIND_CARD = 4;
	public final static int INTERFACE_TYPE_PUSH_MOBILE_REPORT = 5;
	public final static int INTERFACE_TYPE_GET_ORDER_STATUS = 6;
	public final static int INTERFACE_TYPE_GET_APPROVE_RESULT = 7;
	public final static int INTERFACE_TYPE_PUSH_APPROVE_ACK = 8;
	public final static int INTERFACE_TYPE_GET_CONTRACT = 9;
	public final static int INTERFACE_TYPE_GEG_REPAY_PLAN = 10;
	public final static int INTERFACE_TYPE_PUSH_PERIOD_APPLY = 11;
	public final static int INTERFACE_TYPE_GET_TRIAL = 12;
	public final static int INTERFACE_TYPE_GET_REPAY_DTL = 13;
	public final static int INTERFACE_TYPE_PUSH_REPAY = 14;
	public final static int INTERFACE_TYPE_GET_PERIOD_DTL= 15;

	//用户授权类型
	/**网银账单*/
	public final static int AUTH_TYPE_IBANK = 1;
	/**信用卡邮箱*/
	public final static int AUTH_TYPE_EC = 2;
	/**公积金*/
	public final static int AUTH_TYPE_HF = 3;
	/**支付宝*/
	public final static int AUTH_TYPE_ALIPAY = 4;
	/**京东*/
	public final static int AUTH_TYPE_JD = 5;
	/**运营商授权*/
	public final static int AUTH_TYPE_MOBILE = 6;
	/**社保*/
	public final static int AUTH_TYPE_SS = 7;
	/**寿险保单*/
	public final static int AUTH_TYPE_INSURANCE = 8;
	/**征信报告*/
	public final static int AUTH_TYPE_CREDIT = 9;
	/**芝麻信用分*/
	public final static int AUTH_TYPE_ZHIMA = 10;
	
}
