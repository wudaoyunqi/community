package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Projectname: community
 * @Filename: LoginController
 * @Author: yunqi
 * @Date: 2023/2/24 13:06
 * @Description: TODO
 */
@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 访问localhost:8080/community/register地址，返回/site/register.html
     *
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 访问localhost:8080/community/login地址，返回/site/login.html
     *
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 访问localhost:8080/community/forget地址，返回/site/forget.html
     *
     * @return
     */
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }


    /**
     * 入参可以填@RequestParam("username") String username等，填User只要User的属性与form表单的input数据name对的上，SpringMVC就会自动注入值
     * register()和getRegisterPage()的路径相同，但二者的method不同所以可以被区分
     *
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，已经向您的邮箱发送一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");    // 设置跳转链接，即重定向回首页
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活账号
     *
     * @param model
     * @param userId
     * @param activationCode
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{activationCode}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode) {
        int result = userService.activation(userId, activationCode);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");    // 设置跳转链接，即重定向回登录页
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "该账号已被激活！");
            model.addAttribute("target", "/index");    // 设置跳转链接，即重定向回首页
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");    // 设置跳转链接，即重定向回首页
        }
        return "/site/operate-result";
    }

    /**
     * 将验证码存在session里 ---> 将验证码存在Redis里
     *
     * @param response //     * @param session
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码text存入session
//        session.setAttribute("kaptcha", text);

        // 验证码的归属者
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(120);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 120, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 从session里取出getKaptcha()已经生成的验证码text
     *
     * @param model
     * @param username
     * @param password
     * @param code       用户输入的验证码
     * @param rememberme //     * @param session
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code, boolean rememberme,
            /*HttpSession session,*/ HttpServletResponse response, @CookieValue(value = "kaptchaOwner", required = false) String kaptchaOwner) {
        // 检查验证码（旧版是从session中取出验证码的）
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        } else {
            model.addAttribute("codeMsg", "验证码已过期！");
            return "/site/login";
        }

        if (/*StringUtils.isBlank(kaptcha) ||*/ StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        // 检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("token")) {
            Cookie cookie = new Cookie("token", map.get("token").toString());
            cookie.setPath(contextPath);
            // 若设置了过期时间，浏览器就会把cookie写入硬盘里，关闭后再次打开浏览器，cookie仍然有效直到超过设定的过期时间
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";     // 登录成功，重定向回首页
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";         // 登录失败，跳转回登录页
        }

    }


    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("token") String token) {
        userService.logout(token);
        SecurityContextHolder.clearContext();
        return "redirect:/index";    // 重定向默认是get请求
    }

    /**
     * 获取邮箱验证码并发送
     *
     * @param email
     * @param session
     * @return
     */

    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        // 判断空值
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }

        // 发送携带验证码的邮件
        String verifyCode = CommunityUtil.generateUUID().substring(0, 4);
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verifyCode", verifyCode);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "牛客网——忘记密码", content);

        // 保存验证码到session里
        session.setAttribute("verifyCode", verifyCode);
        return CommunityUtil.getJSONString(0);
    }


    /**
     * 忘记密码，填写邮箱、验证码和新密码表单以此重置密码
     *
     * @param model
     * @param email
     * @param verifyCode
     * @param newPassword
     * @param session
     * @return
     */
    @RequestMapping(path = "/forget", method = RequestMethod.POST)
    public String forgetPassword(Model model, String email, String verifyCode, String newPassword, HttpSession session) {
        // 检查验证码
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(code) || StringUtils.isBlank("verifyCode") || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/forget";
        }

        // 检查邮箱，新密码
        Map<String, Object> map = userService.resetPassword(email, newPassword);
        // 如果重置密码成功，则重定向回登录页面
        if (map == null || map.isEmpty()) {
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }


}
