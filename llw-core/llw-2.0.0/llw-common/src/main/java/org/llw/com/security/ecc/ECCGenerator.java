package org.llw.com.security.ecc;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.llw.com.security.BASE64;
 
public class ECCGenerator  {
	static{
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
			Security.addProvider(new BouncyCastleProvider());
		}

	}
 
    public static Map<String,String> getGenerateKey() throws NoSuchProviderException, NoSuchAlgorithmException {
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECCConst.ALGORITHM,ECCConst.PROVIDER);
        keyPairGenerator.initialize(256, new SecureRandom());
        KeyPair kp = keyPairGenerator.generateKeyPair();
        ECPublicKey publicKey = (ECPublicKey) kp.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) kp.getPrivate();
        Map<String,String> map = new HashMap<>();
 
        map.put("privateKey", BASE64.getEncoder().encodeToString(privateKey.getEncoded()));
        map.put("publicKey", BASE64.getEncoder().encodeToString(publicKey.getEncoded()));
        return map;
    }
}
