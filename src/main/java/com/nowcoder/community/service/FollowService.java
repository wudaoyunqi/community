package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_USER;

/**
 * @Projectname: community
 * @Filename: FollowService
 * @Author: yunqi
 * @Date: 2023/3/1 15:41
 * @Description: TODO
 */

@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    /**
     * 取关
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    /**
     * 查询某用户关注的实体数量（一般显示关注用户数量）
     *
     * @param userId
     * @param entityType
     * @return
     */
    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询某实体的粉丝数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已关注该实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某用户关注的人
    public List<Map<String, Object>> getFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // reverseRange返回的是Set<V>，并且有序（不像jdk实现的是无序）
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, limit + offset - 1);
        // 用户没有关注其他人
        if (targetIds == null) {
            return null;
        } else {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Integer targetId : targetIds) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.getUserById(targetId);
                map.put("user", user);
                Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
                map.put("followTime", new Date(score.longValue()));
                list.add(map);
            }
            return list;
        }
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> getFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        // 用户没有粉丝
        if (targetIds == null) {
            return null;
        } else {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Integer targetId : targetIds) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.getUserById(targetId);
                map.put("user", user);
                Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
                map.put("followTime", new Date(score.longValue()));
                list.add(map);
            }
            return list;
        }

    }

}
