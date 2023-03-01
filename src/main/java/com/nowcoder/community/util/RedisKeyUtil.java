package com.nowcoder.community.util;

import static com.nowcoder.community.util.CommunityConstant.entityIdtoEntityName;

/**
 * @Projectname: community
 * @Filename: RedisUtil
 * @Author: yunqi
 * @Date: 2023/2/28 21:53
 * @Description: TODO
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";


    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:'user'userid:'user'entityType -> zset(entityId, nowTime)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + "user" + userId + SPLIT + entityIdtoEntityName.get(entityType) + 's';
    }

    // 某个实体拥有的粉丝
    // follower:user/post:entityId -> zset(userId, nowTime)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityIdtoEntityName.get(entityType) + entityId;
    }

}
