package org.llw.com.exception;

public class ErrorCode {
	public static final String MESSAGE_KEY="message.";
	/**未登录系统**/
	public static final int DEFAULT_ERROR=0;
	
	
	/** 用户未登录*/
	public static int USER_NOT_LOGIN_99 = 99;
	/** 用户未实名*/
	public static int USER_NOT_IDENTIFY_98 = 98;

	/**--------------------文件读取错误  100--------------------------------**/
	/** 数据库连接失败*/
	public static int ERROR_DATABASE_ACCESS_100 = 100;
	
	/** properties 文件读取失败*/
	public static final int PROPERTY_FILE_READ_ERROR = 101;
	
	/** XML 文件读取失败*/
	public static final int XML_FILE_READ_FAILD = 102;
	
	/** Cache 读取失败*/
	public static final int CACHE_GET_FAILD = 103;
	
	/** 文件上传 内存溢出*/
	public static final int FILE_UPLOAD_FAILD_OUT_MEMORY = 104;
	/** 文件上传 文件类型不正确*/
	public static final int FILE_UPLOAD_FAILD_FILE_TYPE = 105;
	/** 文件上传  文件里没有内容*/
	public static final int FILE_UPLOAD_FAILD_FILE_EMPTY = 106;
	
	
	/**远程请求失败*/
	public static int ERROR_REMOTE_CALL_200 = 200;
	
	/**远程请求返回数据示知*/
	public static int ERROR_REMOTE_CALL_201 = 201;
}
