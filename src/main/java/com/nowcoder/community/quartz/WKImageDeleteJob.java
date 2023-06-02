package com.nowcoder.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @Projectname: community
 * @Filename: WKImageDeleteJob
 * @Author: yunqi
 * @Date: 2023/3/16 14:21
 * @Description: TODO
 */
//@Deprecated
//public class WKImageDeleteJob implements Job {
//    private static final Logger logger = LoggerFactory.getLogger(WKImageDeleteJob.class);
//    @Value("${wk.image.storage}")
//    private String wkImageStorage;
//
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        logger.info("[任务开始] 开始清理服务器上用户分享生成的长图...");
//        File[] files = new File(wkImageStorage).listFiles();
//        if (files == null || files.length == 0) {
//            logger.info("[任务取消] 没有WK图片！");
//            return;
//        }
//        for (File file : files) {
//            // 删除一分钟之前创建的图片
//            if (System.currentTimeMillis() - file.lastModified() > 60 * 1000) {
//                file.delete();
//                logger.info("[删除成功] 文件[{}]已删除！", file.getName());
//            }
//        }
//        logger.info("[任务完成] 用户长图清理结束！");
//    }
//}
