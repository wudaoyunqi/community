package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

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

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> getCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int getCommentRowsByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentRowsByEntity(entityType, entityId);
    }

    public Comment getCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    public List<Comment> getCommentsByUserId(int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUserId(userId, offset, limit);
    }

    public int getCommentRowsByUserId(int userId) {
        return commentMapper.selectCommentRowsByUserId(userId);
    }

    /**
     * 先增加评论，再更新帖子的评论数量
     *
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 转义html标记
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));

        // 过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        // 向数据库添加评论
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            discussPostService.updateCommentCount(comment.getEntityId(), getCommentRowsByEntity(comment.getEntityType(), comment.getEntityId()));
        }

        return rows;
    }

}
