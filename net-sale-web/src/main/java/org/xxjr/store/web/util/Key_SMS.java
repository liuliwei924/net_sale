package org.xxjr.store.web.util;

import java.util.Date;

import org.ddq.common.security.MD5Util;

public class Key_SMS {

	/*** 交单跟进平台快捷登录  **/
	public final static String Key_SMS_STORE_KJ_LOGIN = "StorekjLoginKey";

	/**短信签名*/
	public final static String SMS_TGW_SINGN_NAME = "天狗窝区块链";
	
	/**反馈通知 模板ID*/
	public final static String TGW_FEED_BACK_TEMPID = "168064";

	
	public static void main(String[] args) {
		String time = "20190521153000789";
		String telephone = "13684962402";
		String merchId = "test001";
		
		String sign = MD5Util.getEncryptByKey(telephone + "&" + time, merchId);
		System.out.println(sign);
	}
		
}
