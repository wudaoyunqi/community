package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Projectname: community
 * @Filename: DiscussPostMapper
 * @Author: yunqi
 * @Date: 2023/2/23 17:15
 * @Description: TODO
 */

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param()用来给参数取别名，如果该方法只有一个参数，并且在<if>里使用（动态参数），则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

}
