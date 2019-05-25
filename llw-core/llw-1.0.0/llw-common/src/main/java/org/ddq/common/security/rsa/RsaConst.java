package org.ddq.common.security.rsa;

public final class RsaConst {

	/** 编码 */
	public final static String ENCODE = "UTF-8";

	public final static String KEY_X509 = "X509";
	public final static String KEY_PKCS12 = "PKCS12";
	public final static String KEY_ALGORITHM = "RSA";
	public final static String CER_ALGORITHM = "MD5WithRSA";

	public final static String RSA_CHIPER = "RSA/ECB/PKCS1Padding";

	public final static int KEY_SIZE = 1024;
	/** 1024bit 加密块 大小 */
	public final static int ENCRYPT_KEYSIZE = 117;
	/** 1024bit 解密块 大小 */
	public final static int DECRYPT_KEYSIZE = 128;
	
	
	/**
	 * 将16进制字符串转为转换成字符串
	 */
	public static byte[] hex2Bytes(String source) {
		byte[] sourceBytes = new byte[source.length() / 2];
		for (int i = 0; i < sourceBytes.length; i++) {
			sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2, i * 2 + 2), 16);
		}
		return sourceBytes;
	}
	
	
	/**
	 * 将byte[] 转换成字符串
	 */
	public static String byte2Hex(byte[] srcBytes) {
		StringBuilder hexRetSB = new StringBuilder();
		for (byte b : srcBytes) {
			String hexString = Integer.toHexString(0x00ff & b);
			hexRetSB.append(hexString.length() == 1 ? 0 : "").append(hexString);
		}
		return hexRetSB.toString();
	}

}
