package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
//import com.nowcoder.community.quartz.WKImageDeleteJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @Projectname: community
 * @Filename: QuartzConfig
 * @Author: yunqi
 * @Date: 2023/3/7 21:17
 * @Description: TODO
 */

// 配置 -> 初始化数据库 -> 调用数据库读取数据
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程
    // 1, 通过FactoryBean封装了Bean的实例化过程
    // 2, 将FactoryBean装配到Spring容器里
    // 3, 将FactoryBean注入给其他的Bean（传Bean的名字），该Bean得到的是FactoryBean所管理的对象实例

    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);   // 持久化保存
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(alphaJobDetail);
        triggerFactoryBean.setName("alphaTrigger");
        triggerFactoryBean.setGroup("alphaTriggerGroup");
        triggerFactoryBean.setRepeatInterval(3000);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }

    /**
     * 帖子分数刷新任务
     *
     * @return
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);   // 持久化保存
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        triggerFactoryBean.setName("postScoreRefreshTrigger");
        triggerFactoryBean.setGroup("communityTriggerGroup");
        // 每隔三分钟刷新一次帖子分数
        triggerFactoryBean.setRepeatInterval(1000 * 60 * 3);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }

    /**
     * 删除WK图片任务
     *
     * @return
     */
//    @Bean
//    public JobDetailFactoryBean wkImageDeleteJobDetail() {
//        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//        factoryBean.setJobClass(WKImageDeleteJob.class);
//        factoryBean.setName("wkImageDeleteJob");
//        factoryBean.setGroup("communityJobGroup");
//        factoryBean.setDurability(true);
//        factoryBean.setRequestsRecovery(true);
//        return factoryBean;
//    }

//    @Bean
//    public SimpleTriggerFactoryBean wkImageDeleteTrigger(JobDetail wkImageDeleteJobDetail) {
//        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
//        triggerFactoryBean.setJobDetail(wkImageDeleteJobDetail);
//        triggerFactoryBean.setName("wkImageDeleteTrigger");
//        triggerFactoryBean.setGroup("communityTriggerGroup");
//        // 每隔四分钟删除一分钟之前由分享功能所创建的临时文件
//        triggerFactoryBean.setRepeatInterval(1000 * 60 * 4);
//        triggerFactoryBean.setJobDataMap(new JobDataMap());
//        return triggerFactoryBean;
//    }


}
