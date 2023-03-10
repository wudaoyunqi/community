package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @Projectname: community
 * @Filename: MessageController
 * @Author: yunqi
 * @Date: 2023/2/27 17:47
 * @Description: TODO
 */

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 私信列表
     *
     * @param model
     * @param page
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
//        Integer.valueOf("abc");
        User user = hostHolder.getUser();
        // 设置分页信息
        page.setRows(messageService.getConversationCount(user.getId()));
        page.setPath("/letter/list");

        // 会话列表
        List<Message> conversationList = messageService.getConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        if (conversationVoList != null) {
            for (Message conversation : conversationList) {
                Map<String, Object> conversationVo = new HashMap<>();
                int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                User targetUser = userService.getUserById(targetId);
                conversationVo.put("targetUser", targetUser);
                conversationVo.put("conversation", conversation);
                conversationVo.put("letterCount", messageService.getLetterCount(conversation.getConversationId()));
                conversationVo.put("letterUnreadCount", messageService.getLetterUnreadCount(user.getId(), conversation.getConversationId()));
                conversationVoList.add(conversationVo);
            }
        }
        model.addAttribute("conversations", conversationVoList);
        model.addAttribute("conversationUnreadCount", messageService.getLetterUnreadCount(user.getId(), null));
        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }

    /**
     * 与某人的私信详情
     *
     * @param conversationId
     * @param model
     * @param page
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        User user = hostHolder.getUser();
        // 设置分页信息
        page.setRows(messageService.getLetterCount(conversationId));
        page.setPath("/letter/detail/" + conversationId);

        // 会话详情列表
        List<Message> letterList = messageService.getLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVoList = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> letterVo = new HashMap<>();
                User fromUser = userService.getUserById(letter.getFromId());
                letterVo.put("fromUser", fromUser);
                letterVo.put("letter", letter);
                letterVoList.add(letterVo);
            }
        }
        model.addAttribute("letters", letterVoList);
        // 私信目标
        model.addAttribute("targetUser", getLetterTarget(conversationId));
        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    /**
     * 获取私信的对象
     *
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.getUserById(id1);
        } else {
            return userService.getUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                if (hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 发送私信，异步请求
     *
     * @param targetName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String targetName, String content) {
//        Integer.valueOf("abc");
        User targetUser = userService.getUserByName(targetName);
        if (targetUser == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(targetUser.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 获取通知列表（评论、点赞、关注三个主题）
     *
     * @param model
     */
    @LoginRequired
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询三类通知
        getTopicNoticeList(model, TOPIC_COMMENT);
        getTopicNoticeList(model, TOPIC_LIKE);
        getTopicNoticeList(model, TOPIC_FOLLOW);

        // 查询未读消息数量
        int letterUnreadCount = messageService.getLetterUnreadCount(user.getId(), null);
        model.addAttribute("conversationUnreadCount", letterUnreadCount);

        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";

    }

    private void getTopicNoticeList(Model model, String topic) {
        User user = hostHolder.getUser();
        // 查询通知
        Message message = messageService.getLatestNotice(user.getId(), topic);
        Map<String, Object> messageVo = new HashMap<>();
        messageVo.put("message", message);
        if (message != null) {
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.getUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            if (!topic.equals(TOPIC_FOLLOW)) {
                messageVo.put("postId", data.get("postId"));
            }

            int count = messageService.getNoticeCount(user.getId(), topic);
            messageVo.put("count", count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(), topic);
            messageVo.put("unreadCount", unreadCount);
        }
        model.addAttribute(topic + "Notice", messageVo);
    }

    /**
     * 获取通知详情
     *
     * @param topic
     * @param model
     * @param page
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        User user = hostHolder.getUser();

        // 设置分页
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.getNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.getNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> noticeVo = new HashMap<>();
                noticeVo.put("notice", notice);
                // 内容非转义加转换为map
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                noticeVo.put("user", userService.getUserById((Integer) data.get("userId")));
                noticeVo.put("entityType", data.get("entityType"));
                noticeVo.put("entityId", data.get("entityId"));
                if (!topic.equals(TOPIC_FOLLOW)) {
                    noticeVo.put("postId", data.get("postId"));
                }
                noticeVo.put("fromUser", userService.getUserById(notice.getFromId()));

                noticeVoList.add(noticeVo);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";

    }


}
