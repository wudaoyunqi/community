package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Projectname: community
 * @Filename: CaffeineTests
 * @Author: yunqi
 * @Date: 2023/3/16 16:48
 * @Description: TODO
 */

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

    @Test
    public void initDataForTest() {
        List<DiscussPost> list = new ArrayList<>();
        DiscussPost post = new DiscussPost();
        post.setUserId(1);
        post.setTitle("互联网求职计划");
        post.setContent("                 金三银四的金三已经到了，你还沉浸在过年的喜悦中吗？" +
                "                                如果是，那我要让你清醒一下了：目前大部分公司已经开启了内推，正式网申也将在3月份陆续开始，金三银四，春招的求职黄金时期已经来啦！！！" +
                "                                再不准备，作为19应届生的你可能就找不到工作了。。。作为20届实习生的你可能就找不到实习了。。。" +
                "                                现阶段时间紧，任务重，能做到短时间内快速提升的也就只有算法了，" +
                "                                那么算法要怎么复习？重点在哪里？常见笔试面试算法题型和解题思路以及最优代码是怎样的？" +
                "                                跟左程云老师学算法，不仅能解决以上所有问题，还能在短时间内得到最大程度的提升！！！");
        post.setCreateTime(new Date());
        post.setScore(Math.random() * 2000);
        for (int i = 0; i < 10000; ++i) {
            list.add(post);
        }
        for (int i = 0; i < 30; ++i) {
            long start = System.currentTimeMillis();
            postService.addDiscussPostBatch(list);
            long end = System.currentTimeMillis();
            System.out.printf("第%d次插入10000条数据花了%d秒", i + 1, (end - start) / 1000);
            System.out.println();
        }
    }

    @Test
    public void testCache() {
        System.out.println(postService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.getDiscussPosts(0, 0, 10, 0));
    }

}
