package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Projectname: community
 * @Filename: MessageMapper
 * @Author: yunqi
 * @Date: 2023/2/27 16:20
 * @Description: TODO
 */

@Mapper
public interface MessageMapper {
    String INSERT_FIELDS = "from_id, to_id, conversation_id, content, status, create_time";
    String SELECT_FIELDS = "id, " + INSERT_FIELDS;

    // 查询当前用户的会话列表，针对每个会话只返回一条最新的一条私信
    @Select(
            "select " + SELECT_FIELDS + " from message where id in " +
                    "(select max(id) from message where status != 2 and from_id != 1 and (from_id = #{userId} or to_id = #{userId}) group by conversation_id)" +
                    "order by create_time desc limit #{offset}, #{limit}"
    )
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    @Select(
            "select count(distinct conversation_id) from message where status != 2 and from_id != 1 and (from_id = #{userId} or to_id = #{userId})"
    )
    int selectConversationCount(int userId);

    // 查询某个会话的详情列表
    @Select(
            "select " + SELECT_FIELDS + " from message " +
                    "where status != 2 and from_id != 1 and conversation_id = #{conversationId} order by create_time desc limit #{offset}, #{limit}"
    )
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    @Select(
            "select count(id) from message where status != 2 and from_id != 1 and conversation_id = #{conversationId}"
    )
    int selectLetterCount(String conversationId);

    // 查询未读私信数量
    @Select(
            "<script>" +
                    "select count(id) from message where status = 0 and from_id != 1 and to_id = #{userId}" +
                    "<if test=\"conversationId != null\">" +
                    "and conversation_id = #{conversationId}" +
                    "</if>" +
                    "</script>"
    )
    int selectLetterUnreadCount(int userId, @Param("conversationId") String conversationId);

    // 新增一条消息
    @Insert(
            "insert into message (" + INSERT_FIELDS + ") values(#{fromId}, #{toId}, #{conversationId}, #{content}, #{status}, #{createTime})"
    )
    int insertMessage(Message message);


    // 修改消息的状态
    @Update(
            "<script>" +
                    "update message set status = #{status} where id in " +
                    "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\" >" +
                    " #{id} " +
                    "</foreach>" +
                    "</script>"
    )
    int updateStatus(List<Integer> ids, int status);

    // 查询所有主题下最新的通知
    @Select(
            "select " + SELECT_FIELDS + " from message where id in " +
                    "(select max(id) from message where status != 2 and from_id = 1 and to_id = #{userId} and conversation_id = #{topic})"
    )
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题下的通知数量
    @Select(
            "select count(id) from message where status != 2 and from_id = 1 and to_id = #{userId} and conversation_id = #{topic}"
    )
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知数量
    @Select(
            "<script>" +
                    "select count(id) from message where status = 0 and from_id = 1 and to_id = #{userId}" +
                    "<if test=\"topic != null\">" +
                    "and conversation_id = #{topic}" +
                    "</if>" +
                    "</script>"
    )
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题的通知列表，支持分页
    @Select(
            "select " + SELECT_FIELDS +
                    " from message where status != 2 and from_id = 1 and to_id = #{userId} and conversation_id = #{topic} order by create_time desc limit #{offset}, #{limit}"
    )
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
