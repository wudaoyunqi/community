package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

/**
 * @Projectname: community
 * @Filename: AlphaDAOHibernateImpl
 * @Author: yunqi
 * @Date: 2023/2/23 12:54
 * @Description: TODO
 */

// Repository是用来声明dao层的bean，括号里是bean的别名
@Repository("alphaHibernate")
public class AlphaDAOHibernateImpl implements AlphaDAO {
    @Override
    public String select() {
        return "Hibernate";
    }
}
