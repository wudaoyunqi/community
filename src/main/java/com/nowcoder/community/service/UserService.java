package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Projectname: community
 * @Filename: UserService
 * @Author: yunqi
 * @Date: 2023/2/23 17:36
 * @Description: TODO
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 1. 优先从缓存中取值
    private User getUserCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2. 取不到时初始化缓存数据
    // keypoint 后续做缓存穿透的处理
    private User initUserCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        // 设置key过期时间为半小时
        redisTemplate.opsForValue().set(redisKey, user, 30L, TimeUnit.MINUTES);
        return user;
    }

    // 3. 数据变更时清除缓存数据
    private void clearUserCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public User getUserById(int id) {
        User user = getUserCache(id);
        if (user == null) {
            user = initUserCache(id);
            System.out.println("用户" + id + "不在缓存，从数据表中重新拉取数据");
        }
        return user;
    }

    public User getUserByName(String username) {
        return userMapper.selectByName(username);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("用户不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该用户名已被注册！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);     // 普通用户
        user.setStatus(0);   // 未激活
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(500) + 300));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://127.0.0.1:8080/community/activation/101/activation_code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearUserCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        // 验证账号（必须走DB）
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        // 判断账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确");
            return map;
        }

        // 生成登录凭证（牛客官方代码——最终版，将loginTicket作为value的类，实际上数据库不再存在LoginTicket表）
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(user.getId());
//        loginTicket.setTicket(CommunityUtil.generateUUID());
//        loginTicket.setStatus(0);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        // 将登录凭证存储在Redis中（过期时间为30分钟）
        // 不设置过期了，因为后续有一个关于统计uv和dau的需求
        // 但黑马点评实现了uv和dau的功能，并且也将登录凭证过期了
//        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
//        redisTemplate.opsForValue().set(redisKey, loginTicket);


        // 生成登录凭证 <token, userId>
        String token = CommunityUtil.generateUUID();
        String redisKey = RedisKeyUtil.getTokenKey(token);
        redisTemplate.opsForValue().set(redisKey, user.getId());
        redisTemplate.expire(redisKey, expiredSeconds, TimeUnit.MINUTES);   // 设置过期时间1h

        map.put("token", token);

        return map;
    }


    //    public void logout(String token) {
////        loginTicketMapper.updateStatus(ticket, 1);
//        String redisKey = RedisKeyUtil.getTokenKey(token);
//        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
//        loginTicket.setStatus(1);
//        redisTemplate.opsForValue().set(redisKey, loginTicket);
//    }

    /**
     * keypoint 登出只需要删除token缓存，用户信息缓存不用删除，还能减少一次缓存未命中
     * @param token
     */
    public void logout(String token) {
        String redisKey = RedisKeyUtil.getTokenKey(token);
        redisTemplate.delete(redisKey);
    }


    //    /**
//     * 只被LoginTicketInterceptor调用
//     *
//     * @param ticket
//     * @return
//     */
//    public LoginTicket getLoginTicket(String ticket) {
////        return loginTicketMapper.selectByTicket(ticket);
//        String redisKey = RedisKeyUtil.getTokenKey(ticket);
//        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
//    }

    /**
     * 忘记密码——重置密码，已经输入了验证码并通过
     *
     * @param email
     * @param newPassword
     * @return
     */
    public Map<String, Object> resetPassword(String email, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 验证空值
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证邮箱是否已被注册(输入验证码只是验证邮箱是否有效)
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱未注册！");
            return map;
        }

        // 重置密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);

        return map;
    }

    /**
     * 更新头像
     * todo 应该实现用户未登出就能刷新头像的功能，但头像的获取是通过hostHolder存储的完整User对象，而hostHolder是从redis里拿到的user
     * todo 所以更新头像应该更新缓存？对！更新它！别怕！用户只可能修改自己的头像，不会出现并发问题，只会出现数据短期不一致的问题
     *
     * keypoint 对个鸡毛，在存储认证信息的时候只用存<token, userId>，用户的具体信息单独存<userId, userMap>，更改头像或者用户名的时候只需要删除<userId, userMap>
     *     这样下次请求来的时候再去访问数据库获得<userId, newUserMap>，同时<token, userId>还在，不会改变登录状态
     *
     * @param userId
     * @param newHeaderUrl
     * @return
     */
    public int updateHeader(int userId, String newHeaderUrl) {
        int row = userMapper.updateHeaderUrl(userId, newHeaderUrl);
        clearUserCache(userId);
        return row;
    }

    /**
     * 更新密码
     * 用户更新密码后，应该让用户重新登录，所以应该清除redis里存的登录用户信息，
     * 那么下次请求拦截器首先查询redis里token对应的用户信息，发现没有，就让用户重新登录
     * 但这里不用clearCache了，因为controller实现的逻辑是重定向回退出页，再重定向回首页，/logout已经实现了清除登录用户的缓存信息
     * 但是是否有必要更新数据库和删除缓存放一起呢？
     * keypoint 不对，redis里存的最好是非敏感信息，所以密码更新了不影响<userId, userMap>记录，不用删除
     *
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }

        // 验证原密码（hostHolder里只存了UserDTO，密码没有，所以需要访问数据库得到原来的密码）
        User user = userMapper.selectById(userId);
        if (!CommunityUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())) {  // 原密码正确
            map.put("oldPasswordMsg", "原密码不正确！");
            return map;
        }

        if (!CommunityUtil.md5(newPassword + user.getSalt()).equals(user.getPassword())) {  // 新旧密码相同
            map.put("newPasswordMsg", "新密码不能与原密码相同！");
            return map;
        }

        // 更新密码
        userMapper.updatePassword(userId, CommunityUtil.md5(newPassword + user.getSalt()));
        return map;
    }

    /**
     * 获得指定用户的权限
     *
     * @param userId
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.getUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 0:
                        return AUTHORITY_USER;
                    case 1:
                        return AUTHORITY_ADMIN;
                    default:
                        return AUTHORITY_MODERATOR;
                }
            }
        });
        return list;
    }


}
