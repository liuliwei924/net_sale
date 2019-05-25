package org.xxjr.lock.util;


public interface RedisLock {

    void lock(String lockKey, String value);

	public boolean tryLock(String key,String value)
			throws InterRedisLockException;
	
    boolean tryLock(String key, String value, int acquireTimeout, int expireTime)
		   throws InterRedisLockException;
    
    void setTimeOut(int outTime);

	void unlock(String lockKey, String value);

}
