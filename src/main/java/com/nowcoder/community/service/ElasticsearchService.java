package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Projectname: community
 * @Filename: ElasticsearchService
 * @Author: yunqi
 * @Date: 2023/3/5 12:27
 * @Description: TODO
 */

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 新增或者修改文档（行记录）
     * 因为save方法传入的实体id已经存在的话，那么repository会执行覆盖操作；如果id不存在，那就是会新增一条文档（行记录）
     *
     * @param discussPost
     */
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        // 构建搜索条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(Sort.by(Sort.Direction.DESC, "type"))
                .withSort(Sort.by(Sort.Direction.DESC, "score"))
                .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        // 得到查询结果
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        // 将其结果进行分页
        SearchPage<DiscussPost> searchPage = SearchHitSupport.searchPageFor(search, searchQuery.getPageable());

        for (SearchHit<DiscussPost> hit : searchPage.getSearchHits()) {
            DiscussPost post = hit.getContent();
            // 处理高亮显示的结果
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            List<String> titleField = highlightFields.get("title");
            List<String> contentField = highlightFields.get("content");
            if (titleField != null) {
                post.setTitle(titleField.get(0));
            }
            if (contentField != null) {
                post.setContent(contentField.get(0));
            }
        }
        return searchPage;
    }

}
