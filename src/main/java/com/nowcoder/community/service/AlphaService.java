package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDAO;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @Projectname: community
 * @Filename: AlphaService
 * @Author: yunqi
 * @Date: 2023/2/23 13:06
 * @Description: TODO
 */

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDAO alphaDAO;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    public String find() {
        return alphaDAO.select();
    }

    public AlphaService() {
//        System.out.println("实例化AlphaService");
    }

    // @PostConStruct指明该方法在构造器之后调用
    @PostConstruct
    public void init() {
//        System.out.println("初始化AlphaService");
    }

    // @PreDestroy指明该方法在销毁之前调用
    @PreDestroy
    public void destroy() {
//        System.out.println("销毁AlphaService");
    }

    /**
     * 对该方法进行声明式事务，让其内部操作成为一个整体，即事务
     * propagation（传播机制）：A事务调用了B事务，如何指定事务遵循的机制
     * REQUIRED: 支持当前事务（调用自己的外部事物），如果不存在则创建新事务
     * REQUIRED_NEW: 创建一个新事务，并且暂停当前事务（调用自己的外部事务）
     * NESTED: 如果当前存在事务（调用自己的外部事务），则嵌套在该事务中执行（独立的提交和回滚）
     *
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";

    }

    /**
     * 编程式事务
     *
     * @return
     */
    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("我是新人!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");

                return "ok";
            }
        });
    }


}
