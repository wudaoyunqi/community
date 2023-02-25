package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Projectname: community
 * @Filename: CookieUtil
 * @Author: yunqi
 * @Date: 2023/2/25 14:02
 * @Description: TODO
 */
public class CookieUtil {

    public static String getValue(HttpServletRequest request, String key) {
        if (request == null || key == null) {
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
