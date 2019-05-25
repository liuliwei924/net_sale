package org.xxjr.store.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec; 

import lombok.extern.slf4j.Slf4j;
 
/**
 * AES2加密解密
 * @author 
 *
 */
@Slf4j
public class AES2{
    private static final String AESTYPE ="AES/ECB/PKCS5Padding"; 
    /**
     * 加密
     * @param plainText
     * @param keyStr
     * @return
     */
    public static String encrypt(String plainText,String keyStr) { 
        byte[] encrypt = null; 
        try{
            Key key = generateKey(keyStr); 
            Cipher cipher = Cipher.getInstance(AESTYPE); 
            cipher.init(Cipher.ENCRYPT_MODE, key); 
            encrypt = cipher.doFinal(plainText.getBytes("UTF-8"));     
        }catch(Exception e){
        	log.error("content:"+plainText+",password:"+keyStr, e);
        } 
        String rs = parseByte2HexStr(encrypt);
        log.debug("encrypt;content:"+plainText+";password:"+keyStr+";rs:"+rs);
        return rs ;
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
	
    /**
     * 解密
     * @param encryptData
     * @param keyStr 16个字节
     * @return
     */
    public static String decrypt(String encryptData,String keyStr) {
        byte[] decrypt = null; 
        try{ 
            Key key = generateKey(keyStr); 
            Cipher cipher = Cipher.getInstance(AESTYPE); 
            cipher.init(Cipher.DECRYPT_MODE, key); 
            decrypt = cipher.doFinal(parseHexStr2Byte(encryptData));
        }catch(Exception e){ 
           log.error("decry pt error:", e);
        } 
        try {
			String rs = new String(decrypt,"UTF-8") ;
			return rs ;
		} catch (UnsupportedEncodingException e) {
			log.error("un supported", e);
			return "" ;
		} catch (Exception e) {
			log.error("other exception", e);
			return "" ;
		}
    } 
 
    private static Key generateKey(String key)throws Exception{ 
        try{
        	SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES"); 
            return keySpec; 
        }catch(Exception e){
        	log.error("other exception", e);
            throw e; 
        } 
    } 
 
    public static void main(String[] args) {
         
        String keyStr = "xxjr123456@#xx#8";
 
        String plainText = "this is a string will be AES_Encrypt";
        String encText = encrypt(plainText ,keyStr);
        log.info(encText); 
        String decString = decrypt(encText ,keyStr); 
        log.info(decString); 
 
    } 
}