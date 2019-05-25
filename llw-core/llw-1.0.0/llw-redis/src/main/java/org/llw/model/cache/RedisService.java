package org.llw.model.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.ddq.common.util.LogerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import redis.clients.jedis.JedisCluster;
import redis.clients.util.SafeEncoder;


/**
 * Created by llw on 2015/1/27.
 */
@Component
@Configuration
@Slf4j
public class RedisService {
	
	@Autowired
	private JedisCluster jedisCluster;
	
	/***
	 * 发送消息
	 * @param cannel 消息关键字
	 * @param message 消息内容
	 */
	public void sendMessage(String channel, Object message) {
		jedisCluster.publish(SerializeUtil.serialize(channel), SerializeUtil.serialize(message));
	
	}
	
	/** 
     * 压栈 
     *  
     * @param key 
     * @param value 
     * @return 
     */  
    public Long push(String key, Object value) {
    	return jedisCluster.lpush(SerializeUtil.serialize(key), SerializeUtil.serialize(value));
    }  
    
    /** 
     * 出栈 
     *  
     * @param key 
     * @return 
     */  
    public Serializable pop(String key) {  
    	byte[] rt = jedisCluster.rpop(key.getBytes());
   	 	return SerializeUtil.serialize(rt);
    }  
  
    /**
     *  unit 不起作用，都是秒
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public Serializable blpop(String key, int timeout, TimeUnit unit) {
    	List<byte[]> brPop = jedisCluster.brpop(timeout, SerializeUtil.serialize(key));
      	 return (CollectionUtils.isEmpty(brPop) ? null : 
      		 (Serializable) SerializeUtil.unserialize((byte[])brPop.get(1)));
    }  
    
    /** 
     * 出栈 
     *  
     * @param key 
     * @return 
     */  
    public Serializable blpop(String key, int timeout) {
    	List<byte[]> brPop = jedisCluster.brpop(timeout, SerializeUtil.serialize(key));
      	 return (CollectionUtils.isEmpty(brPop) ? null : 
      		 (Serializable) SerializeUtil.unserialize((byte[])brPop.get(1)));
    }  
	/**
	 * 将数据保存到缓存中
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Integer set(final String key, final Object value) {
		byte[]  valueBytes =null;
		try {
			valueBytes =  SerializeUtil.serialize(value);
			jedisCluster.set(SerializeUtil.serialize(key), valueBytes);
		} catch (Exception e) {
			valueBytes = null;
			LogerUtil.error(RedisService.class,"redis set error");
		}
		return valueBytes==null? 0:valueBytes.length;
	}
	
	/**
	 * 将数据保存到缓存中
	 *
	 * @param key
	 * @param value
	 * @param seconds 保留时长
	 * @return
	 */
	public Integer set(final String key, final Object value,final int seconds) {
		byte[]  valueBytes =null;
		try {
			valueBytes =  SerializeUtil.serialize(value);
			jedisCluster.setex(SerializeUtil.serialize(key), seconds, valueBytes);
		} catch (Exception e) {
			valueBytes = null;
			LogerUtil.error(RedisService.class,"redis set error");
		}
		return valueBytes==null? 0:valueBytes.length;
	}
	/***
	 * 删除缓存
	 * @param key
	 * @return
	 */
	public boolean del(final String key) {
		Long count = null;
		try {
			count = jedisCluster.del(SerializeUtil.serialize(key));
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis del error");
		}
		return (null != count) && (count.longValue() == 1L);
	}
	/***
	 * 从缓存获取
	 * @param key
	 * @return
	 */
	public Serializable get(final String key) {
		byte[] value = null;
		try {
			value = jedisCluster.get(SerializeUtil.serialize(key));
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis get error");
		}
		return value == null ? null : (Serializable) SerializeUtil.unserialize(value);
	}
	
	
	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

