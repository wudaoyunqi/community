package com.nowcoder.community.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Projectname: community
 * @Filename: RedisCellRateLimiter
 * @Author: yunqi
 * @Date: 2023/4/26 12:57
 * @Description: TODO
 */

@Component
public class RedisCellRateLimiter {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
                    "local init_burst = tonumber(ARGC[1])\n" +    // 桶中水滴的初始数量
                    "local max_burst = tonumber(ARGV[2])\n" +     // 每段时间内能产生的水滴数量
                    "local period = tonumber(ARGV[3])\n" +        // 时间段的长度
                    "local quote = ARGV[4]\n" +                   //
                    "return redis.call('CL.THROTTLE', key, init_burst, max_burst, period, quote)";


    public boolean tryAcquire(String key, int initBurst, int maxCapacity, int period, int quote) {
        return false;
    }
}
