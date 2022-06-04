package com.learningpark.community.config;

import com.learningpark.community.quartz.ScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置->数据库->调用 只在第一次调用，以后访问直接找数据库
@Configuration
public class QuartzConfig {

    //FactoryBean 简化Bean实例化过程
    // 通过FactoryBean封装bean的实例化过程；-> 将FactoryBean装配到Spring容器里
    // ->将FactoryBean注入给其他的bean ->该bean得到的是FactoryBean所管理的对象实例

    //刷新帖子分数任务

    //配置JobDetail
    @Bean
    public JobDetailFactoryBean scoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(ScoreRefreshJob.class);
        factoryBean.setName("scoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }
    // 配置Trigger(SimpleTriggerFactoryBean,CronTriggerFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean scoreRefreshTrigger(JobDetail scoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(scoreRefreshJobDetail);
        factoryBean.setName("scoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 3); // 三分钟刷新一遍
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

}
