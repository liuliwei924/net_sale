package org.llw.com.security.rsa;

 final class RsaConst {

	 /** 编码 */
	 protected final static String ENCODE = "UTF-8";

	 protected final static String KEY_X509 = "X509";
	 protected final static String KEY_PKCS12 = "PKCS12";
	 protected final static String KEY_ALGORITHM = "RSA";
	 protected final static String CER_ALGORITHM = "MD5WithRSA";

	 protected final static String RSA_CHIPER = "RSA/ECB/PKCS1Padding";

	 protected final static int KEY_SIZE = 1024;
	/** 1024bit 加密块 大小 */
	 protected final static int ENCRYPT_KEYSIZE = 117;
	/** 1024bit 解密块 大小 */
	 protected final static int DECRYPT_KEYSIZE = 128;
}
