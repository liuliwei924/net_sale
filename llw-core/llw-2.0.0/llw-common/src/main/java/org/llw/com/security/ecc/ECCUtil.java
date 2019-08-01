package org.llw.com.security.ecc;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;

import org.llw.com.security.BASE64;

import lombok.extern.slf4j.Slf4j;

/**
 * ecc 加密/解密
 * liulw
 */
@Slf4j
public class ECCUtil {

	 public static String encrypt(String src, String publicKey) {
		 try {
			 byte[] srcB = src.getBytes(ECCConst.ENCODE);
			 byte[] enB = encrypt(srcB, publicKey);
			 
			 return Base64.getEncoder().encodeToString(enB);
			 
		 }catch (Exception e) {
			log.error("ECC 加密失败",e);
		 }
		 
		 return null;
	 }
	 
	 public static String decrypt(String src, String publicKey) {
		 try {
			 byte[] srcB = BASE64.getDecoder().decode(src);
			 byte[] deB = decrypt(srcB, publicKey);
			 
			 return new String(deB,ECCConst.ENCODE);
			 
		 }catch (Exception e) {
			log.error("ECC 解密失败",e);
		 }
		 
		 return null;
	 }
	 

    /**
     * 解密
     * @param data
     * @param publicKey
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);

        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ECCConst.ALGORITHM);

        ECPublicKey pubKey = (ECPublicKey) keyFactory
                .generatePublic(x509KeySpec);

        Cipher cipher = new NullCipher();
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }


    /**
     * 解密
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ECCConst.ALGORITHM);

        ECPrivateKey priKey = (ECPrivateKey) keyFactory
                .generatePrivate(pkcs8KeySpec);

        Cipher cipher = new NullCipher();
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        return cipher.doFinal(data);
    }
    
    public static void main(String[] argu) throws Exception {
    
		Map<String,String> map = ECCGenerator.getGenerateKey();
		String privKey = map.get("privateKey");
		String pubKey = map.get("publicKey");

		System.out.println("私钥：" + privKey);

		System.out.println("公钥：" + pubKey);
		String text = "java ECC 加FFFRGRSSSKFSFKDFEKFsdsdsdsfdfddg";
		long  l1 = System.currentTimeMillis();
		String str = ECCUtil.encrypt(text,pubKey);
		long  l2 = System.currentTimeMillis();
		System.out.println("密文：" + str);
		long  l3 = System.currentTimeMillis();
		String outputStr = new String(ECCUtil.decrypt(str,privKey));
		long  l4 = System.currentTimeMillis();
		System.out.println("原始文本：" + text);
		System.out.println("解密文本：" + outputStr);
		
		System.out.println("加密耗时："+ (l2 - l1));
		System.out.println("解密耗时："+ (l4 - l3));
    }
}