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
    private static final String PREFIX_KAPTCHA = "login:kaptcha";
    private static final String PREFIX_TOKEN = "login:token";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    // 存储更新帖子分数涉及到的postId
    private static final String PREFIX_POST = "post";

    private static final String PREFIX_HOT_POST = "hotPost";

    private static final String PREFIX_RATE_LIMIT_PUBLISH = "rate:limit:publish";


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

    // 登录验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTokenKey(String token) {
        return PREFIX_TOKEN + SPLIT + token;
    }

    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

    public static String getHotPostKey(int offset, int limit) {
        return PREFIX_HOT_POST + SPLIT + offset + SPLIT + limit;
    }

    public static String getRateLimitPublish(int userId) {
        return PREFIX_RATE_LIMIT_PUBLISH + SPLIT + userId;
    }

}
