package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Projectname: community
 * @Filename: UserController
 * @Author: yunqi
 * @Date: 2023/2/25 15:14
 * @Description: TODO
 */

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private CommentService commentService;

    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.bucket.url}")
    private String qiniuImageDomain;

    /**
     * 打开网页时就设置七牛云相关配置
     *
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = "header/" + CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket, fileName, 3600, policy);
        model.addAttribute("upToken", upToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }


    /**
     * 更新头像链接
     *
     * @param fileName
     * @return
     */
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空！");
        }
        String newHeaderUrl = qiniuImageDomain + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), newHeaderUrl);
        return CommunityUtil.getJSONString(0);
    }


    /**
     * 上传头像
     *
     * @param headerImage
     * @param model
     * @return
     */
    @Deprecated
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 空值处理
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";    // 跳转回账号设置页
        }

        // 判断文件格式是否正确
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新当前用户的头像路径(web访问路径)
        // http://127.0.0.1:8080/community/user/header/xxxxx.png
        User user = hostHolder.getUser();
        String newHeaderUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), newHeaderUrl);

        return "redirect:/index";

    }


    /**
     * 获取头像
     * 返回的是图片数据的二进制流，所以不是返回模板也不是返回json数据，故返回类型为空，而不是String
     *
     * @param fileName
     * @param response
     */
    @Deprecated
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放地址
        String filePath = uploadPath + "/" + fileName;
        // 解析文件后缀
        String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filePath);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
//            os.close();
        }

    }


    /**
     * 更新密码
     *
     * @param model
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword, String newPassword) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {  // 更新密码成功，应该重定向回退出页，让用户重新登录
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }


    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 用户
        model.addAttribute("user", user);

        // 点赞数量
        int likeCount = likeService.getUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.getFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 当前登录用户是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    /**
     * 我的帖子
     *
     * @param userId
     * @param model
     * @return
     */
    @GetMapping(path = "/allpost/{userId}")
    public String getMyPostPage(@PathVariable("userId") int userId, Model model, Page page) {
        // keypoint 1. 判断用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // keypoint 2. 设置分页
        int rows = discussPostService.getDiscussPostRows(userId);
        page.setRows(rows);
        page.setPath("/user/allpost/" + userId);
        model.addAttribute("rows", rows);

        // keypoint 3. 获取用户发的帖子列表
        List<DiscussPost> list = discussPostService.getDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                Long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussVOList);
        return "/site/my-post";
    }

    /**
     * 我的回复
     *
     * @param userId
     * @param model
     * @param page
     * @return
     */
    @GetMapping(path = "/allreply/{userId}")
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page) {
        // keypoint 1. 判断用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // keypoint 2. 设置分页
        int rows = commentService.getCommentRowsByUserId(userId);
        page.setRows(rows);
        page.setPath("/user/allreply/" + userId);
        model.addAttribute("rows", rows);

        // keypoint 3. 回复列表
        List<Comment> list = commentService.getCommentsByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                // 显示评论/回复对应的文章信息
                // 如果是对帖子的评论，则直接利用entityId当做postId查询帖子
                if (comment.getEntityType() == ENTITY_TYPE_POST) {
                    DiscussPost post = discussPostService.getDiscussPostById(comment.getEntityId());
                    map.put("post", post);
                } else {
                    // 如果是对评论的回复，则先根据entityId当做commentId查询到评论记录，再根据其entityId作为postId查询帖子（仅限一层回复，没有楼中楼回复）
                    // 很巧的是，前端在记录对评论的回复的时候entityId = commentId，并不是replyId，所以可以查到回复的帖子hhhhh
                    // 至于评论层主或者楼中楼的时候，会记录一个targetId = commentUser / replyUser，所以发送通知的时候是正常的
                    Comment targetComment = commentService.getCommentById(comment.getEntityId());
                    DiscussPost post = discussPostService.getDiscussPostById(targetComment.getEntityId());
                    map.put("post", post);
                }
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments", commentVOList);
        return "/site/my-reply";
    }


}
