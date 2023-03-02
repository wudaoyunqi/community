package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @Projectname: community
 * @Filename: LoginTicketMapper
 * @Author: yunqi
 * @Date: 2023/2/24 21:37
 * @Description: TODO
 */

@Mapper
@Deprecated
public interface LoginTicketMapper {
    String TABLE_NAME = "login_ticket";
    String INSERT_FIELDS = "user_id, ticket, status, expired";
    String SELECT_FIELDS = "id, " + INSERT_FIELDS;

    @Insert(
            "insert into " + TABLE_NAME + " (" + INSERT_FIELDS + ") values(#{userId}, #{ticket}, #{status}, #{expired})"
    )
//    @Options(useGeneratedKeys = true, keyProperty = "id")
        // 好像头条项目没有指定主键是哪个字段也能自动加1
    int insertLoginTicket(LoginTicket loginTicket);


    @Select(
            "select " + SELECT_FIELDS + " from " + TABLE_NAME + " where ticket = #{ticket}"
    )
    LoginTicket selectByTicket(String ticket);

    @Update(
            "<script>" +
                    "update " + TABLE_NAME + " set status = #{status} where ticket = #{ticket}" +
                    "<if test=\"ticket != null\">" +
                    "and 1=1" +
                    "</if>" +
                    "</script>"
    )
    int updateStatus(String ticket, int status);

}
