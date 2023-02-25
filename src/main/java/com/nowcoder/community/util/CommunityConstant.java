package com.nowcoder.community.util;

/**
 * @Projectname: community
 * @Filename: CommunityConstant
 * @Author: yunqi
 * @Date: 2023/2/24 15:37
 * @Description: TODO
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证过期时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登陆凭证过期时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;


}
