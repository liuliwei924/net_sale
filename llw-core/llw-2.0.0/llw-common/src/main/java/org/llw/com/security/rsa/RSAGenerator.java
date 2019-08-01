package org.llw.com.security.rsa;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RSAGenerator {

	/**
	 * 生成密钥对
	 * 
	 * @throws Exception
	 */
	public static Map<String, String> generateKeyPair() {
		try {
			/** 为RSA算法创建一个KeyPairGenerator对象 */
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RsaConst.KEY_ALGORITHM);

			/** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
			keyPairGenerator.initialize(RsaConst.KEY_SIZE);

			/** 生成密匙对 */
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			/** 得到公钥 */
			Key publicKey = keyPair.getPublic();

			/** 得到私钥 */
			Key privateKey = keyPair.getPrivate();
			byte[] publicKeyBytes = publicKey.getEncoded();
			byte[] privateKeyBytes = privateKey.getEncoded();

			String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
			String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);

			Map<String, String> keyMap = new HashMap<String, String>();
			keyMap.put("publicKey", publicKeyBase64);
			keyMap.put("privateKey", privateKeyBase64);
			
			return keyMap;
		} catch (Exception e) {
			log.error("rsaByPrivateKey error",e);
			e.printStackTrace();
		}

		return null;
	}
}
