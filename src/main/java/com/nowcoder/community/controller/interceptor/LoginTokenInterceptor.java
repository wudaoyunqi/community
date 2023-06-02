package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @Projectname: community
 * @Filename: LoginTicketInterceptor
 * @Author: yunqi
 * @Date: 2023/2/25 14:00
 * @Description: TODO
 */
@Component
public class LoginTokenInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostholder;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    //    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 从cookie中获取凭证
//        String ticket = CookieUtil.getValue(request, "ticket");
//        if (ticket != null) {
//            // 从Redis中获取登录凭证（旧版是访问数据库中的login_ticket表得到token）
//            LoginTicket loginTicket = userService.getLoginTicket(ticket);
//            // 检查凭证是否有效
//            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
//                // 根据凭证获取用户
//                User user = userService.getUserById(loginTicket.getUserId());
//                // 在本次请求中持有用户（从发起请求到请求处理完毕，对应的线程一直都存在，所以可以直接获取和设置）
//                hostholder.setUser(user);
//
//                // 构建用户认证结果，并存入SecurityContext，以便于Security进行授权
//                Authentication authentication = new UsernamePasswordAuthenticationToken(
//                        user, user.getPassword(), userService.getAuthorities(user.getId()));
////                SecurityContext context = SecurityContextHolder.createEmptyContext();
////                context.setAuthentication(authentication);
////                SecurityContextHolder.setContext(context);
//                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
//            }
//        }
//        return true;
//    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String token = CookieUtil.getValue(request, "token");
        if (token != null) {
            // 从Redis中获取token对应的用户Id
            Integer userId = (Integer) redisTemplate.opsForValue().get(RedisKeyUtil.getTokenKey(token));
            // 检查token是否有效
            if (userId != null) {
                // token有效，说明用户处于登录状态，根据userId从Redis中获取具体的用户对象
                User user = userService.getUserById(userId);
                // 在本次请求中持有用户（从发起请求到请求处理完毕，对应的线程一直都存在，所以可以直接获取和设置）
                hostholder.setUser(user);

                // 构建用户认证结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

                // 刷新token的有效期
                redisTemplate.expire(RedisKeyUtil.getTokenKey(token), 60L, TimeUnit.MINUTES);
            }
        }
        return true;
    }


    // 模板引擎需要使用用户信息，所以在调用模板引擎之前就应该将user存到model里
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostholder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    // 调用模板引擎之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostholder.clear();
//        SecurityContextHolder.clearContext();
    }
}
