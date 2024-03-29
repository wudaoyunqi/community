package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Projectname: community
 * @Filename: QuartzTests
 * @Author: yunqi
 * @Date: 2023/3/7 21:51
 * @Description: TODO
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTests {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDeleteJob() throws SchedulerException {
        boolean result = scheduler.deleteJob(new JobKey("wkImageDeleteJob", "communityJobGroup"));
        System.out.println(result);
    }

}
