package org.xxjr.store.web.util;

import org.ddq.common.security.md5.Md5;

public class Key_SMS {

	/*** 交单跟进平台快捷登录  **/
	public final static String Key_SMS_STORE_KJ_LOGIN = "StorekjLoginKey";

	/**短信发送次数后缀*/
	public final static String SMS_COUNT_FIX = "_count";
	
	/**图形验证码后缀*/
	public final static String IMG_CODE_FIX = "_imgCode";
	
	public static void main(String[] args) {
		String telephone = "18670787211";
		
		System.out.println(Md5.getInstance().encrypt(telephone));
	}
}
