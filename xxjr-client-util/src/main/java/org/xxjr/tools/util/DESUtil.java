package org.xxjr.tools.util;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
public class DESUtil {
	public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";  
    
    /** 
     * DES算法，加密 
     * 
     * @param data 待加密字符串 
     * @param key  加密私钥，长度不能够小于8位 
     * @return 加密后的字节数组，一般结合Base64编码使用 
     * @throws InvalidAlgorithmParameterException  
     * @throws Exception  
     */  
    public static String encode(String key,String data) {  
        if(data == null)  
            return null;  
        try{  
            DESKeySpec dks = new DESKeySpec(key.getBytes());              
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");  
            //key的长度不能够小于8位字节  
            Key secretKey = keyFactory.generateSecret(dks);  
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);  
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());  
            AlgorithmParameterSpec paramSpec = iv;  
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,paramSpec);             
            byte[] bytes = cipher.doFinal(data.getBytes());              
            return byte2hex(bytes);  
        }catch(Exception e){  
            e.printStackTrace();  
            return data;  
        }  
    }  
  
    /** 
     * DES算法，解密 
     * 
     * @param data 待解密字符串 
     * @param key  解密私钥，长度不能够小于8位 
     * @return 解密后的字节数组 
     * @throws Exception 异常 
     */  
    public static String decode(String key,String data) {  
        if(data == null)  
            return null;  
        try {  
            DESKeySpec dks = new DESKeySpec(key.getBytes());  
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");  
            //key的长度不能够小于8位字节  
            Key secretKey = keyFactory.generateSecret(dks);  
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);  
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());  
            AlgorithmParameterSpec paramSpec = iv;  
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);  
            return new String(cipher.doFinal(hex2byte(data.getBytes())));  
        } catch (Exception e){  
            e.printStackTrace();  
            return data;  
        }  
    }  
  
    /** 
     * 二行制转字符串 
     * @param b 
     * @return 
     */  
    private static String byte2hex(byte[] b) {  
        StringBuilder hs = new StringBuilder();  
        String stmp;  
        for (int n = 0; b!=null && n < b.length; n++) {  
            stmp = Integer.toHexString(b[n] & 0XFF);  
            if (stmp.length() == 1)  
                hs.append('0');  
            hs.append(stmp);  
        }  
        return hs.toString().toUpperCase();  
    }  
      
    private static byte[] hex2byte(byte[] b) {  
        if((b.length%2)!=0)  
            throw new IllegalArgumentException();  
        byte[] b2 = new byte[b.length/2];  
        for (int n = 0; n < b.length; n+=2) {  
            String item = new String(b,n,2);  
            b2[n/2] = (byte)Integer.parseInt(item,16);  
        }  
        return b2;  
    }  
    
    
    public static void main(String[] args) {
    	/*
    	DESUtil t = new DESUtil();
		String encodeRules = "p484o51511";
		String content = "15298967709_17330814099_2018-01-18 14:28:21";
		String a = t.encode(encodeRules, content);
		System.out.println("加密："+a);
		
		String b = t.decode(encodeRules, a);
		String bc ="E806D5A543523FD0635492738A151350AFA3606ACA937F162FD86A1B56B2EFD053FF1484E25D8478FF960F96CC5E30DC";
		System.out.println(t.decode(encodeRules, bc));*/
		
	}
}
