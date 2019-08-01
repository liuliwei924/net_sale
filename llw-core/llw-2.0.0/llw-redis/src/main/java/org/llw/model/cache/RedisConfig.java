package org.llw.model.cache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * Redis 配置类
 *
 * @author liulw
 * @version 2018/6/17 17:46
 */
// @ConditionalOnExpression 动态配置此配置

@ConditionalOnExpression("${spring.redis.enable}")
@Configuration
public class RedisConfig {

    @Bean(name="redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory ) {
        //设置序列化  
    	RedisSerializer<?> defaultSerializer =  new JdkSerializationRedisSerializer(this.getClass().getClassLoader());

        //配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
       
    	redisTemplate.setConnectionFactory(factory);
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);//key序列化
        redisTemplate.setValueSerializer(defaultSerializer);//value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);//Hash key序列化
        redisTemplate.setHashValueSerializer(defaultSerializer);//Hash value序列化
    	redisTemplate.setDefaultSerializer(defaultSerializer);
    	redisTemplate.setEnableDefaultSerializer(false);
    	
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
