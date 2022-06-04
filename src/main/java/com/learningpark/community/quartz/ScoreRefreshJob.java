package com.learningpark.community.quartz;

import com.learningpark.community.entity.DiscussPost;
import com.learningpark.community.service.DiscussPostService;
import com.learningpark.community.service.ElasticsearchService;
import com.learningpark.community.service.LikeService;
import com.learningpark.community.util.CommunityConstant;
import com.learningpark.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreRefreshJob implements Job, CommunityConstant {


    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 记录标准时间
    private static final Date EPOCH;

    static {
        try {
            EPOCH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化标准时间失败!", e);
        }

    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            LOGGER.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        LOGGER.info("[任务开始] 正在刷新帖子分数: " + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        LOGGER.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            LOGGER.error("该帖子不存在: id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //分数公式：log(加精分数75 + 评论数 * 10 + 点赞数 * 2) + (发布时间 - 标准时间)
        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - EPOCH.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }

}
