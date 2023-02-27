package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.service.CommentService;
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

import java.lang.reflect.Array;
import java.util.*;


/**
 * @Projectname: community
 * @Filename: DiscussPostController
 * @Author: yunqi
 * @Date: 2023/2/26 14:08
 * @Description: TODO
 */

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;


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
     *
     * @param discussPostId 访问路径中的参数
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
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

        // 设置评论分页信息
        page.setLimit(5);
        page.setRows(discussPost.getCommentCount());
        page.setPath("/discuss/detail/" + discussPostId);

        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.getComments(ENTITY_TYPE_POST, discussPostId, page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 评论作者
                User commentUser = userService.getUserById(comment.getUserId());
                commentVo.put("commentUser", commentUser);

                // 回复列表
                List<Comment> replyList = commentService.getComments(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 回复VO
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 回复作者
                        User replyUser = userService.getUserById(reply.getUserId());
                        replyVo.put("replyUser", replyUser);
                        // 回复target对象
                        User replyTargetUser = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
                        replyVo.put("replyTargetUser", replyTargetUser);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                // 回复数量
                commentVo.put("replyRows", replyList == null ? 0 : replyList.size());

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }




}
