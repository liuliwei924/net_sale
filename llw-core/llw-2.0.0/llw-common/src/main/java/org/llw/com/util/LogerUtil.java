package org.llw.com.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogerUtil {
	
	public static void debug(String format,Object ...arguments){
		debug(Thread.currentThread().getName(),format,arguments);
	}

	public static void debug(Class<?> clz,String format,Object ...arguments){
		debug(clz.getName(),format,arguments);
	}
	
	public static void debug(String clzName,String format,Object ...arguments) {
		Logger logger = LoggerFactory.getLogger(clzName);
		if(logger.isDebugEnabled()) {
			logger.debug(format, arguments);
		}
	}
	
	public static void warn(String format,Object ...arguments){
		warn(Thread.currentThread().getName(),format,arguments);
	}

	public static void warn(Class<?> clz,String format,Object ...arguments){
		warn(clz.getName(),format,arguments);
	}
	
	public static void warn(String clzName,String format,Object ...arguments) {
		Logger logger = LoggerFactory.getLogger(clzName);
		if(logger.isWarnEnabled()) {
			logger.warn(format, arguments);
		}
	}
	
	public static void info(String format,Object ...arguments){
		info(Thread.currentThread().getName(),format,arguments);
	}

	public static void info(Class<?> clz,String format,Object ...arguments){
		info(clz.getName(),format,arguments);
	}
	
	public static void info(String clzName,String format,Object ...arguments) {
		Logger logger = LoggerFactory.getLogger(clzName);
		if(logger.isInfoEnabled()) {
			logger.debug(format, arguments);
		}
	}
	
	public static void error(String str,Throwable th){
		error(Thread.currentThread().getName(),str,th);
	}
	
	public static void error(String clzName,String str,Throwable th){
		Logger logger = LoggerFactory.getLogger(clzName);
		if(logger.isErrorEnabled()) {
			logger.error(str, th);
		}
	}
	
	public static void error(String format,Object ...arguments){
		error(Thread.currentThread().getName(),format,arguments);
	}

	public static void error(Class<?> clz,String format,Object ...arguments){
		error(clz.getName(),format,arguments);
	}
	
	public static void error(String clzName,String format,Object ...arguments) {
		Logger logger = LoggerFactory.getLogger(clzName);
		if(logger.isErrorEnabled()) {
			logger.error(format, arguments);
		}
	}
	
	public static void main(String[] args) {
		LogerUtil.debug("我是ddd");
	}
}
