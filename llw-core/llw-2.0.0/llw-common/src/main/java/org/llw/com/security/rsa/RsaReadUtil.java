package org.llw.com.security.rsa;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;


/**
 * <b>公私钥读取工具</b><br>
 * <br>
 * 
 * @author 行者
 * @version 4.1.0
 */
@Slf4j
public final class RsaReadUtil {

	/**
	 * 根据Cer文件读取公钥
	 * 
	 * @param pubCerPath
	 * @return
	 */
	public static PublicKey getPublicKeyFromFile(String pubCerPath) {
		FileInputStream pubKeyStream = null;
		try {
			pubKeyStream = new FileInputStream(pubCerPath);
			byte[] reads = new byte[pubKeyStream.available()];
			pubKeyStream.read(reads);
			return getPublicKey(new String(reads,RsaConst.ENCODE));
		} catch (FileNotFoundException e) {
			// //log.error("公钥文件不存在:", e);
		} catch (Exception e) {
			// log.error("公钥文件读取失败:", e);
		} finally {
			if (pubKeyStream != null) {
				try {
					pubKeyStream.close();
				} catch (Exception e) {
					
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据私钥路径读取私钥
	 * 
	 * @param priPath
	 * @return
	 */
	public static PrivateKey getPrivateKeyFromFile(String priPath) {
		InputStream priKeyStream = null;
		try {
			priKeyStream = new FileInputStream(priPath);
			byte[] reads = new byte[priKeyStream.available()];
			priKeyStream.read(reads);
			return getPrivateKey(new String(reads, RsaConst.ENCODE));
		} catch (Exception e) {
			// log.error("解析文件，读取私钥失败:", e);
		} finally {
			if (priKeyStream != null) {
				try {
					priKeyStream.close();
				} catch (Exception e) {
					//
				}
			}
		}
		return null;
	}
	
	//将base64编码后的公钥字符串转成PublicKey实例
	public static PublicKey getPublicKey(String publicKey){
		try{
			byte[ ] keyBytes=Base64.getDecoder().decode(publicKey.getBytes(RsaConst.ENCODE));
			X509EncodedKeySpec keySpec=new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory=KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(keySpec);	
		}catch(Exception e){
			log.error("getPublicKey error",e);
		}
		return null;
	}
		
	//将base64编码后的私钥字符串转成PrivateKey实例
	public static PrivateKey getPrivateKey(String privateKey){
		try{
			byte[ ] keyBytes=Base64.getDecoder().decode(privateKey.getBytes(RsaConst.ENCODE));
			PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory=KeyFactory.getInstance("RSA");
			return keyFactory.generatePrivate(keySpec);
		}catch(Exception e){
			log.error("getPrivateKey error",e);
		}
		return null;
	}
	

	/**
	 * 根据PFX私钥字节流读取私钥
	 * 
	 * @param pfxBytes
	 * @param priKeyPass
	 * @return
	 */
	public static PrivateKey getPrivateKeyByStream(byte[] pfxBytes, String priKeyPass) {
		try {
			KeyStore ks = KeyStore.getInstance(RsaConst.KEY_PKCS12);
			char[] charPriKeyPass = priKeyPass.toCharArray();
			ks.load(new ByteArrayInputStream(pfxBytes), charPriKeyPass);
			Enumeration<String> aliasEnum = ks.aliases();
			String keyAlias = null;
			if (aliasEnum.hasMoreElements()) {
				keyAlias = (String) aliasEnum.nextElement();
			}
			return (PrivateKey) ks.getKey(keyAlias, charPriKeyPass);
		} catch (IOException e) {
			// 加密失败
			// log.error("解析文件，读取私钥失败:", e);
		} catch (KeyStoreException e) {
			// log.error("私钥存储异常:", e);
		} catch (NoSuchAlgorithmException e) {
			// log.error("不存在的解密算法:", e);
		} catch (CertificateException e) {
			// log.error("证书异常:", e);
		} catch (UnrecoverableKeyException e) {
			// log.error("不可恢复的秘钥异常", e);
		}
		return null;
	}
	

	/**
	 * 根据私钥路径读取私钥
	 * 
	 * @param pfxPath
	 * @param priKeyPass
	 * @return
	 */
	public static PrivateKey getPrivateKeyFromFile(String pfxPath, String priKeyPass) {
		InputStream priKeyStream = null;
		try {
			priKeyStream = new FileInputStream(pfxPath);
			byte[] reads = new byte[priKeyStream.available()];
			priKeyStream.read(reads);
			return getPrivateKeyByStream(reads, priKeyPass);
		} catch (Exception e) {
			// log.error("解析文件，读取私钥失败:", e);
		} finally {
			if (priKeyStream != null) {
				try {
					priKeyStream.close();
				} catch (Exception e) {
					//
				}
			}
		}
		return null;
	}
}
