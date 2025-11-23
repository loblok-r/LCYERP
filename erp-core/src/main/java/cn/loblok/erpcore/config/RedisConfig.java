package cn.loblok.erpcore.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author 再来亿组手枪腿
 * @site
 * @company zenstar
 * @create 2025-03-01 12:37
 */

@Component
public class RedisConfig {

    String redisPrex = "redis://";

    @Value("${spring.data.redis.host}")
    String redisHost;
    @Value("${spring.data.redis.host}")
    String redisPort;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress(redisPrex+redisHost+":"+redisPort).setDatabase(0);
        return Redisson.create(config);
    }

}