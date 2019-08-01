package org.llw.com.security.rsa;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import javax.crypto.Cipher;

import org.llw.com.security.BASE64;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * <b>Rsa加解密工具</b><br>
 * <br>
 * 公钥采用X509,Cer格式的<br>
 * 私钥采用PKCS12加密方式的PFX私钥文件<br>
 * 加密算法为1024位的RSA，填充算法为PKCS1Padding<br>
 * 
 * @author liulw
 * @version 4.1.0
 */
@Slf4j
public final class RSAUtil {


	// ======================================================================================
	// 公钥加密私钥解密段
	// ======================================================================================

	/**
	 * 指定Cer公钥路径加密
	 * 
	 * @param src
	 * @param pubCerPath
	 * @return hex串
	 */
	public static String encryptByPubCerFile(String src, String pubCerPath) {

		PublicKey publicKey = RsaReadUtil.getPublicKeyFromFile(pubCerPath);
		if (publicKey == null) {
			return null;
		}
		return encryptByPublicKey(src, publicKey);
	}

	/**
	 * 用公钥内容加密
	 * 
	 * @param src
	 * @param pubKeyText
	 * @return hex串
	 */
	public static String encryptByPubCerText(String src, String pubKeyText) {
		PublicKey publicKey = RsaReadUtil.getPublicKey(pubKeyText);
		if (publicKey == null) {
			return null;
		}
		return encryptByPublicKey(src, publicKey);
	}

	/**
	 * 公钥加密返回
	 * 
	 * @param src
	 * @param publicKey
	 * @param encryptMode
	 * @return hex串
	 */
	public static String encryptByPublicKey(String src, PublicKey publicKey) {
		byte[] destBytes = rsaByPublicKey(src.getBytes(), publicKey, Cipher.ENCRYPT_MODE);

		if (destBytes == null) {
			return null;
		}
		return BASE64.getEncoder().encodeToString(destBytes);
	}

	/**
	 * 根据私钥文件解密
	 * 
	 * @param src
	 * @param pfxPath
	 * @param priKeyPass
	 * @return
	 */
	public static String decryptByPriPfxFile(String src, String pfxPath, String priKeyPass) {
		if (StringUtils.isEmpty(src) || StringUtils.isEmpty(pfxPath)) {
			return null;
		}
		PrivateKey privateKey = RsaReadUtil.getPrivateKeyFromFile(pfxPath, priKeyPass);
		if (privateKey == null) {
			return null;
		}
		return decryptByPrivateKey(src, privateKey);
	}

	/**
	 * 根据私钥文件流解密
	 * 
	 * @param src
	 * @param pfxPath
	 * @param priKeyPass
	 * @return
	 */
	public static String decryptByPriPfxStream(String src, byte[] pfxBytes, String priKeyPass) {
		if (StringUtils.isEmpty(src)) {
			return null;
		}
		PrivateKey privateKey = RsaReadUtil.getPrivateKeyByStream(pfxBytes, priKeyPass);
		if (privateKey == null) {
			return null;
		}
		return decryptByPrivateKey(src, privateKey);
	}
	
	/**
	 * 用私钥内容解密
	 * 
	 * @param src
	 * @param pubKeyText
	 * @return hex串
	 */
	public static String decryptByPrivateText(String src, String priKeyText) {
		PrivateKey publicKey = RsaReadUtil.getPrivateKey(priKeyText);
		if (publicKey == null) {
			return null;
		}
		return decryptByPrivateKey(src, publicKey);
	}

	/**
	 * 私钥解密
	 * 
	 * @param src
	 * @param privateKey
	 * @return
	 */
	public static String decryptByPrivateKey(String src, PrivateKey privateKey) {
		if (StringUtils.isEmpty(src)) {
			return null;
		}
		try {
			byte[] srcB = BASE64.getDecoder().decode(src);
			byte[] destBytes = rsaByPrivateKey(srcB, privateKey, Cipher.DECRYPT_MODE);
			if (destBytes == null) {
				return null;
			}
			return new String(destBytes, RsaConst.ENCODE);
		} catch (Exception e) {
			log.error("RSA 解密内容异常",e);
		}

		return null;
	}


