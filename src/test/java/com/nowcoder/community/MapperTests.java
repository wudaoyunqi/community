package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * @Projectname: community
 * @Filename: MapperTests
 * @Author: yunqi
 * @Date: 2023/2/23 16:45
 * @Description: TODO
 */

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;


    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(8);
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("User6");
        user.setPassword("111111");
        user.setSalt("abc");
        user.setEmail("user6@qq.com");
        user.setType(0);
        user.setHeaderUrl("http://www.nowcoder.com/1010.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10, 1);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testLoginTicketMapper() {
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(1);
//        loginTicket.setTicket("abc");
//        loginTicket.setStatus(0);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

    }

    @Test
    public void testMessageMapper() {
        List<Message> list = messageMapper.selectConversations(2, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(1);
        System.out.println(count);

        list = messageMapper.selectLetters("2_3", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("2_3");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(2, "2_3");
        System.out.println(count);


    }

    @Test
    public void testSelectPostTime() {
        int offset = 280000, limit = 20;
        long start = System.currentTimeMillis();
//        discussPostMapper.selectDiscussPosts(offset, limit);
        long end = System.currentTimeMillis();
        System.out.printf("获取[%d, %d]行数据一共花了%d秒%n", offset, offset + limit, (end - start) / 1000);


        try {
            sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDiscussPostService() {
        DiscussPost data;
        data = new DiscussPost();
        data.setUserId(2);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
        DiscussPost post = discussPostService.getDiscussPostById(data.getId());
    }


}
