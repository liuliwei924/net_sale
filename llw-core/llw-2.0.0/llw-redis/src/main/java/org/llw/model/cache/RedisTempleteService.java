package org.llw.model.cache;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/**
 * spring boot 2.0 集成redis
 * @author liulw 2019-06-30
 *
 */
@Component
public class RedisTempleteService {

	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	public StringRedisTemplate getStringRedisTemplate() {
		return stringRedisTemplate;
	}
	
	public RedisTemplate<String,Object> getRedisTemplate() {
		return redisTemplate;
	}
	
	/**
	 * 是否有这个key
	 * @param key
	 */
	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}
	
	/**
	 * K-V 存值，永久存取
	 * @param key
	 * @param value
	 */
	public void set(String key,Object value) {
		redisTemplate.opsForValue().set(key, value);
	}
	
	/**
	 * K-V 存值，可以设置有效时间
	 * @param key
	 * @param value
	 * @param expireTime 有效时间(单位：秒)
	 */
	public void set(String key,Object value,int expireTime) {
		redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
	}
	
	/**
	 * K-V 存值，如果存在，返回false，不覆盖，没有，插入数据，返回true，永久存值
	 * @param key
	 * @param value
	 */
	public boolean setIfAbsent(String key,Object value) {
		return redisTemplate.opsForValue().setIfAbsent(key, value);
	}
	
	/**
	 * K-V 存值，如果存在，返回false，不覆盖，没有，插入数据，返回true，可以设置有效时间
	 * @param key
	 * @param value
	 * @param expireTime 有效时间(单位：秒)
	 */
	public boolean setIfAbsent(String key,Object value,int expireTime) {
		return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
	}
	
	/**
	 * K-V 存值，如果存在，返回true，不覆盖，没有，插入数据，返回false，永久存值
	 * @param key
	 * @param value
	 */
	public boolean setIfPresent(String key,Object value) {
		return redisTemplate.opsForValue().setIfPresent(key, value);
	}
	
	/**
	 * K-V 存值，如果存在，返回true，不覆盖，没有，插入数据，返回false，可以设置有效时间
	 * @param key
	 * @param value
	 * @param expireTime 有效时间(单位：秒)
	 */
	public boolean setIfPresent(String key,Object value,int expireTime) {
		return redisTemplate.opsForValue().setIfPresent(key, value, expireTime, TimeUnit.SECONDS);
	}
	
	/**
	 * K-V 存值，如果存在，获取旧值，不存在，更新新值为value，返回value
	 * @param key
	 * @param value
	 */
	public Object getAndSet(String key,Object value) {
		return redisTemplate.opsForValue().getAndSet(key, value);
	}
	
	/**
	 * 存二进制
	 * @param key 键
	 * @offset 偏移量  
	 * @param value true 因为二进制只有0，1 则 true 设置为1 false设置为0
	 * return  设置 成功 返回false，证明这偏移量之前没设置过 ，返回true，设置不成功
	 */
	public boolean setBit(String key,long offset,boolean value) {
		return redisTemplate.opsForValue().setBit(key, offset,value);
	}
	
	/**
	 * 存二进制
	 * @param key 键
	 * @offsets 偏移量集合 开启 Pipelined 执行，减少连接 
	 * @param value true 因为二进制只有0，1 则 true 设置为1 false设置为0
	 * return  设置 成功 返回false，证明这偏移量之前没设置过 ，返回true，设置不成功
	 */
	public void setBit(String key,Set<Long> offsets,boolean value) {
		redisTemplate.executePipelined(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				for(Long offset : offsets) {
					try {
						connection.setBit(key.getBytes("UTF-8"), offset, value);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}

	/**
	 * 获取值
	 * @param key
	 */
	public boolean getBit(String key,long offset) {
		return redisTemplate.opsForValue().getBit(key,offset);
	}
	
	/**
	 * 获取值
	 * @param key
	 */
	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}
	
	/**
	 * 获取值
	 * @param key
	 */
	public String get(String key,int start,int end) {
		return redisTemplate.opsForValue().get(key, start, end);
	}
	
	/**
	 * 尾部追加，只限操作字符串
	 * @param key
	 */
	public int append(String key,String value) {
		return redisTemplate.opsForValue().append(key, value);
	}

	/**
	 * value -1 操作
	 * @param key
	 * @param delta 步长
	 */
	public long decrement(String key,long delta) {
		return stringRedisTemplate.opsForValue().decrement(key, delta);
	}
	
	/**
	 * value +1 操作
	 * @param key
	 * @param delta 步长
	 */
	public long increment(String key,long delta) {
		return stringRedisTemplate.opsForValue().increment(key, delta);
	}
	
	/**
	 * 删除key
	 * @param key
	 * @param delta 步长
	 */
	public Boolean del(String key) {
		return redisTemplate.delete(key);
	}
	
	/**
	 * 删除多个key
	 * @param keys
	 */
	public Long delKeys(Set<String> keys) {
		return redisTemplate.delete(keys);
	}
	
	/**
	 * hash put
	 */
	public void hashPut(String key, Object hashKey,Object value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
	}
	
	/**
	 * hash put
	 */
	public void hashPut(String key, Object hashKey,Object value,int second){
		redisTemplate.opsForHash().put(key, hashKey, value);
		if(second > 0) {
			redisTemplate.expire(key, second, TimeUnit.SECONDS);
		}
	}
	
	/**
	 * hash put
	 */
	public void hashPuts(String key, Map<Object,Object> values,int second){
		redisTemplate.opsForHash().putAll(key, values);
		if(second > 0) {
			redisTemplate.expire(key, second, TimeUnit.SECONDS);
		}
		
	}
	
	/**
	 * hashMultiGet
	 */
	public void hashMultiGet(String key, Set<Object> hashKeys){
		redisTemplate.opsForHash().multiGet(key, hashKeys);
	}
	 
	/**
	 * hash put
	 */
	public Object hashGet(String key, Object hashKey) {
		return redisTemplate.opsForHash().get(key, hashKey);
	}
	
	/**
	 * hash delete
	 */
	public Object hashDel(String key,Object ...hashKeys ) {
		return redisTemplate.opsForHash().delete(key, hashKeys);
	}
	
	/**设置过去时间
	 * param expire
	 */
	public Object expire(String key,int second) {
		return redisTemplate.expire(key, second, TimeUnit.SECONDS);
	}
	
	/**set结构类型存值
	 */
	public Object addWithSet(String key,Object...values) {
		return redisTemplate.opsForSet().add(key, values);
	}
	
	/**set 集合中是否存在此值
	 */
	public Object isMemberWithSet(String key,Object value) {
		return redisTemplate.opsForSet().isMember(key, value);
	}
	
	/**set 获取成员
	 */
	public Set<Object> membersWithSet(String key) {
		return redisTemplate.opsForSet().members(key);
	}
	
	/**set集合的元素个数
	 */
	public long sizeWithSet(String key) {
		return redisTemplate.opsForSet().size(key);
	}
	
	/**list 从左边插入
	 */
	public long leftPushAll(String key,Object... values) {
		return redisTemplate.opsForList().leftPushAll(key, values);
	}
	
	/**list 从右边插入
	 */
	public long rightPushAll(String key,Object... values) {
		return redisTemplate.opsForList().rightPushAll(key, values);
	}
	
	/**list 从左边插入
	 */
	public long leftPushAll(String key,List<?> list) {
		return redisTemplate.opsForList().leftPushAll(key, list);
	}
	
	/**list 从右边插入
	 */
	public long rightPushAll(String key,List<?> list) {
		return redisTemplate.opsForList().rightPushAll(key, list);
	}
	
	/**list 从左边取出元素
	 */
	public Object leftPop(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}
	
	/**list 从右边取出元素
	 */
	public Object rightPop(String key) {
		return redisTemplate.opsForList().rightPop(key);
	}
	
	/**list 取出集合区间
	 */
	public List<?> rangeWith(String key,int start,int end) {
		return redisTemplate.opsForList().range(key, start, end);
	}
	
	/**获取list的size
	 */
	public long sizeWithList(String key,int start,int end) {
		return redisTemplate.opsForList().size(key);
	}
	
	/**取出指定区间的集合元素，并保留，多余的删除
	 */
	public void trimWithList(String key,int start,int end) {
		redisTemplate.opsForList().trim(key, start, end);
	}
	
	/**增加，value 是附带值，score是分数，用于排序
	 */
	public void addWithZSet(String key,Object value,double score) {
		redisTemplate.opsForZSet().add(key, value, score);
	}
	
	/** 统计分数区间的个数
	 */
	public Long countWithZSet(String key,double min,double max) {
		return redisTemplate.opsForZSet().count(key, min, max);
	}
	
	/** 增加，value 是集合「DefaultTypedTuple」，score是分数，用于排序
	 */
	public Long countWithZSet(String key,Set<TypedTuple<Object>> tuples) {
		return redisTemplate.opsForZSet().add(key, tuples);
	}
	
	/** 正序取值value，start 开始位置，索引从0开始  end 结束值，-1表示全部读取
	 */
	public Set<Object> rangeWithZSet(String key,long start,long end) {
		return redisTemplate.opsForZSet().range(key, start, end);
	}
	
	/** 反序取值VALUE，start 开始位置，索引从0开始  end 结束值，-1表示全部读取
	 */
	public Set<Object> reverseRangeWithZSet(String key,long start,long end) {
		return redisTemplate.opsForZSet().reverseRange(key, start, end);
	}
	
	/** 通过分数正向取值
	 */
	public Set<Object> rangeByScoreWithZSet(String key,double min,double max) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}
	
	/** 通过分数反向取值
	 */
	public Set<Object> reverseRangeWithZSet(String key,double min,double max) {
		return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
	}
	
	/** 通过分数反向取值
	 */
	public Set<Object> rangeByScoreWithZSet(String key,double min,double max,long offset,long count) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
	}
	
	/**正序 通过分数区间+偏移量读取，offset 相当于数据库的第几页，下标从0开始 count 相当于每一页条数
	 */
	public Set<TypedTuple<Object>> rangeByScoresWithZSet(String key,double min,double max,long offset,long count) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count);
	}
	
	/**反序 通过分数区间+偏移量读取，offset 相当于数据库的第几页，下标从0开始 count 相当于每一页条数
	 */
	public Set<TypedTuple<Object>> reverseRangeByScoresWithZSet(String key,double min,double max,long offset,long count) {
		return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count);
	}
	
	/**正序 通过分数区间+偏移量读取，offset 相当于数据库的第几页，下标从0开始 count 相当于每一页条数
	 */
	public Set<Object> rangeByScore(String key,double min,double max,long offset,long count) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
	}
	
	/**反序 通过分数区间+偏移量读取，offset 相当于数据库的第几页，下标从0开始 count 相当于每一页条数
	 */
	public Set<Object> reverseRangeByScoreWithZSet(String key,double min,double max,long offset,long count) {
		return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
	}
	
	/**HyperLogLog add
	 */
	public Long addWithHyperLogLog(String key,Object...values) {
		return  redisTemplate.opsForHyperLogLog().add(key, values);
	}
	
	/**HyperLogLog size  求每个key 的count， 多key 取得是并集数量
	 */
	public Long sizeWithHyperLogLog(String... keys) {
		return  redisTemplate.opsForHyperLogLog().size(keys);
	}
	
	/**HyperLogLog union，多key 并集在一个key 求总量
	 */
	public Long unionWithHyperLogLog(String destination ,String... sourceKeys) {
		return  redisTemplate.opsForHyperLogLog().union(destination, sourceKeys);
	}

	/**HyperLogLog delete
	 */
	public void delWithHyperLogLog(String key) {
		redisTemplate.opsForHyperLogLog().delete(key);
	}
	
	/** flushDB
	 */
	public void flushDB() {
		redisTemplate.execute(new RedisCallback<String>(){
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushDb();
				return "OK";
			}
		});
	}
	
	/** flushDB
	 */
	public void flushAll() {
		redisTemplate.execute(new RedisCallback<String>(){
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushAll();
				return "OK";
			}
		});
	}
	
	/** 事物操作 redisTemplate.setEnableTransactionSupport(true);设置事物支持
	 * MULTI    开启事务
		EXEC    执行任务队列里所有命令，并结束事务
		DISCARD     放弃事务，清空任务队列，全部不执行，并UNWATCH
		WATCH key [key1]    MULTI执行之前，指定监控某key，如果key发生修改，放弃整个事务执行
		UNWATCH    手动取消监控
	 */
}
