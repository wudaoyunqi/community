package com.nowcoder.community.service;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.util.RedisKeyUtil.getHotPostKey;

/**
 * @Projectname: community
 * @Filename: DiscussPostService
 * @Author: yunqi
 * @Date: 2023/2/23 17:33
 * @Description: TODO
 */

@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口：Cache, LoadingCache（同步缓存）, AsyncLoadingCache（异步缓存）
    // 帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> hotPostListCache;
    private Cache<String, List<DiscussPost>> postListCache2;

    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        hotPostListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if (key == null || StringUtils.isBlank(key)) {
                            throw new IllegalArgumentException("参数不允许为空!");
                        }
                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存：Redis -> mysql
//                        String hotPostListJson = (String) redisTemplate.opsForValue().get(getHotPostKey(offset, limit));
//                        if (Objects.nonNull(hotPostListJson)) {
//                            // Redis里存在
//                            System.out.println("load post list from redis");
//                            logger.info("load post list from redis");
//                            List<DiscussPost> hotPostList = JSONUtil.toList(hotPostListJson, DiscussPost.class);
//                            return hotPostList;
//                        }
//
//                        System.out.println("[Initialized post list] load post list from DB.");
                        logger.info("[Initialized post list] load post list from DB.");
                        List<DiscussPost> hotPostList = discussPostMapper.selectDiscussPosts(0, offset, limit, 1);

//                        System.out.println("write post list to redis");
//                        logger.info("write post list to redis");
//                        redisTemplate.opsForValue().set(getHotPostKey(offset, limit), JSONUtil.toJsonStr(hotPostList), expireSeconds + 10, TimeUnit.SECONDS);


                        return hotPostList;
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer integer) throws Exception {
                        logger.info("[Initialized post rows] load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(integer);
                    }
                });
    }


    public List<DiscussPost> getDiscussPosts(int userId, int offset, int limit, int orderMode) {
//        System.out.println("查询帖子列表: " + userId + " " + offset + " " + limit + " " + orderMode);
        // 当查询热门帖子列表时才进行缓存
//        if (userId == 0 && orderMode == 1) {
//            // 从本地缓存里取帖子数据
//            return hotPostListCache.get(offset + ":" + limit);
//        }
        logger.info("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int getDiscussPostRows(int userId) {
//        System.out.println("查询帖子总数，用户为: " + userId);
//        if (userId == 0) {
////            System.out.println("帖子总数为：" + postRowsCache.get(userId));
//            return postRowsCache.get(userId);
//        }
        logger.info("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 转义html标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public int addDiscussPostBatch(List<DiscussPost> list) {
        return discussPostMapper.insertDiscussPostBatch(list);
    }

    public DiscussPost getDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateDiscussPostType(int id, int type) {
        return discussPostMapper.updateDiscussPostType(id, type);
    }

    public int updateDiscussPostStatus(int id, int status) {
        return discussPostMapper.updateDiscussPostStatus(id, status);
    }

    public int updateDiscussPostScore(int id, double score) {
        return discussPostMapper.updateDiscussPostScore(id, score);
    }

}
