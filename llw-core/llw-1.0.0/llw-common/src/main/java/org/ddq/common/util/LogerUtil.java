package org.ddq.common.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogerUtil {
	
	
	public static void out(String... strs) {
		for (String str : strs) {
			System.out.println(str);
		}
	}

	public static void out(Object... objs) {
		for (Object obj : objs) {
			System.out.println(obj);
		}
	}
	
	//-----------------
	public static void log(String... strs) {
		_log(Thread.currentThread().getName(), strs);
	}

	public static void log(Object... objs) {
		_log(Thread.currentThread().getName(), objs);
	}

	public static void log(Class<?> clz, String... strs) {
		_log(clz.getName(), strs);
	}

	public static void log(Class<?> clz, Object... objs) {
		_log(clz.getName(), objs);
	}
	

	public static void error(Class<?> clz, String... strs) {
		_error(clz.getName(), strs);
	}

	public static void error(Class<?> clz, Throwable thr, String... strs) {
		_error(clz.getName(), thr, strs);
	}
	
	public static void error(Class<?> clz, Throwable thr, Object... objs) {
		_error(clz.getName(), thr, objs);
	}
	
	
	//-----------------
	
	
	public static void debug(String... strs) {
		_debug(Thread.currentThread().getName(), strs);
	}


	public static void debug(Throwable thr, String... strs) {
		_debug(Thread.currentThread().getName(), thr, strs);
	}

	public static void debug(Object... objs) {
		_debug(Thread.currentThread().getName(), objs);
	}

	public static void debug(Throwable thr, Object... objs) {
		_debug(Thread.currentThread().getName(), thr, objs);
	}

	public static void debug(Class<?> clz, String... strs) {
		_debug(clz.getName(), strs);
	}

	public static void debug(Class<?> clz, Throwable thr, String... strs) {
		_debug(clz.getName(), thr, strs);
	}

	public static void debug(Class<?> clz, Object... objs) {
		_debug(clz.getName(), objs);
	}

	public static void debug(Class<?> clz, Throwable thr, Object... objs) {
		_debug(clz.getName(), thr, objs);
	}
	
	
//--------------------
	
	public static void warn(String... strs) {
		_warn(Thread.currentThread().getName(), strs);
	}


	public static void warn(Throwable thr, String... strs) {
		_warn(Thread.currentThread().getName(), thr, strs);
	}

	public static void warn(Object... objs) {
		_warn(Thread.currentThread().getName(), objs);
	}

	public static void warn(Throwable thr, Object... objs) {
		_warn(Thread.currentThread().getName(), thr, objs);
	}

	public static void warn(Class<?> clz, String... strs) {
		_warn(clz.getName(), strs);
	}

	public static void warn(Class<?> clz, Throwable thr, String... strs) {
		_warn(clz.getName(), thr, strs);
	}

	public static void warn(Class<?> clz, Object... objs) {
		_warn(clz.getName(), objs);
	}

	public static void warn(Class<?> clz, Throwable thr, Object... objs) {
		_warn(clz.getName(), thr, objs);
	}
	
	
	
	//---------------
	
	private static void _log(String clzName, String... strs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isInfoEnabled()){
			for (String str : strs) {
				logger.info(str);
			}
		}
	}

	private static void _log(String clzName, Object... objs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isInfoEnabled()){
			for (Object obj : objs) {
				logger.info(obj != null ? obj.toString() : "null");
			}
		}
	}
	
	//-----------------

	private static void _error(String clzName, String... strs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isErrorEnabled()){
			for (String str : strs) {
				logger.error(str);
			}
		}
	}

	private static void _error(String clzName, Throwable thr, String... strs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isErrorEnabled()){
			for (String str : strs) {
				logger.error(str, thr);
			}
		}
	}


	private static void _error(String clzName, Throwable thr, Object... objs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isErrorEnabled()){
			for (Object obj : objs) {
				logger.error(obj != null ? obj.toString() : "null", thr);
			}
		}
	}
	
	//---------------
	
	private static void _debug(String clzName, String... strs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isDebugEnabled()){
			for (String str : strs) {
				logger.debug(str);
			}
		}
	}

	private static void _debug(String clzName, Throwable thr, String... strs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isDebugEnabled()){
			for (String str : strs) {
				logger.debug(str, thr);
			}
		}
	}

	private static void _debug(String clzName, Object... objs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isDebugEnabled()){
			for (Object obj : objs) {
				logger.debug(obj != null ? obj.toString() : "null");
			}
		}
	}

	private static void _debug(String clzName, Throwable thr, Object... objs) {
		Logger logger = LogManager.getLogger(clzName);
		if (logger.isDebugEnabled()){
			for (Object obj : objs) {
				logger.debug(obj != null ? obj.toString() : "null", thr);
			}
		}
	}
	
	
	//----------------
	
	
		private static void _warn(String clzName, String... strs) {
			Logger logger = LogManager.getLogger(clzName);
			if (logger.isWarnEnabled()){
				for (String str : strs) {
					logger.warn(str);
				}
			}
		}

		private static void _warn(String clzName, Throwable thr, String... strs) {
			Logger logger = LogManager.getLogger(clzName);
			if (logger.isWarnEnabled()){
				for (String str : strs) {
					logger.warn(str, thr);
				}
			}
		}

		private static void _warn(String clzName, Object... objs) {
			Logger logger = LogManager.getLogger(clzName);
			if (logger.isWarnEnabled()){
				for (Object obj : objs) {
					logger.warn(obj != null ? obj.toString() : "null");
				}
			}
		}

		private static void _warn(String clzName, Throwable thr, Object... objs) {
			Logger logger = LogManager.getLogger(clzName);
			if (logger.isWarnEnabled()){
				for (Object obj : objs) {
					logger.warn(obj != null ? obj.toString() : "null", thr);
				}
			}
		}
	
//------------------	

	public static boolean isInfoEnabled() {
		return getLogger().isInfoEnabled();
	}
	
	public static boolean isInfoEnabled(Class<?> clz) {
		return getLogger(clz).isInfoEnabled();
	}
	
	public static boolean isErrorEnabled() {
		return getLogger().isErrorEnabled();
	}
	
	public static boolean isErrorEnabled(Class<?> clz) {
		return getLogger(clz).isErrorEnabled();
	}
	
	public static boolean isDebugEnabled() {
		return getLogger().isDebugEnabled();
	}
	
	public static boolean isDebugEnabled(Class<?> clz) {
		return getLogger(clz).isDebugEnabled();
	}
	
	public static boolean isWarnEnabled() {
		return getLogger().isWarnEnabled();
	}
	
	public static boolean isWarnEnabled(Class<?> clz) {
		return getLogger(clz).isWarnEnabled();
	}
	
	
	//-------------------
	
	public static Logger getLogger(){
		Logger logger = LogManager.getLogger(Thread.currentThread().getName());
		return logger;
	}
	
	public static Logger getLogger(String clzName){
		Logger logger = LogManager.getLogger(clzName);
		return logger;
	}
	
	public static Logger getLogger(Class<?> clz){
		Logger logger = LogManager.getLogger(clz);
		return logger;
	}
}
