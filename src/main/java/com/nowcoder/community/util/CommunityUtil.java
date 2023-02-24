package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @Projectname: community
 * @Filename: CommunityUtil
 * @Author: yunqi
 * @Date: 2023/2/24 13:30
 * @Description: TODO
 */
public class CommunityUtil {

    // 生成随机字符串（激活码等）
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密(password + salt -> 加密结果)
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
