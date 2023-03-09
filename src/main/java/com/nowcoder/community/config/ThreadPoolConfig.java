package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Projectname: community
 * @Filename: ThreadPoolConfig
 * @Author: yunqi
 * @Date: 2023/3/7 20:51
 * @Description: TODO
 */

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
