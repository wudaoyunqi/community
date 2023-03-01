package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_USER;

/**
 * @Projectname: community
 * @Filename: FollowController
 * @Author: yunqi
 * @Date: 2023/3/1 16:44
 * @Description: TODO
 */

@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 关注（异步请求）
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注！");
    }

    /**
     * 取消关注（异步请求）
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    /**
     * 查询某用户关注的人
     *
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            // 这里抛出的异常会被ExceptionAdvice捕获到
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.getFolloweeCount(userId, ENTITY_TYPE_USER));

        // 关注的人列表
        List<Map<String, Object>> userList = followService.getFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    /**
     * 查询某用户的粉丝
     *
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            // 这里抛出的异常会被ExceptionAdvice捕获到
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.getFollowerCount(ENTITY_TYPE_USER, userId));

        // 关注的人列表
        List<Map<String, Object>> userList = followService.getFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";
    }

    /**
     * 判断当前登录用户是否关注该用户
     * @param userId
     * @return
     */
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }


}