	public void setJedisCluster(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	
	public boolean setnx(final String key, final String value,final int seconds) {
		String result = this.jedisCluster.set(SerializeUtil.serialize(key), SerializeUtil.serialize(value),
				SafeEncoder.encode("NX"),SafeEncoder.encode("EX"),seconds);
		return (result != null && result.equalsIgnoreCase("OK"));
	}

	
	public boolean delByLua(String key, String value) {
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		Object result = jedisCluster.eval(SafeEncoder.encode(script),1, SerializeUtil.serialize(key), SerializeUtil.serialize(value));
		//	Object result = jedisCluster.eval(SafeEncoder.encode(script), Collections.singletonList(SerializeUtil.serialize(key)), Collections.singletonList(SerializeUtil.serialize(value)));
		return Objects.equals(1L, result);
	}
	
	public boolean delByLua(String key) {
		String script = "return redis.call('del', KEYS[1])";
		Object result = jedisCluster.eval(SafeEncoder.encode(script),1,SerializeUtil.serialize(key));
		return Objects.equals(1L, result);
	}

	/***
	 * 缓存值加1
	 * @param key
	 * @return
	 */
	public long inc(final String key) {
		Long count = null;
		try {
			count = jedisCluster.incr(SerializeUtil.serialize(key));
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis inc error");
		}
		return count;
	}
	
	/**
	 * 将数据保存到缓存中hgetAll
	 *
	 * @param key
	 * @return
	 */
	public Map<String,Object> hgetAll(final String key) {
		Map<String,Object> value = new HashMap<String, Object>();
		try {
			Map<byte[],byte[]> valueTmp = jedisCluster.hgetAll(SerializeUtil.serialize(key));
			
			for(Map.Entry<byte[],byte[]> entry : valueTmp.entrySet()){
				
				value.put((String)SerializeUtil.unserialize(entry.getKey()), SerializeUtil.unserialize(entry.getValue()));
			}
			
			valueTmp = null;
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hgetAll error");
		}
		
		return value;
	}
	
	/**
	 * 将数据保存到缓存中hgetAll
	 *
	 * @param key
	 * @return
	 */
	public List<?> hgetAllToList(final String key) {
		List<Object> list = null;
		try {
			Map<byte[],byte[]> valueTmp = jedisCluster.hgetAll(SerializeUtil.serialize(key));
			if(valueTmp != null && !valueTmp.isEmpty()){
				list = new ArrayList<Object>();
				for(Map.Entry<byte[],byte[]> entry : valueTmp.entrySet()){
					list.add(SerializeUtil.unserialize(entry.getValue()));
				}
			}
			valueTmp = null;
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hgetAll error");
		}
		
		return list;
	}
	
	/**
	 * 将数据保存到缓存中hget
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Object hget(final String key, final String field) {
		try {
			byte[] valueTmp = jedisCluster.hget(SerializeUtil.serialize(key), SerializeUtil.serialize(field));
			
			return SerializeUtil.unserialize(valueTmp);
			
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hget error");
		}
		
		return null;
	}
	
	
	public Long hdel(final String key, final String field) {
		try {
			return jedisCluster.hdel(SerializeUtil.serialize(key), SerializeUtil.serialize(field));
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hdel error");
		}
		
		return 0L;
	}
	
	/**
	 * 将数据保存到缓存中hset
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Long hset(final String key, final String field, final Object value) {
		return hset(key, field,value,0);
	}
	
	/**
	 * 将数据保存到缓存中hset
	 *
	 * @param key
	 * @param value
	 * @param seconds 保留时长
	 * @return 1- success
	 */
	public Long hset(final String key, final String field, final Object value,final int seconds) {
		Long  len =0L;
		try {
			byte[] sKey = SerializeUtil.serialize(key);
			
			len = jedisCluster.hset(sKey, SerializeUtil.serialize(field), SerializeUtil.serialize(value));
			
			if(seconds > 0){
				jedisCluster.expire(sKey, seconds);
			}
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hset error");
		}
		return len;
	}
	
	/**
	 * 将数据保存到缓存中hset
	 *
	 * @param key
	 * @param value
	 * @param seconds 保留时长
	 * @return OK
	 */
	public String hmset(final String key, final Map<String,Object> value,final int seconds) {
		String  len = null;
		Map<byte[],byte[]> valueMap = new HashMap<byte[], byte[]>();
		try {
			for(Map.Entry<String,Object> entry : value.entrySet()){
				valueMap.put(SerializeUtil.serialize(entry.getKey()), SerializeUtil.serialize(entry.getValue()));
			}
			if(valueMap != null && !valueMap.isEmpty()){
				len = jedisCluster.hmset(SerializeUtil.serialize(key),valueMap);
				if(seconds > 0){
					jedisCluster.expire(SerializeUtil.serialize(key), seconds);
				}
			}
			
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hmset error");
		}
		return len;
	}
	
	/**
	 * 获取key存储数据的长度   hlen
	 *
	 * @param key
	 */
	public Long hlen(final String key) {
		Long  len = 0L;
		try {
			len = jedisCluster.hlen(SerializeUtil.serialize(key));
		} catch (Exception e) {
			LogerUtil.error(RedisService.class,"redis hlen error");
		}
		return len;
	}
}
