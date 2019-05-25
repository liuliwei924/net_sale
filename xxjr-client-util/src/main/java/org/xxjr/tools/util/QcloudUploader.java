package org.xxjr.tools.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;

import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xxjr.sys.util.SysParamsUtil;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;

/**
 * 腾讯云上传插件
 * 
 * @author Administrator
 *
 */
public class QcloudUploader {
	
	private static COSClient cosClient;

	private static int appId = 10058268;
	
	private static String headImgBucket = "head";
			
	private static COSClient getCOSClient() {
		if(cosClient == null){
			String secretId = SysParamsUtil.getStringParamByKey("headImgSecretId",
					"AKIDDlJbZDI7rzaIZsCZLgQ9WIsAcWtzNW6P");
			String secretKey = SysParamsUtil.getStringParamByKey(
					"headImgSecretKey", "vO13KaGTI4tcmqcJY4ZkdIcx43YslB1n");
			COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
			ClientConfig clientConfig = new ClientConfig(new Region("ap-shanghai"));
			cosClient = new COSClient(cred, clientConfig);
    	}
		return cosClient;
	}

	/**
	 * 上传本地文件到bucket根目录下
	 * @param key
	 * @param localFilePath
	 * @return
	 */
	public static boolean uploadFile(String key, String localFilePath) {
		String bucketName = headImgBucket + "-" + appId;
		COSClient cosClient = getCOSClient();
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, new File(localFilePath));
		PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
		// 关闭客户端
		cosClient.shutdown();
		return isSuccess(putObjectResult);
	}
	
	/**
	 * 上传文件流到bucket指定目录下
	 * @param key
	 * @param inputStream
	 * @param fileLength
	 * @param contentType
	 * @return
	 */
	public static boolean uploadFile(String key, InputStream inputStream, long fileLength, String contentType) {
		String bucketName = headImgBucket + "-" + appId;
		COSClient cosClient = getCOSClient();
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(fileLength);
		objectMetadata.setContentType(contentType);
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);
		putObjectRequest.setStorageClass(StorageClass.Standard_IA);
		PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
		// 关闭客户端
		cosClient.shutdown();
		return isSuccess(putObjectResult);
	}

	/**
	 * 目录不存在时创建目录
	 * 
	 * @param cosFolderPath
	 * @return
	 */
	public static boolean createDirOnNotExists(String cosFolderPath) {
		COSClient cosClient = getCOSClient();
		String bucketName = headImgBucket + "-" + appId;
		boolean exist = cosClient.doesObjectExist(bucketName, cosFolderPath);
		if(!exist){
			// 目录对象即是一个/结尾的空文件，上传一个长度为 0 的 byte 流
			InputStream input = new ByteArrayInputStream(new byte[0]);
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(0);
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, cosFolderPath, input, objectMetadata);
			PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
			return isSuccess(putObjectResult);
		}
		return true;
	}
	
    /***
     * 保存到腾讯云
     * @param request
     * @param map
     * @param fileType
     * @throws Throwable
     */
    public static String uploadToTenXun(MultipartFile file, String fileType) throws Throwable{
    	InputStream inputStream = file.getInputStream();
    	String contentType = file.getContentType();
    	long fileSize = file.getSize();
    	String desCosPath = "/upfile/" + fileType + "/" 
    			+ DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD) + "/";
    	String originaFileName = file.getOriginalFilename();
		String uploadFileType = ".png";
		if(originaFileName.lastIndexOf(".") > 0){
			uploadFileType = originaFileName.substring(originaFileName.lastIndexOf("."));
		}
    	String saveName = StringUtil.getUUID() + uploadFileType;
    	createDirOnNotExists(desCosPath);
		boolean isSuccess = QcloudUploader.uploadFile(desCosPath + saveName, inputStream, fileSize, contentType);
		if(isSuccess){
			return "https://static.xxjr.com/" + desCosPath + saveName;
		} else {
			return null;
		}
    }

	/**
	 * 删除目录
	 * 
	 * @param cosFolderPath
	 * @return
	 */
	public static void deleteDir(String cosFolderPath) {
		String bucketName = headImgBucket + "-" + appId;
		COSClient cosClient = getCOSClient();
		boolean exist = cosClient.doesObjectExist(bucketName, cosFolderPath);
		if(exist){
			cosClient.deleteObject(bucketName, cosFolderPath);
		}
		cosClient.shutdown();
	}

	/**
	 * 删除文件
	 * @param cosFolderPath
	 * @return
	 */
	public static void deleteFile(String filePath) {
		String bucketName = headImgBucket + "-" + appId;
		COSClient cosClient = getCOSClient();
		cosClient.deleteObject(bucketName, filePath);
		cosClient.shutdown();
	}
	
	

	/**
	 * 操作是否成功
	 * 
	 * @param ret
	 * @return
	 */
	public static boolean isSuccess(PutObjectResult putObjectResult) {
		String etag = putObjectResult.getETag();
		if(!StringUtils.isEmpty(etag)){
			return true;
		}
		return false;
	}
	

}
