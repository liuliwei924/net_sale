package org.llw.model.cache;

import org.ddq.common.core.SpringAppContext;


public class RedisUtils {

	private static volatile RedisService redisService = null;
	
	public static RedisService getRedisService(){
		if( redisService == null ){
			redisService = (RedisService) SpringAppContext.getBean("redisService");
		}
		return redisService;
	}
}
