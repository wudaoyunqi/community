package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Projectname: community
 * @Filename: CommentMapper
 * @Author: yunqi
 * @Date: 2023/2/27 10:37
 * @Description: TODO
 */

@Mapper
public interface CommentMapper {
    String INSERT_FIELDS = "user_id, entity_type, entity_id, target_id, content, status, create_time";

    String SELECT_FIELDS = "id, " + INSERT_FIELDS;

    @Select(
            "select " + SELECT_FIELDS + " from comment " +
                    "where status = 0 and entity_type=#{entityType} and entity_id = #{entityId} order by create_time asc limit #{offset}, #{limit}"
    )
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    @Select(
            "select count(id) from comment where status = 0 and entity_type = #{entityType} and entity_id = #{entityId}"
    )
    int selectCommentRowsByEntity(int entityType, int entityId);

    @Insert(
            "insert into comment (" + INSERT_FIELDS + ") values(#{userId}, #{entityType}, #{entityId}, #{targetId}, #{content}, #{status}, #{createTime})"
    )
    int insertComment(Comment comment);


    @Select(
            "select " + SELECT_FIELDS + " from comment where id = #{id}"
    )
    Comment selectCommentById(int id);

    @Select(
            "select entity_type, entity_id, content, create_time from comment where user_id = #{userId} and status = 0 order by create_time desc limit #{offset}, #{limit}"
    )
    List<Comment> selectCommentsByUserId(int userId, int offset, int limit);

    @Select(
            "select count(id) from comment where user_id = #{userId} and status = 0"
    )
    int selectCommentRowsByUserId(int userId);

}