	// ======================================================================================
	// 公私钥算法
	// ======================================================================================
	/**
	 * 公钥算法
	 * 
	 * @param srcData
	 *            源字节
	 * @param publicKey
	 *            公钥
	 * @param mode
	 *            加密 OR 解密
	 * @return
	 */
	public static byte[] rsaByPublicKey(byte[] srcData, PublicKey publicKey, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(RsaConst.RSA_CHIPER);
			cipher.init(mode, publicKey);
			// 分段加密
			int blockSize = (mode == Cipher.ENCRYPT_MODE) ? RsaConst.ENCRYPT_KEYSIZE : RsaConst.DECRYPT_KEYSIZE;
			byte[] encryptedData = null;
			for (int i = 0; i < srcData.length; i += blockSize) {
				// 注意要使用2的倍数，否则会出现加密后的内容再解密时为乱码
				byte[] doFinal = cipher.doFinal(subarray(srcData, i, i + blockSize));
				encryptedData = addAll(encryptedData, doFinal);
			}
			return encryptedData;

		} catch (Exception e) {
			log.error("公钥算法-不存在的解密算法:",e);
		} 
		return null;
	}

	/**
	 * 私钥算法
	 * 
	 * @param srcData
	 *            源字节
	 * @param privateKey
	 *            私钥
	 * @param mode
	 *            加密 OR 解密
	 * @return
	 */
	public static byte[] rsaByPrivateKey(byte[] srcData, PrivateKey privateKey, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(RsaConst.RSA_CHIPER);
			cipher.init(mode, privateKey);
			// 分段加密
			int blockSize = (mode == Cipher.ENCRYPT_MODE) ? RsaConst.ENCRYPT_KEYSIZE : RsaConst.DECRYPT_KEYSIZE;
			byte[] decryptData = null;
			
			for (int i = 0; i < srcData.length; i += blockSize) {
				byte[] doFinal = cipher.doFinal(subarray(srcData, i, i + blockSize));
				
				decryptData = addAll(decryptData, doFinal);
			}
			return decryptData;
		} catch (Exception e) {
			log.error("私钥解密失败:",e);
		} 
		return null;
	}

	// /////////////==========================
	public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) {
			return null;
		}
		if (startIndexInclusive < 0) {
			startIndexInclusive = 0;
		}
		if (endIndexExclusive > array.length) {
			endIndexExclusive = array.length;
		}
		int newSize = endIndexExclusive - startIndexInclusive;
				
		if (newSize <= 0) {
			return new byte[0];
		}

		byte[] subarray = new byte[newSize];

		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);

		return subarray;
	}

	public static byte[] addAll(byte[] array1, byte[] array2) {
		if (array1 == null) {
			return clone(array2);
		} else if (array2 == null) {
			return clone(array1);
		}
		byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	public static byte[] clone(byte[] array) {
		if (array == null) {
			return null;
		}
		return (byte[]) array.clone();
	}
	
	public static void main(String[] args) {
		Map<String,String> keyMap = RSAGenerator.generateKeyPair();
		
		String pubkey = keyMap.get("publicKey");
		String prikey = keyMap.get("privateKey");
		
		String str = "我是BDY  ，hello";
		long  l1 = System.currentTimeMillis();
		String enS = RSAUtil.encryptByPubCerText(str, pubkey);
		long  l2 = System.currentTimeMillis();
		String deS = RSAUtil.decryptByPrivateText(enS, prikey);
		long  l3 = System.currentTimeMillis();
		
		System.out.println("加密：" + enS);
		System.out.println("解密：" + deS);
		
		System.out.println("加密耗时："+ (l2 - l1));
		System.out.println("解密耗时："+ (l3 - l2));
	}
}
