package com.nowcoder.community.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Projectname: community
 * @Filename: CaffeineConfig
 * @Author: yunqi
 * @Date: 2023/3/31 22:28
 * @Description: TODO
 */

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(128)                                //初始缓存大小
                .maximumSize(1024)                                   //缓存的最大数量，设置这个值可以避免出现内存溢出
                .expireAfterWrite(180, TimeUnit.SECONDS)    //缓存的过期时间，是最后一次写操作后的一个时间
                .build();
    }

}
