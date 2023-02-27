package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Projectname: community
 * @Filename: CommentService
 * @Author: yunqi
 * @Date: 2023/2/27 10:51
 * @Description: TODO
 */

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> getComments(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int getCommentRows(int entityType, int entityId) {
        return commentMapper.selectCommentRowsByEntity(entityType, entityId);
    }

}
