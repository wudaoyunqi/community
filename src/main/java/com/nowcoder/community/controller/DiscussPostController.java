package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @Projectname: community
 * @Filename: DiscussPostController
 * @Author: yunqi
 * @Date: 2023/2/26 14:08
 * @Description: TODO
 */

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    /**
     * 发布帖子
     *
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        // 判断空值
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录！");
        }
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(403, "帖子标题或内容不能为空！");
        }

        // 发布帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 获取帖子详情
     * @param discussPostId 访问路径中的参数
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model) {
        // 帖子
        DiscussPost discussPost = discussPostService.getDiscussPostById(discussPostId);
        if (discussPost == null) {
            return "redirect:/site/error/404";
        }
        model.addAttribute("post", discussPost);

        // 第二次访问数据库获得用户信息，后续可以用redis来优化，提升查询性能
        // 作者
        User user = userService.getUserById(discussPost.getUserId());
        model.addAttribute("user", user);
        return "/site/discuss-detail";
    }


}
