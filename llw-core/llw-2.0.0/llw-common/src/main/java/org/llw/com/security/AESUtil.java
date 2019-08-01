package org.llw.com.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

/**
 * AES 对称算法，效率高于 DES
 * @author liulw 2019-06-25
 *
 */
@Slf4j
public class AESUtil {

	private static final String AES_ALGORITHM = "AES";  
	/**
	 * 加密
	 * 
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密码
	 * @return
	 */
	public static byte[] encrypt(String content, String password) {
		try {
			SecretKey key = generateKey(password);
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		} catch (Exception e) {
			log.error("encryt error:",e);
		}
		return null;
	}

    /** 
     * 获得密钥 
     *  
     * @param secretKey 
     * @return 
     * @throws NoSuchAlgorithmException  
     * @throws InvalidKeyException  
     * @throws InvalidKeySpecException  
     */  
    private static SecretKey generateKey(String secretKey) throws NoSuchAlgorithmException {  
    	// 为我们选择的DES算法生成一个KeyGenerator对象  
        KeyGenerator kg = KeyGenerator.getInstance(AES_ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");  
        secureRandom.setSeed(secretKey.getBytes());  
        kg.init(secureRandom); 
        // 生成密钥  
        return kg.generateKey();  
    }  
	/**
	 * 解密
	 * 
	 * @param content
	 *            待解密内容
	 * @param password
	 *            解密密钥
	 * @return
	 */
	public static byte[] decrypt(byte[] content, String password) {
		try {
			SecretKey key = generateKey(password);
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(content);
			return result; // 加密
		} catch (Exception e) {
	       log.error("decrypt:",e);
		}
		return null;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf	
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}
	
	public static String getEncryptString(String content, String password) {
		byte[] encryptResult = encrypt(content, password);
		String encryptResultStr = parseByte2HexStr(encryptResult);
		return encryptResultStr;
	}
	
	public static String getDecryptString(String encryptResultStr, String password) {
		byte[] decryptFrom = parseHexStr2Byte(encryptResultStr);
		byte[] decryptResult = decrypt(decryptFrom, password);
		return new String(decryptResult);
	}
    
  public static void main(String[] args) {
	String str = "dfdfdffhhy12132顺丰到付";
	
	String ens = AESUtil.getEncryptString(str, "");
	
	System.out.println("ens:" + ens);
	
	String des = AESUtil.getDecryptString(ens, "");
	
	System.out.println("des:" +des);
}
}
