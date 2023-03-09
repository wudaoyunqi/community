package com.nowcoder.community;


import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.elasticsearch.client.elc.QueryBuilders.matchQuery;

/**
 * @Projectname: community
 * @Filename: ElasticsearchTests
 * @Author: yunqi
 * @Date: 2023/3/3 20:51
 * @Description: TODO
 */

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testInsert() {
        // 插入文档数据，若没有索引会自动创建
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(1));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(2));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(3));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(4));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(8));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(1, 0, 10, 1));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(3, 0, 10, 1));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(4, 0, 10, 1));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(3);
        post.setContent("这是我的第二条帖子");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
        discussPostRepository.deleteById(3);
//        discussPostRepository.deleteAll();
    }

    /**
     * repository没有search方法，只能使用template调用search方法，或者RestHighLevelClient类
     */
    @Test
    public void testSearchByTemplate() {
        // 构建搜索条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("帖子", "title", "content"))
                .withSort(Sort.by(Sort.Direction.DESC, "type"))
                .withSort(Sort.by(Sort.Direction.DESC, "score"))
                .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();

        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        SearchPage<DiscussPost> searchPage = SearchHitSupport.searchPageFor(search, searchQuery.getPageable());

        long total = search.getTotalHits();
        List<DiscussPost> list = new ArrayList<>();
//        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        for (SearchHit<DiscussPost> hit : searchPage.getSearchHits()) {
            DiscussPost post = hit.getContent();
            // 处理高亮显示的结果
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            String titleField = highlightFields.get("title").get(0);
            String contentField = highlightFields.get("content").get(0);
            if (titleField != null) {
                post.setTitle(titleField);
            }
            if (contentField != null) {
                post.setContent(contentField);
            }
            list.add(post);
        }
        PageImpl<DiscussPost> pageInfo = new PageImpl<DiscussPost>(list, searchQuery.getPageable(), search.getTotalHits());

        for (DiscussPost post : pageInfo) {
//            System.out.println(post);
        }

    }


}
