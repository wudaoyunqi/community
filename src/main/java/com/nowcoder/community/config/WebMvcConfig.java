package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Projectname: community
 * @Filename: WebMvcConfig
 * @Author: yunqi
 * @Date: 2023/2/25 13:36
 * @Description: TODO
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTokenInterceptor loginTokenInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // security已经帮忙做了登录权限管理，所以这里就只是对刷新token做一下处理
        registry.addInterceptor(loginTokenInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg"/*,
                        "/discuss/detail/**",
                        "/followees/**",
                        "/followers/**",
                        "/index",
                        "/error",
                        "/denied",
                        "/register",
                        "/login",
                        "/activation/**",
                        "/kaptcha",
                        "/search", "/share/**", "/profile/**"*/);

//         在所有路径都生效
//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        // 在所有路径都生效
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        // 在所有路径都生效
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }


    /**
     * 实例化拦截器，使其可以注入redis（因为拦截器是在Bean实例化之前执行的）
     * 不对不对，拦截器被注册为spring容器了，所以在容器里可以直接注入redistemplate，黑马点评有不同的处理
     *
     * @return
     */
//    @Bean
//    public LoginTokenInterceptor getLoginTokenInterceptor() {
//        return new LoginTokenInterceptor();
//    }
}
