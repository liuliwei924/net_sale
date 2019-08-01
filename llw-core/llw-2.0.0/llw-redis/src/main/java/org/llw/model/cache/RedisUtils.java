package org.llw.model.cache;

import org.llw.com.core.SpringAppContext;


public class RedisUtils {

	private static volatile RedisTempleteService redisService = null;
	
	public static RedisTempleteService getRedisService(){
		if(redisService == null) {
			redisService = SpringAppContext.getBean(RedisTempleteService.class);
		}
		return redisService;
	}
}
