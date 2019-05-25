package org.xxjr.lock.util;


import org.llw.model.cache.RedisUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReentRedisLock implements RedisLock, java.io.Serializable  {

	private static final long serialVersionUID = 8320877572015943790L;
	/** 默认缓存 30秒*/
	public  int time_out_default = 30;
	/** 等待时，最多循环几次获取锁，每次等待是1s*/
	public static final int def_acquireTimeout = 40;
	/** redis key 的失效时间*/
	public static final int def_expireTime = 30;
	/**
	 * 获取锁，如果锁已存在，抛异常 InterRedisLockException
	 * @param lockKey redis 锁住的key
	 */
	@Override
	public void lock(String lockKey, String value) {
		boolean flag = RedisUtils.getRedisService().setnx(lockKey, value,time_out_default);
		if(flag){
			return;
		}
		 throw new InterRedisLockException("此锁被占用：" + lockKey);
	}


	/**
	 * 获取到锁 返回 true ，没获取到返回 false
	 * @param key redis 锁住的key
	 * @param value 设置的值
	 */

	@Override
	public boolean tryLock(String key,String value)
		throws InterRedisLockException {
       return tryLock(key, value, def_acquireTimeout, def_expireTime);
	}
	
	/**
	 * 获取到锁 返回 true ，没获取到返回 false
	 * @param key redis 锁住的key
	 * @param value 设置的值
	 * @param acquireTimeout 循环次数
	 * @param expireTime redis值的过期时间
	 */

	@Override
	public boolean tryLock(String key,String value, int acquireTimeout, int expireTime)
		throws InterRedisLockException {
        try {
            // 获取锁的超时时间，超过这个时间则放弃获取锁
            int countDown = 0;
            while (countDown < acquireTimeout) {
                if (RedisUtils.getRedisService().setnx(key, value,expireTime)) {
                    return true;//获取成功
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                countDown ++ ;
            }
        } catch (Exception e) {
            log.error(" try Lock:", e);
        }
        return false;
	}
	/**
	 * 释放锁
	 * @param lockKey redis 锁住的key
	 */
	@Override
	public void unlock(String lockKey,String value) {
		RedisUtils.getRedisService().delByLua(lockKey, value);
	}
	
	/**
	 * 设置超时时间 单位:秒
	 * @param outTime
	 */
	@Override
	public void setTimeOut(int outTime){
		time_out_default = outTime;
	}
}
