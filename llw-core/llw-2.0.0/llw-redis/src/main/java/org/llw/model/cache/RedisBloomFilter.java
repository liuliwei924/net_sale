package org.llw.model.cache;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;

/**
 * Redis 布隆过滤器
 * @author liulw 2019-07-01
 *
 */
public class RedisBloomFilter {

	    //预计插入量
	    private long expectedInsertions = 1000;
	    //可接受的错误率
	    private double fpp = 0.001F;
	    
	    private String enCode = "UTF-8";
	    
	    //布隆过滤器的键在Redis中的前缀 利用它可以统计过滤器对Redis的使用情况
	    private String redisKeyPrefix = "bf:";
	    
	    //bit数组长度
	    private long numBits = 0L;
	    //hash函数数量
	    private int numHashFunctions = 0;

	    public RedisBloomFilter() {
	    	this.numBits = optimalNumOfBits(expectedInsertions, fpp);
	    	this.numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
	    };

	   /**
	    * 
	    * @param expectedInsertions 预计插入量
	    * @param fpp 允许的最大错误率
	    */
	   public RedisBloomFilter(long expectedInsertions,double fpp) {
		  this(expectedInsertions,fpp,"UTF-8");
	   }
	   
	   public RedisBloomFilter(long expectedInsertions,double fpp,String enCode) {
		   this.expectedInsertions = expectedInsertions;
		   this.fpp = fpp;
		   this.enCode = enCode;
		   
		   this.numBits = optimalNumOfBits(expectedInsertions, fpp);
	       this.numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
	   }
		   
	   public void setEnCode(String enCode) {
	        this.enCode = enCode;
	    }

	    public void setExpectedInsertions(long expectedInsertions) {
	        this.expectedInsertions = expectedInsertions;
	    }

	    public void setFpp(double fpp) {
	        this.fpp = fpp;
	    }

	    public void setRedisKeyPrefix(String redisKeyPrefix) {
	        this.redisKeyPrefix = redisKeyPrefix;
	    }
	    
	    /**
	     * 设置过期时间  单位 ：秒
	     * @param key
	     * @param second
	     */
	    public void setExpire(String key,int second) {
	        RedisUtils.getRedisService().expire(key, second);
	    }

	    /**
	     * 删除key
	     * @param key
	     */
	    public void delKey(String key) {
	        RedisUtils.getRedisService().del(key);
	    }
	    
	    //计算hash函数个数 方法来自guava
	    private int optimalNumOfHashFunctions(long n, long m) {
	        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
	    }

	    //计算bit数组长度 方法来自guava
	    private long optimalNumOfBits(long n, double p) {
	        if (p == 0) {
	            p = Double.MIN_VALUE;
	        }
	        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
	    }

	    /**
	     * 判断值是否存在
	     */
	    public boolean isExist(String key, String value) {
	    	long[] indexs = getIndexs(value);
	    	final String redisKey = getRedisKey(key);
	        List<Object> list = RedisUtils.getRedisService().getRedisTemplate().executePipelined(new RedisCallback<Boolean>() {
	  				@Override
	  				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
	  					for(Long offset : indexs) {
	  						try {
	  							connection.getBit(redisKey.getBytes(enCode), offset);
	  						} catch (UnsupportedEncodingException e) {
	  							e.printStackTrace();
	  						}
	  					}
	  					return null;
	  				}
	  			},RedisUtils.getRedisService().getRedisTemplate().getKeySerializer());
	       
	       boolean isExist = true;
	      
	       for(Object obj : list) {
	    	   Boolean bl = (Boolean)obj;
	    	   
	    	   if(!bl) {
	    		   isExist = false; break;
	    	   }
	       }
	      
	       return isExist;  
	    }

	    /**
	     * 
	     * @param key 键
	     * @param value 值
	     */
	    public void put(String key, String value) {
	        long[] indexs = getIndexs(value);
	        final String redisKey = getRedisKey(key);
	        RedisUtils.getRedisService().getRedisTemplate().executePipelined(new RedisCallback<Boolean>() {
				@Override
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					for(Long offset : indexs) {
						try {
							connection.setBit(redisKey.getBytes(enCode), offset, true);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			});
	        
	    }

	    /**
	     * 根据key获取bitmap下标 方法来自guava
	     */
	    private long[] getIndexs(String value) {
	        long hash1 = hash(value);
	        long hash2 = hash1 >>> 16;
	        long[] result = new long[numHashFunctions];
	        for (int i = 0; i < numHashFunctions; i++) {
	            long combinedHash = hash1 + i * hash2;
	            if (combinedHash < 0) {
	                combinedHash = ~combinedHash;
	            }
	            result[i] = combinedHash % numBits;
	        }
	        return result;
	    }

	    /**
	     * 获取一个hash值 方法来自guava
	     */
	    private long hash(String key) {
	        Charset charset = Charset.forName(enCode);
	        return Hashing.murmur3_128().hashObject(key, Funnels.stringFunnel(charset)).asLong();
	    }

	    private String getRedisKey(String where) {
	        return redisKeyPrefix + where;
	    }
	  
	    public static void main(String[] args) {
		//	RedisBloomFilter rbf = new RedisBloomFilter(Integer.MAX_VALUE,0.01D);
		//	System.out.println(rbf.ff());
		}
}
