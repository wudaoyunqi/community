package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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

    @PreDestroy
    public void destroy() {
//        System.out.println("销毁AlphaService");
    }


}
