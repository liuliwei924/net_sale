package org.llw.com.security;

import java.security.MessageDigest;

import org.llw.com.context.AppProperties;
import org.llw.com.exception.SysException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MD5Util {
	
	
	/**
	 * 获取系统密码的加密串
	 * @param password
	 * @return
	 */
	public static String  getEncryptPassword(String password){
		String pwdIsEncrypt = AppProperties.getProperties(AppProperties.PASSWORD_IS_ENCRYPT);
		String addP = "";
		//为1是说明已经加密
		if("1".equals(pwdIsEncrypt)){
			addP = AppProperties.getProperties(AppProperties.PASSWORD_MD5_KEY);
		}
		String newPas = getEncryptByKey(password, addP);
		return newPas;
	}
	
	/**
	 * 给字符串附加key加密
	 * @param str 需要加密的字符串
	 * @param key 附加的key
	 * @return
	 * @throws Exception 
	 */
	public static String  getEncryptByKey(String str,String key){
		String newPas = encrypt(str + key);
		return newPas;
	}
	
	public static String encrypt(String value) {
		try {
			char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
					'a', 'b', 'c', 'd', 'e', 'f' };
				byte[] strTemp = value.getBytes("utf-8");
				MessageDigest mdTemp = MessageDigest.getInstance("MD5");
				mdTemp.update(strTemp);
				byte[] md = mdTemp.digest();
				int j = md.length;
				char str[] = new char[j * 2];
				int k = 0;
				for (int i = 0; i < j; i++) {
					byte byte0 = md[i];
					str[k++] = hexDigits[byte0 >>> 4 & 0xf];
					str[k++] = hexDigits[byte0 & 0xf];
				}
				return new String(str);
		} catch (Exception e) {
			log.error("md5 encrypt:",e);
			throw new SysException("Not Support MD5 Encrypt");
		}
	}
	
	
	public static void main(String[] args) {
		//b9d11b3be25f5a1a7dc8ca04cd310b28
		System.out.println(getEncryptByKey("123456","Liuenc"));
	}


}