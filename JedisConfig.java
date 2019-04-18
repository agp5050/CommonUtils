

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis连接配置.
 *
 * @Author: 
 * @CreateDate: 2018/10/28 23:10
 * @Version: 1.0
 */
@Configuration
public class JedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        RedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);//key序列化
        redisTemplate.setValueSerializer(genericSerializer);//value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);//Hash key序列化
        redisTemplate.setHashValueSerializer(genericSerializer);//Hash value序列化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;

    }

    @Bean
    public JedisPool jedisPool(JedisProps jedisProps) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setBlockWhenExhausted(jedisProps.isBlockWhenExhausted());
        return new JedisPool(config, jedisProps.getHost(), jedisProps.getPort(), 1000 * 2, jedisProps.getPassword(),jedisProps.getDb());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Component
    @ConfigurationProperties(prefix = "jedis")
    public static class JedisProps {
        String host;
        int port = 6379;
        int db = 1;
        long timeout = 1000;
        String password;
        boolean blockWhenExhausted = true;
    }

//    @Bean
//    public RedisCacheConfiguration redisCacheConfiguration(GenericJackson2JsonRedisSerializer serializer) {
//        return RedisCacheConfiguration
//                .defaultCacheConfig()
//                .serializeKeysWith(
//                        RedisSerializationContext
//                                .SerializationPair
//                                .fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(
//                        RedisSerializationContext
//                                .SerializationPair
//                                .fromSerializer(serializer));
//    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append("::" + method.getName() + ":");
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }
}
