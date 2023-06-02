package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @Projectname: community
 * @Filename: SpringBootTests
 * @Author: yunqi
 * @Date: 2023/4/5 15:53
 * @Description: TODO
 */

//@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {
    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;


    /**
     * 该方法在类初始化之前执行，并只执行一次，所以是静态方法，可以在这里初始化数据
     */
    @BeforeAll
    public static void beforeClass() {
        System.out.println("before class");
    }

    /**
     * 可以在这里销毁数据
     */
    @AfterAll
    public static void afterClass() {
        System.out.println("after class");
    }

    /**
     * 在测试方法执行前调用，可以在这里初始化数据
     */
    @BeforeEach
    public void before() {
        System.out.println("before");
        //初始化测试数据
        data = new DiscussPost();
        data.setUserId(2);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    /**
     * 可以在这里销毁数据
     */
    @AfterEach
    public void after() {
        System.out.println("after");
        // 删除测试数据
        discussPostService.updateDiscussPostStatus(data.getId(), 2);
    }

    /**
     * 运行test1方法，控制台会输出before class --> before --> test1 --> after --> after class
     */
    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testGetById() {
        DiscussPost post = discussPostService.getDiscussPostById(data.getId());
        Assert.assertEquals(data.getTitle(), post.getTitle());
        Assert.assertEquals(data.getContent(), post.getContent());
        Assert.assertNotNull(post);
    }


    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateDiscussPostScore(data.getId(), 2000.00);
        Assert.assertEquals(1, rows);
        DiscussPost post = discussPostService.getDiscussPostById(data.getId());
        Assert.assertEquals(2000.00, post.getScore(), 2);
    }

}
