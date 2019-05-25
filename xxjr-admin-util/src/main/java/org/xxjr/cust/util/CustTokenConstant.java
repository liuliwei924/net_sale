package org.xxjr.cust.util;

/**
 * 用户token相关常量
 */
public class CustTokenConstant {

	/**
	 * 用户密码加密类型
	 */
	public final static String CUST_SESSION_TYPE_PWD = "pwd";
	
	/**
	 * 交易密码类型
	 */
	public final static String CUST_SESSION_TYPE_JYPWD = "jypwd";
	
	/**
	 * 用户signId信息
	 */
	public final static String USER_SIGNID = "signId";

	/**微信登录，用户存的sinID*/
	public static final String WX_CUST_Token="wxCustToken";
	
	/** 分销微信登录，用户存的sinID*/
	public static final String WX_FX_CUST_Token="wxFxCustToken";
	
	/**小程序的sinID*/
	public static final String XCX_SIN_Token="xcxSinToken";
	/** 微站用户标识*/
	public static final String WZ_USER_FLAG = "wz_user_flag";
	
	/** 微站用户标识*/
	public static final String DH_USER_FLAG = "dh_user_flag";
	/**
	 * 用户tokenId信息
	 */
	public final static String USER_TOKENID = "tokenId";
	
	/**
	 * 用户id
	 */
	public final static String CUST_ID = "custId";
	
	/**
	 * 互动吧用户id
	 */
	public final static String HDB_CUST_ID="hdbCustId";
	
	/**
	 * 用户登录类型
	 */
	public final static String CUST_LOGINTYPE = "loginType";
	
	/**
	 * 用户登录状态
	 */
	public final static String CUST_LOGINSTATUS = "loginStatus";
	
	/**
	 * 用户登录状态-已登录
	 */
	public final static String CUST_LOGINSTATUS_LOGININ = "loginIn";
	
	/**
	 * 用户登录状态-第三方授权
	 */
	public final static String CUST_LOGINSTATUS_LOGININIT = "loginInit";
	
	/**
	 * 用户登录状态-未登录
	 */
	public final static String CUST_LOGINSTATUS_LOGINOUT = "logintOut";
}
