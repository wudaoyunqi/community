package com.nowcoder.community.quartz;

import com.nowcoder.community.service.UserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Projectname: community
 * @Filename: AlphaJob
 * @Author: yunqi
 * @Date: 2023/3/7 21:15
 * @Description: TODO
 */
public class AlphaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println(Thread.currentThread().getName() + ": execute a quartz job");
    }
}
