package org.xxjr.cust.util;

public class ShowErrorCode {

	
	/**手机号码验证错误代码**/
	public static final int telephone_format = 10001; 
	
	/**
	 * 密码长度错误代码
	 */
	public static final int password_length = 10002;
	
	/**
	 * 短信验证码为空的错误代码
	 */
	public static final int randomNo_empt = 10003;
	
	/**
	 * 短信验证码错误
	 */
	public static final int randomNo_error = 10004;
	
	/**
	 * 手机号码已被使用
	 */
	public static final int telephone_have_use = 10005;
	
	/**
	 * 无此推荐人
	 */
	public static final int no_referer = 10006;
	
	/**
	 * 用户名输入格式不正确
	 */
	public static final int user_name_input_error = 10007;
	
	/**
	 * 用户名已被使用
	 */
	public static final int user_name_have_use = 10008;
	
	
	/**没有参数rewardValue**/
	public static final int REWARD_NO_KEY_rewardValue = 25000;
	/**奖励值rewardValue不正确**/
	public static final int REWARD_rewardValue_isERROR = 25001;
	/**奖励值customerId 不存在**/
	public static final int REWARD_NO_KEY_customerId = 25003;
	
	/**缺少参数，需要来源客户和送客户！**/
	public static final int REWARD_NO_KEY_FROM_TO = 25004;

	/**用户注册，缺少 来源信息**/
	public static final int CUST_HAVE_NO_SOURCETYPE = 25005; 
	

	/**用户注册，微信缺少 openid**/
	public static final int CUST_HAVE_NO_openid = 11009; 
	/**用户注册，微信缺少 unionid**/
	public static final int CUST_HAVE_NO_unionid = 11010; 
	
}
