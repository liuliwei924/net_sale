package org.llw.model.cache;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * redis 实现的分布式锁
 * @author liulw 2019-07-06
 *
 */
public class RedisLock {
	
	private RedisTempleteService redisSer = RedisUtils.getRedisService();
	
	private StringRedisTemplate sRedisTemplate = redisSer.getStringRedisTemplate();
	
	//private RedisTemplate<String,Object> redisTempLate = redisSer.getRedisTemplate();

	private static final String delScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
	
	private static final String setNXScript = "if redis.call('setnx',KEYS[1],ARGV[1])==1 then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end";
	//无设置失效时间
	private static final String setNXScript1 = "if redis.call('setnx',KEYS[1],ARGV[1])==1 then return 1 else return 0 end";
	
	private static RedisScript<Long> delRedisScript = new DefaultRedisScript<>(delScript, Long.class);
	
	private static RedisScript<Long> setNXRedisScript = new DefaultRedisScript<>(setNXScript, Long.class);
	
	private static RedisScript<Long> setNXRedisScript1 = new DefaultRedisScript<>(setNXScript1, Long.class);
	
	private final Long SUCCESS = 1L;

	/**
     * 加锁
     * @param key 键值
     * @param value 当前线程操作时的 System.currentTimeMillis() + 2000，2000是超时时间，这个地方不需要去设置redis的expire，
     *              也不需要超时后手动去删除该key，因为会存在并发的线程都会去删除，造成上一个锁失效，结果都获得锁去执行，并发操作失败了就。
     * @return true 获取到锁，false 没获取到
     */
    public boolean lock(String key, long value) {
       return lock(key,value,-1);
    }
    
	/**
     * 加锁
     * @param key 键值
     * @param value 当前线程操作时的 System.currentTimeMillis() + 2000，2000是超时时间，这个地方不需要去设置redis的expire，
     *              也不需要超时后手动去删除该key，因为会存在并发的线程都会去删除，造成上一个锁失效，结果都获得锁去执行，并发操作失败了就。
     * @param expireTime 单位秒
     * @return true 获取到锁，false 没获取到
     */
    public boolean lock(String key, long value,int expireTime) {
    	 try{
    		 Object result = -1L;
          	if(expireTime != -1) {
          		result = sRedisTemplate.execute(setNXRedisScript, Collections.singletonList(key),value+"",expireTime+"");
          	}else {
          		result = sRedisTemplate.execute(setNXRedisScript1, Collections.singletonList(key),value+"");
          	}
              
           if(SUCCESS.equals(result)) {
              return true;
           }

         }catch(Exception e){
            e.printStackTrace();
         }

         return false;
    }

    /** 无设置失效时间
     * 获取分布式锁
     * @param key 锁
     * @param value 请求标识
     * @param waitTimeout 单位秒
     * @return 是否获取成功
     */
    public boolean tryLock(String key, long value,int waitTimeout) {
    	return tryLock(key,value,-1,waitTimeout);
    }
    /**
     * 获取分布式锁
     * @param key 锁
     * @param value 请求标识
     * @param expireTime 单位秒
     * @param waitTimeout 单位秒
     * @return 是否获取成功
     */
    public boolean tryLock(String key, long value,int expireTime,int waitTimeout) {
        long nanoTime = System.nanoTime(); // 当前时间
        waitTimeout = waitTimeout*1000;
        try{
            do{
            	Object result = -1L;
            	if(expireTime != -1) {
            		result = sRedisTemplate.execute(setNXRedisScript, Collections.singletonList(key),value,expireTime+"");
            	}else {
            		result = sRedisTemplate.execute(setNXRedisScript1, Collections.singletonList(key),value+"");
            	}
                
                if(SUCCESS.equals(result)) {
                    return true;
                }

                Thread.sleep(500L);//休眠500毫秒
            }while ((System.nanoTime() - nanoTime) < TimeUnit.MILLISECONDS.toNanos(waitTimeout));

        }catch(Exception e){
           e.printStackTrace();
        }

        return false;
    }
    
    /**
     * 解锁
     * @param key
     * @param value
     */
    public boolean unlock(String key, long value) {
     
    	Object result = sRedisTemplate.execute(delRedisScript, Collections.singletonList(key),value+"");
       
        return SUCCESS.equals(result);
   
    }
}
